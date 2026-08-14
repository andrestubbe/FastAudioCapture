@echo off
echo [FastAudioCapture] Building Native Library...
call compile.bat
call mvn clean package -DskipTests -q
cd examples\Demo
call mvn package -DskipTests -q
java -cp "target\demo-0.1.1.jar;..\..\target\FastAudioCapture-0.1.1.jar;%USERPROFILE%\.m2\repository\com\github\andrestubbe\FastCore\0.1.0\FastCore-0.1.0.jar;%USERPROFILE%\.m2\repository\com\github\andrestubbe\fastcore\0.1.0\fastcore-0.1.0.jar" fastaudiocapture.demo.Demo
cd ..\..
pause