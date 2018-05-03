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
 *   lbillon
 *    
 */
package fr.toutatice.ecm.platform.collab.tools.notifications;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;

/**
 * Subscribe to all notifications on the document
 * 
 * @author lbillon
 * 
 */
@Operation(id = AllNotificationsSubscribe.ID, category = Constants.CAT_NOTIFICATION, label = "Subscribe to the document", description = "Subscribe to all notifications on the document")
public class AllNotificationsSubscribe {

	public static final String ID = "Notification.AllNotificationsSubscribe";

	@Context
	protected CoreSession session;

	@OperationMethod
	public void run(DocumentModel document) throws NuxeoException {

		DocumentNotificationInfosProvider service = Framework.getService(DocumentNotificationInfosProvider.class);

		service.subscribe(session, document);
	}

}
