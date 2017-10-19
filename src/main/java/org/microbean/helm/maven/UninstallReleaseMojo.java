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

import java.util.Objects;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import hapi.release.ReleaseOuterClass.Release;

import hapi.services.tiller.Tiller.UninstallReleaseRequest;
import hapi.services.tiller.Tiller.UninstallReleaseResponse;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.microbean.helm.ReleaseManager;

@Mojo(name = "uninstall")
public class UninstallReleaseMojo extends AbstractSingleReleaseMojo {

  @Parameter
  private boolean disableHooks;

  @Parameter
  private boolean dryRun;

  @Parameter(defaultValue = "300")
  private long timeout; // in seconds

  @Parameter
  private boolean purge;
  
  public UninstallReleaseMojo() {
    super();
  }

  @Override
  protected void execute(final Callable<ReleaseManager> releaseManagerCallable) throws Exception {
    Objects.requireNonNull(releaseManagerCallable);
    final Log log = this.getLog();
    assert log != null;

    final UninstallReleaseRequest.Builder requestBuilder = UninstallReleaseRequest.newBuilder();
    assert requestBuilder != null;

    requestBuilder.setDisableHooks(this.getDisableHooks());

    final String releaseName = this.getReleaseName();
    if (releaseName != null) {
      requestBuilder.setName(releaseName);
    }

    requestBuilder.setPurge(this.getPurge());
    requestBuilder.setTimeout(this.getTimeout());

    final ReleaseManager releaseManager = releaseManagerCallable.call();
    if (releaseManager == null) {
      throw new IllegalStateException("releaseManagerCallable.call() == null");
    }

    if (log.isInfoEnabled()) {
      log.info("Uninstalling release " + requestBuilder.getName());
    }
    final Future<UninstallReleaseResponse> uninstallReleaseResponseFuture = releaseManager.uninstall(requestBuilder.build());
    assert uninstallReleaseResponseFuture != null;
    final UninstallReleaseResponse uninstallReleaseResponse = uninstallReleaseResponseFuture.get();
    assert uninstallReleaseResponse != null;
    if (log.isInfoEnabled()) {
      final Release release = uninstallReleaseResponse.getRelease();
      assert release != null;
      log.info("Uninstalled release " + release.getName());
    }
    
  }

  public boolean getDisableHooks() {
    return this.disableHooks;
  }

  public void setDisableHooks(final boolean disableHooks) {
    this.disableHooks = disableHooks;
  }

  public boolean getDryRun() {
    return this.dryRun;
  }

  public void setDryRun(final boolean dryRun) {
    this.dryRun = dryRun;
  }

  public boolean getPurge() {
    return this.purge;
  }

  public void setPurge(final boolean purge) {
    this.purge = purge;
  }
  
  public long getTimeout() {
    return this.timeout;
  }

  public void setTimeout(final long timeoutInSeconds) {
    this.timeout = timeoutInSeconds;
  }

}
