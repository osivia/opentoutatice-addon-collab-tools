/*
 * (C) Copyright 2015 Académie de Rennes (http://www.ac-rennes.fr/), OSIVIA (http://www.osivia.com) and others.
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
 * lbillon
 */
package fr.toutatice.ecm.platform.collab.tools.notifications;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.platform.ec.notification.service.NotificationService;
import org.nuxeo.ecm.platform.ec.notification.service.NotificationServiceHelper;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * impl of Notifications service
 *
 * @author lbillon
 *
 */
public class DocumentNotificationInfosProviderImpl implements DocumentNotificationInfosProvider {

    /**
     * A document has a state depending of the user who is browsing it
     */
    public enum SubscriptionStatus {
        /** Default state : can subscribe */
        can_subscribe,
        /** Can unsubscribe if a subscription is already set */
        can_unsubscribe,
        /** If a subscription is defined by other document upper in the hierarchy, or if a group has subscribed before to them */
        has_inherited_subscriptions,
        /** Special cases : Domains, WorkspacesRoot, ... are not allowing subscription */
        no_subscriptions;
    }

    private static final Log log = LogFactory.getLog(DocumentNotificationInfosProviderImpl.class);

    private static final String SUBSCRIPTION_STATUS = "subscription_status";;

    @Override
    public Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDocument) {

        final Map<String, Object> infos = new HashMap<>();

        infos.put(SUBSCRIPTION_STATUS, getStatus(coreSession, currentDocument).toString());

        return infos;
    }

    /**
     * Evaluation status of subscription for the user and the document
     *
     * @param coreSession
     * @param currentDocument
     * @return a status
     * @throws NuxeoException
     */
    public SubscriptionStatus getStatus(CoreSession coreSession, DocumentModel currentDocument) {

        SubscriptionStatus status = SubscriptionStatus.no_subscriptions;

        // Not used in published spaces for the moment
        if (!ToutaticeDocumentHelper.isInPublishSpace(coreSession, currentDocument)) {
            // first : test document type
            if (!(currentDocument.getType().equals("Domain") || currentDocument.getType().equals("WorkspaceRoot"))) {

                // then : test if subscriptions has been put on the current document
                NotificationService notificationService = NotificationServiceHelper.getNotificationService();

                List<String> subscriptionsForUserOnDocument;

                subscriptionsForUserOnDocument = notificationService
                        .getSubscriptionsForUserOnDocument(NuxeoPrincipal.PREFIX + coreSession.getPrincipal().getName(), currentDocument);

                if (subscriptionsForUserOnDocument.size() > 0) {
                    status = SubscriptionStatus.can_unsubscribe;
                } else {
                    // then : test if subscriptions are enabled on parent documents through other subscriptions by the user or
                    // by its group.

                    NuxeoPrincipal currentUser = (NuxeoPrincipal) coreSession.getPrincipal();

                    // First, get user subscriptions
                    List<DocumentModel> subscribedDocuments = notificationService.getSubscribedDocuments(NuxeoPrincipal.PREFIX + currentUser);

                    if (isSubsInheritDocument(coreSession, subscribedDocuments, currentDocument)) {
                        status = SubscriptionStatus.has_inherited_subscriptions;
                    } else {
                        status = SubscriptionStatus.can_subscribe;
                    }
                }
            }
        }

        return status;
    }

    /**
     * Test if currentDoc is a child of any of the subscriptions avaliable.
     *
     * @param coreSession
     * @param allSubscriptions
     * @param currentDoc
     * @return true if is a chield
     * @throws NuxeoException
     */
    private boolean isSubsInheritDocument(CoreSession coreSession, List<DocumentModel> subscribedDocuments, DocumentModel currentDoc) {
        boolean is = false;
        Iterator<DocumentModel> iterator = subscribedDocuments.iterator();

        while (iterator.hasNext() && !is) {
            DocumentModel subscribedDoc = iterator.next();
            is = StringUtils.contains(currentDoc.getPathAsString(), subscribedDoc.getPathAsString());
        }

        return is;
    }

    @Override
    public void subscribe(CoreSession coreSession, DocumentModel currentDocument) {

        if (getStatus(coreSession, currentDocument) == SubscriptionStatus.can_subscribe) {
            final NotificationService notificationService = NotificationServiceHelper.getNotificationService();

            final NuxeoPrincipal principal = (NuxeoPrincipal) coreSession.getPrincipal();

            notificationService.addSubscriptions(NuxeoPrincipal.PREFIX + principal.getName(), currentDocument, false, principal);
        } else {
            throw new NuxeoException("User can not subscribe to this document");
        }
    }

    @Override
    public void unsubscribe(CoreSession coreSession, DocumentModel currentDocument) throws ClassNotFoundException {

        if (getStatus(coreSession, currentDocument) == SubscriptionStatus.can_unsubscribe) {
            final NotificationService notificationService = NotificationServiceHelper.getNotificationService();

            final NuxeoPrincipal principal = (NuxeoPrincipal) coreSession.getPrincipal();

            final List<String> listNotifs = notificationService.getSubscriptionsForUserOnDocument(NuxeoPrincipal.PREFIX + coreSession.getPrincipal().getName(),
                    currentDocument);

            notificationService.removeSubscriptions(NuxeoPrincipal.PREFIX + principal.getName(), listNotifs, currentDocument);
        } else {
            throw new NuxeoException("User can not unsubscribe to this document");
        }
    }

    @Override
    public DocumentModelList getUserSubscriptions(CoreSession session) {
        DocumentModelList documentsSubscribed = new DocumentModelListImpl();

        NotificationService notificationService = NotificationServiceHelper.getNotificationService();
        List<DocumentModel> subscribedDocuments = notificationService.getSubscribedDocuments(NuxeoPrincipal.PREFIX + session.getPrincipal().getName());
        if (subscribedDocuments != null) {
            documentsSubscribed.addAll(subscribedDocuments);
        }

        return documentsSubscribed;
    }

}
