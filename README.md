#Sftp File Polling

Purpose
=======

Reads file from read only Sftp folder.

Components Used To Develop Project
==================================

1. CrushFTP
2. Anypoint Studio
3. mule-ee-distribution-standalone-3.7.1
4. apache-maven-3.3.3-bin

Project Setup
==============

### Step 1: Set Up Crush SFTP Server (Optional: If SFTP server is not already installed)

1. Download crush sftp server from <a href="http://www.crushftp.com/download.html"> http://www.crushftp.com/download.html.
2. Download the zip as per the operating system.
   Extract the zip file and run CRUSHFTP.exe. The following dialogue box is opened
 
   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/crushftpexe.PNG)

3. Click on "Create New Admin User". Provide the username and password to create new admin user.
4. Click on "Start Temporary Server" to start sftp. Sftp server starts with the following message

   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/serverstarted1.PNG)

5. To configure Sftp polling path as read only, log on to the server <a href="http://127.0.0.1:8080/">http://127.0.0.1:8080/.
   After log in below page is displayed.

   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/admin.PNG)
  
6. Create a new user by navigating "Admin"---> "User Manager" -----> "Add".
  
   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/usermanager1.png)
  
7. To configure the Sftp path click on the newly created user. Drag file from Server to user and give it read only access.

   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/sftp%20path%20conf1.PNG)

8. Now log on to the Sftp server with credentials of newly created user to check if the path is correctly configured.
 
### Step 2: Import Mule Project

1. To import the project, first extract "sftp-file-poll.zip". Then import it as "Maven based Mule Project from pom.xml"

   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/projectimport.PNG)
    
2. The project is developed and tested with studio run time Mule Server 3.6.0 EE. Please ensure mule studio has required munit plugins.
3. Open file sftp.properties available under src/main/resources. Change the properties to reflect sftp parameters as per your system configuration.
    
   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/sftp%20cred.PNG)

### Step 3: Code Functinality Details

1. By default sftp connector does not handle duplicate file polling. 
2. To handle duplicate file handling scenario, custom java codes are wriiten, which overrides the connector functionality to avoid duplicate file processing. These java files area available under src/main/java/ 
 
   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/javacode.png)

3. The files which are polled are inserted into database table 'FileDB'. The database used is Mule Embedded in memory derby database. This configuration is done in the class "StorePolledFiles.java". 
4. The class "CustomSftpReceiverRequesterUtil.java" checks the available files in inbound folder and picks them for processing. Before picking the files to process, it checks in the database if the file is already processed or not.
5. This java class "CustomSftpMessageReceiver.java" (which has the poll method and calls "CustomSftpReceiverRequesterUtil.java" ) is injected as the message receiver class for the sftp connector as shown below.

   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/Capture.PNG)


### Step 4: Running Project in Standalone Server

1. Download mule enterprise standalone server zip file and extract it.
2. Open the command prompt and navigate to mule workspace where "sftp-file-polling" project is availablle.
3. Run the command "mvn clean package".

   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/buildscreen.PNG)
4. When build is successful the project archive "sftp-file-poll-1.0.0-SNAPSHOT.zip" is created as shown below

   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/target.png)
5. Now copy this archive to appps folder of the standalone server( mule-enterprise-standalone-3.7.1/apps)
6. Now in the command prompt navigate to the path (mule-enterprise-standalone-3.7.1/bin/). Start mule

   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/mule%20server%20start.PNG)
   
7. When mule standalone server starts it deploys all the project build available in the apps folder.
8. When the project is successfully deployed it shows below message 

   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/deploy.PNG) 

9. Below is the log snippet of files processed by "sftp-file-polling" application.
   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/logsnippet.png) 

### Step 5: Test Case

1. In mule end points are by default mocked. The project uses mock sftp data for testing.
2. The project uses java based munit framework to test duplicate file processing scenario.
3. The test java class is available in src/test/java/MunitSFTPPollTest.java.
4. When trying to push same file twice below log snippet is shown. 

   ![ScreenShot](https://raw.githubusercontent.com/indiramallick1988/Demo2/master/tool/junit.PNG) 
