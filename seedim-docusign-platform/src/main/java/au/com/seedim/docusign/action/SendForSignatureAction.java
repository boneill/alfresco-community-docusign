package au.com.seedim.docusign.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.extensions.webscripts.WebScriptException;

import au.com.seedim.docusign.service.DocusignService;
import au.com.seedim.docusign.service.Recipient;

public class SendForSignatureAction extends ActionExecuterAbstractBase {
	
	private static final Logger logger = Logger.getLogger(SendForSignatureAction.class);
	
	
	private DocusignService docusignService;
	private NodeService nodeService;
	private PersonService personService;
	
	public static final String PARAM_DOCUSIGN_MESSAGE = "message";
	public static final String PARAM_DOCUSIGN_TITLE = "title";
	public static final String PARAM_DOCUSIGN_RECIPIENT_NAME1 = "name1";
	public static final String PARAM_DOCUSIGN_RECIPIENT_EMAIL1 = "email1";
	public static final String PARAM_DOCUSIGN_RECIPIENT_ACTION1 = "action1";
	
	public static final String PARAM_DOCUSIGN_RECIPIENT_NAME2 = "name2";
	public static final String PARAM_DOCUSIGN_RECIPIENT_EMAIL2 = "email2";
	public static final String PARAM_DOCUSIGN_RECIPIENT_ACTION2 = "action2";
	
	public static final String PARAM_DOCUSIGN_RECIPIENT_NAME3 = "name3";
	public static final String PARAM_DOCUSIGN_RECIPIENT_EMAIL3 = "email3";
	public static final String PARAM_DOCUSIGN_RECIPIENT_ACTION3 = "action3";
	
	
	public static final String PARAM_DOCUSIGN_EXPIRY_DAYS = "expireDays";
	public static final String PARAM_DOCUSIGN_EXPIRY_WARN = "expireWarn";
	public static final String PARAM_DOCUSIGN_REMINDER_DELAY = "reminderDelay";
	public static final String PARAM_DOCUSIGN_REMINDER_FREQUENCY = "reminderFrequency";
	
	public static final String PARAM_DOCUSIGN_SIGNED_DOCUMENT_TARGET_ASSOC = "signedDocumentTargetAssoc";
	public static final String PARAM_DOCUSIGN_SUPPLEMENTAL_DOCUMENT_ASSOC = "supplementalDocumentAssoc";

	@SuppressWarnings("unchecked")
	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		if (logger.isDebugEnabled()) logger.debug("Entered SendForSignatureAction executeImpl method");
		
		try {
			
			String title = "Please Sign the attached document";
			String message = "";
	        String email1 = null;
	        String name1 = null;
	        String action1 = "sign";
	        String email2 = null;
	        String name2 = null;
	        String action2 = "sign";
	        String email3 = null;
	        String name3 = null;
	        String action3 = "sign";

	        //String documentId = null;
	        String targetFolderId = null;
	        
	        int expireDays = 14;
	        int expireWarn = 2;
	        int reminderDelay = 2;
	        int reminderFrequency = 2;	
	        
	        
	        title = (String) action.getParameterValue(PARAM_DOCUSIGN_TITLE);
	        
	        if(StringUtils.isBlank(title)) {
	        	title = "Please Sign the attached document";
	        }        
	        
	        message = (String) action.getParameterValue(PARAM_DOCUSIGN_MESSAGE);
	        email1 =  (String) action.getParameterValue(PARAM_DOCUSIGN_RECIPIENT_EMAIL1); 
	        name1 =  (String) action.getParameterValue(PARAM_DOCUSIGN_RECIPIENT_NAME1); 
	        action1 = (String) action.getParameterValue(PARAM_DOCUSIGN_RECIPIENT_ACTION1);
	        
	        email2 =  (String) action.getParameterValue(PARAM_DOCUSIGN_RECIPIENT_EMAIL2); 
	        name2 =  (String) action.getParameterValue(PARAM_DOCUSIGN_RECIPIENT_NAME2);
	        action2 = (String) action.getParameterValue(PARAM_DOCUSIGN_RECIPIENT_ACTION2);
	        
	        email3 =  (String) action.getParameterValue(PARAM_DOCUSIGN_RECIPIENT_EMAIL3); 
	        name3 =  (String) action.getParameterValue(PARAM_DOCUSIGN_RECIPIENT_NAME3);
	        action3 = (String) action.getParameterValue(PARAM_DOCUSIGN_RECIPIENT_ACTION3);
	        
	        validateAction(action1,action2,action3);

	        if(action.getParameterValue(PARAM_DOCUSIGN_EXPIRY_DAYS) != null) {
	        	expireDays = (int) action.getParameterValue(PARAM_DOCUSIGN_EXPIRY_DAYS);
	        }
	        if(action.getParameterValue(PARAM_DOCUSIGN_EXPIRY_WARN) != null) {
	        	expireWarn = (int) action.getParameterValue(PARAM_DOCUSIGN_EXPIRY_WARN);
	        }
	        if(action.getParameterValue(PARAM_DOCUSIGN_REMINDER_DELAY) != null) {
	        	reminderDelay = (int) action.getParameterValue(PARAM_DOCUSIGN_REMINDER_DELAY);
	        }
	        if(action.getParameterValue(PARAM_DOCUSIGN_REMINDER_FREQUENCY) != null) {
	        	reminderFrequency = (int) action.getParameterValue(PARAM_DOCUSIGN_REMINDER_FREQUENCY);
	        }        
	        
	        targetFolderId = (String) action.getParameterValue(PARAM_DOCUSIGN_SIGNED_DOCUMENT_TARGET_ASSOC); 
	        
	        List<String> supplementalList = new ArrayList<String>();
	        List<NodeRef> supplementalDocRefs = new ArrayList<NodeRef>();
	        if(action.getParameterValue(PARAM_DOCUSIGN_SUPPLEMENTAL_DOCUMENT_ASSOC) != null) {
	        	supplementalList = (List<String>) action.getParameterValue(PARAM_DOCUSIGN_SUPPLEMENTAL_DOCUMENT_ASSOC);
	        	if(!supplementalList.isEmpty()) {
	        		if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl supplementalList: " + supplementalList);
	        		for (String supDoc : supplementalList) {
	        			NodeRef supDocRef = new NodeRef(supDoc);
	        			supplementalDocRefs.add(supDocRef);
	        		}
	        	}else {
	        		supplementalDocRefs = null;
	        	}
	        }else {
	        	supplementalDocRefs = null;
	        }
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl targetFolderId : " + targetFolderId);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl title : " + title);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl message : " + message);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl name1: " + name1);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl email1: " + email1);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl action1: " + action1);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl name2: " + name2);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl email2: " + email2);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl action2: " + action2);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl name3: " + name3);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl email3: " + email3);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl action3: " + action3);

	        
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl expireDays: " + expireDays);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl expireWarn: " + expireWarn);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl reminderDelay: " + reminderDelay);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl reminderFrequency: " + reminderFrequency);
	        
	        
	        
	        
	        validateExpiryDetails(expireDays, expireWarn, reminderDelay,reminderFrequency);
	        
	        //if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl targetFolderId : " + action.getParameterValues());
	        
	        /**Map<String,Serializable> paramsValues = action.getParameterValues();
	        for (String paramKey : paramsValues.keySet()) {
	        	if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction executeImpl paramKey : " + paramKey +" value " + paramsValues.get(paramKey));
	        }**/
	        
	        List<Recipient> recipients = new ArrayList<Recipient>();
	        recipients.add(new Recipient(1,name1, email1, action1));
	        
	        if((name2 != null && StringUtils.isNotBlank(name2)) && (email2 != null && StringUtils.isNotBlank(email2)) && (action2 != null && StringUtils.isNotBlank(action2))) {
	        	recipients.add(new Recipient(2,name2, email2, action2));
	        }
	        if((name3 != null && StringUtils.isNotBlank(name3)) && (email3 != null && StringUtils.isNotBlank(email3)) && (action3 != null && StringUtils.isNotBlank(action3))) {
	        	recipients.add(new Recipient(2, name3, email3, action3));
	        }
	        
	        
	        
	        
	        NodeRef targetFolderRef = null;
	        if(targetFolderId != null && !targetFolderId.isEmpty()) {
				targetFolderRef = new NodeRef(targetFolderId);
	        }else {
				targetFolderRef = nodeService.getPrimaryParent(actionedUponNodeRef).getParentRef();
	        }
	        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
	        if(logger.isDebugEnabled()){logger.debug("current User: " + userName);}
			NodeRef personRef = personService.getPerson(userName);
			PersonInfo personInfo = personService.getPerson(personRef);
			userName = personInfo.getFirstName() + " " + personInfo.getLastName();

			//message = message + ".  Requested by " + userName;
			        
	    	String requestStatus = docusignService.sendEnvelope(recipients, userName, title, message,  
					expireDays, expireWarn, reminderDelay, reminderFrequency,
					actionedUponNodeRef, targetFolderRef, supplementalDocRefs
					);
	        if (logger.isDebugEnabled()) logger.debug("SendForSignatureAction requestStatus: " + requestStatus);
	        if (requestStatus.matches("failed")) {
	        	throw new Exception("Signture Request Failed!!!!");
	        }
	        
		}catch (AlfrescoRuntimeException are) {
			// TODO Auto-generated catch block
			are.printStackTrace();
			if(logger.isDebugEnabled()) {logger.debug("AlfrescoRuntimeException in SendForSignatureAction " + are.getMessage());}
			throw new WebScriptException("Signture Request Failed!! " + are.getMessage());
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			if(logger.isDebugEnabled()) {logger.debug("Exception in SendForSignatureAction " + e.getMessage());}
			throw new WebScriptException("Signture Request Failed!! " + e.getMessage());
		}
		
		
		if (logger.isDebugEnabled()) logger.debug("Exited SendForSignatureAction executeImpl method");
		
	}
	public boolean validateAction(String action1, String action2, String action3) {
		boolean isValid = true;
		if(action1 != null && action1.equalsIgnoreCase("cc")) {
			if(action2 != null && action2.equalsIgnoreCase("cc")) {
				if(action3 != null && action3.equalsIgnoreCase("cc")) {
					throw new WebScriptException("There should be at least one Signee");
				}
				if(StringUtils.isBlank(action2)) {
					throw new WebScriptException("There should be at least one Signee");
				}
			}
			if(StringUtils.isBlank(action3)) {
				throw new WebScriptException("There should be at least one Signee");
			}
			
		}
		
		return isValid;
	}
	
	public boolean validateExpiryDetails(int expireDays, int expireWarn, int reminderDelay,int reminderFrequency) {
		
		boolean isValid = true;
		
		if(expireDays < 0) {
			throw new WebScriptException("Days to Expire cannot be less than 0 days");
		}
		if(expireDays > 90) {
			throw new WebScriptException("Days to Expire cannot be greater than 90 days");
		}
		if(expireWarn < 0) {
			throw new WebScriptException("Expire Warning Days cannot be less than 0 days");
		}
		if(expireWarn > 21) {
			throw new WebScriptException("Expire Warning Days cannot be greater than 21 days");
		}
		if(expireWarn > expireDays) {
			throw new WebScriptException("Expire Warning Days cannot be greater than Days to Expire");
		}
		if(reminderDelay < 0) {
			throw new AlfrescoRuntimeException("Send Reminder After cannot be less than 0 days");
		}
		if(reminderDelay > 80) {
			throw new WebScriptException("Send Reminder After cannot be greater than 80 days");
		}
		if(reminderFrequency < 0) {
			throw new WebScriptException("Send Reminder Every cannot be less than 0 days");
		}
		if(reminderFrequency> 21) {
			throw new WebScriptException("Send Reminder Every cannot be greater than 21 days");
		}
		if(reminderDelay > expireDays) {
			throw new WebScriptException("Send Reminder After cannot be greater than Days to Expire");
		}
		
		return isValid;
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_TITLE, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_DOCUSIGN_TITLE), false));
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_MESSAGE, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_DOCUSIGN_MESSAGE), false));
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_RECIPIENT_NAME1, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_DOCUSIGN_RECIPIENT_NAME1), false));
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_RECIPIENT_EMAIL1, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_DOCUSIGN_RECIPIENT_EMAIL1), false));
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_RECIPIENT_ACTION1, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_DOCUSIGN_RECIPIENT_ACTION1), false));
		
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_RECIPIENT_NAME2, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_DOCUSIGN_RECIPIENT_NAME2), false));
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_RECIPIENT_EMAIL2, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_DOCUSIGN_RECIPIENT_EMAIL2), false));
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_RECIPIENT_ACTION2, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_DOCUSIGN_RECIPIENT_ACTION2), false));
		
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_RECIPIENT_NAME3, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_DOCUSIGN_RECIPIENT_NAME3), false));
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_RECIPIENT_EMAIL3, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_DOCUSIGN_RECIPIENT_EMAIL3), false));
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_RECIPIENT_ACTION3, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_DOCUSIGN_RECIPIENT_ACTION3), false));
		
		
		
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_EXPIRY_DAYS, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_DOCUSIGN_EXPIRY_DAYS), false));
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_EXPIRY_WARN, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_DOCUSIGN_EXPIRY_WARN), false));
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_REMINDER_DELAY, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_DOCUSIGN_REMINDER_DELAY), false));
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_REMINDER_FREQUENCY, DataTypeDefinition.INT, false, getParamDisplayLabel(PARAM_DOCUSIGN_REMINDER_FREQUENCY), false));
		
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_SIGNED_DOCUMENT_TARGET_ASSOC, DataTypeDefinition.ASSOC_REF, false, getParamDisplayLabel(PARAM_DOCUSIGN_SIGNED_DOCUMENT_TARGET_ASSOC), false));
		paramList.add(new ParameterDefinitionImpl(PARAM_DOCUSIGN_SUPPLEMENTAL_DOCUMENT_ASSOC, DataTypeDefinition.ASSOC_REF, false, getParamDisplayLabel(PARAM_DOCUSIGN_SUPPLEMENTAL_DOCUMENT_ASSOC), true));

	}

	public DocusignService getDocusignService() {
		return docusignService;
	}

	public void setDocusignService(DocusignService docusignService) {
		this.docusignService = docusignService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public PersonService getPersonService() {
		return personService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

}
