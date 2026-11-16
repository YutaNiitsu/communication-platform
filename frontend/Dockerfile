# --- ビルドステージ ---
FROM node:20-alpine AS builder
WORKDIR /app

# package.json と pnpm-lock.yaml（または package-lock.json等）をコピー
COPY package*.json pnpm-lock.yaml* ./

RUN npm install -g pnpm

# 修正：スクリプト実行を許可するフラグ（--ignore-scripts）をつけてインストールする
RUN pnpm install --frozen-lockfile --ignore-scripts

COPY . .

ARG VITE_WS_BASE_URL
ARG VITE_API_BASE_URL

ENV VITE_WS_BASE_URL=$VITE_WS_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

RUN pnpm build

# --- 配信ステージ (Nginx) ---
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]