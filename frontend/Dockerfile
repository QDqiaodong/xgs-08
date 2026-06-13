ARG DOCKER_REGISTRY=

FROM ${DOCKER_REGISTRY}library/node:18-alpine AS builder

WORKDIR /app

RUN npm config set registry https://registry.npmmirror.com

COPY package*.json ./
RUN npm ci --prefer-offline --no-audit --no-fund

COPY . .
RUN npm run build

FROM ${DOCKER_REGISTRY}library/nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
