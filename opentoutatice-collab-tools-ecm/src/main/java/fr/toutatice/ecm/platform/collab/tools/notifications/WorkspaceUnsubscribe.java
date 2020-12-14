package fr.toutatice.ecm.platform.collab.tools.notifications;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.runtime.api.Framework;

/**
 * Get all the documents followed by the current user
 * 
 * @author lbillon
 * 
 */
@Operation(id = WorkspaceUnsubscribe.ID, category = Constants.CAT_SERVICES, label = "Remove all notifications within a workspace", description = "Remove all notifications within a workspace")
public class WorkspaceUnsubscribe {

	public static final String ID = "Services.WorkspaceUnsubscribe";

	@Context
	protected CoreSession session;
	
    /** workspace leaved (identified by webc:url) */
    @Param(name = "workspaceId")
    private String workspaceId;

	
    /** user leaves workspace */
    @Param(name = "userId")
    private String userId;

	
	@OperationMethod
    public void run() throws ClientException, ClassNotFoundException {
		
		DocumentModelList workspaces = session.query("SELECT * FROM Workspace where webc:url = '"+workspaceId+"'");
		
		if(workspaces.size() == 1) {
			DocumentModel workspace = workspaces.get(0);
			
			// remove subscriptions
			DocumentNotificationInfosProvider service = Framework.getService(DocumentNotificationInfosProvider.class);
			service.workspaceUnsubscribe(session, workspace, userId);
			
			// remove ACLs - TODO
			
		}
		else {
			// error

		}
		

		
		
	}

}
