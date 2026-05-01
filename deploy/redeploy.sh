#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# OmniCharge — Redeploy script
# Run this on the Oracle Cloud VM whenever you push new code
#
# Usage:
#   ./deploy/redeploy.sh                  # redeploy all services
#   ./deploy/redeploy.sh auth-service     # redeploy one service only
# ─────────────────────────────────────────────────────────────────────────────

set -e

SERVICE=$1

echo "=================================================="
echo "  OmniCharge Redeploy"
echo "=================================================="

# Pull latest code
echo ""
echo "Pulling latest code from git..."
git pull origin main

if [ -z "$SERVICE" ]; then
    # Redeploy everything
    echo ""
    echo "Rebuilding and redeploying all services..."
    docker compose -f docker-compose.prod.yml build --parallel
    docker compose -f docker-compose.prod.yml up -d
    echo ""
    echo "All services redeployed!"
else
    # Redeploy single service
    echo ""
    echo "Rebuilding and redeploying: $SERVICE"
    docker compose -f docker-compose.prod.yml build "$SERVICE"
    docker compose -f docker-compose.prod.yml up -d "$SERVICE"
    echo ""
    echo "$SERVICE redeployed!"
fi

echo ""
echo "Current status:"
docker compose -f docker-compose.prod.yml ps
