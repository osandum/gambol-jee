FROM jboss/wildfly:20.0.1.Final

RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin#70365

ADD ./patches/ /tmp/

USER jboss

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/wildfly-conf.cli

ENV POSTGRES_HOSTPORT=localhost:5432
ENV POSTGRES_DATABASE=as10
ENV POSTGRES_USERNAME=as10
ENV POSTGRES_PASSWORD=as10

ENV _JAVA_OPTIONS=-Dfile.encoding=UTF-8

# ADD target/as11-0.2-SNAPSHOT/ /opt/jboss/wildfly/standalone/deployments/as11.war/
ADD target/gambol.war /opt/jboss/wildfly/standalone/deployments/gambol.war
