package clubevent;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField emailField = new JTextField(15);
    private JPasswordField passField = new JPasswordField(15);

    public LoginFrame() {
        setTitle("College Club - Login");
        setSize(320, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(230, 240, 250)); // Simple background color
        setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

        // Form fields
        add(new JLabel("Club Event Manager Login"));
        add(new JLabel("Email:"));
        add(emailField);
        add(new JLabel("Password:"));
        add(passField);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> handleLogin());
        add(loginBtn);

        // Quick demo buttons for easy presentation
        JButton studentDemoBtn = new JButton("Student Demo");
        studentDemoBtn.addActionListener(e -> {
            emailField.setText("alex@college.edu");
            passField.setText("student123");
        });

        JButton execomDemoBtn = new JButton("Execom Demo");
        execomDemoBtn.addActionListener(e -> {
            emailField.setText("sarah@college.edu");
            passField.setText("admin123");
        });

        add(studentDemoBtn);
        add(execomDemoBtn);
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword()).trim();
        try {
            Person p = DBManager.login(email, pass);
            if (p instanceof Student s) {
                new StudentFrame(s).setVisible(true);
                dispose();
            } else if (p instanceof ExecomMember em) {
                new ExecomFrame(em).setVisible(true);
                dispose();
            }
        } catch (InvalidLoginException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
