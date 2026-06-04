@rem Gradle startup script for Windows
@if "%DEBUG%" == "" @echo off
@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_HOME=%DIRNAME%
set GRADLE_USER_HOME=%GRADLE_USER_HOME%
java -version >/dev/null 2>&1 || (echo ERROR: JAVA_HOME is not set && exit /b 1)
java -cp "%APP_HOME%/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
