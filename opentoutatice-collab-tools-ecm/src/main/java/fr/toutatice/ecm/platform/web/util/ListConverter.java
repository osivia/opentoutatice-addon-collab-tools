/*
 * (C) Copyright 2014 Académie de Rennes (http://www.ac-rennes.fr/) and others.
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
 * dchevrier
 * 
 */
package fr.toutatice.ecm.platform.web.util;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.commons.lang.StringUtils;


/**
 * @author david chevrier.
 *
 */
@FacesConverter("fr.toutatice.ecm.platform.web.util.ListConverter")
public class ListConverter implements Converter {
    
    public static final String LIST_SEPARATOR = ",";

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        String[] split = StringUtils.split(value, LIST_SEPARATOR);
        
        List<String> elements = new ArrayList<String>(split.length);
        
        for(String item: split){
            elements.add(item);
        }
        
        return elements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return StringUtils.join((List<String>) value, LIST_SEPARATOR);
    }

}
