import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * MenuFrame allows students to browse items, filter by category,
 * view stock quantities, and manage their cart.
 */
public class MenuFrame extends JFrame {
    private String studentId;
    private String studentName;
    private double walletBalance;

    private JTable tblMenu;
    private DefaultTableModel menuModel;
    private JComboBox<String> cmbCategory;
    
    private JTable tblCart;
    private DefaultTableModel cartModel;
    private JLabel lblCartTotal;
    private JLabel lblWallet;
    
    private List<CartItem> cartList = new ArrayList<>();

    public MenuFrame(String studentId, String studentName, double walletBalance) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.walletBalance = walletBalance;

        setTitle("Sahyadri Food Court - Browse Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);

        // Main layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));
        setContentPane(mainPanel);

        // --- TOP PANEL: Student Info & Navigation ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(41, 128, 185)); // Vivid Blue
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel studentInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        studentInfoPanel.setOpaque(false);
        
        // Load Logo Icon
        ImageIcon logoIcon = new ImageIcon("docs/sahyadri-logo.png");
        if (logoIcon.getIconWidth() > 0) {
            Image img = logoIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
            JLabel lblLogo = new JLabel(new ImageIcon(img));
            lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            studentInfoPanel.add(lblLogo);
        }

        JLabel lblUser = new JLabel("Welcome: " + studentName + " (ID: " + studentId + ")");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(Color.WHITE);
        
        lblWallet = new JLabel("Wallet Balance: ₹" + String.format("%.2f", walletBalance));
        lblWallet.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblWallet.setForeground(new Color(241, 196, 15)); // Gold Color
        
        studentInfoPanel.add(lblUser);
        studentInfoPanel.add(lblWallet);
        topPanel.add(studentInfoPanel, BorderLayout.WEST);

        // Navigation Buttons Panel (Track Orders & Logout)
        JPanel navButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navButtonPanel.setOpaque(false);

        JButton btnTrack = new JButton("Track Orders");
        btnTrack.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTrack.setBackground(new Color(230, 126, 34)); // Orange/Gold
        btnTrack.setForeground(Color.WHITE);
        btnTrack.setFocusPainted(false);
        btnTrack.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnTrack.addActionListener(e -> showTrackOrdersDialog());
        navButtonPanel.add(btnTrack);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(192, 57, 43)); // Soft Red
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        navButtonPanel.add(btnLogout);

        topPanel.add(navButtonPanel, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- CENTER PANEL: Menu browsing & Filter (Left Side) ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Filter Sub-panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel lblFilter = new JLabel("Category:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFilter.setForeground(new Color(52, 73, 94));
        filterPanel.add(lblFilter);

        cmbCategory = new JComboBox<>();
        cmbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbCategory.setPreferredSize(new Dimension(150, 30));
        loadCategories();
        cmbCategory.addActionListener(e -> loadMenuItems());
        filterPanel.add(cmbCategory);

        JButton btnRefresh = new JButton("Refresh Menu");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBackground(new Color(52, 73, 94));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> loadMenuItems());
        filterPanel.add(btnRefresh);

        centerPanel.add(filterPanel, BorderLayout.NORTH);

        // Menu Table
        String[] menuCols = {"Item ID", "Item Name", "Category", "Counter", "Price (₹)", "Available Stock"};
        menuModel = new DefaultTableModel(menuCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblMenu = new JTable(menuModel);
        tblMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblMenu.setRowHeight(25);
        tblMenu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblMenu.getTableHeader().setBackground(new Color(52, 73, 94));
        tblMenu.getTableHeader().setForeground(Color.WHITE);
        tblMenu.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Hide ID column visually but keep it in model
        tblMenu.getColumnModel().getColumn(0).setMinWidth(0);
        tblMenu.getColumnModel().getColumn(0).setMaxWidth(0);
        tblMenu.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollMenu = new JScrollPane(tblMenu);
        centerPanel.add(scrollMenu, BorderLayout.CENTER);

        // Add to Cart panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setOpaque(false);
        
        JLabel lblQty = new JLabel("Qty:");
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 14));
        actionPanel.add(lblQty);
        
        JSpinner spinQty = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spinQty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinQty.setPreferredSize(new Dimension(60, 30));
        actionPanel.add(spinQty);

        JButton btnAddToCart = new JButton("Add to Cart");
        btnAddToCart.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddToCart.setBackground(new Color(46, 204, 113)); // Green
        btnAddToCart.setForeground(Color.WHITE);
        btnAddToCart.setFocusPainted(false);
        btnAddToCart.setPreferredSize(new Dimension(130, 30));
        btnAddToCart.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAddToCart.addActionListener(e -> {
            int selectedRow = tblMenu.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an item from the menu.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int itemId = (int) menuModel.getValueAt(selectedRow, 0);
            String name = (String) menuModel.getValueAt(selectedRow, 1);
            double price = ((Number) menuModel.getValueAt(selectedRow, 4)).doubleValue();
            int stock = (int) menuModel.getValueAt(selectedRow, 5);
            int qty = (int) spinQty.getValue();

            addToCart(itemId, name, price, stock, qty);
            spinQty.setValue(1); // Reset spinner
        });
        actionPanel.add(btnAddToCart);
        centerPanel.add(actionPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // --- EAST PANEL: Cart Sidebar (Right Side) ---
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setPreferredSize(new Dimension(320, 600));
        cartPanel.setBackground(Color.WHITE);
        cartPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(200, 200, 200)));

        JLabel lblCartHeader = new JLabel("Shopping Cart", SwingConstants.CENTER);
        lblCartHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCartHeader.setBorder(new EmptyBorder(15, 10, 15, 10));
        lblCartHeader.setBackground(new Color(236, 240, 241));
        lblCartHeader.setOpaque(true);
        cartPanel.add(lblCartHeader, BorderLayout.NORTH);

        // Cart Table
        String[] cartCols = {"Item Name", "Qty", "Price", "Subtotal"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblCart = new JTable(cartModel);
        tblCart.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblCart.setRowHeight(22);
        tblCart.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblCart.getTableHeader().setBackground(new Color(127, 140, 141));
        tblCart.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollCart = new JScrollPane(tblCart);
        cartPanel.add(scrollCart, BorderLayout.CENTER);

        // Cart Footer (Total and Checkout)
        JPanel cartFooter = new JPanel(new BorderLayout());
        cartFooter.setOpaque(false);
        cartFooter.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setOpaque(false);
        totalPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel lblTotalText = new JLabel("Total Bill:");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalText.setForeground(new Color(52, 73, 94));
        
        lblCartTotal = new JLabel("₹0.00");
        lblCartTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCartTotal.setForeground(new Color(44, 62, 80));
        
        totalPanel.add(lblTotalText, BorderLayout.WEST);
        totalPanel.add(lblCartTotal, BorderLayout.EAST);
        cartFooter.add(totalPanel, BorderLayout.NORTH);

        // Cart Action Buttons (Remove & Checkout)
        JPanel cartButtons = new JPanel(new GridLayout(2, 1, 0, 10));
        cartButtons.setOpaque(false);

        JButton btnRemove = new JButton("Remove Selected");
        btnRemove.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRemove.setBackground(new Color(192, 57, 43));
        btnRemove.setForeground(Color.WHITE);
        btnRemove.setFocusPainted(false);
        btnRemove.addActionListener(e -> removeSelectedCartItem());
        cartButtons.add(btnRemove);

        JButton btnCheckout = new JButton("Proceed to Order");
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCheckout.setBackground(new Color(41, 128, 185));
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setFocusPainted(false);
        btnCheckout.setPreferredSize(new Dimension(0, 40));
        btnCheckout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCheckout.addActionListener(e -> proceedToCheckout());
        cartButtons.add(btnCheckout);

        cartFooter.add(cartButtons, BorderLayout.SOUTH);
        cartPanel.add(cartFooter, BorderLayout.SOUTH);

        mainPanel.add(cartPanel, BorderLayout.EAST);

        // Initial Load
        loadMenuItems();
    }

    private void loadCategories() {
        cmbCategory.removeAllItems();
        cmbCategory.addItem("All Categories");

        String query = "SELECT DISTINCT category FROM Menu_Item WHERE category IS NOT NULL AND category != ''";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                cmbCategory.addItem(rs.getString("category"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadMenuItems() {
        menuModel.setRowCount(0);
        String selectedCategory = (String) cmbCategory.getSelectedItem();

        String query;
        if (selectedCategory == null || selectedCategory.equals("All Categories")) {
            query = "SELECT MI.item_id, MI.item_name, MI.category, C.counter_name, MI.price, MI.stock_qty " +
                    "FROM Menu_Item MI " +
                    "JOIN Counter C ON MI.counter_id = C.counter_id " +
                    "ORDER BY MI.item_name";
        } else {
            query = "SELECT MI.item_id, MI.item_name, MI.category, C.counter_name, MI.price, MI.stock_qty " +
                    "FROM Menu_Item MI " +
                    "JOIN Counter C ON MI.counter_id = C.counter_id " +
                    "WHERE MI.category = ? " +
                    "ORDER BY MI.item_name";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
                ps.setString(1, selectedCategory);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    menuModel.addRow(new Object[]{
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("category"),
                        rs.getString("counter_name"),
                        rs.getDouble("price"),
                        rs.getInt("stock_qty")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load menu items: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void addToCart(int itemId, String name, double price, int stock, int qty) {
        // Find if item is already in cart
        CartItem existingItem = null;
        for (CartItem ci : cartList) {
            if (ci.itemId == itemId) {
                existingItem = ci;
                break;
            }
        }

        int requestedQty = qty;
        if (existingItem != null) {
            requestedQty += existingItem.quantity;
        }

        // Validate stock
        if (requestedQty > stock) {
            JOptionPane.showMessageDialog(this, 
                "Insufficient stock! Available stock: " + stock + " items.", 
                "Stock Out Limit", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (existingItem != null) {
            existingItem.quantity = requestedQty;
        } else {
            cartList.add(new CartItem(itemId, name, price, qty));
        }

        updateCartTable();
    }

    private void removeSelectedCartItem() {
        int selectedRow = tblCart.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item from the cart to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cartList.remove(selectedRow);
        updateCartTable();
    }

    private void updateCartTable() {
        cartModel.setRowCount(0);
        double total = 0.0;
        for (CartItem ci : cartList) {
            double subtotal = ci.price * ci.quantity;
            total += subtotal;
            cartModel.addRow(new Object[]{
                ci.itemName,
                ci.quantity,
                "₹" + String.format("%.2f", ci.price),
                "₹" + String.format("%.2f", subtotal)
            });
        }
        lblCartTotal.setText("₹" + String.format("%.2f", total));
    }

    private void proceedToCheckout() {
        if (cartList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty!", "Cart Empty", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Open OrderFrame and pass cart details
        new OrderFrame(this, studentId, studentName, walletBalance, cartList).setVisible(true);
        this.setEnabled(false); // Disable interaction with menu until checkout completes/cancels
    }

    // Callback when order is successfully placed to refresh menu items, wallet, and clear cart
    public void orderPlacedSuccessfully(double newBalance) {
        this.walletBalance = newBalance;
        this.cartList.clear();
        updateCartTable();
        loadMenuItems();
        
        // Update header balance text
        lblWallet.setText("Wallet Balance: ₹" + String.format("%.2f", newBalance));
    }

    private void showTrackOrdersDialog() {
        JDialog trackDialog = new JDialog(this, "Order History & Live Status Tracking", true);
        trackDialog.setSize(750, 500);
        trackDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        trackDialog.setContentPane(panel);

        JLabel lblTitle = new JLabel("Order History & Queue Tracking", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(44, 62, 80));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(lblTitle, BorderLayout.NORTH);

        // Splitting into Orders List (Top) and Order Details (Bottom)
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        centerPanel.setOpaque(false);

        // Top Panel: Orders List
        JPanel ordersPanel = new JPanel(new BorderLayout());
        ordersPanel.setOpaque(false);
        JLabel lblOrdersTitle = new JLabel("Select an Order to View Items:");
        lblOrdersTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblOrdersTitle.setForeground(new Color(52, 73, 94));
        lblOrdersTitle.setBorder(new EmptyBorder(0, 0, 5, 0));
        ordersPanel.add(lblOrdersTitle, BorderLayout.NORTH);

        String[] columns = {"Order ID", "Date & Time", "Total Amount", "Status", "Est. Waiting Time"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblTrack = new JTable(model);
        tblTrack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblTrack.setRowHeight(22);
        tblTrack.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblTrack.getTableHeader().setBackground(new Color(41, 128, 185));
        tblTrack.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scrollOrders = new JScrollPane(tblTrack);
        ordersPanel.add(scrollOrders, BorderLayout.CENTER);
        centerPanel.add(ordersPanel);

        // Bottom Panel: Order Details
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setOpaque(false);
        JLabel lblDetailsTitle = new JLabel("Order Items Breakdown:");
        lblDetailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDetailsTitle.setForeground(new Color(52, 73, 94));
        lblDetailsTitle.setBorder(new EmptyBorder(0, 0, 5, 0));
        detailsPanel.add(lblDetailsTitle, BorderLayout.NORTH);

        String[] detailCols = {"Item Name", "Quantity", "Unit Price", "Subtotal"};
        DefaultTableModel detailsModel = new DefaultTableModel(detailCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblDetails = new JTable(detailsModel);
        tblDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblDetails.setRowHeight(22);
        tblDetails.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDetails.getTableHeader().setBackground(new Color(52, 73, 94));
        tblDetails.getTableHeader().setForeground(Color.WHITE);
        JScrollPane scrollDetails = new JScrollPane(tblDetails);
        detailsPanel.add(scrollDetails, BorderLayout.CENTER);
        centerPanel.add(detailsPanel);

        panel.add(centerPanel, BorderLayout.CENTER);

        // Selection listener to load details
        tblTrack.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = tblTrack.getSelectedRow();
                if (selectedRow != -1) {
                    int orderId = (Integer) tblTrack.getValueAt(selectedRow, 0);
                    // Fetch details
                    detailsModel.setRowCount(0);
                    String detailsQuery = "SELECT MI.item_name, OI.quantity, OI.unit_price FROM Order_Item OI " +
                                         "JOIN Menu_Item MI ON OI.item_id = MI.item_id " +
                                         "WHERE OI.order_id = ?";
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement ps = conn.prepareStatement(detailsQuery)) {
                        ps.setInt(1, orderId);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String itemName = rs.getString("item_name");
                                int qty = rs.getInt("quantity");
                                double price = rs.getDouble("unit_price");
                                detailsModel.addRow(new Object[]{
                                    itemName,
                                    qty,
                                    "₹" + String.format("%.2f", price),
                                    "₹" + String.format("%.2f", price * qty)
                                });
                            }
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Fetch orders data
        String query = "SELECT order_id, order_date, total_amount, status FROM Orders WHERE student_id = ? ORDER BY order_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    java.sql.Timestamp date = rs.getTimestamp("order_date");
                    double total = rs.getDouble("total_amount");
                    String status = rs.getString("status");

                    // Compute dynamic waiting time
                    String waitTime = "N/A";
                    if ("Completed".equals(status)) {
                        waitTime = "Picked Up";
                    } else if ("Ready".equals(status)) {
                        waitTime = "Ready for Pickup!";
                    } else if ("Pending".equals(status) || "Preparing".equals(status)) {
                        // Count incomplete orders ahead
                        String queueQuery = "SELECT COUNT(*) FROM Orders WHERE (status = 'Pending' OR status = 'Preparing') AND order_id <= ?";
                        try (PreparedStatement psQ = conn.prepareStatement(queueQuery)) {
                            psQ.setInt(1, orderId);
                            try (ResultSet rsQ = psQ.executeQuery()) {
                                if (rsQ.next()) {
                                    int countAhead = rsQ.getInt(1);
                                    int minutes = countAhead * 5;
                                    waitTime = minutes + " mins (Queue: " + countAhead + ")";
                                }
                            }
                        }
                    }

                    model.addRow(new Object[]{
                        orderId,
                        date.toString().substring(0, 19),
                        "₹" + String.format("%.2f", total),
                        status,
                        waitTime
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(trackDialog, "Error loading orders: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        // Close button
        JButton btnClose = new JButton("Close Dialog");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.setBackground(new Color(127, 140, 141));
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> trackDialog.dispose());
        
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setOpaque(false);
        southPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        southPanel.add(btnClose);
        panel.add(southPanel, BorderLayout.SOUTH);

        // Pre-select first order if exists to show details automatically
        if (tblTrack.getRowCount() > 0) {
            tblTrack.setRowSelectionInterval(0, 0);
        }

        trackDialog.setVisible(true);
    }
}

/**
 * Helper class representing an item added to the shopping cart.
 */
class CartItem {
    int itemId;
    String itemName;
    double price;
    int quantity;

    public CartItem(int itemId, String itemName, double price, int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
