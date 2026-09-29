#!/bin/bash
# FoodBridge Local Hosting Script - Runs backend + frontend locally
# Usage: ./run-local.sh [mysql_password] [backend_port] [frontend_port]

set -e

MYSQL_PASSWORD="${1:-}"
BACKEND_PORT="${2:-8080}"
FRONTEND_PORT="${3:-5500}"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=========================================="
echo "    FoodBridge Local Hosting Launcher"
echo "=========================================="
echo ""

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v java &> /dev/null || ! java -version 2>&1 | grep -qE "17|18|19|20|21"; then
    echo "Error: Java 17+ not found"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "Error: Maven not found"
    exit 1
fi

if ! command -v mysql &> /dev/null; then
    echo "Error: MySQL not found"
    exit 1
fi

echo "✓ Java: OK"
echo "✓ Maven: OK"
echo "✓ MySQL: OK"

# Get MySQL password if not provided
if [ -z "$MYSQL_PASSWORD" ]; then
    read -s -p "Enter MySQL root password: " MYSQL_PASSWORD
    echo ""
fi

# Set up database
echo ""
echo "Setting up database..."
mysql -u root -p"$MYSQL_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS foodbridge_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
if [ $? -ne 0 ]; then
    echo "Error: Failed to create database. Check MySQL password."
    exit 1
fi
echo "✓ Database ready"

# Update application.properties
echo ""
echo "Configuring backend..."
PROPS_FILE="$PROJECT_DIR/FoodBridge/backend/src/main/resources/application.properties"
sed -i "s|spring.datasource.password=.*|spring.datasource.password=$MYSQL_PASSWORD|" "$PROPS_FILE"
sed -i "s|server.port=.*|server.port=$BACKEND_PORT|" "$PROPS_FILE"
echo "✓ Configuration updated"

# Start backend
echo ""
echo "Starting Spring Boot backend on port $BACKEND_PORT..."
cd "$PROJECT_DIR/FoodBridge/backend"
mvn spring-boot:run > /tmp/foodbridge-backend.log 2>&1 &
BACKEND_PID=$!

# Wait for backend to start
echo "Waiting for backend to start..."
STARTED=false
for i in {1..60}; do
    if curl -s -o /dev/null -w "%{http_code}" "http://localhost:$BACKEND_PORT/api/auth/me" | grep -q "401"; then
        STARTED=true
        break
    fi
    sleep 2
    echo -n "."
done
echo ""

if [ "$STARTED" = false ]; then
    echo "Error: Backend failed to start within 120 seconds"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi
echo "✓ Backend running at http://localhost:$BACKEND_PORT/api"

# Start frontend
echo ""
echo "Starting frontend on port $FRONTEND_PORT..."
cd "$PROJECT_DIR/FoodBridge/frontend"
python3 -m http.server $FRONTEND_PORT > /tmp/foodbridge-frontend.log 2>&1 &
FRONTEND_PID=$!

sleep 3
echo "✓ Frontend running at http://localhost:$FRONTEND_PORT"

# Summary
echo ""
echo "=========================================="
echo "    FoodBridge is running!"
echo "=========================================="
echo ""
echo "Frontend: http://localhost:$FRONTEND_PORT"
echo "Backend API: http://localhost:$BACKEND_PORT/api"
echo ""
echo "Demo Credentials (password: password123):"
echo "  Admin:       admin@foodbridge.com"
echo "  Donor:       donor1@foodbridge.com"
echo "  NGO (Verified): receiver1@foodbridge.com"
echo ""
echo "Press Ctrl+C to stop both servers"

# Cleanup on exit
trap "echo 'Stopping servers...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; echo 'Stopped.'" EXIT

# Keep running
while true; do sleep 10; done