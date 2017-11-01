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

/**
 * Retrieves the status of a release version.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@Mojo(name = "status")
public class GetReleaseStatusMojo extends AbstractSingleVersionedReleaseMojo {


  /*
   * Instance fields.
   */
  

  /**
   * A {@link List} of <a
   * href="apidocs/org/microbean/helm/maven/ReleaseStatusListener.html">{@code
   * ReleaseStatusListener}</a>s whose elements will be notified of
   * the status retrieval.
   */
  @Parameter(alias = "releaseStatusListenersList")
  private List<ReleaseStatusListener> releaseStatusListeners;
  

  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link GetReleaseStatusMojo}.
   */
  public GetReleaseStatusMojo() {
    super();
  }


  /*
   * Protected instance methods.
   */
  

  /**
   * {@inheritDoc}
   *
   * <p>This implementation retrieves the status for a {@linkplain
   * #getReleaseName() given release} at a {@linkplain #getVersion()
   * particular version} and {@linkplain
   * ReleaseStatusListener#releaseStatusRetrieved(ReleaseStatusEvent)
   * notifies} registered {@link ReleaseStatusListener}s.</p>
   */
  @Override
  protected void execute(final Callable<ReleaseManager> releaseManagerCallable) throws Exception {
    Objects.requireNonNull(releaseManagerCallable);
    final Log log = this.getLog();
    assert log != null;

    final Integer version = this.getVersion();
    if (version == null) {
      throw new IllegalStateException("version was not specified");
    }
    
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

    requestBuilder.setVersion(version.intValue());

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

    final ReleaseStatusEvent event = new ReleaseStatusEvent(this, getReleaseStatusResponse);
    for (final ReleaseStatusListener listener : listeners) {
      if (listener != null) {
        listener.releaseStatusRetrieved(event);
      }
    }
    
  }


  /*
   * Public instance methods.
   */

  
  /**
   * Adds a {@link ReleaseStatusListener} that will be {@linkplain
   * ReleaseStatusListener#releaseStatusRetrieved(ReleaseStatusEvent)
   * notified when a release version's status is retrieved
   *
   * @param listener the {@link ReleaseStatusListener} to add; may be
   * {@code null} in which case no action will be taken
   *
   * @see #removeReleaseStatusListener(ReleaseStatusListener)
   *
   * @see #getReleaseStatusListenersList()
   */
  public void addReleaseStatusListener(final ReleaseStatusListener listener) {
    if (listener != null) {
      if (this.releaseStatusListeners == null) {
        this.releaseStatusListeners = new ArrayList<>();      
      }
      this.releaseStatusListeners.add(listener);
    }
  }

  /**
   * Removes a {@link ReleaseStatusListener} from this {@link
   * GetReleaseStatusMojo}.
   *
   * @param listener the {@link ReleaseStatusListener} to remove; may
   * be {@code null} in which case no action will be taken
   *
   * @see #addReleaseStatusListener(ReleaseStatusListener)
   *
   * @see #getReleaseStatusListenersList()
   */
  public void removeReleaseStatusListener(final ReleaseStatusListener listener) {
    if (listener != null && this.releaseStatusListeners != null) {
      this.releaseStatusListeners.remove(listener);
    }
  }

  /**
   * Invokes the {@link #getReleaseStatusListenersList()} method and
   * {@linkplain Collection#toArray(Object[]) converts its return
   * value to an array}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * @return a non-{@code null} array of {@link
   * ReleaseStatusListener}s
   *
   * @see #getReleaseStatusListenersList()
   */
  public ReleaseStatusListener[] getReleaseStatusListeners() {
    final Collection<ReleaseStatusListener> listeners = this.getReleaseStatusListenersList();
    if (listeners == null || listeners.isEmpty()) {
      return new ReleaseStatusListener[0];
    } else {
      return listeners.toArray(new ReleaseStatusListener[listeners.size()]);
    }
  }

  /**
   * Returns the {@link List} of {@link ReleaseStatusListener}s whose
   * elements will be {@linkplain
   * ReleaseStatusListener#releaseStatusRetrieved(ReleaseStatusEvent)
   * notified when a release version's status is retrieved.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method are permitted to return {@code
   * null}.</p>
   *
   * @return a {@link List} of {@link ReleaseStatusListener}s, or
   * {@code null}
   *
   * @see #setReleaseStatusListenersList(List)
   *
   * @see #addReleaseStatusListener(ReleaseStatusListener)
   *
   * @see #removeReleaseStatusListener(ReleaseStatusListener)
   */
  public List<ReleaseStatusListener> getReleaseStatusListenersList() {
    return this.releaseStatusListeners;
  }

  /**
   * Installs the {@link List} of {@link ReleaseStatusListener}s whose
   * elements will be {@linkplain
   * ReleaseStatusListener#releaseStatusRetrieved(ReleaseStatusEvent)
   * notified when a release version's status is retrieved}.
   *
   * @param releaseStatusListeners the {@link List} of {@link
   * ReleaseStatusListener}s whose elements will be {@linkplain
   * ReleaseStatusListener#releaseStatusRetrieved(ReleaseStatusEvent)
   * notified when a release status is retrieved}; may be {@code null}
   *
   * @see #getReleaseStatusListenersList()
   *
   * @see #addReleaseStatusListener(ReleaseStatusListener)
   *
   * @see #removeReleaseStatusListener(ReleaseStatusListener)
   */
  public void setReleaseStatusListenersList(final List<ReleaseStatusListener> releaseStatusListeners) {
    this.releaseStatusListeners = releaseStatusListeners;
  }

}
