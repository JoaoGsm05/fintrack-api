#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.3.2
# ----------------------------------------------------------------------------

set -eu

# OS specific support.
cygwin=false
darwin=false
mingw=false
case "$(uname)" in
    CYGWIN*) cygwin=true;;
    DARWIN*) darwin=true;;
    MINGW*) mingw=true;;
esac

# Find the project base directory
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-}"
if [ -z "$MAVEN_PROJECTBASEDIR" ]; then
    WDIR="$PWD"
    while :; do
        if [ -d "$WDIR"/.mvn ]; then
            MAVEN_PROJECTBASEDIR=$WDIR
            break
        fi
        if [ "$WDIR" = "/" ]; then
            MAVEN_PROJECTBASEDIR="$PWD"
            break
        fi
        WDIR=$(dirname "$WDIR")
    done
fi

MVNW_REPOURL="${MVNW_REPOURL:-https://repo.maven.apache.org/maven2}"

JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
# If maven-wrapper.jar is not present, try to download it
if [ ! -f "$JAR" ]; then
    MVNW_VERBOSE="${MVNW_VERBOSE:-false}"
    if [ "$MVNW_VERBOSE" = true ]; then
        echo "Couldn't find $JAR, attempting to download it ..."
    fi
    WRAPPER_URL="$(grep -E "^wrapperUrl" "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties" | cut -d= -f2)"
    while IFS="=" read -r KEY VALUE; do
        case $KEY in
            wrapperUrl) WRAPPER_URL="$VALUE";;
        esac
    done < "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
    if command -v curl > /dev/null 2>&1; then
        curl -fsSL -o "$JAR" "$WRAPPER_URL" || rm -f "$JAR"
    elif command -v wget > /dev/null 2>&1; then
        wget -q -O "$JAR" "$WRAPPER_URL" || rm -f "$JAR"
    fi
fi

# Exec Java
JAVA_CMD="${JAVA_HOME:+$JAVA_HOME/bin/java}"
JAVA_CMD="${JAVA_CMD:-java}"

exec "$JAVA_CMD" \
    -classpath "$JAR" \
    "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
    org.apache.maven.wrapper.MavenWrapperMain "$@"
