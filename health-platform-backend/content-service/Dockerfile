FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /app
COPY target/content-service-*.jar app.jar
EXPOSE 9005
ENTRYPOINT ["java","-jar","/app/app.jar"]
