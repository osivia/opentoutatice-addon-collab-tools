/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *
 * Contributors:
 *   mberhaut1
 *    
 */
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
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.collab.tools.constants.CollabToolsConstants;
import fr.toutatice.ecm.platform.collab.tools.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;

@Name("ImageManagerActions")
@Scope(ScopeType.EVENT)
@Install(precedence = ExtendedSeamPrecedence.ADD_ON)
public class ToutaticeCTImageManagerActionsBean extends ToutaticeImageManagerActionsBean {
	
	private static final Log log = LogFactory.getLog(ToutaticeCTImageManagerActionsBean.class);
	
	@SuppressWarnings({"unchecked", "rawtypes"})
    public void createHeadImage() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext eContext = context.getExternalContext();
        String index = eContext.getRequestParameterMap().get("index");
        
        try {
            DocumentModel currentDocument = navigationContext.getCurrentDocument();
            if (!currentDocument.hasSchema(CollabToolsConstants.CST_DOC_SCHEMA_ANNONCE)) {
                return;
            }
            
            // la génération de l'image de tête se fera automatiquement via l'event listener "OnHeadImageDocumentUpdate" (Nuxeo Studio)
            Collection files = (Collection) currentDocument.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TOUTATICE_IMAGES);
            Map<String, Object> file = (Map<String, Object>) CollectionUtils.get(files, new Integer(index));
            currentDocument.setPropertyValue(CollabToolsConstants.CST_DOC_XPATH_HEAD_IMAGE, (Serializable) file.get("file"));
            
            // sauvegarder la modification (et déclencher le resizing de l'image de tête)
            documentManager.saveDocument(currentDocument);

            // notifier la fin de l'opération
            FacesMessages.instance().addFromResourceBundle(
    				"toutatice.fileImporter.create.success.image");
            
            // some changes (versioning) happened server-side, fetch new one
            fetchCurrentDocument(currentDocument);          
        } catch (Exception e) {
            log.error("Failed to generate the head image, error: " + e.getMessage());
        }
    }

}
