# Build Linux runtime dependencies even when the operator uses macOS.
FROM node:24-bookworm AS build
WORKDIR /app
ENV NEXT_TELEMETRY_DISABLED=1
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build
RUN cp -a public .next/standalone/public && cp -a .next/static .next/standalone/.next/static
FROM scratch AS export
COPY --from=build /app/.next/standalone/ /frontend/
