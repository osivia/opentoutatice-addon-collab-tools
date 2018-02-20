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
package fr.toutatice.ecm.platform.collab.tools.quickaccess;

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
 * impl of QuickAccess service
 * 
 * @author jbarberet
 * 
 */
public class DocumentQuickAccessInfosProviderImpl implements DocumentQuickAccessInfosProvider {

	private static final String QUICK_ACCESS_STATUS = "quickAccess_status";
	private static final String FACET_SETS = "Sets";
	private static final String LIST_WEBID_PROPERTY= "webids";
	private static final String NAME_PROPERTY= "name";
	private static final String SCHEMA_TOUTATICE = "toutatice";
	private static final String WEBID_PROPERTY = "ttc:webid";
	private static final String SETS_PROPERTY = "sets:sets";
	private static final String SET_PROPERTY = "set";
	private static final String SETS_SCHEMA = "sets";
	private static final String QUICK_ACCESS_PROPERTY = "quickAccess";

	/**
	 * A document has a state depending of its parent workspace
	 */
	public enum QuickAccessStatus {
		/** Default state : can add to quick access set */
		can_add_to_quickaccess,
		/** Can remove from quick access if a quick access is already set */
		can_remove_from_quickaccess,
		/**
		 * Cases : Workspace parent hasn't Set facet, or the document is in a
		 * Publication spaces or personal spaces and quick access is not allowed
		 */
		cannot_add_to_quickaccess;
	};

	/**
     * {@inheritDoc}
     */
	@Override
	public void addToQuickAccess(CoreSession coreSession, DocumentModel currentDocument) {

		if (getStatus(coreSession, currentDocument) == QuickAccessStatus.can_add_to_quickaccess) {
			//Get workspace
			DocumentModel workspace = ToutaticeDocumentHelper.getWorkspace(coreSession, currentDocument, true);

			//Get Sets schema
			Map<String, Object> map = workspace.getProperties(SETS_SCHEMA);

			//Get list of set
			ArrayList<Map<String, Object>> listSets = (ArrayList) map.get(SETS_PROPERTY);

			boolean quickAccessSetExist = false;
			String webid = (String) currentDocument.getProperty(SCHEMA_TOUTATICE, WEBID_PROPERTY);
			int index = 0;

			//Read throug the list of set to find the quickAccess set
			for(Map<String, Object> set : listSets)
			{
				if (QUICK_ACCESS_PROPERTY.equals(set.get(NAME_PROPERTY)))
				{
					quickAccessSetExist = true;
					String[] listQuickAccess = (String[]) set.get(LIST_WEBID_PROPERTY);

					if (listQuickAccess != null)
					{
						String[] newListQuickAccess = new String[listQuickAccess.length+1];
						for (int i=0; i<listQuickAccess.length; i++)
						{
							newListQuickAccess[i] = listQuickAccess[i];
						}
						//Add the webid to the webids list
						newListQuickAccess[listQuickAccess.length] = webid;
						set.put(LIST_WEBID_PROPERTY, newListQuickAccess);

//						//Set the new set properties to the workspace
//						workspace.setPropertyValue(SETS_PROPERTY+"/"+SET_PROPERTY+"["+index+"]", set);
//
//						//Save workspace silently
//						ToutaticeDocumentHelper.saveDocumentSilently(coreSession, workspace, true);
					}
				}
				index++;
			}

			if (!quickAccessSetExist)
			{
				int quickAccessSetIndex = 0;
				if (listSets !=null)  quickAccessSetIndex = listSets.size();
				
				//Init quickAccess set properties
				Map<String, Object> mapSet = new HashMap<>();
				String[] webids = new String[1];
				webids[0] = webid;
				mapSet.put(NAME_PROPERTY, QUICK_ACCESS_PROPERTY);
				mapSet.put(LIST_WEBID_PROPERTY, webids);

				listSets.add(mapSet);
//					//Set the new set properties to the workspace
//					workspace.setPropertyValue(SETS_PROPERTY+"/"+SET_PROPERTY+"["+quickAccessSetIndex+"]", (Serializable) mapSets);
//
//					//Save workspace silently
//					ToutaticeDocumentHelper.saveDocumentSilently(coreSession, workspace, true);
				
			}
			
			Map<String, Object> mapToSave = new HashMap<>();
			mapToSave.put(SETS_PROPERTY, listSets);
		
			//Set the new set properties to the workspace
//				workspace.setPropertyValue(SETS_PROPERTY, (Serializable) listSets);
			workspace.setProperties(SETS_SCHEMA, mapToSave);

			//Save workspace silently
			ToutaticeDocumentHelper.saveDocumentSilently(coreSession, workspace, true);
			
		} else {
			throw new ClientException("User can not add this document to quickaccess set");
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
	public void removeFromQuickAccess(CoreSession coreSession, DocumentModel currentDocument)
			throws ClientException, ClassNotFoundException {

		if (getStatus(coreSession, currentDocument) == QuickAccessStatus.can_remove_from_quickaccess) {
			//Get workspace
			DocumentModel workspace = ToutaticeDocumentHelper.getWorkspace(coreSession, currentDocument, true);

			//Get sets schema
			Map<String, Object> map = workspace.getProperties(SETS_SCHEMA);
			
			//Get list of set
			ArrayList<HashMap<String, Object>> listSets = (ArrayList) map.get(SETS_PROPERTY);

			String webid = (String) currentDocument.getProperty(SCHEMA_TOUTATICE, WEBID_PROPERTY);
			int index = 0;
			
			//Read throug the list of set to find the quickAccess set
			for(HashMap<String, Object> set : listSets)
			{
				if (QUICK_ACCESS_PROPERTY.equals(set.get(NAME_PROPERTY)))
				{
					String[] listQuickAccess = (String[]) set.get(LIST_WEBID_PROPERTY);

					if (listQuickAccess != null && listQuickAccess.length > 0)
					{
						//Creation of a new list of webids of quickAccess document without the current document's webid
						String[] newListQuickAccess = new String[listQuickAccess.length-1];

						int j = 0;
						for (int i=0; i<listQuickAccess.length; i++)
						{
							if (!StringUtils.equals(webid, (String) listQuickAccess[i]))
							{
								newListQuickAccess[j] = listQuickAccess[i];
								j++;
							}
						}
						set.put(LIST_WEBID_PROPERTY, newListQuickAccess);

						//Set the new set properties to the workspace
						workspace.setPropertyValue(SETS_PROPERTY+"/"+SET_PROPERTY+"["+index+"]", set);
						
						//Save workspace silently
						ToutaticeDocumentHelper.saveDocumentSilently(coreSession, workspace, true);
					}

				}
				index++;
			}
		} else {
			throw new ClientException("User can not remove this document from quickaccess set");
		}

	}

	@Override
	public Map<String, Object> fetchInfos(CoreSession coreSession, DocumentModel currentDocument)
			throws ClientException {

		Map<String, Object> infos = new HashMap<String, Object>();

		infos.put(QUICK_ACCESS_STATUS, getStatus(coreSession, currentDocument).toString());

		return infos;
	}

	/**
	 * Evaluation status of quickAccess for the document
	 * 
	 * @param coreSession
	 * @param currentDocument
	 * @return a status
	 * @throws ClientException
	 */
	private QuickAccessStatus getStatus(CoreSession coreSession, DocumentModel currentDocument) throws ClientException {

		QuickAccessStatus status = QuickAccessStatus.cannot_add_to_quickaccess;

		// Test if workspace parent has Sets facet
		//- if true
		//   Test if document is already added in quickAccess set
		//   - if true, return can_remove_from_quickaccess
		//   - if false, return can_add_to_quickaccess
		//
		//- if false, return cannot_add_to_quickaccess

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
					status = QuickAccessStatus.can_add_to_quickaccess;
					Map<String, Object> map = workspace.getProperties(SETS_SCHEMA);
					ArrayList<HashMap<String, Object>> listSets = (ArrayList) map.get(SETS_PROPERTY);
					for(HashMap<String, Object> set : listSets)
					{
						if (QUICK_ACCESS_PROPERTY.equals(set.get(NAME_PROPERTY)))
						{
							String[] listQuickAccess = (String[]) set.get(LIST_WEBID_PROPERTY);
							for (int i=0; i<listQuickAccess.length; i++)
							{
								if (StringUtils.equals(webid, (String) listQuickAccess[i]))
								{
									status = QuickAccessStatus.can_remove_from_quickaccess;
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
