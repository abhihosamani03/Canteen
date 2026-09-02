USE food_court_db;

DELIMITER //

CREATE TRIGGER after_order_item_insert
AFTER INSERT ON Order_Item
FOR EACH ROW
BEGIN
    UPDATE Menu_Item
    SET stock_qty = stock_qty - NEW.quantity
    WHERE item_id = NEW.item_id;
END //

DELIMITER ;
