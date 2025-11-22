-- ========================================
-- PLATFORM COMPATIBILITY FILTER UPDATE
-- ========================================
-- This script adds platform support to the gaming e-commerce database

USE pr1;

-- 1. Create platforms table (master list of gaming platforms)
CREATE TABLE IF NOT EXISTS platforms (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL,
  type ENUM('CONSOLE', 'PC', 'HANDHELD', 'MOBILE') NOT NULL,
  manufacturer VARCHAR(50),
  icon_path VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Create product_platforms junction table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS product_platforms (
  product_id INT NOT NULL,
  platform_id INT NOT NULL,
  PRIMARY KEY (product_id, platform_id),
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  FOREIGN KEY (platform_id) REFERENCES platforms(id) ON DELETE CASCADE
);

-- 3. Insert common gaming platforms
INSERT INTO platforms (name, type, manufacturer, icon_path) VALUES
('PlayStation 5', 'CONSOLE', 'Sony', 'icons/ps5.png'),
('PlayStation 4', 'CONSOLE', 'Sony', 'icons/ps4.png'),
('Xbox Series X/S', 'CONSOLE', 'Microsoft', 'icons/xbox_series.png'),
('Xbox One', 'CONSOLE', 'Microsoft', 'icons/xbox_one.png'),
('Nintendo Switch', 'HANDHELD', 'Nintendo', 'icons/switch.png'),
('PC (Windows)', 'PC', 'Various', 'icons/windows.png'),
('PC (Steam)', 'PC', 'Valve', 'icons/steam.png'),
('PC (Epic Games)', 'PC', 'Epic', 'icons/epic.png'),
('Steam Deck', 'HANDHELD', 'Valve', 'icons/steamdeck.png'),
('Meta Quest', 'CONSOLE', 'Meta', 'icons/quest.png');

-- 4. Map existing products to platforms based on their names/categories
-- Games - typically cross-platform
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p
CROSS JOIN platforms pl
WHERE p.category = 'Game'
  AND p.name IN ('Call of Duty: MW3', 'EA Sports FC 25', 'Cyberpunk 2077', 'Elden Ring')
  AND pl.name IN ('PlayStation 5', 'Xbox Series X/S', 'PC (Windows)');

-- Spider-Man 2 - PS5 exclusive
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.name = 'Spider-Man 2' AND pl.name = 'PlayStation 5';

-- Controllers - map to their respective platforms
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.name = 'DualSense PS5 Controller'
  AND pl.name IN ('PlayStation 5', 'PC (Windows)');

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.name IN ('Xbox Elite Series 2', 'Razer Wolverine V2')
  AND pl.name IN ('Xbox Series X/S', 'Xbox One', 'PC (Windows)');

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.name = 'Nintendo Pro Controller'
  AND pl.name = 'Nintendo Switch';

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.name = '8BitDo SN30 Pro'
  AND pl.name IN ('Nintendo Switch', 'PC (Windows)');

-- PC Hardware - compatible with PC platforms
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.category = 'PC'
  AND pl.type = 'PC';

-- Accessories - generally cross-platform
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p
CROSS JOIN platforms pl
WHERE p.category = 'Accessory'
  AND p.name NOT IN ('Corsair RM850 PSU', 'ASUS TUF Motherboard')
  AND pl.type IN ('CONSOLE', 'PC', 'HANDHELD');

-- PC-specific accessories
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.name IN ('Corsair RM850 PSU', 'ASUS TUF Motherboard')
  AND pl.type = 'PC';

-- Consoles - map to themselves
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.category = 'Console'
  AND (
    (p.name LIKE '%PlayStation 5%' AND pl.name = 'PlayStation 5') OR
    (p.name LIKE '%PlayStation 4%' AND pl.name = 'PlayStation 4') OR
    (p.name LIKE '%Xbox Series X%' AND pl.name = 'Xbox Series X/S') OR
    (p.name LIKE '%Nintendo Switch%' AND pl.name = 'Nintendo Switch') OR
    (p.name LIKE '%Steam Deck%' AND pl.name = 'Steam Deck')
  );

-- 5. Create indexes for better query performance
CREATE INDEX idx_product_platforms_product ON product_platforms(product_id);
CREATE INDEX idx_product_platforms_platform ON product_platforms(platform_id);
CREATE INDEX idx_platforms_type ON platforms(type);

-- 6. Create view for easy product-platform queries
CREATE OR REPLACE VIEW v_products_with_platforms AS
SELECT
  p.*,
  GROUP_CONCAT(pl.name ORDER BY pl.name SEPARATOR ', ') AS platform_names,
  GROUP_CONCAT(pl.id ORDER BY pl.name SEPARATOR ',') AS platform_ids
FROM products p
LEFT JOIN product_platforms pp ON p.id = pp.product_id
LEFT JOIN platforms pl ON pp.platform_id = pl.id
GROUP BY p.id;

-- ========================================
-- VERIFICATION QUERIES
-- ========================================
-- Uncomment to test:

-- See all platforms
-- SELECT * FROM platforms;

-- See products with their platforms
-- SELECT id, name, category, platform_names FROM v_products_with_platforms;

-- Find all PS5 games
-- SELECT p.*
-- FROM products p
-- JOIN product_platforms pp ON p.id = pp.product_id
-- JOIN platforms pl ON pp.platform_id = pl.id
-- WHERE pl.name = 'PlayStation 5' AND p.category = 'Game';

-- Find cross-platform games (available on 3+ platforms)
-- SELECT p.name, COUNT(pp.platform_id) as platform_count,
--        GROUP_CONCAT(pl.name SEPARATOR ', ') as platforms
-- FROM products p
-- JOIN product_platforms pp ON p.id = pp.product_id
-- JOIN platforms pl ON pp.platform_id = pl.id
-- WHERE p.category = 'Game'
-- GROUP BY p.id
-- HAVING platform_count >= 3;
