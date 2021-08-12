package au.com.seedim.docusign.service;

import com.docusign.esign.client.*;
import com.docusign.esign.client.auth.OAuth;
//import com.docusign.example.jwt.DSConfig;

import au.com.seedim.docusign.api.SendForSignatureWebscript;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * This is an example base class to be extended to show functionality example.
 * its has a apiClient member as a constructor argument for later usage in API calls.
 */
public class JWTAuthBase {

	static final Logger logger = Logger.getLogger(JWTAuthBase.class);
	
	private static final long TOKEN_EXPIRATION_IN_SECONDS = 3600;
    private static final long TOKEN_REPLACEMENT_IN_MILLISECONDS = 10 * 60 * 1000;

    private static OAuth.Account _account;
    private static File privateKeyTempFile = null;
    private static long expiresIn;
    private static String token = null;
    protected final ApiClient apiClient;
    
    private String clientId;
    private String apiUserId;
    private String targetAccount;
    private String authServer;
    private String privateKey;

    protected static String getAccountId() {
        return _account.getAccountId();
    };


    public JWTAuthBase(ApiClient apiClient) throws IOException {
        this.apiClient =  apiClient;
    }

    public JWTAuthBase(ApiClient apiClient, String clientId, String apiUserId, String targetAccount, String authServer,
			String privateKey) throws IOException{
			
    	this.apiClient =  apiClient;
    	this.clientId = clientId;
    	this.apiUserId = apiUserId;;
    	this.targetAccount= targetAccount;
    	this.authServer = authServer;
    	this.privateKey = privateKey;	
			
	}


	protected void checkToken() throws IOException, ApiException {
        if(this.token == null
                || (System.currentTimeMillis() + TOKEN_REPLACEMENT_IN_MILLISECONDS) > this.expiresIn) {
            updateToken();
        }
    }

    private void updateToken() throws IOException, ApiException {
        logger.debug("\n JWTAuthBase: updateToken: Fetching an access token via JWT grant...");

        java.util.List<String> scopes = new ArrayList<String>();
        // Only signature scope is needed. Impersonation scope is implied.
        scopes.add(OAuth.Scope_SIGNATURE);
        
          logger.debug("JWTAuthBase: updateToken: " + this.privateKey);
          String privateKey = this.privateKey;;
          byte[] privateKeyBytes = privateKey.getBytes();
        
        
//        String privateKey = DSConfig.PRIVATE_KEY.replace("\\n","\n");
//        byte[] privateKeyBytes = privateKey.getBytes();
        
        
        apiClient.setOAuthBasePath(this.authServer);
        
        logger.debug("JWTAuthBase: updateToken: Request JWT User Token inputs: \n ClientId: " + clientId + "\n" +
        		"apiUserId: " + apiUserId + "\n" +
        		"privateKey: " + privateKey + "\n" + 
        		"apiClient: " + apiClient);
       

        OAuth.OAuthToken oAuthToken = apiClient.requestJWTUserToken (
                this.clientId,
                this.apiUserId,
                scopes,
                privateKeyBytes,
                TOKEN_EXPIRATION_IN_SECONDS);
        apiClient.setAccessToken(oAuthToken.getAccessToken(), oAuthToken.getExpiresIn());
        
        logger.debug("JWTAuthBase: updateToken: Done. Continuing...\n");

        if(_account == null)
            _account = this.getAccountInfo(apiClient);
        // default or configured account id.
        apiClient.setBasePath(_account.getBaseUri() + "/restapi");

        token = apiClient.getAccessToken();
        expiresIn = System.currentTimeMillis() + (oAuthToken.getExpiresIn() * 1000);
    }

    private OAuth.Account getAccountInfo(ApiClient client) throws ApiException {
        OAuth.UserInfo userInfo = client.getUserInfo(client.getAccessToken());
        OAuth.Account accountInfo = null;

        if(this.targetAccount == null || this.targetAccount.length() == 0 || "FALSE".equals(this.targetAccount)){
            List<OAuth.Account> accounts = userInfo.getAccounts();

            OAuth.Account acct = this.find(accounts, new ICondition<OAuth.Account>() {
                public boolean test(OAuth.Account member) {
                    return (member.getIsDefault() == "true");
                }
            });

            if (acct != null) return acct;

            acct = this.find(accounts, new ICondition<OAuth.Account>() {
                public boolean test(OAuth.Account member) {
                    return (member.getAccountId() == targetAccount);
                }
            });

            if (acct != null) return acct;

        }

        return accountInfo;
    }

    private OAuth.Account find(List<OAuth.Account> accounts, ICondition<OAuth.Account> criteria) {
        for (OAuth.Account acct: accounts) {
            if(criteria.test(acct)){
                return acct;
            }
        }
        return null;
        
    }

    interface ICondition<T> {
        boolean test(T member);
    }
}
