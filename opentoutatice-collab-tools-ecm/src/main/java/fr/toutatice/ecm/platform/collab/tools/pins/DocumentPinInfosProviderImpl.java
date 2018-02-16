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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
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
	private static final String FACET_SETS = "Sets";
	private static final String LIST_WEBID_PROPERTY= "webids";
	private static final String NAME_PROPERTY= "name";
	private static final String SCHEMA_TOUTATICE = "toutatice";
	private static final String WEBID_PROPERTY = "ttc:webid";
	private static final String SETS_PROPERTY = "sets:sets";
	private static final String SET_PROPERTY = "set";
	private static final String SETS_SCHEMA = "sets";
	private static final String PINS_PROPERTY = "pins";

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

	/**
     * {@inheritDoc}
     */
	@Override
	public void pin(CoreSession coreSession, DocumentModel currentDocument) {

		if (getStatus(coreSession, currentDocument) == PinStatus.can_pin) {
			//Get workspace
			DocumentModel workspace = ToutaticeDocumentHelper.getWorkspace(coreSession, currentDocument, true);

			//Get Sets schema
			Map<String, Object> map = workspace.getProperties(SETS_SCHEMA);

			//Get list of set
			ArrayList<HashMap<String, Object>> listSets = (ArrayList) map.get(SETS_PROPERTY);

			boolean pinSetExist = false;
			String webid = (String) currentDocument.getProperty(SCHEMA_TOUTATICE, WEBID_PROPERTY);
			int index = 0;

			//Read throug the list of set to find the pin set
			for(HashMap<String, Object> set : listSets)
			{
				if (PINS_PROPERTY.equals(set.get(NAME_PROPERTY)))
				{
					pinSetExist = true;
					String[] listPins = (String[]) set.get(LIST_WEBID_PROPERTY);

					if (listPins != null)
					{
						String[] newListPins = new String[listPins.length+1];
						for (int i=0; i<listPins.length; i++)
						{
							newListPins[i] = listPins[i];
						}
						//Add the webid to the webids list
						newListPins[listPins.length] = webid;
						set.put(LIST_WEBID_PROPERTY, newListPins);

						//Set the new set properties to the workspace
						workspace.setPropertyValue(SETS_PROPERTY+"/"+SET_PROPERTY+"["+index+"]", set);

						//Save workspace silently
						ToutaticeDocumentHelper.saveDocumentSilently(coreSession, workspace, true);
					}
				}
				index++;
			}

			if (!pinSetExist)
			{
				int pinSetIndex = 0;
				if (listSets !=null)  pinSetIndex = listSets.size();
				
				//Init pin set properties
				Map<String, Object> mapSet = new HashMap<>();
				String[] webids = new String[1];
				webids[0] = webid;
				mapSet.put(NAME_PROPERTY, "pins");
				mapSet.put(LIST_WEBID_PROPERTY, webids);

				Map<String, String> mapSets = new HashMap<>();
				try {
					
					mapSets.put(SET_PROPERTY, convertToJson(mapSet));
					//Set the new set properties to the workspace
					workspace.setPropertyValue(SETS_PROPERTY+"/"+SET_PROPERTY+"["+pinSetIndex+"]", (Serializable) mapSets);

					//Save workspace silently
					ToutaticeDocumentHelper.saveDocumentSilently(coreSession, workspace, true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} else {
			throw new ClientException("User can not pin this document");
		}

	}

	/**
	 * Convert object to Json
	 * @param object
	 * @return
	 * @throws IOException
	 */
	private String convertToJson(Object object) throws IOException
	{
		// JSON object writer
		ObjectMapper mapper = new ObjectMapper();
		ObjectWriter writer = mapper.writer();

		return writer.writeValueAsString(object);
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public void unPin(CoreSession coreSession, DocumentModel currentDocument)
			throws ClientException, ClassNotFoundException {

		if (getStatus(coreSession, currentDocument) == PinStatus.can_unpin) {
			//Get workspace
			DocumentModel workspace = ToutaticeDocumentHelper.getWorkspace(coreSession, currentDocument, true);

			//Get sets schema
			Map<String, Object> map = workspace.getProperties(SETS_SCHEMA);
			
			//Get list of set
			ArrayList<HashMap<String, Object>> listSets = (ArrayList) map.get(SETS_PROPERTY);

			String webid = (String) currentDocument.getProperty(SCHEMA_TOUTATICE, WEBID_PROPERTY);
			int index = 0;
			
			//Read throug the list of set to find the pin set
			for(HashMap<String, Object> set : listSets)
			{
				if (PINS_PROPERTY.equals(set.get(NAME_PROPERTY)))
				{
					String[] listPins = (String[]) set.get(LIST_WEBID_PROPERTY);

					if (listPins != null && listPins.length > 0)
					{
						//Creation of a new list of webids of pin document without the current document's webid
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
						set.put(LIST_WEBID_PROPERTY, newListPins);

						//Set the new set properties to the workspace
						workspace.setPropertyValue(SETS_PROPERTY+"/"+SET_PROPERTY+"["+index+"]", set);
						
						//Save workspace silently
						ToutaticeDocumentHelper.saveDocumentSilently(coreSession, workspace, true);
					}

				}
				index++;
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
	 * Evaluation status of pin for the document
	 * 
	 * @param coreSession
	 * @param currentDocument
	 * @return a status
	 * @throws ClientException
	 */
	private PinStatus getStatus(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {

		PinStatus status = PinStatus.cannot_pin;

		// Test if workspace parent has Sets facet
		//- if true
		//   Test if document is already pin
		//   - if true, return can_unpin
		//   - if false, return can_pin
		//
		//- if false, return cannot_pin

		String webid = (String) currentDocument.getProperty(SCHEMA_TOUTATICE, WEBID_PROPERTY);
		if (StringUtils.isNotEmpty(webid))
		{
			DocumentModel workspace = ToutaticeDocumentHelper.getWorkspace(coreSession, currentDocument, true);
			if (workspace != null)
			{
				Set<String> facetSet = workspace.getFacets();
				boolean hasSetsFacet = facetSet.contains(FACET_SETS);
				if (hasSetsFacet)
				{
					status = PinStatus.can_pin;
					Map<String, Object> map = workspace.getProperties(SETS_SCHEMA);
					ArrayList<HashMap<String, Object>> listSets = (ArrayList) map.get(SETS_PROPERTY);
					for(HashMap<String, Object> set : listSets)
					{
						if (PINS_PROPERTY.equals(set.get(NAME_PROPERTY)))
						{
							String[] listPins = (String[]) set.get(LIST_WEBID_PROPERTY);
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
		}

		return status;
	}
}
