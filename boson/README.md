
# Boson
The project name comes from Higgs Boson.

Basically, boson connects / colllects everything together

To use this project, you need postgres locally installed and it should have a database called boson


## Run Locally

1. Clone the project
```bash
  git clone https://github.com/Bosler-io/boson
```

2. Install dependencies:
* IntelliJ Idea IDE
* Gradle 7.1.1 - 'prefer moving the folder to local Disk:C'
* Postgres SQL with PgAdmin
* Apache spark


3. Open IntelliJ Idea with boson project folder

4. Go to File --> Settings or Use Control+Alt+S to open Settings. 
   * Pop up will appear. On left Panel Expand 
   * 'Build', 'Execution', 'Deployment' --> 'Build Tools' --> 'Gradle'. 
   Expand & on right side look for Keys &
   * 'Build and run using' : 'IntelliJ IDEA'
   * 'Run tests using' : 'IntelliJ IDEA'. 
   * 'Use Gradle from' : 'Specified location'   -->> 'Use the local to gradle-7.1.1 location'
   * 'Gradle JVM' : 'Project SDK azul-11' If not found 
   Expand and click on Download JDK Use Configs 
   * 'Version : 11' & 'Vendor : Azul Community' 
   * 'Location : As is it'"
   * Click Apply --> Ok



5. Click on Add_Configration

![AddConfig](https://i.ibb.co/N38GBsv/Screenshot-2022-08-20-003201.png)

6. Select Application from the configration menu

7. Name it as boson

8. Under 'build and run' select 'Azul Zulu 11', module-cp as boson.main and main class as io.bosler.boson

9. Add the following string in environment variables

* Note: for environment key contact team members!

10. Click on modify options and select 'Shorten Command Line' and there select '@argfile' option.

11. Click on apply and then Ok

![Config Image](https://i.ibb.co/QcdR3Gq/Screenshot-2022-08-20-004427.png)
