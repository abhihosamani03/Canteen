import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * AdminFrame provides the daily sales reporting dashboard
 * along with student insights, trends, and detailed orders.
 */
public class AdminFrame extends JFrame {
    private JSpinner dateSpinner;
    private JTable tblReports;
    private DefaultTableModel reportsModel;
    private JTable tblOrderDetails;
    private DefaultTableModel orderDetailsModel;

    // Student insights components
    private JComboBox<StudentComboItem> cmbStudents;
    private JLabel lblTotalOrders;
    private JLabel lblTotalSpent;
    private JLabel lblAvgSpent;
    private JLabel lblFavItem;
    private JLabel lblBranch;
    private DefaultTableModel studentOrdersModel;

    // Counter table reporting components
    private JTable tblCounterSales;
    private DefaultTableModel counterSalesModel;

    public AdminFrame() {
        setTitle("Sahyadri Food Court - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        // Main Layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));
        setContentPane(mainPanel);

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94)); // Charcoal Navy
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Load Logo Icon if exists
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titlePanel.setOpaque(false);
        ImageIcon logoIcon = new ImageIcon("docs/sahyadri-logo.png");
        if (logoIcon.getIconWidth() > 0) {
            Image img = logoIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
            titlePanel.add(new JLabel(new ImageIcon(img)));
        }

        JLabel lblTitle = new JLabel("Sahyadri Food Court - Admin Dashboard");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle);
        headerPanel.add(titlePanel, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(192, 57, 43)); // Red
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        headerPanel.add(btnLogout, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // --- TABBED PANELS ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // TAB 1: Daily Revenue & Logs
        JPanel tabDaily = new JPanel(new BorderLayout());
        tabDaily.setOpaque(false);
        tabDaily.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Control / Selector Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        controlPanel.setOpaque(false);
        controlPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel lblDate = new JLabel("Select Date:");
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDate.setForeground(new Color(44, 62, 80));
        controlPanel.add(lblDate);

        // Date Spinner
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateSpinner.setPreferredSize(new Dimension(130, 30));
        controlPanel.add(dateSpinner);

        JButton btnGenerate = new JButton("Generate Daily Report");
        btnGenerate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGenerate.setBackground(new Color(41, 128, 185)); // Blue
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setFocusPainted(false);
        btnGenerate.setPreferredSize(new Dimension(180, 30));
        btnGenerate.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnGenerate.addActionListener(e -> generateReport());
        controlPanel.add(btnGenerate);

        JButton btnClear = new JButton("Clear Logs");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClear.setBackground(new Color(127, 140, 141));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);
        btnClear.addActionListener(e -> {
            reportsModel.setRowCount(0);
            orderDetailsModel.setRowCount(0);
            counterSalesModel.setRowCount(0);
        });
        controlPanel.add(btnClear);

        tabDaily.add(controlPanel, BorderLayout.NORTH);

        // Splitting into Stored Procedure results (Top) and Detailed logs side-by-side (Bottom)
        JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        tablesPanel.setOpaque(false);

        // 1. Compiled Reports Panel
        JPanel reportsPanel = new JPanel(new BorderLayout());
        reportsPanel.setOpaque(false);
        JLabel lblReportsTitle = new JLabel("Compiled Daily Sales Summary (Stored Procedure Output)");
        lblReportsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblReportsTitle.setForeground(new Color(52, 73, 94));
        lblReportsTitle.setBorder(new EmptyBorder(0, 0, 5, 0));
        reportsPanel.add(lblReportsTitle, BorderLayout.NORTH);

        String[] reportCols = {"Report Date", "Total Orders", "Total Revenue (₹)", "Best Selling Item"};
        reportsModel = new DefaultTableModel(reportCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblReports = new JTable(reportsModel);
        tblReports.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblReports.setRowHeight(25);
        tblReports.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblReports.getTableHeader().setBackground(new Color(41, 128, 185));
        tblReports.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scrollReports = new JScrollPane(tblReports);
        reportsPanel.add(scrollReports, BorderLayout.CENTER);

        tablesPanel.add(reportsPanel);

        // 2. Bottom Side-by-Side Panels
        JPanel bottomSplitPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomSplitPanel.setOpaque(false);

        // Left: Detailed Orders Panel
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setOpaque(false);
        JLabel lblDetailsTitle = new JLabel("Detailed Orders for Selected Date");
        lblDetailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDetailsTitle.setForeground(new Color(52, 73, 94));
        lblDetailsTitle.setBorder(new EmptyBorder(0, 0, 5, 0));
        detailsPanel.add(lblDetailsTitle, BorderLayout.NORTH);

        String[] orderDetailCols = {"Order ID", "Student Name", "Order Time", "Total Amount (₹)", "Status"};
        orderDetailsModel = new DefaultTableModel(orderDetailCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblOrderDetails = new JTable(orderDetailsModel);
        tblOrderDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblOrderDetails.setRowHeight(23);
        tblOrderDetails.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblOrderDetails.getTableHeader().setBackground(new Color(52, 73, 94));
        tblOrderDetails.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scrollDetails = new JScrollPane(tblOrderDetails);
        detailsPanel.add(scrollDetails, BorderLayout.CENTER);
        bottomSplitPanel.add(detailsPanel);

        // Right: Counter Sales Panel
        JPanel counterSalesPanel = new JPanel(new BorderLayout());
        counterSalesPanel.setOpaque(false);
        JLabel lblCounterSalesTitle = new JLabel("Stall Revenue Breakdown (Counter Table Join)");
        lblCounterSalesTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCounterSalesTitle.setForeground(new Color(52, 73, 94));
        lblCounterSalesTitle.setBorder(new EmptyBorder(0, 0, 5, 0));
        counterSalesPanel.add(lblCounterSalesTitle, BorderLayout.NORTH);

        String[] counterCols = {"Counter Name", "Operator Name", "Revenue (₹)"};
        counterSalesModel = new DefaultTableModel(counterCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblCounterSales = new JTable(counterSalesModel);
        tblCounterSales.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblCounterSales.setRowHeight(23);
        tblCounterSales.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblCounterSales.getTableHeader().setBackground(new Color(52, 73, 94));
        tblCounterSales.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scrollCounter = new JScrollPane(tblCounterSales);
        counterSalesPanel.add(scrollCounter, BorderLayout.CENTER);
        bottomSplitPanel.add(counterSalesPanel);

        tablesPanel.add(bottomSplitPanel);
        tabDaily.add(tablesPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Daily Sales & Logs", tabDaily);

        // TAB 2: Student Insights Panel
        JPanel tabStudent = new JPanel(new BorderLayout());
        tabStudent.setBackground(new Color(245, 247, 250));
        tabStudent.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel studentSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        studentSelectPanel.setOpaque(false);
        studentSelectPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel lblSelectStudent = new JLabel("Select Student:");
        lblSelectStudent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSelectStudent.setForeground(new Color(44, 62, 80));
        studentSelectPanel.add(lblSelectStudent);

        cmbStudents = new JComboBox<>();
        cmbStudents.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbStudents.setPreferredSize(new Dimension(280, 30));
        studentSelectPanel.add(cmbStudents);
        tabStudent.add(studentSelectPanel, BorderLayout.NORTH);

        // Center Panel for Student Insights
        JPanel studentCenterPanel = new JPanel(new GridBagLayout());
        studentCenterPanel.setOpaque(false);
        GridBagConstraints sGbc = new GridBagConstraints();
        sGbc.fill = GridBagConstraints.BOTH;
        sGbc.weighty = 1.0;

        // Left Side: Stats Card Panel
        JPanel statsCardPanel = new JPanel();
        statsCardPanel.setBackground(Color.WHITE);
        statsCardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        statsCardPanel.setLayout(new GridLayout(6, 1, 0, 10));

        JLabel lblTrendsTitle = new JLabel("Student Stats & Trends:");
        lblTrendsTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTrendsTitle.setForeground(new Color(41, 128, 185));

        lblTotalOrders = new JLabel("Total Orders: 0");
        lblTotalOrders.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        lblTotalSpent = new JLabel("Total Spent: ₹0.00");
        lblTotalSpent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        lblAvgSpent = new JLabel("Average Order Value: ₹0.00");
        lblAvgSpent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        lblFavItem = new JLabel("Favorite Food: N/A");
        lblFavItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        lblBranch = new JLabel("Estimated Branch: N/A");
        lblBranch.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        statsCardPanel.add(lblTrendsTitle);
        statsCardPanel.add(lblTotalOrders);
        statsCardPanel.add(lblTotalSpent);
        statsCardPanel.add(lblAvgSpent);
        statsCardPanel.add(lblFavItem);
        statsCardPanel.add(lblBranch);

        sGbc.gridx = 0; sGbc.gridy = 0; sGbc.weightx = 0.35;
        sGbc.insets = new Insets(0, 0, 0, 15);
        studentCenterPanel.add(statsCardPanel, sGbc);

        // Right Side: Orders Table
        JPanel studentOrdersTablePanel = new JPanel(new BorderLayout());
        studentOrdersTablePanel.setOpaque(false);
        JLabel lblStudentOrdersTitle = new JLabel("All Orders Placed by Selected Student:");
        lblStudentOrdersTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStudentOrdersTitle.setForeground(new Color(52, 73, 94));
        lblStudentOrdersTitle.setBorder(new EmptyBorder(0, 0, 5, 0));
        studentOrdersTablePanel.add(lblStudentOrdersTitle, BorderLayout.NORTH);

        String[] sOrderCols = {"Order ID", "Date", "Amount (₹)", "Status", "Items Purchased"};
        studentOrdersModel = new DefaultTableModel(sOrderCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblStudentOrders = new JTable(studentOrdersModel);
        tblStudentOrders.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblStudentOrders.setRowHeight(24);
        tblStudentOrders.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblStudentOrders.getTableHeader().setBackground(new Color(52, 73, 94));
        tblStudentOrders.getTableHeader().setForeground(Color.WHITE);
        
        // Give Items Purchased column more space
        tblStudentOrders.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane scrollStudentOrders = new JScrollPane(tblStudentOrders);
        studentOrdersTablePanel.add(scrollStudentOrders, BorderLayout.CENTER);

        sGbc.gridx = 1; sGbc.gridy = 0; sGbc.weightx = 0.65;
        sGbc.insets = new Insets(0, 0, 0, 0);
        studentCenterPanel.add(studentOrdersTablePanel, sGbc);

        tabStudent.add(studentCenterPanel, BorderLayout.CENTER);
        tabbedPane.addTab("Student Insights & Trends", tabStudent);

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Load students and add combobox action listener
        loadStudentList();
        cmbStudents.addActionListener(e -> {
            StudentComboItem selected = (StudentComboItem) cmbStudents.getSelectedItem();
            if (selected != null) {
                loadStudentInsights(selected.id);
            }
        });

        // Load first student insights on startup
        if (cmbStudents.getItemCount() > 0) {
            cmbStudents.setSelectedIndex(0);
        }
    }

    private void generateReport() {
        Date selectedDate = (Date) dateSpinner.getValue();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(selectedDate);

        // 1. Call Stored Procedure
        String procCall = "{CALL GenerateDailySalesReport(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cs = conn.prepareCall(procCall)) {
            
            cs.setString(1, dateStr);
            
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    int totalOrders = rs.getInt("TotalOrders");
                    double totalRevenue = rs.getDouble("TotalRevenue");
                    String bestSellingItem = rs.getString("BestSellingItem");

                    // Check if we already have this date in the reports table to avoid duplicates
                    boolean exists = false;
                    for (int i = 0; i < reportsModel.getRowCount(); i++) {
                        if (reportsModel.getValueAt(i, 0).equals(dateStr)) {
                            // Update row
                            reportsModel.setValueAt(totalOrders, i, 1);
                            reportsModel.setValueAt(totalRevenue, i, 2);
                            reportsModel.setValueAt(bestSellingItem, i, 3);
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        reportsModel.addRow(new Object[]{
                            dateStr,
                            totalOrders,
                            totalRevenue,
                            bestSellingItem
                        });
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error calling stored procedure: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return;
        }

        // 2. Fetch Detailed Orders for that day
        orderDetailsModel.setRowCount(0);
        String detailsQuery = 
            "SELECT O.order_id, S.name, O.order_date, O.total_amount, O.status " +
            "FROM Orders O " +
            "JOIN Student S ON O.student_id = S.student_id " +
            "WHERE DATE(O.order_date) = ? " +
            "ORDER BY O.order_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(detailsQuery)) {
            
            ps.setString(1, dateStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    String name = rs.getString("name");
                    String orderTime = new SimpleDateFormat("HH:mm:ss").format(rs.getTimestamp("order_date"));
                    double amount = rs.getDouble("total_amount");
                    String status = rs.getString("status");

                    orderDetailsModel.addRow(new Object[]{
                        orderId,
                        name,
                        orderTime,
                        amount,
                        status
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error fetching order logs: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        // 3. Fetch Counter Wise Sales Breakdown
        counterSalesModel.setRowCount(0);
        String counterQuery = 
            "SELECT C.counter_name, C.operator_name, IFNULL(SUM(OI.quantity * OI.unit_price), 0.0) AS revenue " +
            "FROM Counter C " +
            "LEFT JOIN Menu_Item MI ON C.counter_id = MI.counter_id " +
            "LEFT JOIN Order_Item OI ON MI.item_id = OI.item_id " +
            "LEFT JOIN Orders O ON OI.order_id = O.order_id AND DATE(O.order_date) = ? " +
            "GROUP BY C.counter_id, C.counter_name, C.operator_name " +
            "ORDER BY revenue DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(counterQuery)) {
            
            ps.setString(1, dateStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String counterName = rs.getString("counter_name");
                    String operator = rs.getString("operator_name");
                    double revenue = rs.getDouble("revenue");

                    counterSalesModel.addRow(new Object[]{
                        counterName,
                        operator,
                        "₹" + String.format("%.2f", revenue)
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error fetching counter sales logs: " + ex.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void loadStudentList() {
        cmbStudents.removeAllItems();
        String query = "SELECT student_id, name FROM Student ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("student_id");
                String name = rs.getString("name");
                cmbStudents.addItem(new StudentComboItem(id, name));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadStudentInsights(String studentId) {
        // 1. Calculate branch from USN
        String branch = "Unknown Branch";
        String usnUpper = studentId.toUpperCase();
        if (usnUpper.contains("CI")) {
            branch = "AIML (Artificial Intelligence)";
        } else if (usnUpper.contains("CS")) {
            branch = "CSE (Computer Science)";
        } else if (usnUpper.contains("EC")) {
            branch = "ECE (Electronics & Comm.)";
        } else if (usnUpper.contains("ME")) {
            branch = "ME (Mechanical Eng.)";
        }
        lblBranch.setText("Estimated Branch: " + branch);

        // 2. Fetch general trends statistics
        String statsQuery = "SELECT COUNT(order_id) AS total_orders, IFNULL(SUM(total_amount), 0.0) AS total_spent, " +
                             "IFNULL(AVG(total_amount), 0.0) AS avg_spent FROM Orders WHERE student_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(statsQuery)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int totalOrders = rs.getInt("total_orders");
                    double totalSpent = rs.getDouble("total_spent");
                    double avgSpent = rs.getDouble("avg_spent");

                    lblTotalOrders.setText("Total Orders: " + totalOrders);
                    lblTotalSpent.setText("Total Spent: ₹" + String.format("%.2f", totalSpent));
                    lblAvgSpent.setText("Average Order Value: ₹" + String.format("%.2f", avgSpent));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // 3. Fetch favorite item
        String favQuery = "SELECT MI.item_name, SUM(OI.quantity) AS qty " +
                          "FROM Order_Item OI " +
                          "JOIN Orders O ON OI.order_id = O.order_id " +
                          "JOIN Menu_Item MI ON OI.item_id = MI.item_id " +
                          "WHERE O.student_id = ? " +
                          "GROUP BY MI.item_id, MI.item_name " +
                          "ORDER BY qty DESC " +
                          "LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(favQuery)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String itemName = rs.getString("item_name");
                    int qty = rs.getInt("qty");
                    lblFavItem.setText("Favorite Food: " + itemName + " (" + qty + " ordered)");
                } else {
                    lblFavItem.setText("Favorite Food: None");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // 4. Fetch orders table with item details list
        studentOrdersModel.setRowCount(0);
        String listQuery = "SELECT O.order_id, O.order_date, O.total_amount, O.status, " +
                           "GROUP_CONCAT(CONCAT(MI.item_name, ' (x', OI.quantity, ')') SEPARATOR ', ') AS items_summary " +
                           "FROM Orders O " +
                           "JOIN Order_Item OI ON O.order_id = OI.order_id " +
                           "JOIN Menu_Item MI ON OI.item_id = MI.item_id " +
                           "WHERE O.student_id = ? " +
                           "GROUP BY O.order_id " +
                           "ORDER BY O.order_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(listQuery)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(rs.getTimestamp("order_date"));
                    double total = rs.getDouble("total_amount");
                    String status = rs.getString("status");
                    String items = rs.getString("items_summary");

                    studentOrdersModel.addRow(new Object[]{
                        orderId,
                        date,
                        "₹" + String.format("%.2f", total),
                        status,
                        items != null ? items : "No details"
                    });
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

/**
 * Combobox item representing a Student.
 */
class StudentComboItem {
    String id;
    String name;

    public StudentComboItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
