/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.action;

import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.admin.service.AdminService;
import org.ambraproject.models.Journal;
import org.springframework.beans.factory.annotation.Required;

public class BaseAdminActionSupport extends BaseActionSupport {
  protected static final String IMPORT_USER_LIST = "IMPORT_USER_LIST";
  protected static final String IMPORT_USER_LIST_PERMISSIONS = "IMPORT_USER_LIST_PERMISSIONS";
  protected static final String IMPORT_PROFILE_PASSWORD_URL = "ambra.services.registration.url.change-password-imported-profile";
  protected static final String IMPORT_USERS_EMAIL_TITLE = "ambra.services.importUsers.defaultEmailTitle";
  protected static final String IMPORT_USERS_EMAIL_FROM = "ambra.services.importUsers.defaultFromEmail";

  protected AdminService adminService;

  // Fields Used by template
  private Journal journalInfo;

  /**
    * Gets the JournalInfo value object for access in the view.
    *
    * @return Current virtual Journal value object
    */
   public Journal getJournal() {
     return journalInfo;
   }

  /**
   * Sets the AdminService.
   *
   * @param  adminService The adminService to use.
   */
  @Required
  public void setAdminService(AdminService adminService) {
    this.adminService = adminService;
  }

  protected void initJournal() {
    journalInfo = adminService.getJournal(getCurrentJournal());
  }
}
