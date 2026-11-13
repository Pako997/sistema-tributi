FROM jboss/wildfly:18.0.1.Final

COPY modules/com/mysql /opt/jboss/wildfly/modules/com/mysql

COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/

COPY deployments/tributi.war /opt/jboss/wildfly/standalone/deployments/

ENV JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,address=0.0.0.0:8787,server=y,suspend=n"
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "--debug", "8787"]