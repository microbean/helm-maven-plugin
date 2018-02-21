/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017-2018 microBean.
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

import hapi.services.tiller.Tiller.GetHistoryResponseOrBuilder;

/**
 * An {@link AbstractReleaseEvent} describing a retrieval of the
 * history of a <a href="https://docs.helm.sh/glossary/#release">Helm
 * release</a>.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ReleaseHistoryListener
 *
 * @see GetHistoryMojo
 */
public class ReleaseHistoryEvent extends AbstractReleaseEvent {

  
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
   * hapi.services.tiller.Tiller.GetHistoryResponseOrBuilder}
   * describing the history retrieval.
   *
   * <p>This field will never be {@code null}.</p>
   *
   * @see #ReleaseHistoryEvent(GetHistoryMojo,
   * hapi.services.tiller.Tiller.GetHistoryResponseOrBuilder)
   */
  private final GetHistoryResponseOrBuilder getHistoryResponseOrBuilder;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link ReleaseHistoryEvent}.
   *
   * @param source the {@link GetHistoryMojo} responsible for
   * retrieving the history; must not be {@code null}
   *
   * @param getHistoryResponseOrBuilder the {@link
   * hapi.services.tiller.Tiller.GetHistoryResponseOrBuilder}
   * describing the history retrieval; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code source} is {@code
   * null}; thrown by the {@link EventObject#EventObject(Object)}
   * constructor
   *
   * @exception NullPointerException if {@code
   * getReleaseHistoryResponseOrBuilder} is {@code null}
   */
  public ReleaseHistoryEvent(final GetHistoryMojo source, final GetHistoryResponseOrBuilder getHistoryResponseOrBuilder) {
    super(source);
    Objects.requireNonNull(getHistoryResponseOrBuilder);
    this.getHistoryResponseOrBuilder = getHistoryResponseOrBuilder;
  }

  
  /*
   * Instance methods.
   */


  /**
   * Returns the {@link
   * hapi.services.tiller.Tiller.GetHistoryResponseOrBuilder}
   * implementation representing the history retrieval.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link
   * hapi.services.tiller.Tiller.GetHistoryResponseOrBuilder}
   * implementation representing the history retrieval; never {@code
   * null}
   */
  public final GetHistoryResponseOrBuilder getHistoryResponseOrBuilder() {
    return this.getHistoryResponseOrBuilder;
  }

  /**
   * Returns the {@link GetHistoryMojo} responsible for firing
   * this event.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link GetHistoryMojo} responsible for firing this
   * event; never {@code null}
   */
  @Override
  public final GetHistoryMojo getSource() {
    return (GetHistoryMojo)super.getSource();
  }
  
}
