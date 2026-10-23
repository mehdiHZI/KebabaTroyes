FROM node:22-alpine
WORKDIR /app
COPY package.json ./
COPY src ./src
ENV NODE_ENV=production
ENV PORT=8080
ENV DATA_DIR=/data
# Volume Docker monté au runtime — process root pour écrire /data (V1)
RUN mkdir -p /data && chmod 777 /data
VOLUME ["/data"]
EXPOSE 8080
CMD ["node", "src/index.js"]
