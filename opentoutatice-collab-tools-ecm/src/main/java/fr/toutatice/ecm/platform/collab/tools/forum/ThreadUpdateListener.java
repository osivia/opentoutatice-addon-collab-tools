/**
 * 
 */
package fr.toutatice.ecm.platform.collab.tools.forum;

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.comment.api.CommentConstants;
import org.nuxeo.ecm.platform.comment.api.CommentEvents;
import org.nuxeo.ecm.platform.comment.api.CommentManager;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.collab.tools.constants.CollabToolsConstants;
import fr.toutatice.ecm.platform.core.listener.ToutaticeDocumentEventListenerHelper;

/**
 * Update Thread on added, removed Post.
 * 
 * @author david
 *
 */
public class ThreadUpdateListener implements EventListener {

    /** CommentManager. */
    protected static CommentManager commentManager;

    protected static CommentManager getCommentManager() {
        if (commentManager == null) {
            commentManager = Framework.getService(CommentManager.class);
        }
        return commentManager;
    }

    /**
     * Update Thread on added, removed Post.
     */
    @Override
    public void handleEvent(Event event) throws ClientException {

        if (event.getContext() instanceof DocumentEventContext) {

            String eventName = event.getName();
            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
            CoreSession session = docCtx.getCoreSession();
            DocumentModel srcDoc = docCtx.getSourceDocument();

            if (srcDoc != null && ToutaticeDocumentEventListenerHelper.isAlterableDocument(srcDoc)) {
                // Creation case
                boolean isThreadCreated = CollabToolsConstants.THREAD_TYPE.equals(srcDoc.getType()) && DocumentEventTypes.DOCUMENT_CREATED.equals(eventName);
                if (isThreadCreated) {
                    initializeThread(srcDoc);
                } else {
                    // Answer, comment of answer
                    boolean isCommentAddedOrRemoved = (CommentEvents.COMMENT_ADDED.equals(eventName) || CommentEvents.COMMENT_REMOVED.equals(eventName));
                    if (isCommentAddedOrRemoved) {
                        // Check if comment type is Post
                        DocumentModel comment = (DocumentModel) docCtx.getProperty(CommentConstants.COMMENT_DOCUMENT);
                        if ("Post".equals(comment.getType())) {
                            updateAnswersOfThread(srcDoc, session);
                        }
                    }
                }
            }
        }

    }

    private void initializeThread(DocumentModel thread) {
        // Update ttcth:lastCommentDate to set Thread first in list
        // (nb answers is set to 0 by default - schema definition)
        GregorianCalendar dateCreation = (GregorianCalendar) thread.getPropertyValue("dc:created");
        thread.setPropertyValue(CollabToolsConstants.TTC_THREAD_LAST_COMMENT_DATE_XPATH, dateCreation);
    }

    /**
     * Updates the datas about Thread answers.
     * 
     * @param eventName
     * @param thread
     * @param session
     */
    public void updateAnswersOfThread(DocumentModel thread, CoreSession session) {

        // To force reload of Posts
        session.save();

        // -------------

        List<DocumentModel> answers = getCommentManager().getComments(thread);

        Serializable lastCommentAuthor = thread.getPropertyValue("dc:creator");
        GregorianCalendar lastCommentDate = (GregorianCalendar) thread.getPropertyValue("dc:created");
        for (DocumentModel answer : answers) {
            Serializable author = answer.getPropertyValue("post:author");

            GregorianCalendar creationDate = (GregorianCalendar) answer.getPropertyValue("post:creationDate");

            if (lastCommentDate == null || lastCommentDate.before(creationDate)) {
                lastCommentDate = creationDate;
                lastCommentAuthor = author;
            }
        }

        Map<String, Serializable> properties = new HashMap<String, Serializable>();
        properties.put(CollabToolsConstants.TTC_THREAD_NB_COMMENTS_XPATH, answers.size());
        properties.put(CollabToolsConstants.TTC_THREAD_LAST_COMMENT_DATE_XPATH, lastCommentDate);
        properties.put(CollabToolsConstants.TTC_THREAD_LAST_COMMENT_AUTHOR_XPATH, lastCommentAuthor);

        UnrestrictedPropertySetter propertySetter = new UnrestrictedPropertySetter(session, thread.getRef(), properties);
        propertySetter.runUnrestricted();

    }

    /**
     * To set a property in unrestricted way.
     * 
     * @author david
     *
     */
    // FIXME: to move in ToutaticeDocumentHelper.
    protected class UnrestrictedPropertySetter extends UnrestrictedSessionRunner {

        DocumentRef docRef;

        Map<String, Serializable> properties;


        protected UnrestrictedPropertySetter(CoreSession session, DocumentRef docRef, Map<String, Serializable> properties) {
            super(session);
            this.docRef = docRef;
            this.properties = properties;
        }

        @Override
        public void run() throws ClientException {
            DocumentModel doc = session.getSourceDocument(docRef);
            if (doc != null) {
                for (Map.Entry<String, Serializable> property : properties.entrySet()) {

                    doc.setPropertyValue(property.getKey(), property.getValue());
                }

                session.saveDocument(doc);
            }

        }

    }

}
