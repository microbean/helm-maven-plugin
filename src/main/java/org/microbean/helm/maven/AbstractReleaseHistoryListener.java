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

/**
 * A {@link ReleaseHistoryListener} that {@linkplain
 * Log#info(CharSequence) logs} the {@link
 * hapi.services.tiller.Tiller.GetHistoryResponseOrBuilder}
 * {@linkplain
 * ReleaseHistoryEvent#getHistoryResponseOrBuilder() associated
 * with a <code>ReleaseHistoryEvent</code>}.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ReleaseHistoryEvent
 *
 * @see ReleaseHistoryListener
 */
public class AbstractReleaseHistoryListener implements ReleaseHistoryListener {


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link AbstractReleaseHistoryListener}.
   */
  public AbstractReleaseHistoryListener() {
    super();
  }


  /*
   * Instance methods.
   */
  

  /**
   * {@linkplain Log#info(CharSequence) Logs} the {@link
   * hapi.services.tiller.Tiller.GetHistoryResponseOrBuilder}
   * {@linkplain ReleaseHistoryEvent#getHistoryResponseOrBuilder()
   * associated with a <code>ReleaseHistoryEvent</code>}.
   *
   * @param event the {@link ReleaseHistoryEvent} describing the
   * release; may be {@code null} in which case no action will be
   * taken
   *
   * @see ReleaseHistoryEvent
   *
   * @see
   * hapi.services.tiller.Tiller.GetHistoryResponse#toString()
   */
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
