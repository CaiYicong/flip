#!/usr/bin/env bash

bin=`which $0`
bin=`dirname ${bin}`
bin=`cd "$bin" > /dev/null; pwd`

FLIP_HOME=$(cd -P -- "$bin"/.. && pwd -P)

# Bail if we did not detect it
if [[ -z $JAVA_HOME ]]; then
  echo "Error: JAVA_HOME is not set and could not be found." 1>&2
  exit 1
fi

JAVA=$JAVA_HOME/bin/java

JAVA_HEAP_MAX=-Xmx1024m

CLASSPATH="${FLIP_HOME}/lib/*"

CLASS="org.alibaba.FlipCLI"

exec "$JAVA" -cp "$CLASSPATH" $JAVA_HEAP_MAX $CLASS "$@"
