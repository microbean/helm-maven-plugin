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

import java.util.regex.Matcher;

import org.apache.maven.plugins.annotations.Parameter;

import org.microbean.helm.ReleaseManager;

/**
 * An {@link AbstractSingleReleaseMojo} whose implementations change
 * the single <a href="https://docs.helm.sh/glossary/#release">Helm
 * release</a> they are configured to work with (as opposed to just
 * reading its information).
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public abstract class AbstractMutatingReleaseMojo extends AbstractSingleReleaseMojo {


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
   * Whether the operation should be treated as a <em>dry
   * run</em>&mdash;a simulation of a real operation.
   */
  @Parameter(defaultValue = "false")
  private boolean dryRun;

  /**
   * The timeout, in seconds, to use for Kubernetes operations; set to
   * {@code 300} by default for parity with the {@code helm} command
   * line program.
   */
  @Parameter(defaultValue = "300")
  private long timeout; // in seconds

  /**
   * Whether to wait until any Pods in a release are ready.
   */
  @Parameter(defaultValue = "false")
  private boolean wait;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link AbstractMutatingReleaseMojo}.
   */
  protected AbstractMutatingReleaseMojo() {
    super();
  }


  /*
   * Instance methods.
   */
  

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
   * Returns whether this operation will be simulated.
   *
   * @return whether this operation will be simulated
   *
   * @see #setDryRun(boolean)
   */
  public boolean getDryRun() {
    return this.dryRun;
  }

  /**
   * Sets whether this operation will be simulated.
   *
   * @param dryRun if {@code true}, this operation will be simulated
   *
   * @see #getDryRun()
   */
  public void setDryRun(final boolean dryRun) {
    this.dryRun = dryRun;
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
   * Returns {@code true} if this operation will wait for Pods to be
   * ready before returning.
   *
   * @return {@code true} if this operation will wait for Pods to be
   * ready before returning
   *
   * @see #setWait(boolean)
   */
  public boolean getWait() {
    return this.wait;
  }

  /**
   * Sets whether this operation will wait for Pods to be ready before
   * returning.
   *
   * @param wait if {@code true}, this operation will wait for Pods to
   * be ready before returning
   *
   * @see #getWait()
   */
  public void setWait(final boolean wait) {
    this.wait = wait;
  }
  
}
