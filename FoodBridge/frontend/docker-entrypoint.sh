#!/bin/sh
set -e

export PORT="${PORT:-80}"
export BACKEND_URL="${BACKEND_URL:-http://backend:8080}"
export BACKEND_URL=$(echo "$BACKEND_URL" | sed 's/\/*$//')

if [ -n "$API_BASE_URL" ]; then
    echo "[FoodBridge] Setting API_BASE_URL=${API_BASE_URL} in config.js"
    cat <<EOF > /usr/share/nginx/html/js/config.js
window.__ENV__ = {
    API_BASE_URL: "${API_BASE_URL}"
};
EOF
fi

echo "[FoodBridge] Substituting environment variables (PORT=${PORT}, BACKEND_URL=${BACKEND_URL})"
envsubst '${PORT} ${BACKEND_URL}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

exec nginx -g 'daemon off;'
