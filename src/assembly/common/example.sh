#!/bin/bash

export JVM_SID="EXAMPLE_JETTY"

HOSTNAME=`hostname`
#echo "Running on $HOSTNAME"

case $HOSTNAME in
*)

export JETTY_HOME=.

#export JAVA_HOME=/usr/java/latest

JAVA_OPTS="-server -Xms128m -Xmx246m -XX:PermSize=64m -XX:MaxPermSize=128m -Dfile.encoding=utf-8"
#JMX monitoring
#JAVA_OPTS="${JAVA_OPTS} -Dcom.sun.management.jmxremote.port=5679 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
#heap dump is always useful
JAVA_OPTS="${JAVA_OPTS} -XX:+HeapDumpOnOutOfMemoryError"
#garbage collection debugging
#JAVA_OPTS="${JAVA_OPTS} -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"

export JAVA_OPTS
	;;
*)
	echo "Unsupported host $HOSTNAME"
	;;
esac

$JETTY_HOME/jettyctl.sh $*