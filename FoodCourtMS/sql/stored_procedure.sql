USE food_court_db;

DELIMITER //

DROP PROCEDURE IF EXISTS GenerateDailySalesReport //

CREATE PROCEDURE GenerateDailySalesReport(IN report_date DATE)
BEGIN
    DECLARE total_orders INT DEFAULT 0;
    DECLARE total_revenue DECIMAL(8,2) DEFAULT 0.00;
    DECLARE best_selling_item VARCHAR(100) DEFAULT 'None';
    
    -- 1. Get total orders
    SELECT COUNT(*) INTO total_orders
    FROM Orders
    WHERE DATE(order_date) = report_date;
    
    -- 2. Get total revenue
    SELECT IFNULL(SUM(total_amount), 0.00) INTO total_revenue
    FROM Orders
    WHERE DATE(order_date) = report_date;
    
    -- 3. Get best selling item of the day
    SELECT MI.item_name INTO best_selling_item
    FROM Order_Item OI
    JOIN Orders O ON OI.order_id = O.order_id
    JOIN Menu_Item MI ON OI.item_id = MI.item_id
    WHERE DATE(O.order_date) = report_date
    GROUP BY OI.item_id, MI.item_name
    ORDER BY SUM(OI.quantity) DESC
    LIMIT 1;

    -- If no best selling item is found (no orders), set default
    IF best_selling_item IS NULL THEN
        SET best_selling_item = 'None';
    END IF;

    -- Return report
    SELECT 
        total_orders AS TotalOrders, 
        total_revenue AS TotalRevenue, 
        best_selling_item AS BestSellingItem;
END //

DELIMITER ;
