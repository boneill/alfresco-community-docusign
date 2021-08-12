<#escape x as jsonUtils.encodeJSONString(x)>
{
    <#if id??>
		"id" : "${id}",
		"status" : "${status}"
		
	</#if>				
}
</#escape>