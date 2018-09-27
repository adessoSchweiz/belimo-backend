FROM jboss/base-jdk:8

ADD target/user-portal-0.0.1-SNAPSHOT.jar .
CMD ["java", "-jar", "user-portal-0.0.1-SNAPSHOT.jar"]