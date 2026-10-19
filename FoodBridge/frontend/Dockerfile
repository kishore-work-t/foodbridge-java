FROM nginx:alpine

# Remove default nginx configuration and default files
RUN rm -rf /etc/nginx/conf.d/* /usr/share/nginx/html/*

# Copy static frontend assets
COPY . /usr/share/nginx/html/

# Copy nginx configuration template and startup entrypoint
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# Clean up deployment artifacts from web root
RUN rm -f /usr/share/nginx/html/Dockerfile \
          /usr/share/nginx/html/docker-entrypoint.sh \
          /usr/share/nginx/html/nginx.conf.template \
          /usr/share/nginx/html/.dockerignore

# Render sets PORT dynamically, defaults to 80
ENV PORT=80
ENV BACKEND_URL=http://backend:8080

EXPOSE 80

ENTRYPOINT ["/docker-entrypoint.sh"]
