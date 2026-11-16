FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests
RUN mv /app/target/nhd*.jar /app/target/nhd-processor.jar

# Second stage
FROM amazoncorretto:17-alpine
COPY --from=build /app/target/nhd-processor.jar /nhd-processor.jar
ENV TZ="Asia/Kolkata"
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/nhd-processor.jar"]
