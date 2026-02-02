#!/bin/bash

# Script to kill all ports used by DICOM Viewer application
# Backend: 8080, Frontend: 3000, 5173

echo "🔍 Checking for processes using application ports..."
echo ""

# Define ports
BACKEND_PORT=8080
FRONTEND_PORT_1=3000
FRONTEND_PORT_2=5173

# Function to kill process on a specific port
kill_port() {
    local port=$1
    local port_name=$2

    # Find process ID using the port
    local pid=$(lsof -ti:$port)

    if [ -z "$pid" ]; then
        echo "✓ Port $port ($port_name) is not in use"
    else
        echo "⚠️  Port $port ($port_name) is in use by PID: $pid"
        echo "   Killing process $pid..."
        kill -9 $pid 2>/dev/null

        # Verify the process was killed
        sleep 1
        local check_pid=$(lsof -ti:$port)
        if [ -z "$check_pid" ]; then
            echo "✅ Successfully killed process on port $port"
        else
            echo "❌ Failed to kill process on port $port"
        fi
    fi
    echo ""
}

# Kill processes on each port
echo "🛑 Stopping DICOM Viewer processes..."
echo "========================================"
echo ""

kill_port $BACKEND_PORT "Backend"
kill_port $FRONTEND_PORT_1 "Frontend Vite"
kill_port $FRONTEND_PORT_2 "Frontend Vite Alt"

# Also kill any Java processes that might be the backend
echo "🔍 Checking for Java processes (Backend)..."
JAVA_PIDS=$(pgrep -f "DicomViewerApplication")
if [ -n "$JAVA_PIDS" ]; then
    echo "⚠️  Found DicomViewerApplication processes: $JAVA_PIDS"
    echo "   Killing Java processes..."
    echo "$JAVA_PIDS" | xargs kill -9 2>/dev/null
    echo "✅ Killed DicomViewerApplication processes"
else
    echo "✓ No DicomViewerApplication Java processes found"
fi
echo ""

# Check for Node processes that might be the frontend
echo "🔍 Checking for Node processes (Frontend)..."
NODE_PIDS=$(pgrep -f "vite")
if [ -n "$NODE_PIDS" ]; then
    echo "⚠️  Found Vite processes: $NODE_PIDS"
    echo "   Killing Vite processes..."
    echo "$NODE_PIDS" | xargs kill -9 2>/dev/null
    echo "✅ Killed Vite processes"
else
    echo "✓ No Vite processes found"
fi
echo ""

# Check for Gradle daemon
echo "🔍 Checking for Gradle daemon..."
GRADLE_PIDS=$(pgrep -f "GradleDaemon")
if [ -n "$GRADLE_PIDS" ]; then
    echo "⚠️  Found Gradle daemon processes: $GRADLE_PIDS"
    echo "   Note: Gradle daemon will restart automatically when needed"
    # Uncomment the next line if you want to kill Gradle daemon too
    # echo "$GRADLE_PIDS" | xargs kill -9 2>/dev/null
else
    echo "✓ No Gradle daemon found"
fi
echo ""

echo "========================================"
echo "✅ Port cleanup completed!"
echo ""
echo "📊 Current port usage:"
echo "   Port 8080 (Backend):  $(lsof -ti:8080 &>/dev/null && echo 'IN USE' || echo 'FREE')"
echo "   Port 3000 (Frontend): $(lsof -ti:3000 &>/dev/null && echo 'IN USE' || echo 'FREE')"
echo "   Port 5173 (Frontend): $(lsof -ti:5173 &>/dev/null && echo 'IN USE' || echo 'FREE')"
echo ""
