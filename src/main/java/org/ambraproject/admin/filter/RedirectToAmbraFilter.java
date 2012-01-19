/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.admin.filter;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topazproject.ambra.configuration.ConfigurationStore;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter used to redirect actions that should be handled by the associated ambra stack (e.g. fetching the image for an
 * issue image, browsing an issue, etc.)
 *
 * @author Alex Kudick  1/18/12
 */
public class RedirectToAmbraFilter implements Filter {
  private static final Logger log = LoggerFactory.getLogger(RedirectToAmbraFilter.class);

  private String ambraUrl;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    Configuration ambraConfiguration = ConfigurationStore.getInstance().getConfiguration();
    ambraUrl = ambraConfiguration.getString("ambra.platform.webserverUrl");
    log.debug("Set up to redirect requests for ambra pages to {} ", ambraUrl);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    String requestUri = ((HttpServletRequest) request).getRequestURI();
    String queryString = ((HttpServletRequest) request).getQueryString();
    //the context of the admin app, which we want to replace
    String currentContext = ((HttpServletRequest) request).getContextPath();

    String redirectUrl = ambraUrl + requestUri.replaceFirst(currentContext + "/", "") + '?' + queryString;
    log.debug("Redirecting request for {} to {}", requestUri, redirectUrl);
    ((HttpServletResponse) response).sendRedirect(redirectUrl);
  }

  @Override
  public void destroy() {
    //do nothing
  }
}
