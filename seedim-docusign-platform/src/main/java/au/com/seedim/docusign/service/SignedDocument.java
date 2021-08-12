package au.com.seedim.docusign.service;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.model.Date;
import com.docusign.esign.model.Envelope;

/**
 * This class shows get status changes for one or more envelope(s).
 *
 */
public class SignedDocument extends JWTAuthBase{
	
	static final Logger logger = Logger.getLogger(SignedDocument.class);

    public SignedDocument(ApiClient apiClient) throws IOException {
        super(apiClient);
    }

    public SignedDocument(ApiClient apiClient, String clientId, String apiUserId, String targetAccount,
			String authServer, String privateKey) throws IOException{
    	
    	super(apiClient, clientId, apiUserId, targetAccount, authServer, privateKey);
		// TODO Auto-generated constructor stub
	}

	/**
     * get status for one or more envelope(s) in the last 30 days
     * @return - envelopes information
     * @throws ApiException
     */
    public byte[] getSignedDocument(String envelopeId) throws ApiException, IOException {

    	byte[] docBytes = null;
    	
    	this.checkToken();
        EnvelopesApi envelopesApi = new EnvelopesApi(this.apiClient);

        
        Envelope envelope = envelopesApi.getEnvelope(this.getAccountId(), envelopeId);
        String status = envelope.getStatus();
        String completedData = envelope.getCompletedDateTime();
        
        logger.debug("Docusign document status is " + status);
        
        
        if("completed".equalsIgnoreCase(status)) {
        	docBytes = envelopesApi.getDocument(this.getAccountId(), envelopeId, "1");
        
        }
        
        return docBytes;
    }
    
    /**
     * get get the certificate of completion for the signed document
     * @return - envelopes information
     * @throws ApiException
     */
    public byte[] getCerticateOfCompletion(String envelopeId) throws ApiException, IOException {

    	byte[] docBytes = null;
    	
    	this.checkToken();
        EnvelopesApi envelopesApi = new EnvelopesApi(this.apiClient);

        
        Envelope envelope = envelopesApi.getEnvelope(this.getAccountId(), envelopeId);
        String status = envelope.getStatus();
        String completedData = envelope.getCompletedDateTime();
        
        logger.debug("Docusign document status is " + status);
        
        
        if("completed".equalsIgnoreCase(status)) {
        	docBytes = envelopesApi.getDocument(this.getAccountId(), envelopeId, "certificate");
        
        }
        
        return docBytes;
    }

    
    /**
     * get Envelope for completed docs in the last 30 days
     * @return - envelopes information
     * @throws ApiException
     */
    public Envelope getCompletedEnvelope(String envelopeId) throws ApiException, IOException {

    	
    	this.checkToken();
        EnvelopesApi envelopesApi = new EnvelopesApi(this.apiClient);

        
        Envelope envelope = envelopesApi.getEnvelope(this.getAccountId(), envelopeId);
        
        String status = envelope.getStatus();
        
        logger.debug("Docusign document status is " + status);
        
        if("completed".equalsIgnoreCase(status)) {
        	return envelope;
        }
        
        return null;
    }

    /**
     * get Envelope status
     * @throws ApiException
     */
    public String getEnvelopeStatus(String envelopeId) throws ApiException, IOException {

    	
    	this.checkToken();
        EnvelopesApi envelopesApi = new EnvelopesApi(this.apiClient);

        
        Envelope envelope = envelopesApi.getEnvelope(this.getAccountId(), envelopeId);
        String status =  envelope.getStatus();
        logger.debug("Docusign document status is " + status);
        
        return status;
        
    }

    

}
