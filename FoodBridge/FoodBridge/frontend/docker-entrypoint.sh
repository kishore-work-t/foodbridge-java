#!/bin/sh
set -e

export PORT="${PORT:-80}"
export BACKEND_URL="${BACKEND_URL:-http://backend:8080}"
export BACKEND_URL=$(echo "$BACKEND_URL" | sed 's/\/*$//')

# Ensure valid protocol prefix for Nginx proxy_pass
if ! echo "$BACKEND_URL" | grep -q '^[a-zA-Z][a-zA-Z0-9+-.]*://'; then
    if echo "$BACKEND_URL" | grep -q '\.onrender\.com'; then
        export BACKEND_URL="https://${BACKEND_URL}"
    elif echo "$BACKEND_URL" | grep -q ':[0-9]\+'; then
        export BACKEND_URL="http://${BACKEND_URL}"
    elif echo "$BACKEND_URL" | grep -q '\.'; then
        export BACKEND_URL="https://${BACKEND_URL}"
    else
        # Render service slug like 'foodbridge-backend-d0ui'
        export BACKEND_URL="https://${BACKEND_URL}.onrender.com"
    fi
fi

echo "[FoodBridge] Formatted BACKEND_URL: ${BACKEND_URL}"

# If API_BASE_URL was provided by user, use it; otherwise default to "/api"
CLIENT_API_BASE_URL="${API_BASE_URL:-/api}"

# Generate runtime config.js for the frontend browser
cat <<EOF > /usr/share/nginx/html/js/config.js
window.__ENV__ = {
    API_BASE_URL: "${CLIENT_API_BASE_URL}",
    BACKEND_URL: "${BACKEND_URL}"
};
EOF

echo "[FoodBridge] Substituting environment variables (PORT=${PORT}, BACKEND_URL=${BACKEND_URL})"
envsubst '${PORT} ${BACKEND_URL}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

exec nginx -g 'daemon off;'
