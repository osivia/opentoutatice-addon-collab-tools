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
 * mberhaut1
 * dchevrier
 */
package fr.toutatice.ecm.platform.collab.tools.mail;

import static org.jboss.seam.ScopeType.EVENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.core.api.NuxeoGroup;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.platform.ui.web.util.SeamComponentCallHelper;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.ecm.webapp.notification.email.EmailNotificationSenderActionsBean;
import org.nuxeo.ecm.webapp.security.PrincipalListManager;

import fr.toutatice.ecm.platform.core.constants.ExtendedSeamPrecedence;
import fr.toutatice.ecm.platform.web.fragments.PageBean;


/**
 * @author david chevrier
 *
 */
@Name("emailNotifSenderAction")
@Scope(EVENT)
@Install(precedence = ExtendedSeamPrecedence.TOUTATICE)
public class ToutaticeEmailNotificationSenderActionsBean extends EmailNotificationSenderActionsBean {

    /* To send portal notification */
    @In(create = true)
    PageBean pageBean;

    @In(create = true)
    transient UserManager userManager;
    
    /** To keep data in confirm portal view. */
    protected List<String> savedRecipients;
    
    private Integer totalUserCount ;
    
    /** To resolve converter problem on hidden input in confirm portal view */

    private static final long serialVersionUID = -5722586829804268237L;

    protected transient PrincipalListManager principalManager = (PrincipalListManager) SeamComponentCallHelper
            .getSeamComponentByName("principalListManager");

    /**
     * To display names of groups and number of group members.
     */
    public Map<String, Integer> getDisplayedGroups() {
        Map<String, Integer> displayedGroups = new HashMap<String, Integer>();

        List<String> recipients = getRecipients();

        for (String recipient : recipients) {
            NuxeoGroup group = userManager.getGroup(recipient);
            if (group != null) {
                List<String> memberUsers = group.getMemberUsers();
                int nbMembers = memberUsers != null ? memberUsers.size() : 0;
                displayedGroups.put(group.getLabel(), nbMembers);
            }
        }

        return displayedGroups;
    }


    /**
     * To display first name and last name of recipients,
     * and names of groups.
     */
    public ArrayList<String> getDisplayedRecipients() {
        ArrayList<String> displayedRecipients = new ArrayList<String>();

        List<String> recipients = getRecipients();

        for (String recipient : recipients) {
            NuxeoPrincipal principal = userManager.getPrincipal(recipient);
            if (principal != null) {
                displayedRecipients.add(principal.getFirstName() + " " + principal.getLastName());
            }
        }

        return displayedRecipients;
    }
    
    /**
     * To "pre-fill" recipients
     */
    @Override
    public List<String> getRecipients() {
        if (this.savedRecipients == null) {
            this.savedRecipients = new ArrayList<String>();
//            List<String> defaultRecipients = super.getRecipients();
//            if (CollectionUtils.isNotEmpty(defaultRecipients)) {
//                this.savedRecipients.addAll(defaultRecipients);
//            }
//            List<String> selectedUsers = principalManager.getSelectedUsers();
//            if (CollectionUtils.isNotEmpty(selectedUsers)) {
//                this.savedRecipients.addAll(selectedUsers);
//            }
                        
            super.setRecipients(this.savedRecipients);
            
//            if(totalUserCount == null) {
//
//            }
        }
        return this.savedRecipients;
    }

    @Override
    public void setRecipients(List<String> recipients) {
        this.savedRecipients = recipients;
        super.setRecipients(this.savedRecipients);

    }
    
    
    
    public Integer totalUserCount(List<String> recipients) {
    	
        Set<String> all = new HashSet<>();
        
        for (String recipient : savedRecipients) {
        	NuxeoPrincipal principal = userManager.getPrincipal(recipient);
            if (principal != null) {
            	all.add(principal.getName());
            }
            else {
            	NuxeoGroup group = userManager.getGroup(recipient);
                if (group != null) {
                    List<String> memberUsers = group.getMemberUsers();

                    all.addAll(memberUsers);
                }
            }
        }
		return all.size();
	}


	public String redirect(String viewId) {
        return viewId;
    }

    public String send(String viewId) {
        String view = super.send();
        if (view != null) {
            pageBean.setNotificationKey("SUCCESS_MAIL_SENT");
            view = viewId;
        }
        return view;
    }

}
