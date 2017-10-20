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

import hapi.services.tiller.Tiller.GetReleaseStatusRequest;
import hapi.services.tiller.Tiller.GetReleaseStatusResponse;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.microbean.helm.ReleaseManager;

@Mojo(name = "status")
public class GetReleaseStatusMojo extends AbstractSingleVersionedReleaseMojo {


  /*
   * Instance fields.
   */
  

  @Parameter(alias = "releaseStatusListenersList")
  private List<ReleaseStatusListener> releaseStatusListeners;
  

  /*
   * Constructors.
   */
  
  
  public GetReleaseStatusMojo() {
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

    final Collection<? extends ReleaseStatusListener> listeners = this.getReleaseStatusListenersList();
    if (listeners == null || listeners.isEmpty()) {
      if (log.isInfoEnabled()) {
        log.info("Skipping execution because there are no ReleaseStatusListeners specified.");
      }
      return;
    }
    
    final GetReleaseStatusRequest.Builder requestBuilder = GetReleaseStatusRequest.newBuilder();
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
      log.info("Retrieving status for release " + releaseName);
    }
    
    final Future<GetReleaseStatusResponse> getReleaseStatusResponseFuture = releaseManager.getStatus(requestBuilder.build());
    assert getReleaseStatusResponseFuture != null;
    final GetReleaseStatusResponse getReleaseStatusResponse = getReleaseStatusResponseFuture.get();
    assert getReleaseStatusResponse != null;

    final ReleaseStatusEvent event = new ReleaseStatusEvent(this, log, getReleaseStatusResponse);
    for (final ReleaseStatusListener listener : listeners) {
      if (listener != null) {
        listener.releaseStatusRetrieved(event);
      }
    }
    
  }


  /*
   * Public instance methods.
   */

  
  public void addReleaseStatusListener(final ReleaseStatusListener listener) {
    if (listener != null) {
      if (this.releaseStatusListeners == null) {
        this.releaseStatusListeners = new ArrayList<>();      
      }
      this.releaseStatusListeners.add(listener);
    }
  }

  public void removeReleaseStatusListener(final ReleaseStatusListener listener) {
    if (listener != null && this.releaseStatusListeners != null) {
      this.releaseStatusListeners.remove(listener);
    }
  }
  
  public ReleaseStatusListener[] getReleaseStatusListeners() {
    final Collection<ReleaseStatusListener> listeners = this.getReleaseStatusListenersList();
    if (listeners == null || listeners.isEmpty()) {
      return new ReleaseStatusListener[0];
    } else {
      return listeners.toArray(new ReleaseStatusListener[listeners.size()]);
    }
  }

  public List<ReleaseStatusListener> getReleaseStatusListenersList() {
    return this.releaseStatusListeners;
  }

  public void setReleaseStatusListenersList(final List<ReleaseStatusListener> releaseStatusListeners) {
    this.releaseStatusListeners = releaseStatusListeners;
  }

}
