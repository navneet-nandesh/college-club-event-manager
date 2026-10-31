package clubevent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

// ============================================================================
// 1. INTERFACES & CUSTOM EXCEPTIONS (PPT Exception Handling)
// ============================================================================

interface Authenticable {
    boolean login(String email, String password) throws InvalidLoginException;
}

class InvalidLoginException extends Exception {
    public InvalidLoginException(String message) {
        super(message);
    }
}

class EventFullException extends Exception {
    public EventFullException(String message) {
        super(message);
    }
}

class AlreadyRegisteredException extends Exception {
    public AlreadyRegisteredException(String message) {
        super(message);
    }
}

// ============================================================================
// 2. OOP MODELS (Inheritance: Person -> Student, ExecomMember)
// ============================================================================

abstract class Person {
    protected String id;
    protected String name;
    protected String email;
    protected String password;

    public Person(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public abstract void displayInfo();

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}

class Student extends Person implements Authenticable {
    private String department;
    private int year;

    public Student(String id, String name, String email, String password, String department, int year) {
        super(id, name, email, password);
        this.department = department;
        this.year = year;
    }

    public String getDepartment() { return department; }
    public int getYear() { return year; }

    @Override
    public void displayInfo() {
        System.out.println("Student: " + name + " | Dept: " + department + " | Year: " + year);
    }

    @Override
    public boolean login(String inputEmail, String inputPassword) throws InvalidLoginException {
        if (this.email.equalsIgnoreCase(inputEmail) && this.password.equals(inputPassword)) {
            return true;
        }
        throw new InvalidLoginException("Incorrect email or password for " + this.name);
    }
}

class ExecomMember extends Person implements Authenticable {
    private String designation;

    public ExecomMember(String id, String name, String email, String password, String designation) {
        super(id, name, email, password);
        this.designation = designation;
    }

    public String getDesignation() { return designation; }

    @Override
    public void displayInfo() {
        System.out.println("Execom Member: " + name + " | Role: " + designation);
    }

    @Override
    public boolean login(String inputEmail, String inputPassword) throws InvalidLoginException {
        if (this.email.equalsIgnoreCase(inputEmail) && this.password.equals(inputPassword)) {
            return true;
        }
        throw new InvalidLoginException("Incorrect email or password for " + this.name);
    }
}

class Event {
    private String eventId;
    private String name;
    private String date;
    private String venue;
    private int maxSeats;
    private int registeredCount;

    public Event(String eventId, String name, String date, String venue, int maxSeats) {
        this.eventId = eventId;
        this.name = name;
        this.date = date;
        this.venue = venue;
        this.maxSeats = maxSeats;
        this.registeredCount = 0;
    }

    public int seatsLeft() {
        int left = maxSeats - registeredCount;
        return left < 0 ? 0 : left;
    }

    public String getEventId() { return eventId; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getVenue() { return venue; }
    public int getMaxSeats() { return maxSeats; }
    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int count) { this.registeredCount = count; }
}

// ============================================================================
// 3. DATABASE OPERATIONS (Simple JDBC)
// ============================================================================

class DBManager {
    private static final String URL = "jdbc:mysql://localhost:3306/clubevent_db";
    private static final String USER = "root";
    private static final String PASSWORD = "qwerty123";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.out.println("Database connection error: " + e.getMessage());
            return null;
        }
    }

    public static Person login(String email, String password) throws InvalidLoginException {
        Connection con = getConnection();
        if (con == null) {
            throw new InvalidLoginException("Cannot connect to MySQL database.");
        }
        try {
            // Check student table
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
                con.close();
                return s;
            }

            // Check execom_members table
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
                con.close();
                return em;
            }
            con.close();
        } catch (SQLException e) {
            throw new InvalidLoginException("Database error: " + e.getMessage());
        }
        throw new InvalidLoginException("No user found with email: " + email);
    }

    public static List<Event> getAllEvents() {
        List<Event> list = new ArrayList<Event>();
        Connection con = getConnection();
        if (con == null) return list;
        try {
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
                if (countRs.next()) {
                    ev.setRegisteredCount(countRs.getInt(1));
                }
                list.add(ev);
            }
            con.close();
        } catch (Exception e) {
            System.out.println("Error fetching events: " + e.getMessage());
        }
        return list;
    }

    public static void register(Student s, Event e) throws AlreadyRegisteredException, EventFullException {
        Connection con = getConnection();
        if (con == null) return;
        try {
            PreparedStatement psCheck = con.prepareStatement("SELECT * FROM registrations WHERE studentId = ? AND eventId = ?");
            psCheck.setString(1, s.getId());
            psCheck.setString(2, e.getEventId());
            if (psCheck.executeQuery().next()) {
                con.close();
                throw new AlreadyRegisteredException("You are already registered for this event!");
            }
            if (e.seatsLeft() <= 0) {
                con.close();
                throw new EventFullException("Event is full! No seats left.");
            }
            String regId = "REG-" + (System.currentTimeMillis() % 10000);
            PreparedStatement ps = con.prepareStatement("INSERT INTO registrations VALUES (?, ?, ?, ?)");
            ps.setString(1, regId);
            ps.setString(2, s.getId());
            ps.setString(3, e.getEventId());
            ps.setBoolean(4, false);
            ps.executeUpdate();
            con.close();
        } catch (SQLException ex) {
            System.out.println("SQL error during registration: " + ex.getMessage());
        }
    }

    public static void addEvent(Event ev) {
        Connection con = getConnection();
        if (con == null) return;
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO events VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, ev.getEventId());
            ps.setString(2, ev.getName());
            ps.setString(3, ev.getDate());
            ps.setString(4, ev.getVenue());
            ps.setInt(5, ev.getMaxSeats());
            ps.executeUpdate();
            con.close();
        } catch (Exception e) {
            System.out.println("Error adding event: " + e.getMessage());
        }
    }

    public static void assignVolunteer(String memberId, String eventId, String role) {
        Connection con = getConnection();
        if (con == null) return;
        try {
            String volId = "VOL-" + (System.currentTimeMillis() % 10000);
            PreparedStatement ps = con.prepareStatement("INSERT INTO volunteer_assignments VALUES (?, ?, ?, ?)");
            ps.setString(1, volId);
            ps.setString(2, memberId);
            ps.setString(3, eventId);
            ps.setString(4, role);
            ps.executeUpdate();
            con.close();
        } catch (Exception e) {
            System.out.println("Error assigning volunteer: " + e.getMessage());
        }
    }
}

// ============================================================================
// 4. GUI FRAMES (Swing + Layout Managers + Delegation Event Model)
// ============================================================================

class LoginFrame extends JFrame implements ActionListener {
    private JTextField emailField;
    private JPasswordField passField;
    private JButton loginBtn;
    private JButton exitBtn;

    public LoginFrame() {
        setTitle("College Club - Login");
        setSize(320, 180);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container cPane = getContentPane();
        cPane.setLayout(new BorderLayout(5, 5));
        cPane.setBackground(new Color(230, 240, 250));

        // Top Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(new Color(210, 230, 250));
        titlePanel.add(new JLabel("Club Event Manager Login"));
        cPane.add(titlePanel, BorderLayout.NORTH);

        // Center Form
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        centerPanel.setBackground(new Color(230, 240, 250));

        JPanel formGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        formGrid.setBackground(new Color(230, 240, 250));

        formGrid.add(new JLabel("Email:"));
        emailField = new JTextField(10);
        formGrid.add(emailField);

        formGrid.add(new JLabel("Password:"));
        passField = new JPasswordField(10);
        formGrid.add(passField);

        centerPanel.add(formGrid);
        cPane.add(centerPanel, BorderLayout.CENTER);

        // Bottom Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        btnPanel.setBackground(new Color(210, 230, 250));

        loginBtn = new JButton("Login");
        loginBtn.addActionListener(this);
        btnPanel.add(loginBtn);

        exitBtn = new JButton("Exit");
        exitBtn.addActionListener(this);
        btnPanel.add(exitBtn);

        cPane.add(btnPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            handleLogin();
        } else if (e.getSource() == exitBtn) {
            System.exit(0);
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        try {
            Person p = DBManager.login(email, pass);
            if (p instanceof Student) {
                Student s = (Student) p;
                new StudentFrame(s).setVisible(true);
                dispose();
            } else if (p instanceof ExecomMember) {
                ExecomMember em = (ExecomMember) p;
                new ExecomFrame(em).setVisible(true);
                dispose();
            }
        } catch (InvalidLoginException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class StudentFrame extends JFrame implements ActionListener {
    private Student student;
    private JTable table;
    private DefaultTableModel model;
    private List<Event> eventList;

    private JButton registerBtn;
    private JButton infoBtn;
    private JButton logoutBtn;

    public StudentFrame(Student student) {
        this.student = student;
        setTitle("Student Portal - " + student.getName());
        setSize(650, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container cPane = getContentPane();
        cPane.setLayout(new BorderLayout(10, 10));
        cPane.setBackground(new Color(240, 250, 240));

        // Top Header
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(220, 240, 220));
        topPanel.add(new JLabel("Welcome Student: " + student.getName() + " (" + student.getDepartment() + ")"));
        cPane.add(topPanel, BorderLayout.NORTH);

        // Center Table
        String[] cols = new String[]{"Event ID", "Name", "Date", "Venue", "Max Seats", "Seats Left"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        loadEvents();
        cPane.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(new Color(220, 240, 220));

        registerBtn = new JButton("Register for Event");
        registerBtn.addActionListener(this);
        bottomPanel.add(registerBtn);

        infoBtn = new JButton("View Info");
        infoBtn.addActionListener(this);
        bottomPanel.add(infoBtn);

        logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(this);
        bottomPanel.add(logoutBtn);

        cPane.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadEvents() {
        model.setRowCount(0);
        eventList = DBManager.getAllEvents();
        for (int i = 0; i < eventList.size(); i++) {
            Event ev = eventList.get(i);
            model.addRow(new Object[]{
                ev.getEventId(),
                ev.getName(),
                ev.getDate(),
                ev.getVenue(),
                Integer.valueOf(ev.getMaxSeats()),
                Integer.valueOf(ev.seatsLeft())
            });
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerBtn) {
            registerForSelectedEvent();
        } else if (e.getSource() == infoBtn) {
            student.displayInfo();
            JOptionPane.showMessageDialog(this,
                "Student ID: " + student.getId() + "\nName: " + student.getName()
                + "\nDepartment: " + student.getDepartment() + "\nYear: " + student.getYear(),
                "Student Information",
                JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getSource() == logoutBtn) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    private void registerForSelectedEvent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event from the table first.");
            return;
        }

        Event selectedEvent = eventList.get(row);

        try {
            DBManager.register(student, selectedEvent);
            JOptionPane.showMessageDialog(this, "Registration Successful for " + selectedEvent.getName() + "!");
            loadEvents();
        } catch (AlreadyRegisteredException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Already Registered", JOptionPane.WARNING_MESSAGE);
        } catch (EventFullException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Event Full", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class ExecomFrame extends JFrame implements ActionListener {
    private ExecomMember member;
    private JTable table;
    private DefaultTableModel model;
    private List<Event> eventList;

    private JButton addEventBtn;
    private JButton viewParticipantsBtn;
    private JButton assignVolBtn;
    private JButton infoBtn;
    private JButton logoutBtn;

    public ExecomFrame(ExecomMember member) {
        this.member = member;
        setTitle("Execom Dashboard - " + member.getName());
        setSize(700, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container cPane = getContentPane();
        cPane.setLayout(new BorderLayout(10, 10));
        cPane.setBackground(new Color(255, 245, 235));

        // Top Header
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(255, 235, 215));
        topPanel.add(new JLabel("Execom Member: " + member.getName() + " (" + member.getDesignation() + ")"));
        cPane.add(topPanel, BorderLayout.NORTH);

        // Center Table
        String[] cols = new String[]{"Event ID", "Name", "Date", "Venue", "Max Seats", "Seats Left"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        loadEvents();
        cPane.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(new Color(255, 235, 215));

        addEventBtn = new JButton("Add Event");
        addEventBtn.addActionListener(this);
        bottomPanel.add(addEventBtn);

        viewParticipantsBtn = new JButton("View Participants");
        viewParticipantsBtn.addActionListener(this);
        bottomPanel.add(viewParticipantsBtn);

        assignVolBtn = new JButton("Assign Volunteer");
        assignVolBtn.addActionListener(this);
        bottomPanel.add(assignVolBtn);

        infoBtn = new JButton("View Info");
        infoBtn.addActionListener(this);
        bottomPanel.add(infoBtn);

        logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(this);
        bottomPanel.add(logoutBtn);

        cPane.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadEvents() {
        model.setRowCount(0);
        eventList = DBManager.getAllEvents();
        for (int i = 0; i < eventList.size(); i++) {
            Event ev = eventList.get(i);
            model.addRow(new Object[]{
                ev.getEventId(),
                ev.getName(),
                ev.getDate(),
                ev.getVenue(),
                Integer.valueOf(ev.getMaxSeats()),
                Integer.valueOf(ev.seatsLeft())
            });
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addEventBtn) {
            addNewEvent();
        } else if (e.getSource() == viewParticipantsBtn) {
            openParticipants();
        } else if (e.getSource() == assignVolBtn) {
            assignVolunteer();
        } else if (e.getSource() == infoBtn) {
            member.displayInfo();
            JOptionPane.showMessageDialog(this,
                "Execom ID: " + member.getId() + "\nName: " + member.getName()
                + "\nDesignation: " + member.getDesignation() + "\nEmail: " + member.getEmail(),
                "Execom Information",
                JOptionPane.INFORMATION_MESSAGE);
        } else if (e.getSource() == logoutBtn) {
            new LoginFrame().setVisible(true);
            dispose();
        }
    }

    private void openParticipants() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event from the table first.");
            return;
        }
        Event selectedEvent = eventList.get(row);
        new ParticipantsFrame(selectedEvent).setVisible(true);
    }

    private void addNewEvent() {
        String id = "EVT-" + (System.currentTimeMillis() % 1000);
        String name = JOptionPane.showInputDialog(this, "Enter Event Name:");
        if (name == null || name.trim().length() == 0) return;
        String date = JOptionPane.showInputDialog(this, "Enter Date (e.g. 2026-11-20):");
        String venue = JOptionPane.showInputDialog(this, "Enter Venue:");
        String seats = JOptionPane.showInputDialog(this, "Enter Max Seats:");

        try {
            int maxSeats = Integer.parseInt(seats);
            Event ev = new Event(id, name, date, venue, maxSeats);
            DBManager.addEvent(ev);
            JOptionPane.showMessageDialog(this, "Event Added Successfully!");
            loadEvents();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number for seats: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding event: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void assignVolunteer() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event to assign a volunteer.");
            return;
        }
        Event ev = eventList.get(row);
        String role = JOptionPane.showInputDialog(this, "Enter Volunteer Role for " + ev.getName() + ":");
        if (role != null && role.trim().length() > 0) {
            DBManager.assignVolunteer(member.getId(), ev.getEventId(), role);
            JOptionPane.showMessageDialog(this, "Volunteer role '" + role + "' assigned to " + member.getName() + "!");
        }
    }
}

class ParticipantsFrame extends JFrame implements ActionListener {
    private Event event;
    private JTable table;
    private DefaultTableModel model;

    private JButton toggleBtn;
    private JButton closeBtn;

    public ParticipantsFrame(Event event) {
        this.event = event;
        setTitle("Participants - " + event.getName());
        setSize(600, 380);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        Container cPane = getContentPane();
        cPane.setLayout(new BorderLayout(10, 10));
        cPane.setBackground(new Color(255, 240, 245));

        // Top Header
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(245, 225, 235));
        topPanel.add(new JLabel("Participants for: " + event.getName()));
        cPane.add(topPanel, BorderLayout.NORTH);

        // Center Table
        String[] cols = new String[]{"Reg ID", "Student Name", "Department", "Year", "Attended"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        loadParticipants();
        cPane.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Controls
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(new Color(245, 225, 235));

        toggleBtn = new JButton("Toggle Attendance");
        toggleBtn.addActionListener(this);
        bottomPanel.add(toggleBtn);

        closeBtn = new JButton("Close");
        closeBtn.addActionListener(this);
        bottomPanel.add(closeBtn);

        cPane.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadParticipants() {
        model.setRowCount(0);
        Connection con = DBManager.getConnection();
        if (con == null) return;
        try {
            PreparedStatement ps = con.prepareStatement(
                "SELECT r.regId, s.name, s.department, s.year, r.attended " +
                "FROM registrations r JOIN students s ON r.studentId = s.id WHERE r.eventId = ?"
            );
            ps.setString(1, event.getEventId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("regId"),
                    rs.getString("name"),
                    rs.getString("department"),
                    Integer.valueOf(rs.getInt("year")),
                    rs.getBoolean("attended") ? "Yes" : "No"
                });
            }
            con.close();
        } catch (SQLException ex) {
            System.out.println("Error loading participants: " + ex.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == toggleBtn) {
            toggleAttendance();
        } else if (e.getSource() == closeBtn) {
            dispose();
        }
    }

    private void toggleAttendance() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a participant to toggle attendance.");
            return;
        }

        String regId = (String) model.getValueAt(row, 0);
        boolean currentStatus = "Yes".equals(model.getValueAt(row, 4));

        Connection con = DBManager.getConnection();
        if (con == null) return;
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE registrations SET attended = ? WHERE regId = ?");
            ps.setBoolean(1, !currentStatus);
            ps.setString(2, regId);
            ps.executeUpdate();
            con.close();

            loadParticipants();
            JOptionPane.showMessageDialog(this, "Attendance updated!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// ============================================================================
// 5. MAIN ENTRY POINT
// ============================================================================

public class ClubEventManager {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
