package mil.nga.giat.geowave.adapter.raster.plugin;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;

import mil.nga.giat.geowave.adapter.raster.plugin.GeoWaveRasterConfig.ConfigParameter;
import mil.nga.giat.geowave.core.index.StringUtils;

import org.apache.log4j.Logger;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.CRS;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GeoWaveGTRasterFormat extends
		AbstractGridFormat implements
		Format
{
	private final static Logger LOGGER = Logger.getLogger(GeoWaveGTRasterFormat.class);
	public static final ParameterDescriptor<Color> OUTPUT_TRANSPARENT_COLOR = new DefaultParameterDescriptor<Color>(
			"OutputTransparentColor",
			Color.class,
			null,
			null);
	public static final CoordinateReferenceSystem DEFAULT_CRS;

	static {
		try {
			DEFAULT_CRS = CRS.decode("EPSG:4326", true);
		}
		catch (final FactoryException e) {
			LOGGER.error(
					"Unable to decode EPSG:4326 CRS",
					e);
			throw new RuntimeException(
					"Unable to initialize EPSG:4326 CRS");
		}
	}

	public GeoWaveGTRasterFormat() {
		super();
		setInfo();
	}

	/**
	 * Sets the metadata information.
	 */
	private void setInfo() {
		final HashMap<String, String> info = new HashMap<String, String>();

		info.put(
				"name",
				"GeoWaveRasterFormat");
		info.put(
				"description",
				"Image mosaicking and pyramiding in GeoWave");
		info.put(
				"vendor",
				"GeoWave");
		info.put(
				"docURL",
				"https://github.com/ngageoint/geowave");
		info.put(
				"version",
				"0.9.2");
		mInfo = info;

		// reading parameters
		readParameters = new ParameterGroup(
				new DefaultParameterDescriptorGroup(
						mInfo,
						new GeneralParameterDescriptor[] {
							READ_GRIDGEOMETRY2D,
							OUTPUT_TRANSPARENT_COLOR,
							BACKGROUND_COLOR
						}));

		// reading parameters
		writeParameters = null;
	}

	@Override
	public AbstractGridCoverage2DReader getReader(
			final Object source ) {
		return getReader(
				source,
				null);
	}

	@Override
	public AbstractGridCoverage2DReader getReader(
			final Object source,
			final Hints hints ) {
		try {
			return new GeoWaveRasterReader(
					source,
					hints);
		}
		catch (final Exception e) {
			LOGGER.warn(
					"Cannot create geowave raster reader",
					e);

			return null;
		}
	}

	@Override
	public GridCoverageWriter getWriter(
			final Object destination ) {
		throw new UnsupportedOperationException(
				"This plugin does not support writing.");
	}

	@Override
	public boolean accepts(
			final Object source,
			final Hints hints ) {
		if (source == null) {
			return false;
		}
		if (isParamList(source)) {
			return true;
		}
		return validateURL(source);
	}

	@Override
	public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
		throw new UnsupportedOperationException(
				"This plugin does not support writing.");
	}

	@Override
	public GridCoverageWriter getWriter(
			final Object destination,
			final Hints hints ) {
		throw new UnsupportedOperationException(
				"This plugin does not support writing.");
	}

	public static boolean isParamList(
			final Object source ) {
		return ((source instanceof String) && source.toString().contains(
				"=") && source.toString().contains(
				";"));
	}

	public static URL getURLFromSource(
			final Object source ) {
		if (source == null) {
			return null;
		}

		URL sourceURL = null;

		try {
			if (source instanceof File) {
				sourceURL = ((File) source).toURI().toURL();
			}
			else if (source instanceof URL) {
				sourceURL = (URL) source;
			}
			else if (source instanceof String) {
				final File tempFile = new File(
						(String) source);

				if (tempFile.exists()) {
					sourceURL = tempFile.toURI().toURL();
				}
				else {
					sourceURL = new URL(
							URLDecoder.decode(
									(String) source,
									"UTF8"));
				}
			}
		}
		catch (final Exception e) {
			LOGGER.warn(
					"Unable to read source URL",
					e);

			return null;
		}

		return sourceURL;
	}

	public static boolean validateURL(
			final Object source ) {
		final URL sourceUrl = getURLFromSource(source);

		if (sourceUrl == null) {
			return false;
		}

		if (!sourceUrl.getPath().toLowerCase().endsWith(
				".xml")) {
			return false;
		}

		// Namespace is the only required parameter
		// TODO figure out the equivalent of namespace in the current CLI work
		// final ByteArrayOutputStream out = new ByteArrayOutputStream();
		//
		// try {
		// final InputStream in = (InputStream) sourceUrl.getContent();
		// int c;
		//
		// while ((c = in.read()) != -1) {
		// out.write(c);
		// }
		//
		// in.close();
		// out.close();
		// }
		// catch (final IOException e) {
		// return false;
		// }

		// or figure out a different way to validate

		// final String xmlStr;
		// try {
		// out.toString(StringUtils.UTF8_CHAR_SET.toString());
		// }
		// catch (final UnsupportedEncodingException e) {
		// LOGGER.error(
		// "Unable to write ByteArray to UTF-8",
		// e);
		// return false;
		// }

		// if (!xmlStr.contains(ConfigParameter.NAMESPACE.getConfigName())) {
		// return false;
		// }
		return true;
	}
}
