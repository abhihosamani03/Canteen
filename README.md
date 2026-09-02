# 🍽️ Campus Food Court Management System (FoodCourtMS)

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Swing](https://img.shields.io/badge/GUI-Java%20Swing%20(Nimbus)-5382a1?style=for-the-badge)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![JDBC](https://img.shields.io/badge/Database%20Connectivity-JDBC-4A90E2?style=for-the-badge)](https://en.wikipedia.org/wiki/Java_Database_Connectivity)

A desktop database application designed to streamline campus food court operations. Built with **Java Swing**, **JDBC**, and **MySQL 8.x**, this system provides student wallet management, real-time menu ordering, automated inventory deduction via database triggers, and admin business reporting via stored procedures.

---

## 📌 Key Highlights & Features

### 🎓 1. Student Portal
- **Alphanumeric ID Authentication**: Secure login supporting university seat numbers (USN format, e.g., 4SF24CS001).
- **New Student Registration**: Built-in modal with input validation for unique IDs, emails, and phone numbers.
- **Digital Cashless Wallet**: Real-time balance verification to eliminate manual cash transactions.
- **Interactive Menu & Category Filters**: Browse available items categorized into *Meals*, *South Indian*, *Snacks*, *Beverages*, and *Breads*.
- **Live Cart Management**: Add/modify items with instant stock-availability checks.
- **Transactional Checkout**: Enforces ACID properties (checks stock, deducts wallet balance, creates order header, and inserts line items in an atomic database transaction).

### 📊 2. Admin & Management Dashboard
- **Stored Procedure Analytics**: Executes GenerateDailySalesReport to summarize total orders, total revenue, and the best-selling item for any selected date.
- **Detailed Order Audit Logs**: Live feed displaying timestamps, student IDs, counter mappings, and payment amounts.
- **Counter-wise Revenue Analytics**: Tracks earnings across individual stalls (*Spice N Spice*, *South Express*, *Chai & Snacks*).
- **Student Consumption Trends**: Identifies frequent food items, average spend, and department analytics.

### ⚙️ 3. Database Automation & Integrity
- **Automated Stock Deduction**: MySQL AFTER INSERT trigger (fter_order_item_insert) instantly deducts stock quantities upon order placement.
- **Referential Integrity**: Cascading constraints (ON DELETE CASCADE) across 5 relational tables.

---

## 📸 Screenshots & System Previews

| Student Ordering Dashboard | Admin Analytics & Daily Reports |
|:---:|:---:|
| ![Student Dashboard](FoodCourtMS/docs/screenshots/Student%20Dashboard.png) | ![Admin Dashboard](FoodCourtMS/docs/screenshots/Admin%20dasboard.png) |

<p align="center">
  <b>Student Spending Trends & Order History</b><br>
  <img src="FoodCourtMS/docs/screenshots/student%20trends.png" width="80%" alt="Student Trends">
</p>

---

## 🗄️ Database Architecture & Schema

The database consists of **5 normalized relational tables**:

`mermaid
erDiagram
    STUDENT ||--o{ ORDERS : places
    COUNTER ||--o{ MENU_ITEM : offers
    ORDERS ||--|{ ORDER_ITEM : contains
    MENU_ITEM ||--o{ ORDER_ITEM : ordered_in

    STUDENT {
        varchar(50) student_id PK
        varchar(100) name
        varchar(100) email UK
        varchar(15) phone
        decimal(8_2) wallet_balance
    }

    COUNTER {
        int counter_id PK
        varchar(100) counter_name
        varchar(100) location
        varchar(100) operator_name
    }

    MENU_ITEM {
        int item_id PK
        int counter_id FK
        varchar(100) item_name
        decimal(6_2) price
        varchar(50) category
        int stock_qty
    }

    ORDERS {
        int order_id PK
        varchar(50) student_id FK
        datetime order_date
        decimal(8_2) total_amount
        varchar(20) status
    }

    ORDER_ITEM {
        int order_item_id PK
        int order_id FK
        int item_id FK
        int quantity
        decimal(6_2) unit_price
    }
`

---

## 📂 Project Directory Structure

`	ext
FoodCourtMS/
│
├── src/                          # Java Swing source files
│   ├── Main.java                 # Entry point, sets Nimbus UI theme
│   ├── DBConnection.java         # Centralized JDBC connection manager
│   ├── LoginFrame.java           # Authentication & student registration UI
│   ├── MenuFrame.java            # Food ordering, category filter & cart UI
│   ├── OrderFrame.java           # Checkout invoice & transaction processor
│   └── AdminFrame.java           # Admin reporting & analytics dashboard
│
├── sql/                          # MySQL Database scripts
│   ├── create_tables.sql         # Schema DDL (Tables & Triggers)
│   ├── trigger.sql               # Stock deduction trigger definition
│   ├── stored_procedure.sql      # Sales reporting stored procedure
│   └── sample_data.sql           # Prepopulated student & menu records
│
├── docs/                         # Project media and diagrams
│   ├── ER_Diagram.png            # Visual Entity-Relationship Diagram
│   ├── sahyadri-logo.png         # UI logo asset
│   └── screenshots/              # Application screenshots
│
└── lib/                          # External libraries
    └── mysql-connector-java.jar  # MySQL JDBC connector driver
`

---

## 🚀 Setup & Execution Guide

### 1. Database Setup (MySQL)
1. Open **MySQL Workbench** or the **MySQL Command Line Client**.
2. Run the SQL scripts in the following order:
   `sql
   source sql/create_tables.sql;
   source sql/trigger.sql;
   source sql/stored_procedure.sql;
   source sql/sample_data.sql;
   `

### 2. Configure Database Credentials
Open [FoodCourtMS/src/DBConnection.java](FoodCourtMS/src/DBConnection.java) and set your local MySQL username and password:
`java
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";
`

### 3. Compile and Run

#### Option A: Running from Command Line / Terminal
`powershell
# Navigate to the FoodCourtMS directory
cd FoodCourtMS

# Compile Java sources
javac -cp "lib/mysql-connector-java.jar" -d bin src/*.java

# Run the application
java -cp "bin;lib/mysql-connector-java.jar" Main
`

#### Option B: Running in VS Code
1. Open the FoodCourtMS folder in VS Code.
2. Ensure the **Extension Pack for Java** is installed.
3. Open src/Main.java and click **Run** (or press F5).

---

## 🔑 Demo Credentials for Testing

| Role | Identifier / Student ID | Password / Phone | Notes |
| :--- | :--- | :--- | :--- |
| **Student** | 4SF24CS001 | 9876543210 | Amit Sharma (Initial Balance: ₹500.00) |
| **Student** | 4SF24CI005 | 636333 | Abhishek (Initial Balance: ₹1000.00) |
| **Student** | 4SF24CS015 | 9123456780 | Vikram Singh (Initial Balance: ₹150.00) |
| **Admin** | *Click "Admin Portal"* | dmin123 | Access daily revenue & sales reports |

---

## 📜 License
This project is open-source and intended for educational and mini-project database demonstrations.
