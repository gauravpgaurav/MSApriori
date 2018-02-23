public class ItemNode {

	private String itemValue;
	private Double mis;
	private int count;

	public ItemNode() {
		count = 0;
	}

	public ItemNode(String itemValue) {
		super();
		this.itemValue = itemValue;
		count = 0;
	}

	public ItemNode(String itemValue, double mis) {
		super();
		this.itemValue = itemValue;
		this.mis = mis;
		count = 0;
	}

	public String getItemValue() {
		return itemValue;
	}

	public void setItemValue(String itemValue) {
		this.itemValue = itemValue;
	}

	public Double getMis() {
		return mis;
	}

	public void setMis(Double mis) {
		this.mis = mis;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void inc_count() {
		count++;
	}

	public Double calculate_support(int numberOfItems) {
		Double itemSupport = ((double) count) / numberOfItems;
		return itemSupport;
	}

	public boolean isItemValuePosNum() {
		return itemValue.matches("\\d+");
	}
}
