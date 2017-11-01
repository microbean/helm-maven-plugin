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

import hapi.services.tiller.Tiller.GetReleaseContentResponseOrBuilder;

import org.apache.maven.plugin.logging.Log;

/**
 * A {@link ReleaseContentListener} that {@linkplain
 * Log#info(CharSequence) logs} the {@link
 * hapi.services.tiller.Tiller.GetReleaseContentResponseOrBuilder}
 * {@linkplain
 * ReleaseContentEvent#getReleaseContentResponseOrBuilder() associated
 * with a <code>ReleaseContentEvent</code>}.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ReleaseContentEvent
 *
 * @see ReleaseContentListener
 */
public class AbstractReleaseContentListener implements ReleaseContentListener {


  /*
   * Constructors.
   */

  
  /**
   * Creates a new {@link AbstractReleaseContentListener}.
   */
  public AbstractReleaseContentListener() {
    super();
  }


  /*
   * Instance methods.
   */
  

  /**
   * {@linkplain Log#info(CharSequence) Logs} the {@link
   * hapi.services.tiller.Tiller.GetReleaseContentResponseOrBuilder}
   * {@linkplain
   * ReleaseContentEvent#getReleaseContentResponseOrBuilder()
   * associated with a <code>ReleaseContentEvent</code>}.
   *
   * @param event the {@link ReleaseContentEvent} describing the
   * release content; may be {@code null} in which case no action will
   * be taken
   *
   * @see ReleaseContentEvent
   *
   * @see
   * hapi.services.tiller.Tiller.GetReleaseContentResponseOrBuilder#toString()
   */
  @Override
  public void releaseContentRetrieved(final ReleaseContentEvent event) {
    if (event != null) {
      final Log log = event.getLog();
      if (log != null && log.isInfoEnabled()) {
        log.info(String.valueOf(event.getReleaseContentResponseOrBuilder()));
      }
    }
  }
  
}
