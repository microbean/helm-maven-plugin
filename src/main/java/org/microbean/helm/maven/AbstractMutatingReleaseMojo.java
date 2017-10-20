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

public abstract class AbstractMutatingReleaseMojo extends AbstractSingleReleaseMojo {

  @Parameter
  private boolean disableHooks;
  
  @Parameter
  private boolean dryRun;
  
  @Parameter(defaultValue = "300")
  private long timeout; // in seconds

  @Parameter
  private boolean wait;
  
  protected AbstractMutatingReleaseMojo() {
    super();
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

  public long getTimeout() {
    return this.timeout;
  }

  public void setTimeout(final long timeout) {
    this.timeout = timeout;
  }

  public boolean getWait() {
    return this.wait;
  }

  public void setWait(final boolean wait) {
    this.wait = wait;
  }
  
}
