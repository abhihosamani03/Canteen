import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * OrderFrame displays the checkout summary, lets the student confirm their order,
 * performs wallet validation, and executes database inserts within a transaction.
 */
public class OrderFrame extends JFrame {
    private MenuFrame menuFrame;
    private String studentId;
    private String studentName;
    private double walletBalance;
    private List<CartItem> cartItems;
    private double totalAmount;

    private JTable tblSummary;
    private DefaultTableModel summaryModel;
    private JLabel lblTotalBill;
    private JButton btnConfirm;
    private JButton btnCancel;

    public OrderFrame(MenuFrame menuFrame, String studentId, String studentName, double walletBalance, List<CartItem> cartItems) {
        this.menuFrame = menuFrame;
        this.studentId = studentId;
        this.studentName = studentName;
        this.walletBalance = walletBalance;
        this.cartItems = cartItems;

        setTitle("Order Confirmation & Receipt");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(500, 500);
        setLocationRelativeTo(menuFrame);
        setResizable(false);

        // Calculate Total
        totalAmount = 0.0;
        for (CartItem item : cartItems) {
            totalAmount += item.price * item.quantity;
        }

        // Handle Window Closing (re-enable MenuFrame)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelOrder();
            }
        });

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        setContentPane(mainPanel);

        // Header Label
        JLabel lblHeader = new JLabel("Order Receipt Summary", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(new Color(44, 62, 80));
        lblHeader.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        // Receipt Details & Table
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        // Customer Info Panel
        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel lblCustName = new JLabel("Student Name:  " + studentName);
        lblCustName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCustName.setForeground(new Color(127, 140, 141));
        
        JLabel lblCustWallet = new JLabel("Current Wallet Balance:  ₹" + String.format("%.2f", walletBalance));
        lblCustWallet.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCustWallet.setForeground(new Color(41, 128, 185));

        infoPanel.add(lblCustName);
        infoPanel.add(lblCustWallet);
        centerPanel.add(infoPanel, BorderLayout.NORTH);

        // Summary JTable
        String[] columns = {"Item Name", "Qty", "Price", "Subtotal"};
        summaryModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblSummary = new JTable(summaryModel);
        tblSummary.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblSummary.setRowHeight(22);
        tblSummary.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblSummary.getTableHeader().setBackground(new Color(52, 73, 94));
        tblSummary.getTableHeader().setForeground(Color.WHITE);

        // Populate items
        for (CartItem item : cartItems) {
            summaryModel.addRow(new Object[]{
                item.itemName,
                item.quantity,
                "₹" + String.format("%.2f", item.price),
                "₹" + String.format("%.2f", item.price * item.quantity)
            });
        }

        JScrollPane scrollTable = new JScrollPane(tblSummary);
        centerPanel.add(scrollTable, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Footer panel (Total & Buttons)
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel billPanel = new JPanel(new BorderLayout());
        billPanel.setOpaque(false);
        billPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel lblBillText = new JLabel("Grand Total:");
        lblBillText.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblBillText.setForeground(new Color(52, 73, 94));

        lblTotalBill = new JLabel("₹" + String.format("%.2f", totalAmount));
        lblTotalBill.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotalBill.setForeground(new Color(46, 204, 113)); // Green

        billPanel.add(lblBillText, BorderLayout.WEST);
        billPanel.add(lblTotalBill, BorderLayout.EAST);
        footerPanel.add(billPanel, BorderLayout.NORTH);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancel.setBackground(new Color(189, 195, 199)); // Gray
        btnCancel.setForeground(new Color(52, 73, 94));
        btnCancel.setFocusPainted(false);
        btnCancel.setPreferredSize(new Dimension(100, 35));
        btnCancel.addActionListener(e -> cancelOrder());

        btnConfirm = new JButton("Confirm & Pay");
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setBackground(new Color(46, 204, 113)); // Green
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setPreferredSize(new Dimension(150, 35));
        btnConfirm.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnConfirm.addActionListener(e -> processPaymentAndOrder());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnConfirm);
        footerPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    private void cancelOrder() {
        menuFrame.setEnabled(true);
        menuFrame.toFront();
        this.dispose();
    }

    private void processPaymentAndOrder() {
        // 1. Check wallet balance
        if (walletBalance < totalAmount) {
            JOptionPane.showMessageDialog(this,
                "Insufficient Wallet Balance!\n" +
                "Total Order: ₹" + String.format("%.2f", totalAmount) + "\n" +
                "Available Balance: ₹" + String.format("%.2f", walletBalance) + "\n" +
                "Please contact your food court administrator to recharge.",
                "Insufficient Funds", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Perform DB operations in a single Transaction
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // Check current stock levels to prevent race conditions or negative stocks
            for (CartItem item : cartItems) {
                String stockCheckQuery = "SELECT stock_qty, item_name FROM Menu_Item WHERE item_id = ?";
                try (PreparedStatement psStock = conn.prepareStatement(stockCheckQuery)) {
                    psStock.setInt(1, item.itemId);
                    try (ResultSet rsStock = psStock.executeQuery()) {
                        if (rsStock.next()) {
                            int currentStock = rsStock.getInt("stock_qty");
                            String itemName = rsStock.getString("item_name");
                            if (currentStock < item.quantity) {
                                JOptionPane.showMessageDialog(this,
                                    "Item '" + itemName + "' has insufficient stock.\n" +
                                    "Available: " + currentStock + ", Ordered: " + item.quantity + "\n" +
                                    "Order aborted.", "Stock Insufficient", JOptionPane.ERROR_MESSAGE);
                                conn.rollback();
                                return;
                            }
                        }
                    }
                }
            }

            // Deduct Wallet Balance from Student
            String deductWalletQuery = "UPDATE Student SET wallet_balance = wallet_balance - ? WHERE student_id = ?";
            try (PreparedStatement psWallet = conn.prepareStatement(deductWalletQuery)) {
                psWallet.setDouble(1, totalAmount);
                psWallet.setString(2, studentId);
                psWallet.executeUpdate();
            }

            // Insert into Orders table
            String insertOrderQuery = "INSERT INTO Orders (student_id, total_amount, status) VALUES (?, ?, 'Completed')";
            int orderId = -1;
            try (PreparedStatement psOrder = conn.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setString(1, studentId);
                psOrder.setDouble(2, totalAmount);
                psOrder.executeUpdate();

                // Get generated order_id
                try (ResultSet rsKeys = psOrder.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        orderId = rsKeys.getInt(1);
                    }
                }
            }

            if (orderId == -1) {
                throw new SQLException("Failed to retrieve generated order_id.");
            }

            // Insert into Order_Item table
            // The trigger after_order_item_insert will fire for each insert and reduce stock automatically.
            String insertOrderItemQuery = "INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psOrderItem = conn.prepareStatement(insertOrderItemQuery)) {
                for (CartItem item : cartItems) {
                    psOrderItem.setInt(1, orderId);
                    psOrderItem.setInt(2, item.itemId);
                    psOrderItem.setInt(3, item.quantity);
                    psOrderItem.setDouble(4, item.price);
                    psOrderItem.addBatch();
                }
                psOrderItem.executeBatch();
            }

            // Commit the transaction
            conn.commit();

            double newBalance = walletBalance - totalAmount;
            JOptionPane.showMessageDialog(this,
                "Order placed successfully!\n" +
                "Order ID: " + orderId + "\n" +
                "Total Paid: ₹" + String.format("%.2f", totalAmount) + "\n" +
                "Remaining Wallet Balance: ₹" + String.format("%.2f", newBalance),
                "Order Completed", JOptionPane.INFORMATION_MESSAGE);

            // Notify parent menu frame
            menuFrame.setEnabled(true);
            menuFrame.orderPlacedSuccessfully(newBalance);
            menuFrame.toFront();
            this.dispose();

        } catch (SQLException ex) {
            // Rollback on any failure
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            JOptionPane.showMessageDialog(this,
                "Transaction failed and was rolled back.\nError: " + ex.getMessage(),
                "Order Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }
}
