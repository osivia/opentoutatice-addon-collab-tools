/**
 * 
 */
package fr.toutatice.ecm.platform.collab.tools.forum;

import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.nuxeo.ecm.platform.forum.web.api.ThreadAdapter;
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


		if (event.getContext() instanceof DocumentEventContext) {
			DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
			DocumentModel sourceDocument = docCtx.getSourceDocument();
			
			if (sourceDocument != null) {
				
				CoreSession session = sourceDocument.getCoreSession();
				
				if(CollabToolsConstants.THREAD_TYPE.equals(sourceDocument.getType())) {
					updateAnswersOfThread(eventName, sourceDocument,
							session);
				} else if (CollabToolsConstants.POST_TYPE.equals(sourceDocument.getType())){
					DocumentModel thread = getCommentManager().getThreadForComment(sourceDocument);
					updateAnswersOfThread(eventName, thread, session);
				}

			}
		}
	}
	
	/**
	 * Updates the datas about Thread answers.
	 * 
	 * @param eventName
	 * @param doc
	 * @param session
	 */
	private void updateAnswersOfThread(String eventName,
			DocumentModel doc, CoreSession session) {

		// To force reload of Posts
		session.save();
				
		// -------------
		
		List<DocumentModel> answers = getCommentManager().getComments(doc);
		
		Serializable lastCommentAuthor = doc.getPropertyValue("dc:creator");
		GregorianCalendar lastCommentDate = (GregorianCalendar) doc.getPropertyValue("dc:created");
		for(DocumentModel answer : answers) {
			Serializable author = answer.getPropertyValue("post:author");
			
			GregorianCalendar creationDate = (GregorianCalendar) answer.getPropertyValue("post:creationDate");
			
			if(lastCommentDate == null || lastCommentDate.before(creationDate)) {
				lastCommentDate = creationDate;
				lastCommentAuthor = author;
			}
		}
		
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put(CollabToolsConstants.TTC_THREAD_NB_COMMENTS_XPATH, answers.size());
		properties.put(CollabToolsConstants.TTC_THREAD_LAST_COMMENT_DATE_XPATH, lastCommentDate);
		properties.put(CollabToolsConstants.TTC_THREAD_LAST_COMMENT_AUTHOR_XPATH, lastCommentAuthor);
		
		UnrestrictedPropertySetter propertySetter = new UnrestrictedPropertySetter(session,doc.getRef(),
				properties);
		propertySetter.runUnrestricted();

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
		
		Map<String, Serializable> properties;


		protected UnrestrictedPropertySetter(CoreSession session,
				DocumentRef docRef, Map<String, Serializable> properties) {
			super(session);
			this.docRef = docRef;
			this.properties = properties;
		}

		@Override
		public void run() throws ClientException {
			DocumentModel doc = session.getSourceDocument(docRef);
			if (doc != null) {
				for(Map.Entry<String, Serializable> property : properties.entrySet()) {

					doc.setPropertyValue(property.getKey(), property.getValue());
				}
				
				session.saveDocument(doc);
			}

		}

	}

}
