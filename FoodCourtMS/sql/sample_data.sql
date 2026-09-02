USE food_court_db;

-- Disable Safe Update mode temporarily to allow clearing tables
SET SQL_SAFE_UPDATES = 0;

-- Clear existing data (in reverse order of dependencies)
DELETE FROM Order_Item;
DELETE FROM Orders;
DELETE FROM Menu_Item;
DELETE FROM Counter;
DELETE FROM Student;

-- Reset Auto-Increments (where applicable)
ALTER TABLE Counter AUTO_INCREMENT = 1;
ALTER TABLE Menu_Item AUTO_INCREMENT = 1;
ALTER TABLE Orders AUTO_INCREMENT = 1;
ALTER TABLE Order_Item AUTO_INCREMENT = 1;

-- 1. Insert Students with Alphanumeric College IDs
INSERT INTO Student (student_id, name, email, phone, wallet_balance) VALUES
('4SF24CS001', 'Amit Sharma', 'amit.sharma@email.com', '9876543210', 500.00),
('4SF24EC042', 'Priya Patel', 'priya.patel@email.com', '8765432109', 750.00),
('4SF24ME105', 'Rohan Das', 'rohan.das@email.com', '7654321098', 300.00),
('4SF24CI005', 'ABHISHEK', 'abhi.hosamani@email.com', '636333', 1000.00),
('4SF24CS015', 'Vikram Singh', 'vikram.singh@email.com', '9123456780', 150.00);

-- 2. Insert Counters
INSERT INTO Counter (counter_name, location, operator_name) VALUES
('Spice N Spice', 'Block A, Food Court Ground Floor', 'Rajesh Kumar'),
('South Express', 'Block A, Food Court Corner Shop', 'Karthik Pillai'),
('Chai & Snacks', 'Block A, Food Court Terrace', 'Suresh Dev');

-- 3. Insert Menu Items (Expanded to 19 items)
-- Counter 1: Spice N Spice (Meals & North Indian)
INSERT INTO Menu_Item (counter_id, item_name, price, category, stock_qty) VALUES
(1, 'Paneer Butter Masala Combo', 120.00, 'Meals', 50),
(1, 'Chole Bhature', 90.00, 'Meals', 40),
(1, 'Veg Biryani', 110.00, 'Meals', 35),
(1, 'Butter Naan', 40.00, 'Breads', 100),
(1, 'Veg Kolhapuri', 95.00, 'Meals', 30),
(1, 'Garlic Naan', 50.00, 'Breads', 80),
(1, 'Dal Makhani Combo', 110.00, 'Meals', 40);

-- Counter 2: South Express (South Indian)
INSERT INTO Menu_Item (counter_id, item_name, price, category, stock_qty) VALUES
(2, 'Masala Dosa', 70.00, 'South Indian', 60),
(2, 'Idli Sambar (2 pcs)', 45.00, 'South Indian', 80),
(2, 'Medu Vada (2 pcs)', 50.00, 'South Indian', 45),
(2, 'Onion Rava Dosa', 80.00, 'South Indian', 40),
(2, 'Sambar Vada (2 pcs)', 50.00, 'South Indian', 50),
(2, 'Butter Masala Dosa', 85.00, 'South Indian', 35);

-- Counter 3: Chai & Snacks (Snacks/Beverages)
INSERT INTO Menu_Item (counter_id, item_name, price, category, stock_qty) VALUES
(3, 'Masala Chai', 15.00, 'Beverages', 150),
(3, 'Vegetable Samosa (1 pc)', 20.00, 'Snacks', 100),
(3, 'Cheese Maggi', 50.00, 'Snacks', 60),
(3, 'Cold Coffee', 60.00, 'Beverages', 40),
(3, 'Samosa Pav (2 pcs)', 35.00, 'Snacks', 80),
(3, 'Bun Maska', 25.00, 'Snacks', 90),
(3, 'Paneer Grilled Sandwich', 80.00, 'Snacks', 45),
(3, 'Oreo Shake', 75.00, 'Beverages', 35);

-- 4. Insert Orders (Referencing Alphanumeric student_id)
-- Date: 2026-05-21 (Yesterday's orders)
INSERT INTO Orders (student_id, order_date, total_amount, status) VALUES
('4SF24CS001', '2026-05-21 12:30:00', 135.00, 'Completed'),
('4SF24EC042', '2026-05-21 13:00:00', 90.00, 'Completed'),
('4SF24ME105', '2026-05-21 14:15:00', 70.00, 'Completed'),
('4SF24CI005', '2026-05-21 16:00:00', 160.00, 'Completed'),
('4SF24CS015', '2026-05-21 18:30:00', 35.00, 'Completed');

-- Date: 2026-05-22 (Today's orders)
-- Using status values like 'Pending' and 'Preparing' to demonstrate tracking
INSERT INTO Orders (student_id, order_date, total_amount, status) VALUES
('4SF24CS001', '2026-05-22 10:00:00', 110.00, 'Completed'),
('4SF24EC042', '2026-05-22 12:00:00', 210.00, 'Completed'),
('4SF24ME105', '2026-05-22 13:15:00', 140.00, 'Preparing'),
('4SF24CI005', '2026-05-22 15:30:00', 80.00, 'Pending'),
('4SF24CS015', '2026-05-22 17:00:00', 65.00, 'Pending');

-- 5. Insert Order Items (Linking to Orders)
-- Order 1 (Total: 135.00)
INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES
(1, 1, 1, 120.00), -- Paneer Combo
(1, 14, 1, 15.00);  -- Masala Chai (item_id 14 now because of new inserts)

-- Order 2 (Total: 90.00)
INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES
(2, 9, 2, 45.00);  -- Idli Sambar (2 pcs)

-- Order 3 (Total: 70.00)
INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES
(3, 8, 1, 70.00);  -- Masala Dosa

-- Order 4 (Total: 160.00)
INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES
(4, 16, 2, 50.00),  -- Cheese Maggi
(4, 17, 1, 60.00); -- Cold Coffee

-- Order 5 (Total: 35.00)
INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES
(5, 15, 1, 20.00),  -- Vegetable Samosa
(5, 14, 1, 15.00);  -- Masala Chai

-- Order 6 (Total: 110.00)
INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES
(6, 3, 1, 110.00); -- Veg Biryani

-- Order 7 (Total: 210.00)
INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES
(7, 1, 1, 120.00), -- Paneer Combo
(7, 2, 1, 90.00);  -- Chole Bhature

-- Order 8 (Total: 140.00)
INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES
(8, 8, 2, 70.00);  -- Masala Dosa

-- Order 9 (Total: 80.00)
INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES
(9, 17, 1, 60.00), -- Cold Coffee
(9, 15, 1, 20.00);  -- Vegetable Samosa

-- Order 10 (Total: 65.00)
INSERT INTO Order_Item (order_id, item_id, quantity, unit_price) VALUES
(10, 14, 3, 15.00),  -- Masala Chai (3 pcs)
(10, 15, 1, 20.00);  -- Vegetable Samosa

-- Re-enable Safe Update mode
SET SQL_SAFE_UPDATES = 1;
