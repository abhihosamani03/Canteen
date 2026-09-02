import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * LoginFrame provides the authentication screen for Students
 * and a gateway for Admins.
 */
public class LoginFrame extends JFrame {
    private JTextField txtStudentId;
    private JPasswordField txtPhone;
    private JButton btnLogin;
    private JButton btnAdminLogin;

    public LoginFrame() {
        setTitle("Sahyadri Food Court - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Panel with background color
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250)); // Light light-blue background
        setContentPane(mainPanel);

        // Header Panel (Premium Blue Header with Logo)
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(44, 62, 80)); // Deep Navy
        headerPanel.setPreferredSize(new Dimension(550, 80));
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
        
        // Load Logo Icon
        ImageIcon logoIcon = new ImageIcon("docs/sahyadri-logo.png");
        if (logoIcon.getIconWidth() > 0) {
            Image img = logoIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            JLabel lblLogo = new JLabel(new ImageIcon(img));
            headerPanel.add(lblLogo);
        }
        
        JLabel lblTitle = new JLabel("SAHYADRI FOOD COURT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);

        // Student ID Label & Input
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblStudentId = new JLabel("Student ID:");
        lblStudentId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStudentId.setForeground(new Color(52, 73, 94));
        formPanel.add(lblStudentId, gbc);

        gbc.gridx = 1;
        txtStudentId = new JTextField(15);
        txtStudentId.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtStudentId.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        formPanel.add(txtStudentId, gbc);

        // Phone Label & Input
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblPhone = new JLabel("Phone No:");
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPhone.setForeground(new Color(52, 73, 94));
        formPanel.add(lblPhone, gbc);

        gbc.gridx = 1;
        txtPhone = new JPasswordField(15);
        txtPhone.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPhone.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        formPanel.add(txtPhone, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons Panel (Login, Register & Admin)
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        btnLogin = new JButton("Student Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(39, 174, 96)); // Emerald Green
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(150, 40));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnRegister = new JButton("New Register");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegister.setBackground(new Color(41, 128, 185)); // Blue
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setPreferredSize(new Dimension(150, 40));
        btnRegister.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAdminLogin = new JButton("Admin Portal");
        btnAdminLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdminLogin.setBackground(new Color(127, 140, 141)); // Slate Gray
        btnAdminLogin.setForeground(Color.WHITE);
        btnAdminLogin.setFocusPainted(false);
        btnAdminLogin.setPreferredSize(new Dimension(150, 40));
        btnAdminLogin.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        btnAdminLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        footerPanel.add(btnLogin);
        footerPanel.add(btnRegister);
        footerPanel.add(btnAdminLogin);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        // Add Event Listeners
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleStudentLogin();
            }
        });

        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleStudentRegistration();
            }
        });

        btnAdminLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAdminLogin();
            }
        });
    }

    private void handleStudentLogin() {
        String studentIdStr = txtStudentId.getText().trim();
        String phoneStr = new String(txtPhone.getPassword()).trim();

        // 1. Validation
        if (studentIdStr.isEmpty() || phoneStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both Student ID and Phone Number.", 
                "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Query Database
        String query = "SELECT name, wallet_balance FROM Student WHERE student_id = ? AND phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, studentIdStr);
            ps.setString(2, phoneStr);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    double walletBalance = rs.getDouble("wallet_balance");

                    JOptionPane.showMessageDialog(this, 
                        "Welcome, " + name + "!", 
                        "Login Success", JOptionPane.INFORMATION_MESSAGE);

                    // Redirect to MenuFrame
                    new MenuFrame(studentIdStr, name, walletBalance).setVisible(true);
                    this.dispose(); // Close login frame
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Invalid Student ID or Phone Number.", 
                        "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Database error: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void handleAdminLogin() {
        // Admin credentials for simulation or standard pass prompt
        String pass = JOptionPane.showInputDialog(this, 
            "Enter Admin Password:", 
            "Admin Authentication", JOptionPane.QUESTION_MESSAGE);
        
        if (pass == null) return; // User cancelled
        
        if (pass.equals("admin123")) {
            JOptionPane.showMessageDialog(this, 
                "Admin access granted.", 
                "Login Success", JOptionPane.INFORMATION_MESSAGE);
            new AdminFrame().setVisible(true);
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Incorrect Admin Password.", 
                "Access Denied", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleStudentRegistration() {
        JDialog regDialog = new JDialog(this, "New Student Registration", true);
        regDialog.setSize(420, 380);
        regDialog.setLocationRelativeTo(this);
        regDialog.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        regDialog.setContentPane(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // Student ID
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblRegId = new JLabel("College Roll No / ID:");
        lblRegId.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblRegId, gbc);
        
        gbc.gridx = 1;
        JTextField txtRegId = new JTextField(15);
        txtRegId.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(txtRegId, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblName = new JLabel("Full Name:");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblName, gbc);
        
        gbc.gridx = 1;
        JTextField txtName = new JTextField(15);
        txtName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(txtName, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblEmail, gbc);
        
        gbc.gridx = 1;
        JTextField txtEmail = new JTextField(15);
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(txtEmail, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblRegPhone = new JLabel("Phone No:");
        lblRegPhone.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblRegPhone, gbc);
        
        gbc.gridx = 1;
        JTextField txtRegPhone = new JTextField(15);
        txtRegPhone.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(txtRegPhone, gbc);

        // Initial Balance
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblBal = new JLabel("Initial Wallet (₹):");
        lblBal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lblBal, gbc);
        
        gbc.gridx = 1;
        JSpinner spinBalance = new JSpinner(new SpinnerNumberModel(200.00, 0.00, 10000.00, 50.00));
        spinBalance.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(spinBalance, gbc);

        // Register Button
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JButton btnSubmit = new JButton("Register & Create Account");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSubmit.setBackground(new Color(46, 204, 113));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String id = txtRegId.getText().trim();
                String name = txtName.getText().trim();
                String email = txtEmail.getText().trim();
                String phone = txtRegPhone.getText().trim();
                double initialBalance = (Double) spinBalance.getValue();

                if (id.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                    JOptionPane.showMessageDialog(regDialog, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Enforce College USN format (e.g. 4SF24CI005)
                String usnPattern = "^4SF\\d{2}(CI|CS|EC|ME)\\d{3}$";
                if (!id.toUpperCase().matches(usnPattern)) {
                    JOptionPane.showMessageDialog(regDialog, 
                        "Invalid Student ID format!\n" +
                        "Must match your college seat number (USN) format: 4SF24CI005\n\n" +
                        "- College Code: 4SF\n" +
                        "- Year of Admission: e.g. 24\n" +
                        "- Department: CI (AIML), CS (CSE), EC (ECE), ME (ME)\n" +
                        "- Sequence Number: 3 digits (starts from 000)", 
                        "USN Format Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Force to uppercase for database uniformity
                String finalId = id.toUpperCase();

                String insertQuery = "INSERT INTO Student (student_id, name, email, phone, wallet_balance) VALUES (?, ?, ?, ?, ?)";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                    
                    ps.setString(1, finalId);
                    ps.setString(2, name);
                    ps.setString(3, email);
                    ps.setString(4, phone);
                    ps.setDouble(5, initialBalance);
                    
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(regDialog,
                            "Registration Successful!\n\n" +
                            "Welcome to Sahyadri Food Court, " + name + "!\n" +
                            "Use ID: " + finalId + " and Phone: " + phone + " to log in.",
                            "Registration Completed", JOptionPane.INFORMATION_MESSAGE);
                        regDialog.dispose();
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(regDialog, "Error saving registration: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
        panel.add(btnSubmit, gbc);

        regDialog.setVisible(true);
    }
}
