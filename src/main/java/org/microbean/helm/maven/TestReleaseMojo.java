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

import hapi.release.TestRunOuterClass.TestRun;

import hapi.services.tiller.Tiller.TestReleaseRequest;
import hapi.services.tiller.Tiller.TestReleaseResponse;

import org.apache.maven.plugin.MojoFailureException;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.microbean.helm.ReleaseManager;

/**
 * Runs tests against a Helm release.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@Mojo(name = "test")
public class TestReleaseMojo extends AbstractSingleReleaseMojo {


  /*
   * Instance fields.
   */
  

  /**
   * The timeout, in seconds, to use for Kubernetes operations; set to
   * {@code 300} by default for parity with the {@code helm} command
   * line program.
   */
  @Parameter(defaultValue = "300")
  private long timeout; // in seconds

  /**
   * Whether test Pods should be deleted after the test completes.
   */
  @Parameter
  private boolean cleanup;

  /**
   * A {@link List} of <a
   * href="apidocs/org/microbean/helm/maven/ReleaseTestListener.html">{@code
   * ReleaseTestListener}</a>s that will be notified of the test
   * results.
   */
  @Parameter(alias = "releaseTestListenersList")
  private List<ReleaseTestListener> releaseTestListeners;
  

  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link TestReleaseMojo}.
   */
  public TestReleaseMojo() {
    super();
  }


  /*
   * Protected instance methods.
   */
  

  /**
   * {@inheritDoc}
   *
   * <p>This implementation {@linkplain
   * ReleaseManager#test(hapi.services.tiller.Tiller.TestReleaseRequest)
   * runs tests against a release}.</p>
   */
  @Override
  protected void execute(final Callable<ReleaseManager> releaseManagerCallable) throws Exception {
    Objects.requireNonNull(releaseManagerCallable);
    final Log log = this.getLog();
    assert log != null;

    final TestReleaseRequest.Builder requestBuilder = TestReleaseRequest.newBuilder();
    assert requestBuilder != null;

    requestBuilder.setCleanup(this.getCleanup());

    final String releaseName = this.getReleaseName();
    if (releaseName != null) {
      requestBuilder.setName(releaseName);
    }

    requestBuilder.setTimeout(this.getTimeout());
    
    final ReleaseManager releaseManager = releaseManagerCallable.call();
    if (releaseManager == null) {
      throw new IllegalStateException("releaseManagerCallable.call() == null");
    }

    if (log.isInfoEnabled()) {
      log.info("Testing release " + releaseName);
    }
    
    final Iterator<TestReleaseResponse> testReleaseResponses = releaseManager.test(requestBuilder.build());
    assert testReleaseResponses != null;
    if (testReleaseResponses.hasNext()) {
      final List<ReleaseTestListener> listeners = this.getReleaseTestListenersList();
      while (testReleaseResponses.hasNext()) {
        final TestReleaseResponse response = testReleaseResponses.next();
        assert response != null;
        if (listeners != null && !listeners.isEmpty()) {
          final ReleaseTestEvent event = new ReleaseTestEvent(this, response);
          for (final ReleaseTestListener listener : listeners) {
            if (listener != null) {
              listener.releaseTested(event);
            }
          }
        }
        final TestRun.Status status = response.getStatus();
        if (TestRun.Status.FAILURE.equals(status)) {
          throw new MojoFailureException(response.getMsg());
        }
      }
    }
    
  }


  /*
   * Public instance methods.
   */

  /**
   * Returns {@code true} if test Pods should be deleted after the
   * tests complete.
   *
   * @return {@code true} if test Pods should be deleted after the
   * tests complete; {@code false} otherwise
   *
   * @see #setCleanup(boolean)
   */  
  public boolean getCleanup() {
    return this.cleanup;
  }

  /**
   * Sets whether test Pods should be deleted after the
   * tests complete.
   *
   * @param cleanup if {@code true}, test Pods will be deleted after
   * the tests complete
   *
   * @see #getCleanup()
   */
  public void setCleanup(final boolean cleanup) {
    this.cleanup = cleanup;
  }

  /**
   * Returns the timeout value, in seconds, for Kubernetes operations.
   *
   * @return the timeout value, in seconds, for Kubernetes operations
   *
   * @see #setTimeout(long)
   */
  public long getTimeout() {
    return this.timeout;
  }

  /**
   * Sets the timeout value, in seconds, for Kubernetes operations.
   *
   * @param timeout the timeout value, in seconds, for Kubernetes
   * operations
   *
   * @see #getTimeout()
   */
  public void setTimeout(final long timeout) {
    this.timeout = timeout;
  }

  /**
   * Adds a {@link ReleaseTestListener} that will be {@linkplain
   * ReleaseTestListener#releaseTested(ReleaseTestEvent) notified when
   * the tests complete}.
   *
   * @param listener the {@link ReleaseTestListener} to add; may be
   * {@code null} in which case no action will be taken
   *
   * @see #removeReleaseTestListener(ReleaseTestListener)
   *
   * @see #getReleaseTestListenersList()
   */
  public void addReleaseTestListener(final ReleaseTestListener listener) {
    if (listener != null) {
      if (this.releaseTestListeners == null) {
        this.releaseTestListeners = new ArrayList<>();      
      }
      this.releaseTestListeners.add(listener);
    }
  }

  /**
   * Removes a {@link ReleaseTestListener} from this {@link
   * TestReleaseMojo}.
   *
   * @param listener the {@link ReleaseTestListener} to remove; may be
   * {@code null} in which case no action will be taken
   *
   * @see #addReleaseTestListener(ReleaseTestListener)
   *
   * @see #getReleaseTestListenersList()
   */
  public void removeReleaseTestListener(final ReleaseTestListener listener) {
    if (listener != null && this.releaseTestListeners != null) {
      this.releaseTestListeners.remove(listener);
    }
  }

  /**
   * Invokes the {@link #getReleaseTestListenersList()} method and
   * {@linkplain Collection#toArray(Object[]) converts its return
   * value to an array}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * @return a non-{@code null} array of {@link ReleaseTestListener}s
   *
   * @see #getReleaseTestListenersList()
   */
  public ReleaseTestListener[] getReleaseTestListeners() {
    final Collection<ReleaseTestListener> listeners = this.getReleaseTestListenersList();
    if (listeners == null || listeners.isEmpty()) {
      return new ReleaseTestListener[0];
    } else {
      return listeners.toArray(new ReleaseTestListener[listeners.size()]);
    }
  }

  /**
   * Returns the {@link List} of {@link ReleaseTestListener}s whose
   * elements will be {@linkplain
   * ReleaseTestListener#releaseTested(ReleaseTestEvent) notified when
   * the tests complete}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method are permitted to return {@code null}.</p>
   *
   * @return a {@link List} of {@link ReleaseTestListener}s, or {@code null}
   *
   * @see #setReleaseTestListenersList(List)
   *
   * @see #addReleaseTestListener(ReleaseTestListener)
   *
   * @see #removeReleaseTestListener(ReleaseTestListener)
   */
  public List<ReleaseTestListener> getReleaseTestListenersList() {
    return this.releaseTestListeners;
  }

  /**
   * Installs the {@link List} of {@link ReleaseTestListener}s whose
   * elements will be {@linkplain
   * ReleaseTestListener#releaseTested(ReleaseTestEvent) notified when
   * the tests complete}.
   *
   * @param releaseTestListeners the {@link List} of {@link ReleaseTestListener}s whose
   * elements will be {@linkplain
   * ReleaseTestListener#releaseTested(ReleaseTestEvent) notified when
   * the tests complete}; may be {@code null}
   *
   * @see #getReleaseTestListenersList()
   *
   * @see #addReleaseTestListener(ReleaseTestListener)
   *
   * @see #removeReleaseTestListener(ReleaseTestListener)
   */
  public void setReleaseTestListenersList(final List<ReleaseTestListener> releaseTestListeners) {
    this.releaseTestListeners = releaseTestListeners;
  }  

}
