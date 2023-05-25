FROM node:20.2-alpine
WORKDIR /app
COPY package*.json .
RUN npm i --silent
COPY . .
RUN echo "VITE_API_BASE_URL=http://localhost:8080" > .env
EXPOSE 5173
CMD ["npm", "run", "dev"]