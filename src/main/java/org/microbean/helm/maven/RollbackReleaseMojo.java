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

import hapi.services.tiller.Tiller.RollbackReleaseRequest;
import hapi.services.tiller.Tiller.RollbackReleaseResponse;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.microbean.helm.ReleaseManager;

/**
 * <a
 * href="https://docs.helm.sh/using_helm/#helm-upgrade-and-helm-rollback-upgrading-a-release-and-recovering-on-failure">Rolls
 * a release back</a> to a prior version.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@Mojo(name = "rollback")
public class RollbackReleaseMojo extends AbstractForceableMutatingReleaseMojo {


  /*
   * Instance fields.
   */


  /**
   * The version to roll back to.
   */
  @Parameter(required = true)
  private Integer version;


  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link RollbackReleaseMojo}.
   */
  public RollbackReleaseMojo() {
    super();
  }


  /*
   * Instance methods.
   */


  /**
   * {@inheritDoc}
   *
   * <p>This implementation <a
   * href="https://docs.helm.sh/using_helm/#helm-upgrade-and-helm-rollback-upgrading-a-release-and-recovering-on-failure">rolls
   * a named release back</a> to a {@linkplain #getVersion() prior
   * version}.</p>
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
    
    final RollbackReleaseRequest.Builder requestBuilder = RollbackReleaseRequest.newBuilder();
    assert requestBuilder != null;

    requestBuilder.setDisableHooks(this.getDisableHooks());
    requestBuilder.setDryRun(this.getDryRun());
    requestBuilder.setForce(this.getForce());
    requestBuilder.setRecreate(this.getRecreate());

    final String releaseName = this.getReleaseName();
    if (releaseName != null) {
      requestBuilder.setName(releaseName);
    }
    
    requestBuilder.setTimeout(this.getTimeout());
    requestBuilder.setVersion(version.intValue());
    requestBuilder.setWait(this.getWait());

    final ReleaseManager releaseManager = releaseManagerCallable.call();
    if (releaseManager == null) {
      throw new IllegalStateException("releaseManagerCallable.call() == null");
    }

    if (log.isInfoEnabled()) {
      log.info("Rolling back release " + requestBuilder.getName());
    }
    final Future<RollbackReleaseResponse> rollbackReleaseResponseFuture = releaseManager.rollback(requestBuilder.build());
    assert rollbackReleaseResponseFuture != null;
    final RollbackReleaseResponse rollbackReleaseResponse = rollbackReleaseResponseFuture.get();
    assert rollbackReleaseResponse != null;
    if (log.isInfoEnabled()) {
      final Release release = rollbackReleaseResponse.getRelease();
      assert release != null;
      log.info("Rolled back release " + release.getName());
    }
    
  }

  /**
   * Returns the version of the release to roll back to.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method are permitted to return {@code null}.</p>
   *
   * @return the version of the release to roll back to, or {@code
   * null}
   *
   * @see #setVersion(Integer)
   */
  public Integer getVersion() {
    return this.version;
  }

  /**
   * Sets the version of the release to roll back to.
   *
   * @param version the version to roll back to; must not be {@code
   * null}
   *
   * @exception NullPointerException if {@code version} is {@code
   * null}
   *
   * @see #getVersion()
   */
  public void setVersion(final Integer version) {
    Objects.requireNonNull(version);
    this.version = version;
  }

}
