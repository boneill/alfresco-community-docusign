define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/topics",
        "dojo/_base/lang",
        "alfresco/core/CoreXhr",
        "service/constants/Default",
        "dojo/_base/array"],
        function(declare, Core, topics, lang, CoreXhr, AlfConstants, array) {

  return declare([Core,CoreXhr], {

    constructor: function seedim_SignatureDocumentService__constructor(args) {
      
      this.alfLog("log", "SignatureDocumentService constructor", args, this);
      this.alfPublish("SEEDIM_SHOW_SIGNED_DOCUMENT_ACTION", {showSignedDocument: false});
            
      lang.mixin(this, args);
     
      this.alfSubscribe("SEEDIM_GET_SIGNATURE_DOCUMENTS", lang.hitch(this, this.getSignatureDocuments));  
      //this.alfSubscribe("SEEDIM_GET_SIGNATURE_DOCUMENTS_SENTBY", lang.hitch(this, this.getSignatureDocuments));  
      this.alfSubscribe("SEEDIM_GO_TO_SIGNED_DOCUMENT", lang.hitch(this, this.goToSignedDocument)); 
      
    },
    
    status: null,
    signatureDocuments: null,
    sentBy: null,
    showSignedDocument: null,
    
    
    getSignatureDocuments: function seedim_SignatureDocumentService__getSignatureDocuments(payload) {     
      
      if(payload){
        this.alfLog("log", "SignatureDocumentService: A request will be made to get signature documents", payload, this);
        //var currentPage = window.location.href;
        var urlParams = new URLSearchParams(window.location.search);
        this.sentBy = urlParams.get('sentBy');
        this.status = payload.status;
        this.showSignedDocument = payload.showSignedDocument;
        this.alfLog("log", "payload.showSignedDocument: ", payload.showSignedDocument, this);
        if(this.showSignedDocument != null){
        	this.alfPublish("SEEDIM_SHOW_SIGNED_DOCUMENT_ACTION", {showSignedDocument: this.showSignedDocument});
        	//this.alfPublish("ALF_HIDE_WIDGET", {hide: this.showSignedDocument});
        }
        //this.sentBy = payload.sentBy;
        
        this.alfLog("log", "payload.sentBy: ", payload.sentBy, this);
        var webscriptUrl = AlfConstants.PROXY_URI + "seedim/docusign/signaturerequests?status=" + encodeURIComponent(this.status);
        if(this.sentBy != null){
        	webscriptUrl = AlfConstants.PROXY_URI + "seedim/docusign/signaturerequests?status=" + encodeURIComponent(this.status) + "&sentBy=" + encodeURIComponent(this.sentBy);
        	//webscriptUrl = AlfConstants.PROXY_URI + "seedim/docusign/signaturerequests?status=*&sentBy=" + encodeURIComponent(this.sentBy);
        }
                
        this.serviceXhr({
        
        	//url: AlfConstants.PROXY_URI + "api/people?filter=" + encodeURIComponent(this.status),
        	//url: AlfConstants.PROXY_URI + "seedim/docusign/signaturerequests?status=" + encodeURIComponent(this.status) + "&sentBy=admin&blockSize=1",
          url: webscriptUrl,
          method: "GET",
          data: {},     
          successCallback: this.onGetSignatureDocumentsSuccess,
          failureCallback: this.onGetSignatureDocumentsFailure,
          callbackScope: this
        });

      }
    },
    
    onGetSignatureDocumentsSuccess: function seedim_SignatureDocumentService__onGetSignatureDocumentsSuccess(response,originalRequestConfig) {
    
      this.alfLog("log", "onGetSignatureDocumentsSuccess callback reached");
      this.signatureDocuments = response;
      this.alfPublish("SEEDIM_GET_SIGNATURE_DOCUMENTS_SUCCESS", response);   
      

    },
    onGetSignatureDocumentsFailure: function seedim_SignatureDocumentService__onGetSignatureDocumentsFailure(response,originalRequestConfig) {
        
        this.alfLog("log", "onGetSignatureDocumentsFailure callback reached");
        this.signatureDocuments = response;
        this.alfPublish(topics.DISPLAY_NOTIFICATION, {
            message: "Error processing request.  Could not retrieve signature documents " + response, autoClose:false});  
        

      },
      
      goToSignedDocument: function seedim_SignatureDocumentService__goToSignedDocument(payload) {
    	  
    	  if(payload.signedDocumentUrl != null){
    		  this.alfServicePublish("ALF_NAVIGATE_TO_PAGE", {
    			  "url": payload.signedDocumentUrl,
    	          "type": "PAGE_RELATIVE",
    	        });
    	  }else{
    		  this.alfPublish(topics.DISPLAY_NOTIFICATION, {
                  message: "No Signed Document Available", autoClose:false});
    		  
    	  }
    	  
    	  
      }

        
  });
  
  
});