package mil.nga.giat.geowave.core.ingest.local.threaded;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.giat.geowave.core.index.ByteArrayId;
import mil.nga.giat.geowave.core.ingest.GeoWaveData;
import mil.nga.giat.geowave.core.ingest.IngestCommandLineOptions;
import mil.nga.giat.geowave.core.ingest.IngestPluginBase;
import mil.nga.giat.geowave.core.ingest.index.IndexProvider;
import mil.nga.giat.geowave.core.ingest.local.IngestRunData;
import mil.nga.giat.geowave.core.ingest.local.LocalFileIngestPlugin;
import mil.nga.giat.geowave.core.store.CloseableIterator;
import mil.nga.giat.geowave.core.store.adapter.WritableDataAdapter;
import mil.nga.giat.geowave.core.store.index.CommonIndexValue;

/**
 * The multi-threaded ingest plugin extend ingest plugin base and serves as a
 * layer between a regular ingest plugin and the IngestUtils framework. It sends
 * back a multi-threaded iterator which sends records via a blocking queue to
 * any thread that asks for a record.
 */
public class MultiThreadedIngestPlugin<T, O> implements
		IngestPluginBase<T, O>
{

	private final static Logger LOGGER = LoggerFactory.getLogger(MultiThreadedIngestPlugin.class);

	private final T file;
	private final IngestRunData runData;
	private final IngestPluginBase<T, O> plugin;
	private final IndexProvider indexProvider;
	private final int batchSize;
	private final IngestCommandLineOptions options;
	private MultiThreadedIngestIterator<GeoWaveData<O>> wrappedIterator = null;

	public MultiThreadedIngestPlugin(
			final T file,
			final IngestPluginBase<T, O> plugin,
			final IndexProvider indexProvider,
			final IngestRunData runData,
			final IngestCommandLineOptions options,
			final int batchSize ) {
		this.file = file;
		this.plugin = plugin;
		this.indexProvider = indexProvider;
		this.runData = runData;
		this.options = options;
		this.batchSize = batchSize;
	}

	public T getFile() {
		return file;
	}

	public IngestRunData getRunData() {
		return runData;
	}

	public IngestCommandLineOptions getOptions() {
		return options;
	}

	public IndexProvider getIndexProvider() {
		return indexProvider;
	}

	@Override
	public WritableDataAdapter<O>[] getDataAdapters(
			String globalVisibility ) {
		return plugin.getDataAdapters(globalVisibility);
	}

	@Override
	public Class<? extends CommonIndexValue>[] getSupportedIndexableTypes() {
		return plugin.getSupportedIndexableTypes();
	}

	@Override
	public CloseableIterator<GeoWaveData<O>> toGeoWaveData(
			T input,
			Collection<ByteArrayId> primaryIndexIds,
			String globalVisibility ) {

		// Initialize the wrapped iterator if it hasn't been initialized yet
		if (wrappedIterator == null) {
			synchronized (this) {
				if (wrappedIterator == null) {
					wrappedIterator = new MultiThreadedIngestIterator<GeoWaveData<O>>(
							plugin.toGeoWaveData(
									input,
									primaryIndexIds,
									globalVisibility),
							batchSize);
				}
			}
		}

		return wrappedIterator;
	}

	public void terminate()
			throws IOException {
		wrappedIterator.close();
	}

	/**
	 * Static wrapper method to instantiate a multi-threaded ingest plugin
	 * 
	 * @param file
	 * @param plugin
	 * @param indexProvider
	 * @param runData
	 * @param options
	 * @param batchSize
	 * @return
	 */
	public static <T, O> MultiThreadedIngestPlugin<T, O> wrap(
			T file,
			IngestPluginBase<T, O> plugin,
			IndexProvider indexProvider,
			IngestRunData runData,
			IngestCommandLineOptions options,
			int batchSize ) {
		return new MultiThreadedIngestPlugin<T, O>(
				file,
				plugin,
				indexProvider,
				runData,
				options,
				batchSize);
	}

}
