package fr.toutatice.ecm.platform.collab.tools.quickaccess;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;


@Operation(id = RemoveDocumentFromQuickAccess.ID, category = Constants.CAT_DOCUMENT, label = "Remove document from quick access set", description = "Remove document from quick access set")
public class RemoveDocumentFromQuickAccess {

	public static final String ID = "Document.RemoveFromQuickAccess";
	
	@Context
	protected CoreSession session;

	@OperationMethod
	public void run(DocumentModel document) throws ClientException, ClassNotFoundException {

		DocumentQuickAccessInfosProvider service = Framework.getService(DocumentQuickAccessInfosProvider.class);

		service.removeFromQuickAccess(session, document);
	}
}
