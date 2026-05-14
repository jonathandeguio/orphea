# Steps to follow windows on premise installation


1. Install nginx-1.25.5, jdk-11, spark and hadoop winutils
2. Build the frontend (html css and js files) and backend(jar file) to /app folder of nginx directory.

FOLDER Structure needs to be:


```
\Orphea\
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
5. Once the boson is started it should bring up orphea at http://localhost or the specific url set in nginx conf file.
6. To Stop the server run the stopApplication.bat file

To add as a service

Copy nsmm and use it to add a service

nssm install OrpheaBackend "C:\Orphea\startBackend.bat"
nssm install OrpheaFrontend "C:\Orphea\startFrontend.bat"
nssm install OrpheaRedis "C:\Orphea\startRedis.bat"

Set description

nssm set OrpheaBackend Description "This is the Orphea Backend Application running with java."
nssm set OrpheaFrontend Description "This is the Orphea Frontend Application running with Nginx."
nssm set OrpheaRedis Description "This is the Orphea Caching service."



Set logging :


C:\Orphea\softwares>nssm set OrpheaFrontend AppStdout C:\Orphea\logs\OrpheaFrontend.log
Set parameter "AppStdout" for service "OrpheaFrontend".

C:\Orphea\softwares>nssm set OrpheaFrontend AppStderr C:\Orphea\logs\OrpheaFrontend-error.log
Set parameter "AppStderr" for service "OrpheaFrontend".

C:\Orphea\softwares>nssm set OrpheaBackend AppStderr C:\Orphea\logs\OrpheaBackend-error.log
Set parameter "AppStderr" for service "OrpheaBackend".

C:\Orphea\softwares>nssm set OrpheaBackend AppStdout C:\Orphea\logs\OrpheaBackend.log
Set parameter "AppStdout" for service "OrpheaBackend".


C:\Orphea\softwares>nssm set OrpheaRedis AppStderr C:\Orphea\logs\OrpheaRedis-error.log
Set parameter "AppStderr" for service "OrpheaRedis".

C:\Orphea\softwares>nssm set OrpheaRedis AppStdout C:\Orphea\logs\OrpheaRedis.log
Set parameter "AppStdout" for service "OrpheaRedis".

