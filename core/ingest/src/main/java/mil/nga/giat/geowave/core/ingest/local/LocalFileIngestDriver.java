package mil.nga.giat.geowave.core.ingest.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mil.nga.giat.geowave.core.cli.CommandLineResult;
import mil.nga.giat.geowave.core.cli.DataStoreCommandLineOptions;
import mil.nga.giat.geowave.core.ingest.IngestCommandLineOptions;
import mil.nga.giat.geowave.core.ingest.IngestFormatPluginProviderSpi;
import mil.nga.giat.geowave.core.ingest.IngestUtils;
import mil.nga.giat.geowave.core.ingest.local.threaded.IngestEntryWorker;
import mil.nga.giat.geowave.core.ingest.local.threaded.MultiThreadedIngestPlugin;
import mil.nga.giat.geowave.core.store.DataStore;
import mil.nga.giat.geowave.core.store.adapter.WritableDataAdapter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import kafka.log.Log;

/**
 * This extends the local file driver to directly ingest data into GeoWave
 * utilizing the LocalFileIngestPlugin's that are discovered by the system.
 */
public class LocalFileIngestDriver extends
		AbstractLocalFileDriver<LocalFileIngestPlugin<?>, IngestRunData>
{
	private final static Logger LOGGER = Logger.getLogger(LocalFileIngestDriver.class);
	protected DataStoreCommandLineOptions dataStoreOptions;
	protected IngestCommandLineOptions ingestOptions;
	protected ExecutorService ingestExecutor;

	public LocalFileIngestDriver(
			final String operation ) {
		super(
				operation);
	}

	@Override
	protected void parseOptionsInternal(
			final Options options,
			CommandLine commandLine )
			throws ParseException {
		final CommandLineResult<DataStoreCommandLineOptions> dataStoreOptionsResult = DataStoreCommandLineOptions.parseOptions(
				options,
				commandLine);
		dataStoreOptions = dataStoreOptionsResult.getResult();
		if (dataStoreOptionsResult.isCommandLineChange()) {
			commandLine = dataStoreOptionsResult.getCommandLine();
		}
		ingestOptions = IngestCommandLineOptions.parseOptions(commandLine);
		super.parseOptionsInternal(
				options,
				commandLine);
	}

	@Override
	protected void applyOptionsInternal(
			final Options allOptions ) {
		DataStoreCommandLineOptions.applyOptions(allOptions);
		IngestCommandLineOptions.applyOptions(allOptions);
		super.applyOptionsInternal(allOptions);
	}

	@Override
	protected boolean runInternal(
			final String[] args,
			final List<IngestFormatPluginProviderSpi<?, ?>> pluginProviders ) {
		// first collect the local file ingest plugins
		final Map<String, LocalFileIngestPlugin<?>> localFileIngestPlugins = new HashMap<String, LocalFileIngestPlugin<?>>();
		final List<WritableDataAdapter<?>> adapters = new ArrayList<WritableDataAdapter<?>>();
		for (final IngestFormatPluginProviderSpi<?, ?> pluginProvider : pluginProviders) {
			LocalFileIngestPlugin<?> localFileIngestPlugin = null;
			try {
				localFileIngestPlugin = pluginProvider.getLocalFileIngestPlugin();

				if (localFileIngestPlugin == null) {
					LOGGER.warn("Plugin provider for ingest type '" + pluginProvider.getIngestFormatName() + "' does not support local file ingest");
					continue;
				}
			}
			catch (final UnsupportedOperationException e) {
				LOGGER.warn(
						"Plugin provider '" + pluginProvider.getIngestFormatName() + "' does not support local file ingest",
						e);
				continue;
			}
			final boolean indexSupported = (IngestUtils.isSupported(
					localFileIngestPlugin,
					args,
					ingestOptions.getDimensionalityTypes()));
			if (!indexSupported) {
				LOGGER.warn("Local file ingest plugin for ingest type '" + pluginProvider.getIngestFormatName() + "' does not support dimensionality type '" + ingestOptions.getDimensionalityTypeArgument() + "'");
				continue;
			}
			localFileIngestPlugins.put(
					pluginProvider.getIngestFormatName(),
					localFileIngestPlugin);
			adapters.addAll(Arrays.asList(localFileIngestPlugin.getDataAdapters(ingestOptions.getVisibility())));
		}

		final DataStore dataStore = dataStoreOptions.createStore();
		try (IngestRunData runData = new IngestRunData(
				adapters,
				dataStore,
				args)) {

			startExecutor();

			processInput(
					localFileIngestPlugins,
					runData);
		}
		catch (final IOException e) {
			LOGGER.fatal(
					"Unexpected I/O exception when reading input files",
					e);
			return false;
		}
		finally {
			shutdownExecutor();
		}
		return true;
	}

	protected void startExecutor() {

		BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(
				localInput.getThreadsTotal());

		ingestExecutor = new ThreadPoolExecutor(
				1,
				localInput.getThreadsTotal(),
				30,
				TimeUnit.SECONDS,
				blockingQueue,
				new ThreadPoolExecutor.CallerRunsPolicy());

	}

	protected void shutdownExecutor() {
		ingestExecutor.shutdown();
		try {
			while (!ingestExecutor.awaitTermination(
					10,
					TimeUnit.SECONDS)) {
				LOGGER.debug("Awaiting completion of threads.");
			}
		}
		catch (InterruptedException e) {
			LOGGER.error("Failed to terminate executor service");
		}
	}

	@Override
	protected void processFile(
			final File file,
			final String typeName,
			final LocalFileIngestPlugin<?> plugin,
			final IngestRunData ingestRunData )
			throws IOException {

		// Create the multi-thread plugin
		MultiThreadedIngestPlugin<?, ?> wrapperPlugin = MultiThreadedIngestPlugin.wrap(
				file,
				plugin,
				plugin,
				ingestRunData,
				ingestOptions,
				localInput.getBatchSize());

		for (int i = 0; i < localInput.getThreadsPerFile(); i++) {
			IngestEntryWorker<?> worker = IngestEntryWorker.create(wrapperPlugin);
			LOGGER.info("Adding worker for plugin");
			ingestExecutor.submit(worker);
		}
	}
}
