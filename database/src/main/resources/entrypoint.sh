#!/bin/sh

# Replace placeholders in the test-ds.xml file using environment variables
envsubst < $JBOSS_HOME/standalone/deployments/test-ds.xml > /tmp/test-ds.xml
mv /tmp/test-ds.xml $JBOSS_HOME/standalone/deployments/test-ds.xml

# Start WildFly
$JBOSS_HOME/bin/standalone.sh -b 0.0.0.0 -c standalone.xml
