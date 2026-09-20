#!/bin/bash
mkdir -p bin
javac --module-path "/usr/lib/jvm/openjfx:lib/mysql-connector-java-8.0.25.jar:lib/javafx/lib" --add-modules javafx.controls,javafx.fxml -d bin $(find src -name "*.java")
cp -r src/* bin/
find bin -name "*.java" -type f -delete
java --module-path "/usr/lib/jvm/openjfx:bin:lib/mysql-connector-java-8.0.25.jar:lib/javafx/lib" --add-modules javafx.controls,javafx.fxml -m final_homerun/application.Main
