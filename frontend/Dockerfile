FROM jboss/wildfly:25.0.0.Final

RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin#70365

ADD ./patches/ ./target/patches/ /tmp/

USER jboss

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/wildfly-conf.cli

RUN rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history/current

ENV POSTGRES_HOSTPORT=localhost:5432
ENV POSTGRES_DATABASE=as10
ENV POSTGRES_USERNAME=as10
ENV POSTGRES_PASSWORD=as10

ENV _JAVA_OPTIONS=-Dfile.encoding=UTF-8

ADD target/gambol.war /opt/jboss/wildfly/standalone/deployments/gambol.war
