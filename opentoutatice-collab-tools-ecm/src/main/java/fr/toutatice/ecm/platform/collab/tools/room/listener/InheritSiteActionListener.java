/**
 * 
 */
package fr.toutatice.ecm.platform.collab.tools.room.listener;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.ABOUT_TO_CREATE;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.EventImpl;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.webengine.sites.listeners.SiteActionListener;
import org.nuxeo.webengine.sites.utils.SiteConstants;


/**
 * @author david
 *
 */
public class InheritSiteActionListener extends SiteActionListener {

    /**
     * Original SiteActionListener test if source document is of Workspace or WebSite type.
     * We want to test if type is subtype of Workspace too (case of Room).
     */
    public void handleEvent(Event event) throws ClientException {
        if (ABOUT_TO_CREATE.equals(event.getName())) {

            SchemaManager schemaManager = (SchemaManager) Framework.getService(SchemaManager.class);

            EventContext eventContext = event.getContext();
            if (eventContext instanceof DocumentEventContext) {
                DocumentEventContext docContext = (DocumentEventContext) eventContext;
                DocumentModel sourceDocument = docContext.getSourceDocument();

                if (schemaManager.hasSuperType(sourceDocument.getType(), SiteConstants.WORKSPACE)) {
                    String parentPath = null;
                    DocumentModel virtualWs = new DocumentModelImpl(parentPath, sourceDocument.getName(), SiteConstants.WORKSPACE);

                    DocumentEventContext virtualDocCtx = new DocumentEventContext(sourceDocument.getCoreSession(), docContext.getPrincipal(), virtualWs);

                    Event virtualEvent = new EventImpl(ABOUT_TO_CREATE, virtualDocCtx);
                    super.handleEvent(virtualEvent);

                    DocumentModel virtualWsModified = ((DocumentEventContext) virtualEvent.getContext()).getSourceDocument();
                    String webcName = (String) virtualWsModified.getPropertyValue(SiteConstants.WEBCONTAINER_NAME);
                    String webcUrl = (String) virtualWsModified.getPropertyValue(SiteConstants.WEBCONTAINER_URL);

                    sourceDocument.setPropertyValue(SiteConstants.WEBCONTAINER_NAME, webcName);
                    sourceDocument.setPropertyValue(SiteConstants.WEBCONTAINER_URL, webcUrl);
                }

            }
        } else {
            super.handleEvent(event);
        }

    }

}
