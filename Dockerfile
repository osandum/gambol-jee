FROM jboss/wildfly:11.0.0.Final

RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin#70365 

ADD ./patches/ds/ /tmp/

USER jboss

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/wildfly-conf.cli

ENV POSTGRES_HOSTPORT=localhost:5432
ENV POSTGRES_DATABASE=as10
ENV POSTGRES_USERNAME=as10
ENV POSTGRES_PASSWORD=as10

ENV _JAVA_OPTIONS=-Dfile.encoding=UTF-8

# ADD target/as11-0.2-SNAPSHOT/ /opt/jboss/wildfly/standalone/deployments/as11.war/
ADD target/as11-0.2-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/as11.war

