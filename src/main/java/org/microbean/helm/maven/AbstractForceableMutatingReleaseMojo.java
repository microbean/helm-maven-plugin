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
 * An {@link AbstractMutatingReleaseMojo} that can be forced.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public abstract class AbstractForceableMutatingReleaseMojo extends AbstractMutatingReleaseMojo {


  /*
   * Instance fields.
   */


  /**
   * Whether the operation should be forced.
   */
  @Parameter(defaultValue = "false")
  private boolean force;

  /**
   * Whether Pods should be recreated as part of the operation.
   */
  @Parameter(defaultValue = "false")
  private boolean recreate;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link AbstractForceableMutatingReleaseMojo}.
   */
  protected AbstractForceableMutatingReleaseMojo() {
    super();
  }


  /*
   * Instance methods.
   */


  /**
   * Returns whether the operation should be forced.
   *
   * @return whether the operation should be forced
   *
   * @see #setForce(boolean)
   */
  public boolean getForce() {
    return this.force;
  }

  /**
   * Sets whether the operation should be forced.
   *
   * @param force whether the operation should be forced
   *
   * @see #getForce()
   */
  public void setForce(final boolean force) {
    this.force = force;
  }

  /**
   * Returns whether Pods should be recreated as part of the
   * operation.
   *
   * @return whether Pods should be recreated as part of the operation
   *
   * @see #setRecreate(boolean)
   */
  public boolean getRecreate() {
    return this.recreate;
  }

  /**
   * Sets whether Pods should be recreated as part of the operation.
   *
   * @param recreate whether Pods should be recreated as part of the
   * operation
   *
   * @see #getRecreate()
   */
  public void setRecreate(final boolean recreate) {
    this.recreate = recreate;
  }

}
