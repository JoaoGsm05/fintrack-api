@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements. See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership. The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License. You may obtain a copy of the License at
@REM
@REM   https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied. See the License for the
@REM specific language governing permissions and limitations
@REM under the License.

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%~dp0") ELSE SET "BASE_DIR=%__MVNW_ARG0_NAME__%"

@SET MAVEN_PROJECTBASEDIR=%BASE_DIR%
IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn" (
    SET "UPPER_BASEDIR=%MAVEN_PROJECTBASEDIR%.."
    IF EXIST "%UPPER_BASEDIR%\.mvn" SET "MAVEN_PROJECTBASEDIR=%UPPER_BASEDIR%"
)

@SET MVNW_REPOURL=https://repo.maven.apache.org/maven2
@SET JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
@IF NOT EXIST "%JAR%" (
    FOR /F "tokens=2 delims==" %%A IN ('findstr /i "wrapperUrl" "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"') DO SET WRAPPER_URL=%%A
    powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%', '%JAR%')}"
)

@SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
@IF NOT "%JAVA_HOME%"=="" GOTO javaHomeSet
@FOR /F "tokens=*" %%F IN ('where java 2^>nul') DO (SET "JAVA_EXE=%%F" & GOTO javaHomeSet)
:javaHomeSet

@"%JAVA_EXE%" -classpath "%JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
