package au.com.seedim.docusign.model;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;


public interface DocusignModel {
	
    public static final String URI_MODEL = "http://www.seedim.com.au/model/content/docusign/1.0";

	public static final String DOCUMENT_TYPE_ORIGINAL = "Original";
	public static final String DOCUMENT_TYPE_SIGNED = "Signed";
	public static final String DOCUMENT_TYPE_CERTIFICATE_OF_COMPLETION = "Certificate of Completion";
	
	public static final String DOCUMENT_STATUS_VOID = "voided";
	public static final String DOCUMENT_STATUS_COMPLETED = "completed";
	public static final String DOCUMENT_STATUS_SENT = "sent";
	public static final String DOCUMENT_STATUS_FAILED = "failed";

    public static String PREFIX_MODEL = "docusign";

    
    public static QName ASPECT_DIGITAL_SIGNATURE = QName.createQName(URI_MODEL, "digitalSignature");
    public static QName PROP_DOCUMENT_TYPE = QName.createQName(URI_MODEL, "documentType");
    public static QName PROP_COMPLETED_DATE = QName.createQName(URI_MODEL, "completedDate");
    public static QName PROP_SIGNED_DOCUMENT_ID = QName.createQName(URI_MODEL, "signedDocumentId");
    public static QName PROP_SENT_BY = QName.createQName(URI_MODEL, "sentBy");
    public static QName PROP_SENT_DATE = QName.createQName(URI_MODEL, "sentDate");
    public static QName PROP_STATUS = QName.createQName(URI_MODEL, "status");
    public static QName PROP_ENVELOPE_ID = QName.createQName(URI_MODEL, "envelopeId");
    public static QName PROP_RECIPIENT = QName.createQName(URI_MODEL, "recipient");
    public static QName PROP_SIGNER_EMAIL = QName.createQName(URI_MODEL, "signerEmail");
    public static QName PROP_SIGNER_NAME = QName.createQName(URI_MODEL, "signerName");
    public static QName PROP_CC_USER_ID = QName.createQName(URI_MODEL, "ccUserId");
    public static QName ASSOC_SIGNED_DOCUMENT_ASSOC = QName.createQName(URI_MODEL, "signedDocumentAssoc");
    public static QName ASSOC_SIGNED_DOCUMENT_TARGET = QName.createQName(URI_MODEL, "signedDocumentTargetAssoc");
    public static QName ASSOC_SUPPLEMENTAL_DOCUMENT = QName.createQName(URI_MODEL, "supplementalDocumentAssoc");
    
    
    public static QName ASPECT_SIGNED_DOCUMENT = QName.createQName(URI_MODEL, "signedDocumentAspect");
    public static QName PROP_CERTIFICATE_OF_COMPLETION_NODE_ID = QName.createQName(URI_MODEL, "certificateOfCompletionNodeId");
    public static QName ASSOC_CERTIFICATE_OF_COMPLETION = QName.createQName(URI_MODEL, "certificateOfCompletionAssoc");
    
    public static QName ASPECT_SIGNATURE_RESPONSE_DOCUMENT_ = QName.createQName(URI_MODEL, "signatureResponseDocumentAspect");
    public static QName PROP_SIGNATURE_RESPONSE_DOCUMENT_TYPE = QName.createQName(URI_MODEL, "signatureResponseDocumentType");
    
    public static QName ASPECT_APP_CONFIGURATION_ = QName.createQName(URI_MODEL, "appConfigurationAspect");
    public static QName PROP_DOCUSIGN_CONFIG_CLIENT_ID = QName.createQName(URI_MODEL, "clientId");
    public static QName PROP_DOCUSIGN_CONFIG_USER_ID = QName.createQName(URI_MODEL, "userId");
    public static QName PROP_DOCUSIGN_CONFIG_TARGET_ACCOUNT = QName.createQName(URI_MODEL, "targetAccount");
    public static QName PROP_DOCUSIGN_CONFIG_AUTH_SERVER_URL = QName.createQName(URI_MODEL, "authServerURL");
    public static QName PROP_DOCUSIGN_CONFIG_PRIVATE_KEY = QName.createQName(URI_MODEL, "privateKey");
    
    
}

