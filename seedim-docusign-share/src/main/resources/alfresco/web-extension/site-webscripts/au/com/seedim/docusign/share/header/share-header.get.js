//Main Menu
var mainMenu = widgetUtils.findObject(model.jsonModel.widgets, "id", "HEADER_APP_MENU_BAR");

var userid = user.name;


var signatureDocumentLink = {
	    
	    id: "SEEDIM_SIGNATURE_DOCUMENT_LINK",
	    name: "alfresco/menus/AlfMenuBarItem",
	    config: {
	       id: "SEEDIM_SIGNATURE_DOCUMENT_LINK",
	       label: "Signatures",
	       targetUrl: "hdp/ws/signature-documents"
	    }
};
var mySignatureDocumentLink = {
	    
	    id: "SEEDIM_SIGNATURE_DOCUMENT_LINK",
	    name: "alfresco/menus/AlfMenuBarItem",
	    config: {
	       id: "SEEDIM_SIGNATURE_DOCUMENT_LINK",
	       label: "My Documents",
	       targetUrl: "hdp/ws/signature-documents?sentBy=" + userid
	    }
};
var allSignatureDocumentLink = {
	    
	    id: "SEEDIM_SIGNATURE_DOCUMENT_LINK",
	    name: "alfresco/menus/AlfMenuBarItem",
	    config: {
	       id: "SEEDIM_SIGNATURE_DOCUMENT_LINK",
	       label: "Everyone's Documents",
	       targetUrl: "hdp/ws/signature-documents?sentBy=*"
	    }
};

var signatureMenu = {
	    id: "SEEDIM_SIGNATURE_MENU",
	    name: "alfresco/header/AlfMenuBarPopup",
	    config: {
	      id: "SEEDIM_SIGNATURE_MENU",
	            label: "Signatures",
	      widgets: [
	        {   
	                    name: "alfresco/menus/AlfMenuGroup",
	                    config: {
	                       widgets: [
	                    	   //signatureDocumentLink,
	                    	   mySignatureDocumentLink,
	                    	   allSignatureDocumentLink
	                       ]
	                    }
	              } 
	         ]
	    }
	    
	};

if(mainMenu){
	
	mainMenu.config.widgets.push(signatureMenu);
}