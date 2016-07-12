/**
 * 
 */
package fr.toutatice.ecm.platform.collab.tools.forum;

import java.io.Serializable;
import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.comment.api.CommentEvents;
import org.nuxeo.ecm.platform.comment.api.CommentManager;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.collab.tools.constants.CollabToolsConstants;
import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * Update Thread on added, removed Post.
 * 
 * @author david
 *
 */
public class ThreadUpdateListener implements EventListener {
	
	/** CommentManager. */
	protected static CommentManager commentManager;
	
	protected CommentManager getCommentManager(){
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
		
		String eventName = event.getName();
		
		if (CommentEvents.COMMENT_ADDED.equals(eventName)
				|| CommentEvents.COMMENT_REMOVED.equals(eventName)) {

			if (event.getContext() instanceof DocumentEventContext) {
				DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
				DocumentModel sourceDocument = docCtx.getSourceDocument();
				
				if (sourceDocument != null) {
					
					CoreSession session = sourceDocument.getCoreSession();
					
					if(CollabToolsConstants.THREAD_TYPE.equals(sourceDocument.getType())) {
						updateNbAnswersOfThread(eventName, sourceDocument,
								session);
					} else if (CollabToolsConstants.POST_TYPE.equals(sourceDocument.getType())){
						DocumentModel thread = getCommentManager().getThreadForComment(sourceDocument);
						updateNbAnswersOfThread(eventName, thread, session);
					}

				}
			}

		}
		
	}
	
	/**
	 * Updates the numbers of Thread answers.
	 * 
	 * @param eventName
	 * @param thread
	 * @param session
	 */
	private void updateNbAnswersOfThread(String eventName,
			DocumentModel thread, CoreSession session) {

		// To force reload of Posts
		session.save();
		List<DocumentModel> nbAnswers = getCommentManager().getComments(thread);
		
		// Update number of comments on Thread in unrestricted way cause user who add Post may not have Write permission  on Thread.
		// In case of deleted Post, the Thread is considered as not modified (so saved in silent way).
		if(CommentEvents.COMMENT_REMOVED.equals(eventName)){
			thread.setPropertyValue(CollabToolsConstants.TTC_THREAD_NB_COMMENTS_XPATH, nbAnswers.size());
			ToutaticeDocumentHelper.saveDocumentSilently(session, thread, true);
		} else {
			UnrestrictedPropertySetter propertySetter = new UnrestrictedPropertySetter(session,thread.getRef(),
					CollabToolsConstants.TTC_THREAD_NB_COMMENTS_XPATH, nbAnswers.size());
			propertySetter.runUnrestricted();
			// Contributors and modification date are updated by DublincoreListener
			// (it uses originating user)
		}
	}
	
	/**
	 * To set a property in unrestricted way.
	 * 
	 * @author david
	 *
	 */
	//FIXME: to move in ToutaticeDocumentHelper.
	protected class UnrestrictedPropertySetter extends UnrestrictedSessionRunner {

		DocumentRef docRef;

		String xpath;

		Serializable value;

		protected UnrestrictedPropertySetter(CoreSession session,
				DocumentRef docRef, String xpath, Serializable value) {
			super(session);
			this.docRef = docRef;
			this.xpath = xpath;
			this.value = value;
		}

		@Override
		public void run() throws ClientException {
			DocumentModel doc = session.getSourceDocument(docRef);
			if (doc != null) {
				doc.setPropertyValue(xpath, value);
				session.saveDocument(doc);
			}

		}

	}

}
