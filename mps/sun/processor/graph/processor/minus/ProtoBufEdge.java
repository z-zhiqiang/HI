package sun.processor.graph.processor.minus;

public class ProtoBufEdge {

	private int source;

	private int target;

	private int attribute;

	public int getSource() {
		return source;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<Edge ").append(this.source).append("->").append(this.target).append(":").append(this.attribute)
				.append(">");
		return builder.toString();
	}

	public ProtoBufEdge(int source, int target, int attribute) {
		super();
		this.source = source;
		this.target = target;
		this.attribute = attribute;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + attribute;
		result = prime * result + source;
		result = prime * result + target;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProtoBufEdge other = (ProtoBufEdge) obj;
		if (attribute != other.attribute)
			return false;
		if (source != other.source)
			return false;
		if (target != other.target)
			return false;
		return true;
	}

	public int getTarget() {
		return target;
	}

	public int getAttribute() {
		return attribute;
	}

}
