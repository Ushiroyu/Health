FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /app
COPY target/auth-service-*.jar app.jar
EXPOSE 9001
ENTRYPOINT ["java","-jar","/app/app.jar"]
