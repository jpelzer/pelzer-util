#!/bin/bash
export GPG_TTY=$(tty)
#mvn clean test javadoc:jar source:jar package org.apache.maven.plugins:maven-gpg-plugin:sign deploy
mvn clean release:clean release:prepare release:perform -e
