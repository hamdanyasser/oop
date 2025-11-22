-- ========================================
-- GIFT CARD BALANCE MIGRATION
-- Run this in MySQL to add gift card balance tracking
-- ========================================

USE pr1;

-- Add balance tracking columns to digital_codes table
ALTER TABLE digital_codes
ADD COLUMN IF NOT EXISTS original_value DOUBLE DEFAULT 0 COMMENT 'Original gift card value (for gift cards only)',
ADD COLUMN IF NOT EXISTS balance DOUBLE DEFAULT 0 COMMENT 'Current gift card balance (for gift cards only)';

-- Update existing gift card codes with their original values
-- This sets the balance equal to the product price
UPDATE digital_codes dc
INNER JOIN products p ON dc.product_id = p.id
SET dc.original_value = p.price,
    dc.balance = p.price
WHERE dc.code_type = 'GiftCard';

-- ========================================
-- DONE! Your database now supports gift card redemption
-- ========================================
