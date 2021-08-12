package au.com.seedim.docusign.job.checkstatus;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.log4j.Logger;

import au.com.seedim.docusign.model.DocusignModel;
import au.com.seedim.docusign.service.DocusignService;




public class CheckStatusJobExecutor {
  
  private static final Logger logger = Logger.getLogger(CheckStatusJobExecutor.class);
  
  private DocusignService docusignService;

private ActionService actionService;
  /**
   * Public API access
   */
  private ServiceRegistry serviceRegistry;

  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
      this.serviceRegistry = serviceRegistry;
  }
  
  public void execute() {
	  
	  
	  if (logger.isDebugEnabled()){logger.debug("CheckStatusJobExecutor: execute entered ");}
	  try {
		  Map<String, NodeRef> documentsMap = new HashMap<String, NodeRef>(100);
		  
		  String[] statusToCheck = new String[] {"sent", "delivered"};
		  List<String> statusList = Arrays.asList(statusToCheck);
		  List<NodeRef> docList = this.docusignService.getSignatureRequests(statusList, null, 2000, 0);
		  
		  if (logger.isDebugEnabled()){logger.debug("CheckStatusJobExecutor: Number of Docusign sent nodes to process " + docList.size() );}
		  
		  if(docList == null || docList.size() <= 0) {
			  
			  return;
		  }
		  
		  //get envelope Id for each document and add it t the list to be processed.
		  for(NodeRef docRef: docList ) {
			  String envelopeId = (String)this.serviceRegistry.getNodeService().getProperty(docRef, DocusignModel.PROP_ENVELOPE_ID);
			  if(envelopeId != null && !"".equals(envelopeId))
			  documentsMap.put(envelopeId, docRef);
		  }
		  
		  // get status for each from docusign
		 try {
			this.docusignService.processSentDocuments(documentsMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	  }finally {
		  if (logger.isDebugEnabled()){logger.debug("CheckStatusJobExecutor: execute exit ");}
	  }
	  
    
    //processNodes(synchSourceRootFolderNodes);
    
    
  }
  
  /**private void processNodes(List<NodeRef> nodes)
  {
    for (int i = 0; i < nodes.size(); i++)
    {
      // for each node call the action to process it as a pair
      if(nodes.get(i) != null){
        if (logger.isDebugEnabled()){logger.debug("Processing Sync Source Node: " + nodes.get(i));}
        callStartSynchAction(nodes.get(i));
      }
    }
  }**/
  private void processNodes(List<NodeRef> nodes)
  {
    for(NodeRef node : nodes){
      if (logger.isDebugEnabled()){logger.debug("Processing Synch Source Node: " + node);}
      callStartSynchAction(node);
    }
  }
  
  public void callStartSynchAction(NodeRef nodeRef) {
    boolean executeAsync = false;
    Map<String, Serializable> aParams = new HashMap<String, Serializable>();
    Action action = actionService.createAction("start-synch", aParams);
    if (action != null) {
       actionService.executeAction(action, nodeRef, true, executeAsync);
    } else {
       throw new RuntimeException("Could not create start-synch action");
    }
  }                  

    public DocusignService getDocusignService() {
		return docusignService;
    }
	
	public void setDocusignService(DocusignService docusignService) {
		this.docusignService = docusignService;
	}

  public ActionService getActionService() {
    return actionService;
  }

  public void setActionService(ActionService actionService) {
    this.actionService = actionService;
  }

}
