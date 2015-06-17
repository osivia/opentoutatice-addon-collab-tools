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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.BooleanUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.runtime.api.Framework;

import fr.toutatice.ecm.platform.collab.tools.constants.CollabToolsConstants;
import fr.toutatice.ecm.platform.core.constants.ToutaticeNuxeoStudioConst;
import fr.toutatice.ecm.platform.core.helper.ToutaticeSilentProcessRunnerHelper;
import fr.toutatice.ecm.platform.service.portalviews.adapter.WidgetsAdapterService;


/**
 * @author david chevrier.
 *
 */
public class WidgetsAgendaAdapterListener implements EventListener {

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm";

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleEvent(Event event) throws ClientException {
        if (event.getContext() instanceof DocumentEventContext) {
            String eventName = event.getName();

            if (DocumentEventTypes.DOCUMENT_CREATED.equals(eventName) || DocumentEventTypes.BEFORE_DOC_UPDATE.equals(eventName)) {

                EventContext ctx = event.getContext();
                DocumentEventContext docCtx = (DocumentEventContext) event.getContext();
                DocumentModel document = docCtx.getSourceDocument();

                CoreSession session = ctx.getCoreSession();
                if (document != null && !document.isImmutable()) {
                    if (document.hasSchema(CollabToolsConstants.CST_DOC_SCHEMA_EVENT) && document.hasSchema(ToutaticeNuxeoStudioConst.CST_DOC_SCHEMA_TTC_EVENT)) {

                        DateNTimeSilentFiller runner = new DateNTimeSilentFiller(session, document, eventName);
                        runner.silentRun(false);

                    }
                }
            }
        }

    }

    private class DateNTimeSilentFiller extends ToutaticeSilentProcessRunnerHelper {

        private DocumentModel document;
        private String eventName;

        public DateNTimeSilentFiller(CoreSession session, DocumentModel document, String eventName) {
            super(session);
            this.document = document;
            this.eventName = eventName;
        }

        @Override
        public void run() throws ClientException {
            boolean isChangeableDocument = DocumentEventTypes.DOCUMENT_CREATED.equals(eventName);

            WidgetsAdapterService waService = Framework.getService(WidgetsAdapterService.class);

            if (waService.isInPortalViewContext()) {
                fromTTCToNxDate();
            } else {
                fromNxToTTCDate();
            }

            if (isChangeableDocument) {
                this.session.saveDocument(this.document);
            }
        }

        private void fromTTCToNxDate() {
            Boolean allDay = (Boolean) this.document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_ALL_DAY);
            if (allDay != null) {
                this.document.setPropertyValue(CollabToolsConstants.CST_DOC_XPATH_EVENT_ALL_DAY, allDay);
            }

            Calendar ttcDateTimeStart = (GregorianCalendar) this.document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_BEGIN);
            if (null != ttcDateTimeStart) {
                this.document.setPropertyValue(CollabToolsConstants.CST_DOC_XPATH_EVENT_START, ttcDateTimeStart.getTime());
            }

            Calendar ttcDateTimeEnd = (GregorianCalendar) this.document.getPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_END);
            if (null != ttcDateTimeEnd) {
                this.document.setPropertyValue(CollabToolsConstants.CST_DOC_XPATH_EVENT_END, ttcDateTimeEnd.getTime());
            }
        }

        private void fromNxToTTCDate() {
            SimpleDateFormat formatDate = new SimpleDateFormat(DATE_FORMAT);
            SimpleDateFormat formatTime = new SimpleDateFormat(TIME_FORMAT);

            Boolean allDay = (Boolean) this.document.getPropertyValue(CollabToolsConstants.CST_DOC_XPATH_EVENT_ALL_DAY);
            if (allDay != null) {
                this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_ALL_DAY, allDay);
            }

            Calendar nxDateTimeStart = (GregorianCalendar) this.document.getPropertyValue(CollabToolsConstants.CST_DOC_XPATH_EVENT_START);
            if (null != nxDateTimeStart) {

                Date ttcDateTimeBegin = nxDateTimeStart.getTime();
                this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_BEGIN, ttcDateTimeBegin);

                String dateBegin = formatDate.format(ttcDateTimeBegin);
                this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_BEGIN, dateBegin);

                String timeBegin = formatTime.format(ttcDateTimeBegin);
                if (StringUtils.isNotBlank(timeBegin)) {
                    this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_TIME_BEGIN, timeBegin);
                }


            }

            Calendar nxDateTimeEnd = (GregorianCalendar) this.document.getPropertyValue(CollabToolsConstants.CST_DOC_XPATH_EVENT_END);
            if (null != nxDateTimeEnd) {

                Date ttcDateTimeEnd = nxDateTimeEnd.getTime();
                this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_TIME_END, ttcDateTimeEnd);

                String dateEnd = formatDate.format(ttcDateTimeEnd);
                this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_DATE_END, dateEnd);

                String timeEnd = formatTime.format(ttcDateTimeEnd);
                if (StringUtils.isNotBlank(timeEnd)) {
                    this.document.setPropertyValue(ToutaticeNuxeoStudioConst.CST_DOC_XPATH_TTC_EVT_TIME_END, timeEnd);
                }

            }
        }

    }

}
