package fr.toutatice.ecm.platform.collab.tools.workspaces;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.collab.tools.notifications.DocumentNotificationInfosProvider;

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
			ElasticSearchService es = Framework.getService(ElasticSearchService.class);
			
			NxQueryBuilder query = new NxQueryBuilder(session);
			query.nxql("SELECT * FROM Document WHERE ecm:path STARTSWITH '"+workspace.getPathAsString()+"' AND ecm:acl/* = '"+userId+"'");
			DocumentModelList query2 = es.query(query);
			
			for(DocumentModel docWithAcls : query2) {
				
				boolean AcpModified = false;
		        ACP acp = session.getACP(docWithAcls.getRef());
		        ACL[] acls = acp.getACLs();
		        for(ACL acl : acls) {
		        	for(ACE ace : acl.getACEs()) {
		        		if(ace.getUsername().equals(userId)) {
		        			acl.remove(ace);
		        			AcpModified = true;
		        		}
		        	}
		        }
		        
		        if(AcpModified) {
		        	session.setACP(docWithAcls.getRef(), acp, true);
		        }
		        
			}
		}
		else {
			// error

		}
		

		
		
	}

}
