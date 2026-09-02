-- Create database if it does not exist
CREATE DATABASE IF NOT EXISTS food_court_db;
USE food_court_db;

-- Drop tables in reverse order of dependencies to avoid foreign key constraint issues
DROP TABLE IF EXISTS Order_Item;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS Menu_Item;
DROP TABLE IF EXISTS Counter;
DROP TABLE IF EXISTS Student;

-- 1. Student Table
CREATE TABLE IF NOT EXISTS Student (
    student_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15) NOT NULL,
    wallet_balance DECIMAL(8,2) DEFAULT 0.00
);

-- 2. Counter Table
CREATE TABLE IF NOT EXISTS Counter (
    counter_id INT PRIMARY KEY AUTO_INCREMENT,
    counter_name VARCHAR(100) NOT NULL,
    location VARCHAR(100),
    operator_name VARCHAR(100)
);

-- 3. Menu_Item Table
CREATE TABLE IF NOT EXISTS Menu_Item (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    counter_id INT,
    item_name VARCHAR(100) NOT NULL,
    price DECIMAL(6,2) NOT NULL,
    category VARCHAR(50),
    stock_qty INT DEFAULT 0,
    FOREIGN KEY (counter_id) REFERENCES Counter(counter_id) ON DELETE CASCADE
);

-- 4. Orders Table
CREATE TABLE IF NOT EXISTS Orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(50),
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(8,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'Pending',
    FOREIGN KEY (student_id) REFERENCES Student(student_id) ON DELETE CASCADE
);

-- 5. Order_Item Table
CREATE TABLE IF NOT EXISTS Order_Item (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    item_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(6,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES Menu_Item(item_id) ON DELETE CASCADE
);

-- 6. Stock Deduction Trigger
DELIMITER //
DROP TRIGGER IF EXISTS after_order_item_insert //
CREATE TRIGGER after_order_item_insert
AFTER INSERT ON Order_Item
FOR EACH ROW
BEGIN
    UPDATE Menu_Item
    SET stock_qty = stock_qty - NEW.quantity
    WHERE item_id = NEW.item_id;
END //
DELIMITER ;
