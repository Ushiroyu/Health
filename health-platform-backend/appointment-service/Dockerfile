FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /app
COPY target/appointment-service-*.jar app.jar
EXPOSE 9004
ENTRYPOINT ["java","-jar","/app/app.jar"]
