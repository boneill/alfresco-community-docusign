package au.com.seedim.docusign.repo.behaviour;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.copy.CopyBehaviourCallback.AssocCopySourceAction;
import org.alfresco.repo.copy.CopyBehaviourCallback.AssocCopyTargetAction;
import org.alfresco.repo.copy.CopyBehaviourCallback.CopyAssociationDetails;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;
import java.io.Serializable;
import java.util.Map;
import java.util.Collections;

import au.com.seedim.docusign.model.DocusignModel;

public class OnCopyDocusignDocument implements OnCopyNodePolicy {
	
	private static final Logger logger = Logger.getLogger(OnCopyDocusignDocument.class);
	
	private boolean enabled = false;
	protected PolicyComponent policyComponent;
	protected NodeService nodeService;
	
	
	public void init() {
	   if (!enabled) {
	     return;
	   }

	   
	   /**
        * Bind policies
        */
       this.policyComponent.bindClassBehaviour(OnCopyNodePolicy.QNAME , 
    		   DocusignModel.ASPECT_DIGITAL_SIGNATURE, 
           
    		   new JavaBehaviour(this, "getCopyCallback", NotificationFrequency.EVERY_EVENT));
		
	   logger.debug("Initialised : OnCopyNodePolicy");
	  }
	
	/**
	 * Extends the default copy behaviour to remove the Docusign Aspect from the target node created from the copy
	 */
	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
		logger.debug("getCopyCallback : OnCopyNodePolicy: Entered"); 
		//this.nodeService.removeAspect(copyDetails.getTargetNodeRef(), DocusignModel.ASPECT_DIGITAL_SIGNATURE);
		return DocusignContentCopyBehaviourCallback.INSTANCE;
    }

	/**
     * 
     * 
     * @author Brian O'Neill
     * @since 6.2
     */
    protected static class DocusignContentCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new DocusignContentCopyBehaviourCallback();
        
        /**
         * @return          Returns an empty map
         */
        @Override
        public Map<QName, Serializable> getCopyProperties(
                QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties)
        {
            return Collections.emptyMap();
        }
        
        /**
         * Default behaviour:<br/>
         * * AssocCopySourceAction.COPY_REMOVE_EXISTING<br/>
         * * AssocCopyTargetAction.USE_COPIED_OTHERWISE_ORIGINAL_TARGET
         */
        @Override
        public Pair<AssocCopySourceAction, AssocCopyTargetAction> getAssociationCopyAction(
                QName classQName,
                CopyDetails copyDetails,
                CopyAssociationDetails assocCopyDetails)
        {
            return new Pair<AssocCopySourceAction, AssocCopyTargetAction>(
                    AssocCopySourceAction.IGNORE,
                    AssocCopyTargetAction.USE_COPIED_TARGET);
        }
        
        /**
         * Don't copy the transferred aspect.
         *
         * @return          Returns <tt>true</tt> always
         */
        @Override
        public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
        {
        	logger.debug("getMustCopy" + classQName.getLocalName());
            if(classQName.equals(DocusignModel.ASPECT_DIGITAL_SIGNATURE))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        

    }

 
    
	
    public void setPolicyComponent(PolicyComponent policyComponent) {
	    this.policyComponent = policyComponent;
	  }

	  public void setEnabled(boolean enabled) {
	    this.enabled = enabled;
	  }
	  
	  public NodeService getNodeService() {
		return nodeService;
	  }


	  public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	  }

      
  }

