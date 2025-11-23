CREATE DATABASE IF NOT EXISTS pr1;
USE pr1;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('ADMIN', 'CUSTOMER') DEFAULT 'CUSTOMER',
  address VARCHAR(255),
  loyalty_points INT DEFAULT 0 NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  category ENUM('Console', 'PC', 'Accessory', 'Game', 'Controller', 'GiftCard') NOT NULL DEFAULT 'Game',
  price DOUBLE NOT NULL,
  description TEXT,
  imagePath VARCHAR(255),
  stock INT DEFAULT 0,
  age_rating VARCHAR(10) DEFAULT NULL,
  product_type ENUM('Physical', 'Digital', 'GiftCard') NOT NULL DEFAULT 'Physical'
);

CREATE TABLE IF NOT EXISTS platforms (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL,
  type ENUM('CONSOLE', 'PC', 'HANDHELD', 'MOBILE') NOT NULL,
  manufacturer VARCHAR(50),
  icon_path VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS genres (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL,
  description TEXT,
  icon_path VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_platforms (
  product_id INT NOT NULL,
  platform_id INT NOT NULL,
  PRIMARY KEY (product_id, platform_id),
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  FOREIGN KEY (platform_id) REFERENCES platforms(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS product_genres (
  product_id INT NOT NULL,
  genre_id INT NOT NULL,
  PRIMARY KEY (product_id, genre_id),
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  FOREIGN KEY (genre_id) REFERENCES genres(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  total DOUBLE,
  status ENUM('PENDING','DELIVERED') DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS order_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT,
  product_id INT,
  quantity INT,
  price DOUBLE,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE IF NOT EXISTS loyalty_transactions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  order_id INT NULL,
  points INT NOT NULL,
  transaction_type ENUM('EARNED', 'REDEEMED', 'ADJUSTED') NOT NULL,
  description VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
);

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

CREATE TABLE IF NOT EXISTS promotions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT,
  category VARCHAR(50),
  discount DOUBLE NOT NULL CHECK (discount >= 0 AND discount <= 100),
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS wishlist (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  product_id INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  UNIQUE(user_id, product_id)
);

CREATE TABLE IF NOT EXISTS digital_codes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  order_id INT NOT NULL,
  order_item_id INT NOT NULL,
  product_id INT NOT NULL,
  user_id INT NOT NULL,
  code VARCHAR(50) UNIQUE NOT NULL,
  code_type ENUM('GiftCard', 'DigitalDownload') NOT NULL,
  is_redeemed BOOLEAN DEFAULT FALSE,
  redeemed_at TIMESTAMP NULL,
  sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  original_value DOUBLE DEFAULT 0,
  balance DOUBLE DEFAULT 0,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_loyalty_user ON loyalty_transactions(user_id);
CREATE INDEX idx_loyalty_date ON loyalty_transactions(created_at);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_reviews_product ON review(product_id);
CREATE INDEX idx_reviews_user ON review(user_id);
CREATE INDEX idx_code ON digital_codes(code);
CREATE INDEX idx_user_codes ON digital_codes(user_id);
CREATE INDEX idx_order_codes ON digital_codes(order_id);

INSERT IGNORE INTO users (name, email, password_hash, role, address) VALUES
('Admin User', 'admin@gmail.com', '$2a$12$dfo94.3rCAQoEjwlnvaFkeodq52n0S2lld5GrW6M02/sFrcypzYCO', 'ADMIN', 'HQ Building'),
('John Doe', 'john@example.com', '$2a$12$ExampleHashedPasswordHere', 'CUSTOMER', 'Beirut, Lebanon');

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

INSERT INTO products (name, category, price, description, imagePath, stock, age_rating, product_type) VALUES
('PlayStation 5', 'Console', 499.99, 'Next-gen Sony console with ultra-fast SSD and 4K gaming.', 'uploads/ps5.jpg', 25, NULL, 'Physical'),
('Xbox Series X', 'Console', 479.99, 'Powerful Microsoft console with 1TB SSD and HDR support.', 'uploads/xbox_series_x.jpg', 18, NULL, 'Physical'),
('Nintendo Switch OLED', 'Console', 349.99, 'Hybrid handheld console with vibrant OLED display.', 'uploads/switch_oled.jpg', 20, NULL, 'Physical'),
('Steam Deck', 'Console', 399.99, 'Portable gaming PC powered by SteamOS.', 'uploads/steam_deck.jpg', 15, NULL, 'Physical'),
('PlayStation 4 Slim', 'Console', 299.99, 'Reliable 1080p gaming console with 500GB HDD.', 'uploads/ps4_slim.jpg', 12, NULL, 'Physical'),
('Gaming PC Tower RTX4070', 'PC', 1599.00, 'High-end gaming rig with RTX 4070 and Ryzen 7 CPU.', 'uploads/gaming_pc.jpg', 8, NULL, 'Physical'),
('Alienware Aurora R16', 'PC', 2299.00, 'Premium Alienware gaming desktop with liquid cooling.', 'uploads/alienware_r16.jpg', 6, NULL, 'Physical'),
('Asus ROG Strix Laptop', 'PC', 1299.00, '17-inch gaming laptop with 144Hz screen.', 'uploads/asus_rog.jpg', 10, NULL, 'Physical'),
('Logitech G Pro Keyboard', 'Accessory', 149.99, 'Mechanical keyboard with RGB lighting.', 'uploads/logitech_gpro_keyboard.jpg', 40, NULL, 'Physical'),
('Razer DeathAdder Mouse', 'Accessory', 59.99, 'Ergonomic RGB mouse for FPS gamers.', 'uploads/razer_mouse.jpg', 50, NULL, 'Physical'),
('HyperX Cloud II Headset', 'Accessory', 99.99, 'Comfortable surround sound gaming headset.', 'uploads/hyperx_cloud_ii.jpg', 35, NULL, 'Physical'),
('Elgato Stream Deck', 'Accessory', 149.99, 'Customizable streaming control panel.', 'uploads/elgato_streamdeck.jpg', 15, NULL, 'Physical'),
('Samsung Odyssey G9', 'Accessory', 1299.00, 'Ultra-wide curved 49" QLED gaming monitor.', 'uploads/odyssey_g9.jpg', 7, NULL, 'Physical'),
('Corsair RM850 PSU', 'Accessory', 139.00, '850W modular power supply for gaming PCs.', 'uploads/corsair_rm850.jpg', 20, NULL, 'Physical'),
('ASUS TUF Motherboard', 'Accessory', 179.00, 'Durable gaming motherboard with RGB lighting.', 'uploads/tuf_mobo.jpg', 12, NULL, 'Physical'),
('Call of Duty: MW3', 'Game', 69.99, 'The iconic shooter franchise returns with intense multiplayer.', 'uploads/mw3.jpg', 100, 'M', 'Physical'),
('EA Sports FC 25', 'Game', 59.99, 'Next-gen football simulation with dynamic AI.', 'uploads/fc25.jpg', 120, 'E', 'Physical'),
('Spider-Man 2', 'Game', 69.99, 'Open-world superhero adventure on PS5.', 'uploads/spiderman2.jpg', 90, 'T', 'Physical'),
('Elden Ring', 'Game', 59.99, 'Open world action RPG developed by FromSoftware.', 'uploads/elden_ring.jpg', 70, 'M', 'Physical'),
('Cyberpunk 2077', 'Game', 49.99, 'Futuristic RPG with ray tracing and mod support.', 'uploads/cyberpunk2077.jpg', 85, 'M', 'Physical'),
('DualSense PS5 Controller', 'Controller', 69.99, 'Haptic feedback and adaptive triggers.', 'uploads/dualsense.jpg', 80, NULL, 'Physical'),
('Xbox Elite Series 2', 'Controller', 179.99, 'High-end customizable Xbox controller.', 'uploads/xbox_elite2.jpg', 60, NULL, 'Physical'),
('Nintendo Pro Controller', 'Controller', 69.99, 'Wireless controller for Switch with long battery life.', 'uploads/nintendo_pro.jpg', 45, NULL, 'Physical'),
('Razer Wolverine V2', 'Controller', 99.99, 'Wired controller with RGB lighting and remappable buttons.', 'uploads/razer_wolverine.jpg', 50, NULL, 'Physical'),
('8BitDo SN30 Pro', 'Controller', 49.99, 'Retro wireless controller compatible with PC & Switch.', 'uploads/8bitdo_sn30.jpg', 40, NULL, 'Physical'),
('$10 Gaming Gift Card', 'GiftCard', 10.00, 'Digital gift card code worth $10. Redeemable for any products in our store.', 'uploads/giftcard_10.jpg', 999, NULL, 'GiftCard'),
('$25 Gaming Gift Card', 'GiftCard', 25.00, 'Digital gift card code worth $25. Redeemable for any products in our store.', 'uploads/giftcard_25.jpg', 999, NULL, 'GiftCard'),
('$50 Gaming Gift Card', 'GiftCard', 50.00, 'Digital gift card code worth $50. Redeemable for any products in our store.', 'uploads/giftcard_50.jpg', 999, NULL, 'GiftCard'),
('$100 Gaming Gift Card', 'GiftCard', 100.00, 'Digital gift card code worth $100. Redeemable for any products in our store.', 'uploads/giftcard_100.jpg', 999, NULL, 'GiftCard')
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id FROM products p CROSS JOIN platforms pl
WHERE p.category = 'Game' AND p.name IN ('Call of Duty: MW3', 'EA Sports FC 25', 'Cyberpunk 2077', 'Elden Ring')
  AND pl.name IN ('PlayStation 5', 'Xbox Series X/S', 'PC (Windows)')
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id FROM products p, platforms pl
WHERE p.name = 'Spider-Man 2' AND pl.name = 'PlayStation 5'
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id FROM products p, platforms pl
WHERE p.name = 'DualSense PS5 Controller' AND pl.name IN ('PlayStation 5', 'PC (Windows)')
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id FROM products p, platforms pl
WHERE p.name IN ('Xbox Elite Series 2', 'Razer Wolverine V2')
  AND pl.name IN ('Xbox Series X/S', 'Xbox One', 'PC (Windows)')
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id FROM products p, platforms pl
WHERE p.name = 'Nintendo Pro Controller' AND pl.name = 'Nintendo Switch'
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id FROM products p, platforms pl
WHERE p.name = '8BitDo SN30 Pro' AND pl.name IN ('Nintendo Switch', 'PC (Windows)')
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id FROM products p, platforms pl
WHERE p.category = 'PC' AND pl.type = 'PC'
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id FROM products p CROSS JOIN platforms pl
WHERE p.category = 'Accessory' AND p.name NOT IN ('Corsair RM850 PSU', 'ASUS TUF Motherboard')
  AND pl.type IN ('CONSOLE', 'PC', 'HANDHELD')
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id FROM products p, platforms pl
WHERE p.name IN ('Corsair RM850 PSU', 'ASUS TUF Motherboard') AND pl.type = 'PC'
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_platforms (product_id, platform_id)
SELECT p.id, pl.id FROM products p, platforms pl
WHERE p.category = 'Console' AND (
  (p.name LIKE '%PlayStation 5%' AND pl.name = 'PlayStation 5') OR
  (p.name LIKE '%PlayStation 4%' AND pl.name = 'PlayStation 4') OR
  (p.name LIKE '%Xbox Series X%' AND pl.name = 'Xbox Series X/S') OR
  (p.name LIKE '%Nintendo Switch%' AND pl.name = 'Nintendo Switch') OR
  (p.name LIKE '%Steam Deck%' AND pl.name = 'Steam Deck')
)
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_genres (product_id, genre_id)
SELECT p.id, g.id FROM products p, genres g
WHERE p.name = 'Call of Duty: MW3' AND g.name IN ('FPS', 'Action', 'Multiplayer')
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_genres (product_id, genre_id)
SELECT p.id, g.id FROM products p, genres g
WHERE p.name = 'EA Sports FC 25' AND g.name IN ('Sports', 'Simulation', 'Multiplayer')
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_genres (product_id, genre_id)
SELECT p.id, g.id FROM products p, genres g
WHERE p.name = 'Spider-Man 2' AND g.name IN ('Action', 'Adventure', 'Open World')
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_genres (product_id, genre_id)
SELECT p.id, g.id FROM products p, genres g
WHERE p.name = 'Elden Ring' AND g.name IN ('RPG', 'Action', 'Open World')
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO product_genres (product_id, genre_id)
SELECT p.id, g.id FROM products p, genres g
WHERE p.name = 'Cyberpunk 2077' AND g.name IN ('RPG', 'Action', 'Open World', 'FPS')
ON DUPLICATE KEY UPDATE product_id=product_id;

INSERT INTO orders (user_id, total, status, created_at) VALUES
(2, 129.99, 'DELIVERED', '2024-11-10 14:20:00'),
(2, 249.50, 'PENDING', '2024-10-05 10:00:00'),
(2, 499.00, 'DELIVERED', '2024-09-21 18:45:00')
ON DUPLICATE KEY UPDATE id=id;
