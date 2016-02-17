package mil.nga.giat.geowave.core.ingest.local;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.giat.geowave.core.index.ByteArrayId;
import mil.nga.giat.geowave.core.ingest.GeoWaveData;
import mil.nga.giat.geowave.core.ingest.IngestCommandLineOptions;
import mil.nga.giat.geowave.core.ingest.IngestPluginBase;
import mil.nga.giat.geowave.core.ingest.IngestUtils;
import mil.nga.giat.geowave.core.ingest.index.IndexProvider;
import mil.nga.giat.geowave.core.store.CloseableIterator;
import mil.nga.giat.geowave.core.store.IndexWriter;
import mil.nga.giat.geowave.core.store.adapter.WritableDataAdapter;
import mil.nga.giat.geowave.core.store.index.PrimaryIndex;

/**
 * A special implementation of IngestUtils that allows multi-threaded input
 */
public class MultiThreadedLocalIngestUtils {

	private final static int MAX_QUEUE_SIZE = 500;	
	private final static Logger LOGGER = LoggerFactory.getLogger(MultiThreadedLocalIngestUtils.class);
	
	/**
	 * Create a thread pool for usage with multi-threaded ingest. This will create 'nthreads'
	 * which can all pull off of a task queue of 'boundedSize' (number of GeoWaveData items 
	 * outstanding)
	 * @param nthreads
	 * @param boundedSize
	 * @return
	 */
	public static ExecutorService getFixedThreadPool(int nthreads, int boundedSize) {
		BlockingQueue<Runnable> blockingQueue = 
				new ArrayBlockingQueue<Runnable>(
			    boundedSize);
		return new ThreadPoolExecutor(nthreads, nthreads, 30,
			    TimeUnit.SECONDS, blockingQueue,
			    new ThreadPoolExecutor.CallerRunsPolicy());
	}
	
	/**
	 * Perform a multi-threaded ingest
	 * @param input
	 * @param ingestOptions
	 * @param ingestPlugin - the format plugin (to retrieve adapter)
	 * @param indexProvider
	 * @param ingestRunData
	 * @param nthreads
	 * @throws IOException
	 */
	public static <T> void ingest(
			final T input,
			final IngestCommandLineOptions ingestOptions,
			final IngestPluginBase<T, ?> ingestPlugin,
			final IndexProvider indexProvider,
			final IngestRunData ingestRunData,
			final int nthreads)
			throws IOException {
		
		// Create a multithreaded pool
		ExecutorService service = getFixedThreadPool(nthreads, MAX_QUEUE_SIZE);
		
		final String[] dimensionTypes = ingestOptions.getDimensionalityTypes();
		final Map<ByteArrayId, IndexWriter> dimensionalityIndexMap = new HashMap<ByteArrayId, IndexWriter>();
		for (final String dimensionType : dimensionTypes) {
			final PrimaryIndex primaryIndex = IngestUtils.getIndex(
					ingestPlugin,
					ingestRunData.getArgs(),
					dimensionType);
			if (primaryIndex == null) {
				LOGGER.error("Could not get index instance, getIndex() returned null;");
				throw new IOException(
						"Could not get index instance, getIndex() returned null");
			}
			final IndexWriter primaryIndexWriter = ingestRunData.getIndexWriter(primaryIndex);
			final PrimaryIndex idx = primaryIndexWriter.getIndex();
			if (idx == null) {
				LOGGER.error("Could not get index instance, getIndex() returned null;");
				throw new IOException(
						"Could not get index instance, getIndex() returned null");
			}
			dimensionalityIndexMap.put(
					idx.getId(),
					primaryIndexWriter);
		}

		final Map<ByteArrayId, PrimaryIndex> requiredIndexMap = new HashMap<ByteArrayId, PrimaryIndex>();
		final PrimaryIndex[] requiredIndices = indexProvider.getRequiredIndices();
		if ((requiredIndices != null) && (requiredIndices.length > 0)) {
			for (final PrimaryIndex requiredIndex : requiredIndices) {
				requiredIndexMap.put(
						requiredIndex.getId(),
						requiredIndex);
			}
		}
		try (CloseableIterator<?> geowaveDataIt = ingestPlugin.toGeoWaveData(
				input,
				dimensionalityIndexMap.keySet(),
				ingestOptions.getVisibility())) {
			while (geowaveDataIt.hasNext()) {
				// Submit the unit of work to the thread pool
				final GeoWaveData<?> geowaveData = (GeoWaveData<?>) geowaveDataIt.next();
				service.submit(new MultiThreadedIngestUnitOfWork(ingestRunData, 
						geowaveData, dimensionalityIndexMap, requiredIndexMap));
			}
		}
		service.shutdown();
		try {
			service.awaitTermination(250L, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Ingest failed to terminate after 250 seconds");
		}
	}

	/**
	 * This class is used to task the threadpool workers.
	 */
	public static class MultiThreadedIngestUnitOfWork implements Runnable {
		
		private IngestRunData ingestRunData;
		private GeoWaveData<?> geowaveData;
		private Map<ByteArrayId, IndexWriter> dimensionalityIndexMap = new HashMap<ByteArrayId, IndexWriter>();
		private Map<ByteArrayId, PrimaryIndex> requiredIndexMap = new HashMap<ByteArrayId, PrimaryIndex>();
	
		public MultiThreadedIngestUnitOfWork(IngestRunData ingestRunData, GeoWaveData<?> geowaveData,
				Map<ByteArrayId, IndexWriter> dimensionalityIndexMap, Map<ByteArrayId, PrimaryIndex> requiredIndexMap) {
			super();
			this.ingestRunData = ingestRunData;
			this.geowaveData = geowaveData;
			this.dimensionalityIndexMap = dimensionalityIndexMap;
			this.requiredIndexMap = requiredIndexMap;
		}

		@Override
		public void run() {
			final WritableDataAdapter adapter = ingestRunData.getDataAdapter(geowaveData);
			if (adapter == null) {
				LOGGER.warn("Adapter not found for " + geowaveData.getValue());
				return;
			}
			IndexWriter indexWriter;
			for (final ByteArrayId indexId : geowaveData.getIndexIds()) {
				indexWriter = dimensionalityIndexMap.get(indexId);
				if (indexWriter == null) {
					final PrimaryIndex index = requiredIndexMap.get(indexId);
					if (index == null) {
						LOGGER.warn("Index '" + indexId.getString() + "' not found for " + geowaveData.getValue());
						continue;
					}
					indexWriter = ingestRunData.getIndexWriter(index);
				}
				indexWriter.write(
						adapter,
						geowaveData.getValue());
			}			
		}
	}	
}

