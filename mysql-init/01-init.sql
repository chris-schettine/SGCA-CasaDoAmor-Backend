-- MySQL initialization script for SGCA Backend
-- This script runs when the MySQL container starts for the first time

-- Create the database if it doesn't exist (though it's already created by environment variable)
CREATE DATABASE sgca CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create the application user if it doesn't exist
CREATE USER IF NOT EXISTS 'sgca_user'@'%' IDENTIFIED WITH mysql_native_password BY 'admin';

-- Grant all privileges on the sgca database to the application user
GRANT ALL PRIVILEGES ON sgca.* TO 'sgca_user'@'%';

-- Grant SELECT privilege on mysql.user table for connection testing
GRANT SELECT ON mysql.user TO 'sgca_user'@'%';

-- Flush privileges to ensure all changes take effect
FLUSH PRIVILEGES;

-- Use the sgca database
USE sgca;

-- Optional: Create some initial tables if they don't exist
-- Note: Spring Boot with JPA will create these automatically, so this is just a backup

-- Log the initialization
SELECT 'MySQL initialization completed for SGCA Backend' as message;