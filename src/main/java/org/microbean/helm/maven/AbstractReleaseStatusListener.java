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

import hapi.services.tiller.Tiller.GetReleaseStatusResponseOrBuilder;

import org.apache.maven.plugin.logging.Log;

/**
 * A {@link ReleaseStatusListener} that {@linkplain
 * Log#info(CharSequence) logs} the {@link
 * hapi.services.tiller.Tiller.GetReleaseStatusResponseOrBuilder}
 * {@linkplain
 * ReleaseStatusEvent#getReleaseStatusResponseOrBuilder() associated
 * with a <code>ReleaseStatusEvent</code>}.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ReleaseStatusEvent
 *
 * @see ReleaseStatusListener
 */
public class AbstractReleaseStatusListener implements ReleaseStatusListener {


  /*
   * Constructors.
   */

  
  /**
   * Creates a new {@link AbstractReleaseStatusListener}.
   */
  public AbstractReleaseStatusListener() {
    super();
  }


  /*
   * Instance methods.
   */
  

  /**
   * {@linkplain Log#info(CharSequence) Logs} the {@link
   * hapi.services.tiller.Tiller.GetReleaseStatusResponseOrBuilder}
   * {@linkplain
   * ReleaseStatusEvent#getReleaseStatusResponseOrBuilder()
   * associated with a <code>ReleaseStatusEvent</code>}.
   *
   * @param event the {@link ReleaseStatusEvent} describing the
   * release status; may be {@code null} in which case no action will
   * be taken
   *
   * @see ReleaseStatusEvent
   *
   * @see
   * hapi.services.tiller.Tiller.GetReleaseStatusResponseOrBuilder#toString()
   */
  @Override
  public void releaseStatusRetrieved(final ReleaseStatusEvent event) {
    if (event != null) {
      final GetReleaseStatusResponseOrBuilder getReleaseStatusResponseOrBuilder = event.getReleaseStatusResponseOrBuilder();
      final Log log = event.getLog();
      if (log != null && log.isInfoEnabled()) {
        log.info(String.valueOf(getReleaseStatusResponseOrBuilder));
      }
    }
  }
  
}
