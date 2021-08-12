package au.com.seedim.docusign.action;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;

import au.com.seedim.docusign.service.DocusignService;

public class ProcessSignedDocument extends ActionExecuterAbstractBase {
	
	private static final Logger logger = Logger.getLogger(ProcessSignedDocument.class);
	
	
	private DocusignService docusignService;

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		if (logger.isDebugEnabled()) logger.debug("Entered ProcessSignedDocument executeImpl method");
		
		try {
			this.docusignService.getSignedDocument(actionedUponNodeRef);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if (logger.isDebugEnabled()) logger.debug("Exited ProcessSignedDocument executeImpl method");
		
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		
		paramList.add(new ParameterDefinitionImpl("a-parameter", DataTypeDefinition.TEXT, false, getParamDisplayLabel("a-parameter")));		
		
	}

	public DocusignService getDocusignService() {
		return docusignService;
	}

	public void setDocusignService(DocusignService docusignService) {
		this.docusignService = docusignService;
	}

}
