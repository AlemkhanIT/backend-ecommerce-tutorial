-- Create databases for each microservice
CREATE DATABASE user_db;
CREATE DATABASE product_db;
CREATE DATABASE order_db;
CREATE DATABASE notification_db;
CREATE DATABASE review_db;

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE user_db TO user;
GRANT ALL PRIVILEGES ON DATABASE product_db TO user;
GRANT ALL PRIVILEGES ON DATABASE order_db TO user;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO user;
GRANT ALL PRIVILEGES ON DATABASE review_db TO user;
