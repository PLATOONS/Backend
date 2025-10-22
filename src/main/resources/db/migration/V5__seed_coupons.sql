CREATE TABLE IF NOT EXISTS coupon (
  coupon_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  coupon_code VARCHAR(20) NOT NULL UNIQUE,
  discount_amount DOUBLE NOT NULL
);

INSERT INTO coupon (coupon_code, discount_amount) VALUES
  ('WELCOME10', 10.0),
  ('BLACKFRIDAY', 30.0);
