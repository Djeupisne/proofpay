# --- Build stage ---
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
# npm ci = install reproductible à partir du lockfile (à préférer à npm install en CI/prod)
RUN npm ci
COPY . .
RUN npm run build:prod

# --- Serve stage ---
FROM nginx:alpine
COPY --from=build /app/dist/proofpay-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost:80/ || exit 1
