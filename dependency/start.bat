@echo off
set JAVA_HOME=%~dp0jre
echo JAVA_HOME=%JAVA_HOME%
rem set CLASSPATH=%JAVA_HOME%\lib\comm.jar
rem echo CLASSPATH=%CLASSPATH%
set PATH=%JAVA_HOME%\bin
java -jar jcomassist.jar
@pause