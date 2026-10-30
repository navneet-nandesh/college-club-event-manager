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

public class ExecomFrame extends JFrame implements ActionListener {
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

        // 1. NORTH: Welcome Header using FlowLayout
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(255, 235, 215));
        JLabel headerLabel = new JLabel("Execom Member: " + member.getName() + " (" + member.getDesignation() + ")");
        topPanel.add(headerLabel);
        cPane.add(topPanel, BorderLayout.NORTH);

        // 2. CENTER: Table inside JScrollPane
        String[] cols = new String[]{"Event ID", "Name", "Date", "Venue", "Max Seats", "Seats Left"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        loadEvents();
        cPane.add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. SOUTH: Action Buttons using FlowLayout
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
