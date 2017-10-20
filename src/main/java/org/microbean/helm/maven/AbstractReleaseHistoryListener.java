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

import java.util.EventListener;
import java.util.Objects;

import hapi.services.tiller.Tiller.GetHistoryResponseOrBuilder;

import org.apache.maven.plugin.logging.Log;

public class AbstractReleaseHistoryListener implements ReleaseHistoryListener {

  public AbstractReleaseHistoryListener() {
    super();
  }

  @Override
  public void releaseHistoryRetrieved(final ReleaseHistoryEvent event) {
    if (event != null) {
      final GetHistoryResponseOrBuilder getHistoryResponseOrBuilder = event.getHistoryResponseOrBuilder();
      final Log log = event.getLog();
      if (log != null && log.isInfoEnabled()) {
        log.info(String.valueOf(getHistoryResponseOrBuilder));
      }
    }
  }
  
}
