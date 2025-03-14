CREATE DATABASE IF NOT EXISTS ecommerce;
USE ecommerce;

CREATE TABLE Users (
    phone_number VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.0
);

CREATE TABLE Transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_phone VARCHAR(20),
    receiver_phone VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_phone) REFERENCES Users(phone_number),
    FOREIGN KEY (receiver_phone) REFERENCES Users(phone_number)
);

INSERT INTO Users (phone_number, name, password, balance)
VALUES 
    ('1111111111', 'Ariyan', 'ariyanpass', 1000.00),
    ('2222222222', 'Mohtasir', 'mohtasirpass', 1500.00),
    ('3333333333', 'Shisita', 'shisitapass', 2000.00),
    ('4444444444', 'Tasfin', 'tasfinpass', 1200.00),
    ('5555555555', 'Dishad', 'dishadpass', 800.00);
    
SELECT * FROM Users ;
SELECT * FROM Transactions ;


/* 
  zdi database theke ager sign in fo delete korte cai taile prothome transaction table drop kore user table drop korte hbe hbeee
  update korte parbo update command diye
*/
