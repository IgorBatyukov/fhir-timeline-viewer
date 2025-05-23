FROM clojure:temurin-17-tools-deps-1.12.0.1479 AS build

RUN apt-get update && apt-get install -y nodejs npm && apt-get clean

WORKDIR /usr/src/app

COPY deps.edn .
COPY shadow-cljs.edn .
COPY package.json .
COPY package-lock.json .

RUN npm install

COPY resources ./resources/
COPY src ./src/

RUN npx shadow-cljs release app

RUN clojure -Sdeps '{:deps {uberdeps/uberdeps {:mvn/version "1.0.2"}}}' -m uberdeps.uberjar --target app.jar

FROM eclipse-temurin:17

WORKDIR /usr/src/app

COPY --from=build /usr/src/app/app.jar .
COPY --from=build /usr/src/app/resources ./resources/


ENV SERVER_PORT=8080
ENV DB_HOST=db
ENV DB_PORT=5432
ENV DB_NAME=postgres
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres

EXPOSE 8080

CMD ["java", "-cp", "app.jar", "clojure.main", "-m", "fhir-timeline-viewer.core"]
