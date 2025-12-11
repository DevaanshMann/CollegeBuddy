#!/bin/bash
set -e  # Exit on error

# CollegeBuddy Deployment Script
# This script automates the deployment to GCP Cloud Run

echo "🚀 CollegeBuddy Deployment Script"
echo "=================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo -e "${RED}❌ gcloud CLI not found. Please install it first.${NC}"
    echo "Visit: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Configuration
read -p "Enter your GCP Project ID: " PROJECT_ID
read -p "Enter your domain (e.g., collegebuddy.app): " DOMAIN
read -p "Enter Cloud SQL instance name (e.g., collegebuddy-db): " SQL_INSTANCE
read -p "Enter region (default: us-central1): " REGION
REGION=${REGION:-us-central1}

echo ""
echo -e "${YELLOW}📝 Configuration:${NC}"
echo "  Project: $PROJECT_ID"
echo "  Domain: $DOMAIN"
echo "  SQL Instance: $SQL_INSTANCE"
echo "  Region: $REGION"
echo ""
read -p "Continue with deployment? (y/n): " CONFIRM
if [ "$CONFIRM" != "y" ]; then
    echo "Deployment cancelled."
    exit 0
fi

# Set GCP project
echo -e "${YELLOW}🔧 Setting GCP project...${NC}"
gcloud config set project $PROJECT_ID

# Enable required APIs
echo -e "${YELLOW}🔧 Enabling required GCP APIs...${NC}"
gcloud services enable run.googleapis.com
gcloud services enable sql-component.googleapis.com
gcloud services enable sqladmin.googleapis.com
gcloud services enable secretmanager.googleapis.com
gcloud services enable cloudbuild.googleapis.com
echo -e "${GREEN}✅ APIs enabled${NC}"

# Deploy Backend
echo ""
echo -e "${YELLOW}🚀 Deploying Backend to Cloud Run...${NC}"
cd apps/backend

# Check if Dockerfile exists
if [ ! -f "Dockerfile" ]; then
    echo -e "${RED}❌ Dockerfile not found in apps/backend${NC}"
    exit 1
fi

# Get secrets (user should have created these beforehand)
read -p "Have you created secrets in Secret Manager? (y/n): " SECRETS_READY
if [ "$SECRETS_READY" != "y" ]; then
    echo -e "${YELLOW}⚠️  Please create secrets first:${NC}"
    echo "  - jwt-secret"
    echo "  - db-password"
    echo "  - sendgrid-api-key"
    echo ""
    echo "Example:"
    echo "  echo -n 'YOUR_SECRET' | gcloud secrets create jwt-secret --data-file=-"
    exit 1
fi

# Get Cloud SQL connection name
SQL_CONNECTION=$(gcloud sql instances describe $SQL_INSTANCE --format="value(connectionName)")
echo "Cloud SQL Connection: $SQL_CONNECTION"

# Deploy backend
gcloud run deploy collegebuddy-api \
  --source . \
  --platform managed \
  --region $REGION \
  --allow-unauthenticated \
  --set-env-vars="DB_USERNAME=collegebuddy" \
  --set-env-vars="DB_URL=jdbc:postgresql:///$SQL_INSTANCE?cloudSqlInstance=$SQL_CONNECTION&socketFactory=com.google.cloud.sql.postgres.SocketFactory" \
  --set-env-vars="EMAIL_FROM=noreply@$DOMAIN" \
  --set-env-vars="EMAIL_STRATEGY=smtp" \
  --set-env-vars="FRONTEND_URL=https://$DOMAIN" \
  --set-env-vars="CORS_ALLOWED_ORIGINS=https://$DOMAIN,https://www.$DOMAIN" \
  --add-cloudsql-instances=$SQL_CONNECTION \
  --update-secrets=DB_PASSWORD=db-password:latest \
  --update-secrets=JWT_SECRET=jwt-secret:latest \
  --update-secrets=SENDGRID_API_KEY=sendgrid-api-key:latest \
  --memory=1Gi \
  --cpu=1 \
  --min-instances=0 \
  --max-instances=10

# Get backend URL
BACKEND_URL=$(gcloud run services describe collegebuddy-api --region=$REGION --format="value(status.url)")
echo -e "${GREEN}✅ Backend deployed at: $BACKEND_URL${NC}"

# Test backend health
echo ""
echo -e "${YELLOW}🏥 Testing backend health...${NC}"
HEALTH_CHECK=$(curl -s -o /dev/null -w "%{http_code}" $BACKEND_URL/actuator/health)
if [ "$HEALTH_CHECK" = "200" ]; then
    echo -e "${GREEN}✅ Backend health check passed!${NC}"
else
    echo -e "${RED}❌ Backend health check failed (HTTP $HEALTH_CHECK)${NC}"
    echo "Check logs: gcloud run logs read --service=collegebuddy-api --region=$REGION"
    exit 1
fi

# Deploy Frontend
echo ""
echo -e "${YELLOW}🚀 Deploying Frontend to Cloud Run...${NC}"
cd ../frontend

# Update frontend environment
echo "VITE_API_URL=$BACKEND_URL" > .env.production
echo -e "${GREEN}✅ Frontend configured with backend URL${NC}"

# Build frontend
echo -e "${YELLOW}🔨 Building frontend...${NC}"
npm run build
echo -e "${GREEN}✅ Frontend built successfully${NC}"

# Check if we need to create a Dockerfile for frontend
if [ ! -f "Dockerfile" ]; then
    echo -e "${YELLOW}📝 Creating Dockerfile for frontend...${NC}"
    cat > Dockerfile << 'EOF'
FROM nginx:alpine
COPY dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
EOF

    cat > nginx.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    server {
        listen 8080;
        server_name _;
        root /usr/share/nginx/html;
        index index.html;

        location / {
            try_files $uri $uri/ /index.html;
        }

        location /assets {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
}
EOF
    echo -e "${GREEN}✅ Dockerfile created${NC}"
fi

# Deploy frontend
gcloud run deploy collegebuddy-web \
  --source . \
  --platform managed \
  --region $REGION \
  --allow-unauthenticated \
  --memory=512Mi \
  --cpu=1 \
  --min-instances=0 \
  --max-instances=10

# Get frontend URL
FRONTEND_URL=$(gcloud run services describe collegebuddy-web --region=$REGION --format="value(status.url)")
echo -e "${GREEN}✅ Frontend deployed at: $FRONTEND_URL${NC}"

# Summary
echo ""
echo -e "${GREEN}=================================="
echo "🎉 Deployment Complete!"
echo "==================================${NC}"
echo ""
echo -e "${YELLOW}📋 Deployment Summary:${NC}"
echo "  Backend:  $BACKEND_URL"
echo "  Frontend: $FRONTEND_URL"
echo ""
echo -e "${YELLOW}🌐 Next Steps:${NC}"
echo "  1. Test your app at: $FRONTEND_URL"
echo "  2. Map custom domain:"
echo "     - Backend:  api.$DOMAIN → $BACKEND_URL"
echo "     - Frontend: $DOMAIN → $FRONTEND_URL"
echo ""
echo "  Domain mapping commands:"
echo "     gcloud run domain-mappings create --service=collegebuddy-api --domain=api.$DOMAIN --region=$REGION"
echo "     gcloud run domain-mappings create --service=collegebuddy-web --domain=$DOMAIN --region=$REGION"
echo ""
echo -e "${YELLOW}📊 Monitor your deployment:${NC}"
echo "  Backend logs:  gcloud run logs read --service=collegebuddy-api --region=$REGION"
echo "  Frontend logs: gcloud run logs read --service=collegebuddy-web --region=$REGION"
echo ""
echo -e "${GREEN}🚀 Your app is live! Happy deploying!${NC}"
