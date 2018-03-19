/**
 * 
 */
package fr.toutatice.ecm.platform.collab.tools.notifications.exception;

import org.nuxeo.ecm.core.api.NuxeoException;


/**
 * @author david
 *
 */
public class DeleteUserSubscriptionException extends NuxeoException {

    private static final long serialVersionUID = -841424428769289164L;

    /**
     * @param message
     */
    public DeleteUserSubscriptionException(String message) {
        super(message);
    }

}
