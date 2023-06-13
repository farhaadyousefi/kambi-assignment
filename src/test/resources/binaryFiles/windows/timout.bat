@echo off

REM Wait for 10 seconds
ping 127.0.0.1 -n 11 > nul
set "directory=%~1"
cd /d "%directory%"
dir