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

import hapi.services.tiller.Tiller.TestReleaseResponseOrBuilder;

/**
 * An {@link AbstractReleaseEvent} describing the result of running a
 * test on a <a href="https://docs.helm.sh/glossary/#release">Helm
 * release</a>.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ReleaseTestListener
 *
 * @see TestReleaseMojo
 */
public class ReleaseTestEvent extends AbstractReleaseEvent {


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
   * hapi.services.tiller.Tiller.TestReleaseResponseOrBuilder}
   * describing the test run.
   *
   * <p>This field will never be {@code null}.</p>
   *
   * @see #ReleaseTestEvent(TestReleaseMojo,
   * hapi.services.tiller.Tiller.TestReleaseResponseOrBuilder)
   */
  private final TestReleaseResponseOrBuilder testReleaseResponseOrBuilder;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link ReleaseTestEvent}.
   *
   * @param source the {@link TestReleaseMojo} responsible for running
   * the tests; must not be {@code null}
   *
   * @param testReleaseResponseOrBuilder the {@link
   * hapi.services.tiller.Tiller.TestReleaseResponseOrBuilder}
   * describing the test run; must not be {@code null}
   *
   * @exception IllegalArgumentException if {@code source} is {@code
   * null}; thrown by the {@link EventObject#EventObject(Object)}
   * constructor
   *
   * @exception NullPointerException if {@code
   * testReleaseResponseOrBuilder} is {@code null}
   */
  public ReleaseTestEvent(final TestReleaseMojo source, final TestReleaseResponseOrBuilder testReleaseResponseOrBuilder) {
    super(source);
    Objects.requireNonNull(testReleaseResponseOrBuilder);
    this.testReleaseResponseOrBuilder = testReleaseResponseOrBuilder;
  }


  /*
   * Instance methods.
   */


  /**
   * Returns the {@link
   * hapi.services.tiller.Tiller.TestReleaseResponseOrBuilder}
   * implementation representing the test run.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link
   * hapi.services.tiller.Tiller.TestReleaseResponseOrBuilder}
   * implementation representing the test run; never {@code null}
   */
  public final TestReleaseResponseOrBuilder getTestReleaseResponseOrBuilder() {
    return this.testReleaseResponseOrBuilder;
  }

  /**
   * Returns the {@link TestReleaseMojo} responsible for firing this
   * event.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * @return the {@link TestReleaseMojo} responsible for firing this
   * event; never {@code null}
   */
  @Override
  public final TestReleaseMojo getSource() {
    return (TestReleaseMojo)super.getSource();
  }
  
}
