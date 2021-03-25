FROM openjdk:12

EXPOSE 8761

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java","-Djdk.tls.client.protocols=TLSv1.2","-jar","/app.jar"]
