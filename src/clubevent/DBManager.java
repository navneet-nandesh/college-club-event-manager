package clubevent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    // Helper method to safely close database resources in finally blocks
    private static void close(ResultSet rs, Statement st, Connection con) {
        try {
            if (rs != null) rs.close();
            if (st != null) st.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            System.out.println("Error closing resource: " + e.getMessage());
        }
    }

    // Authenticate user against students or execom_members table
    public static Person login(String email, String password) throws InvalidLoginException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            if (con == null) {
                throw new InvalidLoginException("Cannot connect to MySQL database.");
            }

            // 1. Check students table
            ps = con.prepareStatement("SELECT * FROM students WHERE email = ?");
            ps.setString(1, email);
            rs = ps.executeQuery();
            if (rs.next()) {
                Student s = new Student(
                    rs.getString("id"), rs.getString("name"),
                    rs.getString("email"), rs.getString("password"),
                    rs.getString("department"), rs.getInt("year")
                );
                s.login(email, password);
                return s;
            }
            rs.close();
            ps.close();

            // 2. Check execom_members table
            ps = con.prepareStatement("SELECT * FROM execom_members WHERE email = ?");
            ps.setString(1, email);
            rs = ps.executeQuery();
            if (rs.next()) {
                ExecomMember em = new ExecomMember(
                    rs.getString("id"), rs.getString("name"),
                    rs.getString("email"), rs.getString("password"),
                    rs.getString("designation")
                );
                em.login(email, password);
                return em;
            }
        } catch (SQLException e) {
            throw new InvalidLoginException("Database Error: " + e.getMessage());
        } finally {
            close(rs, ps, con);
        }
        throw new InvalidLoginException("No user found with email: " + email);
    }

    // Fetch all events from MySQL
    public static List<Event> getAllEvents() {
        List<Event> list = new ArrayList<Event>();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            if (con == null) return list;

            st = con.createStatement();
            rs = st.executeQuery("SELECT * FROM events");
            while (rs.next()) {
                Event ev = new Event(
                    rs.getString("eventId"), rs.getString("name"),
                    rs.getString("date"), rs.getString("venue"), rs.getInt("maxSeats")
                );

                PreparedStatement countPs = null;
                ResultSet countRs = null;
                try {
                    countPs = con.prepareStatement("SELECT COUNT(*) FROM registrations WHERE eventId = ?");
                    countPs.setString(1, ev.getEventId());
                    countRs = countPs.executeQuery();
                    if (countRs.next()) {
                        ev.setRegisteredCount(countRs.getInt(1));
                    }
                } finally {
                    close(countRs, countPs, null);
                }
                list.add(ev);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching events: " + e.getMessage());
        } finally {
            close(rs, st, con);
        }
        return list;
    }

    // Register a student for an event
    public static void register(Student s, Event e) throws AlreadyRegisteredException, EventFullException {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            if (con == null) return;

            ps = con.prepareStatement("SELECT * FROM registrations WHERE studentId = ? AND eventId = ?");
            ps.setString(1, s.getId());
            ps.setString(2, e.getEventId());
            rs = ps.executeQuery();
            if (rs.next()) {
                throw new AlreadyRegisteredException("You are already registered for this event!");
            }
            if (e.seatsLeft() <= 0) {
                throw new EventFullException("Event is full! No seats left.");
            }
            rs.close();
            ps.close();

            String regId = "REG-" + (System.currentTimeMillis() % 10000);
            ps = con.prepareStatement("INSERT INTO registrations VALUES (?, ?, ?, ?)");
            ps.setString(1, regId);
            ps.setString(2, s.getId());
            ps.setString(3, e.getEventId());
            ps.setBoolean(4, false);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("SQL error: " + ex.getMessage());
        } finally {
            close(rs, ps, con);
        }
    }

    // Add a new event
    public static void addEvent(Event ev) {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            if (con == null) return;

            ps = con.prepareStatement("INSERT INTO events VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, ev.getEventId());
            ps.setString(2, ev.getName());
            ps.setString(3, ev.getDate());
            ps.setString(4, ev.getVenue());
            ps.setInt(5, ev.getMaxSeats());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding event: " + e.getMessage());
        } finally {
            close(null, ps, con);
        }
    }

    // Assign volunteer to an event
    public static void assignVolunteer(String memberId, String eventId, String role) {
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            if (con == null) return;

            String volId = "VOL-" + (System.currentTimeMillis() % 10000);
            ps = con.prepareStatement("INSERT INTO volunteer_assignments VALUES (?, ?, ?, ?)");
            ps.setString(1, volId);
            ps.setString(2, memberId);
            ps.setString(3, eventId);
            ps.setString(4, role);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error assigning volunteer: " + e.getMessage());
        } finally {
            close(null, ps, con);
        }
    }
}
