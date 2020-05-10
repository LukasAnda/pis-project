#FROM gradle:5.6.4-jdk11 as cache
#RUN mkdir -p /home/gradle/cache_home
#ENV GRADLE_USER_HOME /home/gradle/cache_home
#COPY build.gradle /home/gradle/java-code/
#WORKDIR /home/gradle/java-code
#RUN gradle clean build -i --stacktrace

FROM gradle:5.6.4-jdk11 as builder
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
#COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /usr/src/java-code/
WORKDIR /usr/src/java-code
RUN gradle clean wsdl2java build bootJar -i --stacktrace
#RUN gradle bootJar -i --stacktrace

FROM openjdk:11-jre-slim
EXPOSE 8080
USER root
WORKDIR /usr/src/java-app
COPY --from=builder /usr/src/java-code/build/libs/*.jar ./app-server.jar
RUN pwd
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","app-server.jar"]