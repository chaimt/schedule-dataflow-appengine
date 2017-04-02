# AppEngine Dataflow Scheduler
AppEngine application for scheduling the Google Dataflow process for migration process of data.


# Overview
Dataflow does not have a proper scheduling service, see:
https://cloud.google.com/blog/big-data/2016/04/scheduling-dataflow-pipelines-using-app-engine-cron-service-or-cloud-functions

So we have created an AppEngine application with a cron scheduler to run the dataflow process.
This project is a java application that will listen on port 8080 so that it can run as an AppEngine application.
For simplicity we have used sparkjava as the framework for a simple service that answers the http on port 8080.

Note: to install AppEngine you need to create it within a Google Cloud project. To use the flex feature (see app.yaml below)
 the AppEngine must be in region (us-xxx). The region is set once and cannot be changes later (you will need to delete the project to fix this).

Table of Contents

* <a href="#project">Project</a>
* <a href="#usage">Usage</a>
* <a href="#debugging">Configuration</a>
* <a href="#debugging">Debugging</a>
* <a href="#contact">Contact</a>

<a name="project"></a>
## Project
The main application is: ScheduleDataflow. This class will run the dataflow pipeline

###app.yaml
Our application is based on java 8 so we cannot use the basic AppEngine but need to use the flexable one that is based on Docker.
To do this in the app.yaml file we need to define:
```
runtime: custom
env: flex
```

###Docfile
Since we are docker based we need to have a docker file that will install and run our app.
AppEngine needs the docker file to be under: /src/main/docker. Since docker needs all files that it adds to be under the same directory
the docker will not find the target jar file.
To solve this make sure in the pom file there is the following property:

```
<app.stage.artifact>
    ${project.build.directory}/migration-dataflow-appengine-1.0.0-SNAPSHOT-jar-with-dependencies.jar
</app.stage.artifact>
```    
 
<a name="usage"></a>
## Usage
#### Compile
To compile the project, use the standard
```
mvn clean install
```
#### Deploy
To deploy the new AppEngine run: 
```
mvn appengine:deploy
```

To see the app in the cloud go to:
https://console.cloud.google.com/appengine/services?project=bq-migration3

#### Deploy cron
The cron.yaml file is in the root folder. It needs to be deployed separately than the AppEngine app.
To deploy the cron run the command:

```
gcloud app deploy cron.yaml
```

To view the cron job in the cloud go to:
https://console.cloud.google.com/appengine/taskqueues?project=bq-migration3

<a name="configuration"></a>
## Configuration
To change the parameters for the AppEngine application, you need to edit the cron.yaml file.
In this file you can change the parameters that are send to the application, which include all parameters to start the dataflow application

<a name="debugging"></a>
## Debug app
To view the log of the AppEngine application run the following command:
```
gcloud app logs tail -s schedual-dataflow
```

<a name="contact"></a>
## Contact
### Contributors
* [Chaim Turkel](http://www.tikalk.com/java/chaim.turkel/) (Email: chaim@tikalk.com)
