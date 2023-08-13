FROM openjdk:17-alpine
WORKDIR /app
EXPOSE 8091
COPY ./build/libs/gsuite-0.0.1-SNAPSHOT.jar gsuiteservice.jar
CMD ["java","-jar","gsuiteservice.jar"]