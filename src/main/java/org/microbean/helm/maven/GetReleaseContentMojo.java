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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import hapi.release.ReleaseOuterClass.Release;
import hapi.release.StatusOuterClass.Status;

import hapi.services.tiller.Tiller.GetReleaseContentRequest;
import hapi.services.tiller.Tiller.GetReleaseContentResponse;
import hapi.services.tiller.Tiller.ListSort.SortBy;
import hapi.services.tiller.Tiller.ListSort.SortOrder;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.microbean.helm.ReleaseManager;

@Mojo(name = "content")
public class GetReleaseContentMojo extends AbstractSingleVersionedReleaseMojo {


  /*
   * Instance fields.
   */
  

  @Parameter(alias = "releaseContentListenersList")
  private List<ReleaseContentListener> releaseContentListeners;
  

  /*
   * Constructors.
   */
  
  
  public GetReleaseContentMojo() {
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

    final Collection<? extends ReleaseContentListener> listeners = this.getReleaseContentListenersList();
    if (listeners == null || listeners.isEmpty()) {
      if (log.isInfoEnabled()) {
        log.info("Skipping execution because there are no ReleaseContentListeners specified.");
      }
      return;
    }
    
    final GetReleaseContentRequest.Builder requestBuilder = GetReleaseContentRequest.newBuilder();
    assert requestBuilder != null;

    final String releaseName = this.getReleaseName();
    if (releaseName != null) {
      requestBuilder.setName(releaseName);
    }

    requestBuilder.setVersion(this.getVersion());

    final ReleaseManager releaseManager = releaseManagerCallable.call();
    if (releaseManager == null) {
      throw new IllegalStateException("releaseManagerCallable.call() == null");
    }

    if (log.isInfoEnabled()) {
      log.info("Retrieving content for release " + releaseName);
    }
    
    final Future<GetReleaseContentResponse> getReleaseContentResponseFuture = releaseManager.getContent(requestBuilder.build());
    assert getReleaseContentResponseFuture != null;
    final GetReleaseContentResponse getReleaseContentResponse = getReleaseContentResponseFuture.get();
    assert getReleaseContentResponse != null;

    final ReleaseContentEvent event = new ReleaseContentEvent(this, log, getReleaseContentResponse);
    for (final ReleaseContentListener listener : listeners) {
      if (listener != null) {
        listener.releaseContentRetrieved(event);
      }
    }
    
  }


  /*
   * Public instance methods.
   */

  
  public void addReleaseContentListener(final ReleaseContentListener listener) {
    if (listener != null) {
      if (this.releaseContentListeners == null) {
        this.releaseContentListeners = new ArrayList<>();      
      }
      this.releaseContentListeners.add(listener);
    }
  }

  public void removeReleaseContentListener(final ReleaseContentListener listener) {
    if (listener != null && this.releaseContentListeners != null) {
      this.releaseContentListeners.remove(listener);
    }
  }
  
  public ReleaseContentListener[] getReleaseContentListeners() {
    final Collection<ReleaseContentListener> listeners = this.getReleaseContentListenersList();
    if (listeners == null || listeners.isEmpty()) {
      return new ReleaseContentListener[0];
    } else {
      return listeners.toArray(new ReleaseContentListener[listeners.size()]);
    }
  }

  public List<ReleaseContentListener> getReleaseContentListenersList() {
    return this.releaseContentListeners;
  }

  public void setReleaseContentListenersList(final List<ReleaseContentListener> releaseContentListeners) {
    this.releaseContentListeners = releaseContentListeners;
  }

}
