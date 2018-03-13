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
 * An {@link AbstractSingleReleaseMojo} whose implementations operate
 * on a particular versioned release.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public abstract class AbstractSingleVersionedReleaseMojo extends AbstractSingleReleaseMojo {


  /*
   * Instance fields.
   */


  /**
   * The version of the release.
   */
  @Parameter(required = true, defaultValue = "0")
  private Integer version;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link AbstractSingleVersionedReleaseMojo}.
   */
  protected AbstractSingleVersionedReleaseMojo() {
    super();
  }


  /*
   * Instance methods.
   */


  /**
   * Returns the version of the release to operate on.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method are permitted to return {@code
   * null}.</p>
   *
   * @return the version of the release to operate on, or {@code null}
   *
   * @see #setVersion(Integer)
   */
  public Integer getVersion() {
    return this.version;
  }

  /**
   * Sets the version of the release to operate on.
   *
   * @param version the release version; must not be {@code null}
   *
   * @exception NullPointerException if {@code version} is {@code
   * null}
   *
   * @see #getVersion()
   */
  public void setVersion(final Integer version) {
    this.version = version;
  }
  
}
