package clubevent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginFrame extends JFrame implements ActionListener {
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

        // 1. NORTH: Title using FlowLayout
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(new Color(210, 230, 250));
        titlePanel.add(new JLabel("Club Event Manager Login"));
        cPane.add(titlePanel, BorderLayout.NORTH);

        // 2. CENTER: Compact Form with reduced size text fields
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

        // 3. SOUTH: Buttons using FlowLayout
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
