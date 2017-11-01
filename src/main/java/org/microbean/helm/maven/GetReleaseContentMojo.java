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

import hapi.services.tiller.Tiller.GetReleaseContentRequest;
import hapi.services.tiller.Tiller.GetReleaseContentResponse;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.microbean.helm.ReleaseManager;

/**
 * Retrieves the content of a release.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@Mojo(name = "content")
public class GetReleaseContentMojo extends AbstractSingleVersionedReleaseMojo {


  /*
   * Instance fields.
   */
  
  /**
   * A {@link List} of <a
   * href="apidocs/org/microbean/helm/maven/ReleaseContentListener.html">{@code
   * ReleaseContentListener}</a>s that will be notified of a release's
   * content retrieval.
   */
  @Parameter(alias = "releaseContentListenersList")
  private List<ReleaseContentListener> releaseContentListeners;
  

  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link GetReleaseContentMojo}.
   */
  public GetReleaseContentMojo() {
    super();
  }


  /*
   * Protected instance methods.
   */
  

  /**
   * {@inheritDoc}
   *
   * <p>This implementation retrieves the content of a particular
   * version of a single release and {@linkplain
   * ReleaseContentListener#releaseContentRetrieved(ReleaseContentEvent)
   * notifies registers listeners}.</p>
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

    requestBuilder.setVersion(version.intValue());

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

    final ReleaseContentEvent event = new ReleaseContentEvent(this, getReleaseContentResponse);
    for (final ReleaseContentListener listener : listeners) {
      if (listener != null) {
        listener.releaseContentRetrieved(event);
      }
    }
    
  }


  /*
   * Public instance methods.
   */

  
  /**
   * Adds a {@link ReleaseContentListener} that will be {@linkplain
   * ReleaseContentListener#releaseContentRetrieved(ReleaseContentEvent)
   * notified when a release's content is retrieved}.
   *
   * @param listener the {@link ReleaseContentListener} to add; may be
   * {@code null} in which case no action will be taken
   *
   * @see #removeReleaseContentListener(ReleaseContentListener)
   *
   * @see #getReleaseContentListenersList()
   */
  public void addReleaseContentListener(final ReleaseContentListener listener) {
    if (listener != null) {
      if (this.releaseContentListeners == null) {
        this.releaseContentListeners = new ArrayList<>();      
      }
      this.releaseContentListeners.add(listener);
    }
  }

  /**
   * Removes a {@link ReleaseContentListener} from this {@link
   * TestReleaseMojo}.
   *
   * @param listener the {@link ReleaseContentListener} to remove; may be
   * {@code null} in which case no action will be taken
   *
   * @see #addReleaseContentListener(ReleaseContentListener)
   *
   * @see #getReleaseContentListenersList()
   */
  public void removeReleaseContentListener(final ReleaseContentListener listener) {
    if (listener != null && this.releaseContentListeners != null) {
      this.releaseContentListeners.remove(listener);
    }
  }

  /**
   * Invokes the {@link #getReleaseContentListenersList()} method and
   * {@linkplain Collection#toArray(Object[]) converts its return
   * value to an array}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * @return a non-{@code null} array of {@link ReleaseContentListener}s
   *
   * @see #getReleaseContentListenersList()
   */
  public ReleaseContentListener[] getReleaseContentListeners() {
    final Collection<ReleaseContentListener> listeners = this.getReleaseContentListenersList();
    if (listeners == null || listeners.isEmpty()) {
      return new ReleaseContentListener[0];
    } else {
      return listeners.toArray(new ReleaseContentListener[listeners.size()]);
    }
  }

  /**
   * Returns the {@link List} of {@link ReleaseContentListener}s whose
   * elements will be {@linkplain
   * ReleaseContentListener#releaseContentRetrieved(ReleaseContentEvent)
   * notified when the tests complete}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method are permitted to return {@code
   * null}.</p>
   *
   * @return a {@link List} of {@link ReleaseContentListener}s, or
   * {@code null}
   *
   * @see #setReleaseContentListenersList(List)
   *
   * @see #addReleaseContentListener(ReleaseContentListener)
   *
   * @see #removeReleaseContentListener(ReleaseContentListener)
   */
  public List<ReleaseContentListener> getReleaseContentListenersList() {
    return this.releaseContentListeners;
  }

  /**
   * Installs the {@link List} of {@link ReleaseContentListener}s
   * whose elements will be {@linkplain
   * ReleaseContentListener#releaseContentRetrieved(ReleaseContentEvent)
   * notified when a release's contents are retrieved}.
   *
   * @param releaseContentListeners the {@link List} of {@link
   * ReleaseContentListener}s whose elements will be {@linkplain
   * ReleaseContentListener#releaseContentRetrieved(ReleaseContentEvent)
   * notified when a release's contents are retrieved}; may be {@code
   * null}
   *
   * @see #getReleaseContentListenersList()
   *
   * @see #addReleaseContentListener(ReleaseContentListener)
   *
   * @see #removeReleaseContentListener(ReleaseContentListener)
   */
  public void setReleaseContentListenersList(final List<ReleaseContentListener> releaseContentListeners) {
    this.releaseContentListeners = releaseContentListeners;
  }

}
