#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# OmniCharge — Oracle Cloud Free Tier VM Setup Script
# Run this ONCE on a fresh Oracle Cloud Ubuntu 22.04 ARM instance
#
# Usage:
#   chmod +x oracle-cloud-setup.sh
#   ./oracle-cloud-setup.sh
# ─────────────────────────────────────────────────────────────────────────────

set -e  # exit on any error

echo "=================================================="
echo "  OmniCharge — Oracle Cloud VM Setup"
echo "=================================================="

# ── 1. System update ──────────────────────────────────────────────────────────
echo ""
echo "[1/6] Updating system packages..."
sudo apt-get update -y
sudo apt-get upgrade -y
sudo apt-get install -y curl git ufw

# ── 2. Install Docker ─────────────────────────────────────────────────────────
echo ""
echo "[2/6] Installing Docker..."
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
rm get-docker.sh

# Install Docker Compose plugin
sudo apt-get install -y docker-compose-plugin
docker compose version

echo "Docker installed successfully."

# ── 3. Configure Firewall ─────────────────────────────────────────────────────
echo ""
echo "[3/6] Configuring firewall (UFW)..."
sudo ufw allow OpenSSH
sudo ufw allow 8080/tcp   # API Gateway (main entry point)
sudo ufw allow 8761/tcp   # Eureka dashboard
sudo ufw allow 15672/tcp  # RabbitMQ management UI
sudo ufw allow 9411/tcp   # Zipkin tracing UI
sudo ufw --force enable
echo "Firewall configured."

echo ""
echo "⚠️  IMPORTANT: Also open these ports in Oracle Cloud Security List:"
echo "   Go to: OCI Console → Networking → VCN → Security Lists → Ingress Rules"
echo "   Add TCP rules for ports: 8080, 8761, 15672, 9411"

# ── 4. Clone repository ───────────────────────────────────────────────────────
echo ""
echo "[4/6] Setting up project directory..."
read -p "Enter your GitHub repository URL (e.g. https://github.com/yourname/OmniCharge): " REPO_URL

if [ -d "OmniCharge" ]; then
    echo "Directory exists, pulling latest..."
    cd OmniCharge && git pull && cd ..
else
    git clone "$REPO_URL" OmniCharge
fi

cd OmniCharge

# ── 5. Configure environment ──────────────────────────────────────────────────
echo ""
echo "[5/6] Configuring environment variables..."

if [ ! -f ".env" ]; then
    cp .env.example .env
    echo ""
    echo "Please set your secrets in .env now."
    echo "Opening .env for editing in 3 seconds..."
    sleep 3
    nano .env
else
    echo ".env already exists, skipping."
fi

# ── 6. Build and start ────────────────────────────────────────────────────────
echo ""
echo "[6/6] Building and starting all services..."
echo "This will take 5-10 minutes on first run (building all Docker images)..."
echo ""

docker compose -f docker-compose.prod.yml build --parallel
docker compose -f docker-compose.prod.yml up -d

echo ""
echo "=================================================="
echo "  Setup Complete!"
echo "=================================================="
echo ""
echo "Waiting 60 seconds for services to start..."
sleep 60

echo ""
echo "Service Status:"
docker compose -f docker-compose.prod.yml ps

echo ""
# Get the public IP
PUBLIC_IP=$(curl -s ifconfig.me 2>/dev/null || echo "YOUR_VM_IP")

echo "=================================================="
echo "  Your OmniCharge is live at:"
echo "=================================================="
echo ""
echo "  API Gateway:      http://$PUBLIC_IP:8080"
echo "  Swagger UI:       http://$PUBLIC_IP:8080/swagger-ui.html"
echo "  Eureka Dashboard: http://$PUBLIC_IP:8761"
echo "  RabbitMQ UI:      http://$PUBLIC_IP:15672"
echo "  Zipkin Tracing:   http://$PUBLIC_IP:9411"
echo ""
echo "  Update Postman baseUrl to: http://$PUBLIC_IP:8080"
echo "=================================================="
