/**
 * author: Trong-Dat Nguyen
 * Metric Util
 * */
package dtn.stockmatch.util;

import java.util.ArrayList;
import java.util.Vector;

public class MetricUtil {
	//the fixed number: 750, 456 is calculated from 
	//standard mobile screen (Nexus 7)for best Piecewise representation
	 public static double computeXScale(double xMin, double xMax){
		 double xScale = (double)750 / (xMax - xMin);
		 return xScale;
	 }
	 public static double computeYScale(double yMin, double yMax){
		 double yScale = (double)(456 / (yMax - yMin));
		 return yScale;
	 }
	 
	 public static int furthestPoint(Vector<Point> points, int startIndex, int endIndex, double epsilon){
			int index = 0;
			int lenght = points.size();
			
			double max = 0;
			
			for(index = startIndex; index < endIndex; index++){
				double d = getDistance(points.get(startIndex), points.get(endIndex), points.get(index));
				if (d > max){
					max = d;
				}	
			}
			//If the max distance is too near, then return the negative value
			if (max <= epsilon)
				index = -1;
			
			return index;
		}
	 /**
	  * Euclidean distance from p3 to segment(p1,p2)
	  * */
	 	public static double ED(Point p1, Point p2, Point p3){
	 		double d = 0.0f;
	 		
	 		double d13 = Math.sqrt((p1.x - p3.x) * (p1.x - p3.x) + (p1.y - p3.y) * (p1.y - p3.y));
	 		double d23 = Math.sqrt((p2.x - p3.x) * (p2.x - p3.x) + (p2.y - p3.y) * (p2.y - p3.y));
	 		d = d13 + d23;
	 		return d;
	 	}
	 	/**
	 	 * Perpendicular distance from p3 to segment(p1,p2) 
	 	 * */
	 	public static double PD(Point p1, Point p2, Point p3){
	 		return getDistance(p1, p2, p3);
	 	}
	 	/**
	 	 * Vertical distance VD from p3 to segment(p1,p2)
	 	 * */
	 	public static double VD(Point p1, Point p2, Point p3){
	 		double d = 0.0f;
	 		d = Math.abs((p1.y + (p2.y - p1.y)*(p3.x - p1.x)/(p2.x - p1.x)) - p3.y);
	 		return d;
	 	}
		public static double getDistance(Point p1, Point p2){
			double d = 0.0f;
			double dx21 = (p2.x - p1.x);
			double dy21 = (p2.y - p1.y);
			return Math.sqrt(dx21 * dx21 + dy21* dy21);
		}
		/**distance d from p to section p1p2*/
		public static double getDistance(Point p1, Point p2, Point p){
			double d = 0.0f;
			
			double dx21 = (p2.x - p1.x);
			double dy21 = (p2.y - p1.y);
			double temp1 = Math.abs(dx21 * (p1.y - p.y) - (p1.x - p.x) * dy21);
			double temp2 = Math.sqrt(dx21 * dx21 + dy21 * dy21);
			
			d = temp1 / temp2;
			return d;
		}
		/**cosin of angle from three points with p2 is vertex*/
		public static double getCosin(Point p1, Point p2, Point p3){
			double cos = 0;
			double d21 = getDistance(p2, p1);
			double d23 = getDistance(p2, p3);
			double d13 = getDistance(p1, p3);
			
			double arccos = (d21*d21 + d23*d23 - d13*d13) / (2 * d21 * d23);
			return Math.acos(arccos);
			
		}
		public static double[] getBoundedRect(ArrayList<Point> points){
			double[] rect = new double[4];//minX, minY, maxX, maxY
			double minX = Double.MAX_VALUE;
			double maxX = -1;
			double minY = Double.MAX_VALUE;
			double maxY = -1;
			for(Point p : points){
				if(p.x < minX) minX = p.x;
				if(p.x > maxX) maxX = p.x;
				if(p.y < minY) minY = p.y;
				if(p.y > maxY) maxY = p.y;
			}
			rect[0] = minX;
			rect[1] = minY;
			rect[2] = maxX;
			rect[3] = maxY;
			return rect;
		}
		
		/*Compute dynamic time-warping*/
		public static float DTW(float[] S, float[] Q){
			int m, n;
			n = S.length;
			m = Q.length;
			float[][] M = new float[n][m];
			//computes M[1][j], 2<= j <= m
			M[0][0] = Math.abs(S[0] - Q[0]);

			for(int j = 1; j < m; j++){
				M[0][j] = Math.max(Math.abs(S[0] - Q[j]),M[0][j-1]);

			}
			//computes M[i][1], 2 <= i <= n
			for(int i = 1; i < n; i++){
				M[i][0] = Math.max(Math.abs(S[i] - Q[0]), M[i-1][0]);

			}
			//compute remain values
			for(int i = 1; i < n; i++){
				for(int j = 1; j < m; j++){
					float val1 = Math.abs(S[i] - Q[j]);
					float val2 = Math.min(M[i-1][j], M[i][j-1]);
					val2 = Math.min(val2, M[i-1][j-1]);
					M[i][j] = Math.max(val1, val2);

				}
			}			
			return M[m-1][n-1];
		}
		public static float DTWL1(float[] S, float[] Q){
			int m, n;
			n = S.length;
			m = Q.length;
			float[][] M = new float[n][m];
			//computes M[1][j], 2<= j <= m
			M[0][0] = Math.abs(S[0] - Q[0]);
			for(int j = 1; j < m; j++){
				M[0][j] = Math.abs(S[0] - Q[j]) + M[0][j-1];
			}
			//computes M[i][1], 2 <= i <= n
			for(int i = 1; i < n; i++){
				M[i][0] = Math.abs(S[i] - Q[0]) + M[i-1][0];
			}
			//compute remain values
			for(int i = 1; i < n; i++){
				for(int j = 1; j < m; j++){
					float val1 = Math.abs(S[i] - Q[j]);
					float val2 = Math.min(M[i-1][j], M[i][j-1]);
					val2 = Math.min(val2, M[i-1][j-1]);
					M[i][j] = val1 + val2;
//					if(M[i][j] > epsilon)
//						return Float.MAX_VALUE;
				}
			}
			
			return M[m-1][n-1];
		}
}
