-- ========================================
-- LOYALTY POINTS MIGRATION
-- Run this in MySQL to add loyalty points to existing database
-- ========================================

USE pr1;

-- Add loyalty_points column to users table
ALTER TABLE users
ADD COLUMN IF NOT EXISTS loyalty_points INT DEFAULT 0 NOT NULL
COMMENT 'Customer loyalty points: 10 points = $1 spent, 1000 points = $10 discount';

-- Create loyalty_transactions table
CREATE TABLE IF NOT EXISTS loyalty_transactions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  order_id INT NULL COMMENT 'Order that triggered this transaction (if applicable)',
  points INT NOT NULL COMMENT 'Positive for earned, negative for spent',
  transaction_type ENUM('EARNED', 'REDEEMED', 'ADJUSTED') NOT NULL,
  description VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,
  INDEX idx_loyalty_user (user_id),
  INDEX idx_loyalty_date (created_at)
);

-- ========================================
-- DONE! Your database now supports loyalty points
-- ========================================
