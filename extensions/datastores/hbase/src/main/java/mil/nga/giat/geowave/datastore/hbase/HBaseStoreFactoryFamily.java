package mil.nga.giat.geowave.datastore.hbase;

import mil.nga.giat.geowave.core.store.DataStore;
import mil.nga.giat.geowave.core.store.GenericStoreFactory;
import mil.nga.giat.geowave.core.store.StoreFactoryFamilySpi;
import mil.nga.giat.geowave.core.store.adapter.AdapterIndexMappingStore;
import mil.nga.giat.geowave.core.store.adapter.AdapterStore;
import mil.nga.giat.geowave.core.store.adapter.statistics.DataStatisticsStore;
import mil.nga.giat.geowave.core.store.index.IndexStore;
import mil.nga.giat.geowave.datastore.hbase.metadata.HBaseAdapterIndexMappingStoreFactory;
import mil.nga.giat.geowave.datastore.hbase.metadata.HBaseAdapterStoreFactory;
import mil.nga.giat.geowave.datastore.hbase.metadata.HBaseDataStatisticsStoreFactory;
import mil.nga.giat.geowave.datastore.hbase.metadata.HBaseIndexStoreFactory;

public class HBaseStoreFactoryFamily extends
		AbstractHBaseFactory implements
		StoreFactoryFamilySpi
{
	@Override
	public GenericStoreFactory<DataStore> getDataStoreFactory() {
		return new HBaseDataStoreFactory();
	}

	@Override
	public GenericStoreFactory<DataStatisticsStore> getDataStatisticsStoreFactory() {
		return new HBaseDataStatisticsStoreFactory();
	}

	@Override
	public GenericStoreFactory<IndexStore> getIndexStoreFactory() {
		return new HBaseIndexStoreFactory();
	}

	@Override
	public GenericStoreFactory<AdapterStore> getAdapterStoreFactory() {
		return new HBaseAdapterStoreFactory();
	}

	@Override
	public GenericStoreFactory<AdapterIndexMappingStore> getAdapterIndexMappingStoreFactory() {
		return new HBaseAdapterIndexMappingStoreFactory();
	}

}
