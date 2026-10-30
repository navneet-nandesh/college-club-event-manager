package clubevent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ParticipantsFrame extends JFrame implements ActionListener {
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

        // 1. NORTH: Header using FlowLayout
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(new Color(245, 225, 235));
        topPanel.add(new JLabel("Participants for: " + event.getName()));
        cPane.add(topPanel, BorderLayout.NORTH);

        // 2. CENTER: JTable inside JScrollPane
        String[] cols = new String[]{"Reg ID", "Student Name", "Department", "Year", "Attended"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        loadParticipants();
        cPane.add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. SOUTH: Controls using FlowLayout
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
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = DBConnection.getConnection();
            if (con == null) return;

            ps = con.prepareStatement(
                "SELECT r.regId, s.name, s.department, s.year, r.attended " +
                "FROM registrations r JOIN students s ON r.studentId = s.id WHERE r.eventId = ?"
            );
            ps.setString(1, event.getEventId());
            rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("regId"),
                    rs.getString("name"),
                    rs.getString("department"),
                    Integer.valueOf(rs.getInt("year")),
                    rs.getBoolean("attended") ? "Yes" : "No"
                });
            }
        } catch (SQLException ex) {
            System.out.println("Error loading participants: " + ex.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
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

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = DBConnection.getConnection();
            if (con == null) return;

            ps = con.prepareStatement("UPDATE registrations SET attended = ? WHERE regId = ?");
            ps.setBoolean(1, !currentStatus);
            ps.setString(2, regId);
            ps.executeUpdate();

            loadParticipants();
            JOptionPane.showMessageDialog(this, "Attendance updated!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}
