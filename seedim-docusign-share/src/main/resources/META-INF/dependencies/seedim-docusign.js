(function()
{
	

Alfresco.forms.validation.checkExpireDays = function checkExpireDays(field, args, event, form, silent, message) {
	
    var valid = true;
    if (field.value && field.value.length > 0){
	    valid = YAHOO.lang.trim(field.value).length !== 0;
	    if (valid) {
	        //var reg = /^[1-9][0-9]{0,2}$/;
	        //var reg = /^([1-9]|[1-4][0-9]|50)$/
	    	
	        var reg = /^([1-9]|[1-8][0-9]|90)$/
	        valid = reg.test(field.value);
	    }
    }
    return valid;
};
Alfresco.forms.validation.checkExpireWarn = function checkExpireWarn(field, args, event, form, silent, message) {
	
    var valid = true;
    
    if (field.value && field.value.length > 0){
	    valid = YAHOO.lang.trim(field.value).length !== 0;
	    if (valid) {
	
	        var reg = /^([1-9]|1[0-9]|2[01])$/
	        valid = reg.test(field.value);
	    }
    }
    return valid;
};
Alfresco.forms.validation.checkReminderDelay = function checkReminderDelay(field, args, event, form, silent, message) {
	
    var valid = true;
    if (field.value && field.value.length > 0){
	    valid = YAHOO.lang.trim(field.value).length !== 0;
	    if (valid) {
	
	        var reg = /^([1-9]|[1-7][0-9]|80)$/
	        valid = reg.test(field.value);
	    }
    }
    return valid;
};
Alfresco.forms.validation.checkReminderFrequency = function checkReminderFrequency(field, args, event, form, silent, message) {
	
    var valid = true;
    if (field.value && field.value.length > 0){
	    valid = YAHOO.lang.trim(field.value).length !== 0;
	    if (valid) {
	
	        var reg = /^([1-9]|1[0-9]|2[01])$/
	        valid = reg.test(field.value);
	    }
    }
    return valid;
};
/**Alfresco.doclib.Actions.prototype = {
	
		signatureActionFailure: function signatureActionFailure(response, obj)
		{
	       Alfresco.util.PopupManager.displayMessage(
	       {
	           text: this.msg("Hello world!", response)

	       });
		}
	
	};**/
	/**Alfresco.doclib.Actions.prototype.signatureActionFailure = function signatureActionFailure(response)
	{
	       Alfresco.util.PopupManager.displayMessage(
	       {
	           text: this.msg("Hello world!", response)

	       });
		}**/
	
	/**var signatureActionFailure = function signatureActionFailure(response)
	{
	       Alfresco.util.PopupManager.displayMessage(
	       {
	           text: this.msg("Hello world!", response)

	       });
	};
	YAHOO.Bubbling.fire("registerAction",
		    {
		        actionName: "signatureActionFailure",
		        fn: signatureActionFailure
		    });**/

YAHOO.Bubbling.fire("registerAction",
	    {
	        actionName: "onActionFormDialogSeedim",
	        fn: function dlA_onActionWaitingFormDialog(record, owner)
	        {
	               // Get action & params and start create the config for displayForm
	               var action = this.getAction(record, owner),
	                  params = action.params,
	                  config =
	                  {
	                     title: this.msg(action.label)
	                  },
	                  displayName = record.displayName;
	 
	               // Make sure we don't pass the function as a form parameter
	               delete params["function"];
	 
	               // Add configured success callback
	               var success = params["success"];
	               delete params["success"];
	               //var failureMessage = params["failureMessage"];
	               //delete params["failureMessage"];
	               config.success =
	               {
	                  fn: function(response, obj)
	                  {
	                     // Invoke callback if configured and available
	                     if (YAHOO.lang.isFunction(this[success]))
	                     {
	                        this[success].call(this, response, obj);
	                     }

	                     // Fire metadataRefresh so other components may update themselves
	                     YAHOO.Bubbling.fire("metadataRefresh", obj);
	                  },
	                  obj: record,
	                  scope: this
	               };

	               // Add configure success message
	               if (params.successMessage)
	               {
	                  config.successMessage = this.msg(params.successMessage, displayName);
	                  delete params["successMessage"];
	               }

	               // Add configured failure callback
	               //if (YAHOO.lang.isFunction(this[params.failure]))
	               /**if (params.failure)
	               {
	                  config.failure =
	                  {
	                		  fn: function(response, obj)
	    	                  {
	                			  Alfresco.util.PopupManager.displayMessage(
	                				       {
	                				           //text: this.msg("Hello world!", response)
	                				    	   text: this.msg("Hello world!")
	                				       });
	    	                  },
	                     obj: record,
	                     scope: this
	                  };
	                  delete params["failure"];
	               }**/
	            // Add configured failure callback
	               if (YAHOO.lang.isFunction(this[params.failure]))
	               {
	                  config.failure =
	                  {
	                     fn: this[params.failure],
	                     obj: record,
	                     scope: this
	                  };
	                  delete params["failure"];
	               }
	               // Add configure success message
	               if (params.failureMessage)
	               {
	                  //config.failureMessage = this.msg(params.failureMessage, displayName);
	                  config.failureMessage = this.msg(params.failureMessage, "Signature Request Failed");
	                  delete params["failureMessage"];
	               }
	 
	               // Use the remaining properties as form properties
	               config.properties = params;
	                
	               // Finally display form as dialog
	               Alfresco.util.PopupManager.displayForm(config);
	                
	               // Waiting dialog
	               /**waitDialog = Alfresco.util.PopupManager.displayMessage({
	                    text : this.msg("seedim.action.waiting.message"),
	                    spanClass : "wait",
	                    displayTime : 0
	               });
	                
	               YAHOO.Bubbling.on("beforeFormRuntimeInit", function PopupManager_displayForm_onBeforeFormRuntimeInit(layer, args)
	               {
	                    
	                   // Clear wait dialog on close button click
	                   var panel = document.getElementById(config.properties.htmlid + "-panel");
	                   var closeButton = panel.getElementsByClassName("container-close")[0];
	                   if (closeButton) {
	                       closeButton.addEventListener("click", function(){
	                          waitDialog.destroy();
	                       });
	                   }
	                    
	                   // Clear wait dialog on cancel button click
	                   var cancelButton = document.getElementById(config.properties.htmlid + "-form-cancel-button");
	                   if (cancelButton) {
	                      cancelButton.addEventListener("click", function(){
	                          waitDialog.destroy();
	                      });
	                       
	                   }
	               },
	               {
	                  config: config
	               });**/
	                
	            }
	 
	    });

})();