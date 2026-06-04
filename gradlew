#!/bin/sh
# Gradle wrapper script
export ANDROID_SDK_ROOT="C:\Users\tomhu\Android\Sdk"
export ANDROID_HOME="C:\Users\tomhu\Android\Sdk"
export JAVA_HOME="$(dirname $(dirname $(readlink -f $(which java))))"
exec gradle "$@"
