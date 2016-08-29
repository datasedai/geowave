package mil.nga.giat.geowave.core.store.index.writer;

import java.io.Closeable;
import java.util.List;

import mil.nga.giat.geowave.core.index.ByteArrayId;
import mil.nga.giat.geowave.core.store.data.VisibilityWriter;
import mil.nga.giat.geowave.core.store.index.primary.PrimaryIndex;

public interface IndexWriter<T> extends
		Closeable
{
	/**
	 * Write the entry using the index writer's configure field visibility
	 * writer.
	 * 
	 * @param writableAdapter
	 * @param entry
	 * @return
	 */
	public List<ByteArrayId> write(
			final T entry );

	public List<ByteArrayId> write(
			final T entry,
			final VisibilityWriter<T> fieldVisibilityWriter );

	public PrimaryIndex[] getIndices();

	public void flush();
}
