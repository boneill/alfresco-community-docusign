<#escape x as jsonUtils.encodeJSONString(x)>
{
<#if error??>
	"error" : "${error}"
<#else>
		"control": {
	        "totalFound": "${totalFound}",
	        "blockNumber": "${blockNumber}",
	        "blockSize": "${blockSize}"
	    }
		<#if entries??>
		,
			"entries":
			[
										
				   <#list entries as node>
				    {
				    		"nodeId" :"${node.nodeId}",
				    		"nodeRef" :"${node.nodeRef}",
				    		"cm:name" :"${node.cm_name}",
							"docusign:documentType" :"${node.docusign_documentType}",
							<#if node.docusign_completedDate??>
								"docusign:completedDate" :"${node.docusign_completedDate}",
							</#if>
							"docusign:signedDocumentId" :  "${node.docusign_signedDocumentId}",
							<#if node.signedDocumentUrl??>
								"signedDocumentUrl":"${node.signedDocumentUrl}",
							</#if>
							"docusign:sentBy" :"${node.docusign_sentBy}",
							"docusign:sentDate" :"${node.docusign_sentDate}",
							"docusign:status" :"${node.docusign_status}",
							"docusign:envelopeId" :"${node.docusign_envelopeId}",
							<#if node.docusign_recipients??>
								"docusign:recipient":"${node.docusign_recipients}",
							</#if>
							"displayPath" :"${node.displayPath}",
							"path" :"${node.path}",
							"type":"${node.type}",
				    		"isFile":"${node.isFile?c}",
				    		"url":"${node.url}"
				    		

				    }<#if node_has_next>,</#if>
				   </#list>
				
			]
		</#if>
	
</#if>
}
</#escape>

