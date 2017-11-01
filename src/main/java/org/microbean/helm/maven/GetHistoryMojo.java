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
import java.util.List;
import java.util.Objects;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import hapi.services.tiller.Tiller.GetHistoryRequest;
import hapi.services.tiller.Tiller.GetHistoryResponse;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.microbean.helm.ReleaseManager;

@Mojo(name = "history")
public class GetHistoryMojo extends AbstractSingleReleaseMojo {


  /*
   * Instance fields.
   */
  

  @Parameter
  private int max;

  @Parameter(alias = "releaseHistoryListenersList")
  private List<ReleaseHistoryListener> releaseHistoryListeners;
  

  /*
   * Constructors.
   */
  
  
  public GetHistoryMojo() {
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

    final Collection<? extends ReleaseHistoryListener> listeners = this.getReleaseHistoryListenersList();
    if (listeners == null || listeners.isEmpty()) {
      if (log.isInfoEnabled()) {
        log.info("Skipping execution because there are no ReleaseHistoryListeners specified.");
      }
      return;
    }
    
    final GetHistoryRequest.Builder requestBuilder = GetHistoryRequest.newBuilder();
    assert requestBuilder != null;

    requestBuilder.setMax(this.getMax());

    final String releaseName = this.getReleaseName();
    if (releaseName != null) {
      requestBuilder.setName(releaseName);
    }

    final ReleaseManager releaseManager = releaseManagerCallable.call();
    if (releaseManager == null) {
      throw new IllegalStateException("releaseManagerCallable.call() == null");
    }

    if (log.isInfoEnabled()) {
      log.info("Retrieving history for release " + releaseName);
    }
    
    final Future<GetHistoryResponse> getHistoryResponseFuture = releaseManager.getHistory(requestBuilder.build());
    assert getHistoryResponseFuture != null;
    final GetHistoryResponse getHistoryResponse = getHistoryResponseFuture.get();
    assert getHistoryResponse != null;

    final ReleaseHistoryEvent event = new ReleaseHistoryEvent(this, getHistoryResponse);
    for (final ReleaseHistoryListener listener : listeners) {
      if (listener != null) {
        listener.releaseHistoryRetrieved(event);
      }
    }
    
  }


  /*
   * Public instance methods.
   */

  
  public int getMax() {
    return this.max;
  }

  public void setMax(final int max) {
    this.max = max;
  }

  public void addReleaseHistoryListener(final ReleaseHistoryListener listener) {
    if (listener != null) {
      if (this.releaseHistoryListeners == null) {
        this.releaseHistoryListeners = new ArrayList<>();      
      }
      this.releaseHistoryListeners.add(listener);
    }
  }

  public void removeReleaseHistoryListener(final ReleaseHistoryListener listener) {
    if (listener != null && this.releaseHistoryListeners != null) {
      this.releaseHistoryListeners.remove(listener);
    }
  }
  
  public ReleaseHistoryListener[] getReleaseHistoryListeners() {
    final Collection<ReleaseHistoryListener> listeners = this.getReleaseHistoryListenersList();
    if (listeners == null || listeners.isEmpty()) {
      return new ReleaseHistoryListener[0];
    } else {
      return listeners.toArray(new ReleaseHistoryListener[listeners.size()]);
    }
  }

  public List<ReleaseHistoryListener> getReleaseHistoryListenersList() {
    return this.releaseHistoryListeners;
  }

  public void setReleaseHistoryListenersList(final List<ReleaseHistoryListener> releaseHistoryListeners) {
    this.releaseHistoryListeners = releaseHistoryListeners;
  }

}
