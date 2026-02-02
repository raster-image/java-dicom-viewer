#!/bin/bash

# Script to safely start the DICOM Viewer frontend
# Checks for and kills processes using frontend ports, then starts Vite dev server

echo "🚀 Starting DICOM Viewer Frontend..."
echo "========================================"
echo ""

# Define frontend ports
FRONTEND_PORT_1=5173  # Vite default
FRONTEND_PORT_2=3000  # Vite alternative

# Function to kill process on a specific port
kill_port() {
    local port=$1
    local port_name=$2

    # Find process ID using the port
    local pid=$(lsof -ti:$port 2>/dev/null)

    if [ -z "$pid" ]; then
        echo "✓ Port $port ($port_name) is available"
    else
        echo "⚠️  Port $port ($port_name) is in use by PID: $pid"
        echo "   Killing process $pid..."
        kill -9 $pid 2>/dev/null

        # Verify the process was killed
        sleep 1
        local check_pid=$(lsof -ti:$port 2>/dev/null)
        if [ -z "$check_pid" ]; then
            echo "✅ Successfully freed port $port"
        else
            echo "❌ Failed to free port $port. Trying force kill..."
            kill -9 $check_pid 2>/dev/null
            sleep 1
        fi
    fi
}

# Check and kill any Vite processes
echo "🔍 Checking for existing Vite processes..."
VITE_PIDS=$(pgrep -f "vite" 2>/dev/null)
if [ -n "$VITE_PIDS" ]; then
    echo "⚠️  Found existing Vite processes: $VITE_PIDS"
    echo "   Killing Vite processes..."
    echo "$VITE_PIDS" | xargs kill -9 2>/dev/null
    sleep 1
    echo "✅ Killed existing Vite processes"
else
    echo "✓ No existing Vite processes found"
fi
echo ""

# Kill processes on each port
echo "🛑 Checking frontend ports..."
kill_port $FRONTEND_PORT_1 "Vite Default"
kill_port $FRONTEND_PORT_2 "Vite Alternative"
echo ""

# Verify we're in the frontend directory
if [ ! -f "package.json" ]; then
    echo "❌ Error: package.json not found!"
    echo "   Please run this script from the frontend directory:"
    echo "   cd frontend && ./start-frontend.sh"
    exit 1
fi

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "⚠️  node_modules not found. Installing dependencies..."
    npm install
    echo ""
fi

echo "========================================"
echo "✅ Ports are ready!"
echo ""
echo "📊 Port status:"
echo "   Port 5173: $(lsof -ti:5173 &>/dev/null && echo 'IN USE' || echo 'FREE')"
echo "   Port 3000: $(lsof -ti:3000 &>/dev/null && echo 'IN USE' || echo 'FREE')"
echo ""
echo "🎬 Starting Vite dev server..."
echo "========================================"
echo ""

# Start the frontend
npm run dev
