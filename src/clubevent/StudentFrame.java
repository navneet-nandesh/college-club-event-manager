package clubevent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class StudentFrame extends JFrame implements ActionListener {
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

        // 1. NORTH: Welcome Header using FlowLayout
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(220, 240, 220));
        JLabel welcomeLabel = new JLabel("Welcome Student: " + student.getName() + " (" + student.getDepartment() + ")");
        topPanel.add(welcomeLabel);
        cPane.add(topPanel, BorderLayout.NORTH);

        // 2. CENTER: Events Table inside JScrollPane
        String[] cols = new String[]{"Event ID", "Name", "Date", "Venue", "Max Seats", "Seats Left"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        loadEvents();
        cPane.add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. SOUTH: Bottom Control Panel using FlowLayout
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

        // Multiple catch clauses: subclass exceptions before superclass exception
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
