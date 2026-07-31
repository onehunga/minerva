FROM oven/bun:1.3 AS bun

FROM mcr.microsoft.com/playwright:v1.61.1-noble
COPY --from=bun /usr/local/bin/bun /usr/local/bin/bun
WORKDIR /work

COPY package.json bun.lock ./
RUN bun install --frozen-lockfile

COPY . .
