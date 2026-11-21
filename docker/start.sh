#!/bin/bash

set -e

echo "================================="
echo "Starting UniLib container..."
echo "================================="

# Render provides PORT automatically
PORT=${PORT:-8080}

# Validate external TiDB configuration
: "${DB_HOST:?DB_HOST is missing}"
: "${DB_PORT:?DB_PORT is missing}"
: "${DB_NAME:?DB_NAME is missing}"
: "${DB_USER:?DB_USER is missing}"
: "${DB_PASSWORD:?DB_PASSWORD is missing}"

echo "Using external database: ${DB_HOST}"
echo "Local MySQL startup and SQL import are disabled."

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

# Limit Java memory usage
export CATALINA_OPTS="-Xms64m -Xmx192m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC"

echo "Starting Tomcat..."

exec "${CATALINA_HOME}/bin/catalina.sh" run
