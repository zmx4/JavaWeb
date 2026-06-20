-- 创建数据库
CREATE DATABASE IF NOT EXISTS shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE shop;

-- 注意：users表将由JPA自动创建（ddl-auto: update）
-- 如果需要手动创建表，可以取消下面的注释


CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE  TABLE admin (
    user_id BIGINT PRIMARY KEY,
    foreign key (user_id) references users(id)
);

CREATE TABLE product_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    create_time DATETIME,
    update_time DATETIME,
    INDEX idx_name (name),
    foreign key (category_id) references product_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    create_time DATETIME,
    update_time DATETIME,
    foreign key (user_id) references users(id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    foreign key (order_id) references orders(id),
    foreign key (product_id) references products(id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 订单项表，记录每个订单中的商品信息，包括商品ID、数量和价格
CREATE TABLE shopping_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    create_time DATETIME,
    update_time DATETIME,
    foreign key (user_id) references users(id),
    foreign key (product_id) references products(id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 购物车表，记录用户添加到购物车中的商品信息，包括商品ID和数量
CREATE TABLE shopping_cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    create_time DATETIME,
    update_time DATETIME,
    foreign key (cart_id) references shopping_cart(id),
    foreign key (product_id) references products(id),
    INDEX idx_cart_id (cart_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 地址表，记录用户的收货地址信息
CREATE TABLE addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    recipient_name VARCHAR(255) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    recipient_address TEXT NOT NULL,
    foreign key (user_id) references users(id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    create_time DATETIME,
    update_time DATETIME,
    foreign key (user_id) references users(id),
    foreign key (product_id) references products(id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========== 测试数据 ==========

-- 商品分类
INSERT INTO product_categories (name, description, create_time, update_time) VALUES
('数码电子', '手机、电脑、平板等数码产品', NOW(), NOW()),
('服装鞋包', '男装、女装、鞋子、箱包', NOW(), NOW()),
('食品饮料', '零食、饮品、生鲜等', NOW(), NOW()),
('图书文具', '书籍、文具、办公用品', NOW(), NOW()),
('运动户外', '运动装备、户外用品', NOW(), NOW());

-- 商品数据
INSERT INTO products (category_id, name, description, price, stock, create_time, update_time) VALUES
(1, 'iPhone 16 Pro', 'Apple最新旗舰手机，A18芯片，钛金属设计', 8999.00, 50, '2026-01-15 10:00:00', '2026-01-15 10:00:00'),
(1, 'MacBook Pro 16', 'M4 Pro芯片，Liquid Retina XDR显示屏', 19999.00, 20, '2026-02-01 09:00:00', '2026-02-01 09:00:00'),
(1, 'iPad Air', 'M2芯片，11英寸Liquid Retina显示屏', 4799.00, 35, '2026-02-20 14:00:00', '2026-02-20 14:00:00'),
(1, 'AirPods Pro 3', '自适应音频，主动降噪', 1899.00, 100, '2026-03-10 11:00:00', '2026-03-10 11:00:00'),
(1, 'Sony WH-1000XM6', '业界领先降噪，30小时续航', 2499.00, 40, '2026-03-25 16:00:00', '2026-03-25 16:00:00'),
(2, '春季休闲外套', '轻薄透气，适合春夏季穿着', 299.00, 200, '2026-01-20 08:00:00', '2026-01-20 08:00:00'),
(2, '运动跑步鞋', '轻便缓震，适合日常跑步', 459.00, 150, '2026-02-10 10:30:00', '2026-02-10 10:30:00'),
(2, '真皮双肩背包', '头层牛皮，商务休闲两用', 599.00, 80, '2026-03-05 13:00:00', '2026-03-05 13:00:00'),
(2, '纯棉T恤', '舒适纯棉，多色可选', 99.00, 500, '2026-04-01 09:00:00', '2026-04-01 09:00:00'),
(3, '进口坚果礼盒', '精选6种坚果，送礼佳品', 168.00, 300, '2026-01-25 11:00:00', '2026-01-25 11:00:00'),
(3, '精品咖啡豆', '阿拉比卡咖啡豆，深度烘焙', 89.00, 200, '2026-02-15 15:00:00', '2026-02-15 15:00:00'),
(3, '有机绿茶', '高山有机茶叶，清香回甘', 128.00, 120, '2026-03-20 10:00:00', '2026-03-20 10:00:00'),
(4, '《Java编程思想》', '经典Java教程，第5版', 119.00, 60, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
(4, '钢笔套装', '德国品牌，含墨水和笔尖', 258.00, 45, '2026-02-28 14:30:00', '2026-02-28 14:30:00'),
(4, 'A4打印纸', '80g高白度，500张/包', 35.00, 1000, '2026-04-10 09:00:00', '2026-04-10 09:00:00'),
(5, '瑜伽垫', '6mm加厚防滑，含收纳袋', 129.00, 180, '2026-01-30 10:00:00', '2026-01-30 10:00:00'),
(5, '登山杖', '碳纤维材质，可折叠', 199.00, 90, '2026-03-01 11:30:00', '2026-03-01 11:30:00'),
(5, '露营帐篷', '3-4人双层防暴雨帐篷', 699.00, 30, '2026-04-05 16:00:00', '2026-04-05 16:00:00'),
(5, '公路自行车', '铝合金车架，21速变速', 2399.00, 15, '2026-05-01 09:00:00', '2026-05-01 09:00:00');
