# Deploy OmniCharge to DigitalOcean (GitHub Student Pack)

## What You Get
- **$200 free credits** valid for 12 months
- Managed infrastructure, simple UI
- No sleep/spin-down — always on
- Estimated burn rate: ~$39/month → ~5 months free

---

## Step 1 — Activate DigitalOcean via Student Pack

1. Go to **https://education.github.com/pack**
2. Find **DigitalOcean** → Click **Get access**
3. Connect your GitHub account
4. You'll be redirected to DigitalOcean — create your account
5. $200 credit is applied automatically ✅

> You'll need to add a credit card but **$200 covers everything** — you won't be charged until credits run out (~5 months).

---

## Step 2 — Create a Droplet (VM)

1. Login to **https://cloud.digitalocean.com**
2. Click **Create → Droplets**
3. Configure:
   - **Region**: Bangalore (closest to India) or Singapore
   - **OS**: Ubuntu 22.04 LTS x64
   - **Plan**: Basic → **Regular** → **$24/month (4GB RAM, 2 vCPU, 80GB SSD)**
   - **Authentication**: SSH Key → Add New SSH Key

### Generate SSH Key (run on your PC)
```bash
# In Git Bash or PowerShell
ssh-keygen -t ed25519 -C "omnicharge-digitalocean" -f ~/.ssh/omnicharge_do
```
This creates two files:
- `~/.ssh/omnicharge_do` — private key (keep safe, never share)
- `~/.ssh/omnicharge_do.pub` — public key (paste this into DigitalOcean)

```bash
# Copy the public key content
cat ~/.ssh/omnicharge_do.pub
```
Paste the output into DigitalOcean's SSH key field.

4. **Hostname**: `omnicharge-server`
5. Click **Create Droplet**

Wait ~1 minute for it to show **Active** status. Note the **public IP address**.

---

## Step 3 — Configure Firewall

1. Go to **Networking → Firewalls → Create Firewall**
2. Name: `omnicharge-firewall`
3. Add **Inbound Rules**:

| Type | Protocol | Port | Sources |
|---|---|---|---|
| SSH | TCP | 22 | All IPv4 |
| Custom | TCP | 8080 | All IPv4 |
| Custom | TCP | 8761 | All IPv4 |
| Custom | TCP | 15672 | All IPv4 |
| Custom | TCP | 9411 | All IPv4 |

4. Under **Apply to Droplets** → select `omnicharge-server`
5. Click **Create Firewall**

---

## Step 4 — SSH Into Your Droplet

```bash
ssh -i ~/.ssh/omnicharge_do root@YOUR_DROPLET_IP
```

---

## Step 5 — Install Docker & Deploy

Once SSH'd in, run these commands:

```bash
# Update system
apt-get update -y && apt-get upgrade -y

# Install Docker
curl -fsSL https://get.docker.com | sh
apt-get install -y docker-compose-plugin git

# Verify
docker --version
docker compose version
```

```bash
# Clone your repo
git clone https://github.com/SoumyadeepMandal2003/OmniCharge.git
cd OmniCharge

# Set up environment variables
cp .env.example .env
nano .env
```

Fill in your `.env`:
```env
MYSQL_ROOT_PASSWORD=StrongRootPass123!
MYSQL_USER=omnicharge
MYSQL_PASSWORD=StrongDbPass456!
RABBITMQ_USER=omnicharge
RABBITMQ_PASSWORD=StrongRabbitPass789!
JWT_SECRET=OmniChargeVeryLongSecretKeyForJWT2024SuperSecureMinThirtyTwoChars
INTERNAL_SECRET=OmniChargeInternalSecret2024Secure
```

Save with `Ctrl+X → Y → Enter`

```bash
# Build and start everything (5-10 mins first time)
docker compose -f docker-compose.prod.yml up -d --build
```

---

## Step 6 — Verify Everything is Running

```bash
# Check all containers
docker compose -f docker-compose.prod.yml ps
```

Expected output — all should show `Up`:
```
omnicharge-mysql          Up (healthy)
omnicharge-rabbitmq       Up (healthy)
omnicharge-zipkin         Up
omnicharge-eureka         Up (healthy)
omnicharge-config         Up (healthy)
omnicharge-gateway        Up
omnicharge-auth           Up
omnicharge-user           Up
omnicharge-operator       Up
omnicharge-recharge       Up
omnicharge-payment        Up
omnicharge-notification   Up
```

Wait ~2-3 minutes after startup for all services to register with Eureka.

---

## Step 7 — Access Your Services

Replace `YOUR_DROPLET_IP` with your actual DigitalOcean Droplet IP:

| Service | URL |
|---|---|
| **API Gateway** | `http://YOUR_DROPLET_IP:8080` |
| **Swagger UI** | `http://YOUR_DROPLET_IP:8080/swagger-ui.html` |
| **Eureka Dashboard** | `http://YOUR_DROPLET_IP:8761` |
| **RabbitMQ UI** | `http://YOUR_DROPLET_IP:15672` |
| **Zipkin Tracing** | `http://YOUR_DROPLET_IP:9411` |

### Update Postman
Change the `baseUrl` collection variable from:
```
http://localhost:8080
```
to:
```
http://YOUR_DROPLET_IP:8080
```

---

## Step 8 — Set Up Auto-Deploy via GitHub Actions

Every `git push` to `main` will automatically redeploy to your Droplet.

### Add GitHub Secrets
Go to your repo → **Settings → Secrets and variables → Actions → New repository secret**

| Secret Name | Value |
|---|---|
| `DO_DROPLET_IP` | Your Droplet's public IP |
| `DO_SSH_PRIVATE_KEY` | Contents of `~/.ssh/omnicharge_do` |

To get the private key:
```bash
cat ~/.ssh/omnicharge_do
```
Copy everything including `-----BEGIN OPENSSH PRIVATE KEY-----` lines.

The GitHub Actions workflow (`.github/workflows/deploy.yml`) is already configured — it will auto-deploy on every push. ✅

---

## Daily Usage

### Push a code change (auto-deploys)
```bash
git add .
git commit -m "your changes"
git push origin main
# GitHub Actions deploys in ~10 minutes
```

### Manual redeploy on Droplet
```bash
ssh -i ~/.ssh/omnicharge_do root@YOUR_DROPLET_IP
cd OmniCharge
./deploy/redeploy.sh

# Redeploy single service
./deploy/redeploy.sh auth-service
```

### View logs
```bash
docker compose -f docker-compose.prod.yml logs -f
docker compose -f docker-compose.prod.yml logs -f auth-service
```

### Restart a service
```bash
docker compose -f docker-compose.prod.yml restart auth-service
```

---

## Credit Usage Estimate

| Resource | Monthly Cost |
|---|---|
| Droplet (4GB RAM, 2 vCPU) | $24/month |
| Bandwidth (5TB included) | $0 |
| **Total** | **$24/month** |
| **$200 credit lasts** | **~8 months** ✅ |

> After credits run out, migrate to Oracle Cloud Free Tier (config already in `docker-compose.prod.yml` — same setup, just different VM).

---

## Troubleshooting

### Service won't start
```bash
docker compose -f docker-compose.prod.yml logs service-name
# Example:
docker compose -f docker-compose.prod.yml logs auth-service
```

### Can't access from browser
- Check firewall rules in DigitalOcean dashboard
- Verify containers are running: `docker compose -f docker-compose.prod.yml ps`

### Out of memory
```bash
free -h   # check available memory
docker stats   # see per-container usage
```

### Database issues
```bash
docker compose -f docker-compose.prod.yml logs mysql
docker compose -f docker-compose.prod.yml restart mysql
```

### Clean up disk space
```bash
docker system prune -f
```
