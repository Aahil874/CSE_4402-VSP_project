@echo off
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac --module-path "lib\javafx\lib;lib\mysql-connector-java-8.0.25.jar" --add-modules javafx.controls,javafx.fxml -d bin @sources.txt
del sources.txt
xcopy /s /e /y src\* bin\ >nul
del /s /q bin\*.java >nul
java --module-path "lib\javafx\lib;bin;lib\mysql-connector-java-8.0.25.jar" --add-modules javafx.controls,javafx.fxml -m final_homerun/application.Main
pause
