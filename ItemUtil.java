import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ItemUtil {

	public static int numTransactions = 0;
	public static Double supportDiffConst = 0.0;
	public static ArrayList<LevelNode> cannotBeTogether;
	public static ArrayList<ItemNode> mustHave;

	/**
	 * Extracts MIS values, SDC value & Constraints (if present) from the
	 * filePath provided
	 * 
	 * @param filePath
	 *            - parameter-file.txt
	 * @return
	 */
	public static ArrayList<ItemNode> get_MS_Values(String filePath) {

		ArrayList<ItemNode> ms = new ArrayList<>();
		try {
			FileReader input = new FileReader(filePath);
			BufferedReader reader = new BufferedReader(input);
			String parameterLine = null;
			while ((parameterLine = reader.readLine()) != null) {
				if (parameterLine.startsWith("MIS")) {
					/* MIS values extracted */
					String value = parameterLine.substring(parameterLine.indexOf('(') + 1, parameterLine.indexOf(')'));
					String mis = parameterLine.substring(parameterLine.indexOf('=') + 2, parameterLine.length());
					ItemNode item = new ItemNode(value, Double.parseDouble(mis));
					ms.add(item);
				} else if (parameterLine.startsWith("SDC")) {
					/* SDC value extracted */
					String sdc = parameterLine.substring(parameterLine.indexOf('=') + 2, parameterLine.length());
					supportDiffConst = Double.parseDouble(sdc);
				} else if (parameterLine.startsWith("cannot_be_together")) {
					/* 'Cannot Be Together' constraint parameters extracted */
					cannotBeTogether = new ArrayList<>();
					String cannotBeTogetherLine = parameterLine.substring(parameterLine.indexOf(':') + 2,
							parameterLine.length() - 1);
					String[] cannotHaveLevelNodes = cannotBeTogetherLine.replaceAll("\\s", "").split("},");
					for (int i = 0; i < cannotHaveLevelNodes.length; i++) {
						LevelNode tempLevel = new LevelNode();
						String[] cannotHaveItemNodes = cannotHaveLevelNodes[i].replace("{", "").split(",");
						for (int j = 0; j < cannotHaveItemNodes.length; j++) {
							ItemNode tempItemNode = new ItemNode(cannotHaveItemNodes[j]);
							tempLevel.getItemSet().add(tempItemNode);
							tempLevel.generateAltRep();
						}
						cannotBeTogether.add(tempLevel);
					}
				} else if (parameterLine.startsWith("must-have")) {
					/* 'Must Have' constraint parameters extracted */
					mustHave = new ArrayList<>();
					String mustHaveLine = parameterLine.substring(parameterLine.indexOf(':') + 2,
							parameterLine.length());
					String[] mustHaveParams = mustHaveLine.replaceAll("\\s", "").split("or");
					for (int i = 0; i < mustHaveParams.length; i++) {
						ItemNode temp = new ItemNode(mustHaveParams[i]);
						mustHave.add(temp);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", filePath);
			e.printStackTrace();
		}
		return ms;
	}

	/**
	 * 
	 * Sorts itemNode ArrayList ms according to ascending order of MIS values
	 * 
	 * @param ms
	 * @return
	 */
	public static ArrayList<ItemNode> sort_MS_Values(ArrayList<ItemNode> ms) {
		List<ItemNode> msList = new ArrayList<ItemNode>();
		msList = ms;
		Collections.sort(msList, new Comparator<ItemNode>() {
			public int compare(ItemNode item1, ItemNode item2) {
				int mis = item1.getMis().compareTo(item2.getMis());
				if (mis == 0) {
					return mis;
				}
				return item1.getMis() > item2.getMis() ? 1 : item1.getMis() < item2.getMis() ? -1 : 0;
			}
		});
		return ms;
	}

	/**
	 * Function to print (on console as well as in output file) the Frequent
	 * Item Set with corresponding count & items
	 * 
	 * @param levelNodeArrayList
	 * @param filePath
	 *
	 */
	public static void print_LevelNode_Array(ArrayList<LevelNode> levelNodeArrayList, String filePath) {
		try {
			boolean isAppend = true;
			int levelNumber = 1;
			if (!levelNodeArrayList.isEmpty()) {
				levelNumber = levelNodeArrayList.get(0).getLevel();
			}
			PrintWriter outputHead = new PrintWriter(new FileOutputStream(new File(filePath), isAppend));
			Iterator<LevelNode> itr = levelNodeArrayList.iterator();
			String head = "Frequent " + levelNumber + "-itemsets\n";
			if (levelNumber > 1) {
				head = "\n\n" + head;
			}
			System.out.println(head);
			outputHead.println(head);
			outputHead.close();
			while (itr.hasNext()) {
				PrintWriter outputItem = new PrintWriter(new FileOutputStream(new File(filePath), isAppend));
				LevelNode currentLevel = itr.next();
				String item = "\t" + currentLevel.getCount() + " : " + currentLevel.getAltRep();
				if (currentLevel.getLevel() > 1) {
					item += ("\nTailcount = " + currentLevel.getT_count());
				}
				System.out.println(item);
				outputItem.println(item);
				outputItem.close();
			}
			PrintWriter outputTail = new PrintWriter(new FileOutputStream(new File(filePath), isAppend));
			String tail = "\n\tTotal number of frequent " + levelNumber + "-itemsets = " + levelNodeArrayList.size();
			System.out.println(tail);
			outputTail.println(tail);
			outputTail.close();
		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", filePath);
			e.printStackTrace();
		}
	}

	/**
	 * Method to count occurrences of level node sets in the transaction list.
	 * Creates a string array of each transaction. Creates an ArrayList
	 * <String> of each level node set(value of each individual item). Calls
	 * function to see if set is in each transaction. If yes, updates the count
	 * for Level node in candidates list. Method tests each level node against
	 * one transaction, adjusting counts, then moves to the next transaction
	 * 
	 * @param filename
	 * @param candidates
	 */
	public static void check_candidates_with_transactions(String filename, ArrayList<LevelNode> candidates) {

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line, noBraces; // line with no curly braces
			String[] lineRegex; // line as array of strings without white space
								// or commas
			ArrayList<String> compareArray = new ArrayList<String>();
			LevelNode temp;

			while ((line = reader.readLine()) != null) {

				// get rid of curly braces at begining and edn
				noBraces = line.substring(1, line.length() - 1);

				// get rid of white space and commas
				lineRegex = noBraces.split(", ");

				// for each level node in candidates
				for (int x = 0; x < candidates.size(); x++) {

					temp = candidates.get(x);

					// for each item node in level node set
					for (int y = 0; y < temp.getItemSet().size(); y++) {

						// get value of single item in set
						String value = temp.getItemSet().get(y).getItemValue();

						// build set as an array list of strings
						compareArray.add(value);
					}

					// see if set is in an individual transaction
					boolean result = is_set_in_transaction(compareArray, lineRegex);

					// debugging print statement
					// System.out.println("result: " + result);

					// yes, increase count in level node
					if (result) {
						temp.inc_count();
					}

					// remove first item for tail count against transaction
					compareArray.remove(0);

					boolean result_tail = is_set_in_transaction(compareArray, lineRegex);

					// just for debugging
					// System.out.println("t_result: " + result_tail);

					if (result_tail) { // tail of set is in transaction

						// debugging print statement
						// System.out.println("adding to the tail count");
						temp.inc_t_count();
					}

					// clear the compareArray for the next level node to test

					compareArray.clear();

				} // end of level item set loop

			} // end of level node loop

			reader.close();

		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", filename);
			e.printStackTrace();

		}
	}

	/**
	 * method to check a LevelNode set against a single transaction. If each
	 * item in the set is in the transaction line, it returns true; else false
	 * 
	 * @param compareArray
	 * @param lineRegex
	 * @return
	 */
	private static boolean is_set_in_transaction(ArrayList<String> compareArray, String[] lineRegex) {

		Boolean test = false;

		for (int x = 0; x < compareArray.size(); x++) {
			String item = compareArray.get(x);
			for (int y = 0; y < lineRegex.length; y++) {
				/* if value is in transaction */
				if (item.equals(lineRegex[y])) {
					test = true;
				}
			}

			if (test == false) {
				/*
				 * if test was not turned true, one of the values in the set is
				 * not present in transaction
				 */
				return false;
			}
			/*
			 * item was in transaction; thats why we did not return false yet.
			 * reset the boolean flag
			 */
			test = false;
		}
		return true;
	}

	/**
	 * Takes the name of a file that has multiple transactions and counts the
	 * number of occurrences of individual items from the singleItemList in all
	 * transactions. Also counts total transactions and sets global variable
	 * num_transactions
	 * 
	 * @param filename
	 * @param singleItemList
	 * 
	 * @return
	 */
	public static int get_item_count(String filename, ArrayList<ItemNode> singleItemList) {

		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			/* line with no curly braces */
			String line, noBraces;
			/* line as array of strings without white space or commas */
			String[] lineRegex;

			while ((line = reader.readLine()) != null) {

				/* get rid of curly braces at beginning and end */
				noBraces = line.substring(1, line.length() - 1);

				/* get rid of white space and commas */
				lineRegex = noBraces.split(", ");

				/* for each item in list */
				for (int x = 0; x < singleItemList.size(); x++) {

					ItemNode temp = singleItemList.get(x);
					String value = temp.getItemValue();

					for (int y = 0; y < lineRegex.length; y++) {

						/* if item matches value, increment item count */
						if (value.equals(lineRegex[y])) {
							temp.inc_count();
						}
					}
				}
				numTransactions++;
			}
			reader.close();
			return 1;

		} catch (Exception e) {
			System.err.format("Exception occurred trying to read '%s'.", filename);
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 
	 * Takes two arguments, the data set T and the sorted items M, to produce
	 * the seeds L for generating candidate itemsets of length 2
	 * 
	 * @param itemNodeArray
	 * @return
	 */
	public static ArrayList<ItemNode> init_pass(ArrayList<ItemNode> itemNodeArray) {
		ArrayList<ItemNode> l = new ArrayList<>();
		Iterator<ItemNode> itr = itemNodeArray.iterator();
		Double misValue = 0.0;
		while (itr.hasNext()) {
			ItemNode currentItem = itr.next();
			Double itemSupportCount = currentItem.calculate_support(numTransactions);
			if (l.isEmpty()) {
				/*
				 * First item i in itemNodeArray that meets MIS(i). i is
				 * inserted into l
				 */
				if (itemSupportCount >= currentItem.getMis()) {
					/* if itemSupportCount >= than item MIS value */
					l.add(currentItem);
					misValue = currentItem.getMis();
				}
			} else {
				/*
				 * For each subsequent item j in itemNodeArray after i, if
				 * j.count/n >= MIS(i), then j is also inserted into l
				 */
				if (itemSupportCount >= misValue) {
					l.add(currentItem);
				}
			}
		}
		return l;
	}

	/**
	 * Method generates frequent itemsets of size 1
	 * 
	 * @param l
	 * @return
	 */
	public static ArrayList<LevelNode> generate_freq_itemset_1(ArrayList<ItemNode> l) {
		ArrayList<LevelNode> frequentItemSet1 = new ArrayList<>();
		Iterator<ItemNode> itr = l.iterator();
		while (itr.hasNext()) {
			LevelNode f1 = new LevelNode();
			ItemNode currentItem = itr.next();
			Double itemSupportCount = currentItem.calculate_support(numTransactions);
			if (itemSupportCount >= currentItem.getMis()) {
				/* if itemSupportCount >= than item MIS value */
				f1.getItemSet().add(currentItem);
				f1.setCount(currentItem.getCount());
				f1.generateAltRep();
				f1.setLevel(1);
				frequentItemSet1.add(f1);
			}
		}
		return frequentItemSet1;
	}

	/**
	 * Method generates frequent itemsets of size k
	 * 
	 * @param ck
	 * @return
	 */
	public static ArrayList<LevelNode> generate_freq_itemset_k(ArrayList<LevelNode> ck) {
		ArrayList<LevelNode> frequentItemSetk = new ArrayList<>();
		Iterator<LevelNode> itr = ck.iterator();
		while (itr.hasNext()) {
			LevelNode currentLevelNode = itr.next();
			Double levelSupportCount = ((double) currentLevelNode.getCount()) / numTransactions;
			if (levelSupportCount >= currentLevelNode.getItemSet().get(0).getMis()) {
				/* if c.count/n >= MIS(c[1]) */
				frequentItemSetk.add(currentLevelNode);
			}
		}
		return frequentItemSetk;
	}

	/**
	 * It takes an argument L, and returns a superset of the set of all frequent
	 * 2-itemsets i.e. the candidate itemsets of length 2
	 * 
	 * @param l
	 * @return
	 */
	public static ArrayList<LevelNode> level2_candidate_gen(ArrayList<ItemNode> l) {
		ArrayList<LevelNode> c2 = new ArrayList<>();
		for (int i = 0; i < l.size(); i++) {
			/* For each item 'item1 in L */
			ItemNode item1 = l.get(i);
			Double itemSupportCount1 = item1.calculate_support(numTransactions);
			if (itemSupportCount1 >= item1.getMis()) {
				/* if item1.count/n >= MIS(item1) */
				for (int j = i + 1; j < l.size(); j++) {
					/* for each item 'item2' in L that is after 'item1' */
					ItemNode item2 = l.get(j);
					Double itemSupportCount2 = item2.calculate_support(numTransactions);
					double supportDiff = item2.calculate_support(numTransactions)
							- item1.calculate_support(numTransactions);

					if ((itemSupportCount2 >= item1.getMis()) && (Math.abs(supportDiff) <= supportDiffConst)) {
						/*
						 * if item2.count/n >= MIS(item1) and |sup(item2) -
						 * sup(item1)| ≤ SDC
						 */
						LevelNode tempLevelNode = new LevelNode();
						tempLevelNode.getItemSet().add(item1);
						tempLevelNode.getItemSet().add(item2);
						tempLevelNode.generateAltRep();
						tempLevelNode.setLevel(2);
						c2.add(tempLevelNode);
					}
				}
			}
		}
		return c2;
	}

	/**
	 * This method has two parts - Join Step & Pruning Step
	 * 
	 * Join Step : Joins two frequent (k-1)- itemsets to produce a possible
	 * candidate c. Pruning Step : Determines whether all the k-1 sub- sets
	 * (there are k of them) of c are in fk_1
	 * 
	 * @param fk_1
	 * @param k
	 * @return
	 */
	public static ArrayList<LevelNode> levelk_candidate_gen(ArrayList<LevelNode> fk_1, int k) {
		/* Join Step Begins */
		ArrayList<LevelNode> ck = new ArrayList<>();
		for (int i = 0; i < fk_1.size(); i++) {
			LevelNode levelNode1 = fk_1.get(i);
			for (int j = i + 1; j < fk_1.size(); j++) {
				boolean isValidSet = false;
				LevelNode levelNode2 = fk_1.get(j);
				ArrayList<ItemNode> itemSet1 = levelNode1.getItemSet();
				ArrayList<ItemNode> itemSet2 = levelNode2.getItemSet();
				Iterator<ItemNode> levelNode1Itr = itemSet1.iterator();
				Iterator<ItemNode> levelNode2Itr = itemSet2.iterator();
				if (itemSet1.size() != itemSet2.size()) {
					break;
				}
				while (levelNode1Itr.hasNext() && levelNode2Itr.hasNext()) {
					ItemNode item1 = levelNode1Itr.next();
					ItemNode item2 = levelNode2Itr.next();
					if (item1.equals(itemSet1.get(itemSet1.size() - 1))) {
						String item1Value = item1.getItemValue();
						String item2Value = item2.getItemValue();
						int compare = item1Value.compareToIgnoreCase(item2Value);

						if (item1.isItemValuePosNum() && item2.isItemValuePosNum()) {
							// int item1IntValue = Integer.parseInt(item1Value);
							// int item2IntValue = Integer.parseInt(item2Value);
							break;
							/*
							 * if (item1IntValue > item2IntValue) { isValidSet =
							 * false; // TODO : Compare to algorithm break; }
							 */
						} else if (compare >= 0) {
							isValidSet = false;
							break;
						} else {
							double supportDiff = item1.calculate_support(numTransactions)
									- item2.calculate_support(numTransactions);
							if (Math.abs(supportDiff) > supportDiffConst) {
								isValidSet = false;
								break;
							}
						}

					} else if (item1.equals(item2)) {
						isValidSet = true;
					} else {
						isValidSet = false;
						break;
					}
				}

				LevelNode c = new LevelNode();
				if (isValidSet) {
					ArrayList<ItemNode> newItemSet = (ArrayList<ItemNode>) itemSet1.clone();
					ItemNode lastItemOfSet2 = itemSet2.get(itemSet2.size() - 1);
					newItemSet.add(lastItemOfSet2);
					c.setLevel(k);
					c.setItemSet(newItemSet);
				}
				if (!c.getItemSet().isEmpty()) {
					/* Pruning Step Begins */
					ck.add(c);
					ArrayList<LevelNode> globalSubSetsList = new ArrayList<>();
					processSubsets(c.getItemSet(), k - 1, globalSubSetsList);
					for (int g = 0; g < globalSubSetsList.size(); g++) {
						boolean checkToDeleteSet = false;
						boolean confirmDelete = false;
						ItemNode c_1 = c.getItemSet().get(0);
						double c_1_MIS = c.getItemSet().get(0).getMis();
						double c_2_MIS = c.getItemSet().get(1).getMis();
						if ((globalSubSetsList.get(g).getItemSet().contains(c_1)) || (c_1_MIS == c_2_MIS)) {
							checkToDeleteSet = true;
						}
						if (checkToDeleteSet) {
							Iterator<LevelNode> fk_1_itr = fk_1.iterator();
							while (fk_1_itr.hasNext()) {
								LevelNode temp = fk_1_itr.next();
								if (temp.getAltRep().equals(globalSubSetsList.get(g).getAltRep())) {
									confirmDelete = false;
									break;
								} else {
									confirmDelete = true;
								}
							}
							if (confirmDelete) {
								ck.remove(c);
							}
						}
					}
				}
			}
		}
		return ck;

	}

	/**
	 * TO BE TESTED : Methods to generate k-1 subsets of given set into
	 * globalSubSetsList
	 * 
	 * @param set
	 * @param k
	 * @param globalSubSetsList
	 */
	static void processSubsets(ArrayList<ItemNode> set, int k, ArrayList<LevelNode> globalSubSetsList) {
		ArrayList<ItemNode> subset = new ArrayList<>();
		for (int i = 0; i < k; i++) {
			subset.add(new ItemNode());
		}
		processLargerSubsets(set, subset, 0, 0, globalSubSetsList);
	}

	static void processLargerSubsets(ArrayList<ItemNode> set, ArrayList<ItemNode> subset, int subsetSize, int nextIndex,
			ArrayList<LevelNode> globalSubSetsList) {
		if (subsetSize == subset.size()) {
			process(subset, globalSubSetsList);
		} else {
			for (int j = nextIndex; j < set.size(); j++) {
				subset.set(subsetSize, set.get(j));
				processLargerSubsets(set, subset, subsetSize + 1, j + 1, globalSubSetsList);
			}
		}
	}

	static void process(ArrayList<ItemNode> subset, ArrayList<LevelNode> globalSubSetsList) {
		LevelNode tempLevelNode = new LevelNode();
		tempLevelNode.getItemSet().addAll(subset);
		tempLevelNode.generateAltRep();
		globalSubSetsList.add(tempLevelNode);
	}

	/**
	 * This method applies the "cannot_be_together" constraint on the k-size
	 * frequent item set passed to it
	 * 
	 * @param fk
	 */
	public static void apply_constraint_cannot_be_together(ArrayList<LevelNode> fk) {
		if (cannotBeTogether != null && !cannotBeTogether.isEmpty()) {
			Iterator<LevelNode> levelItr = fk.iterator();
			while (levelItr.hasNext()) {
				boolean itemExists = true;
				LevelNode tempLevelNode = levelItr.next();
				Iterator<LevelNode> constraintItr = cannotBeTogether.iterator();
				while (constraintItr.hasNext()) {
					LevelNode constraintLevelNode = constraintItr.next();
					Iterator<ItemNode> constraintItemItr = constraintLevelNode.getItemSet().iterator();
					while (constraintItemItr.hasNext()) {
						if (!itemExists) {
							break;
						} else {
							ItemNode constraintItem = constraintItemItr.next();
							for (int i = 0; i < tempLevelNode.getItemSet().size(); i++) {
								if (constraintItem.getItemValue()
										.equals(tempLevelNode.getItemSet().get(i).getItemValue())) {
									itemExists = true;
									break;
								} else {
									itemExists = false;
								}
							}
						}

					}
				}
				if (itemExists) {
					levelItr.remove();
				}
			}
		}
	}

	/**
	 * This method applies the "must-have" constraint on the k-size frequent
	 * item set passed to it
	 * 
	 * @param fk
	 */
	public static void apply_constraint_must_have(ArrayList<LevelNode> fk) {
		if (mustHave != null && !mustHave.isEmpty()) {
			Iterator<LevelNode> levelItr = fk.iterator();
			/* [{10,20},{30,40}] */
			while (levelItr.hasNext()) {
				/* {10,20} */
				LevelNode tempLevelNode = levelItr.next();
				boolean hasItem = false;
				Iterator<ItemNode> itemItr = tempLevelNode.getItemSet().iterator();
				while (itemItr.hasNext()) {
					/* {10} */
					ItemNode tempItemNode = itemItr.next();
					for (int i = 0; i < mustHave.size(); i++) {
						if (tempItemNode.getItemValue().equals(mustHave.get(i).getItemValue())) {
							hasItem = true;
							break;
						}
					}
					if (hasItem) {
						break;
					}
				}
				if (!hasItem) {
					levelItr.remove();
				}
			}
		}
	}
}
