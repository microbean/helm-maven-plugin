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

import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Objects;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.inject.Inject;

import hapi.chart.ChartOuterClass.Chart;

import hapi.release.ReleaseOuterClass.Release;

import hapi.services.tiller.Tiller.UpdateReleaseRequest;
import hapi.services.tiller.Tiller.UpdateReleaseResponse;

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
 * href="https://docs.helm.sh/using_helm/#helm-upgrade-and-helm-rollback-upgrading-a-release-and-recovering-on-failure">Updates
 * a release</a> with a new chart.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@Mojo(name = "update")
public class UpdateReleaseMojo extends AbstractForceableMutatingReleaseMojo {


  /*
   * Instance fields.
   */


  /**
   * The {@link MavenProject} in effect.
   *
   * <p>This field is never {@code null}.</p>
   *
   * @see #UpdateReleaseMojo(MavenProject, MavenSession)
   */
  private final MavenProject project;

  /**
   * The {@link MavenSession} in effect.
   *
   * <p>This field is never {@code null}.</p>
   *
   * @see #UpdateReleaseMojo(MavenProject, MavenSession)
   */
  private final MavenSession session;

  /**
   * A {@link URL} to the new chart to update the release with.  If
   * omitted,
   * <code>file:/${project.build.directory}/generated-sources/helm/charts/${project.artifactId}</code>
   * will be used instead.
   */
  @Parameter
  private URL chartUrl;

  /**
   * Whether values should be reset to the values built in to the
   * chart.  Ignored if {@codde reuseValues} is {@code true}.
   */
  @Parameter(defaultValue = "false")
  private boolean resetValues;

  /**
   * Whether values should be reused from the prior release, merged
   * together with any additional values specified in the {@code
   * valuesYaml} parameter.  Ignored if {@code resetValues} is {@code
   * true}.
   */
  @Parameter
  private boolean reuseValues;

  /**
   * New values in YAML format to use when updating the release.  May
   * be combined with the effects of the {@code resetValues} and
   * {@code reuseValues} parameters.
   */
  @Parameter
  private String valuesYaml;
  
  
  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link UpdateReleaseMojo}.
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
  public UpdateReleaseMojo(final MavenProject project, final MavenSession session) {
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
   * href="https://docs.helm.sh/using_helm/#helm-upgrade-and-helm-rollback-upgrading-a-release-and-recovering-on-failure">updates</a>
   * the release named by the {@linkplain #getReleaseName() supplied
   * release name} with a new <a
   * href="https://docs.helm.sh/developing_charts/#charts">chart</a>
   * residing at the {@linkplain #getChartUrl() indicated URL}.</p>
   */
  @Override
  protected void execute(final Callable<ReleaseManager> releaseManagerCallable) throws Exception {
    Objects.requireNonNull(releaseManagerCallable);
    final Log log = this.getLog();
    assert log != null;

    URL chartUrl = this.getChartUrl();
    if (chartUrl == null) {
      final Path chartDirectoryPath = Paths.get(new StringBuilder(this.project.getBuild().getDirectory()).append("/generated-sources/helm/charts/").append(this.project.getArtifactId()).toString());
      assert chartDirectoryPath != null;
      chartUrl = chartDirectoryPath.toUri().toURL();
      if (!Files.isDirectory(chartDirectoryPath)) {
        throw new MojoExecutionException("Non-existent chartUrl: " + chartUrl);
      }
    }
    assert chartUrl != null;
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

    final UpdateReleaseRequest.Builder requestBuilder = UpdateReleaseRequest.newBuilder();
    assert requestBuilder != null;

    requestBuilder.setDisableHooks(this.getDisableHooks());
    requestBuilder.setDryRun(this.getDryRun());
    requestBuilder.setForce(this.getForce());
    requestBuilder.setRecreate(this.getRecreate());

    final String releaseName = this.getReleaseName();
    if (releaseName != null) {
      requestBuilder.setName(releaseName);
    }

    requestBuilder.setResetValues(this.getResetValues());
    requestBuilder.setReuseValues(this.getReuseValues());
    requestBuilder.setTimeout(this.getTimeout());

    final String valuesYaml = this.getValuesYaml();
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
      log.info("Updating release " + requestBuilder.getName());
    }
    final Future<UpdateReleaseResponse> updateReleaseResponseFuture = releaseManager.update(requestBuilder, chartBuilder);
    assert updateReleaseResponseFuture != null;
    final UpdateReleaseResponse updateReleaseResponse = updateReleaseResponseFuture.get();
    assert updateReleaseResponse != null;
    if (log.isInfoEnabled()) {
      final Release release = updateReleaseResponse.getRelease();
      assert release != null;
      log.info("Updated release " + release.getName());
    }
    
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
   * Returns {@code true} if, during the update, values should be
   * reset to the values built in to the {@linkplain #getChartUrl()
   * new chart}.
   *
   * @return {@code true} if, during the update, values should be
   * reset to the values built in to the {@linkplain #getChartUrl()
   * new chart}; {@code false} otherwise
   *
   * @see #setResetValues(boolean)
   */
  public boolean getResetValues() {
    return this.resetValues;
  }

  /**
   * Sets whether, during the update, values should be
   * reset to the values built in to the {@linkplain #getChartUrl()
   * new chart}.
   *
   * @param resetValues if {@code true}, during the update values will
   * be reset to the values built in to the {@linkplain #getChartUrl()
   * new chart}
   *
   * @see #getResetValues()
   */
  public void setResetValues(final boolean resetValues) {
    this.resetValues = resetValues;
  }

  /**
   * Returns {@code true} if, during the update, any new values should
   * be merged with those present in the prior version of the release
   * being updated.
   *
   * @return {@code true} if, during the update, any new values should
   * be merged with those present in the prior version of the release
   * being updated; {@code false} otherwise
   *
   * @see #setReuseValues(boolean)
   */
  public boolean getReuseValues() {
    return this.reuseValues;
  }

  /**
   * Sets whether, during the update, any new values should
   * be merged with those present in the prior version of the release
   * being updated.
   *
   * @param reuseValues if {@code true} during the update any new
   * values will be merged with those present in the prior version of
   * the release being updated
   *
   * @see #getReuseValues()
   */
  public void setReuseValues(final boolean reuseValues) {
    this.reuseValues = reuseValues;
  }

  /**
   * Returns a YAML {@link String} representing the values to use to
   * customize the update.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * @return a YAML {@link String} representing the values to use to
   * customize the update, or {@code null}
   *
   * @see #setValuesYaml(String)
   */
  public String getValuesYaml() {
    return this.valuesYaml;
  }

  /**
   * Installs a YAML {@link String} representing the values to use to
   * customize the update.
   *
   * @param valuesYaml the YAML {@link String} representing the values to use to
   * customize the update; may be {@code null}
   *
   * @see #getValuesYaml()
   */
  public void setValuesYaml(final String valuesYaml) {
    this.valuesYaml = valuesYaml;
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

}
