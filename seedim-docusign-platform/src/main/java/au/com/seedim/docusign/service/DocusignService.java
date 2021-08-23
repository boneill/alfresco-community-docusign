package au.com.seedim.docusign.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;

//import static com.docusign.example.jwt.DSHelper.printPrettyJSON;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.nodelocator.CompanyHomeNodeLocator;
import org.alfresco.repo.nodelocator.NodeLocatorService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition.SortType;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.client.auth.OAuth;
import com.docusign.esign.model.Envelope;
import com.docusign.esign.model.EnvelopeSummary;
import com.docusign.esign.model.EnvelopesInformation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import au.com.seedim.docusign.model.DocusignModel;
import au.com.seedim.docusign.utils.DocumentUtil;


public class DocusignService {

  static final Logger logger = Logger.getLogger(DocusignService.class);
  
  private SearchService searchService;
  protected NodeService nodeService;
  protected FileFolderService fileFolderService;
  private DictionaryService dictionaryService;
  private NamespaceService namespaceService;
  private SiteService siteService;
  private ContentService contentService;
  private CopyService copyService;
  private CheckOutCheckInService checkOutCheckInService;
  private VersionService versionService;
  private NodeLocatorService nodeLocatorService;
  private AuthenticationService authenticationService;
  private RetryingTransactionHelper retryingTransactionHelper;
  // locations fo the templates, these are set in the context file


private String clientId;
  private String apiUserId;
  private String targetAccount;
  private String authServer;
  private String privateKey;
  private String privateKeyPemFile;
  
  private NodeRef configRef = null;
  
  private static final ApiClient apiClient = new ApiClient();
  private static final long TOKEN_EXPIRATION_IN_SECONDS = 3600;
  private static final long TOKEN_REPLACEMENT_IN_MILLISECONDS = 10 * 60 * 1000;
  
  

  /**
   * 
   * @param signerEmail
   * @param signerName
   * @param ccEmail
   * @param ccName
   * @param userName
   * @param documentRef
   * @param targetFolderRef
   * @return: String status of request
   */
  public String sendEnvelope(List<Recipient> recipientList, String userName, String title, String message, int expireDays,
			int expireWarn, int reminderDelay, int reminderFrequency, NodeRef documentRef, NodeRef targetFolderRef, List<NodeRef>supplementalDocumentRefs) {
	
	  
	  if(logger.isDebugEnabled()){logger.debug("Entered sendEnvelope method with params:" +
			  "recipienList:" + recipientList +
			  "userName:" + userName  + 
			  "expireDays:" + expireDays  +
			  "expireWarn:" + expireWarn  +
			  "reminderDelay:" + reminderDelay  +
			  "reminderFrequency:" + reminderFrequency  +
		  	  "documentRef " + documentRef + 
		  	  "targetFolderRef" + targetFolderRef );}
	  
	  
	  String status =  DocusignModel.DOCUMENT_STATUS_SENT; 
	  
	  try {
          System.setProperty("https.protocols","TLSv1.2");
          logger.debug("\nSending an envelope. ");
          
          File docFile = DocumentUtil.stream2file(documentRef, contentService);
          String documentName = (String)nodeService.getProperty(documentRef, ContentModel.PROP_NAME);
          
          //this.privateKey = this.getPrivateKeyPemFile();
          
          getDocusignAPIConnectionDetails();
          
          //List <File>supplementalDocumentFiles = new ArrayList();
          Map<String, File> supplementalDocumentFiles= new HashMap<String, File>();
          if(supplementalDocumentRefs != null) {
	          for(NodeRef docRef: supplementalDocumentRefs ) {
	        	  File file = DocumentUtil.stream2file(docRef, contentService);
	        	  String fileName = (String)nodeService.getProperty(docRef, ContentModel.PROP_NAME);
	        	  //supplementalDocumentFiles.add(file);
	        	  supplementalDocumentFiles.put(fileName, file);
	          }
          }else {
        	  supplementalDocumentFiles = null;
          }
          
          EnvelopeSummary result = new SendEnvelope(apiClient, clientId, apiUserId, targetAccount, authServer, this.privateKey)
        		  .sendEnvelope(recipientList, userName, title, message, docFile, documentName, supplementalDocumentFiles,
        		    	expireDays, expireWarn, reminderDelay, reminderFrequency );
          
          logger.debug(
                  String.format("Envelope status: %s. Envelope ID: %s",
                  result.getStatus(),
                  result.getEnvelopeId()));

          
          // TODO - break up better
          
          StringBuffer signerList = new StringBuffer("");
          StringBuffer signerEmailList = new StringBuffer("");
          StringBuffer ccList = new StringBuffer("");
          
          //String[] recipients = new String[recipientList.size()];
          ArrayList<String> recipients = new ArrayList<String>(recipientList.size());
          int i = 0;
          for(Recipient recipient: recipientList) {
        	  
        	  //recipients[i] = recipient.getPropertyValue();
        	  //i++;
        	  
        	  recipients.add(recipient.getPropertyValue());
        	  
//        	  if("sign".equalsIgnoreCase(recipient.action)){
//        	  		if(signerList.length() > 0) {
//        	  			signerList.append(" | ");
//        	  			signerEmailList.append(" | ");
//        	  			
//        	  		}
//        		  	signerList.append(recipient.name);
//        		  	signerEmailList.append(recipient.email);
//        	  }else {
//        		  if(ccList.length() > 0) {
//      	  			ccList.append(" | ");
//      	  		}
//        		  ccList.append(recipient.name);
//        	  }
          }
          
          // add the docusign properties to the node
          logger.debug("Adding properties " + recipients.toString());
          Map<QName,Serializable> properties = new HashMap<QName, Serializable>();
          properties.put(DocusignModel.PROP_RECIPIENT, recipients);
          
//          properties.put(DocusignModel.PROP_SIGNER_EMAIL, signerEmailList.toString() );
//          properties.put(DocusignModel.PROP_SIGNER_NAME, signerList.toString() );
//          properties.put(DocusignModel.PROP_CC_USER_ID, ccList.toString() );
          
          properties.put(DocusignModel.PROP_ENVELOPE_ID, result.getEnvelopeId());
          properties.put(DocusignModel.PROP_STATUS, result.getStatus());
          
          properties.put(DocusignModel.PROP_DOCUMENT_TYPE, DocusignModel.DOCUMENT_TYPE_ORIGINAL);
          properties.put(DocusignModel.PROP_SENT_BY, authenticationService.getCurrentUserName());
          properties.put(DocusignModel.PROP_SENT_DATE, new Date());
          
          if(nodeService.hasAspect(documentRef, DocusignModel.ASPECT_DIGITAL_SIGNATURE)) {

              // add the association for the target folder
//        	  nodeService.getTargetAssocs(documentRef, DocusignModel.ASSOC_SIGNED_DOCUMENT_TARGET);
              //nodeService.setAssociations(documentRef, DocusignModel.ASSOC_SIGNED_DOCUMENT_TARGET, Arrays.asList(targetFolderRef));
              
        	  try {
        	  nodeService.createAssociation(documentRef, targetFolderRef, DocusignModel.ASSOC_SIGNED_DOCUMENT_TARGET);
        	  }catch(AssociationExistsException ae) {
        		  // do nothing as association already exists
        	  }
              
        	  nodeService.addProperties(documentRef, properties);
          }else {

        	  nodeService.addAspect(documentRef,DocusignModel.ASPECT_DIGITAL_SIGNATURE, properties);
        	  // add the association for the target folder
              nodeService.createAssociation(documentRef, targetFolderRef, DocusignModel.ASSOC_SIGNED_DOCUMENT_TARGET);
              
        	  
          }
          
//          
//          logger.debug("\nListing envelopes in the account whose status changed in the last 30 days...");
//          EnvelopesInformation envelopesList = new ListEnvelopes(apiClient).list();
//
//          List<Envelope> envelopes = envelopesList.getEnvelopes();
//          if(envelopesList != null && envelopes.size() > 2) {
//              System.out.println(
//                      String.format("Results for %d envelopes were returned. Showing the first two:",
//                              envelopesList.getEnvelopes().size()));
//                              envelopesList.setEnvelopes(Arrays.asList(envelopes.get(0), envelopes.get(1)));
//          }
//
//          printPrettyJSON(envelopesList);
          
          


      } catch (IOException e) {
          e.printStackTrace();
          status =  DocusignModel.DOCUMENT_STATUS_FAILED;
      } catch(Exception e) {
    	  e.printStackTrace();
    	  status =  DocusignModel.DOCUMENT_STATUS_FAILED;
      }
	  
	  return status;
	  
		
	}

  
  
  
  /**
   * 
   * @param signerEmail
   * @param signerName
   * @param ccEmail
   * @param ccName
   * @param userName
   * @param documentRef
   * @param targetFolderRef
   * @return: String status of request
   */
  public String sendEnvelope(String signerEmail, String signerName, String ccEmail, String ccName, String userName, 
		  int expireDays, int expireWarn, int reminderDelay, int reminderFrequency,
			NodeRef documentRef, NodeRef targetFolderRef) {
		// TODO Auto-generated method stub
	  
	  if(logger.isDebugEnabled()){logger.debug("Entered sendEnvelope method with params:" +
			  "signerEmail:" + signerEmail +
			  "signerName:" + signerName  + 
		  	  "ccEmail:" + ccEmail + 
		  	  "ccName:" + ccName + 
		  	  "userName:" + userName + 
		  	  "documentRef " + documentRef + 
		  	  "targetFolderRef" + targetFolderRef );}
	  
	  
	  String status =  DocusignModel.DOCUMENT_STATUS_SENT; 
	  
	  try {
          System.setProperty("https.protocols","TLSv1.2");
          logger.debug("\nSending an envelope. ");
          
          File docFile = DocumentUtil.stream2file(documentRef, contentService);
          String documentName = (String)nodeService.getProperty(documentRef, ContentModel.PROP_NAME);
          
          //this.privateKey = this.getPrivateKeyPemFile();
          
          getDocusignAPIConnectionDetails();
          
          EnvelopeSummary result = new SendEnvelope(apiClient, clientId, apiUserId, targetAccount, authServer, this.privateKey)
        		  .sendEnvelope(signerEmail, signerName, ccEmail, ccName, docFile, documentName,
        				  expireDays, expireWarn, reminderDelay, reminderFrequency);
          
          logger.debug(
                  String.format("Envelope status: %s. Envelope ID: %s",
                  result.getStatus(),
                  result.getEnvelopeId()));

          
          // add the docusign properties to the node
          logger.debug("Adding properties");
          Map<QName,Serializable> properties = new HashMap<QName, Serializable>();
          properties.put(DocusignModel.PROP_SIGNER_EMAIL, signerEmail );
          properties.put(DocusignModel.PROP_SIGNER_NAME, signerName );
          properties.put(DocusignModel.PROP_CC_USER_ID, userName );
          
          properties.put(DocusignModel.PROP_ENVELOPE_ID, result.getEnvelopeId());
          properties.put(DocusignModel.PROP_STATUS, result.getStatus());
          
          properties.put(DocusignModel.PROP_DOCUMENT_TYPE, DocusignModel.DOCUMENT_TYPE_ORIGINAL);
          properties.put(DocusignModel.PROP_SENT_BY, authenticationService.getCurrentUserName());
          properties.put(DocusignModel.PROP_SENT_DATE, new Date());
          
          if(nodeService.hasAspect(documentRef, DocusignModel.ASPECT_DIGITAL_SIGNATURE)) {

              // add the association for the target folder
//        	  nodeService.getTargetAssocs(documentRef, DocusignModel.ASSOC_SIGNED_DOCUMENT_TARGET);
              //nodeService.setAssociations(documentRef, DocusignModel.ASSOC_SIGNED_DOCUMENT_TARGET, Arrays.asList(targetFolderRef));
              
        	  try {
        	  nodeService.createAssociation(documentRef, targetFolderRef, DocusignModel.ASSOC_SIGNED_DOCUMENT_TARGET);
        	  }catch(AssociationExistsException ae) {
        		  // do nothing as association already exists
        	  }
              
        	  nodeService.addProperties(documentRef, properties);
          }else {

        	  nodeService.addAspect(documentRef,DocusignModel.ASPECT_DIGITAL_SIGNATURE, properties);
        	  // add the association for the target folder
              nodeService.createAssociation(documentRef, targetFolderRef, DocusignModel.ASSOC_SIGNED_DOCUMENT_TARGET);
              
        	  
          }
          
//          
//          logger.debug("\nListing envelopes in the account whose status changed in the last 30 days...");
//          EnvelopesInformation envelopesList = new ListEnvelopes(apiClient).list();
//
//          List<Envelope> envelopes = envelopesList.getEnvelopes();
//          if(envelopesList != null && envelopes.size() > 2) {
//              System.out.println(
//                      String.format("Results for %d envelopes were returned. Showing the first two:",
//                              envelopesList.getEnvelopes().size()));
//                              envelopesList.setEnvelopes(Arrays.asList(envelopes.get(0), envelopes.get(1)));
//          }
//
//          printPrettyJSON(envelopesList);
          
          


      } catch (IOException e) {
          e.printStackTrace();
          status =  DocusignModel.DOCUMENT_STATUS_FAILED;
      } catch(Exception e) {
    	  e.printStackTrace();
    	  status =  DocusignModel.DOCUMENT_STATUS_FAILED;
      }
	  
	  return status;
	  
		
	}
  
  /**
   * get all the coneection details for connecting to docusign
 * @throws FileNotFoundException 
   */
  private void getDocusignAPIConnectionDetails() throws FileNotFoundException {
	// TODO Auto-generated method stub
	  NodeRef ddRef = null;
	  
	  
	  if(this.configRef == null) {
	  
		  NodeRef companyHomeNodeRef = nodeLocatorService.getNode(CompanyHomeNodeLocator.NAME, null, null);
		  if(companyHomeNodeRef != null) {
			  ddRef = this.nodeService.getChildByName(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, "Data Dictionary");
		  }
	  
		  if(ddRef != null) {
			  this.configRef = this.nodeService.getChildByName(ddRef, ContentModel.ASSOC_CONTAINS, "Docusign-Config");
		  }
	  
	  }
	  
	  if(this.configRef == null) {
		  throw new FileNotFoundException("Could not retrieve docusing config details. " + 
				  	"Please ensure that the following folder has been updated with docusign properties " + 
				  	"/CompanyHome/Data Dictionary/Docusign-Config ");
	  }
	  
	  Map properties = this.nodeService.getProperties(this.configRef);
	  this.clientId = (String)properties.get(DocusignModel.PROP_DOCUSIGN_CONFIG_CLIENT_ID);
	  this.apiUserId = (String)properties.get(DocusignModel.PROP_DOCUSIGN_CONFIG_USER_ID);
	  this.authServer = (String)properties.get(DocusignModel.PROP_DOCUSIGN_CONFIG_AUTH_SERVER_URL);
	  this.targetAccount = (String)properties.get(DocusignModel.PROP_DOCUSIGN_CONFIG_TARGET_ACCOUNT);
	  this.privateKey = (String)properties.get(DocusignModel.PROP_DOCUSIGN_CONFIG_PRIVATE_KEY);
	  
	  logger.debug("config values returned:" + "clientId: " + this.clientId + " \n" + 
			  "userId: " + this.apiUserId + " \n" +
			  "authServer: " + this.authServer + " \n" +
			  "targetAccount: " + this.targetAccount + " \n");
	  
	  if(this.clientId == null || this.apiUserId == null || this.authServer == null || this.targetAccount == null || this.privateKey == null) {
		  
		  throw new FileNotFoundException("Could not retrieve docusing config details. " + 
				  	"Please ensure that the following folder has been updated with docusign properties " + 
				  	"/CompanyHome/Data Dictionary/Docusign-Config ");
	  }
	  
}

  
  public List<NodeRef> getSignatureRequests(List<String> statusList, String sentBy, int blockSize, int blockNumber) {
	  List<NodeRef> requestDocumentList = new ArrayList<NodeRef>();
	    
	    
	  	// quesry: ASPECT:'docusign:digitalSignature' AND docusign:status:'sent' AND docusign:sentBy:'admin'
	  
	  	if(logger.isDebugEnabled()) {logger.debug("getSignatureRequests entered. statusList = " + statusList + " sentBy = " + sentBy + " blockSize = " + blockSize + " blockNumber = " + blockNumber );}
	  
	  	// create status list as a grouping search structure eg	title:((big OR large) AND banana)
	  	StringBuffer statusGroup = new StringBuffer();
	  	
	  	int index = 0;
	  	for(String status: statusList) {
	  		if(index > 0) {
	  			statusGroup.append(" OR ");
	  		}
	  		statusGroup.append(status);
	  		
	  		index++;
	  	}
	  	
	  	statusGroup.insert(0,"(");
	  	statusGroup.append(")");
	  	
	  	
	    final StringBuffer query = new StringBuffer("ASPECT:\"docusign:digitalSignature\"");
	    query.append(" AND docusign:status:" + statusGroup.toString());
	    String signatureRequestType = "Original";
	    query.append(" AND docusign:documentType:" + signatureRequestType);
	    if(sentBy != null) {
	    	query.append(" AND @docusign:sentBy:\"" + sentBy + "\"" );
	    }
	    	
	    
	      ResultSet resultSet = null;
	      
	      
	    try{
	      if(logger.isDebugEnabled()) {logger.debug("getSignatureRequests: Find Digital Signature Requests search query " + query.toString());}
	      
	      int skipCount = 0;
	      if(blockNumber > 1) {
	    	  skipCount = (blockNumber - 1) * blockSize;
	      }
	      PagingRequest pagingRequest = new PagingRequest(skipCount, blockSize);
	      SortDefinition sortDefinition = new SortDefinition(SortType.FIELD, "docusign:sentDate", false);
	      
	      StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
	  
	      SearchParameters searchParameters = new SearchParameters();
	      searchParameters.addStore(storeRef);
	      searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
	      searchParameters.setQuery(query.toString());
	      searchParameters.addSort(sortDefinition);
	      searchParameters.setSkipCount(pagingRequest.getSkipCount());
	      searchParameters.setMaxItems(pagingRequest.getMaxItems());
//	      
	      resultSet = this.searchService.query(searchParameters);
	      
	      logger.debug(resultSet.getNumberFound());
	      
	      requestDocumentList = resultSet.getNodeRefs();
	    
	    } catch(Exception e) {
	      // 
	      e.printStackTrace();
	      if(logger.isDebugEnabled()) {logger.debug("Error in getSignatureRequests " + e.getMessage());}
	      throw e;
	      
	    }
	    
	    return requestDocumentList;
	}
  

/**
   * This method will view the status of a signature in Docusign and if complete, 
   * download the document from docusign and store it in the repository at the target destination.
   * 
   * @param actionedUponNodeRef
 * @throws Exception 
   */
  public void getSignedDocument(NodeRef nodeRef) throws Exception {
	
	  try {
	  
			 Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
			 String envelopeId = (String)properties.get(DocusignModel.PROP_ENVELOPE_ID);
			 
			 logger.debug("Processing request for document signature for " + nodeRef);
			 
			 // if the status is still sent then get the document from docusign using the Envelope ID
			 if(properties != null && "sent".equalsIgnoreCase((String)properties.get(DocusignModel.PROP_STATUS))) {
				 
				 
				 //if(apiClient!=null && apiClient.getAccessToken())
				 getDocusignAPIConnectionDetails();
				 SignedDocument signedDocument = new SignedDocument(apiClient, clientId, apiUserId, targetAccount, authServer, this.privateKey);
				
				 String status = signedDocument.getEnvelopeStatus(envelopeId);
				 if(DocusignModel.DOCUMENT_STATUS_VOID.equalsIgnoreCase(status)) {
					 this.nodeService.setProperty(nodeRef, DocusignModel.PROP_STATUS, DocusignModel.DOCUMENT_STATUS_VOID);
					 return;
				 }else if (DocusignModel.DOCUMENT_STATUS_SENT.equalsIgnoreCase(status)) {
					 logger.debug("Document not signed yet for doc " + nodeRef);
				 }
				 else if(DocusignModel.DOCUMENT_STATUS_COMPLETED.equalsIgnoreCase(status)) {
				 
					 
					 byte[] signedDocAsBytes  = null;
					 try {
					 signedDocAsBytes = signedDocument.getSignedDocument(envelopeId);
					 }
					 catch(Exception e) {
						 logger.debug(e.getMessage());
					 }
					 
					 if(signedDocAsBytes != null) {
					
						 Envelope envelope = signedDocument.getCompletedEnvelope(envelopeId);
						 String completionDate = envelope.getCompletedDateTime();
	
						 // get the assoc for the target document.  If no value set then the parent of the current node becomes the target the signed document will be stored in.
						 NodeRef targetNodeRef = null;
						 List<AssociationRef> assocList = this.nodeService.getTargetAssocs(nodeRef, DocusignModel.ASSOC_SIGNED_DOCUMENT_TARGET);
						 if(assocList != null) {
							 AssociationRef targetRef = assocList.get(0);
							 targetNodeRef = targetRef.getTargetRef();
						 }
						 
						 targetNodeRef = targetNodeRef!=null? targetNodeRef : nodeService.getPrimaryParent(nodeRef).getParentRef();
						 
						 
						 
						 
						 
						 // set docusign aspect values so that the signed document can be traced.
						 Map<QName, Serializable> signedProperties = new HashMap<QName, Serializable>(4);
						 String signedDocumentName = getSignedDocumentName((String)properties.get(ContentModel.PROP_NAME), targetNodeRef);
						 signedProperties.put(ContentModel.PROP_NAME, signedDocumentName);
						 signedProperties.put(DocusignModel.PROP_DOCUMENT_TYPE, DocusignModel.DOCUMENT_TYPE_SIGNED);
						 signedProperties.put(DocusignModel.PROP_STATUS, "completed");
					      
					     Date compDate = ISO8601DateFormat.parse(completionDate);
					     signedProperties.put(DocusignModel.PROP_COMPLETED_DATE, compDate);
						 
					     // set props that were set when document sent for signature.
					     signedProperties.put(DocusignModel.PROP_SIGNER_EMAIL, properties.get(DocusignModel.PROP_SIGNER_EMAIL));
					     signedProperties.put(DocusignModel.PROP_SIGNER_NAME, properties.get(DocusignModel.PROP_SIGNER_NAME));
					     signedProperties.put(DocusignModel.PROP_ENVELOPE_ID, properties.get(DocusignModel.PROP_ENVELOPE_ID));
					     signedProperties.put(DocusignModel.PROP_CC_USER_ID, properties.get(DocusignModel.PROP_CC_USER_ID));
						 
						 
						 
						 NodeRef signedDocumentRef = this.createSignedContentNode(targetNodeRef, signedDocumentName, signedProperties, signedDocAsBytes);
						 this.nodeService.createAssociation(nodeRef, signedDocumentRef, DocusignModel.ASSOC_SIGNED_DOCUMENT_ASSOC);
						 
						 // update the status of the original documetn to completed an set the signed Date
						 this.nodeService.setProperty(nodeRef, DocusignModel.PROP_STATUS, DocusignModel.DOCUMENT_STATUS_COMPLETED);
						 // set the nodeREf for the signed document on the original signature request document for reference sake
						 this.nodeService.setProperty(nodeRef, DocusignModel.PROP_SIGNED_DOCUMENT_ID, signedDocumentRef.getId());
						 // set teh signed document date on the orginal document for reference purposes
						 this.nodeService.setProperty(nodeRef, DocusignModel.PROP_COMPLETED_DATE, compDate);
				 }
			 }
		} // status not set to sent on document
			 
			 
			 
	  }catch(IOException ioe) {
		  throw new Exception("Could not retrieve signed document from Docusign");
	  }
	  
	}


/**
   * This method will get all of the envelopes in Docusign where a document status has changed in the last 5 days
   * 
   * For each item found, it proceses the document associated with the change.  
   * @param: documentMap: contains a list of documents and their associated envelopeIds that we need to check the status's for.
   * 
 * @throws Exception 
   */
  public void processSentDocuments(Map<String, NodeRef> documentMap) throws Exception {
	
	  try {
	  
			 
			 logger.debug("DocusignSerice.processSentDocuments: Entered");
			 
				 getDocusignAPIConnectionDetails();
				 ListEnvelopes listEnvelopesApi = new ListEnvelopes(apiClient, clientId, apiUserId, targetAccount, authServer, this.privateKey);
				 
				
				 Calendar calendar = Calendar.getInstance();
			      
			      // Decrementing days by 1
			     calendar.add(Calendar.DATE, -5);
				 Date fromDate = calendar.getTime();
				
				 List envelopeIdList = new ArrayList<String>(documentMap.keySet());
				 
				 EnvelopesInformation envelopesInformation = listEnvelopesApi.list(envelopeIdList, fromDate);
				 
				 List<Envelope> envelopeList = envelopesInformation.getEnvelopes();
				 logger.debug("DocusignSerice.processSentDocuments: Found  " + envelopeList.size() + " docusign envelopes to process");
				 if(envelopeList != null) {
					 for(Envelope envelope: envelopeList) {
						if(envelope != null) {
							this.processDocumentStatusChangeInRetry(envelope, documentMap.get(envelope.getEnvelopeId()));
						}
					 }
				 }
				 			 
			 
	  }finally {
		  logger.debug("DocusignSerice.processSentDocuments: Exited ");
	  }
	  
	}
  
  
  /**
   * For a document, process the change to the status in Docusign and also if signing is complete then download the signed document.
   * 
   * @param envelope
   * @param nodeRef
   */
  private void processDocumentStatusChange(Envelope envelope, NodeRef nodeRef) {
	  
	  try {
			 logger.debug("DocusignSerice.processDocumentStatusChange: Entered for envelopeId " + envelope.getEnvelopeId() + " and nodeRef " + nodeRef 
					 + "\n Name: " + this.nodeService.getProperty(nodeRef, ContentModel.PROP_NAME) );
			 
			 String status = envelope.getStatus();
			 
			 // only process it if the status has changed from sent.
			 if(!"sent".equalsIgnoreCase(status)) {
				 String statusChanged = envelope.getStatusChangedDateTime();
				 String envelopeId = envelope.getEnvelopeId();
				  
				 // TODO set the status changed date
				 //Date statusChangedDate = new Date(statusChaged);
				 //this.nodeService.setProperty(nodeRef, DocusignModel.PROP_STATUS_CHANGED_DATE, statusChangedDate);
				
				 this.nodeService.setProperty(nodeRef, DocusignModel.PROP_STATUS, status);
				 logger.debug("DocusignSerice.processDocumentStatusChange: Updated status to: " + status);
				 
				 if(DocusignModel.DOCUMENT_STATUS_COMPLETED.equalsIgnoreCase(status)) {
				 
					 // process signed document
					 String completionDate = envelope.getCompletedDateTime();
					 Date compDate = ISO8601DateFormat.parse(completionDate);
					 processSignedDocument(nodeRef, envelopeId, compDate);
				 }
			 }
	
		 
	  }catch(Exception e) {
		  e.printStackTrace();
		  if(logger.isDebugEnabled()) { logger.debug("Exception: Could not process document " + nodeRef + " Message: " + e.getMessage());}
	  }
	  finally {
		  logger.debug("DocusignSerice.getDocusignStatus: Exited ");
	  }
	}
  
  /** Link Client Folder to Client RuleSet
   *  Link Client/General folder to Client/General ruleset
   * 
   * @param newFolderRef
   */
  private void processDocumentStatusChangeInRetry(Envelope envelope, NodeRef nodeRef) {
    
    if(logger.isDebugEnabled()){logger.debug("Entered processDocumentStatusChange method");}
    
    
    
    RetryingTransactionHelper txHelper = this.retryingTransactionHelper;
    
    boolean readOnly = false;
    boolean requiresNew = false;
    
    txHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
      {
          public Void execute() throws Throwable {
             // Do something in a new transaction...
            
        	  try {
     			 logger.debug("DocusignSerice.processDocumentStatusChange: Entered for envelopeId " + envelope.getEnvelopeId() + " and nodeRef " + nodeRef 
     					 + "\n Name: " + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME) );
     			 
     			 String status = envelope.getStatus();
     			 
     			 // only process it if the status has changed from sent.
     			 if(!"sent".equalsIgnoreCase(status)) {
     				 String statusChanged = envelope.getStatusChangedDateTime();
     				 String envelopeId = envelope.getEnvelopeId();
     				  
     				 // TODO set the status changed date
     				 //Date statusChangedDate = new Date(statusChaged);
     				 //this.nodeService.setProperty(nodeRef, DocusignModel.PROP_STATUS_CHANGED_DATE, statusChangedDate);
     				
     				 nodeService.setProperty(nodeRef, DocusignModel.PROP_STATUS, status);
     				 logger.debug("DocusignSerice.processDocumentStatusChange: Updated status to: " + status);
     				 
     				 if(DocusignModel.DOCUMENT_STATUS_COMPLETED.equalsIgnoreCase(status)) {
     				 
     					 // process signed document
     					 String completionDate = envelope.getCompletedDateTime();
     					 Date compDate = ISO8601DateFormat.parse(completionDate);
     					 processSignedDocument(nodeRef, envelopeId, compDate);
     				 }
     			 }
     	
     		 
     	  }catch(Exception e) {
     		  e.printStackTrace();
     		  if(logger.isDebugEnabled()) { logger.debug("Exception: Could not process document " + nodeRef + " Message: " + e.getMessage());}
     		  throw e;
     	  }
     	  

        	  
			return null;           
          }
      }, readOnly, requiresNew);
    if(logger.isDebugEnabled()){logger.debug("Exited processDocumentStatusChange method");}
  }

  
  /**
   * 
   * @param nodeRef
   * @param envelopeId
   * @param completionDate
 * @throws Exception 
   */
  private void processSignedDocument(NodeRef nodeRef, String envelopeId, Date completionDate) throws Exception {
	  
	  logger.debug("DocusignSerice.processSignedDocument: for nodeRef " + nodeRef + " and envelopeId " + envelopeId);
		  
		  byte[] signedDocAsBytes  = null;
		  byte[] certificateOfCompletionAsBytes = null;
 		  try {
			 
			 
			//if(apiClient!=null && apiClient.getAccessToken())
			 //getDocusignAPIConnectionDetails();
			 SignedDocument signedDocument = new SignedDocument(apiClient, clientId, apiUserId, targetAccount, authServer, this.privateKey);
			 
			 signedDocAsBytes = signedDocument.getSignedDocument(envelopeId);
			 
			 
			 if(signedDocAsBytes != null) {
			
//					 Envelope envelope = signedDocument.getCompletedEnvelope(envelopeId);
//					 String completionDate = envelope.getCompletedDateTime();

				 // get the assoc for the target document.  If no value set then the parent of the current node becomes the target the signed document will be stored in.
				 NodeRef targetNodeRef = null;
				 List<AssociationRef> assocList = this.nodeService.getTargetAssocs(nodeRef, DocusignModel.ASSOC_SIGNED_DOCUMENT_TARGET);
				 if(assocList != null) {
					 AssociationRef targetRef = assocList.get(0);
					 targetNodeRef = targetRef.getTargetRef();
				 }
				 
				 targetNodeRef = targetNodeRef!=null? targetNodeRef : nodeService.getPrimaryParent(nodeRef).getParentRef();
		
				 
				 // set docusign aspect values so that the signed document can be traced.
				 Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
				 
				 Map<QName, Serializable> signedProperties = new HashMap<QName, Serializable>(4);
				 String signedDocumentName = getSignedDocumentName((String)properties.get(ContentModel.PROP_NAME), targetNodeRef);
				 signedProperties.put(ContentModel.PROP_NAME, signedDocumentName);
				 signedProperties.put(DocusignModel.PROP_DOCUMENT_TYPE, DocusignModel.DOCUMENT_TYPE_SIGNED);
				 
				 signedProperties.put(DocusignModel.PROP_STATUS, "completed");
			      
			     
			     signedProperties.put(DocusignModel.PROP_COMPLETED_DATE, completionDate);
				 
			     // set props that were set when document sent for signature.
			     signedProperties.put(DocusignModel.PROP_RECIPIENT, properties.get(DocusignModel.PROP_RECIPIENT));
			     signedProperties.put(DocusignModel.PROP_ENVELOPE_ID, properties.get(DocusignModel.PROP_ENVELOPE_ID));
			     signedProperties.put(DocusignModel.PROP_SENT_DATE, properties.get(DocusignModel.PROP_SENT_DATE));
			     signedProperties.put(DocusignModel.PROP_SENT_BY, properties.get(DocusignModel.PROP_SENT_BY));
			     
				 NodeRef signedDocumentRef = this.createSignedContentNode(targetNodeRef, signedDocumentName, signedProperties, signedDocAsBytes);
				 this.nodeService.createAssociation(nodeRef, signedDocumentRef, DocusignModel.ASSOC_SIGNED_DOCUMENT_ASSOC);
				 
				 logger.debug("DocusignSerice.processSignedDocument: Signed document created with nodeRef " + signedDocumentRef);
				 
				 // add the certificate of completion document as a child of the signedDocument
				 NodeRef certOfCompleationRef = storeCertificateOfCompletion(signedDocumentRef, envelopeId, signedDocument);
				 if(!this.nodeService.hasAspect(signedDocumentRef, DocusignModel.ASPECT_SIGNED_DOCUMENT)) {
					 Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
					 props.put(DocusignModel.PROP_CERTIFICATE_OF_COMPLETION_NODE_ID, certOfCompleationRef.getId());
					 this.nodeService.addAspect(signedDocumentRef, DocusignModel.ASPECT_SIGNED_DOCUMENT, props);
				 }else {
					 this.nodeService.setProperty(signedDocumentRef, DocusignModel.PROP_CERTIFICATE_OF_COMPLETION_NODE_ID, certOfCompleationRef.getId());
				 }
				 
				 // update the status of the original documetn to completed an set the signed Date
				 this.nodeService.setProperty(nodeRef, DocusignModel.PROP_STATUS, DocusignModel.DOCUMENT_STATUS_COMPLETED);
				 // set the nodeREf for the signed document on the original signature request document for reference sake
				 this.nodeService.setProperty(nodeRef, DocusignModel.PROP_SIGNED_DOCUMENT_ID, signedDocumentRef.getId());
				 // set teh signed document date on the orginal document for reference purposes
				 this.nodeService.setProperty(nodeRef, DocusignModel.PROP_COMPLETED_DATE, completionDate);

			 }
 		  }catch(IOException | ApiException ioe) {
 			  throw new Exception("Could not retrieve signed document from Docusign");
		}finally {
			logger.debug("DocusignSerice.procesSignedDocument: Exited ");
	 }

  }

  /**
   * Store the certificate of completion document in the repo as a child of the nodeRef
   * 
   * @param signedDocumentRef
   * @param envelopeId
   * @param signedDocument
   * @return
   * @throws ApiException
   * @throws IOException
   */
  private NodeRef storeCertificateOfCompletion(NodeRef signedDocumentRef, String envelopeId, SignedDocument signedDocument) 
		  throws ApiException, IOException {
	
	  
	logger.debug("storeCertificateOfCompletion entered");
	NodeRef nodeRef = null;
	try {
		byte[] docBytes = signedDocument.getCerticateOfCompletion(envelopeId);
		
		if(docBytes != null && docBytes.length > 0) {
			logger.debug("storeCertificateOfCompletion retrieved certification of completion from docusign");
		      // use the node service to create a new node
			String docName = "Certificate of Completion.pdf";
			
			Map<QName, Serializable> props = new HashMap<>();
			props.put(ContentModel.PROP_NAME, docName);
			props.put(DocusignModel.PROP_SIGNATURE_RESPONSE_DOCUMENT_TYPE, DocusignModel.DOCUMENT_TYPE_CERTIFICATE_OF_COMPLETION);
			
		      nodeRef = this.nodeService.createNode(
		    		  signedDocumentRef, 
	                  DocusignModel.ASSOC_CERTIFICATE_OF_COMPLETION, 
	                  QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, docName),
	                  ContentModel.TYPE_CONTENT, props).getChildRef();
		                          
		      // Use the content service to set the content onto the newly created node
		      ContentWriter writer = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
		      writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
		      writer.setEncoding("UTF-8");
		      
		      writer.putContent(new ByteArrayInputStream(docBytes));
		      
		  	logger.debug("storeCertificateOfCompletion retrieved certification added to alfresco as a child node of " + signedDocumentRef);
		}else {
			logger.debug("storeCertificateOfCompletion: No certification of completion available");
		}
	      
	      return nodeRef;
	
	 
		
	} finally {
		logger.debug("storeCertificateOfCompletion exited");
	}
	  
}




private String getSignedDocumentName(String name, NodeRef targetFolderRef) {
	// TODO Auto-generated method stub
	  
	  logger.debug("SignedDocumentName Original Name: " + name);
	  String signedName = name;
	  if(name.contains(".")) {
		  String fileName = name.split("\\.")[0];
		  //String extension = name.split("\\.")[1];
	  	  signedName = fileName + "_signed.pdf";  
	  }
	  
	  signedName = DocumentUtil.getUniqueNodeNameInFolder(signedName, targetFolderRef, nodeService);
	  
	  logger.debug("SignedDocumentName: " + signedName);
	  
	  
	  return signedName;
}


/**
   * This method printing pretty json format
   * @param arg - any object to be written as string
   */
  private void printPrettyJSON(Object arg) {
      try {
    	  ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    	  String jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arg);
          System.out.println("Results:");
          System.out.println(jsonInString);
      } catch (JsonProcessingException e) {
          e.printStackTrace();
      }
  }
  
  /** 
   * Read a PEM file into a string from the classpath at the location specified
   * 
   * @return string
   */
  private String readPrivatekeyFromPemFile() 
		  throws IOException {
	  logger.debug("readPrivateKey entered");
		  
	
	  
	  	try {
	  		// try reading file from file system
	  		File resource = new ClassPathResource(
		      this.getPrivateKeyPemFile()).getFile();
	  			return new String(Files.readAllBytes(resource.toPath()));
	  	}catch(IOException ie) {
	  		
	  	}
	  	
	  	InputStream resource = new ClassPathResource(
	  			this.getPrivateKeyPemFile()).getInputStream();
	  	
	  	String key = null;
	  	
	  	try ( BufferedReader reader = new BufferedReader(
	  	      new InputStreamReader(resource)) ) {
	  	         key = reader.lines()
	  	          .collect(Collectors.joining("\n"));
	  	    }
	  	    
		    logger.debug("Read key of \n" + key);
		    
		    return key;
		    
  }
  

  
  private NodeRef createSignedContentNode(NodeRef parent, String name, Map<QName, Serializable> props, byte[] content)
  {

      // Create a map to contain the values of the properties of the node
          
      //Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
	  
	  logger.debug("name: , " + name);
	  logger.debug("parent: , " + parent);
	  logger.debug("props: , " + props);
	  
	// set the response document type as well.  this will result in the signatureResponseDocumentAspect also being added to the signed document
	props.put(DocusignModel.PROP_SIGNATURE_RESPONSE_DOCUMENT_TYPE, DocusignModel.DOCUMENT_TYPE_SIGNED);	 
		 
		 
      

      // use the node service to create a new node
      NodeRef node = this.nodeService.createNode(
                          parent, 
                          ContentModel.ASSOC_CONTAINS, 
                          QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                          ContentModel.TYPE_CONTENT, 
                          props).getChildRef();
                          
      // Use the content service to set the content onto the newly created node
      ContentWriter writer = this.contentService.getWriter(node, ContentModel.PROP_CONTENT, true);
      writer.setMimetype(MimetypeMap.MIMETYPE_PDF);
      writer.setEncoding("UTF-8");
      
      writer.putContent(new ByteArrayInputStream(content));
      
      // Return a node reference to the newly created node
      return node;
  } 
  
  
  
  public SearchService getSearchService() {
    return searchService;
  }

  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  public NodeService getNodeService() {
    return nodeService;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public DictionaryService getDictionaryService() {
    return dictionaryService;
  }

  public void setDictionaryService(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  public FileFolderService getFileFolderService() {
    return fileFolderService;
  }

  public void setFileFolderService(FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  public SiteService getSiteService() {
    return siteService;
  }

  public void setSiteService(SiteService siteService) {
    this.siteService = siteService;
  }

  
  public NamespaceService getNamespaceService() {
    return namespaceService;
  }

  public void setNamespaceService(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  
  public CopyService getCopyService() {
    return copyService;
  }

  public void setCopyService(CopyService copyService) {
    this.copyService = copyService;
  }

  public CheckOutCheckInService getCheckOutCheckInService() {
    return checkOutCheckInService;
  }

  public void setCheckOutCheckInService(
      CheckOutCheckInService checkOutCheckInService) {
    this.checkOutCheckInService = checkOutCheckInService;
  }
  
  public VersionService getVersionService() {
    return versionService;
  }

  public void setVersionService(VersionService versionService) {
    this.versionService = versionService;
  }
  
  public ContentService getContentService() {
		return contentService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}


	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	  
	  public String getApiUserId() {
		return apiUserId;
	}

	public void setApiUserId(String apiUserId) {
		this.apiUserId = apiUserId;
	}

	public String getTargetAccount() {
		return targetAccount;
	}

	public void setTargetAccount(String targetAccount) {
		this.targetAccount = targetAccount;
	}

	public String getAuthServer() {
		return authServer;
	}

	public void setAuthServer(String authServer) {
		this.authServer = authServer;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public static ApiClient getApiclient() {
		return apiClient;
	}

	public String getPrivateKeyPemFile() {
		return privateKeyPemFile;
	}

	public void setPrivateKeyPemFile(String privateKeyPemFile) {
		this.privateKeyPemFile = privateKeyPemFile;
	}

	private void getOAuth(){
//		  System.setProperty("https.protocols","TLSv1.2");
//	      System.out.println("\nSending an envelope. The envelope includes HTML, Word, and PDF documents. It takes about 15 seconds for DocuSign to process the envelope request... ");
	      
	      java.util.List<String> scopes = new ArrayList<String>();
	      // Only signature scope is needed. Impersonation scope is implied.
	      scopes.add(OAuth.Scope_SIGNATURE);
	      
	      
	      String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" + 
	      		"MIIEogIBAAKCAQEAmbDLNAKNjBbs2D6jai+ItSwq9ByyI9nSQMye8eGg8gFoBL4V\n" + 
	      		"Xu/cZuxkCsGJgnLw+lWjGqurUUbPUZyEq5VL1BKg2qGrIK0smajdUz+y/K35tdRV\n" + 
	      		"fx55ZEQGLfGTnhTvXIm61ZOOqwFj5QOwlShwGJ7kMEao6FfJbfSce7+oezWDBK0o\n" + 
	      		"+BudpKghFGhgHZBybAR66Q81Bp5Q3d3Y3dgP1WKkuLJgMFy+POWBG3adN9clTSF1\n" + 
	      		"IyMU8k3mC+VtKRSCTMjvBVsZ11VIrtEsZlyuVR8q4RSRwoa4rk5lR4HCi3x2AZKj\n" + 
	      		"KgICXuwE3RgrOD0+kIrW3WRJvbv7PmtB9TbrmQIDAQABAoIBAARxpz6GwF8HnquW\n" + 
	      		"DBWa8EIE1+0dE02UE89J22K1GnlE7yQL9wQ6vU3Akhf5hqdd+XfTmBbCVf7QpFgA\n" + 
	      		"xZlMhNfJ/hq0SAV23G5JArVQhzVlNm46iIpXcD0k9mTHsnQEuPMdDGj2jk6FiMTr\n" + 
	      		"eWSmua8MkR6QPHwTZcCnLQsq/I0rcHZ20axnoIsBb/5LuxEI0G3i3kDksbJ9yXJT\n" + 
	      		"KFO7c/CUj2Wciwd6rODYmZPOIBNax6pWILaVSb7xsATZ0Lnv23k4mkqpMxBjcc6F\n" + 
	      		"VCscCfyemV2aLXkZdb0C4ayI6B6Geazp1bWEBuZEqbicrm3si5w+7ziwHvUvmdv+\n" + 
	      		"lGvV/YECgYEAznQ8bpX1fEFIooc4An7YR9OoIxVhXQSbokWo4izXnsP94bHdeKVF\n" + 
	      		"PR+nA772rQKLYVLBLJKh8RXkKhJJ+vbgsx+iRnBOloFw3wh7fysnRl4nM0QZhrul\n" + 
	      		"ltZI3LvwFZ0NqMheYFbnyuTRcj4gnmCUzc8K3g+TQz79qiJp11GC+1ECgYEAvpL4\n" + 
	      		"Em7xLmgamO449nDwb/AhbIc5UHw0IlqF7lSg1zF6YTJW6W59/bvpusYMC5EtoyKG\n" + 
	      		"xy9fdSe8anENHalo+u+J4dNlK7dcNpbqdJGBLg5lQkrFZreW2nOQc2ofbyLEsXod\n" + 
	      		"WKmHld3cboOfvAr/GPlawiR/4fqMMQMW1w/9yckCgYAXQtFsREkcbt6YMVQGM/R6\n" + 
	      		"Y0aWbVpC+LnwVpjootTIdpysSTYC2dwrPt26dlGkvJynooNRWlQUrbq/YsAjHp3C\n" + 
	      		"kNXfydeQ7ZSiDHqdoWcTbphMFmgp3gqSLCKZfDNYvBFF6w13HVOQcKeIj6dtMMST\n" + 
	      		"S7iVvHuDIB3EhYwXTn/LgQKBgGWO0s6X+sVdSxdGlGqYgzlUAavhYCTJG8tW7Fq3\n" + 
	      		"FMO5JVuYxQI/FLtcIdGb6x1a77QpGSQa1ccMNveOaYvuFPjtIFCNJDQo0eZxhF48\n" + 
	      		"kFfn6gRq7kmv+p9NyKQI4NmL8MAXsH3oUk4GXosyb7R5M7quX7RoWRInQAB6gfh4\n" + 
	      		"GCopAoGAEhT9E+EP5zJot5+V6MZ6VE5bPWCW4FUfP1hIyr3wpHywXCx+9XNMV8lE\n" + 
	      		"QnyvVreSpMWns+TmvzS2kvZb2BGfkn8hC+JuqShw3pvu79GDuwT2HHk+v6wHIBDO\n" + 
	      		"655oAlXa6C5C0TeP1+QLrdKEN1gYOsfrOaUZYtMM9BwJiKUb2Uk=\n" + 
	      		"-----END RSA PRIVATE KEY-----";
	      
	      
	      
	      //String privateKey = DSConfig.PRIVATE_KEY.replace("\\n","\n");
	      byte[] privateKeyBytes = privateKey.getBytes();
	      
	      apiClient.setOAuthBasePath("account-d.docusign.com");
	     
	      
	      
	      try {
				OAuth.OAuthToken oAuthToken = apiClient.requestJWTUserToken (
						"6bb0ae23-ff56-4043-8f5a-dcd0f81f4b06",
				        "6f83e3b2-44a8-48f1-a41f-96bb9d940836",
				        scopes,
				        privateKeyBytes,
				        TOKEN_EXPIRATION_IN_SECONDS);
				
				//System.out.println("oAuth received");
				
			} catch (IllegalArgumentException | IOException | ApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  }

	public NodeLocatorService getNodeLocatorService() {
		return nodeLocatorService;
	}


	public void setNodeLocatorService(NodeLocatorService nodeLocatorService) {
		this.nodeLocatorService = nodeLocatorService;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	public RetryingTransactionHelper getRetryingTransactionHelper() {
		return retryingTransactionHelper;
	}
		
		
	public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper) {
		this.retryingTransactionHelper = retryingTransactionHelper;
	}




	
	
}
