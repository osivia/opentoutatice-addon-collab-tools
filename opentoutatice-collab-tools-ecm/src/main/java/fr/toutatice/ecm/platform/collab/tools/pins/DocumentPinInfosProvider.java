package fr.toutatice.ecm.platform.collab.tools.pins;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.core.services.infos.provider.DocumentInformationsProvider;

public interface DocumentPinInfosProvider extends DocumentInformationsProvider {

	void pin(CoreSession coreSession, DocumentModel currentDocument);
	
	void unPin(CoreSession coreSession, DocumentModel currentDocument) throws ClientException, ClassNotFoundException;
	
}
