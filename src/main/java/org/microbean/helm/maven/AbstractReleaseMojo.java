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

import java.io.IOException;

import java.util.Objects;

import java.util.concurrent.Callable;

import java.util.regex.Matcher;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugins.annotations.Parameter;

import org.microbean.helm.ReleaseManager;
import org.microbean.helm.Tiller;

public abstract class AbstractReleaseMojo extends AbstractHelmMojo {


  /*
   * Instance fields.
   */

  
  @Parameter
  private boolean skip;
  
  @Parameter
  private Config clientConfiguration;


  /*
   * Constructors.
   */

  
  protected AbstractReleaseMojo() {
    super();
  }


  /*
   * Public instance methods.
   */

  
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    final Log log = this.getLog();
    assert log != null;

    if (this.getSkip()) {
      if (log.isDebugEnabled()) {
        log.debug("Skipping execution by request.");
      }
      return;
    }

    final ReleaseManagerCallable releaseManagerCallable = new ReleaseManagerCallable();
    Throwable throwable = null;
    try {
      this.execute(releaseManagerCallable);
    } catch (final InterruptedException interruptedException) {
      Thread.currentThread().interrupt();
      final MojoExecutionException mojoExecutionException = new MojoExecutionException(interruptedException.getMessage(), interruptedException);
      throwable = mojoExecutionException;
      throw mojoExecutionException;
    } catch (final RuntimeException | MojoExecutionException | MojoFailureException throwMe) {
      throwable = throwMe;
      throw throwMe;
    } catch (final Exception otherStuff) {
      final MojoExecutionException mojoExecutionException = new MojoExecutionException(otherStuff.getMessage(), otherStuff);
      throwable = mojoExecutionException;
      throw mojoExecutionException;
    } finally {
      if (releaseManagerCallable.releaseManager != null) {
        try {
          releaseManagerCallable.releaseManager.close();
        } catch (final IOException ioException) {
          if (throwable != null) {
            throwable.addSuppressed(ioException);
          } else {
            throw new MojoExecutionException(ioException.getMessage(), ioException);
          }
        }
      }
    }
  }

  public Config getClientConfiguration() {
    return this.clientConfiguration;
  }

  public void setClientConfiguration(final Config config) {
    this.clientConfiguration = config;
  }

  public boolean getSkip() {
    return this.skip;
  }

  public void setSkip(final boolean skip) {
    this.skip = skip;
  }


  /*
   * Protected instance methods.
   */
  
  
  protected abstract void execute(final Callable<ReleaseManager> releaseManagerCallable) throws Exception;

  protected DefaultKubernetesClient createClient() throws IOException {
    final DefaultKubernetesClient client;
    final Config config = this.getClientConfiguration();
    if (config == null) {
      client = new DefaultKubernetesClient();
    } else {
      client = new DefaultKubernetesClient(config);
    }
    return client;
  }
  
  protected Tiller createTiller(final DefaultKubernetesClient client) throws IOException {
    Objects.requireNonNull(client);
    return new Tiller(client);
  }

  protected ReleaseManager createReleaseManager(final Tiller tiller) throws IOException {
    Objects.requireNonNull(tiller);
    return new ReleaseManager(tiller);
  }

  protected void validateNamespace(final String namespace) {
    if (namespace != null) {
      final int namespaceLength = namespace.length();
      if (namespaceLength > ReleaseManager.DNS_LABEL_MAX_LENGTH) {
        throw new IllegalArgumentException("Invalid namespace: " + namespace + "; length is greater than " + ReleaseManager.DNS_LABEL_MAX_LENGTH + " characters: " + namespaceLength);
      } else if (namespaceLength > 0) {
        final Matcher matcher = ReleaseManager.DNS_LABEL_PATTERN.matcher(namespace);
        assert matcher != null;
        if (!matcher.matches()) {
          throw new IllegalArgumentException("Invalid namespace: " + namespace + "; must match " + ReleaseManager.DNS_LABEL_PATTERN.toString());
        }
      }
    }
  }
  

  /*
   * Inner and nested classes.
   */

  
  private final class ReleaseManagerCallable implements Callable<ReleaseManager> {

    private ReleaseManager releaseManager;
    
    private ReleaseManagerCallable() {
      super();
    }

    @Override
    public final ReleaseManager call() throws IOException {
      if (this.releaseManager == null) {
        this.releaseManager = createReleaseManager(createTiller(createClient()));
      }
      return this.releaseManager;
    }
    
  }
  
}
