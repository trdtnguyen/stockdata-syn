package dtn.stockmatch.util;

import java.util.Comparator;

public class PointYAscendingComparator implements Comparator<Point>{
	public int compare(Point p1, Point p2) {
		return (int)(p1.y - p2.y);
	}

}
