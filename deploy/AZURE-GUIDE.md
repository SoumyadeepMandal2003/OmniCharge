# Deploy OmniCharge to Microsoft Azure (Student Pack)

## What You Get
- **$100 free credit** — no credit card required
- 25+ always-free Azure services
- Estimated burn rate: ~$15-20/month → **5-6 months free**

---

## Step 1 — Activate Azure for Students

1. Go to 👉 **https://education.github.com/pack**
2. Find **Microsoft Azure** → Click **Get access**
3. Sign in with your GitHub account
4. You'll be redirected to Azure — sign up with your **college/university email**
   - If you don't have one, use your GitHub student verification
5. $100 credit applied automatically ✅
6. **No credit card required** ✅

> Azure portal: **https://portal.azure.com**

---

## Step 2 — Generate SSH Key (on your PC)

Open **Git Bash** and run:
```bash
ssh-keygen -t rsa -b 4096 -C "omnicharge-azure" -f ~/.ssh/omnicharge_azure
```

This creates:
- `~/.ssh/omnicharge_azure` — private key (never share)
- `~/.ssh/omnicharge_azure.pub` — public key (used in Azure)

Get the public key content:
```bash
cat ~/.ssh/omnicharge_azure.pub
```
Copy this — you'll need it in Step 3.

---

## Step 3 — Create Azure Virtual Machine

1. Login to **https://portal.azure.com**
2. Click **Create a resource → Virtual Machine**
3. Configure:

### Basics Tab
| Field | Value |
|---|---|
| Subscription | Azure for Students |
| Resource group | Create new → `omnicharge-rg` |
| VM name | `omnicharge-server` |
| Region | `(Asia Pacific) Central India` or `Southeast Asia` |
| Image | **Ubuntu Server 22.04 LTS** |
| Size | Click **See all sizes** → search `B2s` → **Standard_B2s** (2 vCPU, 4GB RAM, ~$15/month) |
| Authentication | SSH public key |
| Username | `azureuser` |
| SSH public key | Paste your `omnicharge_azure.pub` content |

### Disks Tab
- OS disk type: **Standard SSD** (cheaper)

### Networking Tab
- Leave defaults (it creates a VNet automatically)
- Public IP: **Create new** (static)
- NIC network security group: **Basic**
- Public inbound ports: **Allow selected** → SSH (22)

4. Click **Review + Create → Create**

Wait ~2 minutes for deployment to complete.

---

## Step 4 — Open Firewall Ports

After VM is created:

1. Go to your VM → **Networking → Add inbound port rule**
2. Add these rules one by one:

| Port | Name | Priority |
|---|---|---|
| 8080 | api-gateway | 110 |
| 8761 | eureka | 120 |
| 15672 | rabbitmq-ui | 130 |
| 9411 | zipkin | 140 |

For each rule:
- Source: Any
- Destination port: (the port number)
- Protocol: TCP
- Action: Allow

---

## Step 5 — Get Your VM's Public IP

1. Go to your VM in Azure portal
2. Look for **Public IP address** on the overview page
3. Note it down — e.g. `20.197.xxx.xxx`

---

## Step 6 — SSH Into Your VM

```bash
ssh -i ~/.ssh/omnicharge_azure azureuser@YOUR_VM_PUBLIC_IP

# Example:
ssh -i ~/.ssh/omnicharge_azure azureuser@20.197.xxx.xxx
```

---

## Step 7 — Run Setup Script

Once SSH'd in:

```bash
# Download and run setup script
curl -fsSL https://raw.githubusercontent.com/SoumyadeepMandal2003/OmniCharge/main/deploy/azure-setup.sh -o setup.sh
chmod +x setup.sh
./setup.sh
```

**OR manually:**

```bash
# Install Docker
sudo apt-get update -y
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
sudo apt-get install -y docker-compose-plugin git
newgrp docker

# Clone repo
git clone https://github.com/SoumyadeepMandal2003/OmniCharge.git
cd OmniCharge

# Configure secrets
cp .env.example .env
nano .env
```

Fill in `.env`:
```env
MYSQL_ROOT_PASSWORD=StrongRootPass123!
MYSQL_USER=omnicharge
MYSQL_PASSWORD=StrongDbPass456!
RABBITMQ_USER=omnicharge
RABBITMQ_PASSWORD=StrongRabbitPass789!
JWT_SECRET=OmniChargeVeryLongSecretKeyForJWT2024SuperSecureMinThirtyTwoChars
INTERNAL_SECRET=OmniChargeInternalSecret2024Secure
```

Save: `Ctrl+X → Y → Enter`

```bash
# Build and start (5-10 mins first time)
docker compose -f docker-compose.prod.yml up -d --build
```

---

## Step 8 — Verify Everything is Running

```bash
docker compose -f docker-compose.prod.yml ps
```

All 12 containers should show **Up**. Wait ~3 minutes after startup.

---

## Step 9 — Access Your Services

| Service | URL |
|---|---|
| **API Gateway** | `http://YOUR_VM_IP:8080` |
| **Swagger UI** | `http://YOUR_VM_IP:8080/swagger-ui.html` |
| **Eureka Dashboard** | `http://YOUR_VM_IP:8761` |
| **RabbitMQ UI** | `http://YOUR_VM_IP:15672` |
| **Zipkin Tracing** | `http://YOUR_VM_IP:9411` |

### Update Postman
Change `baseUrl` variable to:
```
http://YOUR_VM_IP:8080
```

---

## Step 10 — Set Up Auto-Deploy (GitHub Actions)

Every `git push` to `main` auto-deploys to your Azure VM.

### Add GitHub Secrets
Go to your repo → **Settings → Secrets and variables → Actions**

Add these 3 secrets:

| Secret Name | Value |
|---|---|
| `AZURE_VM_IP` | Your VM's public IP (e.g. `20.197.xxx.xxx`) |
| `AZURE_VM_USER` | `azureuser` |
| `AZURE_SSH_PRIVATE_KEY` | Contents of `~/.ssh/omnicharge_azure` |

Get private key content:
```bash
cat ~/.ssh/omnicharge_azure
```
Copy everything including `-----BEGIN RSA PRIVATE KEY-----` lines.

Now every `git push` auto-deploys! ✅

---

## Daily Usage

### Push code changes (auto-deploys)
```bash
git add .
git commit -m "your changes"
git push origin main
# GitHub Actions deploys in ~10 minutes
```

### Manual redeploy
```bash
ssh -i ~/.ssh/omnicharge_azure azureuser@YOUR_VM_IP
cd OmniCharge
./deploy/redeploy.sh

# Single service
./deploy/redeploy.sh auth-service
```

### View logs
```bash
docker compose -f docker-compose.prod.yml logs -f
docker compose -f docker-compose.prod.yml logs -f api-gateway
```

### Restart a service
```bash
docker compose -f docker-compose.prod.yml restart auth-service
```

---

## Credit Usage Estimate

| Resource | Monthly Cost |
|---|---|
| Standard_B2s VM (2 vCPU, 4GB) | ~$15/month |
| Storage (30GB SSD) | ~$2/month |
| Bandwidth | ~$1/month |
| **Total** | **~$18/month** |
| **$100 credit lasts** | **~5-6 months** ✅ |

> **Tip**: Stop the VM when not using it to save credits:
> Azure Portal → VM → **Stop** (deallocated = no compute charge)

---

## Troubleshooting

### Can't SSH in
```bash
# Check key permissions
chmod 600 ~/.ssh/omnicharge_azure
ssh -i ~/.ssh/omnicharge_azure azureuser@YOUR_VM_IP
```

### Port not accessible
- Go to Azure Portal → VM → Networking → verify inbound rules exist
- Check containers are running: `docker compose -f docker-compose.prod.yml ps`

### Service won't start
```bash
docker compose -f docker-compose.prod.yml logs service-name
```

### Out of memory
```bash
free -h
docker stats --no-stream
```

### Save credits — stop VM when not using
```bash
# From Azure Portal → VM → Stop
# Or via Azure CLI:
az vm deallocate --resource-group omnicharge-rg --name omnicharge-server
```

### Restart stopped VM
```bash
az vm start --resource-group omnicharge-rg --name omnicharge-server
```
