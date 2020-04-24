package dtn.stockmatch.pip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

import dtn.stockmatch.util.MetricUtil;
import dtn.stockmatch.util.Point;


public class SBTree implements Serializable{
	
	private int mSize;
	//original points, need to store in memory for update task
	private ArrayList<Point> P;
	private Node mRoot;
	
	public SBTree(){
		mSize = 0;
		P = new ArrayList<>();;
		mRoot = null;
	}
	public SBTree(ArrayList<Point> inputP){
		P = new ArrayList<Point>();
		P.addAll(inputP);
		mSize = inputP.size();
		mRoot = createTree(0, P.size()-1, P.size());
	}
	/**
	 * Create tree which nPIPs nodes and start < nodeID < end
	 * */
	private Node createTree(int start, int end, int nPIPs){
		int maxVDId = -1;
		double maxVD = 0;
		long cnt = 0;
		Node prevNode = null;
		//Last data point
		
		Node root = new Node();
		root.id = end;
		root.x = P.get(end).x;
		root.y = P.get(end).y;
		root.left = null;
		root.right = null;
		root.dist = 0;
		cnt++;
		
		
		//First data point
		Node nextRoot = new Node();//child node
		nextRoot.id = start;
		nextRoot.x = P.get(start).x;
		nextRoot.y = P.get(start).y;
		nextRoot.left = null;
		nextRoot.right = null;
		nextRoot.dist = 0;
		
		cnt++;
		
		root.left = nextRoot;
		prevNode = nextRoot;
		
		Node pnode = nextRoot;//parent node
		
		//find third point
		for(int i = start + 1; i < end; i++){
			double VD = MetricUtil.VD(P.get(start), P.get(end), P.get(i));
			if(VD > maxVD) {
				maxVD = VD;
				maxVDId = i;
			}
		}
		if(maxVD == 0){
			//all segments are the line
			maxVDId = ((end - 1) + (start+1))/2;
		}
		//Add first segment to queue
		int defaultQueueSize = mSize;
		Queue<PIPSegment> queue = new PriorityQueue<PIPSegment>(defaultQueueSize, 
				new Comparator<PIPSegment>(){
			public int compare(PIPSegment seg1, PIPSegment seg2){
				if(seg2.maxVD > seg1.maxVD) return 1;
				else if(seg2.maxVD == seg1.maxVD) return 0;
				else return -1;
				//return (int) (seg2.maxVD - seg1.maxVD);
			}
		});
		queue.add(new PIPSegment(start, end, maxVDId, maxVD));
		//repeat until find nPIPs
		while(cnt < nPIPs){
			pnode = nextRoot;//restart the original position
			//Select point with maxVD to adjacent PIPs points
			PIPSegment seg = queue.remove();
			
			//Create node
			Node cnode = new Node();
			cnode.id = seg.maxVDId;
			cnode.x = P.get(cnode.id).x;
			cnode.y = P.get(cnode.id).y;
			cnode.dist = seg.maxVD;
			cnode.left = null;
			cnode.right = null;
			cnt++;
			
			
			//Repeat until find the placing position
			while(true){
				if(cnode.id < pnode.id){
					if(pnode.left == null){
						pnode.left = cnode; //position found
						prevNode = cnode;
//						System.out.println("Add node " + cnode.id + " as left child of node " + pnode.id);
						break;
					}
					else
						pnode = pnode.left;
				}
				else{
					if(pnode.right == null){
						pnode.right = cnode; //position found
						prevNode = cnode;
//						System.out.println("Add node " + cnode.id + " as right child of node " + pnode.id);
						break;
					}
					else
						pnode = pnode.right;
				}
			}
			//Create new segments
			if(seg.maxVDId - seg.start > 1){
				//find PIP
				maxVD = 0;
				for(int i = seg.start + 1; i < seg.maxVDId; i++){
					double VD = MetricUtil.VD(P.get(seg.start), P.get(seg.maxVDId), P.get(i));
					if(VD > maxVD) {
						maxVD = VD;
						maxVDId = i;
					}
				}
				if(maxVD == 0){
					//all segments are the line
					maxVDId = ((seg.maxVDId - 1) + (seg.start + 1))/2;
				}
				PIPSegment newSeg = new PIPSegment(seg.start, seg.maxVDId, maxVDId, maxVD);
				queue.add(newSeg);
			}
			if(seg.end - seg.maxVDId > 1){
				//find PIP
				maxVD = 0;
				for(int i = seg.maxVDId + 1; i < seg.end; i++){
					double VD = MetricUtil.VD(P.get(seg.maxVDId), P.get(seg.end), P.get(i));
					if(VD > maxVD) {
						maxVD = VD;
						maxVDId = i;
					}
				}
				if(maxVD == 0){
					//all segments are the line
					maxVDId = ((seg.end - 1) + (seg.maxVDId+1))/2;
				}
				PIPSegment newSeg = new PIPSegment(seg.maxVDId, seg.end, maxVDId, maxVD);
				queue.add(newSeg);
			}
		}//end repeat until find nPIPs
		return root;
	}
	/**
	 * Point by point update approach
	 * Input: new point
	 * Output: none
	 * */
	public void point_by_point_update(Point newPoint){
		P.add(newPoint);
		mSize++;
		Node newNode = new Node();
		newNode.id = mSize - 1;
		newNode.left = mRoot.left;
		newNode.right = null;
		
		
		//Remove the current root
		Node endNode = mRoot;
		endNode.left = endNode.right = null;
		
		mRoot = newNode;
		int position = findRebuildPosition(mRoot.left, newNode);
		
		
		Node pnode = mRoot.left;
		if(position == -1){//find the most right null child then insert the newNode in
			while(pnode.right != null){
				pnode = pnode.right;
			}
			pnode.right = endNode;
			//Update more info for endNode
			
			double VD =  MetricUtil.VD(P.get(pnode.id), P.get(mRoot.id), P.get(endNode.id));
			endNode.dist = VD;
		}
		else{
			while(pnode.id != position){
				pnode = pnode.right;
			}
			int subTreeSize = newNode.id - pnode.id + 1;
			Node newRootTree = createTree(pnode.id, newNode.id, subTreeSize);
			pnode.right = newRootTree.left.right;
		}
	}
	/**
	 *Retrieve Subsequence In-order travel SBTree from root to retrieve nPIPs points.
	 *Input: SBTree root: root of the subtree
	 *		start, end: range of the points in subtree
	 *		nPIPs: number of PIPs
	 *Output: Node List L[1...nPIPs]
	 * */
	public ArrayList<Node> retrieveSubsequence(int start, int end, int nPIPs){
		ArrayList<Node> nodeList = new ArrayList<Node>();
		int defaultQueueSize = mSize;
		Queue<Node> queue = new PriorityQueue<Node>(defaultQueueSize, 
				new Comparator<Node>(){
			public int compare(Node node1, Node node2){
				if(node2.dist > node1.dist) return 1;
				else if(node2.dist == node1.dist) return 0;
				else return -1;
				//return (int) (seg2.maxVD - seg1.maxVD);
			}
		});
		int cnt = 0;
		
		queue.add(mRoot);
		while(cnt < nPIPs && !queue.isEmpty()){
			Node node = queue.remove();
			if(start <= node.id && node.id <= end){
				nodeList.add(node);
				cnt++;
			}
			if(node.left != null)
				queue.add(node.left);
			if(node.right != null)
				queue.add(node.right);
		}
		return nodeList;
	}
	/**
	 *Retrieve Subsequence
	 *Input: SBTree root: root of the subtree
	 *		start, end: range of the points in subtree
	 *		nPIPs: number of PIPs
	 *Output: List L[1...nPIPs] are indices
	 * */
	public ArrayList<Integer> retrieveSubsequenceIndices(int start, int end, int nPIPs){
		ArrayList<Integer> nodeList = new ArrayList<Integer>();
		int defaultQueueSize = mSize;
		Queue<Node> queue = new PriorityQueue<Node>(defaultQueueSize, 
				new Comparator<Node>(){
			public int compare(Node node1, Node node2){
				if(node2.dist > node1.dist) return 1;
				else if(node2.dist == node1.dist) return 0;
				else return -1;
				//return (int) (seg2.maxVD - seg1.maxVD);
			}
		});
		int cnt = 0;
		
		queue.add(mRoot);
		while(cnt < nPIPs && !queue.isEmpty()){
			Node node = queue.remove();
			if(start <= node.id && node.id <= end){
				nodeList.add(node.id);
				cnt++;
			}
			if(node.left != null)
				queue.add(node.left);
			if(node.right != null)
				queue.add(node.right);
		}
		return nodeList;
	}
	public ArrayList<Integer> retrieveSubsequence2(Node root, int start, int end, int nPIPs){
		ArrayList<Integer> indices = new ArrayList<Integer>();
		int cnt = 0;
		//init marked
		HashMap<Integer, Boolean> visited = new HashMap<Integer, Boolean>();
		int size = end - start + 1;
		for(int i = start; i <= end; i++){
			visited.put(i, false);
		}
		
		Node node = new Node();
		node.dist = -1;
		while(cnt < nPIPs){
			node = findNextPIPConstraint(root, start, end, node, visited);
			indices.add(node.id);
			visited.put(node.id, true);//marked as VISITED
			cnt++;
		}
		
		return indices;
	}
	/**
	 * used in update functions
	 * Input: cnode the root of the subtree
	 * 		newPoint: new point need to be inserted in tree
	 * Output: the node which is the root of the sub-tree need to rebuild
	 * */
	private int findRebuildPosition(Node cnode, Node newNode){
		Node rebuildNode = null;
		Node oldMaxVDNode = cnode.right;//only the right sub-tree be changed when insert
		//find the current maxVD node from cnode.id to newNode.id
		int maxVDId = cnode.id;
		double maxVD = 0;
		for(int i = cnode.id + 1; i < newNode.id; i++){
			double VD = MetricUtil.VD(P.get(cnode.id), P.get(newNode.id), P.get(i));
			if(VD > maxVD) {
				maxVD = VD;
				maxVDId = i;
			}
		}
		//If the new MaxVD is different with the previous one
		if(oldMaxVDNode.id != maxVDId)
			return cnode.id;
		//Update the distance
		oldMaxVDNode.dist = maxVD;
		if(oldMaxVDNode.right == null)//find out the position to insert
			return -1;
		//
		return findRebuildPosition(oldMaxVDNode, newNode);
	}
	/**
	 * used in retrieveSubsequence function
	 * Input: Node curnode
	 * 		  int start
	 * 		  int end
	 * 		  Node nextNode
	 * Output: nextNode
	 * */
	private Node findNextPIPConstraint(Node curNode, int start, int end, Node nextNode, HashMap<Integer, Boolean> visited){
		boolean temp = visited.get(curNode.id);
		if(!visited.get(curNode.id) && (start <= curNode.id && curNode.id <= end)){
			if(curNode.dist > nextNode.dist)
				return curNode;
		}
		else{
			if(curNode.left != null){
				return findNextPIPConstraint(curNode.left, start, end, nextNode, visited);
			}
			if(curNode.right != null){
				return findNextPIPConstraint(curNode.right, start, end, nextNode, visited);
			}
		}
		return null;
	}
	public String writeToFile(String filename){
		String status = "";
		
		File file = new File(filename);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			//ArrayList<Node> nodeList = retrieveSubsequence(0, mSize - 1, mSize);
			//oos.writeObject(nodeList);
			oos.writeInt(mSize);
			oos.writeObject(mRoot);
			oos.flush();
			oos.close();
			
			return "load file successful";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "cannot load file";
		}
	}
	public ArrayList<Node> readFromFile(String filename){
		String status = "";
		File file = new File(filename);
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			ArrayList<Node> nodeList = new ArrayList<Node>();
			mSize = ois.readInt();
			mRoot = (Node)ois.readObject();
			nodeList = retrieveSubsequence(0, mSize - 1, mSize);
			//Create P
			P = new ArrayList();
			for(int i = 0; i < mSize; i++){
				P.add(null);
			}
			for(Node node : nodeList){
				P.set(node.id, new Point(node.x, node.y));
			}
			return nodeList;
/*			
			nodeList = (ArrayList<Node>) ois.readObject();
			ois.close();
			//build the tree
			mSize = 0;
			mRoot = null;
			P = new ArrayList(nodeList.size());
			for(int i = 0; i < nodeList.size(); i++){
				P.add(null);
			}
			//P = new ArrayList(nodeList.size());
			for(Node node : nodeList){
				addNode(node);
			}
			return nodeList;
			*/
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	public void addNode(Node node){
		if(mRoot == null){
			mRoot = node;
			
		}
		else{
			Node pnode = mRoot;
			while(pnode != null){
				if(node.id < pnode.id){
					if(pnode.left == null){
						pnode.left = node;
						break;
					}
					else
						pnode = pnode.left;
				}
				else if(node.id > pnode.id){
					if(pnode.right == null){
						pnode.right = node;
						break;
					}
					else
						pnode = pnode.right;
				}
			}
			
		}
		mSize++;
		P.set(node.id, new Point(node.x, node.y));
		
		//P.add(new Point(node.x, node.y));
		
	}
	public Node getRoot(){
		return mRoot;
	}
	public int getSize(){
		return mSize;
	}
	//for testing
	public String printBFSTree(){
		String res = "";
		Queue<Node> queue = new ArrayBlockingQueue<Node>(mSize);
		queue.add(mRoot);
		while(!queue.isEmpty()){
			Node node = queue.remove();
			res = res + " " + node.id;
			if(node.left != null) queue.add(node.left);
			if(node.right != null) queue.add(node.right);
		}
		System.out.println("BFS Order==================================");
		return res;
	}
}
