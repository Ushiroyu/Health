FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /app
COPY target/doctor-service-*.jar app.jar
EXPOSE 9003
ENTRYPOINT ["java","-jar","/app/app.jar"]
