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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import hapi.release.ReleaseOuterClass.Release;
import hapi.release.StatusOuterClass.Status;

import hapi.services.tiller.Tiller.ListReleasesRequest;
import hapi.services.tiller.Tiller.ListReleasesResponse;
import hapi.services.tiller.Tiller.ListSort.SortBy;
import hapi.services.tiller.Tiller.ListSort.SortOrder;

import org.apache.maven.plugin.logging.Log;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.microbean.helm.ReleaseManager;

/**
 * Retrieves releases matching certain criteria and notifies
 * registered listeners.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 */
@Mojo(name = "list")
public class ListReleasesMojo extends AbstractReleaseMojo {


  /*
   * Instance fields.
   */


  /**
   * The regular expression that will be applied to the list of
   * releases.  Only releases that match the filter will be considered
   * for listing.
   */
  @Parameter
  private String filter;

  /**
   * The maximum number of releases to retrieve.
   */
  @Parameter(defaultValue = "256")
  private long limit;

  /**
   * The namespace from which releases will be listed.
   */
  @Parameter
  private String namespace;

  /**
   * The next release name in the list; used to offset from the start
   * value.
   */
  @Parameter
  private String offset;

  /**
   * A <a
   * href="https://microbean.github.io/microbean-helm/apidocs/hapi/services/tiller/Tiller.ListSort.SortBy.html">{@code
   * SortBy}</a> indicating how the results should be sorted.  Useful
   * values are <a
   * href="https://microbean.github.io/microbean-helm/apidocs/hapi/services/tiller/Tiller.ListSort.SortBy.html#NAME">{@code
   * NAME}</a> and <a
   * href="https://microbean.github.io/microbean-helm/apidocs/hapi/services/tiller/Tiller.ListSort.SortBy.html#LAST_RELEASED">{@code
   * LAST_RELEASED}</a>.
   */
  @Parameter(defaultValue = "NAME")
  private SortBy sortBy;

  /**
   * A <a
   * href="https://microbean.github.io/microbean-helm/apidocs/hapi/services/tiller/Tiller.ListSort.SortOrder.html">{@code
   * SortOrder}</a> indicating whether results should appear sorted in
   * ascending or descending order.
   */
  @Parameter
  private SortOrder sortOrder;

  /**
   * A {@link List} of <a
   * href="https://microbean.github.io/microbean-helm/apidocs/hapi/release/StatusOuterClass.Status.Code.html">{@code
   * StatusOuterClass.Status.Code}</a>s.  Releases must have one of
   * these status codes to be included in the list created by this
   * goal.
   */
  @Parameter
  private List<Status.Code> statusCodes;

  /**
   * A {@link List} of <a
   * href="apidocs/org/microbean/helm/maven/ReleaseDiscoveryListener.html">{@code
   * ReleaseDiscoveryListener}</a>s whose elements will be notified of
   * each release in the list created by this goal.
   */
  @Parameter(alias = "releaseDiscoveryListenersList")
  private List<ReleaseDiscoveryListener> releaseDiscoveryListeners;
  

  /*
   * Constructors.
   */
  

  /**
   * Creates a new {@link ListReleasesMojo}.
   */
  public ListReleasesMojo() {
    super();
  }


  /*
   * Protected instance methods.
   */
  

  /**
   * {@inheritDoc}
   *
   * <p>This implementation retrieves information about releases and
   * {@linkplain
   * ReleaseDiscoveryListener#releaseDiscovered(ReleaseDiscoveryEvent)
   * notifies} {@linkplain #getReleaseDiscoveryListenersList()
   * registered <code>ReleaseDiscoveryListener</code>s}.</p>
   */
  @Override
  protected void execute(final Callable<ReleaseManager> releaseManagerCallable) throws Exception {
    Objects.requireNonNull(releaseManagerCallable);
    final Log log = this.getLog();
    assert log != null;

    final Collection<? extends ReleaseDiscoveryListener> listeners = this.getReleaseDiscoveryListenersList();
    if (listeners == null || listeners.isEmpty()) {
      if (log.isInfoEnabled()) {
        log.info("Skipping execution because there are no ReleaseDiscoveryListeners specified.");
      }
      return;
    }

    final ListReleasesRequest.Builder requestBuilder = ListReleasesRequest.newBuilder();
    assert requestBuilder != null;

    final String filter = this.getFilter();
    if (filter != null) {
      requestBuilder.setFilter(filter);
    }

    requestBuilder.setLimit(this.getLimit());

    String namespace = this.getNamespace();
    if (namespace == null || namespace.isEmpty()) {
      final io.fabric8.kubernetes.client.Config configuration = this.getClientConfiguration();
      if (configuration == null) {
        namespace = "default";
      } else {
        namespace = configuration.getNamespace();
        if (namespace == null || namespace.isEmpty()) {
          namespace = "default";
        }
      }
    }
    this.validateNamespace(namespace);
    requestBuilder.setNamespace(namespace);

    final String offset = this.getOffset();
    if (offset != null) {
      requestBuilder.setOffset(offset);
    }

    final SortBy sortBy = this.getSortBy();
    if (sortBy != null) {
      requestBuilder.setSortBy(sortBy);
    }

    final SortOrder sortOrder = this.getSortOrder();
    if (sortOrder != null) {
      requestBuilder.setSortOrder(sortOrder);
    }

    final Iterable<Status.Code> statusCodes = this.getStatusCodes();
    if (statusCodes != null) {
      requestBuilder.addAllStatusCodes(statusCodes);
    }

    final ReleaseManager releaseManager = releaseManagerCallable.call();
    if (releaseManager == null) {
      throw new IllegalStateException("releaseManagerCallable.call() == null");
    }

    if (log.isInfoEnabled()) {
      log.info("Listing releases in namespace " + namespace);
    }
    
    final Iterator<? extends ListReleasesResponse> listReleasesResponseIterator = releaseManager.list(requestBuilder.build());
    assert listReleasesResponseIterator != null;
    while (listReleasesResponseIterator.hasNext()) {
      final ListReleasesResponse response = listReleasesResponseIterator.next();
      assert response != null;
      final ReleaseDiscoveryEvent event = new ReleaseDiscoveryEvent(this, response);
      for (final ReleaseDiscoveryListener listener : listeners) {
        if (listener != null) {
          listener.releaseDiscovered(event);
        }
      }
    }
    
  }


  /*
   * Public instance methods.
   */

  
  /**
   * Returns the regular expression by which releases will be
   * filtered.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * @return the regular expression by which releases will be
   * filtered, or {@code null}
   *
   * @see #setFilter(String)
   */
  public String getFilter() {
    return this.filter;
  }

  /**
   * Sets the regular expression by which releases will be filtered.
   *
   * @param filter the regular expression by which releases will be
   * filtered; may be {@code null}
   *
   * @see #getFilter()
   */
  public void setFilter(final String filter) {
    this.filter = filter;
  }

  /**
   * Returns the maximum number of releases to retrieve.
   *
   * @return the maximum number of releases to retrieve
   *
   * @see #setLimit(long)
   */
  public long getLimit() {
    return this.limit;
  }

  /**
   * Sets the maximum number of releases to retrieve.
   *
   * @param limit the maximum number of releases to retrieve
   *
   * @see #getLimit()
   */
  public void setLimit(final long limit) {
    this.limit = limit;
  }

  /**
   * Returns the <a
   * href="https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/">namespace</a>
   * to which releases that are retrieved must belong.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method are permitted to return {@code
   * null}.</p>
   *
   * @return the <a
   * href="https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/">namespace</a>
   * to which releases that are retrieved must belong, or {@code null}
   *
   * @see #setNamespace(String)
   */
  public String getNamespace() {
    return this.namespace;
  }

  /**
   * Sets the <a
   * href="https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/">namespace</a>
   * to which releases that are retrieved must belong.
   *
   * @param namespace the <a
   * href="https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/">namespace</a>
   * to which releases that are retrieved must belong; may be {@code
   * null}
   *
   * @see #getNamespace()
   */
  public void setNamespace(final String namespace) {
    this.validateNamespace(namespace);
    this.namespace = namespace;
  }

  /**
   * Returns the next release name from which listing should begin.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method are permitted to return {@code
   * null}.</p>
   *
   * @return the next release name from which listing should begin, or
   * {@code null}
   *
   * @see #setOffset(String)x
   */
  public String getOffset() {
    return this.offset;
  }

  /**
   * Sets the next release name from which listing should begin.
   *
   * @param offset the next release name from which listing should
   * begin; may be {@code null}
   *
   * @see #getOffset()
   */
  public void setOffset(final String offset) {
    this.offset = offset;
  }

  /**
   * Returns a {@link SortBy} indicating how the result list should be
   * sorted.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * @return a {@link SortBy} indicating how the result list should be
   * sorted, or {@code null}
   *
   * @see #setSortBy(Tiller.ListSort.SortBy)
   */
  public SortBy getSortBy() {
    return this.sortBy;
  }

  /**
   * Sets the {@link SortBy} indicating how the result list should be
   * sorted.
   *
   * @param sortBy the {@link SortBy} indicating how the result list
   * should be sorted; may be {@code null}
   *
   * @see #getSortBy()
   */
  public void setSortBy(final SortBy sortBy) {
    this.sortBy = sortBy;
  }

  /**
   * Returns the {@link SortOrder} describing the order of the sorted
   * result list.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method are permitted to return {@code null}.</p>
   *
   * @return the {@link SortOrder} describing the order of the sorted
   * result list, or {@code null}
   *
   * @see #setSortOrder(Tiller.ListSort.SortOrder)
   */
  public SortOrder getSortOrder() {
    return this.sortOrder;
  }

  /**
   * Sets the {@link SortOrder} describing the order of the sorted
   * result list.
   *
   * @param sortOrder the {@link SortOrder} describing the order of
   * the sorted result list; may be {@code null}
   *
   * @see #getSortOrder()
   */
  public void setSortOrder(final SortOrder sortOrder) {
    this.sortOrder = sortOrder;
  }

  /**
   * Returns the {@link List} of {@link Status.Code} instances that
   * describes the possible status codes a release must have in order
   * to be considered for further listing.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method are permitted to return {@code null}.</p>
   *
   * @return a {@link List} of {@link Status.Code} instances that
   * describes the possible status codes a release must have in order
   * to be considered for further listing, or {@code null}
   *
   * @see #setStatusCodes(List)
   */
  public List<Status.Code> getStatusCodes() {
    return this.statusCodes;
  }

  /**
   * Sets the {@link List} of {@link Status.Code} instances that
   * describes the possible status codes a release must have in order
   * to be considered for further listing.
   *
   * @param statusCodes a {@link List} of {@link Status.Code}
   * instances that describes the possible status codes a release must
   * have in order to be considered for further listing; may be {@code
   * null}
   *
   * @see #getStatusCodes()
   */
  public void setStatusCodes(final List<Status.Code> statusCodes) {
    this.statusCodes = statusCodes;
  }

  /**
   * Adds a {@link ReleaseDiscoveryListener} that will be {@linkplain
   * ReleaseDiscoveryListener#releaseDiscovered(ReleaseDiscoveryEvent)
   * notified when a release is retrieved
   *
   * @param listener the {@link ReleaseDiscoveryListener} to add; may be
   * {@code null} in which case no action will be taken
   *
   * @see #removeReleaseDiscoveryListener(ReleaseDiscoveryListener)
   *
   * @see #getReleaseDiscoveryListenersList()
   */
  public void addReleaseDiscoveryListener(final ReleaseDiscoveryListener listener) {
    if (listener != null) {
      if (this.releaseDiscoveryListeners == null) {
        this.releaseDiscoveryListeners = new ArrayList<>();      
      }
      this.releaseDiscoveryListeners.add(listener);
    }
  }

  /**
   * Removes a {@link ReleaseDiscoveryListener} from this {@link
   * GetHistoryMojo}.
   *
   * @param listener the {@link ReleaseDiscoveryListener} to remove; may
   * be {@code null} in which case no action will be taken
   *
   * @see #addReleaseDiscoveryListener(ReleaseDiscoveryListener)
   *
   * @see #getReleaseDiscoveryListenersList()
   */
  public void removeReleaseDiscoveryListener(final ReleaseDiscoveryListener listener) {
    if (listener != null && this.releaseDiscoveryListeners != null) {
      this.releaseDiscoveryListeners.remove(listener);
    }
  }

  /**
   * Invokes the {@link #getReleaseDiscoveryListenersList()} method and
   * {@linkplain Collection#toArray(Object[]) converts its return
   * value to an array}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * @return a non-{@code null} array of {@link
   * ReleaseDiscoveryListener}s
   *
   * @see #getReleaseDiscoveryListenersList()
   */
  public ReleaseDiscoveryListener[] getReleaseDiscoveryListeners() {
    final Collection<ReleaseDiscoveryListener> listeners = this.getReleaseDiscoveryListenersList();
    if (listeners == null || listeners.isEmpty()) {
      return new ReleaseDiscoveryListener[0];
    } else {
      return listeners.toArray(new ReleaseDiscoveryListener[listeners.size()]);
    }
  }

  /**
   * Returns the {@link List} of {@link ReleaseDiscoveryListener}s whose
   * elements will be {@linkplain
   * ReleaseDiscoveryListener#releaseDiscovered(ReleaseDiscoveryEvent)
   * notified when a release is retrieved.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method are permitted to return {@code
   * null}.</p>
   *
   * @return a {@link List} of {@link ReleaseDiscoveryListener}s, or
   * {@code null}
   *
   * @see #setReleaseDiscoveryListenersList(List)
   *
   * @see #addReleaseDiscoveryListener(ReleaseDiscoveryListener)
   *
   * @see #removeReleaseDiscoveryListener(ReleaseDiscoveryListener)
   */
  public List<ReleaseDiscoveryListener> getReleaseDiscoveryListenersList() {
    return this.releaseDiscoveryListeners;
  }

  /**
   * Installs the {@link List} of {@link ReleaseDiscoveryListener}s
   * whose elements will be {@linkplain
   * ReleaseDiscoveryListener#releaseDiscovered(ReleaseDiscoveryEvent)
   * notified when a release is retrieved}.
   *
   * @param releaseDiscoveryListeners the {@link List} of {@link
   * ReleaseDiscoveryListener}s whose elements will be {@linkplain
   * ReleaseDiscoveryListener#releaseDiscovered(ReleaseDiscoveryEvent)
   * notified when a release is retrieved}; may be {@code
   * null}
   *
   * @see #getReleaseDiscoveryListenersList()
   *
   * @see #addReleaseDiscoveryListener(ReleaseDiscoveryListener)
   *
   * @see #removeReleaseDiscoveryListener(ReleaseDiscoveryListener)
   */
  public void setReleaseDiscoveryListenersList(final List<ReleaseDiscoveryListener> releaseDiscoveryListeners) {
    this.releaseDiscoveryListeners = releaseDiscoveryListeners;
  }

}
