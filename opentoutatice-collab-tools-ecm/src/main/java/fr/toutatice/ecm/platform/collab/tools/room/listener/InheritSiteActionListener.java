/**
 * 
 */
package fr.toutatice.ecm.platform.collab.tools.room.listener;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.ABOUT_TO_CREATE;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.EventImpl;
import org.nuxeo.ecm.core.schema.SchemaManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.webengine.sites.listeners.SiteActionListener;
import org.nuxeo.webengine.sites.utils.SiteConstants;

import fr.toutatice.ecm.platform.core.helper.ToutaticeQueryHelper;


/**
 * @author david
 *
 */
public class InheritSiteActionListener extends SiteActionListener {

    /** Repo unicity query of webcUrl. */
    private static final String REPO_WEBCURL_UNICITY_QUERY = "select * from Document where webc:url = '%s' and ecm:isVersion = 0 and ecm:isProxy = 0";

    /** Increment prefix for webcUrl. */
    private static final String INCREMENT_PREFIX = "_";

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

                    // Check repo unicity of generated webc:url cause SiteActionListener doesn't assure it
                    String webcUrl = (String) virtualWsModified.getPropertyValue(SiteConstants.WEBCONTAINER_URL);

                    while (isNotUnique(sourceDocument.getCoreSession(), webcUrl)) {
                        webcUrl = generateWebcUrl(webcUrl);
                    }

                    String webcName = (String) virtualWsModified.getPropertyValue(SiteConstants.WEBCONTAINER_NAME);

                    sourceDocument.setPropertyValue(SiteConstants.WEBCONTAINER_NAME, webcName);
                    sourceDocument.setPropertyValue(SiteConstants.WEBCONTAINER_URL, webcUrl);
                }

            }
        } else {
            super.handleEvent(event);
        }

    }

    /**
     * Checks if webc:url is unique on <b>repository</b>.
     *
     * @param session
     * @param webcUrl
     * @return true if unique
     */
    private boolean isNotUnique(CoreSession session, String webcUrl) {
        return ToutaticeQueryHelper.queryUnrestricted(session, String.format(REPO_WEBCURL_UNICITY_QUERY, webcUrl)).size() >= 1;
    }

    /**
     * Generates unique webcUrl on repo.
     * 
     * @param webcUrl
     * @return
     */
    private String generateWebcUrl(String webcUrl) {
        // Check if webcUrl has yet been tried to be made unique by SiteActionListener
        String increment = StringUtils.substringAfterLast(webcUrl, INCREMENT_PREFIX);
        if (StringUtils.isNotBlank(increment)) {
            int incr = Integer.valueOf(increment) + 1;
            webcUrl = StringUtils.substringBeforeLast(webcUrl, INCREMENT_PREFIX).concat(INCREMENT_PREFIX).concat(String.valueOf(incr));
        } else {
            webcUrl = webcUrl.concat(INCREMENT_PREFIX).concat(String.valueOf(1));
        }
        
        return webcUrl;
    }

}
