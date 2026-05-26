#!/bin/sh
# Gradle wrapper script
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD=java
fi
DIR=$(dirname "$0")
exec "$JAVACMD" \
    -Dorg.gradle.appname=gradlew \
    -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain "$@"
