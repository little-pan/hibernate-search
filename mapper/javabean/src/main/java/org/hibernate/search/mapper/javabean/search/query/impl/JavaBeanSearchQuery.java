/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.mapper.javabean.search.query.impl;

import org.hibernate.search.engine.search.query.spi.IndexSearchQuery;
import org.hibernate.search.mapper.javabean.search.query.SearchQuery;
import org.hibernate.search.mapper.javabean.search.query.SearchResult;

public class JavaBeanSearchQuery<T> implements SearchQuery<T> {

	private final IndexSearchQuery<T> delegate;

	private Long firstResultIndex;
	private Long maxResultsCount;

	public JavaBeanSearchQuery(IndexSearchQuery<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public SearchResult<T> fetch() {
		return new JavaBeanSearchResult( delegate.fetch( maxResultsCount, firstResultIndex ) );
	}

	@Override
	public long fetchTotalHitCount() {
		return delegate.fetchTotalHitCount();
	}

	@Override
	public JavaBeanSearchQuery<T> setFirstResult(Long firstResultIndex) {
		this.firstResultIndex = firstResultIndex;
		return this;
	}

	@Override
	public JavaBeanSearchQuery<T> setMaxResults(Long maxResultsCount) {
		this.maxResultsCount = maxResultsCount;
		return this;
	}

	@Override
	public String getQueryString() {
		return delegate.getQueryString();
	}
}