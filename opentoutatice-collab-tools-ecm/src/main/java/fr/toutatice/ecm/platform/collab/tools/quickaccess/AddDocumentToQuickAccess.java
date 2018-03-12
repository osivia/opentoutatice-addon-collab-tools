package fr.toutatice.ecm.platform.collab.tools.quickaccess;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;


@Operation(id = AddDocumentToQuickAccess.ID, category = Constants.CAT_DOCUMENT, label = "Add the document to quickAccess set", description = "Add the document to quickAccess set")
public class AddDocumentToQuickAccess {

	public static final String ID = "Document.AddToQuickAccess";
	
	@Context
	protected CoreSession session;

	@OperationMethod
	public void run(DocumentModel document) throws ClientException {

		DocumentQuickAccessInfosProvider service = Framework.getService(DocumentQuickAccessInfosProvider.class);

		service.addToQuickAccess(session, document);
	}
}
