@echo off

echo 🚀 Running Hero Demo...
cd examples\Demo
call mvn -q compile exec:java -Dexec.mainClass=fastaudiocapture.ConsoleRecordDemo
cd ..\..
pause
