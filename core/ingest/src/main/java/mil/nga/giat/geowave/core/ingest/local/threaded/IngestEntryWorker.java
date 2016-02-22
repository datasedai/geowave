package mil.nga.giat.geowave.core.ingest.local.threaded;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.giat.geowave.core.ingest.IngestUtils;
import mil.nga.giat.geowave.core.ingest.local.IngestRunData;
import mil.nga.giat.geowave.core.ingest.local.LocalFileIngestPlugin;

/**
 * Execute the IngestUtils.ingest method continuously until
 */
public class IngestEntryWorker<T> implements
		Runnable
{

	private final static Logger LOGGER = LoggerFactory.getLogger(IngestEntryWorker.class);

	private MultiThreadedIngestPlugin<T, ?> plugin = null;

	public IngestEntryWorker(
			MultiThreadedIngestPlugin<T, ?> plugin ) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		try {
			LOGGER.info("Worker executing for plugin");

			IngestUtils.ingest(
					plugin.getFile(),
					plugin.getOptions(),
					plugin,
					plugin.getIndexProvider(),
					plugin.getRunData());
		}
		catch (IOException e) {
			LOGGER.error(
					"Failed to ingest file",
					e);
			try {
				plugin.terminate();
			}
			catch (IOException e1) {}
		}
	}

	public static <T> IngestEntryWorker<T> create(
			MultiThreadedIngestPlugin<T, ?> wrapperPlugin ) {
		return new IngestEntryWorker<>(
				wrapperPlugin);
	}
}
