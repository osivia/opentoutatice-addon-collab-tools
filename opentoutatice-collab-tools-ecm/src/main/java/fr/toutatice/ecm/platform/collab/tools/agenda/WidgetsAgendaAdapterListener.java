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
 * lbillon
 * dchevrier
 */
package fr.toutatice.ecm.platform.collab.tools.agenda;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

import fr.toutatice.ecm.platform.collab.tools.constants.CollabToolsConstants;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;


/**
 * @author david chevrier.
 *
 */
public class WidgetsAgendaAdapterListener implements EventListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(Event event) throws ClientException {
        if (event.getContext() instanceof DocumentEventContext) {
            EventContext ctx = event.getContext();
            DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
            DocumentModel document = docCtx.getSourceDocument();

            String eventName = event.getName();
            CoreSession session = ctx.getCoreSession();
            if (document != null && !document.isImmutable()) {
                if (document.hasSchema(CollabToolsConstants.CST_DOC_SCHEMA_EVENT) && document.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TTC_EVENT)) {
                    try {
                        fillNxDateNTime(document, session, eventName);
                    } catch (ParseException e) {
                        throw new ClientException(e);
                    }
                }
            }
        }

    }

    public void fillNxDateNTime(DocumentModel document, CoreSession session, String eventName) throws ParseException {
        boolean isChangeableDocument = DocumentEventTypes.DOCUMENT_CREATED.equals(eventName);
        boolean toManage = false;
        
        Calendar ttcDateTimeStart = (GregorianCalendar) document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_BEGIN);
        document.setPropertyValue(CollabToolsConstants.CST_DOC_XPATH_EVENT_START, ttcDateTimeStart.getTime());

        Calendar ttcDateTimeEnd = (GregorianCalendar) document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_END);
        document.setPropertyValue(CollabToolsConstants.CST_DOC_XPATH_EVENT_END, ttcDateTimeEnd.getTime());

        if (toManage && isChangeableDocument) {
            session.saveDocument(document);
        }

    }


}
