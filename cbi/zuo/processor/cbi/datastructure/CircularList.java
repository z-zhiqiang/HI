package zuo.processor.cbi.datastructure;

public class CircularList{
	private final int maxSize;
	private final FixPointStructure[] list;
	private int numberOfElements;
	private int index;
	
	public CircularList(int size){
		this.maxSize = size;
		this.list = new FixPointStructure[maxSize];
		this.numberOfElements = 0;
		this.index = -1;
	}
	
	public void insertElement(FixPointStructure structure){
		index = (index + 1) % maxSize;
		list[index] = structure;
		numberOfElements++;
	}
	public FixPointStructure getCurrentElement(){
		return list[index];
	}
	public FixPointStructure getPreviousKthElement(int k){
		assert(k < maxSize);
		return list[(index + maxSize - k) % maxSize];
	}
	
	public int getNumberOfElements(){
		return numberOfElements;
	}
	public boolean isEmpty(){
		return (numberOfElements == 0);
	}
	public int getIndex(){
		return index;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public FixPointStructure[] getList() {
		return list;
	}
	
}
