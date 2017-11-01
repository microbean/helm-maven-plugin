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

/**
 * Uninstalls a release.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@Mojo(name = "uninstall")
public class UninstallReleaseMojo extends AbstractSingleReleaseMojo {


  /*
   * Instance fields.
   */


  /**
   * Whether <a
   * href="https://github.com/kubernetes/helm/blob/master/docs/charts_hooks.md#hooks">release
   * hooks</a> should be disabled.
   */
  @Parameter(defaultValue = "false")
  private boolean disableHooks;

  /**
   * The default timeout, in seconds, to use for Kubernetes
   * operations; set to {@code 300} by default for parity with the
   * {@code helm} command line program.
   */
  @Parameter(defaultValue = "300")
  private long timeout; // in seconds

  /**
   * Whether the release being uninstalled should be purged entirely
   * instead of being marked as deleted.
   */
  @Parameter(defaultValue = "false")
  private boolean purge;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link UninstallReleaseMojo}.
   */
  public UninstallReleaseMojo() {
    super();
  }


  /*
   * Instance methods.
   */
  

  /**
   * {@inheritDoc}
   *
   * <p>This implementation <a
   * href="https://docs.helm.sh/using_helm/#helm-delete-deleting-a-release">uninstalls</a>
   * the release with the {@linkplain #getReleaseName() supplied
   * release name}.</p>
   */
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

  /**
   * Returns {@code true} if <a
   * href="https://github.com/kubernetes/helm/blob/master/docs/charts_hooks.md#hooks">chart
   * hooks</a> are disabled.
   *
   * @return {@code true} if <a
   * href="https://github.com/kubernetes/helm/blob/master/docs/charts_hooks.md#hooks">chart
   * hooks</a> are disabled
   *
   * @see #setDisableHooks(boolean)
   */
  public boolean getDisableHooks() {
    return this.disableHooks;
  }

  /**
   * Sets whether <a
   * href="https://github.com/kubernetes/helm/blob/master/docs/charts_hooks.md#hooks">chart
   * hooks</a> are disabled.
   *
   * @param disableHooks if {@code true}, <a
   * href="https://github.com/kubernetes/helm/blob/master/docs/charts_hooks.md#hooks">chart
   * hooks</a> will be disabled
   *
   * @see #getDisableHooks()
   */
  public void setDisableHooks(final boolean disableHooks) {
    this.disableHooks = disableHooks;
  }

  /**
   * Returns whether the release being uninstalled should be purged entirely
   * instead of being marked as deleted.
   *
   * @return {@code true} if the release being uninstalled should be
   * purged entirely instead of being marked as deleted; {@code false}
   * if it should be marked as deleted
   *
   * @see #setPurge(boolean)
   */
  public boolean getPurge() {
    return this.purge;
  }

  /**
   * Sets whether the release being uninstalled should be purged
   * entirely instead of being marked as deleted.
   *
   * @param purge if {@code true} the release being uninstalled will
   * be purged entirely
   *
   * @see #getPurge()
   */
  public void setPurge(final boolean purge) {
    this.purge = purge;
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
  public void setTimeout(final long timeoutInSeconds) {
    this.timeout = timeoutInSeconds;
  }

}
