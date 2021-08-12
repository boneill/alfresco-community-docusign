package au.com.seedim.docusign.service;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.api.EnvelopesApi.ListStatusChangesOptions;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.model.EnvelopesInformation;
import org.joda.time.LocalDate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * This class shows get status changes for one or more envelope(s).
 *
 */
public class ListEnvelopes extends JWTAuthBase{

    public ListEnvelopes(ApiClient apiClient) throws IOException {
        super(apiClient);
    }
    
    public ListEnvelopes(ApiClient apiClient, String clientId, String apiUserId, String targetAccount, String authServer,
    		String privateKey) throws IOException {
    	super(apiClient, clientId, apiUserId, targetAccount, authServer, privateKey);
    }
    
    
    

    /**
     * get status for one or more envelope(s) in the last 30 days
     * @return - envelopes information
     * @throws ApiException
     */
    public EnvelopesInformation list() throws ApiException, IOException {

        this.checkToken();
        EnvelopesApi envelopeApi = new EnvelopesApi(this.apiClient);

        ListStatusChangesOptions options = envelopeApi.new ListStatusChangesOptions();
        LocalDate date = LocalDate.now().minusDays(30);
        options.setFromDate(date.toString("yyyy/MM/dd"));

        return envelopeApi.listStatusChanges(this.getAccountId(), options);

    }
    
    /**
     * get status for one or more envelope(s) in the last 30 days
     * @return - envelopes information
     * @throws ApiException
     */
    public EnvelopesInformation list(List<String> envelopeIdList, Date fromDate) throws ApiException, IOException {

        this.checkToken();
        EnvelopesApi envelopeApi = new EnvelopesApi(this.apiClient);

        logger.debug("Envelope ID List" + envelopeIdList);
        
        ListStatusChangesOptions options = envelopeApi.new ListStatusChangesOptions();
        options.setEnvelopeIds(String.join(",", envelopeIdList));
        
        //LocalDate date = LocalDate.now().minusDays(5);
        if(fromDate != null) {
        	
        	SimpleDateFormat sdf;
        	sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        	//sdf.setTimeZone(TimeZone.getTimeZone("CET"));
        	String textDate = sdf.format(fromDate);

        	
        	options.setFromDate(textDate);
        }

        return envelopeApi.listStatusChanges(this.getAccountId(), options);

    }

}
