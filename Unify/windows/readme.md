# Steps to follow windows on premise installation


1. Install nginx-1.25.5, jdk-11, spark and hadoop winutils
2. Build the frontend (html css and js files) and backend(jar file) to /app folder of nginx directory.

FOLDER Structure needs to be:


```
\Bosler\
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
5. Once the boson is started it should bring up bosler at http://localhost or the specific url set in nginx conf file.
6. To Stop the server run the stopApplication.bat file

To add as a service

Copy nsmm and use it to add a service

nssm install BoslerBackend "C:\Bosler\startBackend.bat"
nssm install BoslerFrontend "C:\Bosler\startFrontend.bat"
nssm install BoslerRedis "C:\Bosler\startRedis.bat"

Set description

nssm set BoslerBackend Description "This is the Bosler Backend Application running with java."
nssm set BoslerFrontend Description "This is the Bosler Frontend Application running with Nginx."
nssm set BoslerRedis Description "This is the Bosler Caching service."



Set logging :


C:\Bosler\softwares>nssm set BoslerFrontend AppStdout C:\Bosler\logs\BoslerFrontend.log
Set parameter "AppStdout" for service "BoslerFrontend".

C:\Bosler\softwares>nssm set BoslerFrontend AppStderr C:\Bosler\logs\BoslerFrontend-error.log
Set parameter "AppStderr" for service "BoslerFrontend".

C:\Bosler\softwares>nssm set BoslerBackend AppStderr C:\Bosler\logs\BoslerBackend-error.log
Set parameter "AppStderr" for service "BoslerBackend".

C:\Bosler\softwares>nssm set BoslerBackend AppStdout C:\Bosler\logs\BoslerBackend.log
Set parameter "AppStdout" for service "BoslerBackend".


C:\Bosler\softwares>nssm set BoslerRedis AppStderr C:\Bosler\logs\BoslerRedis-error.log
Set parameter "AppStderr" for service "BoslerRedis".

C:\Bosler\softwares>nssm set BoslerRedis AppStdout C:\Bosler\logs\BoslerRedis.log
Set parameter "AppStdout" for service "BoslerRedis".

