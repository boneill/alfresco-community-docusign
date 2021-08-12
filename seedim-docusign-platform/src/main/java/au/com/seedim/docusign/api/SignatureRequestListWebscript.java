package au.com.seedim.docusign.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import au.com.seedim.docusign.model.DocusignModel;
import au.com.seedim.docusign.service.DocusignService;
import au.com.seedim.docusign.service.Recipient;
import au.com.seedim.utils.webscript.WebscriptUtil;

public class SignatureRequestListWebscript extends DeclarativeWebScript{
	
	static final Logger logger = Logger.getLogger(SignatureRequestListWebscript.class);
	
	
	
	private DocusignService docusignService;
	private PersonService personService;
	private NodeService nodeService;
	private PermissionService permissionService;


	@Override
  protected Map<String, Object> executeImpl(WebScriptRequest request, Status status, Cache cache) {
	  
		if(logger.isDebugEnabled()){logger.debug("Entered SendForSignatureWebscript execute method");}
		Map<String, Object> model = new HashMap<String, Object>();
		
		try
    	{
			String requestStatus = null;
	        String sentBy = null;
	        int blockSize = 100;
	        int blockNumber = 1;
	        String blockSizeStr = null;
	        String blockNumberStr = null;
	        
							
			
	      //WebscriptUtil.allowedParameterValue("template", template, TemplateAllowedValues);
	        
	        requestStatus = WebscriptUtil.processParameter(request,"status",requestStatus);
			sentBy = WebscriptUtil.getOptionalParameter(request,"sentBy",sentBy);
			
			blockSizeStr = WebscriptUtil.getOptionalParameter(request, "blockSize", blockSizeStr);
			blockNumberStr = WebscriptUtil.getOptionalParameter(request, "blockNumber", blockNumberStr);
			
			try {
			      	if(blockSizeStr != null) {
			      		blockSize = Integer.parseInt(blockSizeStr);
			      	}
			      	
			      	if(blockNumberStr != null) {
			      		blockNumber = Integer.parseInt(blockNumberStr);
			      	}
			    } catch (NumberFormatException e) {
			    	
			    	throw new WebScriptException(400,
			                "Invalid number format for blockSize or blockNumber" + e.getMessage());
			    }
			
			logger.debug("parameters passed: status = " +requestStatus + " sentBy = " + sentBy + " blockSize/blockNumber" + blockSize + "/" + blockNumber);
			String [] statusArray = new String [] {requestStatus};
			List<String> statusList = Arrays.asList(statusArray);
			
			List<NodeRef> signatureRequestDocuments= this.docusignService.getSignatureRequests(statusList, sentBy, blockSize, blockNumber);
			
			List entries = this.parseResults(signatureRequestDocuments);
			
			
			logger.debug("entries = " + entries);
					
			model.put("totalFound", signatureRequestDocuments.size());
			model.put("blockSize", blockSize);
			model.put("blockNumber", blockNumber);
			model.put("entries", entries);
			
			
			status.setCode(Status.STATUS_OK);
			
			
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
		if(logger.isDebugEnabled()){logger.debug("Exited SendForSignatureWebscript execute method: entities found " + model.get("entries").toString());}
		
		return model;
		
	}
	
	/** 
	 * Parse the results into a set of nodoes for display
	 * @param nodeRefList
	 * @return
	 */
	private List parseResults(List<NodeRef> nodeRefList) {
		// TODO Auto-generated method stub
    	if(logger.isDebugEnabled())logger.debug("parseResults entered");
    	
    	List<Object> entries = new ArrayList<Object>();
    	
    	try {
    		if(nodeRefList != null) {
    			
    			if(logger.isDebugEnabled())logger.debug("parseResults: document list size " + nodeRefList.size() );
    			
				for (NodeRef nodeRef : nodeRefList) {
					
		            if (logger.isDebugEnabled()) {logger.debug("parseResults: processing nodeRef: " + nodeRef);}
		            Map<String, Object> node = new HashMap<String, Object>();
		            Map<QName,Serializable> props = nodeService.getProperties(nodeRef);
		            
		            node.put("nodeId", nodeRef.getId());
		            node.put("nodeRef", nodeRef.toString());           
		            node.put("cm_name", parseStringValue(props.get(ContentModel.PROP_NAME)));
		            node.put("docusign_documentType", parseStringValue(props.get(DocusignModel.PROP_DOCUMENT_TYPE)));
		            if (!"".matches(parseDateValue(props.get(DocusignModel.PROP_COMPLETED_DATE)))){
		            	node.put("docusign_completedDate", parseDateValue(props.get(DocusignModel.PROP_COMPLETED_DATE)));
		            }
		            node.put("docusign_signedDocumentId", parseStringValue(props.get(DocusignModel.PROP_SIGNED_DOCUMENT_ID)));
		            node.put("docusign_sentBy", parseStringValue(props.get(DocusignModel.PROP_SENT_BY)));
		            node.put("docusign_sentDate", parseDateValue(props.get(DocusignModel.PROP_SENT_DATE)));
		            node.put("docusign_status", parseStringValue(props.get(DocusignModel.PROP_STATUS)));
		            node.put("docusign_envelopeId", parseStringValue(props.get(DocusignModel.PROP_ENVELOPE_ID)));
		            //node.put("docusign_signerEmail", parseStringValue(props.get(DocusignModel.PROP_SIGNER_EMAIL)));
		            //node.put("docusign_signerName", parseStringValue(props.get(DocusignModel.PROP_SIGNER_NAME)));
		            //node.put("docusign_ccUserId", parseStringValue(props.get(DocusignModel.PROP_CC_USER_ID)));
		            
		            StringBuffer buff = new StringBuffer("");
		            List<String> recipientList = (ArrayList<String>) props.get(DocusignModel.PROP_RECIPIENT);
		            
		            
		            
		            int recipientNum = 0;
		            if(recipientList != null) {
		            
		            	if (logger.isDebugEnabled()) {logger.debug("parseResults: recipientList: " + recipientList.toString());}
		            	
		            	for(String recipientStr: recipientList) {
		            		
		            		if (logger.isDebugEnabled()) {logger.debug("parseResults: recipientStr: " + recipientStr);}
		            		Recipient recipient = new Recipient(recipientStr);
		            		if (logger.isDebugEnabled()) {logger.debug("parseResults: recipient: " + recipient.toString());}
		            		if(recipientNum > 0) {
		            			buff.append(", ");
		            		}
		            		buff.append(recipient.parseValue());
		            		recipientNum++;
		            	}
		            }
		            
		            node.put("docusign_recipients", parseStringValue(buff.toString()));
		            
		            Path nodePath = nodeService.getPath(nodeRef);
		            String nodeDisplayPath = nodePath.toDisplayPath(nodeService, permissionService);
		            if(logger.isDebugEnabled()){logger.debug("path " + nodeDisplayPath);}
		            
		            node.put("displayPath", nodeDisplayPath);
		            node.put("path", getNodePath(nodeDisplayPath));
		            node.put("type","document");
		            node.put("isFile", true);
		            
		            String url = this.getNodeUrl(nodeRef,nodeDisplayPath);
		            if(logger.isDebugEnabled()){logger.debug("url " + url);}
		            node.put("url", url);
		            if(props.get(DocusignModel.PROP_SIGNED_DOCUMENT_ID) != null) {
		            	NodeRef signedDocumentNodeRef = new NodeRef("workspace://SpacesStore/"+ props.get(DocusignModel.PROP_SIGNED_DOCUMENT_ID));
		            	
		            	 Path signedDocNodePath = nodeService.getPath(signedDocumentNodeRef);
				         String signedDocNodeDisplayPath = signedDocNodePath.toDisplayPath(nodeService, permissionService);
				         String signedDocUrl = this.getNodeUrl(signedDocumentNodeRef,signedDocNodeDisplayPath);
				         node.put("signedDocumentUrl", signedDocUrl);
				         if(logger.isDebugEnabled()){logger.debug("signedDocumentUrl " + url);}
		            }
		            //if(logger.isDebugEnabled())logger.debug("nodeUrl: " +  displayUrlStr);
		            //nodes.put("isFile", isFile);
		            //items.put(nodeRef.toString(), nodes);
		           
		            
		            
		            entries.add(node);
		           
		          }
    		}
    	}finally {
    		if(logger.isDebugEnabled())logger.debug("parseResults exited" );
    	}
    	
    	return entries;
	}
	
	private String getNodePath(String displayPath) {
		  
		  String displayPathStr = displayPath;
		  if(displayPath.indexOf("/documentLibrary/") > 0) {
			  String[] splitDisplayPath = displayPath.split("/documentLibrary/");
			  displayPathStr = splitDisplayPath[1];
		  }
		  
		  return displayPathStr;
	  }
	
	private String getNodeUrl(NodeRef nodeRef, String displayPath) {
		  
		  String displayUrlStr = "";
		  //if(logger.isDebugEnabled())logger.debug("displayPath: " +  displayPath);
		  if(displayPath.indexOf("/documentLibrary") > 0) {
			  String[] splitDisplayPath = displayPath.split("/");
			  //if(logger.isDebugEnabled())logger.debug("site shortname:" +  splitDisplayPath[3]);
			  //http://localhost:8180/share/page/document-details?nodeRef=workspace://SpacesStore/4a1db682-799a-4e12-9008-9158e5c397e0
			  //http://localhost:8180/share/page/context/mine/document-details?nodeRef=workspace://SpacesStore/e8b6acfe-375d-481a-99af-82e499384e2c
			  //http://localhost:8180/share/page/site/swsdp/document-details?nodeRef=workspace://SpacesStore/a7879b9a-d24b-45b6-b590-4b579b35c2c3	  		  
			  displayUrlStr = "site/" + splitDisplayPath[3] + "/document-details?nodeRef=" + nodeRef;
			  //if(logger.isDebugEnabled())logger.debug("url: " +  displayUrlStr);
		  }
		  
		  else if(displayPath.indexOf("/User Homes") > 0) {
			  displayUrlStr = "context/mine/document-details?nodeRef=" + nodeRef;
		  }
		  else if(displayPath.indexOf("/Shared") > 0) {
			  displayUrlStr = "context/shared/document-details?nodeRef=" + nodeRef;
		  }
		  else {
			  if(logger.isDebugEnabled())logger.debug("Not in Site or My Files or Shared");
			  displayUrlStr = "document-details?nodeRef=" + nodeRef;
		  }
		  
		  return displayUrlStr ;
	  }

	/**
	 * Parse a property to an IsoDate
	 * 
	 * @param date
	 * @return
	 */
	private String parseDateValue(Serializable date) {
		String ret = "";
		
		if(date != null) {
			Date theDate = (Date)date;
			ret = ISO8601DateFormat.format(theDate);
		}
		
		return ret;
	}
	
	/**
	 * Parse a String property
	 * 
	 * @param date
	 * @return
	 */
	private String parseStringValue(Serializable val) {
		String ret = "";
		
		if(val != null) {
			ret = "" + val;
		}
		
		return ret;
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
	
	public PermissionService getPermissionService() {
		return permissionService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
}
