CREATE TABLE IF NOT EXISTS Bank_account
(
    id             INTEGER AUTO_INCREMENT PRIMARY KEY,
    bank_name      VARCHAR(100),
    account_number VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS Users
(
    id            INTEGER AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(255) NOT NULL,
    phone         VARCHAR(20)  NOT NULL UNIQUE,
    wallet INTEGER ,
    email         VARCHAR(255),
    password_hash VARCHAR(255) NOT NULL,
    `role`        ENUM ('buyer',
        'seller',
        'courier',
        'admin'
        )                      NOT NULL,
    address       TEXT         NOT NULL,
    profile_image LONGTEXT     NULL,
    bank_id       INTEGER,
    confirmed     BOOLEAN,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bank_id) REFERENCES Bank_account (id)
);

CREATE TABLE IF NOT EXISTS Restaurants
(
    id             INTEGER AUTO_INCREMENT PRIMARY KEY,
    owner_id       INTEGER      NOT NULL,
    `name`         VARCHAR(255) NOT NULL,
    address        TEXT         NOT NULL,
    phone          VARCHAR(20)  NOT NULL,
    logo           LONGTEXT     NULL,
    tax_fee        INTEGER   DEFAULT 0,
    additional_fee INTEGER   DEFAULT 0,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES Users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `Keywords`
(
    id   INTEGER AUTO_INCREMENT PRIMARY KEY,
    food_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    FOREIGN KEY (food_id) REFERENCES fooditems(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Menus
(
    id            INTEGER AUTO_INCREMENT PRIMARY KEY,
    restaurant_id INTEGER      NOT NULL,
    title         VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (restaurant_id, title),
    FOREIGN KEY (restaurant_id) REFERENCES Restaurants (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS FoodItems
(
    id            INTEGER AUTO_INCREMENT PRIMARY KEY,
    restaurant_id INTEGER      NOT NULL,
    name          VARCHAR(255) NOT NULL,
    image         LONGTEXT     NULL,
    description   TEXT         NOT NULL,
    price         INTEGER      NOT NULL,
    supply        INTEGER      NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (restaurant_id) REFERENCES Restaurants (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS FoodItem_menus(
    food_item_id INTEGER NOT NULL,
    menu_id INTEGER NOT NULL,
    PRIMARY KEY (food_item_id, menu_id),
    FOREIGN KEY (food_item_id) REFERENCES FoodItems (id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES menus (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Coupons
(
    id          INTEGER AUTO_INCREMENT PRIMARY KEY,
    coupon_code VARCHAR(50)    NOT NULL UNIQUE,
    `type`      ENUM ('fixed',
        'percent'
        )                      NOT NULL,
    `value`     INTEGER        NOT NULL,
    min_price   INTEGER        NOT NULL,
    user_count  INTEGER        NOT NULL,
    start_date  DATE           NOT NULL,
    end_date    DATE           NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Orders
(
    id               INTEGER AUTO_INCREMENT PRIMARY KEY,
    customer_id      INTEGER NOT NULL,
    restaurant_id    INTEGER NOT NULL,
    courier_id       INTEGER NULL,
    coupon_id        INTEGER NULL,
    delivery_address TEXT    NOT NULL,
    raw_price        INTEGER NOT NULL,
    tax_fee          INTEGER NOT NULL,
    additional_fee   INTEGER NOT NULL,
    courier_fee      INTEGER NOT NULL,
    pay_price        INTEGER NOT NULL,
    status           ENUM (
        'unpaid',
        'waiting',
        'preparing',
        'cancelled',
        'findingCourier',
        'onTheWay',
        'completed'
        )                    NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Users (id) ON DELETE RESTRICT,
    FOREIGN KEY (restaurant_id) REFERENCES Restaurants (id) ON DELETE RESTRICT,
    FOREIGN KEY (courier_id) REFERENCES Users (id) ON DELETE SET NULL,
    FOREIGN KEY (coupon_id) REFERENCES Coupons (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS Order_Items
(
    order_id INTEGER NOT NULL,
    item_id  INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    PRIMARY KEY (order_id, item_id),
    FOREIGN KEY (order_id) REFERENCES Orders (id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES FoodItems (id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS Ratings
(
    id         INTEGER AUTO_INCREMENT PRIMARY KEY,
    order_id   INTEGER NOT NULL UNIQUE,
    user_id    INTEGER NOT NULL,
    rating     INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment    TEXT    NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES Orders (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Rating_Images
(
    id         INTEGER AUTO_INCREMENT PRIMARY KEY,
    rating_id  INTEGER  NOT NULL,
    image_data LONGTEXT NOT NULL,
    FOREIGN KEY (rating_id) REFERENCES Ratings (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Transactions
(
    id         INTEGER AUTO_INCREMENT PRIMARY KEY,
    order_id   INTEGER                                     NULL,
    user_id    INTEGER                                     NOT NULL,
    method     ENUM ('wallet', 'online')                   NOT NULL,
    status     ENUM ('success', 'failed')                  NOT NULL,
    amount     INTEGER                                     NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES Orders (id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS Favorite_Restaurants
(
    user_id       INTEGER NOT NULL,
    restaurant_id INTEGER NOT NULL,
    PRIMARY KEY (user_id, restaurant_id),
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE,
    FOREIGN KEY (restaurant_id) REFERENCES Restaurants (id) ON DELETE CASCADE
);