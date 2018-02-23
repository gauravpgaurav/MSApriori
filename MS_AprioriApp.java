import java.io.PrintWriter;
import java.util.ArrayList;

public class MS_AprioriApp {

	public static void main(String[] args) {

		String localDir = System.getProperty("user.dir");
		String paramFilePath = new String(localDir + "/data/parameter-file.txt");
		String inputFilePath = new String(localDir + "/data/input-data.txt");
		String outputFilePath = new String(localDir + "/output/output-data.txt");
		clearFile(outputFilePath);
		/*
		 * ***************************** START *****************************
		 */
		/* Parameter File is parsed to get MIS values, SDC & Constraints */
		ArrayList<ItemNode> ms = ItemUtil.get_MS_Values(paramFilePath);
		/* Items are sorted in ascending order of their MIS values */
		ArrayList<ItemNode> m = ItemUtil.sort_MS_Values(ms);
		/*
		 * Count of all items are updated after scanning the Transaction/Input
		 * data
		 */
		ItemUtil.get_item_count(inputFilePath, m);
		ArrayList<ItemNode> l = ItemUtil.init_pass(m);
		/* Frequent Itemset of size 1 is generated */
		ArrayList<LevelNode> f1 = ItemUtil.generate_freq_itemset_1(l);

		/* Frequent Item Lists & Candidate item Lists */
		ArrayList<ArrayList<LevelNode>> freqItemList = new ArrayList<ArrayList<LevelNode>>();
		ArrayList<ArrayList<LevelNode>> candItemList = new ArrayList<ArrayList<LevelNode>>();

		/* To place f1 at '1' index position of freqItemList */
		freqItemList.add(new ArrayList<>());

		if (f1.isEmpty()) {
			/* If no frequent item sets exist in f1 */
			ItemUtil.print_LevelNode_Array(f1, outputFilePath);
		}
		freqItemList.add(f1);

		int k = 2;
		while (!freqItemList.get(k - 1).isEmpty()) {
			if (k == 2) {
				/* C2 Level Candidates generated from set L */
				ArrayList<LevelNode> c2 = ItemUtil.level2_candidate_gen(l);
				candItemList.add(c2);
			} else {
				/* Ck Level Candidates generated from set Fk-1 */
				ArrayList<LevelNode> ck = ItemUtil.levelk_candidate_gen(freqItemList.get(k - 1), k);
				candItemList.add(ck);
			}
			/* Count & Tail-count of candidates updated */
			ItemUtil.check_candidates_with_transactions(inputFilePath, candItemList.get(k - 2));
			/* Fk generated from the candidates from Ck */
			ArrayList<LevelNode> fk = ItemUtil.generate_freq_itemset_k(candItemList.get(k - 2));
			freqItemList.add(fk);

			/* Iterate to next level */
			k++;
		}

		for (int i = 1; i < freqItemList.size(); i++) {
			/* Constraints applied on all Frequent Item sets before printing */
			ItemUtil.apply_constraint_must_have(freqItemList.get(i));
			ItemUtil.apply_constraint_cannot_be_together(freqItemList.get(i));
			/* Finally, all Frequent Item sets are printed */
			if (!freqItemList.get(i).isEmpty()) {
				ItemUtil.print_LevelNode_Array(freqItemList.get(i), outputFilePath);
			}
		}

		/*
		 * ***************************** END *****************************
		 */
	}

	/**
	 * This method is used to clear all contents of the given file located at
	 * 'filePath'
	 * 
	 * @param filePath
	 */
	public static void clearFile(String filePath) {
		try {
			PrintWriter writer;
			writer = new PrintWriter(filePath);
			writer.print("");
			writer.close();
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", filePath);
			e.printStackTrace();
		}
	}
}
