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

import java.util.EventObject;
import java.util.Objects;

import hapi.services.tiller.Tiller.GetHistoryResponseOrBuilder;

import org.apache.maven.plugin.logging.Log;

public class ReleaseHistoryEvent extends EventObject {

  private final GetHistoryResponseOrBuilder getHistoryResponseOrBuilder;

  private final Log log;
  
  public ReleaseHistoryEvent(final GetHistoryMojo source, final Log log, final GetHistoryResponseOrBuilder getHistoryResponseOrBuilder) {
    super(source);
    Objects.requireNonNull(getHistoryResponseOrBuilder);
    this.getHistoryResponseOrBuilder = getHistoryResponseOrBuilder;
    this.log = log;
  }

  public final GetHistoryResponseOrBuilder getHistoryResponseOrBuilder() {
    return this.getHistoryResponseOrBuilder;
  }

  public final Log getLog() {
    return this.log;
  }

  @Override
  public final GetHistoryMojo getSource() {
    return (GetHistoryMojo)super.getSource();
  }
  
}
