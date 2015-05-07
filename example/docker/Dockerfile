FROM jboss/base-jdk:7

RUN cd /opt
RUN mkdir chooks
RUN cd chooks

ADD example-chooks.jar /opt/chooks/

EXPOSE 8080

CMD $JAVA_HOME/bin/java -jar /opt/chooks/example-chooks.jar
