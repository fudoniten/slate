# Build stage
FROM node:20-alpine AS builder

WORKDIR /build

# Install build dependencies
RUN apk add --no-cache \
    openjdk17 \
    clojure \
    git \
    curl

# Copy project files
COPY deps.edn shadow-cljs.edn package.json package-lock.json* ./
COPY src ./src
COPY resources ./resources

# Install dependencies and build
RUN npm install
RUN npm run build

# Production stage
FROM node:20-alpine

WORKDIR /app

# Install runtime dependencies
RUN apk add --no-cache \
    dumb-init \
    ca-certificates

# Copy built artifacts from builder
COPY --from=builder /build/resources/public ./resources/public
COPY --from=builder /build/server.js ./
COPY --from=builder /build/package.json ./

# Install production dependencies only
RUN npm install --omit=dev

# Create non-root user
RUN addgroup -g 1000 app && \
    adduser -D -u 1000 -G app app && \
    chown -R app:app /app

USER app

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD node -e "require('http').get('http://localhost:3000/health', (r) => {if (r.statusCode !== 200) throw new Error(r.statusCode)})"

ENTRYPOINT ["dumb-init", "--"]
CMD ["node", "server.js"]
