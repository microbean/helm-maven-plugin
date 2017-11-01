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

import hapi.services.tiller.Tiller.GetReleaseContentResponseOrBuilder;

/**
 * An {@link AbstractReleaseEvent} describing a retrieval of the
 * content of a <a href="https://docs.helm.sh/glossary/#release">Helm
 * release</a>.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ReleaseContentListener
 *
 * @see GetReleaseContentMojo
 */
public class ReleaseContentEvent extends AbstractReleaseEvent {

  
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
   * hapi.services.tiller.Tiller.GetReleaseContentResponseOrBuilder}
   * describing the release content retrieval.
   *
   * <p>This field will never be {@code null}.</p>
   *
   * @see #ReleaseContentEvent(GetReleaseContentMojo,
   * hapi.services.tiller.Tiller.GetReleaseContentResponseOrBuilder)
   */
  private final GetReleaseContentResponseOrBuilder getReleaseContentResponseOrBuilder;
  
  public ReleaseContentEvent(final GetReleaseContentMojo source, final GetReleaseContentResponseOrBuilder getReleaseContentResponseOrBuilder) {
    super(source);
    Objects.requireNonNull(getReleaseContentResponseOrBuilder);
    this.getReleaseContentResponseOrBuilder = getReleaseContentResponseOrBuilder;
  }

  
  /*
   * Instance methods.
   */


  /**
   * Returns the {@link
   * hapi.services.tiller.Tiller.GetReleaseContentResponseOrBuilder}
   * implementation representing the release content retrieval.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link
   * hapi.services.tiller.Tiller.GetReleaseContentResponseOrBuilder}
   * implementation representing the release content retrieval; never
   * {@code null}
   */
  public final GetReleaseContentResponseOrBuilder getReleaseContentResponseOrBuilder() {
    return this.getReleaseContentResponseOrBuilder;
  }

  /**
   * Returns the {@link GetReleaseContentMojo} responsible for firing
   * this event.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link GetReleaseContentMojo} responsible for firing
   * this event; never {@code null}
   */
  @Override
  public final GetReleaseContentMojo getSource() {
    return (GetReleaseContentMojo)super.getSource();
  }
  
}
