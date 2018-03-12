package fr.toutatice.ecm.platform.collab.tools.quickaccess;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.core.services.infos.provider.DocumentInformationsProvider;

public interface DocumentQuickAccessInfosProvider extends DocumentInformationsProvider {

	/**
	 * Add a document to quickAccess set
	 * @param coreSession the session
	 * @param currentDocument the document to add to quickAccess set
	 */
	void addToQuickAccess(CoreSession coreSession, DocumentModel currentDocument);
	
	/**
	 * Remove a document from quickAccess set
	 * @param coreSession the session
	 * @param currentDocument the document to remove from quickAccess set
	 * @throws ClientException
	 * @throws ClassNotFoundException
	 */
	void removeFromQuickAccess(CoreSession coreSession, DocumentModel currentDocument) throws ClientException, ClassNotFoundException;
	
}
