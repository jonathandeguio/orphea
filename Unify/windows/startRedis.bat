@echo off

:: Define Variables
set BOSLER_BASE=C:\Bosler
set REDIS_HOME=%BOSLER_BASE%\softwares\Redis-x64-5.0.14.1

:: Run the JAR file
%REDIS_HOME%\redis-server.exe
