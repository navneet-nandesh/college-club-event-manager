package clubevent;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

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
// 2. DOMAIN MODEL CLASSES (PPT Inheritance & Encapsulation)
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
// 3. DATABASE MANAGER (JDBC & Queries)
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

    public static Student loginStudent(String email, String password) throws InvalidLoginException {
        Connection con = getConnection();
        if (con == null) {
            throw new InvalidLoginException("Cannot connect to MySQL database.");
        }
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM students WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Student s = new Student(
                    rs.getString("id"), rs.getString("name"),
                    rs.getString("email"), rs.getString("password"),
                    rs.getString("department"), rs.getInt("year")
                );
                s.login(email, password);
                con.close();
                return s;
            }
            con.close();
        } catch (SQLException e) {
            throw new InvalidLoginException("Database error: " + e.getMessage());
        }
        throw new InvalidLoginException("No student found with email: " + email);
    }

    public static ExecomMember loginExecom(String email, String password) throws InvalidLoginException {
        Connection con = getConnection();
        if (con == null) {
            throw new InvalidLoginException("Cannot connect to MySQL database.");
        }
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM execom_members WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ExecomMember em = new ExecomMember(
                    rs.getString("id"), rs.getString("name"),
                    rs.getString("email"), rs.getString("password"),
                    rs.getString("designation")
                );
                em.login(email, password);
                con.close();
                return em;
            }
            con.close();
        } catch (SQLException e) {
            throw new InvalidLoginException("Database error: " + e.getMessage());
        }
        throw new InvalidLoginException("No execom member found with email: " + email);
    }

    public static void registerStudent(Student s) throws Exception {
        Connection con = getConnection();
        if (con == null) {
            throw new Exception("Cannot connect to MySQL database.");
        }
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO students VALUES (?, ?, ?, ?, ?, ?)");
            ps.setString(1, s.getId());
            ps.setString(2, s.getName());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPassword());
            ps.setString(5, s.getDepartment());
            ps.setInt(6, s.getYear());
            ps.executeUpdate();
            con.close();
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("duplicate")) {
                throw new Exception("Student ID or Email already exists!");
            }
            throw new Exception("Registration error: " + e.getMessage());
        }
    }

    public static void registerExecomMember(ExecomMember em) throws Exception {
        Connection con = getConnection();
        if (con == null) {
            throw new Exception("Cannot connect to MySQL database.");
        }
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO execom_members VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, em.getId());
            ps.setString(2, em.getName());
            ps.setString(3, em.getEmail());
            ps.setString(4, em.getPassword());
            ps.setString(5, em.getDesignation());
            ps.executeUpdate();
            con.close();
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("duplicate")) {
                throw new Exception("Member ID or Email already exists!");
            }
            throw new Exception("Registration error: " + e.getMessage());
        }
    }

    public static ArrayList<Event> getAllEvents() {
        ArrayList<Event> list = new ArrayList<Event>();
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
// 4. GUI FRAMES (PPT Swing, Layout Managers, Delegation Event Model)
// ============================================================================

// --- 4.1 ROLE SELECTION / WELCOME FRAME ---
class RoleSelectFrame extends JFrame implements ActionListener {
    private JButton studentBtn;
    private JButton execomBtn;
    private JButton exitBtn;

    public RoleSelectFrame() {
        setTitle("College Club Event Manager");
        setSize(380, 240);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container cPane = getContentPane();
        cPane.setLayout(new BorderLayout(5, 5));
        cPane.setBackground(new Color(230, 240, 250));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(new Color(30, 60, 110));
        JLabel titleLabel = new JLabel("College Club Event Manager");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        titlePanel.add(titleLabel);
        cPane.add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        centerPanel.setBackground(new Color(230, 240, 250));

        JPanel grid = new JPanel(new GridLayout(3, 1, 10, 10));
        grid.setBackground(new Color(230, 240, 250));

        studentBtn = new JButton("Student Portal (Login / Register)");
        studentBtn.addActionListener(this);
        grid.add(studentBtn);

        execomBtn = new JButton("Execom Portal (Login / Register)");
        execomBtn.addActionListener(this);
        grid.add(execomBtn);

        exitBtn = new JButton("Exit Application");
        exitBtn.addActionListener(this);
        grid.add(exitBtn);

        centerPanel.add(grid);
        cPane.add(centerPanel, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == studentBtn) {
            new StudentLoginFrame().setVisible(true);
            dispose();
        } else if (e.getSource() == execomBtn) {
            new ExecomLoginFrame().setVisible(true);
            dispose();
        } else if (e.getSource() == exitBtn) {
            System.exit(0);
        }
    }
}

// --- 4.2 STUDENT LOGIN FRAME ---
class StudentLoginFrame extends JFrame implements ActionListener {
    private JTextField emailField;
    private JPasswordField passField;
    private JButton loginBtn;
    private JButton registerBtn;
    private JButton backBtn;

    public StudentLoginFrame() {
        setTitle("Student Login");
        setSize(360, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container cPane = getContentPane();
        cPane.setLayout(new BorderLayout(5, 5));
        cPane.setBackground(new Color(230, 240, 250));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(new Color(30, 60, 110));
        JLabel titleLabel = new JLabel("Student Login Portal");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titlePanel.add(titleLabel);
        cPane.add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 12));
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

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btnPanel.setBackground(new Color(210, 230, 250));

        loginBtn = new JButton("Login");
        loginBtn.addActionListener(this);
        btnPanel.add(loginBtn);

        registerBtn = new JButton("Register");
        registerBtn.addActionListener(this);
        btnPanel.add(registerBtn);

        backBtn = new JButton("Back");
        backBtn.addActionListener(this);
        btnPanel.add(backBtn);

        cPane.add(btnPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            handleLogin();
        } else if (e.getSource() == registerBtn) {
            new StudentRegisterFrame().setVisible(true);
            dispose();
        } else if (e.getSource() == backBtn) {
            new RoleSelectFrame().setVisible(true);
            dispose();
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Student s = DBManager.loginStudent(email, pass);
            new StudentFrame(s).setVisible(true);
            dispose();
        } catch (InvalidLoginException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// --- 4.3 STUDENT REGISTRATION FRAME ---
class StudentRegisterFrame extends JFrame implements ActionListener {
    private JTextField idField;
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passField;
    private JTextField deptField;
    private JTextField yearField;

    private JButton registerBtn;
    private JButton cancelBtn;

    public StudentRegisterFrame() {
        setTitle("Register New Student");
        setSize(380, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container cPane = getContentPane();
        cPane.setLayout(new BorderLayout(5, 5));
        cPane.setBackground(new Color(230, 240, 250));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(new Color(30, 60, 110));
        JLabel titleLabel = new JLabel("New Student Registration");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titlePanel.add(titleLabel);
        cPane.add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        centerPanel.setBackground(new Color(230, 240, 250));

        JPanel formGrid = new JPanel(new GridLayout(6, 2, 8, 8));
        formGrid.setBackground(new Color(230, 240, 250));

        formGrid.add(new JLabel("Student ID:"));
        idField = new JTextField(10);
        formGrid.add(idField);

        formGrid.add(new JLabel("Full Name:"));
        nameField = new JTextField(10);
        formGrid.add(nameField);

        formGrid.add(new JLabel("Email:"));
        emailField = new JTextField(10);
        formGrid.add(emailField);

        formGrid.add(new JLabel("Password:"));
        passField = new JPasswordField(10);
        formGrid.add(passField);

        formGrid.add(new JLabel("Department:"));
        deptField = new JTextField(10);
        formGrid.add(deptField);

        formGrid.add(new JLabel("Year (1-4):"));
        yearField = new JTextField(10);
        formGrid.add(yearField);

        centerPanel.add(formGrid);
        cPane.add(centerPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        btnPanel.setBackground(new Color(210, 230, 250));

        registerBtn = new JButton("Register");
        registerBtn.addActionListener(this);
        btnPanel.add(registerBtn);

        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(this);
        btnPanel.add(cancelBtn);

        cPane.add(btnPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerBtn) {
            handleRegister();
        } else if (e.getSource() == cancelBtn) {
            new StudentLoginFrame().setVisible(true);
            dispose();
        }
    }

    private void handleRegister() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        String dept = deptField.getText().trim();
        String yearStr = yearField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty() || dept.isEmpty() || yearStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int year = 1;
        try {
            year = Integer.parseInt(yearStr);
            if (year < 1 || year > 5) {
                JOptionPane.showMessageDialog(this, "Year must be a number between 1 and 5.", "Invalid Year", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year must be a valid integer.", "Invalid Year", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Student s = new Student(id, name, email, pass, dept, year);
            DBManager.registerStudent(s);
            JOptionPane.showMessageDialog(this, "Student registered successfully! Please log in.");
            new StudentLoginFrame().setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// --- 4.4 EXECOM LOGIN FRAME ---
class ExecomLoginFrame extends JFrame implements ActionListener {
    private JTextField emailField;
    private JPasswordField passField;
    private JButton loginBtn;
    private JButton registerBtn;
    private JButton backBtn;

    public ExecomLoginFrame() {
        setTitle("Execom Member Login");
        setSize(360, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container cPane = getContentPane();
        cPane.setLayout(new BorderLayout(5, 5));
        cPane.setBackground(new Color(230, 240, 250));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(new Color(30, 60, 110));
        JLabel titleLabel = new JLabel("Execom Member Login Portal");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titlePanel.add(titleLabel);
        cPane.add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 12));
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

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btnPanel.setBackground(new Color(210, 230, 250));

        loginBtn = new JButton("Login");
        loginBtn.addActionListener(this);
        btnPanel.add(loginBtn);

        registerBtn = new JButton("Register");
        registerBtn.addActionListener(this);
        btnPanel.add(registerBtn);

        backBtn = new JButton("Back");
        backBtn.addActionListener(this);
        btnPanel.add(backBtn);

        cPane.add(btnPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            handleLogin();
        } else if (e.getSource() == registerBtn) {
            new ExecomRegisterFrame().setVisible(true);
            dispose();
        } else if (e.getSource() == backBtn) {
            new RoleSelectFrame().setVisible(true);
            dispose();
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ExecomMember em = DBManager.loginExecom(email, pass);
            new ExecomFrame(em).setVisible(true);
            dispose();
        } catch (InvalidLoginException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// --- 4.5 EXECOM REGISTRATION FRAME ---
class ExecomRegisterFrame extends JFrame implements ActionListener {
    private JTextField idField;
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passField;
    private JTextField desigField;

    private JButton registerBtn;
    private JButton cancelBtn;

    public ExecomRegisterFrame() {
        setTitle("Register New Execom Member");
        setSize(380, 290);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container cPane = getContentPane();
        cPane.setLayout(new BorderLayout(5, 5));
        cPane.setBackground(new Color(230, 240, 250));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(new Color(30, 60, 110));
        JLabel titleLabel = new JLabel("New Execom Member Registration");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titlePanel.add(titleLabel);
        cPane.add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        centerPanel.setBackground(new Color(230, 240, 250));

        JPanel formGrid = new JPanel(new GridLayout(5, 2, 8, 8));
        formGrid.setBackground(new Color(230, 240, 250));

        formGrid.add(new JLabel("Member ID:"));
        idField = new JTextField(10);
        formGrid.add(idField);

        formGrid.add(new JLabel("Full Name:"));
        nameField = new JTextField(10);
        formGrid.add(nameField);

        formGrid.add(new JLabel("Email:"));
        emailField = new JTextField(10);
        formGrid.add(emailField);

        formGrid.add(new JLabel("Password:"));
        passField = new JPasswordField(10);
        formGrid.add(passField);

        formGrid.add(new JLabel("Designation:"));
        desigField = new JTextField(10);
        formGrid.add(desigField);

        centerPanel.add(formGrid);
        cPane.add(centerPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        btnPanel.setBackground(new Color(210, 230, 250));

        registerBtn = new JButton("Register");
        registerBtn.addActionListener(this);
        btnPanel.add(registerBtn);

        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(this);
        btnPanel.add(cancelBtn);

        cPane.add(btnPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerBtn) {
            handleRegister();
        } else if (e.getSource() == cancelBtn) {
            new ExecomLoginFrame().setVisible(true);
            dispose();
        }
    }

    private void handleRegister() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        String desig = desigField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty() || desig.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ExecomMember em = new ExecomMember(id, name, email, pass, desig);
            DBManager.registerExecomMember(em);
            JOptionPane.showMessageDialog(this, "Execom Member registered successfully! Please log in.");
            new ExecomLoginFrame().setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// --- 4.6 STUDENT MAIN PORTAL FRAME ---
class StudentFrame extends JFrame implements ActionListener {
    private Student student;
    private JTable table;
    private DefaultTableModel model;
    private ArrayList<Event> eventList;

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
        cPane.setBackground(new Color(240, 248, 255));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(70, 130, 180));
        JLabel headerLabel = new JLabel("Welcome, " + student.getName() + " (" + student.getDepartment() + ")");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(headerLabel);
        cPane.add(topPanel, BorderLayout.NORTH);

        String[] cols = new String[]{"Event ID", "Name", "Date", "Venue", "Max Seats", "Seats Left"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        loadEvents();
        cPane.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(new Color(220, 235, 245));

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
            new RoleSelectFrame().setVisible(true);
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

// --- 4.7 EXECOM MAIN PORTAL FRAME ---
class ExecomFrame extends JFrame implements ActionListener {
    private ExecomMember member;
    private JTable table;
    private DefaultTableModel model;
    private ArrayList<Event> eventList;

    private JButton addEventBtn;
    private JButton viewParticipantsBtn;
    private JButton assignVolBtn;
    private JButton infoBtn;
    private JButton logoutBtn;

    public ExecomFrame(ExecomMember member) {
        this.member = member;
        setTitle("Execom Portal - " + member.getName());
        setSize(700, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container cPane = getContentPane();
        cPane.setLayout(new BorderLayout(10, 10));
        cPane.setBackground(new Color(245, 245, 250));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(60, 90, 150));
        JLabel headerLabel = new JLabel("Execom Dashboard: " + member.getName() + " (" + member.getDesignation() + ")");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(headerLabel);
        cPane.add(topPanel, BorderLayout.NORTH);

        String[] cols = new String[]{"Event ID", "Name", "Date", "Venue", "Max Seats", "Seats Left"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        loadEvents();
        cPane.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(new Color(230, 230, 240));

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
            new RoleSelectFrame().setVisible(true);
            dispose();
        }
    }

    private void openParticipants() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event to view participants.");
            return;
        }
        Event selectedEvent = eventList.get(row);
        new ParticipantsFrame(selectedEvent).setVisible(true);
    }

    private void addNewEvent() {
        JTextField idField = new JTextField(8);
        JTextField nameField = new JTextField(8);
        JTextField dateField = new JTextField(8);
        JTextField venueField = new JTextField(8);
        JTextField seatsField = new JTextField(8);

        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.add(new JLabel("Event ID:"));
        panel.add(idField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Date:"));
        panel.add(dateField);
        panel.add(new JLabel("Venue:"));
        panel.add(venueField);
        panel.add(new JLabel("Max Seats:"));
        panel.add(seatsField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Event", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            int seats = Integer.parseInt(seatsField.getText().trim());
            Event ev = new Event(
                idField.getText().trim(),
                nameField.getText().trim(),
                dateField.getText().trim(),
                venueField.getText().trim(),
                seats
            );
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

// --- 4.8 PARTICIPANTS FRAME ---
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

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(245, 225, 235));
        topPanel.add(new JLabel("Participants for: " + event.getName()));
        cPane.add(topPanel, BorderLayout.NORTH);

        String[] cols = new String[]{"Reg ID", "Student Name", "Department", "Year", "Attended"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        loadParticipants();
        cPane.add(new JScrollPane(table), BorderLayout.CENTER);

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
                new RoleSelectFrame().setVisible(true);
            }
        });
    }
}
