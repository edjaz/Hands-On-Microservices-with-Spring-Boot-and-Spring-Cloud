FROM openjdk:12

EXPOSE 9999

ADD ./build/libs/*.jar app.jar

ENTRYPOINT ["java","-Djdk.tls.client.protocols=TLSv1.2","-jar","/app.jar"]
