FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /app
COPY target/activity-service-*.jar app.jar
EXPOSE 9006
ENTRYPOINT ["java","-jar","/app/app.jar"]
