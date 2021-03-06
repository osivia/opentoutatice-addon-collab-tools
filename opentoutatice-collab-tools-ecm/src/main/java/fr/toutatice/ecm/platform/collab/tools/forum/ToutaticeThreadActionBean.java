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
 * mberhaut1
 */
package fr.toutatice.ecm.platform.collab.tools.forum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.model.DocumentPart;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.platform.forum.web.ThreadActionBean;
import org.nuxeo.ecm.webapp.helpers.EventManager;
import org.nuxeo.ecm.webapp.helpers.EventNames;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;

@Name("threadAction")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeThreadActionBean extends ThreadActionBean {

    private static final long serialVersionUID = 1L;

    @In(create = true, required = false)
    protected FacesMessages facesMessages;

    @In(create = true)
    protected EventManager eventManager;

    /** Indicates if we have to get DocumentModel value of attribute moderated */
    private boolean isModeratedFilled;

    /** Indicates if we have to get DocumentModel value of attribute selectedModerators */
    private boolean isSelectedModeratorsFilled;

    @Override
    protected DocumentModel getThreadModel() throws ClientException {
        // Fill with form
        DocumentModel changeableDocument = navigationContext.getChangeableDocument();

        DocumentModel threadModel = super.getThreadModel();

        // Fill ~empty threadModel with changeableDocument
        DocumentPart[] parts = changeableDocument.getParts();
        if (parts != null) {

            for (DocumentPart part : parts) {
                if (!part.isScalar()) {
                    Collection<Property> properties = part.getChildren();

                    Iterator<Property> iterator = properties.iterator();
                    while (iterator.hasNext()) {
                        Property property = iterator.next();
                        threadModel.setPropertyValue(property.getPath(), property.getValue());
                    }
                } else {
                    threadModel.setPropertyValue(part.getPath(), part.getValue());
                }
            }
        }

        return threadModel;
    }

    @Override
    public boolean isModerated() {
        if (!isModeratedFilled) {
            DocumentModel thread = navigationContext.getCurrentDocument();
            try {
                this.moderated = super.isThreadModerated(thread);
                isModeratedFilled = true;
            } catch (ClientException e) {
                this.moderated = false;
            }
        }
        return this.moderated;
    }

    @Override
    public List<String> getSelectedModerators() {
        if (!isSelectedModeratorsFilled) {
            this.selectedModerators = super.getModerators();
            if (this.selectedModerators == null) {
                this.selectedModerators = new ArrayList<String>();
            }
            isSelectedModeratorsFilled = true;
        }
        return this.selectedModerators;
    }

    public String addThread(String viewId) throws ClientException {
        super.addThread();
        return viewId;
    }

    public String updateThread() throws ClientException {
        DocumentModel currentDocument = navigationContext.getCurrentDocument();

        currentDocument.setProperty(schema, "moderated", moderated);
        List<String> sM = getSelectedModerators();

        if (!moderated) {
            sM.clear();
        } else {
            // We automatically add administrators (with prefix) as moderators
            if (!sM.contains(NuxeoGroup.PREFIX + SecurityConstants.ADMINISTRATORS)) {
                sM.add(NuxeoGroup.PREFIX + SecurityConstants.ADMINISTRATORS);
            }

            // We can also remove Administrator since his group is added
            if (sM.contains(NuxeoPrincipal.PREFIX + SecurityConstants.ADMINISTRATOR)) {
                sM.remove(NuxeoPrincipal.PREFIX + SecurityConstants.ADMINISTRATOR);
            }
        }
        setSelectedModerators(sM);
        currentDocument.setProperty(schema, "moderators", this.selectedModerators);

        // notifications avant
        Events.instance().raiseEvent(EventNames.BEFORE_DOCUMENT_CHANGED, currentDocument);

        // sauvegarde
        currentDocument = documentManager.saveDocument(currentDocument);

        // notifications après
        navigationContext.invalidateCurrentDocument();
        facesMessages.add(StatusMessage.Severity.INFO, resourcesAccessor.getMessages().get("document_modified"),
                resourcesAccessor.getMessages().get(currentDocument.getType()));
        EventManager.raiseEventsOnDocumentChange(currentDocument);
        return navigationContext.navigateToDocument(currentDocument, "after-edit");
    }

    public String updateThread(String viewId) throws ClientException {
        updateThread();
        return viewId;
    }

    @Observer(value = {EventNames.NEW_DOCUMENT_CREATED, EventNames.DOCUMENT_SELECTION_CHANGED}, create = false)
    public void refresh() throws ClientException {
        super.clean();
        isModeratedFilled = false;
        isSelectedModeratorsFilled = false;
    }

}