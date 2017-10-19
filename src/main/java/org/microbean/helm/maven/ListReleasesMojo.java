/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017 MicroBean.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.helm.maven;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import hapi.release.ReleaseOuterClass.Release;
import hapi.release.StatusOuterClass.Status;

import hapi.services.tiller.Tiller.ListReleasesRequest;
import hapi.services.tiller.Tiller.ListReleasesResponse;
import hapi.services.tiller.Tiller.ListSort.SortBy;
import hapi.services.tiller.Tiller.ListSort.SortOrder;
import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.microbean.helm.ReleaseManager;

@Mojo(name = "list")
public class ListReleasesMojo extends AbstractReleaseMojo {

  @Parameter
  private String filter;

  @Parameter(defaultValue = "256")
  private long limit;
  
  @Parameter
  private String namespace;

  @Parameter
  private String offset;

  @Parameter
  private SortBy sortBy;

  @Parameter
  private SortOrder sortOrder;

  @Parameter
  private List<Status.Code> statusCodes;


  /*
   * Constructors.
   */
  
  
  public ListReleasesMojo() {
    super();
  }


  /*
   * Protected instance methods.
   */
  

  @Override
  protected void execute(final Callable<ReleaseManager> releaseManagerCallable) throws Exception {
    Objects.requireNonNull(releaseManagerCallable);
    final Log log = this.getLog();
    assert log != null;

    final ListReleasesRequest.Builder requestBuilder = ListReleasesRequest.newBuilder();
    assert requestBuilder != null;

    final String filter = this.getFilter();
    if (filter != null) {
      requestBuilder.setFilter(filter);
    }

    requestBuilder.setLimit(this.getLimit());

    String namespace = this.getNamespace();
    if (namespace == null || namespace.isEmpty()) {
      final io.fabric8.kubernetes.client.Config configuration = this.getClientConfiguration();
      if (configuration == null) {
        namespace = "default";
      } else {
        namespace = configuration.getNamespace();
        if (namespace == null || namespace.isEmpty()) {
          namespace = "default";
        }
      }
    }
    this.validateNamespace(namespace);
    requestBuilder.setNamespace(namespace);

    final String offset = this.getOffset();
    if (offset != null) {
      requestBuilder.setOffset(offset);
    }

    final SortBy sortBy = this.getSortBy();
    if (sortBy != null) {
      requestBuilder.setSortBy(sortBy);
    }

    final SortOrder sortOrder = this.getSortOrder();
    if (sortOrder != null) {
      requestBuilder.setSortOrder(sortOrder);
    }

    final Iterable<Status.Code> statusCodes = this.getStatusCodes();
    if (statusCodes != null) {
      requestBuilder.addAllStatusCodes(statusCodes);
    }

    final ReleaseManager releaseManager = releaseManagerCallable.call();
    if (releaseManager == null) {
      throw new IllegalStateException("releaseManagerCallable.call() == null");
    }

    if (log.isInfoEnabled()) {
      log.info("Listing releases in namespace " + namespace);
    }
    final Iterator<? extends ListReleasesResponse> listReleasesResponseIterator = releaseManager.list(requestBuilder.build());
    assert listReleasesResponseIterator != null;
    if (listReleasesResponseIterator.hasNext()) {
      if (log.isInfoEnabled()) {
        while (listReleasesResponseIterator.hasNext()) {
          final ListReleasesResponse response = listReleasesResponseIterator.next();
          assert response != null;
          log.info(response.toString());
        }
      }
    }
    
  }


  /*
   * Public instance methods.
   */

  
  public String getFilter() {
    return this.filter;
  }

  public void setFilter(final String filter) {
    this.filter = filter;
  }

  public long getLimit() {
    return this.limit;
  }

  public void setLimit(final long limit) {
    this.limit = limit;
  }

  public String getNamespace() {
    return this.namespace;
  }

  public void setNamespace(final String namespace) {
    this.validateNamespace(namespace);
    this.namespace = namespace;
  }

  public String getOffset() {
    return this.offset;
  }

  public void setOffset(final String offset) {
    this.offset = offset;
  }

  public SortBy getSortBy() {
    return this.sortBy;
  }

  public void setSortBy(final SortBy sortBy) {
    this.sortBy = sortBy;
  }

  public SortOrder getSortOrder() {
    return this.sortOrder;
  }

  public void setSortOrder(final SortOrder sortOrder) {
    this.sortOrder = sortOrder;
  }

  public List<Status.Code> getStatusCodes() {
    return this.statusCodes;
  }

  public void setStatusCodes(final List<Status.Code> statusCodes) {
    this.statusCodes = statusCodes;
  }

}
