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

import java.io.Serializable;

import java.util.EventObject;
import java.util.Objects;

import org.apache.maven.plugin.logging.Log;

/**
 * An {@link EventObject} describing a meaningful event in the life of
 * a <a href="https://docs.helm.sh/glossary/#release">Helm
 * release</a>.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see EventObject
 */
public abstract class AbstractReleaseEvent extends EventObject {


  /*
   * Static fields.
   */


  /**
   * The version of this class for {@linkplain Serializable
   * serialization} purposes.
   */
  private static final long serialVersionUID = 1L;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link AbstractReleaseEvent}.
   *
   * @param source the {@link AbstractReleaseMojo} responsible for
   * firing the event; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code source} is {@code
   * null}; thrown by the {@link EventObject#EventObject(Object)}
   * constructor
   */
  protected AbstractReleaseEvent(final AbstractReleaseMojo source) {
    super(source);
  }


  /*
   * Instance methods.
   */


  /**
   * Returns the {@link Log} that may be used to log information.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the {@link Log} that may be used to log information, or
   * {@code null}
   */
  public final Log getLog() {
    final AbstractReleaseMojo source = this.getSource();
    final Log log;
    if (source == null) {
      log = null;
    } else {
      log = source.getLog();
    }
    return log;
  }

  /**
   * Overrides the {@link EventObject#getSource()} method to cast its
   * return type to an {@link AbstractReleaseMojo}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * @return the {@link AbstractReleaseMojo} implementation that fired
   * this event; never {@code null}
   */
  @Override
  public AbstractReleaseMojo getSource() {
    return (AbstractReleaseMojo)super.getSource();
  }
  
}
