/*
 * $HeadURL: http://svn.ambraproject.org/svn/ambra/ambra-admin/branches/january_fixes/src/main/java/org/ambraproject/search/service/ArticleIndexingService.java $
 * $Id: ArticleIndexingService.java 10898 2012-04-23 22:53:23Z josowski $
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

package org.ambraproject.search.service;

import org.apache.commons.configuration.Configuration;

/**
 * Service class for indexing SOLR data
 *
 * @author Dragisa Krsmanovic
 * @author Bill OConnor
 * @author Joe Osowski
 */
public interface IndexingService {

  /**
   * Send one articles for re-indexing.
   *
   * @param articleId Article ID
   * @throws Exception if operation fails
   */
  public void indexArticle(String articleId) throws Exception;

  /**
   * Method that is fired on article publish operation.
   * <p/>
   * Message is sent to an asynchronous, SEDA queue and from there it's sent to plos-queue. That
   * way we ensure that publish operation will succeed even if ActiveMQ is down.
   *
   * @see org.ambraproject.queue.Routes
   * @param articleId ID of the published article
   * @param authId the authorization ID of the current user
   * @throws Exception if message send fails
   */
  public void articlePublished(String articleId, String authId) throws Exception;

  /**
   * Method that is fired on article cross publish operation.
   * <p/>
   * Message is sent to an asynchronous, SEDA queue and from there it's sent to plos-queue. That
   * way we ensure that cross publish operation will succeed even if ActiveMQ is down.
   *
   * @see org.ambraproject.queue.Routes
   * @param articleId ID of the cross published article
   * @throws Exception if message send fails
   */
  public void articleCrossPublished(String articleId) throws Exception;

  /**
   * Set the ambra configuration to use
   *
   * @param ambraConfiguration
   */
  public void setAmbraConfiguration(Configuration ambraConfiguration);

  /**
   * Method that is fired on article delete operation.
   * <p/>
   * Message is sent to an asynchronous, SEDA queue and from there it's sent to plos-queue. That
   * way we ensure that delete operation will succeed even if ActiveMQ is down.
   *
   * @see org.ambraproject.queue.Routes
   * @param articleId ID of the deleted article
   * @throws Exception if message send fails
   */
  public void articleDeleted(String articleId) throws Exception;

  /**
   * Grab all the academic editors from the raptor service and send them to SOLR for a reindex
   *
   * @throws Exception if anything fails
   */
  public void reindexAcademicEditors() throws Exception;
}
