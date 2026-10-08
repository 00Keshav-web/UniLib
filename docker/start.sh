#!/bin/bash

set -e

echo "================================="
echo "Starting UniLib container..."
echo "================================="

# Render provides PORT automatically
PORT=${PORT:-8080}

echo "Starting MySQL..."

# Start MySQL
service mysql start

echo "Waiting for MySQL..."

until mysqladmin ping --silent; do
    sleep 2
done

echo "MySQL is ready."

# Default environment values
DB_NAME=${DB_NAME:-unilib}
DB_USER=${DB_USER:-unilib_app}
DB_PASSWORD=${DB_PASSWORD:-unilib_demo_password}

echo "Creating UniLib database and user..."

mysql -u root <<SQL
CREATE DATABASE IF NOT EXISTS ${DB_NAME};

CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost'
IDENTIFIED BY '${DB_PASSWORD}';

ALTER USER '${DB_USER}'@'localhost'
IDENTIFIED BY '${DB_PASSWORD}';

GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';

FLUSH PRIVILEGES;
SQL

echo "Database setup complete."

# Check whether tables already exist
TABLE_COUNT=$(mysql -u root -N -s -e \
    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}';")

if [ "$TABLE_COUNT" -eq 0 ]; then
    echo "Importing UniLib database dump..."
    mysql -u root "${DB_NAME}" < /app/database/unilib.sql
    echo "Database dump imported successfully."
else
    echo "Database already contains tables. Skipping import."
fi

# Configure Tomcat for Render
echo "Configuring Tomcat for Render port: ${PORT}"

# Disable Tomcat shutdown port
sed -i 's/<Server port="[^"]*"/<Server port="-1"/' \
    "${CATALINA_HOME}/conf/server.xml"

# Configure HTTP connector
sed -i "s/port=\"8080\"/port=\"${PORT}\"/" \
    "${CATALINA_HOME}/conf/server.xml"

# Make sure Tomcat accepts external connections
sed -i "s/<Connector port=\"${PORT}\"/<Connector address=\"0.0.0.0\" port=\"${PORT}\"/" \
    "${CATALINA_HOME}/conf/server.xml"

echo "Tomcat configured on 0.0.0.0:${PORT}"

echo "Starting Tomcat..."

exec "${CATALINA_HOME}/bin/catalina.sh" run