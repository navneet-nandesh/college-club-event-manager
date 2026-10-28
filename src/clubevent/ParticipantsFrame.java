package clubevent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ParticipantsFrame extends JFrame {
    private Event event;
    private JTable table;
    private DefaultTableModel model;

    public ParticipantsFrame(Event event) {
        this.event = event;
        setTitle("Participants - " + event.getName());
        setSize(550, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(255, 240, 245)); // Simple background color
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(245, 225, 235));
        topPanel.add(new JLabel("Participants for: " + event.getName()));
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Reg ID", "Student Name", "Department", "Year", "Attended"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        loadParticipants();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Controls
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(245, 225, 235));

        JButton toggleBtn = new JButton("Toggle Attendance");
        toggleBtn.addActionListener(e -> toggleAttendance());
        bottomPanel.add(toggleBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadParticipants() {
        model.setRowCount(0);
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT r.regId, s.name, s.department, s.year, r.attended " +
                "FROM registrations r JOIN students s ON r.studentId = s.id WHERE r.eventId = ?"
            );
            ps.setString(1, event.getEventId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("regId"), rs.getString("name"),
                    rs.getString("department"), rs.getInt("year"),
                    rs.getBoolean("attended") ? "Yes" : "No"
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "UPDATE registrations SET attended = ? WHERE regId = ?"
            );
            ps.setBoolean(1, !currentStatus);
            ps.setString(2, regId);
            ps.executeUpdate();
            loadParticipants();
            JOptionPane.showMessageDialog(this, "Attendance updated!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
