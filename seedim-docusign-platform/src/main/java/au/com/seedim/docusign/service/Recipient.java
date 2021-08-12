package au.com.seedim.docusign.service;

import java.util.HashMap;

public class Recipient {
	
	public String name;
	public String email;
	public String action; // sign or cc
	public int order;
	
	public Recipient(int order, String name, String email, String action) {
		this.order = order;
		this.name = name;
		this.email = email;
		this.action = action;
	}
	
	/**
	 * create a recipient object based on value retrieved from the Repository, it will be a compound value with expected format.
	 * order=3|name=Kavi tanakoor|email=ktanakoor@gmail.com|action=sign 
	 * 
	 * @param propertyValue
	 */
	public Recipient(String propertyValue) {
		
		System.out.println("propertyValue: " + propertyValue);
		
		String[] keyValuePairs = propertyValue.split("\\|");
		
		HashMap map = new HashMap<String, String>();
		for(String pair : keyValuePairs)                        //iterate over the pairs
		{
		    String[] entry = pair.split("=");
		    
		    //split the pairs to get key and value 
		    map.put(entry[0].trim(), entry[1].trim());          //add them to the hashmap and trim whitespaces
		}
		
		
		this.name = (String)map.get("name");
		this.email = (String)map.get("email");;
		this.action = (String)map.get("action");;
		if(map.containsKey("order")) {
			this.order = Integer.parseInt((String)map.get("order"));
		}
	}

	@Override
	public String toString() {
		return "Recipient [name=" + name + ", email=" + email + ", action=" + action + "]";
	}
	
	/**
	 * Creates a display value for the string
	 * @return string with format for display eg 1. Kavi tanakoor - ktanakoor@gmail.com (Signer)
	 */
	public String parseValue() {
		return String.valueOf(order) + ". " + name + " - " + email + "(" + action + ")"; 
	}
	
	public String getPropertyValue(){
		StringBuffer buff = new StringBuffer();
		buff.append("order=" + String.valueOf(order) + "|");
		buff.append("name=" + name + "|");
		buff.append("email=" + email + "|");
		buff.append("action=" + action);
		
		return buff.toString();	
		
	}

}
