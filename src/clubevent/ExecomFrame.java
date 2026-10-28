package clubevent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ExecomFrame extends JFrame {
    private ExecomMember member;
    private JTable table;
    private DefaultTableModel model;
    private List<Event> eventList;

    public ExecomFrame(ExecomMember member) {
        this.member = member;
        setTitle("Execom Dashboard - " + member.getName());
        setSize(700, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(255, 245, 235)); // Simple background color
        setLayout(new BorderLayout(10, 10));

        // Top Header
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(255, 235, 215));
        topPanel.add(new JLabel("Execom Member: " + member.getName() + " (" + member.getDesignation() + ")"));
        add(topPanel, BorderLayout.NORTH);

        // Events Table
        String[] cols = {"Event ID", "Name", "Date", "Venue", "Max Seats", "Seats Left"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        loadEvents();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom Controls
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(255, 235, 215));

        JButton addEventBtn = new JButton("Add Event");
        addEventBtn.addActionListener(e -> addNewEvent());
        bottomPanel.add(addEventBtn);

        JButton viewParticipantsBtn = new JButton("View Participants");
        viewParticipantsBtn.addActionListener(e -> openParticipants());
        bottomPanel.add(viewParticipantsBtn);

        JButton assignVolBtn = new JButton("Assign Volunteer");
        assignVolBtn.addActionListener(e -> assignVolunteer());
        bottomPanel.add(assignVolBtn);

        JButton infoBtn = new JButton("Display Info");
        infoBtn.addActionListener(e -> {
            member.displayInfo();
            JOptionPane.showMessageDialog(this, "Name: " + member.getName() + "\nRole: " + member.getDesignation());
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
        if (name == null || name.trim().isEmpty()) return;
        String date = JOptionPane.showInputDialog(this, "Enter Date (e.g. 2026-11-20):");
        String venue = JOptionPane.showInputDialog(this, "Enter Venue:");
        String seats = JOptionPane.showInputDialog(this, "Enter Max Seats:");
        try {
            Event ev = new Event(id, name, date, venue, Integer.parseInt(seats));
            DBManager.addEvent(ev);
            JOptionPane.showMessageDialog(this, "Event Added Successfully!");
            loadEvents();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding event: " + ex.getMessage());
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
        if (role != null && !role.trim().isEmpty()) {
            DBManager.assignVolunteer(member.getId(), ev.getEventId(), role);
            JOptionPane.showMessageDialog(this, "Volunteer role '" + role + "' assigned to " + member.getName() + "!");
        }
    }
}
