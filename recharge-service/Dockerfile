# 1. Start with a lightweight Java runtime environment
FROM eclipse-temurin:17-jre-alpine

# 2. Create a temporary volume for Spring Boot
VOLUME /tmp

# 3. Copy your compiled project into the container
COPY target/*.jar app.jar

# 4. Define the command to run your app
ENTRYPOINT ["java","-jar","/app.jar"]