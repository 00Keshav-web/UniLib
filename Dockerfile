# =========================
# Stage 1: Build UniLib
# =========================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests


# =========================
# Stage 2: Runtime
# MySQL + Tomcat
# =========================
FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y \
        mysql-server \
        openjdk-17-jre-headless \
        curl \
        ca-certificates \
        tar && \
    rm -rf /var/lib/apt/lists/*

# Reduce MySQL memory usage for Render's 512 MB instance
RUN cat > /etc/mysql/mysql.conf.d/zz-unilib-memory.cnf <<'EOF'
[mysqld]
innodb_buffer_pool_size=64M
innodb_log_buffer_size=8M
key_buffer_size=8M
max_connections=20
table_open_cache=64
performance_schema=OFF
tmp_table_size=8M
max_heap_table_size=8M
EOF

# Tomcat
ENV CATALINA_HOME=/opt/tomcat

RUN curl -fsSL \
    https://dlcdn.apache.org/tomcat/tomcat-11/v11.0.26/bin/apache-tomcat-11.0.26.tar.gz \
    -o /tmp/tomcat.tar.gz && \
    mkdir -p /opt && \
    tar -xzf /tmp/tomcat.tar.gz -C /opt && \
    mv /opt/apache-tomcat-11.0.26 /opt/tomcat && \
    rm /tmp/tomcat.tar.gz

# Remove unused default Tomcat applications
RUN rm -rf \
    /opt/tomcat/webapps/docs \
    /opt/tomcat/webapps/examples \
    /opt/tomcat/webapps/host-manager \
    /opt/tomcat/webapps/manager \
    /opt/tomcat/webapps/ROOT

# Copy UniLib WAR
COPY --from=builder /app/target/unilib.war \
    /opt/tomcat/webapps/unilib.war

# Copy database dump
COPY database/unilib.sql /app/database/unilib.sql

# Copy startup script
COPY docker/start.sh /app/start.sh

RUN chmod +x /app/start.sh

EXPOSE 10000

ENTRYPOINT ["/app/start.sh"]