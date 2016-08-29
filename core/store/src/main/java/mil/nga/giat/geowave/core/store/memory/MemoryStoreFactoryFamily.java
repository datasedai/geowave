package mil.nga.giat.geowave.core.store.memory;

import mil.nga.giat.geowave.core.store.DataStore;
import mil.nga.giat.geowave.core.store.GenericStoreFactory;
import mil.nga.giat.geowave.core.store.StoreFactoryFamilySpi;
import mil.nga.giat.geowave.core.store.adapter.AdapterIndexMappingStore;
import mil.nga.giat.geowave.core.store.adapter.AdapterStore;
import mil.nga.giat.geowave.core.store.adapter.statistics.DataStatisticsStore;
import mil.nga.giat.geowave.core.store.index.IndexStore;

public class MemoryStoreFactoryFamily extends
		AbstractMemoryFactory implements
		StoreFactoryFamilySpi
{

	@Override
	public GenericStoreFactory<DataStore> getDataStoreFactory() {
		return new MemoryDataStoreFactory();
	}

	@Override
	public GenericStoreFactory<DataStatisticsStore> getDataStatisticsStoreFactory() {
		return new MemoryDataStatisticsStoreFactory();
	}

	@Override
	public GenericStoreFactory<IndexStore> getIndexStoreFactory() {
		return new MemoryIndexStoreFactory();
	}

	@Override
	public GenericStoreFactory<AdapterStore> getAdapterStoreFactory() {
		return new MemoryAdapterStoreFactory();
	}

	@Override
	public GenericStoreFactory<AdapterIndexMappingStore> getAdapterIndexMappingStoreFactory() {
		return new MemoryAdapterIndexMappingStoreFactory();
	}

}
