/**
 * 
 */
package fr.toutatice.ecm.platform.collab.tools.compat.picturebook.bean;

import static org.jboss.seam.ScopeType.CONVERSATION;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.picture.web.PictureBookManager;

import fr.toutatice.ecm.platform.collab.tools.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.PortalConstants;
import fr.toutatice.ecm.platform.web.document.ToutaticeDocumentActionsBean;


/**
 * @author david
 */
@Name("documentActions")
@Scope(CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.ADD_ON)
public class PictureBookActionsBean extends ToutaticeDocumentActionsBean {

    private static final long serialVersionUID = -6577762872856528540L;

    private static final Log log = LogFactory.getLog(PictureBookActionsBean.class);

    @In(create = true)
    protected transient PictureBookManager pictureBookManager;

    public String createNSetOnLinePictureBook() throws Exception {
        String view = StringUtils.EMPTY;

        if (null != pictureBookManager) {
            // create
            view = pictureBookManager.createPictureBook();
            // mise en ligne
            final DocumentModel newDocument = navigationContext.getCurrentDocument();
            setDocumentOnline(newDocument);
        } else {
            log.error("Failed to get the picture book manager from seam context");
        }

        return view;
    }

    public String createNSetOnLinePictureBook(String viewId) throws Exception {
        if (null != pictureBookManager) {
            // create
            pictureBookManager.createPictureBook();
            // mise en ligne
            final DocumentModel newDocument = navigationContext.getCurrentDocument();
            setDocumentOnline(newDocument);
        } else {
            log.error("Failed to get the picture book manager from seam context");
        }

        return viewId;
    }

    public String createPictureBook(String viewId) throws Exception {
        if (null != pictureBookManager) {
            // create
            pictureBookManager.createPictureBook();
            pageBean.setNotificationKey(PortalConstants.Notifications.SUCCESS_MESSAGE_CREATE.name());
            live = true;
        } else {
            log.error("Failed to get the picture book manager from seam context");
        }

        return viewId;
    }

}
