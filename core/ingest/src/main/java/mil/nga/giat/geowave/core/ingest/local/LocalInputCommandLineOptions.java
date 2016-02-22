package mil.nga.giat.geowave.core.ingest.local;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

/**
 * This class encapsulates all of the options and parsed values specific to
 * directing the ingestion framework to a local file system. The user must set
 * an input file or directory and can set a list of extensions to narrow the
 * ingestion to. The process will recurse a directory and filter by the
 * extensions if provided.
 */
public class LocalInputCommandLineOptions
{
	private final static Logger LOGGER = Logger.getLogger(LocalInputCommandLineOptions.class);
	private final String input;
	private final String[] extensions;
	private final int threadsTotal;
	private final int threadsPerFile;
	private final int batchSize;

	public LocalInputCommandLineOptions(
			final String input,
			final String[] extensions,
			final int threadsTotal,
			final int threadsPerFile,
			final int batchSize ) {
		this.input = input;
		this.extensions = extensions;
		this.threadsTotal = threadsTotal;
		this.threadsPerFile = threadsPerFile;
		this.batchSize = batchSize;
	}

	public String getInput() {
		return input;
	}

	public int getThreadsTotal() {
		return threadsTotal;
	}

	public int getThreadsPerFile() {
		return threadsPerFile;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public String[] getExtensions() {
		return extensions;
	}

	public static LocalInputCommandLineOptions parseOptions(
			final CommandLine commandLine )
			throws ParseException {
		int threadsTotal = 1;
		if (commandLine.hasOption("tt")) {
			try {
				threadsTotal = Integer.parseInt(commandLine.getOptionValue("tt"));
				if (threadsTotal < 1) {
					throw new ParseException(
							"Invalid total threads input");
				}
			}
			catch (final Exception ex) {
				LOGGER.warn(
						"Error parsing total threads argument, ignoring total threads option",
						ex);
			}
		}
		int threadsPerFile = threadsTotal;
		if (commandLine.hasOption("tpf")) {
			try {
				threadsPerFile = Integer.parseInt(commandLine.getOptionValue("tpf"));
				if (threadsPerFile < 1) {
					throw new ParseException(
							"Invalid threads per file input");
				}
				if (threadsPerFile > threadsTotal) {
					threadsPerFile = threadsTotal;
				}
			}
			catch (final Exception ex) {
				LOGGER.warn(
						"Error parsing threads per file argument, ignoring threads per file option",
						ex);
			}
		}
		int batchSize = 500;
		if (commandLine.hasOption("bs")) {
			try {
				batchSize = Integer.parseInt(commandLine.getOptionValue("bs"));
				if (batchSize < 1) {
					throw new ParseException(
							"Invalid batch size input");
				}
			}
			catch (final Exception ex) {
				LOGGER.warn(
						"Error parsing batch size argument, ignoring batch size option",
						ex);
			}
		}
		String value = null;
		if (commandLine.hasOption("b")) {
			value = commandLine.getOptionValue("b");
		}
		else {
			throw new ParseException(
					"Unable to ingest data, input file or base directory not specified");
		}
		String[] extensions = null;

		if (commandLine.hasOption("x")) {
			try {
				extensions = commandLine.getOptionValue(
						"x").split(
						",");

			}
			catch (final Exception ex) {
				LOGGER.warn(
						"Error parsing extensions argument, ignoring file extension option",
						ex);
			}
		}
		return new LocalInputCommandLineOptions(
				value,
				extensions,
				threadsTotal,
				threadsPerFile,
				batchSize);
	}

	public static void applyOptions(
			final Options allOptions ) {
		allOptions.addOption(new Option(
				"b",
				"base",
				true,
				"Base input file or directory to crawl with one of the supported ingest types"));

		allOptions.addOption(
				"x",
				"extension",
				true,
				"individual or comma-delimited set of file extensions to accept (optional)");

		allOptions.addOption(
				"tt",
				"total-threads",
				true,
				"number of total threads to use for ingest, default to 1 (optional)");

		allOptions.addOption(
				"tpf",
				"threads-per-file",
				true,
				"number of threads to use for ingest for each file, default to 'total-threads' (optional)");

		allOptions.addOption(
				"bs",
				"batch-size",
				true,
				"total entries to read from source before writing (optional)");

	}
}
