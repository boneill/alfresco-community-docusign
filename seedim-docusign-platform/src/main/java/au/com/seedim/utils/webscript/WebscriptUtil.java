package au.com.seedim.utils.webscript;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;




public class WebscriptUtil {
  
  private NamespaceService namespaceService;
  
  static final Logger logger = Logger.getLogger(WebscriptUtil.class);
  
  
  public WebscriptUtil(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
    
  }
  
  
  /**
   * Checek to see if a value exists for a dependent param if the master param has been set.
   * @param request
   * @param masterParam
   * @param dependentParam
   * @return
   */
  public static String dependentParameter(WebScriptRequest request, String masterParam, String dependentParam, String value){
    
    // only run this test if the master param is set
    if(request.getParameter(masterParam)!= null && !(StringUtils.isBlank(request.getParameter(masterParam)))){
    
      
      if((request.getParameter(dependentParam))!= null && !(StringUtils.isBlank(request.getParameter(dependentParam)))){
      
        value = request.getParameter(dependentParam);
        if(logger.isDebugEnabled()){logger.debug("Dependent value for param "+ dependentParam + " is set.");}
      }
      else{
        throw new WebScriptException(400,
                "Parameter " + dependentParam + " must have a value if a value is supplied for parameter " + masterParam);
      }
    }
    
    return value;
  }
   
   /**
    * Check if the parameter has been set and if not throw an exception
    * @param request
    * @param param
    * @param value
    * @return
    */
   public static String processParameter(WebScriptRequest request, String param, String value){
     if((request.getParameter(param))!= null && !(StringUtils.isBlank(request.getParameter(param)))){
       
         value = request.getParameter(param);
         if(logger.isDebugEnabled()){logger.debug("Passed value for: "+ param + " - " + value);}
     }else{
       throw new WebScriptException(400,
               "Invalid Parameter passed for " + param);
     }
     
     return value;
   }
   
   /**
    * Get non mandatory paramater
    * @param request
    * @param param
    * @param value
    * @return
    */
   public static String getOptionalParameter(WebScriptRequest request, String param, String value){
     
       
         value = request.getParameter(param);
         if(logger.isDebugEnabled()){logger.debug("Passed value for: "+ param + " - " + value);}
     
     
         return value;
   }
   
   /**
    * Check if the parameter has a value
    * 
    * @param request
    * 
    * @param param
    * @param value  - populate param value into this variable if it has a value.
    * 
    * @return boolean (true if param has value, false otherwise)
    */
   public static boolean hasParameterValue(WebScriptRequest request, String param, String value){
     
     if((request.getParameter(param))!= null && !(StringUtils.isBlank(request.getParameter(param)))){
       
         value = request.getParameter(param);
         if(logger.isDebugEnabled()){logger.debug("Passed value for: "+ param + " - " + value);}
     }else{
       return false;
     }
     
     return true;
   }
   
   
   
   
   public static boolean allowedParameterValue(String param, String value, String [] allowedVals){
     
     boolean allowed = false;
     if(value!= null && !StringUtils.isBlank(value)){
       
       
       for(int i = 0; i < allowedVals.length; i++){
         if(value.equalsIgnoreCase(allowedVals[i])){
           allowed = true;
           break;
         }
       }
       
       if(!allowed){
         if(logger.isDebugEnabled()){logger.debug("Invalid Parameter value passed for " + param + ". Allowed values " + allowedVals);}
     
         throw new WebScriptException(400,
               "Invalid Parameter value passed for " + param + ". Allowed values " + Arrays.toString(allowedVals));
       }
     }
     return allowed;
   }
   
   /**
    * Check if the parameter has been set in the json and if not throw an exception
    
    * @param JSONObject
    * @param param
    * @return
 * @throws JSONException 
    */
   public static String processParameter(Object jsonData, String param) throws JSONException{
     
     String value = null;
     
     if (jsonData != null && jsonData instanceof JSONObject)
     {
         // verify param  
         Object valObj = ((JSONObject) jsonData).get(param);
         if (valObj != null)
         {
           value = valObj.toString();  
           
         }else{
           throw new WebScriptException(400,
               "Invalid Parameter passed for " + param);
         }
     }else{
       throw new WebScriptException(400, "Invalid JSON Values passed into webscript in body");
     }
     
     
     return value;
   }

   /**
    * Check if the parameter has been set in the json and if its mandatory and not set then throw an exception
    
    * @param JSONObject
    * @param param
    * @param mandatory
    * @return
 * @throws JSONException 
    */
   public static String processParameter(Object jsonData, String param, boolean mandatory) throws JSONException{
     
     String value = null;
     
     if (jsonData != null && jsonData instanceof JSONObject)
     {
         // verify param  
         Object valObj = ((JSONObject) jsonData).get(param);
         if (valObj != null)
         {
           value = valObj.toString();  
           logger.debug("value " + value);
           
         }else{
           if(mandatory){
             logger.debug("Not mandatory");
             throw new WebScriptException(400,
               "Invalid Parameter passed for " + param);
           }
         }
     }else{
       throw new WebScriptException(400, "Invalid JSON Values passed into webscript in body for param " + param);
     }
     
     //logger.debug(param + " value " + value);
     
     return value;
   
   }
  
   public static Object processMultivalueParamter(Object jsonData, String param, boolean mandatory) throws JSONException {
	   
	   Object value = null;
	   logger.debug("Processing param: " + param);
	   if (jsonData != null && jsonData instanceof JSONObject)
	     {
	         // verify param  
	         Object valObj = ((JSONObject) jsonData).get(param);
	         logger.debug("Multi value JSONObject " + valObj);
	         
	         if (valObj != null)
	         {
	        	 if (valObj instanceof String)
                 {
	        		 logger.debug("Multi value JSONString " + valObj);
                     if (((String) valObj).length() == 0)
                     {
                         // empty string for multi-valued properties
                         // should be stored as null
                         value = null;
                     }
                     else
                     {
                         // if value is a String convert to List of
                         // String
                         List<String> list = Arrays.asList(((String)valObj).split(",", -1));

                         // persist the List
                         value = list;
                     }
                 }
                 else if (valObj instanceof JSONArray)
                 {
                     // if value is a JSONArray convert to List of Object
                	 logger.debug("Multi value JSONArray " + valObj);
                     JSONArray jsonArr = (JSONArray) valObj;
                     int arrLength = jsonArr.size();
                     List<String> list = new ArrayList<String>(arrLength);
                     for (int x = 0; x < arrLength; x++)
					 {
					     list.add((String) jsonArr.get(x));
					 }

                     // persist the list
                     
                     value = list;
                     logger.debug("Multi value field " + value.toString());
                 }
                 else if(valObj.toString().startsWith("[") && valObj.toString().endsWith("]")) {
                	 
                	 logger.debug("Multi value JSONArray " + valObj);
                	 JSONArray jsonArr = (JSONArray) valObj;
                     int arrLength = jsonArr.size();
                     List<String> list = new ArrayList<String>(arrLength);
                     for (int x = 0; x < arrLength; x++)
					 {
					     list.add((String) jsonArr.get(x));
					 }

                     // persist the list
                     
                     value = list;
                     logger.debug("Multi value field " + value.toString());
                 }
	           
	         }else{
	           if(mandatory){
	             logger.debug("Not mandatory");
	             throw new WebScriptException(400,
	               "Invalid Parameter passed for " + param);
	           }
	         }
	     }else{
	       throw new WebScriptException(400, "Invalid JSON Values passed into webscript in body");
	     }
	  
	  return value; 
   }



public static List<HashMap<String, Object>> processArrayOfObjectsParamter(Object jsonData, String param, boolean mandatory) throws JSONException {
	   
	List<HashMap<String, Object>> listOfMaps = null;
	
	   logger.debug("Processing param: " + param);
	   
	   if (jsonData != null && jsonData instanceof JSONObject)
	   {
	         // verify param  
	         Object valObj = ((JSONObject) jsonData).get(param);
	         logger.debug("Multi value JSONObject " + valObj);
	         if (valObj instanceof JSONArray)
             {
                     // if value is a JSONArray convert to List of Object
                	 logger.debug("Multi value JSONArray " + valObj);
                	 

                	 ObjectMapper objectMapper = new ObjectMapper();
                	 String jsonString = valObj.toString(); 
                	 try {
						listOfMaps = objectMapper.readValue(jsonString,
						         new TypeReference<List<HashMap<String, Object>>>(){});
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw new WebScriptException(400, "Invalid JSON Values passed into webscript in body for param " + param);
					}
                	 	 
                     logger.debug("Array of Hashmaps " + listOfMaps.toString());
             }else {
            	 
            	 
            	 throw new WebScriptException(400, "Invalid JSON Values passed into webscript in body for param " + param);                 
             }
	           
         }
	         
	         if(mandatory){
	             if(listOfMaps == null || listOfMaps.size() < 1) {
	             
	            	 throw new WebScriptException(400,
	               "Invalid Parameter passed for " + param);
	           }
     }
	     
	  
	  return listOfMaps; 
   }

   
   
   
   public static int getOptionalIntegerParameter(WebScriptRequest request, String paramName) {
	// TODO Auto-generated method stub
	String value = getOptionalParameter(request, paramName, null );
	return parseInt(value, paramName);
	
}

private static int parseInt(String value, String paramName) {
	// TODO Auto-generated method stub
	if(value == null || value.length() <= 0) {
		return 0;
	}
	
	try {
		return Integer.parseInt(value);
		
	}catch(NumberFormatException e) {
		throw new WebScriptException(400,
	               "Invalid integer value passed for Parameter passed for " + paramName);
	}
	
}


/**
 * Check if the parameter has been set in the json and if its a valid integer and mandatory and if not throw an exception
 
 * @param JSONObject
 * @param param
 * @param mandatory
 * @return
* @throws JSONException 
 */
public static int processIntegerParameter(Object jsonData, String param, boolean mandatory) throws JSONException{
	String value = processParameter(jsonData, param, mandatory);
	return parseInt(value, param);
}

   
  
    
  
}
