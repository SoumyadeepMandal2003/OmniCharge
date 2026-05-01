# Deploy OmniCharge to Oracle Cloud Free Tier

## What You Get (Free Forever)
- 4 ARM CPU cores
- **24 GB RAM** (runs your entire stack with room to spare)
- 200 GB storage
- Always free — no credit card charges after signup

---

## Step 1 — Create Oracle Cloud Account

1. Go to https://www.oracle.com/cloud/free/
2. Click **Start for Free**
3. Fill in your details — use a **real credit card** (required for verification, you won't be charged)
4. Complete email verification

> ⚠️ Signup can take 10-30 minutes. Sometimes it fails — try a different browser or incognito mode.

---

## Step 2 — Create the Free VM

1. Login to https://cloud.oracle.com
2. Go to **Compute → Instances → Create Instance**
3. Configure:
   - **Name**: `omnicharge-server`
   - **Image**: Ubuntu 22.04 (Minimal)
   - **Shape**: Click **Change Shape** → Select **Ampere** → `VM.Standard.A1.Flex`
   - **OCPUs**: 4
   - **Memory**: 24 GB
   - ✅ This is the **Always Free** shape
4. Under **Add SSH Keys**:
   - Select **Generate a key pair for me**
   - Download both `private key` and `public key` files — **save these safely!**
5. Click **Create**

Wait 2-3 minutes for the VM to show **Running** status.

---

## Step 3 — Open Firewall Ports in Oracle Cloud

Oracle Cloud has TWO firewalls — you need to open both.

### 3a. Security List (Oracle's firewall)
1. Go to **Networking → Virtual Cloud Networks**
2. Click your VCN → **Security Lists** → **Default Security List**
3. Click **Add Ingress Rules** and add these one by one:

| Source CIDR | Protocol | Port |
|---|---|---|
| 0.0.0.0/0 | TCP | 8080 |
| 0.0.0.0/0 | TCP | 8761 |
| 0.0.0.0/0 | TCP | 15672 |
| 0.0.0.0/0 | TCP | 9411 |

### 3b. VM's internal firewall (iptables)
SSH into your VM first (Step 4), then run:
```bash
sudo iptables -I INPUT -p tcp --dport 8080 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 8761 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 15672 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 9411 -j ACCEPT
sudo netfilter-persistent save
```

---

## Step 4 — SSH Into Your VM

Find your VM's **Public IP** on the instance details page.

```bash
# On your local PC (Git Bash or PowerShell)
ssh -i path/to/your-private-key.key ubuntu@YOUR_VM_PUBLIC_IP

# Example:
ssh -i C:/Users/Admin/Downloads/ssh-key.key ubuntu@140.238.xxx.xxx
```

> If you get a permissions error on Windows:
> Right-click the key file → Properties → Security → Advanced → Disable inheritance → Remove all → Add yourself with Full Control

---

## Step 5 — Run the Setup Script on the VM

Once SSH'd in, run:

```bash
# Download and run the setup script
curl -fsSL https://raw.githubusercontent.com/YOUR_USERNAME/OmniCharge/main/deploy/oracle-cloud-setup.sh -o setup.sh
chmod +x setup.sh
./setup.sh
```

Or manually:
```bash
# Install Docker
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
sudo apt-get install -y docker-compose-plugin git
newgrp docker

# Clone your repo
git clone https://github.com/YOUR_USERNAME/OmniCharge.git
cd OmniCharge

# Set up environment
cp .env.example .env
nano .env   # fill in your passwords/secrets

# Build and start everything
docker compose -f docker-compose.prod.yml up -d --build
```

First build takes **5-10 minutes** — it's compiling all 9 services.

---

## Step 6 — Verify Everything is Running

```bash
# Check all containers
docker compose -f docker-compose.prod.yml ps

# Watch logs
docker compose -f docker-compose.prod.yml logs -f

# Check a specific service
docker compose -f docker-compose.prod.yml logs auth-service
```

All services should show **Up** status after ~2 minutes.

---

## Step 7 — Access Your Services

Replace `YOUR_VM_IP` with your actual Oracle Cloud VM public IP:

| Service | URL |
|---|---|
| **API Gateway** | `http://YOUR_VM_IP:8080` |
| **Swagger UI** | `http://YOUR_VM_IP:8080/swagger-ui.html` |
| **Eureka Dashboard** | `http://YOUR_VM_IP:8761` |
| **RabbitMQ UI** | `http://YOUR_VM_IP:15672` |
| **Zipkin Tracing** | `http://YOUR_VM_IP:9411` |

### Update Postman
In your Postman collection, change the `baseUrl` variable from:
```
http://localhost:8080
```
to:
```
http://YOUR_VM_IP:8080
```

---

## Step 8 — Set Up Auto-Deploy (Optional but Recommended)

Every time you push code to GitHub, it auto-deploys to your VM.

### Add GitHub Secrets
Go to your GitHub repo → **Settings → Secrets and variables → Actions → New repository secret**

Add these 3 secrets:

| Secret Name | Value |
|---|---|
| `ORACLE_VM_IP` | Your VM's public IP (e.g. `140.238.xxx.xxx`) |
| `ORACLE_VM_USER` | `ubuntu` |
| `ORACLE_SSH_PRIVATE_KEY` | Contents of your downloaded private key file |

To get the private key contents:
```bash
# On Windows (Git Bash)
cat /c/Users/Admin/Downloads/ssh-key.key
```
Copy the entire output including `-----BEGIN RSA PRIVATE KEY-----` lines.

Now every `git push` to `main` will automatically redeploy!

---

## Daily Usage

### Deploy a code change
```bash
# On your local PC — just push to git
git add .
git commit -m "your changes"
git push origin main
# GitHub Actions auto-deploys in ~10 minutes
```

### Manual redeploy on VM
```bash
ssh -i your-key.key ubuntu@YOUR_VM_IP
cd OmniCharge
./deploy/redeploy.sh

# Or redeploy just one service
./deploy/redeploy.sh auth-service
```

### View logs
```bash
ssh -i your-key.key ubuntu@YOUR_VM_IP
cd OmniCharge
docker compose -f docker-compose.prod.yml logs -f api-gateway
```

### Restart a crashed service
```bash
docker compose -f docker-compose.prod.yml restart auth-service
```

### Stop everything
```bash
docker compose -f docker-compose.prod.yml down
```

---

## Memory Usage on the VM

| Service | RAM |
|---|---|
| MySQL | ~400MB |
| RabbitMQ | ~200MB |
| Zipkin | ~200MB |
| service-discovery | ~256MB |
| config-server | ~256MB |
| api-gateway | ~384MB |
| auth-service | ~512MB |
| user-service | ~512MB |
| recharge-service | ~512MB |
| payment-service | ~512MB |
| operator-service | ~384MB |
| notification-service | ~256MB |
| **Total** | **~4.4GB** |
| **VM has** | **24GB** |
| **Free headroom** | **~19GB** ✅ |

---

## Troubleshooting

### Service won't start
```bash
docker compose -f docker-compose.prod.yml logs service-name
```

### Port not accessible from outside
- Check Oracle Security List (Step 3a)
- Check iptables on VM (Step 3b)
- Verify with: `curl http://YOUR_VM_IP:8080/actuator/health`

### Out of disk space
```bash
docker system prune -f   # removes unused images/containers
```

### Database connection refused
```bash
# Check MySQL is healthy
docker compose -f docker-compose.prod.yml ps mysql
docker compose -f docker-compose.prod.yml logs mysql
```
