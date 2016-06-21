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
 * lbillon
 * dchevrier
 */
package fr.toutatice.ecm.platform.collab.tools.forum;

import java.io.Serializable;
import java.util.Map;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.comment.api.CommentConstants;
import org.nuxeo.ecm.platform.comment.api.CommentEvents;
import org.nuxeo.ecm.platform.ec.notification.NotificationListenerHook;


/**
 * @author David Chevrier.
 *
 */
public class ThreadPostNotificationListener implements NotificationListenerHook {

    /**
     * Set variable comment with post value
     * in case of Threads posts.
     */
    @Override
    public void handleNotifications(Event event) throws Exception {
        String eventName = event.getName();

        DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
        String type = docCtx.getSourceDocument().getType();

        if (CommentEvents.COMMENT_ADDED.equals(eventName)) {

            if (type.equals("Post") || type.equals("Thread")) {

                setPostContent(docCtx);

            }

        }

    }

    /**
     * Set the post content to be available in mail template.
     * 
     * @param docCtx
     */
    private void setPostContent(DocumentEventContext docCtx) {
        Map<String, Serializable> properties = docCtx.getProperties();

        DocumentModel post = (DocumentModel) properties.get(CommentConstants.COMMENT_DOCUMENT);
        String postContent = (String) post.getProperty("post", "text");

        properties.put(CommentConstants.COMMENT, postContent);
    }

}
