import java.util.ArrayList;
import java.util.Iterator;

public class LevelNode {

	private ArrayList<ItemNode> itemSet;
	private int level;
	private String altRep;
	private int count;
	private int t_count;

	public LevelNode() {
		count = 0;
		t_count = 0;
		itemSet = new ArrayList<>();
	}

	public ArrayList<ItemNode> getItemSet() {
		return itemSet;
	}

	public void setItemSet(ArrayList<ItemNode> itemSet) {
		this.itemSet = itemSet;
		generateAltRep();
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getAltRep() {
		return altRep;
	}

	public void setAltRep(String altRep) {
		this.altRep = altRep;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getT_count() {
		return t_count;
	}

	public void setT_count(int t_count) {
		this.t_count = t_count;
	}

	public void inc_count() {
		count++;
	}

	public void inc_t_count() {
		t_count++;
	}

	public void generateAltRep() {
		Iterator<ItemNode> itr = itemSet.iterator();
		String altStr = "{";
		while (itr.hasNext()) {
			ItemNode tempItem = itr.next();
			altStr += (tempItem.getItemValue() + ", ");
		}
		altStr = altStr.substring(0, altStr.length() - 2) + "}";
		setAltRep(altStr);
	}
}
