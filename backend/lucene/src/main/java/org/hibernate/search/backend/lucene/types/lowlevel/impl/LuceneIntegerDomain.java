/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.types.lowlevel.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;

import org.hibernate.search.backend.lucene.lowlevel.docvalues.impl.JoiningLongMultiValuesSource;
import org.hibernate.search.backend.lucene.lowlevel.facet.impl.FacetCountsUtils;
import org.hibernate.search.backend.lucene.lowlevel.join.impl.NestedDocsProvider;
import org.hibernate.search.util.common.data.Range;

import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.DoubleValues;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.Query;
import org.hibernate.search.backend.lucene.lowlevel.docvalues.impl.LongMultiValuesToSingleValuesSource;
import org.hibernate.search.backend.lucene.lowlevel.docvalues.impl.MultiValueMode;
import org.hibernate.search.backend.lucene.lowlevel.facet.impl.LongMultiValueFacetCounts;
import org.hibernate.search.backend.lucene.lowlevel.facet.impl.LongMultiValueRangeFacetCounts;

public class LuceneIntegerDomain implements LuceneNumericDomain<Integer> {
	private static final LuceneNumericDomain<Integer> INSTANCE = new LuceneIntegerDomain();

	public static LuceneNumericDomain<Integer> get() {
		return INSTANCE;
	}

	@Override
	public Integer getMinValue() {
		return Integer.MIN_VALUE;
	}

	@Override
	public Integer getMaxValue() {
		return Integer.MAX_VALUE;
	}

	@Override
	public Integer getPreviousValue(Integer value) {
		return Math.addExact( value, -1 );
	}

	@Override
	public Integer getNextValue(Integer value) {
		return Math.addExact( value, 1 );
	}

	@Override
	public Comparator<Integer> createComparator() {
		return Comparator.naturalOrder();
	}

	@Override
	public Query createExactQuery(String absoluteFieldPath, Integer value) {
		return IntPoint.newExactQuery( absoluteFieldPath, value );
	}

	@Override
	public Query createRangeQuery(String absoluteFieldPath, Integer lowerLimit, Integer upperLimit) {
		return IntPoint.newRangeQuery(
				absoluteFieldPath, lowerLimit, upperLimit
		);
	}

	@Override
	public Integer sortedDocValueToTerm(long longValue) {
		return (int) longValue;
	}

	@Override
	public Facets createTermsFacetCounts(String absoluteFieldPath, FacetsCollector facetsCollector,
			NestedDocsProvider nestedDocsProvider) throws IOException {
		JoiningLongMultiValuesSource source = JoiningLongMultiValuesSource.fromIntField(
				absoluteFieldPath, nestedDocsProvider
		);
		return new LongMultiValueFacetCounts(
				absoluteFieldPath, source,
				facetsCollector
		);
	}

	@Override
	public Facets createRangeFacetCounts(String absoluteFieldPath, FacetsCollector facetsCollector,
			Collection<? extends Range<? extends Integer>> ranges,
			NestedDocsProvider nestedDocsProvider) throws IOException {
		JoiningLongMultiValuesSource source = JoiningLongMultiValuesSource.fromIntField(
				absoluteFieldPath, nestedDocsProvider
		);
		return new LongMultiValueRangeFacetCounts(
				absoluteFieldPath, source,
				facetsCollector,
				FacetCountsUtils.createLongRangesForIntegralValues( ranges )
		);
	}

	@Override
	public IndexableField createIndexField(String absoluteFieldPath, Integer numericValue) {
		return new IntPoint( absoluteFieldPath, numericValue );
	}

	@Override
	public IndexableField createSortedDocValuesField(String absoluteFieldPath, Integer numericValue) {
		return new SortedNumericDocValuesField( absoluteFieldPath, numericValue.longValue() );
	}

	@Override
	public FieldComparator.NumericComparator<Integer> createFieldComparator(String fieldName, int numHits,
			MultiValueMode multiValueMode, Integer missingValue, NestedDocsProvider nestedDocsProvider) {
		LongMultiValuesToSingleValuesSource source = LongMultiValuesToSingleValuesSource.fromIntField( fieldName, multiValueMode, nestedDocsProvider );
		return new IntegerFieldComparator( numHits, fieldName, missingValue, source );
	}

	public static class IntegerFieldComparator extends FieldComparator.IntComparator {

		private final LongMultiValuesToSingleValuesSource source;

		public IntegerFieldComparator(int numHits, String field, Integer missingValue, LongMultiValuesToSingleValuesSource source) {
			super( numHits, field, missingValue );
			this.source = source;
		}

		@Override
		protected NumericDocValues getNumericDocValues(LeafReaderContext context, String field) throws IOException {
			return source.getRawNumericDocValues( context, DoubleValues.withDefault( DoubleValues.EMPTY, missingValue ) );
		}
	}
}
