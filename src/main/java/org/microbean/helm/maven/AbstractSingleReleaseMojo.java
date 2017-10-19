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

public abstract class AbstractSingleReleaseMojo extends AbstractReleaseMojo {

  @Parameter
  private String releaseName;
  
  protected AbstractSingleReleaseMojo() {
    super();
  }

  public String getReleaseName() {
    return this.releaseName;
  }

  public void setReleaseName(final String releaseName) {
    validateReleaseName(releaseName);
    this.releaseName = releaseName;
  }
  
  protected void validateReleaseName(final String name) {
    if (name != null && !name.isEmpty()) {
      final Matcher matcher = ReleaseManager.DNS_SUBDOMAIN_PATTERN.matcher(name);
      assert matcher != null;
      if (!matcher.matches()) {
        throw new IllegalArgumentException("Invalid release name: " + name + "; must match " + ReleaseManager.DNS_SUBDOMAIN_PATTERN.toString());
      }
    }
  }
  
}
