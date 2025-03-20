FROM node:22 as build-stage

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

CMD ["npm", "start"]

EXPOSE 4200
