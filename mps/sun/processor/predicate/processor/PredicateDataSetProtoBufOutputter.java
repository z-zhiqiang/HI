package sun.processor.predicate.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sun.dim.fileformat.v3.ItemsetFileFormat.TransactionDBFMT;
import org.sun.dim.fileformat.v3.ItemsetFileFormat.TransactionFMT;
import org.sun.dim.fileformat.v3.ItemsetFileFormat.TransactionListFMT;
import org.sun.dim.fileformat.v3.ItemsetFileFormat.VertexFMT;

import sun.processor.graph.processor.minus.EdgeMap;
import sun.processor.predicate.PredicateDataSet;
import sun.processor.predicate.PredicateDataSet.Run;
import sun.processor.predicate.PredicateItem;
import sun.processor.predicate.PredicateItem.PredicateValue;

public class PredicateDataSetProtoBufOutputter extends
		AbstractPredicateDataSetProcessor {

	private static class FakeVertexMap {
		private Set<Integer> vertices = new HashSet<Integer>();

		public void add(int id) {
			this.vertices.add(id);
		}

		public void save(TransactionDBFMT.Builder dbBuilder) {
			for (Integer vertex : vertices) {
				VertexFMT.Builder vBuilder = VertexFMT.newBuilder().setId(
						vertex.intValue());
				dbBuilder.addVertices(vBuilder.build());
			}
		}
	}

	private final File outputFile;

	public PredicateDataSetProtoBufOutputter(File outputFile) {
		this.outputFile = outputFile;
	}

	private void createEdgeTransactions(EdgeMap edgeMap, FakeVertexMap vertexMap,
			Run[] cfgs, String methodName, TransactionListFMT.Builder listBuilder) {
		for (Run cfg : cfgs) {
			List<PredicateItem> edges = cfg.getAllItems();
			if (edges.size() == 0) {
				continue;
			}
			TransactionFMT.Builder txBuilder = TransactionFMT.newBuilder();
			txBuilder.setLabel(cfg.getLabel());
			txBuilder.setExecutionId(cfg.getId());
			for (PredicateItem edge : edges) {
				if (!edge.getPredicateStatus().equals(PredicateValue.TRUE))
					continue;
				final int id = edge.getKey().getId();
				// vertexMap.getId(id);
				// vertexMap.getId(id);
				vertexMap.add(id);

				final int edgeId = edgeMap.createId(id, id, id);
				txBuilder.addItems(edgeId);
			}
			listBuilder.addTransactions(txBuilder.build());
		}
	}

	@Override
	protected void processPredicateDataSet(PredicateDataSet dataset, int[] statistics) {
		TransactionDBFMT.Builder dbBuilder = TransactionDBFMT.newBuilder();
		EdgeMap edgeMap = new EdgeMap();
		FakeVertexMap vertexMap = new FakeVertexMap();

		for (String methodName : dataset.getFunctionNames()) {
			TransactionListFMT.Builder listBuilder = TransactionListFMT.newBuilder();
			this.createEdgeTransactions(edgeMap, vertexMap,
					dataset.project(methodName), methodName, listBuilder);
			dbBuilder.addLists(listBuilder.build());
		}

		// vertex transactions;
		for (Run run : dataset.getRuns()) {
			TransactionFMT.Builder txBuilder = TransactionFMT.newBuilder();
			txBuilder.setLabel(run.getLabel());
			txBuilder.setExecutionId(run.getId());
			for (PredicateItem i : run.getAllItems()) {
				if (!i.getPredicateStatus().equals(PredicateValue.TRUE)) {
					continue;
				}
				txBuilder.addItems(i.getKey().getId());
			}
			dbBuilder.addVertexTransactions(txBuilder.build());
		}

		edgeMap.save(dbBuilder);
		vertexMap.save(dbBuilder);

		dbBuilder.setPositiveSupport(dataset.getPositive());
		dbBuilder.setNegativeSupport(dataset.getNegative());

		if (!this.outputFile.getParentFile().exists()) {
			this.outputFile.getParentFile().mkdir();
		}
		try {
			FileOutputStream os = new FileOutputStream(this.outputFile);
			TransactionDBFMT db = dbBuilder.build();

			System.out.println("edge count = " + db.getEdgesCount());
			System.out.println("vertex count = " + db.getVerticesCount());
			
			//added to get the count info
			statistics[3] = db.getVerticesCount();

			db.writeTo(os);
			os.close();
			System.out.println("outputting minus dataset to " + this.outputFile);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
