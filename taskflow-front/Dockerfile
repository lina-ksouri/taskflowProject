FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# Les variables VITE_* sont intégrées au build
ARG VITE_KEYCLOAK_URL
ARG VITE_KEYCLOAK_REALM=taskflow
ARG VITE_KEYCLOAK_CLIENT_ID=taskflow-front
ARG VITE_API_URL
RUN npm run build

FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO /dev/null http://127.0.0.1/ || exit 1
