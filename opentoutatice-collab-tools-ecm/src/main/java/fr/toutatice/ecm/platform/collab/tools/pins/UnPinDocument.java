package fr.toutatice.ecm.platform.collab.tools.pins;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;


@Operation(id = UnPinDocument.ID, category = Constants.CAT_DOCUMENT, label = "Unpin the document", description = "Unpin the document")
public class UnPinDocument {

	public static final String ID = "Document.UnPin";
	
	@Context
	protected CoreSession session;

	@OperationMethod
	public void run(DocumentModel document) throws ClientException, ClassNotFoundException {

		DocumentPinInfosProvider service = Framework.getService(DocumentPinInfosProvider.class);

		service.unPin(session, document);
	}
}
