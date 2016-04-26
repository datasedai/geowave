package mil.nga.giat.geowave.cli.osm.accumulo.osmschema;

import com.google.common.base.Preconditions;

/**
 *
 */
public class ColumnQualifier
{
	public static final byte[] ID = "-id".getBytes(Constants.CHARSET);
	public static final byte[] LATITUDE = "-lat".getBytes(Constants.CHARSET);
	public static final byte[] LONGITUDE = "-lon".getBytes(Constants.CHARSET);
	public static final byte[] VERSION = "-ver".getBytes(Constants.CHARSET);
	public static final byte[] TIMESTAMP = "-ts".getBytes(Constants.CHARSET);
	public static final byte[] CHANGESET = "-cs".getBytes(Constants.CHARSET);
	public static final byte[] USER_TEXT = "-ut".getBytes(Constants.CHARSET);
	public static final byte[] USER_ID = "-uid".getBytes(Constants.CHARSET);
	public static final byte[] OSM_VISIBILITY = "-vis".getBytes(Constants.CHARSET);
	public static final byte[] REFERENCES = "-ref".getBytes(Constants.CHARSET);
	public static final String REFERENCE_MEMID_PREFIX = "-refmem";
	public static final String REFERENCE_ROLEID_PREFIX = "-refrol";
	public static final String REFERENCE_TYPE_PREFIX = "-reftype";

	public static final String REFERENCE_SEPARATOR = "_";

	public static byte[] getRelationMember(
			String prefix,
			int i ) {
		return (prefix + REFERENCE_SEPARATOR + String.valueOf(i)).getBytes(Constants.CHARSET);
	}

	public static byte[] TAG_QUALIFIER(
			String tag ) {
		Preconditions.checkNotNull(tag);
		return tag.getBytes(Constants.CHARSET);
	}
}
