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

/**
 * An {@link EventListener} that is notified when a <a
 * href="https://docs.helm.sh/glossary/#release">Helm release</a> has
 * been retrieved, usually by a {@link ListReleasesMojo} instance.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ReleaseDiscoveryEvent
 *
 * @see ListReleasesMojo
 */
public interface ReleaseDiscoveryListener extends EventListener {

  /**
   * Called when a <a
   * href="https://docs.helm.sh/glossary/#release">Helm release</a>
   * has been {@linkplain ListReleasesMojo retrieved}.
   *
   * @param event the {@link ReleaseDiscoveryEvent} describing the
   * release discovery
   *
   * @see ReleaseDiscoveryEvent
   *
   * @see ListReleasesMojo
   */
  public void releaseDiscovered(final ReleaseDiscoveryEvent event);
  
}
