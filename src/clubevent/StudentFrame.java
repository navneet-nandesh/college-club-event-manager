package clubevent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentFrame extends JFrame {
    private Student student;
    private JTable table;
    private DefaultTableModel model;
    private List<Event> eventList;

    public StudentFrame(Student student) {
        this.student = student;
        setTitle("Student Portal - " + student.getName());
        setSize(650, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(240, 250, 240)); // Simple background color
        setLayout(new BorderLayout(10, 10));

        // Top Header
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(220, 240, 220));
        topPanel.add(new JLabel("Welcome Student: " + student.getName() + " (" + student.getDepartment() + ")"));
        add(topPanel, BorderLayout.NORTH);

        // Events Table
        String[] cols = {"Event ID", "Name", "Date", "Venue", "Max Seats", "Seats Left"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        loadEvents();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Button Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(220, 240, 220));

        JButton registerBtn = new JButton("Register for Event");
        registerBtn.addActionListener(e -> registerForSelectedEvent());
        bottomPanel.add(registerBtn);

        JButton infoBtn = new JButton("Display Info");
        infoBtn.addActionListener(e -> {
            student.displayInfo();
            JOptionPane.showMessageDialog(this, "Name: " + student.getName() + "\nDept: " + student.getDepartment() + "\nYear: " + student.getYear());
        });
        bottomPanel.add(infoBtn);

        JButton traverseBtn = new JButton("Traverse Console");
        traverseBtn.addActionListener(e -> DBManager.traverseAll());
        bottomPanel.add(traverseBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        bottomPanel.add(logoutBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadEvents() {
        model.setRowCount(0);
        eventList = DBManager.getAllEvents();
        for (Event ev : eventList) {
            model.addRow(new Object[]{
                ev.getEventId(), ev.getName(), ev.getDate(), ev.getVenue(), ev.getMaxSeats(), ev.seatsLeft()
            });
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
            loadEvents(); // Refresh seatsLeft
        } catch (AlreadyRegisteredException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Already Registered", JOptionPane.WARNING_MESSAGE);
        } catch (EventFullException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Event Full", JOptionPane.ERROR_MESSAGE);
        }
    }
}
