package au.com.seedim.docusign.api;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import au.com.seedim.docusign.model.DocusignModel;
import au.com.seedim.docusign.service.DocusignService;
import au.com.seedim.utils.webscript.WebscriptUtil;

public class SendForSignatureWebscript extends DeclarativeWebScript{
	
	static final Logger logger = Logger.getLogger(SendForSignatureWebscript.class);
	
	
	
	private DocusignService docusignService;
	private PersonService personService;
	private NodeService nodeService;


	

	@Override
  protected Map<String, Object> executeImpl(WebScriptRequest request, Status status, Cache cache) {
	  
		if(logger.isDebugEnabled()){logger.debug("Entered SendForSignatureWebscript execute method");}
		Map<String, Object> model = new HashMap<String, Object>();
		
		try
    	{
			String signerEmail = null;
	        String signerName = null;
	        String ccEmail = null;
	        String ccName = null;
	        String documentId = null;
	        String targetFolderId = null;

	        int expireDays = 0;
	        int expireWarn = 0;
	        int reminderDelay = 0;
	        int reminderFrequency = 0;
	        
	        //WebscriptUtil.allowedParameterValue("template", template, TemplateAllowedValues);
	        
	        signerEmail = WebscriptUtil.processParameter(request,"signerEmail",signerEmail);
			signerName = WebscriptUtil.processParameter(request,"signerName",signerName);
			
			ccEmail = WebscriptUtil.getOptionalParameter(request,"ccEmail",ccEmail);
			ccName = WebscriptUtil.getOptionalParameter(request,"ccName",ccName);
			
			
			expireDays = WebscriptUtil.getOptionalIntegerParameter(request,"expireDays");
			expireWarn = WebscriptUtil.getOptionalIntegerParameter(request,"expireWarn");
			reminderDelay = WebscriptUtil.getOptionalIntegerParameter(request,"reminderDelay");
			reminderFrequency = WebscriptUtil.getOptionalIntegerParameter(request,"reminderFrequency");
			
			String userName = AuthenticationUtil.getFullyAuthenticatedUser();
			if(ccEmail == null) {
				
				
				if(logger.isDebugEnabled()){logger.debug("current User: " + userName);}
				NodeRef personRef = personService.getPerson(userName);
				
				ccEmail = (String)nodeService.getProperty(personRef, ContentModel.PROP_EMAIL);
				PersonInfo personInfo = personService.getPerson(personRef);
				ccName = personInfo.getFirstName() + " " + personInfo.getLastName();
			}
			
			
			documentId = WebscriptUtil.processParameter(request,"documentId",documentId);
			targetFolderId = WebscriptUtil.processParameter(request,"targetFolderId", targetFolderId);
			
			NodeRef documentRef = new NodeRef("workspace","SpacesStore",documentId);
			
			NodeRef targetFolderRef = null;
			if(targetFolderId != null && !"".equalsIgnoreCase(targetFolderId) && !"null".equalsIgnoreCase(targetFolderId))
				targetFolderRef = new NodeRef("workspace","SpacesStore", targetFolderId);
			else
				targetFolderRef = nodeService.getPrimaryParent(documentRef).getParentRef();
	
			String requestStatus = docusignService.sendEnvelope(signerEmail, signerName, ccEmail, ccName, userName,  
					expireDays, expireWarn, reminderDelay, reminderFrequency,
					documentRef, targetFolderRef 
					);
			
			model.put("id", documentRef.getId());
			model.put("status", requestStatus);
			
			status.setCode(Status.STATUS_OK);
			
			if(DocusignModel.DOCUMENT_STATUS_FAILED.equalsIgnoreCase(requestStatus))
				status.setCode(Status.STATUS_BAD_REQUEST);
 
//  	}catch(FileNotFoundException fnfe){
//      
//      status.setCode(Status.STATUS_NOT_FOUND, fnfe.getMessage());
  	}
  	catch(WebScriptException wse){
      
      status.setCode(wse.getStatus(), wse.getMessage());
  	}
	catch (Exception ex) {
		throw new WebScriptException("Error in SendForSignatureWebscript execute method: " + ex.getMessage());
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
