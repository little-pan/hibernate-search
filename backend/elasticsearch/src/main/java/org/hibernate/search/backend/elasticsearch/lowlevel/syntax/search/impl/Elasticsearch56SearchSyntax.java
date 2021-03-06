/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.elasticsearch.lowlevel.syntax.search.impl;

/**
 * The search syntax for ES5.6 to 6.2.
 */
public class Elasticsearch56SearchSyntax extends Elasticsearch60SearchSyntax {

	@Override
	public String getTermAggregationOrderByTermToken() {
		return "_term"; // _key in ES6.0+
	}

	@Override
	public boolean useOldSortNestedApi() {
		return true;
	}
}
