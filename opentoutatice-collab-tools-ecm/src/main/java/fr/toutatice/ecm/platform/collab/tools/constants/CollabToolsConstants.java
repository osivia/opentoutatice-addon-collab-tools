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
 *   mberhaut1
 *    
 */
package fr.toutatice.ecm.platform.collab.tools.constants;


public interface CollabToolsConstants {
    
    String CST_DOC_SCHEMA_ARTICLE = "annonce";
    String CST_DOC_SCHEMA_ARTICLE_PREFIX = "annonce";
    
    String CST_DOC_XPATH_HEAD_IMAGE = CST_DOC_SCHEMA_ARTICLE_PREFIX + ":image";
    
    String CST_DOC_SCHEMA_EVENT = "vevent";
    String CST_DOC_XPATH_EVENT_ALL_DAY = CST_DOC_SCHEMA_EVENT + ":allDay";
    String CST_DOC_XPATH_EVENT_START = CST_DOC_SCHEMA_EVENT + ":dtstart";
    String CST_DOC_XPATH_EVENT_END = CST_DOC_SCHEMA_EVENT + ":dtend";
    
    String THREAD_TYPE = "Thread";
    String POST_TYPE = "Post";
    String TTC_THREAD_SCHEMA = "thread_toutatice";
    String TTC_THREAD_NB_COMMENTS_XPATH = "ttcth:nbComments";

}
