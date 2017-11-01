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

import java.io.Serializable; // for javadoc only

import java.util.EventObject; // for javadoc only
import java.util.Objects;

import hapi.services.tiller.Tiller.ListReleasesResponseOrBuilder;

/**
 * An {@link AbstractReleaseEvent} describing a discovery of a <a
 * href="https://docs.helm.sh/glossary/#release">Helm release</a>
 * performed by a {@link ListReleasesMojo} instance.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ReleaseDiscoveryListener
 *
 * @see ListReleasesMojo
 */
public class ReleaseDiscoveryEvent extends AbstractReleaseEvent {

  
  /*
   * Static fields.
   */


  /**
   * The version of this class for {@linkplain Serializable
   * serialization} purposes.
   */
  private static final long serialVersionUID = 1L;
    

  /*
   * Instance fields.
   */


  /**
   * The {@link
   * hapi.services.tiller.Tiller.ListReleasesResponseOrBuilder}
   * describing the release discovery.
   *
   * <p>This field will never be {@code null}.</p>
   *
   * @see #ReleaseDiscoveryEvent(ListReleasesMojo,
   * hapi.services.tiller.Tiller.ListReleasesResponseOrBuilder)
   */
  private final ListReleasesResponseOrBuilder listReleasesResponseOrBuilder;
  

  /*
   * Constructors.
   */


  /**
   * Creates a new {@link ReleaseDiscoveryEvent}.
   *
   * @param source the {@link ListReleasesMojo} responsible for
   * retrieving the release; must not be {@code null}
   *
   * @param listReleasesResponseOrBuilder the {@link
   * hapi.services.tiller.Tiller.ListReleasesResponseOrBuilder}
   * describing the release retrieval; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code source} is {@code
   * null}; thrown by the {@link EventObject#EventObject(Object)}
   * constructor
   *
   * @exception NullPointerException if {@code
   * listReleasesResponseOrBuilder} is {@code null}
   */
  public ReleaseDiscoveryEvent(final ListReleasesMojo source, final ListReleasesResponseOrBuilder listReleasesResponseOrBuilder) {
    super(source);
    Objects.requireNonNull(listReleasesResponseOrBuilder);
    this.listReleasesResponseOrBuilder = listReleasesResponseOrBuilder;
  }

  
  /*
   * Instance methods.
   */


  /**
   * Returns the {@link
   * hapi.services.tiller.Tiller.ListReleasesResponseOrBuilder}
   * implementation representing the release retrieval.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link
   * hapi.services.tiller.Tiller.ListReleasesResponseOrBuilder}
   * implementation representing the release retrieval; never {@code
   * null}
   */
  public final ListReleasesResponseOrBuilder getListReleasesResponseOrBuilder() {
    return this.listReleasesResponseOrBuilder;
  }
  
  /**
   * Returns the {@link ListReleasesMojo} responsible for firing this
   * event.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link ListReleasesMojo} responsible for firing this
   * event; never {@code null}
   */
  @Override
  public final ListReleasesMojo getSource() {
    return (ListReleasesMojo)super.getSource();
  }
  
}
