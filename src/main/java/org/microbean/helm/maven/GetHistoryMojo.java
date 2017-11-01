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

/**
 * Retrieves the history of a release.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@Mojo(name = "history")
public class GetHistoryMojo extends AbstractSingleReleaseMojo {


  /*
   * Instance fields.
   */
  

  /**
   * The maximum number of versions to return.
   */
  @Parameter
  private int max;

  /**
   * A {@link List} of <a
   * href="apidocs/org/microbean/helm/maven/ReleaseHistoryListener.html">{@code
   * ReleaseHistoryListener}</a>s whose elements will be notified of
   * each item in the history.
   */
  @Parameter(alias = "releaseHistoryListenersList")
  private List<ReleaseHistoryListener> releaseHistoryListeners;
  

  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link GetHistoryMojo}.
   */
  public GetHistoryMojo() {
    super();
  }


  /*
   * Protected instance methods.
   */
  

  /**
   * {@inheritDoc}
   *
   * <p>This implementation retrieves the history for a {@linkplain
   * #getReleaseName() given release} and {@linkplain
   * ReleaseHistoryListener#releaseHistoryRetrieved(ReleaseHistoryEvent)
   * notifies} registered {@link ReleaseHistoryListener}s.</p>
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


  /**
   * Returns the maximum number of history entries to retrieve.
   *
   * @return the maximum number of history entries to retrieve
   *
   * @see #setMax(int)
   */
  public int getMax() {
    return this.max;
  }

  /**
   * Sets the maximum number of history entries to retrieve.
   *
   * @param max the maximum number of history entries to retrieve
   *
   * @see #getMax()
   */
  public void setMax(final int max) {
    this.max = max;
  }

  /**
   * Adds a {@link ReleaseHistoryListener} that will be {@linkplain
   * ReleaseHistoryListener#releaseHistoryRetrieved(ReleaseHistoryEvent)
   * notified when a release history is retrieved
   *
   * @param listener the {@link ReleaseHistoryListener} to add; may be
   * {@code null} in which case no action will be taken
   *
   * @see #removeReleaseHistoryListener(ReleaseHistoryListener)
   *
   * @see #getReleaseHistoryListenersList()
   */
  public void addReleaseHistoryListener(final ReleaseHistoryListener listener) {
    if (listener != null) {
      if (this.releaseHistoryListeners == null) {
        this.releaseHistoryListeners = new ArrayList<>();      
      }
      this.releaseHistoryListeners.add(listener);
    }
  }

  /**
   * Removes a {@link ReleaseHistoryListener} from this {@link
   * GetHistoryMojo}.
   *
   * @param listener the {@link ReleaseHistoryListener} to remove; may
   * be {@code null} in which case no action will be taken
   *
   * @see #addReleaseHistoryListener(ReleaseHistoryListener)
   *
   * @see #getReleaseHistoryListenersList()
   */
  public void removeReleaseHistoryListener(final ReleaseHistoryListener listener) {
    if (listener != null && this.releaseHistoryListeners != null) {
      this.releaseHistoryListeners.remove(listener);
    }
  }

  /**
   * Invokes the {@link #getReleaseHistoryListenersList()} method and
   * {@linkplain Collection#toArray(Object[]) converts its return
   * value to an array}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * @return a non-{@code null} array of {@link
   * ReleaseHistoryListener}s
   *
   * @see #getReleaseHistoryListenersList()
   */
  public ReleaseHistoryListener[] getReleaseHistoryListeners() {
    final Collection<ReleaseHistoryListener> listeners = this.getReleaseHistoryListenersList();
    if (listeners == null || listeners.isEmpty()) {
      return new ReleaseHistoryListener[0];
    } else {
      return listeners.toArray(new ReleaseHistoryListener[listeners.size()]);
    }
  }

  /**
   * Returns the {@link List} of {@link ReleaseHistoryListener}s whose
   * elements will be {@linkplain
   * ReleaseHistoryListener#releaseHistoryRetrieved(ReleaseHistoryEvent)
   * notified when a release history is retrieved.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method are permitted to return {@code
   * null}.</p>
   *
   * @return a {@link List} of {@link ReleaseHistoryListener}s, or
   * {@code null}
   *
   * @see #setReleaseHistoryListenersList(List)
   *
   * @see #addReleaseHistoryListener(ReleaseHistoryListener)
   *
   * @see #removeReleaseHistoryListener(ReleaseHistoryListener)
   */
  public List<ReleaseHistoryListener> getReleaseHistoryListenersList() {
    return this.releaseHistoryListeners;
  }

  /**
   * Installs the {@link List} of {@link ReleaseHistoryListener}s
   * whose elements will be {@linkplain
   * ReleaseHistoryListener#releaseHistoryRetrieved(ReleaseHistoryEvent)
   * notified when a release history is retrieved}.
   *
   * @param releaseHistoryListeners the {@link List} of {@link
   * ReleaseHistoryListener}s whose elements will be {@linkplain
   * ReleaseHistoryListener#releaseHistoryRetrieved(ReleaseHistoryEvent)
   * notified when a release history is retrieved}; may be {@code
   * null}
   *
   * @see #getReleaseHistoryListenersList()
   *
   * @see #addReleaseHistoryListener(ReleaseHistoryListener)
   *
   * @see #removeReleaseHistoryListener(ReleaseHistoryListener)
   */
  public void setReleaseHistoryListenersList(final List<ReleaseHistoryListener> releaseHistoryListeners) {
    this.releaseHistoryListeners = releaseHistoryListeners;
  }

}
