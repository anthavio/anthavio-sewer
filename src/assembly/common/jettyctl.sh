#!/bin/bash

DIRNAME=`dirname $0`
PROGNAME=`basename $0`

warn() {
	echo "${PROGNAME}: $*" 2>&1
}

die() {
    warn $*
    exit 1
}

# required JVM_SID
if [ "$JVM_SID" == "" ]; then
	die "JVM_SID is not set"
fi
# optional JETTY_HOME, JAVA_OPTS, JETTY_CONFIGS, STOP_TIMEOUT
if [ "$JETTY_HOME" == "" ]; then
	JETTY_HOME=.
fi

cd $JETTY_HOME

if [[ $EUID -eq 0 ]]; then
	die "Do not run this script as root"
fi

usage() {
	die "usage: $0 start|stop|kill|restart|status"
}

doStart() {
	
	isRunningJvm ${JVM_SID}
	if [ $? -eq 0 ]; then
		die "JVM ${JVM_SID} is running with pid ${JVM_PID}"
	fi
	
	JETTY_CONFIGS="$JETTY_HOME/etc/jetty.xml $JETTY_HOME/etc/jetty-logging.xml"
	nohup $JAVA_HOME/bin/java -DJVM_SID=${JVM_SID} -Djetty.home=$JETTY_HOME $JAVA_OPTS -jar $JETTY_HOME/start.jar > $JETTY_HOME/logs/std.out 2>&1 &  
	
	RC_NOHUP=$?
#	if [ "x$3" != "xnt" -a "x$3" != "xnotail" ]; then
#		tail -f ${SYSOUT_FILE}
#	fi
	if [ ${RC_NOHUP} -eq 0 ]; then
		exit 0
	else
		exit 1
	fi
}

doStop() {
	
	isRunningJvm ${JVM_SID}
	if [ $? -eq 1 ]; then
		die "JVM ${JVM_SID} not found"
	fi

	if [ "$STOP_TIMEOUT" != "" ]; then
		TIMEOUT=$STOP_TIMEOUT
	else
		TIMEOUT=30
	fi
	
	while [ $TIMEOUT -gt 0 ]
	do
		kill $JVM_PID 2>/dev/null
		isRunningPid $JVM_PID
		if [ $? -eq 0 ]; then
			break
		fi
		sleep 5
		let TIMEOUT=$TIMEOUT-1
	done
	
	if [ $TIMEOUT -le 0 ]; then
		kill -9 $JVM_PID 2>/dev/null
		echo "JVM did not stop in limit and therefore was kill by 9 SIGNAL"
	fi
}

doKill() {
	isRunningJvm ${JVM_SID}
	if [ $? -eq 1 ]; then
		die "JVM ${JVM_SID} not found"
	fi
	kill -9 ${JVM_PID}
} 

doStatus() {
	isRunningJvm ${JVM_SID}
	if [ $? -eq 1 ]; then
		echo "JVM ${JVM_SID} not found"
		exit 1
	else
		echo "JVM ${JVM_SID} is running with pid ${JVM_PID}"
		exit 0
	fi
}

function isRunningJvm() {
	getJvmPid $1
	if [ "x${JVM_PID}" = "x" ]; then 
		return 1
	else
		return 0
	fi
}

function isRunningPid() {
    ps -p $1 >/dev/null 2>/dev/null || return 0
    return 1
}

function getJvmPid() {
	JVM_PID=`$JAVA_HOME/bin/jps -J-Xms32m -J-Xmx32m -mlv | grep "JVM_SID=${1}" | cut -f 1 -d ' '`
	return $JVM_PID
}

case "$1" in
"start"|"st")
	doStart
	;;
"stop"|"sp")
	doStop
	;;
"kill"|"kl")
	doKill
	;;
"status"|"ss")
	doStatus
	;;
"restart"|"rt")
	doStop
	sleep 5
	doStart
	;;
*)
	usage
	;;
esac 
