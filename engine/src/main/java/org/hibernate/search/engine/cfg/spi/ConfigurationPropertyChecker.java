/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.cfg.spi;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.search.engine.cfg.ConfigurationPropertyCheckingStrategyName;
import org.hibernate.search.engine.cfg.EngineSettings;
import org.hibernate.search.engine.logging.impl.Log;
import org.hibernate.search.util.common.AssertionFailure;
import org.hibernate.search.util.common.impl.CollectionHelper;
import org.hibernate.search.util.common.logging.impl.LoggerFactory;

/**
 * A utility that checks usage of property keys
 * by wrapping a {@link ConfigurationPropertySource}
 * and requiring special hooks to be called before and after bootstrap.
 */
public class ConfigurationPropertyChecker {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private static final ConfigurationProperty<ConfigurationPropertyCheckingStrategyName>
			CONFIGURATION_PROPERTY_CHECKING_STRATEGY =
			ConfigurationProperty.forKey( EngineSettings.CONFIGURATION_PROPERTY_CHECKING_STRATEGY )
					.as( ConfigurationPropertyCheckingStrategyName.class, ConfigurationPropertyCheckingStrategyName::of )
					.withDefault( EngineSettings.Defaults.CONFIGURATION_PROPERTY_CHECKING_STRATEGY )
					.build();

	public static ConfigurationPropertyChecker create() {
		return new ConfigurationPropertyChecker();
	}

	private final Set<String> availablePropertyKeys = ConcurrentHashMap.newKeySet();
	private final Set<String> consumedPropertyKeys = ConcurrentHashMap.newKeySet();

	private volatile boolean warn;

	private ConfigurationPropertyChecker() {
	}

	public ConfigurationPropertySource wrap(AllAwareConfigurationPropertySource source) {
		ConfigurationPropertySource trackingSource =
				new ConsumedPropertyTrackingConfigurationPropertySource(
						source, this::addConsumedPropertyKey
				);
		ConfigurationPropertyCheckingStrategyName checkingStrategy =
				CONFIGURATION_PROPERTY_CHECKING_STRATEGY.get( trackingSource );
		switch ( checkingStrategy ) {
			case WARN:
				this.warn = true;
				availablePropertyKeys.addAll( source.resolveAll( EngineSettings.PREFIX ) );
				return trackingSource;
			case IGNORE:
				return source;
			default:
				throw new AssertionFailure(
						"Unexpected configuration property checking strategy name: " + checkingStrategy
				);
		}
	}

	public void beforeBoot() {
		if ( !warn ) {
			log.configurationPropertyTrackingDisabled();
		}
	}

	public void afterBoot(ConfigurationPropertyChecker firstPhaseChecker, ConfigurationPropertySource propertySource) {
		if ( !warn ) {
			return;
		}

		List<ConfigurationPropertyChecker> checkers =
				CollectionHelper.asImmutableList( firstPhaseChecker, this );
		Set<String> unconsumedPropertyKeys = new LinkedHashSet<>();

		// Add all available property keys
		for ( ConfigurationPropertyChecker checker : checkers ) {
			unconsumedPropertyKeys.addAll( checker.availablePropertyKeys );
		}

		// Remove all consumed property keys
		for ( ConfigurationPropertyChecker checker : checkers ) {
			unconsumedPropertyKeys.removeAll( checker.consumedPropertyKeys );
		}

		if ( !unconsumedPropertyKeys.isEmpty() ) {
			log.configurationPropertyTrackingUnusedProperties(
					unconsumedPropertyKeys,
					CONFIGURATION_PROPERTY_CHECKING_STRATEGY.resolveOrRaw( propertySource ),
					ConfigurationPropertyCheckingStrategyName.IGNORE.getExternalRepresentation()
			);
		}
	}

	private void addConsumedPropertyKey(String key) {
		consumedPropertyKeys.add( key );
	}

}