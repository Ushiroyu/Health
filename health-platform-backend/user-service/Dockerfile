FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /app
COPY target/user-service-*.jar app.jar
EXPOSE 9002
ENTRYPOINT ["java","-jar","/app/app.jar"]
