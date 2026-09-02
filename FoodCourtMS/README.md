# Food Court Management System (FoodCourtMS)

A modern, transactional, and inventory-automated desktop database application built using **Java Swing**, **JDBC**, and **MySQL 8.x**. This mini-project digitizes a campus food court, implementing cashless ordering, real-time inventory triggers, and manager reporting dashboards.

---

## 📂 Project Directory Structure

```text
FoodCourtMS/
│
├── src/
│   ├── DBConnection.java      # Centralized database connection helper using JDBC
│   ├── LoginFrame.java        # Authentication screen for students & admin gate
│   ├── MenuFrame.java         # Browse menu items, filter categories, manage cart
│   ├── OrderFrame.java        # Order checkout, wallet verification, transaction block
│   ├── AdminFrame.java        # Admin dashboard running sales reports & daily logs
│   └── Main.java              # Entry point of the application
│
├── sql/
│   ├── create_tables.sql      # Database schema (Student, Counter, Menu_Item, etc.)
│   ├── trigger.sql            # stock deduction trigger (after_order_item_insert)
│   ├── stored_procedure.sql   # GenerateDailySalesReport stored procedure
│   └── sample_data.sql        # Prepopulated Indian campus food court records
│
├── docs/
│   ├── ER_Diagram.png         # Database Entity-Relationship Diagram (ERD)
│   └── Project_Report.docx    # Professional generated MS Word Project Report
│
└── lib/
    └── mysql-connector-java.jar # JDBC Driver for MySQL connectivity
```

---

## 📊 Database Schema Design

The system runs on a MySQL database named `food_court_db` consisting of 5 normalized tables:

### 1. Student Table
* Stores registered student profiles and current digital wallet balances.
* `student_id` (INT, Primary Key, Auto-Increment)
* `name` (VARCHAR(100), Not Null)
* `email` (VARCHAR(100), Unique)
* `phone` (VARCHAR(15), Not Null)
* `wallet_balance` (DECIMAL(8,2), Default 0.00)

### 2. Counter Table
* Identifies food counters/stalls inside the campus court.
* `counter_id` (INT, Primary Key, Auto-Increment)
* `counter_name` (VARCHAR(100), Not Null)
* `location` (VARCHAR(100))
* `operator_name` (VARCHAR(100))

### 3. Menu_Item Table
* Lists specific food items mapped to respective stalls and available stock counts.
* `item_id` (INT, Primary Key, Auto-Increment)
* `counter_id` (INT, Foreign Key referencing `Counter(counter_id)`)
* `item_name` (VARCHAR(100), Not Null)
* `price` (DECIMAL(6,2), Not Null)
* `category` (VARCHAR(50))
* `stock_qty` (INT, Default 0)

### 4. Orders Table
* Captures transaction headers for payments made by students.
* `order_id` (INT, Primary Key, Auto-Increment)
* `student_id` (INT, Foreign Key referencing `Student(student_id)`)
* `order_date` (DATETIME, Default NOW())
* `total_amount` (DECIMAL(8,2), Not Null)
* `status` (VARCHAR(20), Default 'Pending')

### 5. Order_Item Table
* Records detail lines mapping orders to menu items, quantities, and transactional unit prices.
* `order_item_id` (INT, Primary Key, Auto-Increment)
* `order_id` (INT, Foreign Key referencing `Orders(order_id)`)
* `item_id` (INT, Foreign Key referencing `Menu_Item(item_id)`)
* `quantity` (INT, Not Null)
* `unit_price` (DECIMAL(6,2), Not Null)

---

## ⚙️ Trigger & Stored Procedure Details

### MySQL Trigger: `after_order_item_insert`
* **Type**: `AFTER INSERT` on `Order_Item`
* **Logic**: Whenever a new row is inserted into `Order_Item`, the trigger automatically updates `Menu_Item`, subtracting the ordered `quantity` from the matching item's `stock_qty`.
* **Benefit**: Ensures live front-of-house stock count without running multiple SQL queries from the Java client application.

### MySQL Stored Procedure: `GenerateDailySalesReport`
* **Signature**: `GenerateDailySalesReport(IN report_date DATE)`
* **Returns**:
  1. `TotalOrders`: Total number of orders placed on `report_date`.
  2. `TotalRevenue`: Sum of order totals on `report_date`.
  3. `BestSellingItem`: Name of the menu item with the highest quantity sold on `report_date`.
* **Benefit**: Leverages MySQL's aggregate functions and query optimizer on the database side rather than loading and processing bulk datasets in Java memory.

---

## 🗺️ Entity-Relationship (ER) Diagram Description

The database schema utilizes standard relational links:
* **Student to Orders**: `1-to-Many` relationship. A student can place multiple orders over time. Linked via `student_id`.
* **Counter to Menu_Item**: `1-to-Many` relationship. A counter (e.g. "South Express") hosts multiple menu items (e.g. Masala Dosa, Idli). Linked via `counter_id`.
* **Orders to Order_Item**: `1-to-Many` relationship. An order receipt consists of one or more food line items. Linked via `order_id`.
* **Menu_Item to Order_Item**: `1-to-Many` relationship. A menu item can be sold across multiple receipts. Linked via `item_id`.

---

## 🚀 Setup & Execution Guide

### 1. MySQL Workbench Database Setup
1. Launch **MySQL Workbench** and connect to your local MySQL instance.
2. Open and run `sql/create_tables.sql` to initialize the database and tables.
3. Open and run `sql/trigger.sql` to compile the inventory trigger.
4. Open and run `sql/stored_procedure.sql` to compile the reporting procedure.
5. Open and run `sql/sample_data.sql` to load standard data.
6. Alternatively, execute from the MySQL CLI:
   ```sql
   source sql/create_tables.sql;
   source sql/trigger.sql;
   source sql/stored_procedure.sql;
   source sql/sample_data.sql;
   ```

### 2. Configure Java Connectivity (Credentials)
If your local MySQL root password is not `root`, update static values in `src/DBConnection.java`:
```java
private static final String USER = "your_mysql_username";
private static final String PASSWORD = "your_mysql_password";
```

### 3. Importing and Running inside IDEs

#### A. IntelliJ IDEA
1. Open IntelliJ, select **New Project** -> **From Existing Sources** or click **Open** and select the `FoodCourtMS/` folder.
2. Set the Project JDK (Java SE 8 or newer, e.g. Java 17/21/25).
3. **Add the JDBC Driver JAR**:
   * Go to **File** -> **Project Structure** -> **Libraries**.
   * Click **Add New Library (+)** -> **Java**.
   * Browse and select the file `FoodCourtMS/lib/mysql-connector-java.jar`.
   * Click **Apply** and **OK**.
4. Right-click `src/Main.java` and select **Run 'Main.main()'**.

#### B. Eclipse IDE
1. Open Eclipse, select **File** -> **Import** -> **General** -> **Existing Projects into Workspace**.
2. Select root directory `FoodCourtMS/` and click **Finish**.
3. **Configure Build Path**:
   * Right-click the project -> **Build Path** -> **Configure Build Path...**
   * Select the **Libraries** tab.
   * Highlight **Classpath** (if Eclipse versions show Classpath/Modulepath) and click **Add External JARs...**
   * Select `mysql-connector-java.jar` from the `lib/` folder.
   * Click **Apply and Close**.
4. Right-click `src/Main.java` -> **Run As** -> **Java Application**.

---

## 📋 Ready-to-Test Demo Scenarios

### Test Case 1: Student Login & Shopping Cart
* **Credentials**:
  * **Student ID**: `1`
  * **Phone Number**: `9876543210`
* **Steps**:
  1. Log in. Check the header to confirm "Amit Sharma" is logged in with a wallet balance of `₹500.00`.
  2. Select category **South Indian**.
  3. Select **Masala Dosa**, enter Quantity `2`, and click **Add to Cart**.
  4. Notice the cart list updates to show total subtotal `₹140.00`.

### Test Case 2: Placing an Order & Inventory Trigger
* **Steps**:
  1. In the cart sidebar, click **Proceed to Order**. The `OrderFrame` dialog modal opens showing the invoice bill.
  2. Click **Confirm & Pay**.
  3. A success message dialog appears showing Order ID and remaining wallet balance.
  4. Notice the main menu immediately updates the wallet balance to `₹360.00` and reduces the "Available Stock" of Masala Dosa by 2 (e.g. from 56 to 54) automatically via the MySQL trigger.

### Test Case 3: Insufficient Funds & Out-of-Stock Validations
* **Test Case A**: Log in with Student ID `5` (Phone: `9123456780`, Wallet: `₹150.00`). Try to order 2x Paneer Butter Masala Combo (Total cost ₹240.00). Click confirm: Swing displays an error indicating insufficient funds.
* **Test Case B**: Try adding a quantity of `70` for **Cold Coffee** (Stock: 38). Swing immediately displays a validation dialog indicating insufficient stock.

### Test Case 4: Admin Dashboard & Callable Procedure
* **Steps**:
  1. From the main login screen, click **Admin Portal**.
  2. Enter the password: `admin123`.
  3. The dashboard opens. Select the date `2026-05-22` (Today's date) using the date spinner and click **Generate Daily Report**.
  4. The top table calls the stored procedure and displays:
     * Total Orders: `5` (or more based on checkout tests)
     * Total Revenue: compiled amount
     * Best Selling Item: `Masala Chai` (or Dosa based on checks)
  5. The bottom table displays a log of each detailed order placed today.
