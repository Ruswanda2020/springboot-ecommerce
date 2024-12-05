FROM openjdk:21-jdk-slim

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw mvnw
COPY pom.xml pom.xml
COPY src src/

RUN chmod +x mvnw

RUN ./mvnw package -DskipTests

RUN cp target/*.jar app.jar

EXPOSE 3000

CMD ["java", "-jar", "app.jar"]
