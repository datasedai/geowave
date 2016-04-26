package mil.nga.giat.geowave.datastore.accumulo;

import mil.nga.giat.geowave.core.store.DataStore;
import mil.nga.giat.geowave.core.store.StoreFactoryOptions;
import mil.nga.giat.geowave.datastore.accumulo.index.secondary.AccumuloSecondaryIndexDataStore;
import mil.nga.giat.geowave.datastore.accumulo.metadata.AccumuloAdapterIndexMappingStore;
import mil.nga.giat.geowave.datastore.accumulo.metadata.AccumuloAdapterStore;
import mil.nga.giat.geowave.datastore.accumulo.metadata.AccumuloDataStatisticsStore;
import mil.nga.giat.geowave.datastore.accumulo.metadata.AccumuloIndexStore;
import mil.nga.giat.geowave.datastore.accumulo.operations.config.AccumuloOptions;
import mil.nga.giat.geowave.datastore.accumulo.operations.config.AccumuloRequiredOptions;

public class AccumuloDataStoreFactory extends
		AbstractAccumuloStoreFactory<DataStore>
{

	@Override
	public DataStore createStore(
			final StoreFactoryOptions options ) {
		if (!(options instanceof AccumuloRequiredOptions)) {
			throw new AssertionError(
					"Expected " + AccumuloRequiredOptions.class.getSimpleName());
		}
		AccumuloRequiredOptions opts = (AccumuloRequiredOptions) options;
		if (opts.getAdditionalOptions() == null) {
			opts.setAdditionalOptions(new AccumuloOptions());
		}

		BasicAccumuloOperations accumuloOperations = createOperations(opts);
		return new AccumuloDataStore(
				new AccumuloIndexStore(
						accumuloOperations),
				new AccumuloAdapterStore(
						accumuloOperations),
				new AccumuloDataStatisticsStore(
						accumuloOperations),
				new AccumuloSecondaryIndexDataStore(
						accumuloOperations,
						opts.getAdditionalOptions()),
				new AccumuloAdapterIndexMappingStore(
						accumuloOperations),
				accumuloOperations,
				opts.getAdditionalOptions());

	}
}
