# Steps to follow windows on premise installation


1. Install nginx-1.25.5, jdk-11, spark and hadoop winutils
2. Build the frontend (html css and js files) and backend(jar file) to /app folder of nginx directory.

FOLDER Structure needs to be:


```
\MoveToData\
+-- \app\
|   +-- \frontend\
|   +-- \boson\
+-- \softwares\
    +-- \JDK\
    |   +-- \zulu11.72.19-ca-jdk11.0.23-win_x64\
    +-- \nginx-1.25.5\
        +-- \conf\

```


3. Create a db with name - boson and make required changes to startApplication env variables such as db userName and password.
4. run the startApplication file with admin cmd.
5. Once the boson is started it should bring up movetodata at http://localhost or the specific url set in nginx conf file.
6. To Stop the server run the stopApplication.bat file

To add as a service

Copy nsmm and use it to add a service

nssm install MoveToDataBackend "C:\MoveToData\startBackend.bat"
nssm install MoveToDataFrontend "C:\MoveToData\startFrontend.bat"
nssm install MoveToDataRedis "C:\MoveToData\startRedis.bat"

Set description

nssm set MoveToDataBackend Description "This is the MoveToData Backend Application running with java."
nssm set MoveToDataFrontend Description "This is the MoveToData Frontend Application running with Nginx."
nssm set MoveToDataRedis Description "This is the MoveToData Caching service."



Set logging :


C:\MoveToData\softwares>nssm set MoveToDataFrontend AppStdout C:\MoveToData\logs\MoveToDataFrontend.log
Set parameter "AppStdout" for service "MoveToDataFrontend".

C:\MoveToData\softwares>nssm set MoveToDataFrontend AppStderr C:\MoveToData\logs\MoveToDataFrontend-error.log
Set parameter "AppStderr" for service "MoveToDataFrontend".

C:\MoveToData\softwares>nssm set MoveToDataBackend AppStderr C:\MoveToData\logs\MoveToDataBackend-error.log
Set parameter "AppStderr" for service "MoveToDataBackend".

C:\MoveToData\softwares>nssm set MoveToDataBackend AppStdout C:\MoveToData\logs\MoveToDataBackend.log
Set parameter "AppStdout" for service "MoveToDataBackend".


C:\MoveToData\softwares>nssm set MoveToDataRedis AppStderr C:\MoveToData\logs\MoveToDataRedis-error.log
Set parameter "AppStderr" for service "MoveToDataRedis".

C:\MoveToData\softwares>nssm set MoveToDataRedis AppStdout C:\MoveToData\logs\MoveToDataRedis.log
Set parameter "AppStdout" for service "MoveToDataRedis".

