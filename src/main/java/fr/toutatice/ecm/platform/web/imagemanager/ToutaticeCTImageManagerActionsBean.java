package fr.toutatice.ecm.platform.web.imagemanager;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.collab.tools.constants.CollabToolsConstants;
import fr.toutatice.ecm.platform.collab.tools.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.NuxeoStudioConst;

@Name("ImageManagerActions")
@Scope(ScopeType.EVENT)
@Install(precedence = ExtendedSeamPrecedence.ADD_ON)
public class ToutaticeCTImageManagerActionsBean extends ToutaticeImageManagerActionsBean {
	
	private static final Log log = LogFactory.getLog(ToutaticeCTImageManagerActionsBean.class);
	
	@SuppressWarnings({"unchecked", "rawtypes"})
    public void createHeadImage(ActionEvent event) {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext eContext = context.getExternalContext();
        String index = eContext.getRequestParameterMap().get("index");
        
        try {
            DocumentModel currentDocument = navigationContext.getCurrentDocument();
            if (!currentDocument.hasSchema(CollabToolsConstants.CST_DOC_SCHEMA_ANNONCE)) {
                return;
            }
            
            // la génération de l'image de tête se fera automatiquement via l'event listener "OnHeadImageDocumentUpdate" (Nuxeo Studio)
            Collection files = (Collection) currentDocument.getPropertyValue(NuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES);
            Map<String, Object> file = (Map<String, Object>) CollectionUtils.get(files, new Integer(index));
            currentDocument.setPropertyValue(CollabToolsConstants.CST_DOC_XPATH_HEAD_IMAGE, (Serializable) file.get("file"));
            
            // sauvegarder la modification (et déclencher le resizing de l'image de tête)
            documentManager.saveDocument(currentDocument);

            // notifier la fin de l'opération
            facesMessages.add(StatusMessage.Severity.INFO,
                    resourcesAccessor.getMessages().get("toutatice.fileImporter.create.success.image"));
            
            // some changes (versioning) happened server-side, fetch new one
            fetchCurrentDocument(currentDocument);          
        } catch (Exception e) {
            log.error("Failed to generate the head image, error: " + e.getMessage());
        }
    }

}
