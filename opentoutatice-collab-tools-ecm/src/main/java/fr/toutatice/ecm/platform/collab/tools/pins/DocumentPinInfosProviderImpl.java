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
package fr.toutatice.ecm.platform.collab.tools.pins;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentHelper;

/**
 * impl of Pin service
 * 
 * @author jbarberet
 * 
 */
public class DocumentPinInfosProviderImpl implements DocumentPinInfosProvider {

	private static final String PIN_STATUS = "pin_status";
	private static final String FACET_HAS_PINS = "HasPins";
	private static final String LIST_PINS = "pin:listwebid";
	private static final String SCHEMA_TOUTATICE = "toutatice";
	private static final String WEBID = "ttc:webid";

	/**
	 * A document has a state depending of its parent workspace
	 */
	public enum PinStatus {
		/** Default state : can pin */
		can_pin,
		/** Can unpin if a pin is already set */
		can_unpin,
		/**
		 * Cases : Workspace parent hasn't HasPins facet, or the document is in a
		 * Publication spaces or personal spaces and pin is not allowed
		 */
		cannot_pin;
	};

	@Override
	public void pin(CoreSession coreSession, DocumentModel currentDocument) {

		if (getStatus(coreSession, currentDocument) == PinStatus.can_pin) {
			DocumentModel workspace = ToutaticeDocumentHelper.getWorkspace(coreSession, currentDocument, true);
			String[] listPins = (String[]) workspace.getPropertyValue(LIST_PINS);
			
			String webid = (String) currentDocument.getProperty(SCHEMA_TOUTATICE, WEBID);
			if (listPins != null)
			{
				String[] newListPins = new String[listPins.length+1];
				for (int i=0; i<listPins.length; i++)
				{
					newListPins[i] = listPins[i];
				}
				newListPins[listPins.length] = webid;
				workspace.setPropertyValue(LIST_PINS, newListPins);
			}
			
			ToutaticeDocumentHelper.saveDocumentSilently(coreSession, workspace, true);
		} else {
			throw new ClientException("User can not pin this document");
		}

	}

	@Override
	public void unPin(CoreSession coreSession, DocumentModel currentDocument)
			throws ClientException, ClassNotFoundException {

		if (getStatus(coreSession, currentDocument) == PinStatus.can_unpin) {
			DocumentModel workspace = ToutaticeDocumentHelper.getWorkspace(coreSession, currentDocument, true);
			String[] listPins = (String[]) workspace.getPropertyValue(LIST_PINS);
			String webid = (String) currentDocument.getProperty(SCHEMA_TOUTATICE, WEBID);
			
			if (listPins != null && listPins.length > 0)
			{
				String[] newListPins = new String[listPins.length-1];
				int j = 0;
				for (int i=0; i<listPins.length; i++)
				{
					if (!StringUtils.equals(webid, (String) listPins[i]))
					{
						newListPins[j] = listPins[i];
						j++;
					}
				}
				workspace.setPropertyValue(LIST_PINS, newListPins);
				ToutaticeDocumentHelper.saveDocumentSilently(coreSession, workspace, true);
			}
		} else {
			throw new ClientException("User can not unsubscribe to this document");
		}

	}

	@Override
	public Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDocument)
			throws ClientException {

		Map<String, Object> infos = new HashMap<String, Object>();

		infos.put(PIN_STATUS, getStatus(coreSession, currentDocument).toString());

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
	private PinStatus getStatus(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {

		PinStatus status = PinStatus.cannot_pin;

		//1) tester si le workspace parent a la facet HasPins
		//- si oui
		//Tester si le document est déjà épinglé
		// - si oui, retourner can_unpin
		// - si non, retourner can_pin
		//
		// - si non, retourner cannot_pin

		String webid = (String) currentDocument.getProperty(SCHEMA_TOUTATICE, WEBID);
		if (StringUtils.isNotEmpty(webid))
		{
			DocumentModel workspace = ToutaticeDocumentHelper.getWorkspace(coreSession, currentDocument, true);
			if (workspace != null)
			{
				Set<String> facetSet = workspace.getFacets();
				boolean hasPinFacet = facetSet.contains(FACET_HAS_PINS);
				if (hasPinFacet)
				{
					String[] listPins = (String[]) workspace.getPropertyValue(LIST_PINS);
					status = PinStatus.can_pin;
					if (listPins!= null && listPins.length > 0)
					{
						
						for (int i=0; i<listPins.length; i++)
						{
							if (StringUtils.equals(webid, (String) listPins[i]))
							{
								status = PinStatus.can_unpin;
								break;
							}
						}
					}
				}
			}
		}

		return status;
	}
}
