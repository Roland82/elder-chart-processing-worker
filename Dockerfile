FROM hseeberger/scala-sbt:latest

MAINTAINER roland.ormrod@googlemail.com

RUN mkdir /usr/app
COPY target/scala-2.11/elder_2.11-SNAPSHOT-one-jar.jar /usr/app/app.jar
CMD ["java", "-jar", "/usr/app/app.jar"]