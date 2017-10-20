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

public abstract class AbstractForceableMutatingReleaseMojo extends AbstractMutatingReleaseMojo {

  @Parameter
  private boolean force;

  @Parameter
  private boolean recreate;
  
  protected AbstractForceableMutatingReleaseMojo() {
    super();
  }

  public boolean getForce() {
    return this.force;
  }
  
  public void setForce(final boolean force) {
    this.force = force;
  }

  public boolean getRecreate() {
    return this.recreate;
  }
  
  public void setRecreate(final boolean recreate) {
    this.recreate = recreate;
  }

}
