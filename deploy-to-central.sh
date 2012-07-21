#!/bin/bash
mvn clean test javadoc:jar source:jar package org.apache.maven.plugins:maven-gpg-plugin:sign deploy

