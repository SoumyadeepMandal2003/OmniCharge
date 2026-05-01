#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# OmniCharge — Azure VM Setup Script
# Run this ONCE on a fresh Azure Ubuntu 22.04 VM
#
# Usage:
#   chmod +x azure-setup.sh
#   ./azure-setup.sh
# ─────────────────────────────────────────────────────────────────────────────

set -e

echo "=================================================="
echo "  OmniCharge — Azure VM Setup"
echo "=================================================="

# ── 1. System update ──────────────────────────────────────────────────────────
echo ""
echo "[1/5] Updating system..."
sudo apt-get update -y
sudo apt-get upgrade -y
sudo apt-get install -y curl git

# ── 2. Install Docker ─────────────────────────────────────────────────────────
echo ""
echo "[2/5] Installing Docker..."
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
sudo apt-get install -y docker-compose-plugin
echo "Docker installed."

# ── 3. Clone repo ─────────────────────────────────────────────────────────────
echo ""
echo "[3/5] Cloning OmniCharge repo..."
if [ -d "OmniCharge" ]; then
    cd OmniCharge && git pull && cd ..
else
    git clone https://github.com/SoumyadeepMandal2003/OmniCharge.git
fi
cd OmniCharge

# ── 4. Configure environment ──────────────────────────────────────────────────
echo ""
echo "[4/5] Setting up environment variables..."
if [ ! -f ".env" ]; then
    cp .env.example .env
    echo "Opening .env for editing..."
    sleep 2
    nano .env
else
    echo ".env already exists."
fi

# ── 5. Build and start ────────────────────────────────────────────────────────
echo ""
echo "[5/5] Building and starting all services (5-10 mins)..."
newgrp docker << 'DOCKERGROUP'
docker compose -f docker-compose.prod.yml up -d --build
DOCKERGROUP

echo ""
echo "=================================================="
echo "  Setup Complete!"
echo "=================================================="
PUBLIC_IP=$(curl -s ifconfig.me 2>/dev/null || echo "YOUR_VM_IP")
echo ""
echo "  API Gateway:  http://$PUBLIC_IP:8080"
echo "  Swagger UI:   http://$PUBLIC_IP:8080/swagger-ui.html"
echo "  Eureka:       http://$PUBLIC_IP:8761"
echo "  RabbitMQ UI:  http://$PUBLIC_IP:15672"
echo "  Zipkin:       http://$PUBLIC_IP:9411"
echo "=================================================="
