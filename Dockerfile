FROM openjdk:11
COPY ./ /tmp
WORKDIR /tmp
# ENTRYPOINT ["java", "-cp .:lib/*", "SimpleHttpServer"]
ENTRYPOINT ["java","-jar","Server.jar"]