package dtn.stockmatch.util;

import java.util.Comparator;



public class Point {
	public int id;
	public int o_id;//original index, only used for piecewise
	public double x;
	public double y;
	
	public Point(double x, double y){
		this.x = x;
		this.y = y;
	}
	public Point(int id, double x, double y){
		this.id = id;
		this.x = x;
		this.y = y;
	}
	public Point(int id, int o_id, double x, double y){
		this.id = id;
		this.o_id = o_id;
		this.x = x;
		this.y = y;
	}
	public Point(Point p){
		this.x = p.x;
		this.y = p.y;
	}
	public void copy(Point p){
		this.x = p.x;
		this.y = p.y;
	}
}
