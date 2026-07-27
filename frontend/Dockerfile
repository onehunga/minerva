FROM oven/bun:1.3 AS bun

FROM node:24-bookworm AS build
COPY --from=bun /usr/local/bin/bun /usr/local/bin/bun
WORKDIR /app

COPY frontend/package.json frontend/bun.lock ./
RUN bun install --frozen-lockfile

COPY frontend/ .
RUN bun run build

FROM peaceiris/mdbook:v0.5.0 AS docs
WORKDIR /docs
COPY docs/ .
RUN mdbook build

FROM nginx:1.28-alpine
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
COPY --from=docs /docs/book /usr/share/nginx/html/docs
EXPOSE 80
