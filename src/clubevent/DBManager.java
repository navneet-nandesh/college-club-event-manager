package clubevent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    // Authenticate user against MySQL students or execom_members table
    public static Person login(String email, String password) throws InvalidLoginException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) throw new InvalidLoginException("Cannot connect to MySQL database.");

            // 1. Check students table
            PreparedStatement ps1 = con.prepareStatement("SELECT * FROM students WHERE email = ?");
            ps1.setString(1, email);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                Student s = new Student(
                    rs1.getString("id"), rs1.getString("name"),
                    rs1.getString("email"), rs1.getString("password"),
                    rs1.getString("department"), rs1.getInt("year")
                );
                s.login(email, password);
                return s;
            }

            // 2. Check execom_members table
            PreparedStatement ps2 = con.prepareStatement("SELECT * FROM execom_members WHERE email = ?");
            ps2.setString(1, email);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                ExecomMember em = new ExecomMember(
                    rs2.getString("id"), rs2.getString("name"),
                    rs2.getString("email"), rs2.getString("password"),
                    rs2.getString("designation")
                );
                em.login(email, password);
                return em;
            }
        } catch (SQLException e) {
            throw new InvalidLoginException("Database Error: " + e.getMessage());
        }
        throw new InvalidLoginException("No user found with email: " + email);
    }

    // Fetch all events from MySQL
    public static List<Event> getAllEvents() {
        List<Event> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return list;
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM events");
            while (rs.next()) {
                Event ev = new Event(
                    rs.getString("eventId"), rs.getString("name"),
                    rs.getString("date"), rs.getString("venue"), rs.getInt("maxSeats")
                );
                PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM registrations WHERE eventId = ?");
                ps.setString(1, ev.getEventId());
                ResultSet countRs = ps.executeQuery();
                if (countRs.next()) ev.setRegisteredCount(countRs.getInt(1));
                list.add(ev);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Register a student for an event
    public static void register(Student s, Event e) throws AlreadyRegisteredException, EventFullException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            PreparedStatement psCheck = con.prepareStatement(
                "SELECT * FROM registrations WHERE studentId = ? AND eventId = ?"
            );
            psCheck.setString(1, s.getId());
            psCheck.setString(2, e.getEventId());
            if (psCheck.executeQuery().next()) {
                throw new AlreadyRegisteredException("You are already registered for this event!");
            }
            if (e.seatsLeft() <= 0) {
                throw new EventFullException("Event is full! No seats left.");
            }
            String regId = "REG-" + (System.currentTimeMillis() % 10000);
            PreparedStatement psInsert = con.prepareStatement("INSERT INTO registrations VALUES (?, ?, ?, ?)");
            psInsert.setString(1, regId);
            psInsert.setString(2, s.getId());
            psInsert.setString(3, e.getEventId());
            psInsert.setBoolean(4, false);
            psInsert.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Add a new event
    public static void addEvent(Event ev) {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            PreparedStatement ps = con.prepareStatement("INSERT INTO events VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, ev.getEventId());
            ps.setString(2, ev.getName());
            ps.setString(3, ev.getDate());
            ps.setString(4, ev.getVenue());
            ps.setInt(5, ev.getMaxSeats());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Assign volunteer
    public static void assignVolunteer(String memberId, String eventId, String role) {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            String volId = "VOL-" + (System.currentTimeMillis() % 10000);
            PreparedStatement ps = con.prepareStatement("INSERT INTO volunteer_assignments VALUES (?, ?, ?, ?)");
            ps.setString(1, volId);
            ps.setString(2, memberId);
            ps.setString(3, eventId);
            ps.setString(4, role);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Print complete database traversal
    public static void traverseAll() {
        System.out.println("==================================================");
        System.out.println("   TRAVERSING COLLEGE CLUB EVENT DATABASE        ");
        System.out.println("==================================================");

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                System.out.println("MySQL is offline. Check connection to port 3306.");
                return;
            }

            System.out.println("\n[1] TRAVERSING EVENTS:");
            for (Event ev : getAllEvents()) {
                System.out.println("  - " + ev.getEventId() + ": " + ev.getName() 
                    + " | Date: " + ev.getDate() + " | Venue: " + ev.getVenue() 
                    + " | Seats: " + ev.getRegisteredCount() + "/" + ev.getMaxSeats() 
                    + " (Left: " + ev.seatsLeft() + ")");
            }

            System.out.println("\n[2] TRAVERSING REGISTRATIONS & PARTICIPANTS:");
            Statement st2 = con.createStatement();
            ResultSet rs2 = st2.executeQuery(
                "SELECT r.regId, s.name, s.department, e.name AS eventName, r.attended " +
                "FROM registrations r JOIN students s ON r.studentId = s.id " +
                "JOIN events e ON r.eventId = e.eventId"
            );
            while (rs2.next()) {
                System.out.println("  - " + rs2.getString("regId") + ": " 
                    + rs2.getString("name") + " (" + rs2.getString("department") + ")" 
                    + " -> " + rs2.getString("eventName") 
                    + " [Attended: " + (rs2.getBoolean("attended") ? "Yes" : "No") + "]");
            }

            System.out.println("\n[3] TRAVERSING VOLUNTEER ASSIGNMENTS:");
            Statement st3 = con.createStatement();
            ResultSet rs3 = st3.executeQuery(
                "SELECT v.volId, m.name, m.designation, e.name AS eventName, v.role " +
                "FROM volunteer_assignments v JOIN execom_members m ON v.memberId = m.id " +
                "JOIN events e ON v.eventId = e.eventId"
            );
            while (rs3.next()) {
                System.out.println("  - " + rs3.getString("volId") + ": " 
                    + rs3.getString("name") + " (" + rs3.getString("designation") + ")" 
                    + " as '" + rs3.getString("role") + "' for " + rs3.getString("eventName"));
            }

            System.out.println("\n==================================================");
            System.out.println("   TRAVERSAL FINISHED - LAUNCHING USER INTERFACE  ");
            System.out.println("==================================================\n");
        } catch (Exception ex) {
            System.out.println("Error traversing database: " + ex.getMessage());
        }
    }
}
