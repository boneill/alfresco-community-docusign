package au.com.seedim.docusign.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.apache.log4j.Logger;
import org.json.simple.parser.JSONParser;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import au.com.seedim.docusign.service.DocusignService;
import au.com.seedim.docusign.service.Recipient;
import au.com.seedim.utils.webscript.WebscriptUtil;

public class SendForSignatureMultiUserWebscript extends DeclarativeWebScript{
	
	static final Logger logger = Logger.getLogger(SendForSignatureMultiUserWebscript.class);
		
	private DocusignService docusignService;
	private PersonService personService;
	private NodeService nodeService;

	@Override
  protected Map<String, Object> executeImpl(WebScriptRequest request, Status status, Cache cache) {
	  
		if(logger.isDebugEnabled()){logger.debug("Entered SendForSignatureMultiUserWebscript execute method");}
		Map<String, Object> model = new HashMap<String, Object>();
		
		try
	      {
			  
	  		// get the values from the body and parse them into a json object
	  		String jsonString  = request.getContent().getContent();
	        if(logger.isDebugEnabled()){logger.debug("ClientFolderService: json params" + jsonString);}
	        
	        JSONParser jp = new JSONParser();
	        Object jsonData = jp.parse(jsonString);

	        // Iterate through the JSON object and verify mandatory parameters are included in json.
	        int expireDays = WebscriptUtil.processIntegerParameter(jsonData,"expireDays", false);
	        int expireWarn = WebscriptUtil.processIntegerParameter(jsonData,"expireWarn", false);
	        int reminderDelay = WebscriptUtil.processIntegerParameter(jsonData,"remindDays", false);
	        int reminderFrequency = WebscriptUtil.processIntegerParameter(jsonData,"remindFrequency", false);
	        String title = WebscriptUtil.processParameter(jsonData,"title",true);
	        String message = WebscriptUtil.processParameter(jsonData,"message",false);
	        
	        
	        String documentId = WebscriptUtil.processParameter(jsonData,"documentId",true);
			NodeRef documentRef = new NodeRef("workspace","SpacesStore",documentId);
			
			String targetFolderId = WebscriptUtil.processParameter(jsonData,"targetFolderId", false);
			NodeRef targetFolderRef = null;
			if(targetFolderId != null && !"".equalsIgnoreCase(targetFolderId) && !"null".equalsIgnoreCase(targetFolderId))
				targetFolderRef = new NodeRef("workspace","SpacesStore", targetFolderId);
			else
				targetFolderRef = nodeService.getPrimaryParent(documentRef).getParentRef();
	
	        List<HashMap<String, Object>> recipientList = WebscriptUtil.processArrayOfObjectsParamter(jsonData, "recipients", true);
	        List<Recipient> recipients = new ArrayList<Recipient>(); 
	        logger.debug(recipientList);
	        
	        // validate each field in the list of recipients
	        int order = 1;
	        for(HashMap recipient: recipientList) {
	        	String email = (String)recipient.get("email");
	        	if(email == null || email.length() < 1) {
	        		throw new IllegalArgumentException("email is mandatory for a recipient");
	        	}
	        	
	        	String name = (String)recipient.get("name");
	        	if(name == null || name.length() < 1) {
	        		throw new IllegalArgumentException("name is mandatory for a recipient");
	        	}
	        	

	        	String action = (String)recipient.get("action");
	        	if(action == null || action.length() < 1) {
	        		throw new IllegalArgumentException("action is mandatory for a recipient");
	        	}
	        	
	        	recipients.add(new Recipient(order, name, email, action));
	        	order++;
	        }
	    
	        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
				
			if(logger.isDebugEnabled()){logger.debug("current User: " + userName);}
			NodeRef personRef = personService.getPerson(userName);
			PersonInfo personInfo = personService.getPerson(personRef);
			userName = personInfo.getFirstName() + " " + personInfo.getLastName();
			
			//title = title + ".  Requested by " + userName;
		    
	    	String requestStatus = docusignService.sendEnvelope(recipients, userName, title, message,  
					expireDays, expireWarn, reminderDelay, reminderFrequency,
					documentRef, targetFolderRef,null //TODO adjust supplementalDocRefs
					);
//	
	        
//	        String ccName = null;
//	        String documentId = null;
//	        String targetFolderId = null;
//
//	        int expireDays = 0;
//	        int expireWarn = 0;
//	        int reminderDelay = 0;
//	        int reminderFrequency = 0;
//	        
//	        //WebscriptUtil.allowedParameterValue("template", template, TemplateAllowedValues);
//	        
//	        signerEmail = WebscriptUtil.processParameter(request,"signerEmail",signerEmail);
//			signerName = WebscriptUtil.processParameter(request,"signerName",signerName);
//			
//			ccEmail = WebscriptUtil.getOptionalParameter(request,"ccEmail",ccEmail);
//			ccName = WebscriptUtil.getOptionalParameter(request,"ccName",ccName);
//			
//			
//			expireDays = WebscriptUtil.getOptionalIntegerParameter(request,"expireDays");
//			expireWarn = WebscriptUtil.getOptionalIntegerParameter(request,"expireWarn");
//			reminderDelay = WebscriptUtil.getOptionalIntegerParameter(request,"reminderDelay");
//			reminderFrequency = WebscriptUtil.getOptionalIntegerParameter(request,"reminderFrequency");
//			
//			String userName = AuthenticationUtil.getFullyAuthenticatedUser();
//			if(ccEmail == null) {
//				
//				
//				if(logger.isDebugEnabled()){logger.debug("current User: " + userName);}
//				NodeRef personRef = personService.getPerson(userName);
//				
//				ccEmail = (String)nodeService.getProperty(personRef, ContentModel.PROP_EMAIL);
//				PersonInfo personInfo = personService.getPerson(personRef);
//				ccName = personInfo.getFirstName() + " " + personInfo.getLastName();
//			}
//			
//			
//			documentId = WebscriptUtil.processParameter(request,"documentId",documentId);
//			targetFolderId = WebscriptUtil.processParameter(request,"targetFolderId", targetFolderId);
//			
//			NodeRef documentRef = new NodeRef("workspace","SpacesStore",documentId);
//			
//			NodeRef targetFolderRef = null;
//			if(targetFolderId != null && !"".equalsIgnoreCase(targetFolderId) && !"null".equalsIgnoreCase(targetFolderId))
//				targetFolderRef = new NodeRef("workspace","SpacesStore", targetFolderId);
//			else
//				targetFolderRef = nodeService.getPrimaryParent(documentRef).getParentRef();
//	
//			String requestStatus = docusignService.sendEnvelope(signerEmail, signerName, ccEmail, ccName, userName,  
//					expireDays, expireWarn, reminderDelay, reminderFrequency,
//					documentRef, targetFolderRef 
//					);
//			
//			model.put("id", documentRef.getId());
//			model.put("status", requestStatus);
//			
//			status.setCode(Status.STATUS_OK);
//			
//			if(DocusignModel.DOCUMENT_STATUS_FAILED.equalsIgnoreCase(requestStatus))
//				status.setCode(Status.STATUS_BAD_REQUEST);
// 
////  	}catch(FileNotFoundException fnfe){
////      
////      status.setCode(Status.STATUS_NOT_FOUND, fnfe.getMessage());
//	    }catch(FileNotFoundException fnfe) {
//	      	    	
//	      	status.setCode(Status.STATUS_NOT_FOUND); 
//	          status.setMessage(fnfe.getMessage());
//	          status.setException(fnfe);
//	          status.setRedirect(true);
//	          	
	    }catch(IllegalArgumentException paramex) {
	      	
	  		status.setCode(Status.STATUS_BAD_REQUEST); 
	  		status.setMessage(paramex.getMessage());
	  		status.setException(paramex);
	  		status.setRedirect(true);
	  		
	      }catch(Exception e){
	        
	      	status.setCode(Status.STATUS_INTERNAL_SERVER_ERROR); 
	  		status.setMessage("Error creating client " + e.getMessage());
	  		status.setException(e);
	  		status.setRedirect(true);
	  		
	  		throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, e.getMessage(), e);
	  		
	  		//throw new WebScriptException(Status.STATUS_NOT_FOUND, "Error Setting permission: " + fne.getMessage(), fne );
	        

	
	      }finally {
	    	  if(logger.isDebugEnabled()){logger.debug("Entered SendForSignatureMultiUserWebscript execute method exited");}
		}
	  		
	  	
		if(logger.isDebugEnabled()){logger.debug("Exited SendForSignatureWebscript execute method");}
		
		return model;
		
	}
	
	public DocusignService getDocusignService() {
		return docusignService;
	}



	public void setDocusignService(DocusignService docusignService) {
		this.docusignService = docusignService;
	}

	public PersonService getPersonService() {
		return personService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
}
