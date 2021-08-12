
//Inject Services on page
model.jsonModel = {
    services: [
      "alfresco/services/CrudService",
      "alfresco/services/ActionService",
      "alfresco/services/SearchService",
      "alfresco/services/DocumentService",
      "alfresco/services/NavigationService",
      "alfresco/services/NotificationService",
      "alfresco/services/ContentService",
      "alfresco/services/DialogService",
      "docusign/SignatureDocumentsService"

      /**,{
        name: "alfresco/services/LoggingService",
        config: {
          loggingPreferences: {
            enabled: true,
            all: true
          }
        }
      }**/
    ]
    
};

//Title Control Widget
var title = {
  
  id: "SET_PAGE_TITLE",
  name: "alfresco/header/SetTitle",
  config: {
	  additionalCssClasses: "no-margin",
      title: "Signature Documents"
  }
}

var showAction = {
	    initialValue: false,
	    rules: [
	      {
	        topic: 'SEEDIM_SHOW_SIGNED_DOCUMENT_ACTION',
	        attribute: 'showSignedDocument',
	        is: [true],
	        isNot: [false]
	      }
	    ]
	};

var statusOptions = [];
var sentStatusControl = {
		   id: "DOCUSIGN_STATUS_SENT",
		   name: "alfresco/menus/AlfCheckableMenuItem",
		   config: {
		      label: "Sent",
		      value: "sent",
		      group: "DOCUSIGN_STATUS",
		      publishTopic: "SEEDIM_GET_SIGNATURE_DOCUMENTS",
		      checked: true,
		      hashName: "status",
		      publishPayload: {
		         status:"sent",
		         showSignedDocument:false
		      }
		   }
		}

statusOptions.push(
		sentStatusControl
	);



statusOptions.push({
	   id: "DOCUSIGN_STATUS_COMPLETED",
	   name: "alfresco/menus/AlfCheckableMenuItem",
	   config: {
	      label: "Completed",
	      value: "completed",
	      group: "DOCUSIGN_STATUS",
	      publishTopic: "SEEDIM_GET_SIGNATURE_DOCUMENTS",
	      checked: false,
	      hashName: "status",
	      publishPayload: {
	         status:"completed",
	         showSignedDocument:true
	      }
	   }
	});

statusOptions.push({
	   id: "DOCUSIGN_STATUS_ALL",
	   name: "alfresco/menus/AlfCheckableMenuItem",
	   config: {
	      label: "All",
	      value: "*",
	      group: "DOCUSIGN_STATUS",
	      publishTopic: "SEEDIM_GET_SIGNATURE_DOCUMENTS",
	      checked: false,
	      hashName: "status",
	      publishPayload: {
	         status:"*",
	         showSignedDocument:false
	      }
	   }
	});

statusOptions.push({
	   id: "DOCUSIGN_STATUS_VOIDED",
	   name: "alfresco/menus/AlfCheckableMenuItem",
	   config: {
	      label: "Voided",
	      value: "voided",
	      group: "DOCUSIGN_STATUS",
	      publishTopic: "SEEDIM_GET_SIGNATURE_DOCUMENTS",
	      checked: false,
	      hashName: "status",
	      publishPayload: {
	         status:"voided",
	         showSignedDocument:false
	      }
	   }
	});



/**var sentByOptions = [];

sentByOptions.push({
		  id: "DOCUSIGN_SENTBY_ME",
		  name: "alfresco/menus/AlfCheckableMenuItem",
		  config: {
		     label: "My Documents",
		     value: user.name,
		     group: "DOCUSIGN_SENTBY",
		     publishTopic: "SEEDIM_GET_SIGNATURE_DOCUMENTS",
		     checked: false,
		     hashName: "sentBy",
		     publishPayload: {
		        sentBy:user.name
		     }
		   }
		});

sentByOptions.push({
	  id: "DOCUSIGN_SENTBY_ALL",
	  name: "alfresco/menus/AlfCheckableMenuItem",
	  config: {
	     label: "Everyone's Documents",
	     value: "*",
	     group: "DOCUSIGN_SENTBY",
	     publishTopic: "SEEDIM_GET_SIGNATURE_DOCUMENTS",
	     checked: false,
	     hashName: "sentBy",
	     publishPayload: {
	        sentBy:"*"
	     }
	   }
	});**/


var docusignStatusControl = {
    id: "DOCUSIGN_TOP_MENU_BAR_STATUS_MENU_BAR",
    name: "alfresco/menus/AlfMenuBar",
    config: {
       widgets: [
          {
             id: "DOCUSIGN_STATUS_SELECTION_MENU",
             name: "alfresco/menus/AlfMenuBarSelect",
             config: {
                selectionTopic: "SEEDIM_GET_SIGNATURE_DOCUMENTS",
                widgets: [
                   {
                      id: "DOCUSIGN_STATUS_SELECTION_MENU_GROUP",
                      name: "alfresco/menus/AlfMenuGroup",
                      config: {
                         widgets: statusOptions
                      }
                   }
                ]
             }
          }
       ]
    }
};
/**var docusignSentByControl = {
	    id: "DOCUSIGN_TOP_MENU_BAR_SENTBY_MENU_BAR",
	    name: "alfresco/menus/AlfMenuBar",
	    config: {
	       widgets: [
	          {
	             id: "DOCUSIGN_SENTBY_SELECTION_MENU",
	             name: "alfresco/menus/AlfMenuBarSelect",
	             config: {
	                selectionTopic: "SEEDIM_GET_SIGNATURE_DOCUMENTS",
	                widgets: [
	                   {
	                      id: "DOCUSIGN_SENTBY_SELECTION_MENU_GROUP",
	                      name: "alfresco/menus/AlfMenuGroup",
	                      config: {
	                         widgets: sentByOptions
	                      }
	                   }
	                ]
	             }
	          }
	       ]
	    }
	};**/

var docusignFilterControl = {
		   id: "DOCUSIGN_TOP_MENU_BAR",
		   name: "alfresco/layout/LeftAndRight",
		   config: {
			  widgetsRight: [
		    	  docusignStatusControl
		    	  //,docusignSentByControl
		      ]
		   }
		};
var sortControl = {
        name: "alfresco/lists/SortFieldSelect",
        config: {
          label: "Sort By",
          sortFieldOptions: [
            { label: "Document Name", value: "cm:name" },
            { label: "Sent Date", value: "docusign:sentDate" }
          ]
        }
      }


var goToSignedDocument = {
		  name: "alfresco/renderers/PropertyLink",
		  config: {
		    //visibilityConfig: showAction,
		    currentItem: {
		      label: "Go to Signed Document"
		    },
		    propertyToRender: "label",
		    useCurrentItemAsPayload: false,
		    publishTopic: "ALF_NAVIGATE_TO_PAGE",
		    publishPayloadType: "PROCESS",
		    publishPayloadModifiers: ["processCurrentItemTokens"],
		    publishPayload: {
		    	url: "{signedDocumentUrl}",
				type: "PAGE_RELATIVE"
		    }
		  }
		};
//AlfList widget
var list = {
	  //name: "alfresco/lists/AlfList",
	  name:"alfresco/lists/AlfSortablePaginatedList",
	  config: {
		noDataMessage: "No Signature Documents",
	    dataFailureMessage: "No Signature Documents",
	    //pubSubScope: "SIGNATURE_PAGE_",
	    currentPageSize: 25,
	    //loadDataPublishTopic: "ALF_CRUD_GET_ALL",
	    loadDataPublishTopic: "SEEDIM_GET_SIGNATURE_DOCUMENTS",
	    loadDataPublishPayload: {
	        status: sentStatusControl.config.value,
	        showSignedDocument:false
	        //,sentBy: sentBy

	      },
	    itemsProperty: "entries"
	    /**widgets: [
	      {
	        name: "alfresco/lists/views/HtmlListView",
	        config: {
	          propertyToRender: "userName"
	        }
	      }
	    ]**/
	  }
	}

//AlfListView
var views = [
  {
    //name: "alfresco/documentlibrary/views/AlfDocumentListView",
    name: "alfresco/lists/views/AlfListView",
      config: {
        noItemsMessage: "No Signature Documents",
        additionalCssClasses: "bordered",
        widgetsForHeader: [
          /**{
             name: "alfresco/lists/views/layouts/HeaderCell",
             config: {
                label: " "
             }
          },**/
          {
              name: "alfresco/lists/views/layouts/HeaderCell",
              config: {
                 label: " "
              }
           },
          {
                name: "alfresco/lists/views/layouts/HeaderCell",
                config: {
                   label: "Name"
                }
           },
           {
               name: "alfresco/lists/views/layouts/HeaderCell",
               config: {
                  label: "Request Status"                                                         
               }
           },
           {
               name: "alfresco/lists/views/layouts/HeaderCell",
               config: {
                  label: "Recipient"                                                         
               }
           },
           {
               name: "alfresco/lists/views/layouts/HeaderCell",
               config: {
                  label: "Sent By"
               }
           },
           {
               name: "alfresco/lists/views/layouts/HeaderCell",
               config: {
                  label: "Sent Date"                                                         
               }
           },
           {
               name: "alfresco/lists/views/layouts/HeaderCell",
               config: {
                  label: "Completed On"                                                         
               }
           },
           {
             name: "alfresco/lists/views/layouts/HeaderCell",
             config: {
                label: "",                                         
              
             }
        }
          ],

      widgets: [
        {
          id: "VIEW_ROW",
          name: "alfresco/lists/views/layouts/Row",
          config: {
            additionalCssClasses: "mediumpad",
            widgets: [
              
              /**{
                name: "alfresco/lists/views/layouts/Cell",
                config: {
                  additionalCssClasses: "mediumpad",
                  width:"10px",
                  widgets: [
                    {
                      name: "alfresco/renderers/Selector"
                    }
                  ]
                }
              },**/
              {
                name: "alfresco/lists/views/layouts/Cell",
                config: {
                  additionalCssClasses: "mediumpad",
                  width:"17px",
                  widgets: [
                    {                    
                      name: "alfresco/renderers/Thumbnail",
                      config: {
                        dimensions: {
                           w: "16px",
                           h: "16px",
                           margins: "1px"
                        }
                        
                      }
                    }
                  ]
                }
              },
              {
                name: "alfresco/lists/views/layouts/Cell",
                config: {
                  additionalCssClasses: "mediumpad",
                  widgets: [
                    /**{
                      id: "SIGNATURE_DOCUMENT_NAME",
                      name: "alfresco/renderers/Property",
                      config: {
                          propertyToRender: "cm:name"
                      }
                    }**/
                	{
                		id: "SIGNATURE_DOCUMENT_NAME",
                		name: "alfresco/renderers/PropertyLink",
            			config: {
            				propertyToRender: "cm:name",
            				publishTopic: "ALF_NAVIGATE_TO_PAGE",
            				publishPayloadType: "PROCESS",
            				useCurrentItemAsPayload: false,
            				publishPayloadModifiers: ["processCurrentItemTokens"],
            				publishPayload: {
            					url: "{url}",
            					type: "PAGE_RELATIVE"
            				}
            			  }
            		}
                  ]
               }
             },
             {
                name: "alfresco/lists/views/layouts/Cell",
                config: {
                  additionalCssClasses: "mediumpad",
                  widgets: [
                    {
                      id: "SIGNATURE_DOCUMENT_STATUS",
                      name: "alfresco/renderers/Property",
                      config: {
                        //propertyToRender: "node.name"
                    	  propertyToRender: "docusign:status"
                      }
                    }
                  ]
               }
             },
             {
                name: "alfresco/lists/views/layouts/Cell",
                config: {
                  additionalCssClasses: "mediumpad",
                  widgets: [
                    {
                      id: "SIGNATURE_DOCUMENT_RECIPIENT",
                      name: "alfresco/renderers/Property",
                      config: {
                        //propertyToRender: "node.name"
                    	  propertyToRender: "docusign:recipient"
                      }
                    }
                  ]
               }
             },
             {
                 name: "alfresco/lists/views/layouts/Cell",
                 config: {
                   additionalCssClasses: "mediumpad",
                   widgets: [
                     {
                       id: "SIGNATURE_DOCUMENT_SENTBY",
                       name: "alfresco/renderers/Property",
                       config: {
                         //propertyToRender: "node.name"
                     	  propertyToRender: "docusign:sentBy"
                       }
                     }
                   ]
                }
              },
              {
                  name: "alfresco/lists/views/layouts/Cell",
                  config: {
                    additionalCssClasses: "smallpad",
                    widgets: [
                      {
                        id: "SIGNATURE_DOCUMENT_SENTDATE",
                        name: "alfresco/renderers/Date",
                        config: {
                          simple: true,
                      	  propertyToRender: "docusign:sentDate"
                        }
                      }
                    ]
                 }
               },
               {
                   name: "alfresco/lists/views/layouts/Cell",
                   config: {
                     additionalCssClasses: "smallpad",
                     widgets: [
                       {
                         id: "SIGNATURE_DOCUMENT_COMPLETEDON",
                         name: "alfresco/renderers/Date",
                         config: {
                          simple: true,
                       	  propertyToRender: "docusign:completedDate"
                         }
                       }
                     ]
                  }
                },
               {
                   name: "alfresco/lists/views/layouts/Cell",
                   config: {
                     additionalCssClasses: "smallpad",
                     
                     widgets: [
                    	 //goToSignedDocument
                    	 {
                    		  name: "alfresco/renderers/Actions",
                    		  
                    		  config: {
                    			  //visibilityConfig: showAction,
                    			  /**visibilityConfig: {
                    				  initialValue: false,
                    				  rules: [
                    				      {
                    				        topic: 'SEEDIM_SHOW_SIGNED_DOCUMENT_ACTION',
                    				        attribute: 'showSignedDocument',
                    				        is: [true],
                    				        //isNot: [false]
                    				        strict:true
                    				      }
                    				    ]
                    				},**/
                    			  mergeActions: true,
                    			  onlyShowOnHover: true,
                    			  customActions: [
                    			      {
                    			        id: "SIGNED DOCUMENT",
                    			        label: "Go to Signed Document",
                    			        publishTopic: "SEEDIM_GO_TO_SIGNED_DOCUMENT",
                    			        publishPayloadType: "CURRENT_ITEM",
                    			        type: "javascript"
                    			      },
                    			    ],
                    			    widgetsForActions: []
                    		  }
                    	 }
                     ]
                  }
                }
          ]
        }
      }
    ]
  }
}];

var listPaginator = {
		  name: "alfresco/lists/Paginator",
		  config: {
		    //pubSubScope: "SIGNATURE_PAGE_",
		    documentsPerPage: 25,
		    pageSizes: [25,50,100],
		    widgetsAfter: [
		    	//sortControl
		    	/**{
		            name: "alfresco/lists/SortOrderToggle"
		        }**/
		    ]
		  }
		};
//Add view to list
list.config.widgets = views;

model.jsonModel.widgets = [
//  {
//    name: "alfresco/logging/DebugLog"
//  }, 
  title,
  {
    name: "alfresco/layout/HorizontalWidgets",
    config: {
      widgetMarginLeft: "20",
      widgetMarginRight: "10",
      widgets: [{
		  name: "alfresco/layout/ClassicWindow",
		  widthPc: "100",
		  config: {
			 title: "",
			 additionalCssClasses: "no-margin",
			 widgets: [
				 {
			            name: "alfresco/layout/SimplePanel",
			              config: {
			                handleOverflow: true,
			                widgets: [
			                	docusignFilterControl,
			                	//sortControl,
			                	/**{
			                        name: "alfresco/menus/AlfMenuBar",
			                        config: {
			                          widgets: [

					                    {
					                        id: "DOCUSIGN_STATUS_SELECTION_MENU",
					                        name: "alfresco/menus/AlfMenuBarSelect",
					                        config: {
					                           selectionTopic: "SEEDIM_GET_SIGNATURE_DOCUMENTS",
					                           widgets: [
					                              {
					                                 id: "DOCUSIGN_STATUS_SELECTION_MENU_GROUP",
					                                 name: "alfresco/menus/AlfMenuGroup",
					                                 config: {
					                                    widgets: statusOptions
					                                 }
					                              }
					                           ]
					                        }
					                     }
			                            
			                            ,{
			                              name: "alfresco/documentlibrary/AlfSelectedItemsMenuBarPopup",
			                              config: {
			                                 passive: false,
			                                 itemKeyProperty: "nodeRef",
			                                 label: "Selected items",
			                                 widgets: [
			                                    {
			                                       name: "alfresco/menus/AlfMenuGroup",
			                                       config: {
			                                          widgets: [
			                                             {
			                                                name: "alfresco/menus/AlfSelectedItemsMenuItem",
			                                                config: {
			                                                   label: "Download As Zip",
			                                                   clearSelectedItemsOnClick: true,
			                                                   publishTopic:"ALF_SELECTED_DOCUMENTS_ACTION_REQUEST",
			                                                   publishPayload: {
			                                                     action:"onActionDownload",
			                                                   }
			                                                                                           
			                                                }
			                                             }
			                                          ]
			                                       }
			                                    }
			                                 ]
			                              }
			                           }

			                          ]
			                        }
			                      },**/
			                	list,
			                	listPaginator
			                ]
			              }
			      }
				 
			]
		  }
		}
      ]
    }
  }
  
];
