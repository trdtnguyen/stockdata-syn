package dtn.stockmatch.pip;

import java.util.ArrayList;




public class PIPTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//SBTree tree = null;
/*	
		ArrayList<Point> points = new ArrayList();
		points.add(new Point(56, 43));
		points.add(new Point(90, 68));
		points.add(new Point(123, 55));
		points.add(new Point(157, 119));
		points.add(new Point(192, 141));
		points.add(new Point(225, 105));
		points.add(new Point(259, 119));
		points.add(new Point(294, 68));
		points.add(new Point(328, 57));
		points.add(new Point(363, 92));
		//points.add(new Point(397,78));//11th point that doesn't change the tree
		//points.add(new Point(397,137));//11th point that change the tree at point 4
		//points.add(new Point(397,39));//11th point that change the tree at point 8
		tree = new SBTree(points);
*/		
		
		//System.out.println(tree.printBFSTree());
		String filename = "tree.dat";
		//tree.writeToFile(filename);
		
		SBTree tree = new SBTree();
		tree.readFromFile(filename);
		
		
		
		int start = 0;
		int end = tree.getSize() - 1;
		int nPIPs = tree.getSize();
		
		ArrayList<Node> nl = tree.retrieveSubsequence(start, end, nPIPs);
		System.out.println("\nRead from file:");
		for(Node node : nl){
			System.out.println(" " + node.id + ":" + node.dist);
		}
		System.out.println(" ");
/*
		
		int nPIPs = points.size();
		ArrayList<Integer> indices = tree.retrieveSubsequence(start, end, nPIPs);
		System.out.println("\nRetrieve Subsequence (" + start + "," + end + "), nPIPs="+nPIPs);
		for(Integer i : indices){
			System.out.print(" " + i);
		}
*/

		
		//change y as: 78, 137, 39
		double x = 397;
		double y = 39;
//		tree.point_by_point_update(new Point(x,y));
		System.out.println("Add point (" + x + "," + y +")");
		//System.out.println(tree.printBFSTree());
		//System.out.println(tree.printOrder());
		
		start = 5;
		end = tree.getSize() - 1;
		nPIPs = tree.getSize();
		nl = tree.retrieveSubsequence(start, end, nPIPs);
		System.out.println("\nRetrieve Subsequence (" + start + "," + end + "), nPIPs="+nPIPs);
		for(Node node : nl){
			System.out.print(" " + node.id + ":" + node.dist);
		}
		
	}

}
