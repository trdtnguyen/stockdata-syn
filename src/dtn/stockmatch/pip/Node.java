package dtn.stockmatch.pip;

import java.io.Serializable;
import java.util.Date;

import dtn.stockmatch.pip.Node;

public class Node implements Serializable{
	public int id;
	public double x;
	public double y;
	public Node left;
	public Node right;
	public double dist;
}
