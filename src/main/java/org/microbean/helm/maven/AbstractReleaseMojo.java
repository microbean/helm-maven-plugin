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

import java.io.IOException;

import java.util.Map;
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

/**
 * An {@link AbstractHelmMojo} that provides other <a
 * href="https://microbean.github.io/microbean-helm/">Helm</a>-related
 * <a
 * href="https://maven.apache.org/developers/mojo-api-specification.html">mojo</a>
 * implementations the ability to work with a {@link ReleaseManager}.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #execute(Callable)
 */
public abstract class AbstractReleaseMojo extends AbstractHelmMojo {


  /*
   * Instance fields.
   */


  /**
   * Whether to skip execution.
   */
  @Parameter(defaultValue = "false", property = "helm.skip")
  private boolean skip;

  /**
   * The <a
   * href="https://static.javadoc.io/io.fabric8/kubernetes-client/3.1.8/io/fabric8/kubernetes/client/Config.html">{@code
   * Config}</a> describing how a <a
   * href="https://static.javadoc.io/io.fabric8/kubernetes-client/3.1.8/io/fabric8/kubernetes/client/DefaultKubernetesClient.html">{@code
   * DefaultKubernetesClient}</a> should connect to a Kubernetes
   * cluster.
   */
  @Parameter
  private Config clientConfiguration;

  /**
   * The Kubernetes cluster namespace in which Tiller may be found.
   */
  @Parameter(defaultValue = "kube-system", property = "tiller.namespace")
  private String tillerNamespace;
  
  /**
   * The port on which Tiller may be reached.
   */
  @Parameter(defaultValue = "44134", property = "tiller.port")
  private int tillerPort;

  /**
   * The Kubernetes labels normally found on Tiller pods.
   */
  @Parameter(property = "tiller.labels")
  private Map<String, String> tillerLabels;

  
  /*
   * Constructors.
   */


  /**
   * Creates a new {@link AbstractReleaseMojo}.
   */
  protected AbstractReleaseMojo() {
    super();
  }


  /*
   * Public instance methods.
   */


  /**
   * {@linkplain #getSkip() Skips} execution if instructed, or calls
   * the {@link #execute(Callable)} method with a {@link Callable}
   * containing the results of an invocation of the {@link
   * #createReleaseManager(Tiller)} method.
   *
   * @exception MojoExecutionException if there was a problem
   * executing this mojo
   *
   * @exception MojoFailureException if the mojo executed properly,
   * but the job it was to perform failed in some way
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

  /**
   * Returns the {@link Config} describing how a {@link
   * DefaultKubernetesClient} is to connect to a Kubernetes cluster.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * @return a {@link Config}, or {@code null}
   *
   * @see #setClientConfiguration(Config)
   */
  public Config getClientConfiguration() {
    return this.clientConfiguration;
  }

  /**
   * Installs the {@link Config} describing how a {@link
   * DefaultKubernetesClient} is to connect to a Kubernetes cluster.
   *
   * @param config the {@link Config} to use; may be {@code null}
   *
   * @see #getClientConfiguration()
   */
  public void setClientConfiguration(final Config config) {
    this.clientConfiguration = config;
  }  

  /**
   * Returns {@code true} if this {@link AbstractReleaseMojo} should
   * not execute.
   *
   * @return {@code true} if this {@link AbstractReleaseMojo} should
   * not execute; {@code false} otherwise
   *
   * @see #setSkip(boolean)
   */
  public boolean getSkip() {
    return this.skip;
  }

  /**
   * Controls whether this {@link AbstractReleaseMojo} should execute.
   *
   * @param skip if {@code true}, this {@link AbstractReleaseMojo}
   * will not execute
   *
   * @see #getSkip()
   */
  public void setSkip(final boolean skip) {
    this.skip = skip;
  }

  /**
   * Returns the Kubernetes namespace in which Tiller may be found.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the Kubernetes namespace in which Tiller may be found, or
   * {@code null}
   */
  public String getTillerNamespace() {
    return this.tillerNamespace;
  }

  /**
   * Sets the Kubernetes namespace in which Tiller may be found.
   *
   * @param tillerNamespace the Kubernetes namespace in which Tiller
   * may be found; may be {@code null} in which case {@code
   * kube-system} will be used by the {@link
   * #createTiller(DefaultKubernetesClient)} method instead
   */
  public void setTillerNamespace(String tillerNamespace) {
    this.tillerNamespace = tillerNamespace;
  }

  /**
   * Returns the port on which Tiller may be found.
   *
   * @return the port on which Tiller may be found; normally {@code
   * 44134}
   */
  public int getTillerPort() {
    return this.tillerPort;
  }

  /**
   * Sets the port on which Tiller may be found.
   *
   * @param tillerPort the port on which Tiller may be found; normally
   * {@code 44134}
   */
  public void setTillerPort(final int tillerPort) {
    this.tillerPort = tillerPort;
  }

  /**
   * Returns the Kubernetes labels that Tiller Pods have.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the Kubernetes labels that Tiller Pods have, or {@code
   * null}
   */
  public Map<String, String> getTillerLabels() {
    return this.tillerLabels;
  }

  /**
   * Sets the Kubernetes labels that Tiller Pods have.
   *
   * <p>Tiller Pods are normally labeled with {@code app = helm} and
   * {@code name = tiller}.</p>
   *
   * @param tillerLabels a {@link Map} containing the labels; may be
   * {@code null}
   */
  public void setTillerLabels(final Map<String, String> tillerLabels) {
    this.tillerLabels = tillerLabels;
  }


  /*
   * Protected instance methods.
   */
  

  /**
   * Performs a release-oriented task using a {@link ReleaseManager}
   * {@linkplain Callable#call() available} from the supplied {@link
   * Callable}.
   *
   * @param releaseManagerCallable the {@link Callable} that will
   * provide a {@link ReleaseManager}; must not be {@code null}
   *
   * @exception Exception if an error occurs
   */
  protected abstract void execute(final Callable<ReleaseManager> releaseManagerCallable) throws Exception;

  /**
   * Creates a {@link DefaultKubernetesClient} for communicating with
   * Kubernetes clusters.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * <p>The default implementation calls the {@link
   * #getClientConfiguration()} method and {@linkplain
   * DefaultKubernetesClient#DefaultKubernetesClient(Config) uses its
   * return value}, unless it is {@code null}, in which case a new
   * {@link DefaultKubernetesClient} is created via its {@linkplain
   * DefaultKubernetesClient#DefaultKubernetesClient() no-argument
   * constructor}.</p>
   *
   * @return a new, non-{@code null} {@link DefaultKubernetesClient}
   *
   * @exception IOException if there was a problem creating the client
   */
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

  /**
   * Creates a {@link Tiller} and returns it.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * <p>This implementation passes the supplied {@link
   * DefaultKubernetesClient} to the <a
   * href="https://microbean.github.io/microbean-helm/apidocs/org/microbean/helm/Tiller.html#Tiller-T-">appropriate
   * <code>Tiller</code> constructor</a>.</p>
   *
   * @param client the {@link DefaultKubernetesClient} to use to
   * communicate with a Kubernetes cluster; must not be {@code null}
   *
   * @return a new {@link Tiller}; never {@code null}
   *
   * @exception NullPointerException if {@code client} is {@code null}
   *
   * @exception IOException if there was a problem creating a {@link
   * Tiller}
   */
  protected Tiller createTiller(final DefaultKubernetesClient client) throws IOException {
    Objects.requireNonNull(client);
    return new Tiller(client, this.getTillerNamespace(), this.getTillerPort(), this.getTillerLabels());
  }

  /**
   * Creates a {@link ReleaseManager} and returns it.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * <p>This implementation passes the supplied {@link
   * Tiller} to the {@linkplain ReleaseManager#ReleaseManager(Tiller)
   * appropriate <code>ReleaseManager</code> constructor}.</p>
   *
   * @param tiller the {@link Tiller} to use to communicate with a
   * Tiller server; must not be {@code null}
   *
   * @return a new {@link ReleaseManager}; never {@code null}
   *
   * @exception NullPointerException if {@code tiller} is {@code null}
   *
   * @exception IOException if there was a problem creating a {@link
   * ReleaseManager}
   */
  protected ReleaseManager createReleaseManager(final Tiller tiller) throws IOException {
    Objects.requireNonNull(tiller);
    return new ReleaseManager(tiller);
  }

  /**
   * Validates a <a
   * href="https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/">Kubernetes
   * namespace</a> for correctness.
   *
   * <p>The default implementation checks the supplied {@code
   * namespace} to see if it is less than or equal to {@value
   * ReleaseManager#DNS_LABEL_MAX_LENGTH} characters, and if it
   * {@linkplain Matcher#matches() matches} the value of the {@link
   * ReleaseManager#DNS_LABEL_PATTERN} field.</p>
   *
   * @param namespace the <a
   * href="https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/">namespace</a>
   * to validate; may be {@code null}
   *
   * @exception IllegalArgumentException if {@code namespace} is
   * invalid
   */
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


  /**
   * A {@link Callable} whose {@link #call()} method yields the same
   * {@link ReleaseManager} for every invocation, {@linkplain
   * AbstractReleaseMojo#createReleaseManager(Tiller) creating one} if
   * necessary.
   *
   * @author <a href="https://about.me/lairdnelson"
   * target="_parent">Laird Nelson</a>
   *
   * @see AbstractReleaseMojo#createReleaseManager(Tiller)
   *
   * @see AbstractReleaseMojo#createTiller(DefaultKubernetesClient)
   *
   * @see AbstractReleaseMojo#createClient()
   */
  private final class ReleaseManagerCallable implements Callable<ReleaseManager> {


    /*
     * Instance fields.
     */


    /**
     * The {@link ReleaseManager} to return from the {@link #call()}
     * method.
     *
     * <p>This field may be {@code null}.</p>
     *
     * @sed #call()
     */
    private ReleaseManager releaseManager;


    /*
     * Constructors.
     */


    /**
     * Creates a new {@link ReleaseManagerCallable}.
     */
    private ReleaseManagerCallable() {
      super();
    }


    /*
     * Instance methods.
     */


    /**
     * Returns a {@link ReleaseManager}, {@linkplain
     * AbstractReleaseMojo#createReleaseManager(Tiller) creating one}
     * if necessary.
     *
     * <p>This method never returns {@code null}.</p>
     *
     * @return a {@link ReleaseManager}; never {@code null}
     *
     * @exception IOException if there was a problem creating a {@link
     * ReleaseManager}
     */
    @Override
    public final ReleaseManager call() throws IOException {
      if (this.releaseManager == null) {
        this.releaseManager = createReleaseManager(createTiller(createClient()));
      }
      return this.releaseManager;
    }
    
  }
  
}
