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

@Mojo(name = "test")
public class TestReleaseMojo extends AbstractSingleReleaseMojo {


  /*
   * Instance fields.
   */
  

  @Parameter(defaultValue = "300")
  private long timeout; // in seconds

  @Parameter
  private boolean cleanup;

  @Parameter(alias = "releaseTestListenersList")
  private List<ReleaseTestListener> releaseTestListeners;
  

  /*
   * Constructors.
   */
  
  
  public TestReleaseMojo() {
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
          final ReleaseTestEvent event = new ReleaseTestEvent(this, log, response);
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

  
  public boolean getCleanup() {
    return this.cleanup;
  }

  public void setCleanup(final boolean cleanup) {
    this.cleanup = cleanup;
  }
  
  public long getTimeout() {
    return this.timeout;
  }

  public void setTimeout(final long timeout) {
    this.timeout = timeout;
  }

  public void addReleaseTestListener(final ReleaseTestListener listener) {
    if (listener != null) {
      if (this.releaseTestListeners == null) {
        this.releaseTestListeners = new ArrayList<>();      
      }
      this.releaseTestListeners.add(listener);
    }
  }
  
  public void removeReleaseTestListener(final ReleaseTestListener listener) {
    if (listener != null && this.releaseTestListeners != null) {
      this.releaseTestListeners.remove(listener);
    }
  }
  
  public ReleaseTestListener[] getReleaseTestListeners() {
    final Collection<ReleaseTestListener> listeners = this.getReleaseTestListenersList();
    if (listeners == null || listeners.isEmpty()) {
      return new ReleaseTestListener[0];
    } else {
      return listeners.toArray(new ReleaseTestListener[listeners.size()]);
    }
  }

  public List<ReleaseTestListener> getReleaseTestListenersList() {
    return this.releaseTestListeners;
  }

  public void setReleaseTestListenersList(final List<ReleaseTestListener> releaseTestListeners) {
    this.releaseTestListeners = releaseTestListeners;
  }  

}
