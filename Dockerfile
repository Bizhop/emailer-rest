FROM amazoncorretto:17

COPY "build/libs/emailer-rest-0.0.1-SNAPSHOT.jar" "/app.jar"

EXPOSE 8080
CMD [ "-jar", "/app.jar" ]
ENTRYPOINT [ "java" ]
