/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/) and others.
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
 * dchevrier
 */
package fr.toutatice.ecm.platform.collab.tools.mail;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.collab.tools.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.service.portalviews.adapter.WidgetsAdapterService;
import fr.toutatice.ecm.platform.web.document.ToutaticeWebActionsBean;


/**
 * @author David Chevrier.
 *
 */
@Name("webActions")
@Scope(ScopeType.CONVERSATION)
@Install(precedence = ExtendedSeamPrecedence.ADD_ON)
public class ToutaticeCTWebActionsBean extends ToutaticeWebActionsBean {

    private static final long serialVersionUID = -6124671913159928250L;
    
    @Override
    public boolean isInPortalViewContext(){
        WidgetsAdapterService widgetsAdapterService = Framework.getLocalService(WidgetsAdapterService.class);
        widgetsAdapterService.addPortalViewsIds("send_notification_email", "document_notif_email");
        return widgetsAdapterService.isInPortalViewContext();
    }

}
