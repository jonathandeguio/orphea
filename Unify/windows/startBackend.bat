@echo off

:: Define Variables
set BOSLER_BASE=C:\Bosler
set JAVA_HOME=%BOSLER_BASE%\softwares\JDK\zulu11.52.13-ca-jdk11.0.13-win_x64
set HADOOP_HOME=%BOSLER_BASE%\softwares\hadoop-3.3.6
set SPARK_HOME=%BOSLER_BASE%\softwares\spark-3.5.1-bin-hadoop3
set JDK_BIN=%JAVA_HOME%\bin
set JAR_FILE=%BOSLER_BASE%\app\boson\boson-0.0.1-SNAPSHOT.jar

:: Environment Variables
:: Get the current timestamp using PowerShell and set the LAST_UPDATED_ON variable
for /f %%i in ('powershell -Command "[int][double]::Parse((Get-Date -UFormat %%s))"') do set LAST_UPDATED_ON=%%i

:: Print the timestamp to verify
echo LAST_UPDATED_ON=%LAST_UPDATED_ON%

set VERSION-platform=0.1.1
set VERSION-boslerDocs=0.0.20
set VERSION-boson=0.8.81
set VERSION-frontend=0.5.37

set SWAGGER_UI=false

set BASE_URL=http://demo.bosler.io

:: Database settings
set DB_USERNAME=postgres
set DB_PASSWORD=solqris
set DB_HOST=localhost
set DB_PORT=5432

set BOSLER_MOUNT_PATH=/Bosler/data/client
set GIT_CLONED_PATH=/Bosler/data/client/git/cloned

set BACKING_FS=localfs
set LOCAL_FS_DIRECTORY=/Bosler/data

set ALLOWED_ORIGINS=http://127.0.0.1:5500,http://127.0.0.1:3000,http://localhost:8082,http://localhost:8080,https://demo.bosler.io

set SPARK_MASTER=local[*]

set TOKEN_EXPIRATION=43200000
:: Below token secret should be changed per install
set TOKEN_SECRET=04ca023b39512e46d0c2cf4b48d5aac61d34302994c87boslerdemoed4eff225dcf3b0a218739f3897051a057f9b846a69ea2927a587044164b7bae5e1306219d50b588cb1

:: Add JAVA_HOME to PATH
set PATH=%PATH%;%JDK_BIN%;%HADOOP_HOME%\bin

:: Run the JAR file
%JDK_BIN%\java -jar %JAR_FILE%
