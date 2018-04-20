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
 * An {@link AbstractReleaseMojo} that works on exactly one <a
 * href="https://docs.helm.sh/glossary/#release">Helm release</a>.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
public abstract class AbstractSingleReleaseMojo extends AbstractReleaseMojo {


  /*
   * Instance fields.
   */


  /**
   * The name of the release.
   */
  @Parameter(required = true, property = "helm.releaseName")
  private String releaseName;


  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link AbstractSingleReleaseMojo}.
   */
  protected AbstractSingleReleaseMojo() {
    super();
  }


  /*
   * Instance methods.
   */
  

  /**
   * Returns the release name.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * @return the release name, or {@code null}
   *
   * @see #setReleaseName(String)
   */
  public String getReleaseName() {
    return this.releaseName;
  }

  /**
   * Sets the release name.
   *
   * @param releaseName the name of the release; must not be {@code
   * null} and must {@linkplain #validateReleaseName(String) pass
   * validation}
   *
   * @see #getReleaseName()
   */
  public void setReleaseName(final String releaseName) {
    validateReleaseName(releaseName);
    this.releaseName = releaseName;
  }

  /**
   * Validates the supplied {@code name} as a <a
   * href="https://docs.helm.sh/glossary/#release">Helm release</a>
   * name.
   *
   * <p>This implementation checks to see if the supplied {@code name}
   * is non-{@code null}, not {@linkplain String#isEmpty() empty}, and
   * {@linkplain Matcher#matches() matches} the value of the {@link
   * ReleaseManager#DNS_SUBDOMAIN_PATTERN} field.</p>
   *
   * @param name the release name to validate; must not be {@code
   * null}
   *
   * @exception IllegalArgumentException if {@code name} is not valid
   */
  protected void validateReleaseName(final String name) {
    if (name == null) {
      throw new IllegalArgumentException("Invalid release name: null");
    } else if (name.isEmpty()) {
      throw new IllegalArgumentException("Invalid release name: ");
    } else {
      final Matcher matcher = ReleaseManager.DNS_SUBDOMAIN_PATTERN.matcher(name);
      assert matcher != null;
      if (!matcher.matches()) {
        throw new IllegalArgumentException("Invalid release name: " + name + "; must match " + ReleaseManager.DNS_SUBDOMAIN_PATTERN.toString());
      }
    }
  }
  
}
