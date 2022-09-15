FROM openjdk:8-jre-alpine

ADD target/scala-**/akka-consumer-assembly-1.0.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]

