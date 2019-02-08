/**
 * 
 */
package fr.toutatice.ecm.platform.collab.tools.userprofile;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.user.center.profile.UserProfileServiceImpl;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.elasticsearch.query.NxQueryBuilder;
import org.nuxeo.runtime.api.Framework;

/**
 * @author loic
 *
 */
public class TtcUserProfileServiceImpl extends UserProfileServiceImpl {

	
	/* (non-Javadoc)
	 * @see org.nuxeo.ecm.user.center.profile.UserProfileServiceImpl#getUserProfileDocument(org.nuxeo.ecm.core.api.CoreSession)
	 */
	@Override
	public DocumentModel getUserProfileDocument(CoreSession session) throws ClientException {
		
		String userName = session.getPrincipal().getName();
		return this.getUserProfileDocument(userName,session);
				
	}
	
	/* (non-Javadoc)
	 * @see org.nuxeo.ecm.user.center.profile.UserProfileServiceImpl#getUserProfileDocument(java.lang.String, org.nuxeo.ecm.core.api.CoreSession)
	 */
	@Override
	public DocumentModel getUserProfileDocument(String userName, CoreSession session) throws ClientException {
		
		ElasticSearchService service = Framework.getService(ElasticSearchService.class);
		
		if(service !=null) {
			NxQueryBuilder queryBuilder = new NxQueryBuilder(session);
			queryBuilder.nxql("SELECT * FROM UserProfile WHERE ttc_userprofile:login = '"+userName+"' AND ecm:isProxy = 0 " + 
					" AND ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'");
			DocumentModelList query = service.query(queryBuilder);
			
			if(query.size() > 0) {
				return query.get(0);
			}
			else {
				return super.getUserProfileDocument(userName, session);

			}
		}
		
		else {
			return super.getUserProfileDocument(userName, session);
		}
	}
}
