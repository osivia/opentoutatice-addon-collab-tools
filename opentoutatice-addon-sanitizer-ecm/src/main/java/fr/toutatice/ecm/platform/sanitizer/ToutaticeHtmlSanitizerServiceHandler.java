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
package fr.toutatice.ecm.platform.sanitizer;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
import org.nuxeo.ecm.platform.htmlsanitizer.FieldDescriptor;
import org.nuxeo.ecm.platform.htmlsanitizer.HtmlSanitizerDescriptor;
import org.nuxeo.ecm.platform.htmlsanitizer.HtmlSanitizerServiceImpl;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;

public class ToutaticeHtmlSanitizerServiceHandler<T> extends HtmlSanitizerServiceImpl implements InvocationHandler {
	private static final Log log = LogFactory.getLog(ToutaticeHtmlSanitizerServiceHandler.class);

	protected T object;

	public ToutaticeHtmlSanitizerServiceHandler() {
		this(null);
	}

	public ToutaticeHtmlSanitizerServiceHandler(T object) {
		setObject(object);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			// TODO: check the method signature, not only its name
			if ("sanitizeDocument".contains(method.getName())) {
				DocumentModel doc = (DocumentModel) args[0];

				if (policy == null) {
					log.error("Cannot sanitize, no policy registered");
					return null;
				}
				
				for (HtmlSanitizerDescriptor sanitizer : sanitizers) {
					if (!sanitizer.types.isEmpty()
							&& !sanitizer.types.contains(doc.getType())) {
						continue;
					}
					for (FieldDescriptor field : sanitizer.fields) {
						String fieldName = field.getContentField();
						String filterField = field.getFilterField();
						if (filterField != null) {
							Property filterProp;
							try {
								filterProp = doc.getProperty(filterField);
							} catch (PropertyNotFoundException e) {
								continue;
							}
							if (field.match(String.valueOf(filterProp.getValue())) != field.doSanitize()) {
								continue;
							}
						}
						Property prop;
						try {
							prop = doc.getProperty(fieldName);
						} catch (PropertyNotFoundException e) {
							continue;
						}
						Serializable value = prop.getValue();
						if (value == null) {
							continue;
						}
						if (!(value instanceof String)) {
							log.debug("Cannot sanitize non-string field: " + field);
							continue;
						}
						String info = "doc " + doc.getPathAsString() + " (" + doc.getId() + ") field " + field;
						String newValue = sanitizeString((String) value, info);
						if (!newValue.equals(value)) {
							prop.setValue(newValue);
						}
					}
				}
				return null;
			} else {
				return method.invoke(this.object, args);
			}
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	@Override
	public String sanitizeString(String string, String info) {
		if (policy == null) {
			log.error("Cannot sanitize, no policy registered");
			return string;
		}
		try {
			CleanResults cr = new AntiSamy().scan(string, policy);
			for (Object err : cr.getErrorMessages()) {
				log.debug(String.format("Sanitizing %s: %s", info == null ? "" : info, err));
			}
			return cr.getCleanHTML();
		} catch (Exception e) {
			log.error(String.format("Cannot sanitize %s: %s", info == null ? "" : info, e));
			return string;
		}
	}

	public T newProxy(T object, Class<T> itf) {
		setObject(object);
		return itf.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[] { itf }, this));
	}

	protected void setObject(T object) {
		this.object = object;
		loadProxiedServiceRegistries();
	}

	/**
	 * Load the registered contribution done by the proxied (native) service.
	 */
	private void loadProxiedServiceRegistries() {
		if (null != this.object) {
			this.policy = ((HtmlSanitizerServiceImpl) this.object).policy;
			this.sanitizers = ((HtmlSanitizerServiceImpl) this.object).sanitizers;
			this.allPolicies = ((HtmlSanitizerServiceImpl) this.object).allPolicies;
			this.allSanitizers = ((HtmlSanitizerServiceImpl) this.object).allSanitizers;
		}
	}

}
