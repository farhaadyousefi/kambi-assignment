FROM ubuntu:latest

WORKDIR /app

RUN apt-get update -y && \
    apt-get install -y openjdk-17-jdk && \
    apt-get install -y net-tools && \
    apt-get install -y iputils-ping

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
 
COPY src ./src
# EXPOSE 8080
 
CMD ["./mvnw", "spring-boot:run"]