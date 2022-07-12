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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.model.NoSuchDocumentException;
import org.nuxeo.ecm.platform.ec.notification.UserSubscription;
import org.nuxeo.ecm.platform.ec.notification.service.NotificationService;
import org.nuxeo.ecm.platform.ec.notification.service.NotificationServiceHelper;
import org.nuxeo.ecm.platform.ec.placeful.Annotation;
import org.nuxeo.ecm.platform.ec.placeful.interfaces.PlacefulService;
import org.nuxeo.ecm.platform.notification.api.NotificationManager;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.collab.tools.notifications.exception.DeleteUserSubscriptionException;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * impl of Notifications service
 * 
 * @author lbillon
 * 
 */
public class DocumentNotificationInfosProviderImpl implements DocumentNotificationInfosProvider {

    private static final Log log = LogFactory.getLog(DocumentNotificationInfosProviderImpl.class);

    private static final String SUBSCRIPTION_STATUS = "subscription_status";

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
    };

    @Override
    public void subscribe(CoreSession coreSession, DocumentModel currentDocument) {

        if (getStatus(coreSession, currentDocument) == SubscriptionStatus.can_subscribe) {
            NotificationManager notificationManager = Framework.getService(NotificationManager.class);

            NuxeoPrincipal principal = (NuxeoPrincipal) coreSession.getPrincipal();

            notificationManager.addSubscriptions(NuxeoPrincipal.PREFIX + principal.getName(), currentDocument, false, principal);
        } else {
            throw new ClientException("User can not subscribe to this document");
        }


    }

    @Override
    public void unsubscribe(CoreSession coreSession, DocumentModel currentDocument) throws ClientException, ClassNotFoundException {

        if (getStatus(coreSession, currentDocument) == SubscriptionStatus.can_unsubscribe) {
            NotificationManager notificationManager = Framework.getService(NotificationManager.class);

            NuxeoPrincipal principal = (NuxeoPrincipal) coreSession.getPrincipal();


            List<String> listNotifs = notificationManager.getSubscriptionsForUserOnDocument(NuxeoPrincipal.PREFIX + coreSession.getPrincipal().getName(),
                    currentDocument.getId());
            notificationManager.removeSubscriptions(NuxeoPrincipal.PREFIX + principal.getName(), listNotifs, currentDocument.getId());
        } else {
            throw new ClientException("User can not unsubscribe to this document");
        }


    }
    
	@Override
	public void workspaceUnsubscribe(CoreSession session, DocumentModel workspace, String user)
			throws ClientException, ClassNotFoundException {
		
        NotificationManager notificationManager = Framework.getService(NotificationManager.class);

		DocumentModelList usersubscriptions = getSubscriptionsFor(session, user);
		
		for(DocumentModel subscr : usersubscriptions) {
		
			if (StringUtils.startsWith(subscr.getPathAsString(), workspace.getPathAsString())) {
	            List<String> listNotifs = notificationManager.getSubscriptionsForUserOnDocument(NuxeoPrincipal.PREFIX + user,
	            		subscr.getId());
	            notificationManager.removeSubscriptions(NuxeoPrincipal.PREFIX + user, listNotifs, subscr.getId());
			}
		}
	}
	
	

    @Override
    public Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {

        Map<String, Object> infos = new HashMap<String, Object>();

        infos.put(SUBSCRIPTION_STATUS, getStatus(coreSession, currentDocument).toString());

        return infos;
    }

    /**
     * Evaluation status of subscription for the user and the document
     * 
     * @param coreSession
     * @param currentDocument
     * @return a status
     * @throws ClientException
     */
    public SubscriptionStatus getStatus(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {

        SubscriptionStatus status = SubscriptionStatus.no_subscriptions;

        // Not used in published spaces for the moment
        if (!ToutaticeDocumentHelper.isInPublishSpace(coreSession, currentDocument)) {
            // first : test document type
            if (!(currentDocument.getType().equals("Domain") || currentDocument.getType().equals("WorkspaceRoot"))) {

                // then : test if subscriptions has been put on the current document
                NotificationManager notificationManager = Framework.getService(NotificationManager.class);

                List<String> subscriptionsForUserOnDocument;

                try {

                    subscriptionsForUserOnDocument = notificationManager.getSubscriptionsForUserOnDocument(NuxeoPrincipal.PREFIX
                            + coreSession.getPrincipal().getName(), currentDocument.getId());
                } catch (ClassNotFoundException e) {
                    throw new ClientException(e);
                }

                if (subscriptionsForUserOnDocument.size() > 0) {
                    status = SubscriptionStatus.can_unsubscribe;
                } else {
                    // then : test if subscriptions are enabled on parent documents through other subscriptions by the user or
                    // by its group.

                    NuxeoPrincipal currentUser = (NuxeoPrincipal) coreSession.getPrincipal();

                    PlacefulService service;
                    try {
                        service = NotificationServiceHelper.getPlacefulService();
                    } catch (Exception e) {
                        throw new ClientException(e);
                    }
                    String className = service.getAnnotationRegistry().get(NotificationService.SUBSCRIPTION_NAME);
                    String shortClassName = className.substring(className.lastIndexOf('.') + 1);

                    PlacefulService serviceBean = NotificationServiceHelper.getPlacefulServiceBean();
                    List<Annotation> tempSubscriptions = new ArrayList<Annotation>();

                    // First, get user subscriptions
                    Map<String, Object> paramMap = new HashMap<String, Object>();
                    paramMap.put("userId", NuxeoPrincipal.PREFIX + currentUser.getName());

                    tempSubscriptions.addAll(serviceBean.getAnnotationListByParamMap(paramMap, shortClassName));

                    // Then, get group subscriptions
                    // for (String group : currentUser.getAllGroups()) {
                    // paramMap.put("userId", NuxeoGroup.PREFIX + group);
                    // tempSubscriptions.addAll(serviceBean.getAnnotationListByParamMap(paramMap, shortClassName));
                    // }


                    if (isSubsInheritDocument(coreSession, tempSubscriptions, currentDocument)) {
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
     * @throws ClientException
     */
    private boolean isSubsInheritDocument(CoreSession coreSession, List<Annotation> allSubscriptions, DocumentModel currentDoc) throws ClientException {

        for (Object obj : allSubscriptions) {
            UserSubscription us = (UserSubscription) obj;
            try {
                
                String parentPath = StringUtils.substringBeforeLast(currentDoc.getPathAsString(), "/");

                DocumentModel doc = ToutaticeDocumentHelper.getUnrestrictedDocument(coreSession, us.getDocId());
                boolean contains = StringUtils.contains(parentPath, doc.getPathAsString());

                if (contains) {
                    return true;
                }
            } catch (ClientException de) {
                // Doc doesn't exist anymore (not found)
                if (de.getCause() instanceof NoSuchDocumentException) {
                    removeUserSubscription(coreSession, us);

                    if (log.isDebugEnabled()) {
                        log.debug("NoSuchDocumentException: subscription " + us.getNotification() + " on document " + us.getDocId() + "for user "
                                + us.getUserId() + " removed");
                    }
                }
            }
        }

        return false;
    }

    @Override
    public DocumentModelList getUserSubscriptions(CoreSession coreSession) {

    	return getSubscriptionsFor(coreSession, coreSession.getPrincipal().getName());
    }
    
    
    private DocumentModelList getSubscriptionsFor(CoreSession coreSession, String user) {

        DocumentModelList documentsSubscribed = new DocumentModelListImpl();

        PlacefulService serviceBean = NotificationServiceHelper.getPlacefulServiceBean();

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("userId", NuxeoPrincipal.PREFIX + user);

        String className = serviceBean.getAnnotationRegistry().get(NotificationService.SUBSCRIPTION_NAME);
        String shortClassName = className.substring(className.lastIndexOf('.') + 1);

        List<Annotation> userSubscriptions = new ArrayList<Annotation>();

        userSubscriptions.addAll(serviceBean.getAnnotationListByParamMap(paramMap, shortClassName));

        for (Annotation subscription : userSubscriptions) {
            UserSubscription us = (UserSubscription) subscription;
            try {

                DocumentModel subscribedDoc = coreSession.getDocument(new IdRef(us.getDocId()));
                documentsSubscribed.add(subscribedDoc);

            } catch (ClientException ce) {
                repareUserSubscription(coreSession, us, ce);
            }

        }

        return documentsSubscribed;
    }

    /**
     * @param serviceBean
     * @param us
     * @param ce
     * @throws DeleteUserSubscriptionException
     */
    protected void repareUserSubscription(CoreSession session, UserSubscription us, ClientException ce)
            throws DeleteUserSubscriptionException {

        if (ce.getCause() instanceof NoSuchDocumentException) {
            // Document doesn't exist anymore: remove subscription
            removeUserSubscription(session, us);

            if (log.isDebugEnabled()) {
                log.debug("NoSuchDocumentException: subscription " + us.getNotification() + " on document " + us.getDocId() + "for user " + us.getUserId()
                        + " removed");
            }
        } else if (ce instanceof DocumentSecurityException) {
            // Can not access document anymore
            removeUserSubscription(session, us);

            if (log.isDebugEnabled()) {
                log.debug("DocumentSecurityException: subscription " + us.getNotification() + " on document " + us.getDocId() + "for user " + us.getUserId()
                        + " removed");
            }
        } else {
            // Technical error
            throw ce;
        }
    }

    /**
     * Remove given UserSubscription.
     * 
     * @param service
     * @param session
     * @param us
     * @throws DeleteUserSubscriptionException
     */
    protected void removeUserSubscription(CoreSession session, UserSubscription us) throws DeleteUserSubscriptionException {
        String currentUserId = NuxeoPrincipal.PREFIX + session.getPrincipal().getName();

        if (StringUtils.equals(us.getUserId(), currentUserId)) {
            // Do not use: PlacefulService#removeAnnotation(us) cause Hibernate exception:
            // java.lang.IllegalArgumentException: Removing a detached instance
            NotificationServiceHelper.getNotificationService().removeSubscription(us.getUserId(), us.getNotification(), us.getDocId());
        } else {
            throw new DeleteUserSubscriptionException("Trying deleting subscription for user " + us.getUserId() + "instead of user " + currentUserId);
        }
    }



}
