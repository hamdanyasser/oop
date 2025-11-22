-- ========================================
-- DATABASE SCHEMA - ShopEase Gaming E-Commerce
-- This is the master schema file
-- Update this file whenever database changes are made
-- ========================================

CREATE DATABASE IF NOT EXISTS pr1;
USE pr1;

-- ========================================
-- 1. USERS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('ADMIN', 'CUSTOMER') DEFAULT 'CUSTOMER',
  address VARCHAR(255),
  loyalty_points INT DEFAULT 0 NOT NULL COMMENT 'Customer loyalty points: 10 points = $1 spent, 1000 points = $10 discount'
);

-- ========================================
-- 2. LOYALTY TRANSACTIONS TABLE
-- Track history of points earned and spent
-- ========================================
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
-- 3. PRODUCTS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  category ENUM('Console', 'PC', 'Accessory', 'Game', 'Controller', 'GiftCard') NOT NULL DEFAULT 'Game',
  price DOUBLE NOT NULL,
  description TEXT,
  imagePath VARCHAR(255),
  stock INT DEFAULT 0,
  age_rating VARCHAR(10) DEFAULT NULL COMMENT 'ESRB: E, E10+, T, M, AO or PEGI: 3, 7, 12, 16, 18',
  product_type ENUM('Physical', 'Digital', 'GiftCard') NOT NULL DEFAULT 'Physical' COMMENT 'Physical: shipped items, Digital: download codes, GiftCard: redeemable codes'
);

-- ========================================
-- 3. PLATFORMS TABLE
-- Gaming platforms (PS5, Xbox, Switch, PC, etc.)
-- ========================================
CREATE TABLE IF NOT EXISTS platforms (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL,
  type ENUM('CONSOLE', 'PC', 'HANDHELD', 'MOBILE') NOT NULL,
  manufacturer VARCHAR(50),
  icon_path VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- 4. PRODUCT_PLATFORMS TABLE
-- Many-to-Many relationship: Products can support multiple platforms
-- ========================================
CREATE TABLE IF NOT EXISTS product_platforms (
  product_id INT NOT NULL,
  platform_id INT NOT NULL,
  PRIMARY KEY (product_id, platform_id),
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  FOREIGN KEY (platform_id) REFERENCES platforms(id) ON DELETE CASCADE
);

-- ========================================
-- 5. GENRES TABLE
-- Game genres (FPS, RPG, Sports, Racing, etc.)
-- ========================================
CREATE TABLE IF NOT EXISTS genres (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL,
  description TEXT,
  icon_path VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- 6. PRODUCT_GENRES TABLE
-- Many-to-Many relationship: Products can have multiple genres
-- ========================================
CREATE TABLE IF NOT EXISTS product_genres (
  product_id INT NOT NULL,
  genre_id INT NOT NULL,
  PRIMARY KEY (product_id, genre_id),
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

-- ========================================
-- 7. ORDERS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  total DOUBLE,
  status ENUM('PENDING','DELIVERED') DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ========================================
-- 8. ORDER_ITEMS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS order_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT,
  product_id INT,
  quantity INT,
  price DOUBLE,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id)
);

-- ========================================
-- 9. REVIEWS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS review (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT NOT NULL,
  user_id INT NOT NULL,
  rating INT CHECK (rating BETWEEN 1 AND 5),
  comment VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ========================================
-- 10. PROMOTIONS TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS promotions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT,
  category VARCHAR(50),
  discount DOUBLE NOT NULL CHECK (discount >= 0 AND discount <= 100),
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ========================================
-- 11. WISHLIST TABLE
-- ========================================
CREATE TABLE IF NOT EXISTS wishlist (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  product_id INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  UNIQUE(user_id, product_id)
);

-- ========================================
-- 12. DIGITAL CODES TABLE
-- Track gift cards and digital product codes sent to customers
-- ========================================
CREATE TABLE IF NOT EXISTS digital_codes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  order_item_id INT NOT NULL,
  product_id INT NOT NULL,
  user_id INT NOT NULL,
  code VARCHAR(50) UNIQUE NOT NULL COMMENT 'Unique redemption code',
  code_type ENUM('GiftCard', 'DigitalDownload') NOT NULL,
  is_redeemed BOOLEAN DEFAULT FALSE,
  redeemed_at TIMESTAMP NULL,
  sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_code (code),
  INDEX idx_user_codes (user_id),
  INDEX idx_order_codes (order_id)
);

-- ========================================
-- 13. INDEXES FOR PERFORMANCE
-- ========================================
CREATE INDEX IF NOT EXISTS idx_product_platforms_product ON product_platforms(product_id);
CREATE INDEX IF NOT EXISTS idx_product_platforms_platform ON product_platforms(platform_id);
CREATE INDEX IF NOT EXISTS idx_platforms_type ON platforms(type);
CREATE INDEX IF NOT EXISTS idx_product_genres_product ON product_genres(product_id);
CREATE INDEX IF NOT EXISTS idx_product_genres_genre ON product_genres(genre_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_reviews_product ON review(product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user ON review(user_id);

-- ========================================
-- 13. VIEWS
-- ========================================
CREATE OR REPLACE VIEW v_products_with_platforms AS
SELECT
  p.*,
  GROUP_CONCAT(DISTINCT pl.name ORDER BY pl.name SEPARATOR ', ') AS platform_names,
  GROUP_CONCAT(DISTINCT pl.id ORDER BY pl.name SEPARATOR ',') AS platform_ids
FROM products p
LEFT JOIN product_platforms pp ON p.id = pp.product_id
LEFT JOIN platforms pl ON pp.platform_id = pl.id
GROUP BY p.id;

CREATE OR REPLACE VIEW v_products_with_genres AS
SELECT
  p.*,
  GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS genre_names,
  GROUP_CONCAT(DISTINCT g.id ORDER BY g.name SEPARATOR ',') AS genre_ids
FROM products p
LEFT JOIN product_genres pg ON p.id = pg.product_id
LEFT JOIN genres g ON pg.genre_id = g.id
GROUP BY p.id;

CREATE OR REPLACE VIEW v_products_complete AS
SELECT
  p.*,
  GROUP_CONCAT(DISTINCT pl.name ORDER BY pl.name SEPARATOR ', ') AS platform_names,
  GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS genre_names
FROM products p
LEFT JOIN product_platforms pp ON p.id = pp.product_id
LEFT JOIN platforms pl ON pp.platform_id = pl.id
LEFT JOIN product_genres pg ON p.id = pg.product_id
LEFT JOIN genres g ON pg.genre_id = g.id
GROUP BY p.id;

-- ========================================
-- SAMPLE DATA
-- ========================================

-- Default Users
INSERT IGNORE INTO users (name, email, password_hash, role, address) VALUES
('Admin User', 'admin@gmail.com', '$2a$12$dfo94.3rCAQoEjwlnvaFkeodq52n0S2lld5GrW6M02/sFrcypzYCO', 'ADMIN', 'HQ Building'),
('John Doe', 'john@example.com', '$2a$12$ExampleHashedPasswordHere', 'CUSTOMER', 'Beirut, Lebanon');

-- Gaming Platforms
INSERT IGNORE INTO platforms (name, type, manufacturer, icon_path) VALUES
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

-- Game Genres
INSERT IGNORE INTO genres (name, description, icon_path) VALUES
('Action', 'Fast-paced games focused on physical challenges', 'icons/action.png'),
('Adventure', 'Story-driven exploration games', 'icons/adventure.png'),
('RPG', 'Role-playing games with character progression', 'icons/rpg.png'),
('FPS', 'First-person shooter games', 'icons/fps.png'),
('Sports', 'Athletic and competitive sports games', 'icons/sports.png'),
('Racing', 'Vehicle racing and driving games', 'icons/racing.png'),
('Strategy', 'Tactical and strategic gameplay', 'icons/strategy.png'),
('Simulation', 'Real-world simulation games', 'icons/simulation.png'),
('Fighting', 'One-on-one combat games', 'icons/fighting.png'),
('Horror', 'Scary and survival horror games', 'icons/horror.png'),
('Open World', 'Large explorable game worlds', 'icons/openworld.png'),
('Multiplayer', 'Online multiplayer focused games', 'icons/multiplayer.png'),
('Platformer', 'Jump and run platforming games', 'icons/platformer.png'),
('Puzzle', 'Logic and problem-solving games', 'icons/puzzle.png'),
('Survival', 'Resource management and survival games', 'icons/survival.png');

-- Sample Products
INSERT INTO products (name, category, price, description, imagePath, stock, age_rating) VALUES
-- 🎮 Consoles
('PlayStation 5', 'Console', 499.99, 'Next-gen Sony console with ultra-fast SSD and 4K gaming.', 'uploads/ps5.jpg', 25, NULL),
('Xbox Series X', 'Console', 479.99, 'Powerful Microsoft console with 1TB SSD and HDR support.', 'uploads/xbox_series_x.jpg', 18, NULL),
('Nintendo Switch OLED', 'Console', 349.99, 'Hybrid handheld console with vibrant OLED display.', 'uploads/switch_oled.jpg', 20, NULL),
('Steam Deck', 'Console', 399.99, 'Portable gaming PC powered by SteamOS.', 'uploads/steam_deck.jpg', 15, NULL),
('PlayStation 4 Slim', 'Console', 299.99, 'Reliable 1080p gaming console with 500GB HDD.', 'uploads/ps4_slim.jpg', 12, NULL),

-- 💻 PC Gear
('Gaming PC Tower RTX4070', 'PC', 1599.00, 'High-end gaming rig with RTX 4070 and Ryzen 7 CPU.', 'uploads/gaming_pc.jpg', 8, NULL),
('Alienware Aurora R16', 'PC', 2299.00, 'Premium Alienware gaming desktop with liquid cooling.', 'uploads/alienware_r16.jpg', 6, NULL),
('Asus ROG Strix Laptop', 'PC', 1299.00, '17-inch gaming laptop with 144Hz screen.', 'uploads/asus_rog.jpg', 10, NULL),
('Logitech G Pro Keyboard', 'Accessory', 149.99, 'Mechanical keyboard with RGB lighting.', 'uploads/logitech_gpro_keyboard.jpg', 40, NULL),
('Razer DeathAdder Mouse', 'Accessory', 59.99, 'Ergonomic RGB mouse for FPS gamers.', 'uploads/razer_mouse.jpg', 50, NULL),

-- 🎧 Accessories
('HyperX Cloud II Headset', 'Accessory', 99.99, 'Comfortable surround sound gaming headset.', 'uploads/hyperx_cloud_ii.jpg', 35, NULL),
('Elgato Stream Deck', 'Accessory', 149.99, 'Customizable streaming control panel.', 'uploads/elgato_streamdeck.jpg', 15, NULL),
('Samsung Odyssey G9', 'Accessory', 1299.00, 'Ultra-wide curved 49" QLED gaming monitor.', 'uploads/odyssey_g9.jpg', 7, NULL),
('Corsair RM850 PSU', 'Accessory', 139.00, '850W modular power supply for gaming PCs.', 'uploads/corsair_rm850.jpg', 20, NULL),
('ASUS TUF Motherboard', 'Accessory', 179.00, 'Durable gaming motherboard with RGB lighting.', 'uploads/tuf_mobo.jpg', 12, NULL),

-- 🕹 Games (with ESRB ratings)
('Call of Duty: MW3', 'Game', 69.99, 'The iconic shooter franchise returns with intense multiplayer.', 'uploads/mw3.jpg', 100, 'M'),
('EA Sports FC 25', 'Game', 59.99, 'Next-gen football simulation with dynamic AI.', 'uploads/fc25.jpg', 120, 'E'),
('Spider-Man 2', 'Game', 69.99, 'Open-world superhero adventure on PS5.', 'uploads/spiderman2.jpg', 90, 'T'),
('Elden Ring', 'Game', 59.99, 'Open world action RPG developed by FromSoftware.', 'uploads/elden_ring.jpg', 70, 'M'),
('Cyberpunk 2077', 'Game', 49.99, 'Futuristic RPG with ray tracing and mod support.', 'uploads/cyberpunk2077.jpg', 85, 'M'),

-- 🎮 Controllers
('DualSense PS5 Controller', 'Controller', 69.99, 'Haptic feedback and adaptive triggers.', 'uploads/dualsense.jpg', 80, NULL),
('Xbox Elite Series 2', 'Controller', 179.99, 'High-end customizable Xbox controller.', 'uploads/xbox_elite2.jpg', 60, NULL),
('Nintendo Pro Controller', 'Controller', 69.99, 'Wireless controller for Switch with long battery life.', 'uploads/nintendo_pro.jpg', 45, NULL),
('Razer Wolverine V2', 'Controller', 99.99, 'Wired controller with RGB lighting and remappable buttons.', 'uploads/razer_wolverine.jpg', 50, NULL),
('8BitDo SN30 Pro', 'Controller', 49.99, 'Retro wireless controller compatible with PC & Switch.', 'uploads/8bitdo_sn30.jpg', 40, NULL)
ON DUPLICATE KEY UPDATE id=id;

-- Product-Platform Mappings
-- Cross-platform games
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p
CROSS JOIN platforms pl
WHERE p.category = 'Game'
  AND p.name IN ('Call of Duty: MW3', 'EA Sports FC 25', 'Cyberpunk 2077', 'Elden Ring')
  AND pl.name IN ('PlayStation 5', 'Xbox Series X/S', 'PC (Windows)')
ON DUPLICATE KEY UPDATE product_id=product_id;

-- PS5 exclusive
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.name = 'Spider-Man 2' AND pl.name = 'PlayStation 5'
ON DUPLICATE KEY UPDATE product_id=product_id;

-- Controllers
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.name = 'DualSense PS5 Controller'
  AND pl.name IN ('PlayStation 5', 'PC (Windows)')
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.name IN ('Xbox Elite Series 2', 'Razer Wolverine V2')
  AND pl.name IN ('Xbox Series X/S', 'Xbox One', 'PC (Windows)')
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.name = 'Nintendo Pro Controller'
  AND pl.name = 'Nintendo Switch'
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.name = '8BitDo SN30 Pro'
  AND pl.name IN ('Nintendo Switch', 'PC (Windows)')
ON DUPLICATE KEY UPDATE product_id=product_id;

-- PC Hardware
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.category = 'PC'
  AND pl.type = 'PC'
ON DUPLICATE KEY UPDATE product_id=product_id;

-- Accessories (cross-platform)
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p
CROSS JOIN platforms pl
WHERE p.category = 'Accessory'
  AND p.name NOT IN ('Corsair RM850 PSU', 'ASUS TUF Motherboard')
  AND pl.type IN ('CONSOLE', 'PC', 'HANDHELD')
ON DUPLICATE KEY UPDATE product_id=product_id;

-- PC-specific accessories
INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id
FROM products p, platforms pl
WHERE p.name IN ('Corsair RM850 PSU', 'ASUS TUF Motherboard')
  AND pl.type = 'PC'
ON DUPLICATE KEY UPDATE product_id=product_id;

-- Consoles
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
  )
ON DUPLICATE KEY UPDATE product_id=product_id;

-- ========================================
-- Product-Genre Mappings
-- ========================================

-- Call of Duty: MW3 - FPS, Action, Multiplayer
INSERT INTO product_genres (product_id, genre_id)
SELECT p.id, g.id
FROM products p, genres g
WHERE p.name = 'Call of Duty: MW3'
  AND g.name IN ('FPS', 'Action', 'Multiplayer')
ON DUPLICATE KEY UPDATE product_id=product_id;

-- EA Sports FC 25 - Sports, Simulation, Multiplayer
INSERT INTO product_genres (product_id, genre_id)
SELECT p.id, g.id
FROM products p, genres g
WHERE p.name = 'EA Sports FC 25'
  AND g.name IN ('Sports', 'Simulation', 'Multiplayer')
ON DUPLICATE KEY UPDATE product_id=product_id;

-- Spider-Man 2 - Action, Adventure, Open World
INSERT INTO product_genres (product_id, genre_id)
SELECT p.id, g.id
FROM products p, genres g
WHERE p.name = 'Spider-Man 2'
  AND g.name IN ('Action', 'Adventure', 'Open World')
ON DUPLICATE KEY UPDATE product_id=product_id;

-- Elden Ring - RPG, Action, Open World
INSERT INTO product_genres (product_id, genre_id)
SELECT p.id, g.id
FROM products p, genres g
WHERE p.name = 'Elden Ring'
  AND g.name IN ('RPG', 'Action', 'Open World')
ON DUPLICATE KEY UPDATE product_id=product_id;

-- Cyberpunk 2077 - RPG, Action, Open World, FPS
INSERT INTO product_genres (product_id, genre_id)
SELECT p.id, g.id
FROM products p, genres g
WHERE p.name = 'Cyberpunk 2077'
  AND g.name IN ('RPG', 'Action', 'Open World', 'FPS')
ON DUPLICATE KEY UPDATE product_id=product_id;

-- Sample Orders
INSERT INTO orders (user_id, total, status, created_at) VALUES
(2, 129.99, 'DELIVERED', '2024-11-10 14:20:00'),
(2, 249.50, 'PENDING',   '2024-10-05 10:00:00'),
(2, 499.00, 'DELIVERED', '2024-09-21 18:45:00')
ON DUPLICATE KEY UPDATE id=id;

-- ========================================
-- Sample Gift Cards
-- ========================================
INSERT INTO products (name, category, price, description, imagePath, stock, age_rating, product_type) VALUES
('$10 Gaming Gift Card', 'GiftCard', 10.00, 'Digital gift card code worth $10. Redeemable for any products in our store.', 'uploads/giftcard_10.jpg', 999, NULL, 'GiftCard'),
('$25 Gaming Gift Card', 'GiftCard', 25.00, 'Digital gift card code worth $25. Redeemable for any products in our store.', 'uploads/giftcard_25.jpg', 999, NULL, 'GiftCard'),
('$50 Gaming Gift Card', 'GiftCard', 50.00, 'Digital gift card code worth $50. Redeemable for any products in our store.', 'uploads/giftcard_50.jpg', 999, NULL, 'GiftCard'),
('$100 Gaming Gift Card', 'GiftCard', 100.00, 'Digital gift card code worth $100. Redeemable for any products in our store.', 'uploads/giftcard_100.jpg', 999, NULL, 'GiftCard')
ON DUPLICATE KEY UPDATE id=id;

-- ========================================
-- END OF SCHEMA
-- Last Updated: 2025-11-22
-- Version: 1.5 (Added Loyalty Points System)
-- ========================================
