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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java.net.URI;
import java.net.URL;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Objects;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import java.util.regex.Matcher;

import javax.inject.Inject;

import hapi.chart.ChartOuterClass.Chart;

import hapi.release.ReleaseOuterClass.Release;

import hapi.services.tiller.Tiller.InstallReleaseRequest;
import hapi.services.tiller.Tiller.InstallReleaseResponse;

import org.apache.maven.execution.MavenSession;

import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.maven.project.MavenProject;

import org.microbean.helm.ReleaseManager;

import org.microbean.helm.chart.AbstractChartLoader;
import org.microbean.helm.chart.URLChartLoader;

/**
 * <a
 * href="https://github.com/kubernetes/helm/blob/master/docs/using_helm.md#helm-install-installing-a-package">Installs
 * a chart and hence creates a release</a>.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@Mojo(name = "install")
public class InstallReleaseMojo extends AbstractMutatingReleaseMojo {


  /*
   * Instance fields.
   */


  /**
   * The {@link MavenProject} in effect.
   */
  private final MavenProject project;

  /**
   * The {@link MavenSession} in effect.
   */
  private final MavenSession session;

  /**
   * The name of the release to install.  If omitted, a release name
   * will be generated and used instead.
   *
   * @see #getReleaseName()
   *
   * @see #setReleaseName(String)
   *
   * @see #validateReleaseName(String)
   */
  /*
   * This field shadows the AbstractSingleReleaseMojo#releaseName
   * field on purpose to relax its "required" nature.
   */
  @Parameter
  private String releaseName;

  /**
   * The <a
   * href="https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/">namespace</a>
   * into which the release will be installed.
   */
  @Parameter
  private String releaseNamespace;

  /**
   * Whether to reuse the release name for repeated installations.  It
   * is strongly recommended that you not set this to {@code true} in
   * production scenarios.
   */
  @Parameter(defaultValue = "false")
  private boolean reuseReleaseName;

  /**
   * Whether a missing or non-resolvable {@code chartUrl} parameter
   * will result in skipped execution or an error.
   */
  @Parameter(defaultValue = "false")
  private boolean lenient;

  /**
   * YAML-formatted values to supply at the time of installation.
   *
   * If this parameter and the {@code valuesYamlUri} parameter are
   * both specified, this parameter is preferred if its value is
   * non-{@code null} and non-empty.
   */
  @Parameter(property = "helm.install.valuesYaml")
  private String valuesYaml;

  /**
   * A URI identifying a document containing YAML-formatted values to
   * supply at the time of installation.
   *
   * If this parameter and the {@code valuesYaml} parameter are both
   * specified, this parameter is ignored if the value of the {@code
   * valuesYaml} parameter is non-{@code null} and non-empty.
   */
  @Parameter(property = "helm.install.valuesYamlUri")
  private URI valuesYamlUri;
  
  /**
   * A {@link URL} representing the chart to install.  If omitted,
   * <code>file:/${project.build.directory}/generated-sources/helm/charts/${project.artifactId}</code>
   * will be used instead.
   */
  @Parameter(required = true,
             defaultValue = "file:${project.build.directory}/generated-sources/helm/charts/${project.artifactId}",
             property = "helm.install.chartUrl"
  )
  private URL chartUrl;

  
  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link InstallReleaseMojo}.
   *
   * @param project the {@link MavenProject} in effect; must not be
   * {@code null}
   *
   * @param session the {@link MavenSession} in effect; must not be
   * {@code null}
   *
   * @exception NullPointerException if either {@code project} or
   * {@code session} is {@code null}
   */
  @Inject
  public InstallReleaseMojo(final MavenProject project, final MavenSession session) {
    super();
    Objects.requireNonNull(project);
    Objects.requireNonNull(session);
    this.project = project;
    this.session = session;
  }


  /*
   * Instance methods.
   */


  /**
   * {@inheritDoc}
   *
   * <p>This implementation <a
   * href="https://github.com/kubernetes/helm/blob/master/docs/using_helm.md#helm-install-installing-a-package">installs</a>
   * the <a
   * href="https://docs.helm.sh/developing_charts/#charts">chart</a>
   * residing at the {@linkplain #getChartUrl() indicated URL} and
   * thus creates a <a
   * href="https://docs.helm.sh/glossary/#release">release</a>.</p>
   */
  @Override
  protected void execute(final Callable<ReleaseManager> releaseManagerCallable) throws Exception {
    Objects.requireNonNull(releaseManagerCallable);
    final Log log = this.getLog();
    assert log != null;

    URL chartUrl = this.getChartUrl();
    if (chartUrl == null) {
      final Path chartPath = Paths.get(new StringBuilder(this.project.getBuild().getDirectory()).append("/generated-sources/helm/charts/").append(this.project.getArtifactId()).toString());
      assert chartPath != null;
      chartUrl = chartPath.toUri().toURL();
      if (!Files.isDirectory(chartPath)) {
        if (this.isLenient()) {
          if (log.isWarnEnabled()) {
            log.warn("Non-existent or unresolvable default chartUrl (" + chartUrl + "); skipping execution");
          }
          chartUrl = null;
        } else {
          throw new MojoExecutionException("Non-existent or unresolvable default chartUrl: " + chartUrl);
        }
      }
    }
    
    if (chartUrl != null) {
      if (log.isDebugEnabled()) {
        log.debug("chartUrl: " + chartUrl);
      }
      
      Chart.Builder chartBuilder = null;
      try (final AbstractChartLoader<URL> chartLoader = this.createChartLoader()) {
        if (chartLoader == null) {
          throw new IllegalStateException("createChartLoader() == null");
        }
        if (log.isDebugEnabled()) {
          log.debug("chartLoader: " + chartLoader);
          log.debug("Loading Helm chart from " + chartUrl);
        }
        chartBuilder = chartLoader.load(chartUrl);
      }
      
      if (chartBuilder == null) {
        throw new IllegalStateException("chartBuilder.load(\"" + chartUrl + "\") == null");
      }
      
      if (log.isInfoEnabled()) {
        log.info("Loaded Helm chart from " + chartUrl);
      }
      
      final InstallReleaseRequest.Builder requestBuilder = InstallReleaseRequest.newBuilder();
      assert requestBuilder != null;
      
      requestBuilder.setDisableHooks(this.getDisableHooks());
      requestBuilder.setDryRun(this.getDryRun());
      
      final String releaseName = this.getReleaseName();
      if (releaseName != null) {
        requestBuilder.setName(releaseName);
      }
      
      final String releaseNamespace = this.getReleaseNamespace();
      if (releaseNamespace != null) {
        requestBuilder.setNamespace(releaseNamespace);
      }
      
      requestBuilder.setReuseName(this.getReuseReleaseName());
      requestBuilder.setTimeout(this.getTimeout());

      String valuesYaml = this.getValuesYaml();
      if (valuesYaml == null || valuesYaml.isEmpty()) {
        final URI valuesYamlUri = this.getValuesYamlUri();
        if (valuesYamlUri != null) {
          final URL yamlUrl = valuesYamlUri.toURL();
          assert yamlUrl != null;
          try (final Reader reader = new BufferedReader(new InputStreamReader(yamlUrl.openStream(), StandardCharsets.UTF_8))) {
            final StringBuilder sb = new StringBuilder();
            final char[] buffer = new char[4096];
            int charsRead = -1;
            while ((charsRead = reader.read(buffer, 0, buffer.length)) >= 0) {
              sb.append(buffer, 0, charsRead);
            }
            valuesYaml = sb.toString();
          }
        }
      }
      if (valuesYaml != null && !valuesYaml.isEmpty()) {
        final hapi.chart.ConfigOuterClass.Config.Builder values = requestBuilder.getValuesBuilder();
        assert values != null;
        values.setRaw(valuesYaml);
      }
      
      requestBuilder.setWait(this.getWait());
      
      final ReleaseManager releaseManager = releaseManagerCallable.call();
      if (releaseManager == null) {
        throw new IllegalStateException("releaseManagerCallable.call() == null");
      }
      
      if (log.isInfoEnabled()) {
        log.info("Installing release " + requestBuilder.getName());
      }
      final Future<InstallReleaseResponse> installReleaseResponseFuture = releaseManager.install(requestBuilder, chartBuilder);
      assert installReleaseResponseFuture != null;
      final InstallReleaseResponse installReleaseResponse = installReleaseResponseFuture.get();
      assert installReleaseResponse != null;
      if (log.isInfoEnabled()) {
        final Release release = installReleaseResponse.getRelease();
        assert release != null;
        log.info("Installed release " + release.getName());
      }
    }
  }

  /**
   * Creates and returns an {@link AbstractChartLoader} capable of
   * loading a Helm chart from a {@link URL}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * <p>This implementation returns a new {@link URLChartLoader}.</p>
   *
   * @return a new {@link AbstractChartLoader} implementation; never
   * {@code null}
   */
  protected AbstractChartLoader<URL> createChartLoader() {
    return new URLChartLoader();
  }

  /**
   * Returns a {@link URL} identifying a Helm chart that can be read
   * by the {@link AbstractChartLoader} produced by the {@link
   * #createChartLoader()} method.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return a {@link URL} to a Helm chart, or {@code null}
   *
   * @see #setChartUrl(URL)
   */
  public URL getChartUrl() {
    return this.chartUrl;
  }

  /**
   * Sets the {@link URL} identifying a Helm chart that can be read
   * by the {@link AbstractChartLoader} produced by the {@link
   * #createChartLoader()} method.
   *
   * @param chartUrl the {@link URL} identifying a Helm chart that can
   * be read by the {@link AbstractChartLoader} produced by the {@link
   * #createChartLoader()} method; may be {@code null}
   */
  public void setChartUrl(final URL chartUrl) {
    this.chartUrl = chartUrl;
  }

  /**
   * Returns {@code true} if this {@link InstallReleaseMojo} is
   * <em>lenient</em>; if {@code true}, a missing or unresolvable
   * {@link #getChartUrl() chartUrl} parameter will result in
   * execution being skipped rather than a {@link
   * MojoExecutionException} being thrown.
   *
   * @return {@code true} if this {@link InstallReleaseMojo} is
   * <em>lenient</em>; {@code false} otherwise
   */
  public boolean isLenient() {
    return this.lenient;
  }

  /**
   * Sets whether this {@link InstallReleaseMojo} is <em>lenient</em>;
   * if {@code true} is supplied, a missing or unresolvable {@link
   * #getChartUrl() chartUrl} parameter will result in execution being
   * skipped rather than a {@link MojoExecutionException} being
   * thrown.
   *
   * @param lenient whether this {@link InstallReleaseMojo} is
   * <em>lenient</em>; if {@code true}, a missing or unresolvable
   * {@link #getChartUrl() chartUrl} parameter will result in
   * execution being skipped rather than a {@link
   * MojoExecutionException} being thrown
   */
  public void setLenient(final boolean lenient) {
    this.lenient = lenient;
  }
  
  /**
   * Returns the <a
   * href="https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/">namespace</a>
   * into which the release will be installed.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @return the <a
   * href="https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/">namespace</a>
   * into which the release will be installed, or {@code null}
   *
   * @see #setReleaseNamespace(String)
   */
  public String getReleaseNamespace() {
    return this.releaseNamespace;
  }

  /**
   * Sets the <a
   * href="https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/">namespace</a>
   * into which the release will be installed.
   *
   * @param releaseNamespace the <a
   * href="https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/">namespace</a>
   * into which the release will be installed; may be {@code null}
   *
   * @see #getReleaseNamespace()
   */
  public void setReleaseNamespace(final String releaseNamespace) {
    this.releaseNamespace = releaseNamespace;
  }

  /**
   * Returns {@code true} if the {@linkplain #getReleaseName()
   * supplied release name} should be reused across installations.
   *
   * @return {@code true} if the {@linkplain #getReleaseName()
   * supplied release name} should be reused across installations;
   * {@code false} otherwise
   *
   * @see #setReuseReleaseName(boolean)
   */
  public boolean getReuseReleaseName() {
    return this.reuseReleaseName;
  }

  /**
   * Sets whether the {@linkplain #getReleaseName() supplied release
   * name} should be reused across installations.
   *
   * @param reuseReleaseName whether the {@linkplain #getReleaseName()
   * supplied release name} should be reused across installations
   *
   * @see #getReuseReleaseName()
   */
  public void setReuseReleaseName(final boolean reuseReleaseName) {
    this.reuseReleaseName = reuseReleaseName;
  }

  /**
   * Returns a YAML {@link String} representing the values to use to
   * customize the installation.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * @return a YAML {@link String} representing the values to use to
   * customize the installation, or {@code null}
   *
   * @see #setValuesYaml(String)
   */
  public String getValuesYaml() {
    return this.valuesYaml;
  }

  /**
   * Installs a YAML {@link String} representing the values to use to
   * customize the installation.
   *
   * @param valuesYaml the YAML {@link String} representing the values to use to
   * customize the installation; may be {@code null}
   *
   * @see #getValuesYaml()
   */
  public void setValuesYaml(final String valuesYaml) {
    this.valuesYaml = valuesYaml;
  }

  /**
   * Returns a {@link URI} identifying a YAML document containing the
   * values to use to customize the installation.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * @return a {@link URI} identifying a YAML document containing the
   * values to use to customize the installation, or {@code null}
   *
   * @see #setValuesYamlUri(URI)
   */
  public URI getValuesYamlUri() {
    return this.valuesYamlUri;
  }

  /**
   * Sets the {@link URI} identifying a YAML document containing the
   * values to use to customize the installation.
   *
   * @param valuesYamlUri the {@link URI} identifying a YAML document
   * containing the values to use to customize the installation; may
   * be {@code null}
   *
   * @see #getValuesYamlUri()
   */
  public void setValuesYamlUri(final URI valuesYamlUri) {
    this.valuesYamlUri = valuesYamlUri;
  }
  
  /**
   * {@inheritDoc}
   *
   * <p>This implementation allows the supplied {@code name} to be
   * {@code null} or {@linkplain String#isEmpty() empty}.</p>
   */
  @Override
  protected void validateReleaseName(final String name) {
    if (name != null && !name.isEmpty()) {
      super.validateReleaseName(name);
    }
  }
 
}
