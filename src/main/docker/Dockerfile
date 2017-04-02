FROM gcr.io/google_appengine/openjdk8
VOLUME /tmp
ADD dataflow-package-appengine-1.0.0-SNAPSHOT-jar-with-dependencies.jar app.jar
CMD [ "java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]