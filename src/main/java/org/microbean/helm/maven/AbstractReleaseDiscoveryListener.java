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

import hapi.services.tiller.Tiller.ListReleasesResponseOrBuilder;

import org.apache.maven.plugin.logging.Log;

/**
 * A {@link ReleaseDiscoveryListener} that {@linkplain
 * Log#info(CharSequence) logs} the {@link
 * hapi.services.tiller.Tiller.ListReleasesResponseOrBuilder}
 * {@linkplain
 * ReleaseDiscoveryEvent#getListReleasesResponseOrBuilder() associated
 * with a <code>ReleaseDiscoveryEvent</code>}.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ReleaseDiscoveryEvent
 *
 * @see ReleaseDiscoveryListener
 */
public class AbstractReleaseDiscoveryListener implements ReleaseDiscoveryListener {


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link AbstractReleaseDiscoveryListener}.
   */
  public AbstractReleaseDiscoveryListener() {
    super();
  }

  

  /*
   * Instance methods.
   */
  

  /**
   * {@linkplain Log#info(CharSequence) Logs} the {@link
   * hapi.services.tiller.Tiller.ListReleasesResponseOrBuilder}
   * {@linkplain
   * ReleaseDiscoveryEvent#getListReleasesResponseOrBuilder()
   * associated with a <code>ReleaseDiscoveryEvent</code>}.
   *
   * @param event the {@link ReleaseDiscoveryEvent} describing the
   * release; may be {@code null} in which case no action will be
   * taken
   *
   * @see ReleaseDiscoveryEvent
   *
   * @see
   * hapi.services.tiller.Tiller.ListReleasesResponse#toString()
   */
  @Override
  public void releaseDiscovered(final ReleaseDiscoveryEvent event) {
    if (event != null) {
      final Log log = event.getLog();
      if (log != null && log.isInfoEnabled()) {
        log.info(String.valueOf(event.getListReleasesResponseOrBuilder()));
      }
    }
  }
  
}
