package mil.nga.giat.geowave.core.ingest.local.threaded;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.directory.api.util.exception.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.giat.geowave.core.store.CloseableIterator;

public class MultiThreadedIngestIterator<E> implements
		CloseableIterator<E>
{

	private final static Logger LOGGER = LoggerFactory.getLogger(MultiThreadedIngestIterator.class);

	private CloseableIterator<E> wrappedIterator;
	private ConcurrentMap<Long, BlockingQueue<E>> threadQueues = new ConcurrentHashMap<Long, BlockingQueue<E>>();
	private Semaphore readSemaphore = new Semaphore(
			1);
	private boolean isFinished = false;
	private int batchSize;

	protected MultiThreadedIngestIterator(
			CloseableIterator<E> wrappedIterator,
			int batchSize ) {
		this.wrappedIterator = wrappedIterator;
		this.batchSize = batchSize;
	}

	@Override
	public boolean hasNext() {

		// Have I seen this guy before?
		long threadId = Thread.currentThread().getId();
		BlockingQueue<E> queue = threadQueues.get(threadId);
		if (queue == null) {
			queue = new ArrayBlockingQueue<E>(
					batchSize);
			threadQueues.put(
					threadId,
					queue);
		}

		E item = null;
		while (item == null && !isFinished) {
			// Load more if we don't have any...
			if (queue.isEmpty()) {
				loadMore();
			}

			// Attempt to take an item from the list. If it fails,
			// then it was exhausted before we got here, try to
			// load more.
			try {
				item = queue.poll(
						100,
						TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e) {
				LOGGER.error("Thread interrupted while polling data items");
			}
		}
		LOGGER.info("Thread finished for plugin: " + queue.isEmpty());
		return queue.isEmpty();
	}

	@Override
	public E next() {
		long threadId = Thread.currentThread().getId();
		BlockingQueue<E> queue = threadQueues.get(threadId);
		try {
			return queue.take();
		}
		catch (InterruptedException e) {
			LOGGER.error("Thread interrupted while taking data items");
		}
		return null;
	}

	@Override
	public void close()
			throws IOException {
		isFinished = true;
		synchronized (this) {
			if (wrappedIterator != null) {
				wrappedIterator.close();
				wrappedIterator = null;
			}
		}
	}

	/**
	 * Read the iterator, loading up the blocking queue with X items
	 */
	private void loadMore() {
		boolean acquired = false;
		try {
			acquired = readSemaphore.tryAcquire(
					100,
					TimeUnit.MILLISECONDS);
			if (acquired) {
				LOGGER.info("Loading more for plugin");

				// If we got the lease, read the batch size amount into the
				// queue
				// Read up to batchSize entries for every blocking queue.
				for (BlockingQueue<E> blockingQueue : threadQueues.values()) {
					while (wrappedIterator.hasNext() && blockingQueue.remainingCapacity() > 0) {
						try {
							blockingQueue.put(wrappedIterator.next());
						}
						catch (InterruptedException e) {
							LOGGER.error("Thread interrupted while reading data items");
							break;
						}
						isFinished = !wrappedIterator.hasNext();
					}
					if (isFinished) {
						break;
					}
				}
			}
		}
		catch (InterruptedException e1) {
			LOGGER.error("Thread interrupted while attempting to load more items");
		}
		finally {
			if (acquired) {
				readSemaphore.release();
			}
		}
	}

	@Override
	public void remove() {
		throw new NotImplementedException(
				"Remove is not implemented for multi-threaded ingest");
	}

}
