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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.UnknownServiceException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Objects;

import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;

import hapi.chart.ChartOuterClass.Chart;
import hapi.chart.MetadataOuterClass.MetadataOrBuilder;

import org.apache.maven.model.Build;

import org.apache.maven.project.MavenProject;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.microbean.helm.chart.AbstractChartLoader;
import org.microbean.helm.chart.AbstractChartWriter;
import org.microbean.helm.chart.URLChartLoader;
import org.microbean.helm.chart.TapeArchiveChartWriter;

@Mojo(name = "package")
public class PackageMojo extends AbstractHelmMojo {


  /*
   * Instance fields.
   */
  

  private final MavenProject project;
  
  @Parameter(defaultValue = "false")
  private boolean skip;
  
  @Parameter(required = true)
  private URI chartContentsUri;
  
  @Parameter
  private AbstractChartLoader<URL> chartLoader;

  @Parameter
  private AbstractChartWriter chartWriter;

  @Parameter
  private URI chartTargetUri;


  /*
   * Constructors.
   */


  @Inject
  public PackageMojo(final MavenProject project) {
    super();
    this.project = project;
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

    final URI chartContentsUri = this.getChartContentsUri();
    if (chartContentsUri == null) {
      throw new IllegalStateException("getChartContentsUri() == null");
    }
    URL chartContentsUrl = null;
    try {
      chartContentsUrl = chartContentsUri.toURL();
    } catch (final IOException ioException) {
      throw new MojoExecutionException(ioException.getMessage(), ioException);
    }
    assert chartContentsUrl != null;

    AbstractChartLoader<URL> chartLoader = this.getChartLoader();
    if (chartLoader == null) {
      chartLoader = new URLChartLoader();
    }

    Throwable throwable = null;

    Chart.Builder chart = null;
    try {
      chart = chartLoader.load(chartContentsUrl);
    } catch (final RuntimeException runtimeException) {
      throwable = runtimeException;
      throw runtimeException;
    } catch (final IOException exception) {
      final MojoExecutionException e = new MojoExecutionException(exception.getMessage(), exception);
      throwable = e;
      throw e;
    } finally {
      try {
        chartLoader.close();
      } catch (final IOException suppressMe) {
        if (throwable == null) {
          throw new MojoExecutionException(suppressMe.getMessage(), suppressMe);
        } else {
          throwable.addSuppressed(suppressMe);
        }
      }
    }
    throwable = null;

    final MetadataOrBuilder metadata = chart.getMetadata();
    if (metadata == null) {
      throw new IllegalStateException("chart.getMetadata() == null");
    }
    final String chartName = metadata.getName();
    if (chartName == null) {
      throw new IllegalStateException("metadata.getName() == null");
    } else if (chartName.isEmpty()) {
      throw new IllegalStateException("metadata.getName().isEmpty()");
    }

    AbstractChartWriter chartWriter = this.getChartWriter();
    if (chartWriter == null) {
      URI chartTargetUri = this.getChartTargetUri();
      if (chartTargetUri == null) {
        final Build build = this.project.getBuild();
        assert build != null;
        final String targetDirectory = build.getDirectory();
        assert targetDirectory != null;
        final Path targetDirectoryPath = Paths.get(targetDirectory);
        assert targetDirectoryPath != null;
        final Path helmChartsDirectoryPath = targetDirectoryPath.resolve("generated-sources/helm/charts");
        assert helmChartsDirectoryPath != null;
        assert helmChartsDirectoryPath.isAbsolute();
        final Path chartDirectoryPath = helmChartsDirectoryPath.resolve(chartName + ".tgz");
        assert chartDirectoryPath != null;
        chartTargetUri = chartDirectoryPath.toUri();
      }
      assert chartTargetUri != null;
      if ("file".equals(chartTargetUri.getScheme())) {
        final String chartTargetUriPath = chartTargetUri.getPath();
        if (chartTargetUriPath != null) {
          final Path chartTargetPath = Paths.get(chartTargetUriPath).normalize();
          assert chartTargetPath != null;
          final Path parent = chartTargetPath.getParent();
          if (parent != null) {
            try {
              Files.createDirectories(parent);
            } catch (final IOException ioException) {
              throw new MojoExecutionException(ioException.getMessage(), ioException);
            }
          }
        }
      }
      URL chartTargetURL = null;
      try {
        chartTargetURL = chartTargetUri.toURL();
      } catch (final IOException ioException) {
        throw new MojoExecutionException(ioException.getMessage(), ioException);
      }
      assert chartTargetURL != null;
      URLConnection connection = null;
      try {
        connection = chartTargetURL.openConnection();
      } catch (final IOException ioException) {
        throw new MojoExecutionException(ioException.getMessage(), ioException);
      }      
      assert connection != null;
      connection.setDoOutput(true);
      OutputStream outputStream = null;
      try {
        outputStream = connection.getOutputStream();
      } catch (final UnknownServiceException outputNotSupported) {
        // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4191800
        if ("file".equals(chartTargetURL.getProtocol())) {
          final Path chartTargetPath = Paths.get(chartTargetURL.getPath());
          assert chartTargetPath != null;
          try {
            outputStream = Files.newOutputStream(chartTargetPath);
          } catch (final IOException ioException) {
            ioException.addSuppressed(outputNotSupported);
            throw new MojoExecutionException(ioException.getMessage(), ioException);
          }
        } else {
          throw new MojoExecutionException(outputNotSupported.getMessage(), outputNotSupported);
        }
      } catch (final IOException ioException) {
        throw new MojoExecutionException(ioException.getMessage(), ioException);
      }
      try {
        chartWriter = new TapeArchiveChartWriter(new BufferedOutputStream(new GZIPOutputStream(outputStream)));
      } catch (final IOException ioException) {
        throw new MojoExecutionException(ioException.getMessage(), ioException);
      }
    }
    assert chartWriter != null;

    throwable = null;
    try {
      chartWriter.write(chart);
    } catch (final RuntimeException runtimeException) {
      throwable = runtimeException;
      throw runtimeException;
    } catch (final IOException ioException) {
      final MojoExecutionException e = new MojoExecutionException(ioException.getMessage(), ioException);
      throwable = e;
      throw e;
    } finally {
      try {
        chartWriter.close();
      } catch (final IOException suppressMe) {
        if (throwable == null) {
          throw new MojoExecutionException(suppressMe.getMessage(), suppressMe);
        } else {
          throwable.addSuppressed(suppressMe);
        }
      }
    }
    
  }

  public boolean getSkip() {
    return this.skip;
  }

  public void setSkip(final boolean skip) {
    this.skip = skip;
  }

  public AbstractChartLoader<URL> getChartLoader() {
    return this.chartLoader;
  }

  public void setChartLoader(final AbstractChartLoader<URL> chartLoader) {
    this.chartLoader = chartLoader;
  }

  public AbstractChartWriter getChartWriter() {
    return this.chartWriter;
  }

  public void setChartWriter(final AbstractChartWriter chartWriter) {
    this.chartWriter = chartWriter;
  }

  public URI getChartContentsUri() {
    return this.chartContentsUri;
  }

  public void setChartContentsUri(final URI chartContentsUri) {
    this.chartContentsUri = chartContentsUri;
  }

  public URI getChartTargetUri() {
    return this.chartTargetUri;
  }

  public void setChartTargetUri(final URI chartTargetUri) {
    this.chartTargetUri = chartTargetUri;
  }

  
}
