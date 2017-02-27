package mil.nga.giat.geowave.datastore.accumulo.query;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.accumulo.core.client.BatchDeleter;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.client.ScannerBase;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.log4j.Logger;

import mil.nga.giat.geowave.core.store.CloseableIterator;
import mil.nga.giat.geowave.core.store.CloseableIteratorWrapper;
import mil.nga.giat.geowave.core.store.adapter.DataAdapter;
import mil.nga.giat.geowave.core.store.callback.ScanCallback;
import mil.nga.giat.geowave.core.store.filter.DedupeFilter;
import mil.nga.giat.geowave.core.store.index.PrimaryIndex;
import mil.nga.giat.geowave.datastore.accumulo.AccumuloOperations;

public class AccumuloRowIdsDelete<T> extends AccumuloRowIdsQuery<T> {

	private static final Logger LOGGER = Logger.getLogger(AccumuloRowIdsDelete.class);
	
	public AccumuloRowIdsDelete(
			DataAdapter adapter, 
			PrimaryIndex index, 
			Collection rows,
			ScanCallback scanCallback,
			DedupeFilter dedupFilter, 
			String[] authorizations) {
		super(
				adapter, 
				index, 
				rows, 
				scanCallback, 
				dedupFilter, 
				authorizations);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected CloseableIterator<Object> initCloseableIterator(
			ScannerBase scanner,
			Iterator it ) {
		return new CloseableIteratorWrapper(
				new Closeable() {
					@Override
					public void close()
							throws IOException {
						if (scanner instanceof BatchDeleter) {
							try {
								((BatchDeleter) scanner).delete();
							}
							catch (MutationsRejectedException | TableNotFoundException e) {
								LOGGER.warn("Unable to delete rows by query constraints",e);
							}
						}
						scanner.close();
					}
				},
				it);
	}

	@Override
	protected ScannerBase createScanner(
			AccumuloOperations accumuloOperations,
			String tableName,
			boolean batchScanner,
			String... authorizations )
			throws TableNotFoundException {
		return accumuloOperations.createBatchDeleter(
				tableName,
				authorizations);
	}
	
	
}
