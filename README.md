**Alfresco Docusign Addon**

The Docusign Addon allows a user to request an electronic signature for a document from within Alfresco Share or Alfresco Digital Workspace.  A signature request can be sent to Multiple Signers and the signature lifecycle may be tracked directly from Alfresco.  On completion of the signature request by all signees a completed copy of the signed document is stored in Alfresco for your records.

**Building Alfresco Docusign Addon**

    Clone Alfresco Shop from github repo
	   $ git clone https://github.com/boneill/Alfresco-Docusign.git
    
    Build Alfresco AMPs
	   From Alfresco-Docusign directory, run the following command 
     		./run.sh build_start
	   The above command generates two amps located in the following directory;
   	   	Repo AMP
	    	Alfresco-Docusign/seedim-docusign-platform/target/seedim-docusign-platform-1.0-SNAPSHOT.amp
   		Share AMP 
	 	Alfresco-Docusign/seedim-docusign-share/target/seedim-docusign-share-1.0-SNAPSHOT.amp

    Build Alfresco Docusign Content App
	   From Alfresco-Docusign/alfresco-content-app folder run the following commands to build the Alfresco Docusign Content App.
	   Install Alfresco Content App dependencies
		   npm install
	   Build ACA-Shared library since the Alfresco-Docusign Add On uses resources from the ACA-Shared
		   ng build aca-shared
	   Build Docusign library which is the core of the Alfresco Docusign Content App
		   npm run build:docusign
	   Run the following command to verify the app builds and works accordingly
		   npm start
	   The following command creates a transpiled version of the code in Alfresco-Docusign/alfresco-content-app/dist folder that can be released to a web server like
	   Apache/NGIX or a web container like Apache Tomcat. The default release folder is called app but it can be renamed as required e.g workspace.
		   ng build –prod

**Installing Alfresco Docusign Addon**

    Installing Alfresco AMPs
    This requires the installation of the Repository and Share amps.
    	Download the required AMPs from the release section of the Alfresco-Docusign in Github or download the source code and build the amps as described in the 
	Building Alfresco Docusign Add section.
    	Stop Alfresco
    	Copy seedim-docusign-platform-1.0-SNAPSHOT.amp to $AlfrescoHome/amps
    	Copy seedim-docusign-share-1.0-SNAPSHOT.ampto $AlfrescoHome/amps_share
    	Run <AlfrescoHome>/bin/apply_amps.sh -force for Linux and <AlfrescoHome>/bin/apply_amps.bat -force for Windows
    	Start Alfresco
    	Important:  
    	On startup of the new install a bootstrap class creates the necessary config files in the Data Dictionary.  
    	A bootrap class will only run once and cause the startup to fail on subsequent starts.  
    	In order to prevent this, you must disable the bootstrap as follows: 
      		Locate your global alfresco-global.properties file.  Depending on the installation it is often located in $AlfrescoHome/tomcat/shared/classes 
      		or $AlfrescoHome/tomcat/webapps/alfresco/WEB-INF/lib.
      		Add the following config in  the global alfresco-global.properties:  DS_MODULE_BOOTSTRAP_DATA=false

**Installing the Alfresco Docusign Angular Application**

 The following install instructions are for installing the Angular Application on the Tomcat that you are running Alfresco on.
 However, it does not need to be co-located with the Alfresco application and could be installed on nginx or apache web server.
       
      Download the alfresco-docusign.zip from the release section of the Alfresco-Docusign in Github or download the source code and 
      build the angular distribution files as described in the Building Alfresco Docusign Add section.
      Extract the alfresco-docusign.zip folder and copy the alfresco-docusign folder to Tomcat webapps directory.
      Update the app.config.json (in the root of the alfresco-docusign folder). Set “ecmHost”: “<URL to your Alfresco application>.eg http://localhost:8080”
      
**Configuring Alfresco Docusign Add On**

    Once Alfresco has started, browse to Repository > Data Dictionary.
    
   <img width="464" alt="docusign_config_folder" src="https://user-images.githubusercontent.com/9836573/128952980-1e4a2981-184f-4d00-ad7d-e9037b9dde42.png">   
    
    You should see a folder called Docusign-Config. Hover over it and click on Edit Properties > All Properties. The following form should be available.
   ![docusign_config_form](https://user-images.githubusercontent.com/9836573/128952895-8cf7b75a-fd9a-4055-bf5e-6381ac105169.png)
   
    
    Fill in the JWT Authentication details for your Docusign Account
   	Docusign Client Id (API ID): <YOUR Docusign API ID>
   	Docusign User Id: <YOUR Docusign USER ID>
   	Target Account:FALSE
   	Docusign Auth Server URL: account.docusign.com
   	Docusign Private Key
      		Open your Docusign Private API with your favourite text editor, copy and paste its content in the Docusign Private Key box.
    	Note
    	Docusign Auth Server URL can be set to account-d.docusign.com for testing purposes.

**Using Alfresco Docusign Add On**
Please see below link for using the Alfresco Docusign Add On from Alfresco Share or Alfresco Content App
https://www.seedim.com.au/alfresco-addon-documentation/#getting-started

