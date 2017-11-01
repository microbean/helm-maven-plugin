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

import hapi.services.tiller.Tiller.GetReleaseStatusResponseOrBuilder;

/**
 * An {@link AbstractReleaseEvent} describing a retrieval of the
 * status of a <a href="https://docs.helm.sh/glossary/#release">Helm
 * release</a>.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ReleaseStatusListener
 *
 * @see GetReleaseStatusMojo
 */
public class ReleaseStatusEvent extends AbstractReleaseEvent {


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
   * hapi.services.tiller.Tiller.GetReleaseStatusResponseOrBuilder}
   * describing the status retrieval.
   *
   * <p>This field will never be {@code null}.</p>
   *
   * @see #ReleaseStatusEvent(GetReleaseStatusMojo,
   * hapi.services.tiller.Tiller.GetReleaseStatusResponseOrBuilder)
   */
  private final GetReleaseStatusResponseOrBuilder getReleaseStatusResponseOrBuilder;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link ReleaseStatusEvent}.
   *
   * @param source the {@link GetReleaseStatusMojo} responsible for
   * retrieving the status; must not be {@code null}
   *
   * @param getReleaseStatusResponseOrBuilder the {@link
   * hapi.services.tiller.Tiller.GetReleaseStatusResponseOrBuilder}
   * describing the status retrieval; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code source} is {@code
   * null}; thrown by the {@link EventObject#EventObject(Object)}
   * constructor
   *
   * @exception NullPointerException if {@code
   * getReleaseStatusResponseOrBuilder} is {@code null}
   */
  public ReleaseStatusEvent(final GetReleaseStatusMojo source, final GetReleaseStatusResponseOrBuilder getReleaseStatusResponseOrBuilder) {
    super(source);
    Objects.requireNonNull(getReleaseStatusResponseOrBuilder);
    this.getReleaseStatusResponseOrBuilder = getReleaseStatusResponseOrBuilder;
  }


  /*
   * Instance methods.
   */


  /**
   * Returns the {@link
   * hapi.services.tiller.Tiller.GetReleaseStatusResponseOrBuilder}
   * implementation representing the status retrieval.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link
   * hapi.services.tiller.Tiller.GetReleaseStatusResponseOrBuilder}
   * implementation representing the status retrieval; never {@code
   * null}
   */
  public final GetReleaseStatusResponseOrBuilder getReleaseStatusResponseOrBuilder() {
    return this.getReleaseStatusResponseOrBuilder;
  }

  /**
   * Returns the {@link GetReleaseStatusMojo} responsible for firing
   * this event.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link GetReleaseStatusMojo} responsible for firing
   * this event; never {@code null}
   */
  @Override
  public final GetReleaseStatusMojo getSource() {
    return (GetReleaseStatusMojo)super.getSource();
  }
  
}
