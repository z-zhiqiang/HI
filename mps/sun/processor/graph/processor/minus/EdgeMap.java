package sun.processor.graph.processor.minus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.sun.dim.fileformat.v3.ItemsetFileFormat.EdgeFMT;
import org.sun.dim.fileformat.v3.ItemsetFileFormat.TransactionDBFMT;

public class EdgeMap {

	private int id;

	private Map<ProtoBufEdge, Integer> map;

	public EdgeMap() {
		this.map = new HashMap<ProtoBufEdge, Integer>();
		id = 0;
	}

	public int createId(int source, int target, int attribute) {
		ProtoBufEdge edge = new ProtoBufEdge(source, target, attribute);
		Integer i = this.map.get(edge);
		// if (i == null) {
		// i = new Integer(id++);
		// this.map.put(edge, i);
		// return i.intValue();
		// } else {
		// throw new RuntimeException("this edge has been created." + edge);
		// }
		if (i == null) {
			i = new Integer(id++);
			this.map.put(edge, i);
		}
		return i.intValue();
	}

	public void save(TransactionDBFMT.Builder dbBuilder) {
		ArrayList<ProtoBufEdge> edges = new ArrayList<ProtoBufEdge>(map.size());
		for (int i = 0; i < map.size(); i++) {
			edges.add(null);
		}
		for (Map.Entry<ProtoBufEdge, Integer> entry : map.entrySet()) {
			int edgeId = entry.getValue().intValue();
			edges.set(edgeId, entry.getKey());
		}
		for (int i = 0; i < edges.size(); i++) {
			ProtoBufEdge edge = edges.get(i);
			assert (i == this.map.get(edge).intValue());
			EdgeFMT.Builder edgeBuider = EdgeFMT.newBuilder()
					.setSource(edge.getSource()).setTarget(edge.getTarget())
					.setAttribute(edge.getAttribute());
			dbBuilder.addEdges(edgeBuider.build());
		}
	}

}
