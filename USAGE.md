# Usage and Examples

This document describes how to use the [helm-maven-plugin][] from
within your [Maven][] build.

## Overview

The [helm-maven-plugin][] project makes working with [Helm][] possible
from inside [Maven][] by supplying [Maven][] plugins that make use of
the [microbean-helm][] project.

The project consists of several [mojos][mojo] that are fully documented
in the [Goals][goals] document.

## Recipes

The following are recipes to follow to accomplish
common [Helm][]-oriented tasks as part of your [Maven][] build.

### Packaging a Helm Chart

Let's say you have the contents of a Helm chart you want to package
up.  Let's further assume you have them in
`src/helm/charts/your-project`, a directory in a Maven project whose
`artifactId` is `your-project`.  That directory might look like this:

    Chart.yaml
    values.yaml
    templates/
    .helmignore
    requirements.yaml
    charts/
    
&hellip;or might contain fewer or more files in accordance
with [what a Helm chart needs to look like][chart-file-structure].
Let's also assume that you're going to package this chart up with an
intent to do something else with it (like install it) later, but you
haven't gotten that far yet.

You'll want to archive this directory as a `.tar.gz` file as part of
the build, and place the resulting packaged chart somewhere you can
make use of it later.  For this simplest of all cases, you could do
this with the [maven-assembly-plugin][], but we'll do it with the
[helm-maven-plugin][] as part of
the [`generate-sources` lifecycle phase][lifecycles] as follows:

    <plugin>
      <groupId>org.microbean</groupId>
      <artifactId>helm-maven-plugin</artifactId>
      <version>2.8.2.1.0.4</version>
      <executions>
        <execution>
          <id>Package your-project chart</id>
          <phase>generate-sources</phase> <!-- pick a suitable phase, though this seems good -->
          <goals>
            <goal>package</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
    
This example uses a minimal amount of configuration.  It takes
advantage of the fact that by default the [`package` goal][package]
will [read from your `src/helm/charts/${project.artifactId}` directory][package-chartcontentsuri]
and will [write a file named
`${project.build.directory}/generated-sources/helm/charts/${project.artifactId}.tgz`][package-charttargeturi].

Here is the same stanza, explicitly configured this time:

    <plugin>
      <groupId>org.microbean</groupId>
      <artifactId>helm-maven-plugin</artifactId>
      <version>2.8.2.1.0.4</version>
      <executions>
        <execution>
          <id>Package your-project chart</id>
          <phase>generate-sources</phase> <!-- pick a suitable phase, though this seems good -->
          <goals>
            <goal>package</goal>
          </goals>
          <configuration>
            <chartContentsUri>file:${project.basedir}/src/helm/charts/${project.artifactId}</chartContentsUri>
            <chartTargetUri>file:${project.build.directory}/generated-sources/helm/charts/${project.artifactId}.tgz</chartTargetUri>
          </configuration>
        </execution>
      </executions>
    </plugin>
    
Note that the [`chartContentsUri`][package-chartcontentsuri]
and [`chartTargetUri`][package-charttargeturi] configuration elements
are actually URIs.  This permits writing a chart archive to an FTP or
HTTP server, for example.

## Installing a Chart to Create a Release

Once you have a [Helm][] [chart][chart-file-structure] in packaged or
unpackaged form, you need to read it in and send it to [Tiller][],
thus creating a [_release_][release].

To do this, you use
the [`install` goal][install].  [`install`][install] needs to know
where your chart is, and needs to know how to connect to
your [Kubernetes cluster][kubernetes-cluster].  The configuration
required below is to accomplish those goals.

Here is a minimally configured stanza that
will
[take a chart from `file:${project.build.directory}/generated-sources/helm/charts/${project.artifactId}`][install-charturl] (note
that the default is a directory, not a file, i.e. created perhaps by
way of [Maven resources][maven-resources] or some other mechanism, and
will [connect to the cluster][install-clientconfiguration]
using
[all the mechanisms available to the `DefaultKubernetesClient`][kubernetes-client-config] class:

    <plugin>
      <groupId>org.microbean</groupId>
      <artifactId>helm-maven-plugin</artifactId>
      <version>2.8.2.1.0.4</version>
      <executions>
        <execution>
          <id>Install your-project chart</id>
          <phase>deploy</phase> <!-- pick a suitable phase -->
          <goals>
            <goal>install</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
    
This example uses a minimal amount of configuration.  It takes
advantage of the fact
that
[by default the `install` goal will read from your `${project.build.directory}/generated-sources/helm/charts/${project.artifactId}` directory][install-charturl],
if it exists, and
will [connect to your Kubernetes cluster][install-clientconfiguration]
using
[all the mechanisms available to the `DefaultKubernetesClient`][kubernetes-client-config] class
(including a `~/.kube/config` file if you have one).  Since no
[explicit release name][install-releasename] is specified, the release will be named
by [Tiller][] according to the rules embodied by
this [Github project][moniker].

Here is the same stanza, this time fully configured, and specifying
a [release name][install-releasename],
and [specifying a value][install-valuesyaml] for your made-up chart's
hypothetical `frobnicationInterval` setting:

    <plugin>
      <groupId>org.microbean</groupId>
      <artifactId>helm-maven-plugin</artifactId>
      <version>2.8.2.1.0.4</version>
      <executions>
        <execution>
          <id>Install your-project chart</id>
          <phase>deploy</phase> <!-- pick a suitable phase -->
          <goals>
            <goal>install</goal>
          </goals>
          <configuration>
            <chartUrl>file:${project.build.directory}/generated-sources/helm/charts/${project.artifactId}</chartUrl>
            <releaseName>your-first-release</releaseName>
            <valuesYaml><[!CDATA[frobnicationInterval: 37]]></valuesYaml>
          </configuration>
        </execution>
      </executions>
    </plugin>

Note in particular the [`<valuesYaml>` setting][install-valuesyaml]; `CDATA` will help you
here since most non-trivial YAML requires strict indentation.

In the `<configuration>` element, you could, if you wanted, set up a
custom [`Config`][kubernetes-client-config] object, by specifying
a [`<clientConfiguration>` child element][install-clientconfiguration]
and following
the [rules of Maven plugin instantiation][maven-plugin-configuration].

### Listing the Contents of a Release

This example shows you how to make use of a release that has already
been installed into your Kubernetes cluster.  The key thing to look at
here is the [listeners-oriented stanza][content-releasecontentlisteners], which shows how you can
customize the basic behavior.  The behavior here is of the
log-to-`STDOUT` variety, but if you supplied your own listener you
could do more interesting things:

    <plugin>
      <groupId>org.microbean</groupId>
      <artifactId>helm-maven-plugin</artifactId>
      <version>2.8.2.1.0.4</version>
      <executions>
        <execution>
          <id>List the contents of your-first-release</id>
          <phase>test</phase> <!-- pick a suitable phase -->
          <goals>
            <goal>content</goal>
          </goals>
          <configuration>
            <releaseName>your-first-release</releaseName>
            <releaseContentListeners>
              <abstractReleaseContentListener/>
            </releaseContentListeners>
          </configuration>
        </execution>
      </executions>
    </plugin>

Note the `<abstractReleaseContentListener/>` element,
which
[follows the rules of Maven plugin configuration][maven-plugin-configuration],
which allows for customizable behavior.  Here, the
`org.microbean.helm.maven.AbstractReleaseContentListener` class is
instantiated.

### Uninstalling a Release

Building on what we've learned, here's a stanza that [uninstalls a
release using the `uninstall` goal][uninstall]:

    <plugin>
      <groupId>org.microbean</groupId>
      <artifactId>helm-maven-plugin</artifactId>
      <version>2.8.2.1.0.4</version>
      <executions>
        <execution>
          <id>Uninstall your-first-release</id>
          <phase>test</phase> <!-- pick a suitable phase -->
          <goals>
            <goal>uninstall</goal>
          </goals>
          <configuration>
            <purge>true</purge>
            <releaseName>your-first-release</releaseName>
          </configuration>
        </execution>
      </executions>
    </plugin>

[uninstall]: https://microbean.github.io/helm-maven-plugin/uninstall-mojo.html
[content-releasecontentlisteners]: https://microbean.github.io/helm-maven-plugin/content-mojo.html#releaseContentListeners
[install-clientconfiguration]: https://microbean.github.io/helm-maven-plugin/install-mojo.html#clientConfiguration
[install]: https://microbean.github.io/helm-maven-plugin/install-mojo.html
[install-charturl]: https://microbean.github.io/helm-maven-plugin/install-mojo.html#chartUrl
[install-releasename]: https://microbean.github.io/helm-maven-plugin/install-mojo.html#releaseName
[install-valuesyaml]: https://microbean.github.io/helm-maven-plugin/install-mojo.html#valuesYaml
[package]: https://microbean.github.io/helm-maven-plugin/package-mojo.html
[package-chartcontentsuri]: https://microbean.github.io/helm-maven-plugin/package-mojo.html#chartContentsUri
[package-charttargeturi]: https://microbean.github.io/helm-maven-plugin/package-mojo.html#chartTargetUri
[helm-maven-plugin]: https://microbean.github.io/helm-maven-plugin/
[microbean-helm]: https://microbean.github.io/microbean-helm/
[maven]: http://maven.apache.org/
[mojo]: http://maven.apache.org/plugin-developers/index.html
[goals]: https://microbean.github.io/helm-maven-plugin/plugin-info.html
[helm]: https://docs.helm.sh/
[chart-file-structure]: https://docs.helm.sh/developing_charts/#the-chart-file-structure
[maven-assembly-plugin]: http://maven.apache.org/plugins/maven-assembly-plugin/index.html
[lifecycles]: https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#Lifecycle_Reference
[tiller]: https://docs.helm.sh/glossary/#tiller
[release]: https://docs.helm.sh/glossary/#release
[kubernetes-cluster]: https://kubernetes.io/docs/setup/
[kubernetes-client-config]: https://github.com/fabric8io/kubernetes-client/blob/v3.0.0/kubernetes-client/src/main/java/io/fabric8/kubernetes/client/Config.java#L231-L321
[moniker]: https://github.com/technosophos/moniker#monicker-generate-cute-random-names
[maven-plugin-configuration]: https://maven.apache.org/guides/mini/guide-configuring-plugins.html#Configuring_Parameters
