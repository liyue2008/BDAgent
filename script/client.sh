#!/bin/bash
EXE_NAME=BDAgentClient
#for PID in `ps -ef | grep -v grep | grep java | grep $EXE_NAME | awk '{print $2}'`
#do kill -9 $PID
#done
PRG="$0"

while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/"$link"
    fi
done

PRGDIR=`dirname "$PRG"`

cp=$PRGDIR/:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/lib/dt.jar
for libfile in $PRGDIR/*.jar; do
    cp=$libfile:$cp
done

DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1045"
#CLASSPATH=$cp
$JAVA_HOME/bin/java -D$EXE_NAME $JAVA_OPTS -classpath $cp bdagent.Client
