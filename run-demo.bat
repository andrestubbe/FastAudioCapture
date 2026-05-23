@echo off
echo ⚡ Building Main Project...
call mvn clean package -DskipTests
if %ERRORLEVEL% NEQ 0 ( pause & exit /b )
echo 🚀 Running Hero Demo...
cd examples\ConsoleRecordDemo.java
call mvn compile exec:java -Dexec.mainClass=fastaudiocapture.Demo
cd ..\..
pause
