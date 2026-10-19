# 🚀 Deploying FoodBridge on Render

This guide walks you through deploying **FoodBridge** (Spring Boot 3 + Java 17 backend, Nginx frontend, and database) to [Render](https://render.com).

---

## 📋 Table of Contents
1. [Architecture & Deployment Options](#architecture--deployment-options)
2. [Method 1: 1-Click Blueprint Deployment (Recommended)](#method-1-1-click-blueprint-deployment-recommended)
3. [Method 2: Manual Web Service Deployment](#method-2-manual-web-service-deployment)
   - [Step A: Create Database](#step-a-create-database)
   - [Step B: Deploy Backend](#step-b-deploy-backend)
   - [Step C: Deploy Frontend](#step-c-deploy-frontend)
4. [Environment Variables Reference](#environment-variables-reference)
5. [Local Testing with Docker Compose](#local-testing-with-docker-compose)
6. [Troubleshooting & Common Issues](#troubleshooting--common-issues)

---

## 🏗️ Architecture & Deployment Options

FoodBridge consists of two deployable tiers and a database:
- **Backend**: Spring Boot 3 container running on Java 17. Exposes REST API and `/api/health`.
- **Frontend**: Nginx container serving responsive HTML/CSS/Bootstrap pages and reverse-proxying `/api/` to the backend (or connecting directly via CORS).
- **Database**:
  - **Render PostgreSQL (Native Free Tier)**: Supported out-of-the-box! FoodBridge includes both PostgreSQL and MySQL drivers. Hibernate automatically detects the dialect and creates all tables.
  - **External MySQL (Aiven, TiDB Cloud, PlanetScale)**: Simply provide `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.

---

## ⚡ Method 1: 1-Click Blueprint Deployment (Recommended)

Render Blueprints use `render.yaml` to spin up the database, backend, and frontend with all environment variables automatically wired together.

1. **Push your code to GitHub**:
   Ensure your repository has `render.yaml` and the Dockerfiles committed to GitHub.
2. **Log into Render**:
   Go to [dashboard.render.com](https://dashboard.render.com).
3. **Create New Blueprint Instance**:
   - Click **New +** → **Blueprint**.
   - Connect your GitHub repository (`foodbridge-java`).
   - Render will detect `render.yaml` and display the blueprint plan:
     - `foodbridge-db` (PostgreSQL Database)
     - `foodbridge-backend` (Web Service - Docker)
     - `foodbridge-frontend` (Web Service - Docker)
4. **Click "Apply"**:
   Render will build both containers and link them automatically!

---

## 🛠️ Method 2: Manual Web Service Deployment

If you prefer to configure services manually in the Render dashboard:

### Step A: Create Database

#### Option 1: Render PostgreSQL (Free)
1. In Render Dashboard, click **New +** → **PostgreSQL**.
2. Name: `foodbridge-db`
3. Database: `foodbridge`
4. User: `foodbridge_user`
5. Plan: **Free**
6. Click **Create Database**.
7. Once created, copy the **Internal Database URL** (for backend running on Render) or **External Database URL**.

#### Option 2: External Cloud MySQL (Free on TiDB / Aiven)
1. Create a free MySQL database on [TiDB Cloud](https://tidbcloud.com) or [Aiven](https://aiven.io).
2. Copy your JDBC connection string, username, and password.

---

### Step B: Deploy Backend Service

1. Click **New +** → **Web Service**.
2. Connect your repository.
3. Configure settings:
   - **Name**: `foodbridge-backend`
   - **Language**: `Docker`
   - **Root Directory**: leave blank (or `FoodBridge` if configuring from subfolder)
   - **Dockerfile Path**: `FoodBridge/backend/Dockerfile`
   - **Docker Build Context**: `FoodBridge/backend`
   - **Instance Type**: **Free**
   - **Health Check Path**: `/api/health`
4. Add **Environment Variables**:
   | Key | Value / Source | Description |
   |-----|----------------|-------------|
   | `PORT` | `10000` | Port Render assigns |
   | `DATABASE_URL` | *(Paste Render Postgres URL)* | Render `postgres://...` URL or JDBC URL |
   | `SPRING_DATASOURCE_USERNAME` | `foodbridge_user` | (Only if not in DATABASE_URL) |
   | `SPRING_DATASOURCE_PASSWORD` | *(Your DB password)* | (Only if not in DATABASE_URL) |
   | `APP_CORS_ALLOWED_ORIGINS` | `https://*.onrender.com,http://localhost:5500` | Allowed browser origins |
   | `JWT_SECRET` | *(Click 'Generate' or enter 32+ char secret)* | JWT Signing Key |
5. Click **Create Web Service**.
6. Wait for the build to finish. Once running, copy your backend URL:
   `https://foodbridge-backend.onrender.com`

---

### Step C: Deploy Frontend Service

1. Click **New +** → **Web Service**.
2. Connect the same repository.
3. Configure settings:
   - **Name**: `foodbridge-frontend`
   - **Language**: `Docker`
   - **Dockerfile Path**: `FoodBridge/frontend/Dockerfile`
   - **Docker Build Context**: `FoodBridge/frontend`
   - **Instance Type**: **Free**
4. Add **Environment Variables**:
   | Key | Value | Description |
   |-----|-------|-------------|
   | `PORT` | `10000` | Dynamic port |
   | `BACKEND_URL` | `https://foodbridge-backend.onrender.com` | Your deployed backend URL |
   | `API_BASE_URL` | `https://foodbridge-backend.onrender.com/api` | (Optional: direct client API URL) |
5. Click **Create Web Service**.
6. Once deployed, open your frontend URL:
   `https://foodbridge-frontend.onrender.com`

---

## 🔑 Environment Variables Reference

### Backend (`foodbridge-backend`)
| Variable | Default Value | Description |
|----------|---------------|-------------|
| `PORT` | `8080` | Server listening port (Render sets `10000`) |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/foodbridge_db...` | Standard JDBC connection URL |
| `DATABASE_URL` | *(None)* | Supports Render Postgres URL format (`postgres://user:pass@host:5432/db`) |
| `SPRING_DATASOURCE_USERNAME`| `root` | Database user |
| `SPRING_DATASOURCE_PASSWORD`| `YOUR_MYSQL_PASSWORD_HERE` | Database password |
| `APP_CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5500...` | Comma-separated allowed origins (wildcards like `https://*.onrender.com` supported) |
| `JWT_SECRET` | Base64 256-bit default | Secret key for signing JWT tokens |
| `JWT_EXPIRATION` | `86400000` (24h) | Token lifespan in milliseconds |

### Frontend (`foodbridge-frontend`)
| Variable | Default Value | Description |
|----------|---------------|-------------|
| `PORT` | `80` | Nginx HTTP port (Render sets `10000`) |
| `BACKEND_URL` | `http://backend:8080` | URL to backend service for Nginx reverse proxy |
| `API_BASE_URL` | `""` | Direct backend API URL for client-side JS (optional) |

---

## 🐳 Local Testing with Docker Compose

Test the complete system locally with a single command before pushing to Render:

```bash
docker compose up --build
```

- **Frontend**: http://localhost:5500
- **Backend API**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/api/health
- **MySQL DB**: localhost:3306

### Demo Login Accounts
All accounts use password: `password123`
- **Admin**: `admin@foodbridge.com`
- **Donor**: `donor1@foodbridge.com`
- **Receiver**: `receiver1@foodbridge.com`

---

## ❓ Troubleshooting & Common Issues

### 1. Backend Health Check Fails (`/api/health`)
- Ensure the backend binds to `server.port=${PORT:8080}`.
- Check Render logs for database connection errors.

### 2. Database Connection Error on PostgreSQL
- If using Render's PostgreSQL, verify that the `DATABASE_URL` variable is added to the backend web service. FoodBridge automatically translates `postgres://` into `jdbc:postgresql://`.

### 3. CORS Error in Browser
- Ensure `APP_CORS_ALLOWED_ORIGINS` contains your frontend domain (e.g. `https://foodbridge-frontend.onrender.com`).
- By default, `https://*.onrender.com` is automatically permitted in FoodBridge's `SecurityConfig`.

### 4. Cold Starts on Render Free Tier
- Render Free web services spin down after 15 minutes of inactivity. The first request may take ~30–50 seconds while the service boots up. Once awake, subsequent requests respond immediately.
