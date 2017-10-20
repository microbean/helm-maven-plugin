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

@Mojo(name = "install")
public class InstallReleaseMojo extends AbstractMutatingReleaseMojo {

  private final MavenProject project;

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
  @Parameter
  private String releaseName;
  
  @Parameter
  private String releaseNamespace;

  @Parameter
  private boolean reuseReleaseName;

  @Parameter
  private String valuesYaml;

  @Parameter
  private URL chartUrl;
  
  @Inject
  public InstallReleaseMojo(final MavenProject project, final MavenSession session) {
    super();
    Objects.requireNonNull(project);
    Objects.requireNonNull(session);
    this.project = project;
    this.session = session;
  }

  @Override
  protected void execute(final Callable<ReleaseManager> releaseManagerCallable) throws Exception {
    Objects.requireNonNull(releaseManagerCallable);
    final Log log = this.getLog();
    assert log != null;

    URL chartUrl = this.getChartUrl();
    if (chartUrl == null) {
      final Path chartDirectoryPath = Paths.get(new StringBuilder(this.project.getBuild().getDirectory()).append("/helm/").append(this.project.getArtifactId()).toString());
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

  protected AbstractChartLoader<URL> createChartLoader() {
    return new URLChartLoader();
  }

  public URL getChartUrl() {
    return this.chartUrl;
  }

  public void setChartUrl(final URL chartUrl) {
    this.chartUrl = chartUrl;
  }
  
  public String getReleaseNamespace() {
    return this.releaseNamespace;
  }

  public void setReleaseNamespace(final String releaseNamespace) {
    this.releaseNamespace = releaseNamespace;
  }

  public boolean getReuseReleaseName() {
    return this.reuseReleaseName;
  }

  public void setReuseReleaseName(final boolean reuseReleaseName) {
    this.reuseReleaseName = reuseReleaseName;
  }

  public String getValuesYaml() {
    return this.valuesYaml;
  }

  public void setValuesYaml(final String valuesYaml) {
    this.valuesYaml = valuesYaml;
  }

  @Override
  protected void validateReleaseName(final String name) {
    if (name != null && !name.isEmpty()) {
      super.validateReleaseName(name);
    }
  }
 
}
