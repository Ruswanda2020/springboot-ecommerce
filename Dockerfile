# openjdk 23 sebagai base image
FROM openjdk:23-slim

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src src/
RUN chmod +x mvnw

RUN ./mvnw package -DskipTest
RUN cp target/*.jar app.jar
EXPOSE 4000
#run jar file
CMD ["java", "-jar", "app.jar"]