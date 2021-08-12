package au.com.seedim.docusign.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.model.Attachment;
import com.docusign.esign.model.CarbonCopy;
import com.docusign.esign.model.Document;
import com.docusign.esign.model.EnvelopeDefinition;
import com.docusign.esign.model.EnvelopeSummary;
import com.docusign.esign.model.Expirations;
import com.docusign.esign.model.Notification;
import com.docusign.esign.model.Recipients;
import com.docusign.esign.model.Reminders;
import com.docusign.esign.model.Signer;

import au.com.seedim.docusign.utils.DocumentUtil;

class SendEnvelope extends JWTAuthBase {


    public SendEnvelope(ApiClient apiClient, String clientId, String apiUserId, String targetAccount, String authServer, String privateKey) throws IOException {
        super(apiClient, clientId, apiUserId, targetAccount, authServer, privateKey);
        
    }
    
    
    /**
     * 
     * @param recipientList
     * @param userName
     * @param message
     * @param docFile
     * @param fileName
     * @param expireDays
     * @param expireWarn
     * @param reminderDelay
     * @param reminderFrequency
     * @return
     * @throws ApiException
     * @throws IOException
     */
    public EnvelopeSummary sendEnvelope(List<Recipient> recipientList, String userName, String messageTitle, String messageBody, File docFile, String fileName, Map<String, File> supplementalDocuments, 
    		int expireDays, int expireWarn, int reminderDelay, int reminderFrequency ) throws ApiException, IOException {

        this.checkToken();
        
        List<Document> docList = new ArrayList<Document>();
        EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition();
        
        //String requestMessage = messageTitle + " (Requested from " + userName + ")";
        envelopeDefinition.setEmailSubject(messageTitle);
        if(messageBody != null) {
        	envelopeDefinition.setEmailBlurb(messageBody);
        }

        Document doc = new Document();
        doc.setDocumentBase64(DocumentUtil.base64String(docFile));
        doc.setName(fileName);
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        doc.setFileExtension(extension);
        doc.setDocumentId("1");
        
        docList.add(doc);
        
        // add supplemental documents : NOT SUPPORTED IN TRIAL LICENSE
        if (supplementalDocuments != null) {
	        for(String supfileName: supplementalDocuments.keySet()) {
	        	Document suppDoc = createSupplementalDocument(supfileName, supplementalDocuments.get(supfileName), "no_interaction ", "" + docList.size() + 1);
	        	docList.add(suppDoc);
	        }
        }
        
        // The order in the docs array determines the order in the envelope
        envelopeDefinition.setDocuments(docList);
         
        // create a signer recipient to sign the document, identified by name and email
        // We're setting the parameters via the object creation
        Recipients recipients = createRecipients(recipientList);
        envelopeDefinition.setRecipients(recipients);
        
        // add the expiration and reminders to the node
 		if(expireDays > 0 || reminderDelay > 0) {
 			Notification notifications = new Notification();
 			notifications.setUseAccountDefaults("false");
         
 			if(expireDays > 0) {
 				notifications.setExpirations(this.createExpirations(expireDays, expireWarn));
 			}
 			
 			if(reminderDelay > 0) {
 				notifications.setReminders(this.createReminders(reminderDelay, reminderFrequency));
 				
 			}
         
 			envelopeDefinition.setNotification(notifications);
 		}
 		
 		
    
        // Request that the envelope be sent by setting |status| to "sent".
        // To request that the envelope be created as a draft, set to "created"
        envelopeDefinition.setStatus("sent");
        
        logger.debug("EnvelopeDefinition " + envelopeDefinition.toString());

        EnvelopesApi envelopeApi = new EnvelopesApi(this.apiClient);
        EnvelopeSummary results = envelopeApi.createEnvelope(getAccountId(), envelopeDefinition);

        return results;
    }
    
    /** Create a Supplemental Document from the supplied file
     * 
     * @param file
     * @param string
     * @return
     * @throws IOException 
     */
    private Document createSupplementalDocument(String supFileName,File file, String signerMustAcknowledge, String index ) throws IOException {

    	Document doc = null;
    	try {
    	
    		doc = new Document();
	        doc.setDocumentBase64(DocumentUtil.base64String(file));
	        doc.setName(supFileName);
	        
	        
	        String extension = supFileName.substring(supFileName.lastIndexOf(".") + 1);
	        doc.setFileExtension(extension);
	        doc.setDocumentId(index);
	        doc.setSignerMustAcknowledge(signerMustAcknowledge);
	        doc.setIncludeInDownload("false");
	        doc.setDisplay("modal");
	    
	    }finally {
	    
	    }
        
        return doc;
    	
    	
	}


	/** 
     * Add all of the recipients to a recipients object and return
     * @param recipientList
     * @return
     */
	private Recipients createRecipients(List<Recipient> recipientList) {
		// TODO Auto-generated method stub
		// Add the recipients to the envelope object
        Recipients recipients = new Recipients();
		List<Signer> signerList = new ArrayList<Signer>();
		List<CarbonCopy> carbonCopyList = new ArrayList<CarbonCopy>();
		
		int i = 0;
		for(Recipient recipient: recipientList) {
			i++;
			if(recipient.action != null && "sign".equalsIgnoreCase(recipient.action)) {
				
				Signer signer = new Signer();
		        signer.setEmail(recipient.email);
		        signer.setName(recipient.name);
		        signer.setRecipientId(String.valueOf(i));
		        signer.setRoutingOrder(String.valueOf(i));
		        signerList.add(signer);
		        
		        
				
			}else {
				CarbonCopy cc = new CarbonCopy();
		        cc.setEmail(recipient.email);
		        cc.setName(recipient.name);
		        cc.setRoutingOrder(String.valueOf(i));
		        cc.setRecipientId(String.valueOf(i));
		        carbonCopyList.add(cc);
		        
			}
			
			if(logger.isDebugEnabled()) {logger.debug("addRecipients: added " + recipient + " at position " + i);}
		}
		
        
        recipients.setSigners(signerList);
        recipients.setCarbonCopies(carbonCopyList);

        return recipients;
		
	}


    /**
     * Deprecated - Use sendEnvelope with recipientList above
     * 
     * @param signerEmail
     * @param signerName
     * @param ccEmail
     * @param ccName
     * @param docFile
     * @param fileName
     * @param expireDays
     * @param expireWarn
     * @param reminderDelay
     * @param reminderFrequency
     * @return
     * @throws ApiException
     * @throws IOException
     */
    public EnvelopeSummary sendEnvelope(String signerEmail, String signerName, String ccEmail, String ccName, File docFile, String fileName, 
    		int expireDays, int expireWarn, int reminderDelay, int reminderFrequency ) throws ApiException, IOException {

        this.checkToken();
        
        
        EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition();
        envelopeDefinition.setEmailSubject("Please sign this document sent from " + ccName);

        Document doc = new Document();
        doc.setDocumentBase64(DocumentUtil.base64String(docFile));
        doc.setName(fileName);
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        doc.setFileExtension(extension);
        doc.setDocumentId("1");

        // The order in the docs array determines the order in the envelope
        envelopeDefinition.setDocuments(Arrays.asList(doc));
        
        // create a signer recipient to sign the document, identified by name and email
        // We're setting the parameters via the object creation
        Signer signer1 = new Signer();
        signer1.setEmail(signerEmail);
        signer1.setName(signerName);
        signer1.setRecipientId("1");
        signer1.setRoutingOrder("1");
        // routingOrder (lower means earlier) determines the order of deliveries
        // to the recipients. Parallel routing order is supported by using the
        // same integer as the order for two or more recipients.

        // create a cc recipient to receive a copy of the documents, identified by name and email
        // We're setting the parameters via setters
        CarbonCopy cc1 = new CarbonCopy();
        cc1.setEmail(ccEmail);
        cc1.setName(ccName);
        cc1.setRoutingOrder("2");
        cc1.setRecipientId("2");
        
        // Create signHere fields (also known as tabs) on the documents,
        // We're using anchor (autoPlace) positioning
        //
        
        // Add the recipients to the envelope object
        Recipients recipients = new Recipients();
        recipients.setSigners(Arrays.asList(signer1));
        recipients.setCarbonCopies(Arrays.asList(cc1));
        envelopeDefinition.setRecipients(recipients);
        
        // add the expiration and reminders to the node
		if(expireDays > 0 || reminderDelay > 0) {
			Notification notification = new Notification();
			notification.setUseAccountDefaults("false");
        
			if(expireDays > 0) {
				notification.setExpirations(this.createExpirations(expireDays, expireWarn));
			}
			
			if(reminderDelay > 0) {
				notification.setReminders(this.createReminders(reminderDelay, reminderFrequency));
				
			}
        
			envelopeDefinition.setNotification(notification);
		}
        // Request that the envelope be sent by setting |status| to "sent".
        // To request that the envelope be created as a draft, set to "created"
        envelopeDefinition.setStatus("sent");

        EnvelopesApi envelopeApi = new EnvelopesApi(this.apiClient);
        EnvelopeSummary results = envelopeApi.createEnvelope(this.getAccountId(), envelopeDefinition);

        return results;
    }
    
    /**
	 *  create expirations for the envelope
	 *  
	 * 
	 * @param expireDays
	 * @param expireWarn
	 * @return
	 */
    private Expirations createExpirations(int expireDays, int expireWarn){
    	
    	Expirations expirations = new Expirations();
    	
    	if(expireDays > 0 ) {
	    	
	    	expirations.setExpireEnabled("true");	
	    	expirations.setExpireAfter(Integer.toString(expireDays));  // envelope will expire after 30 days
	    	if(expireWarn < expireDays) {
	    		expirations.setExpireWarn(Integer.toString(expireWarn));  // expiration reminder would be sent two days before expiration
	    	}	
    	}	
    	
    	return expirations;
    }
    
   
   /**
    * Set reminders for the envelope
    *  
    * @param notification
    * @param reminderDelay
    * @param reminderFrequency
    * @return
    */
    private Reminders createReminders(int reminderDelay, int reminderFrequency){
    	
    	Reminders reminders = new Reminders();
    	
    	if(reminderDelay > 0) {
	    	
    		reminders.setReminderEnabled("true");
    		reminders.setReminderDelay(Integer.toString(reminderDelay));  // first reminder to be sent X days after envelope was sent
    		
    		if(reminderFrequency > 0)
    			reminders.setReminderFrequency(Integer.toString(reminderFrequency)); // keep sending reminders every two days	    	
    	
    	}
    	
    	return reminders;
    }
}
