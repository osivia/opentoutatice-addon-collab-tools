package fr.toutatice.ecm.platform.collab.tools.pins;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.core.services.infos.provider.DocumentInformationsProvider;

public interface DocumentPinInfosProvider extends DocumentInformationsProvider {

	/**
	 * Pin a document
	 * @param coreSession the session
	 * @param currentDocument the document to pin
	 */
	void pin(CoreSession coreSession, DocumentModel currentDocument);
	
	/**
	 * Unpin a document
	 * @param coreSession the session
	 * @param currentDocument the document to unpin
	 * @throws ClientException
	 * @throws ClassNotFoundException
	 */
	void unPin(CoreSession coreSession, DocumentModel currentDocument) throws ClientException, ClassNotFoundException;
	
}
