package dtn.stockdatagenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

import javax.crypto.EncryptedPrivateKeyInfo;

import org.apache.commons.io.FileUtils;

import dtn.stockmatch.pip.SBTree;
import dtn.stockmatch.util.Point;
public class StockDataGenerator {
	public static final String MSG_READ_FILE_ERROR = "Read file error";
	public static final String MSG_TASK_SUCCESSFUL = "Task successful";
	
	public static final String CONFIG_FILE = "config.cnf";
	
	public static String FOLDER_SEED = "seed";
	public static String FOLDER_UNIFORM_SCALE = "us";
	public static String FOLDER_JOIN = "join";
	public static String FOLDER_TIMEWARPING_SCALE = "ws";
	public static String FOLDER_NOISE_ADD = "na";
	public static String FOLDER_PIP = "pip";
	public static String FOLDER_OUTPUT = "output";
	
	public static String START_DATE="20200421";
	
	public static int K = 4;
	public static int N = 20;
	public static double NOISE_P = 0.5;
	public static double NOISE_RANGE1 = 0.1;
	public static double NOISE_RANGE2 = 0.2;
	public static int NPIP = 10;
	public static int NSIM_POINT = 30;
	public static int TW_MODE=1;
	public static int JOIN_OFFSET=50;
	
	
	public static String FILE_ID_NAME_MAPPING = "syn_ID_NAME.txt";
	/**
	 * modes
	 * 1: Uniform scale
	 * n = (k-1) * c + 1
	 * n: number of points; c: number of segments, k: input param
	 * Params: 
	  
	 */
	public static void main(String[] args) {
		System.out.println("Read config file.");
		readConfig();
		
		String msg = "";
		int mode = Integer.parseInt(args[0]);
		
		//
		switch(mode){
			case 0:{
				/*mode=0: auto */
				delDirs();
				
				msg = uniformScale(FOLDER_SEED, FOLDER_UNIFORM_SCALE, K);
				msg = timeWarpingScale(FOLDER_UNIFORM_SCALE, FOLDER_TIMEWARPING_SCALE, N);
				msg = extractPIP(FOLDER_TIMEWARPING_SCALE, FOLDER_PIP, NPIP);
				msg = addNoises(FOLDER_PIP, FOLDER_NOISE_ADD, NOISE_P, NOISE_RANGE1, NOISE_RANGE2);
				msg = simulate(FOLDER_NOISE_ADD, FOLDER_OUTPUT, NSIM_POINT);
				
				
//				msg = uniformScale(FOLDER_SEED, FOLDER_UNIFORM_SCALE, K);
//				msg = extractPIP(FOLDER_UNIFORM_SCALE, FOLDER_PIP, NPIP);
//				msg = timeWarpingScale(FOLDER_PIP, FOLDER_TIMEWARPING_SCALE, N);
//				msg = addNoises(FOLDER_TIMEWARPING_SCALE, FOLDER_NOISE_ADD, NOISE_P, NOISE_RANGE1, NOISE_RANGE2);
//				msg = simulate(FOLDER_NOISE_ADD, FOLDER_OUTPUT, NSIM_POINT);
				
				//Cannot use join because we cannot know the found pattern is relevant or not.
//				msg = uniformScale(FOLDER_SEED, FOLDER_UNIFORM_SCALE, K);
//				msg = extractPIP(FOLDER_UNIFORM_SCALE, FOLDER_PIP, NPIP);
//				msg = joinSequences(FOLDER_PIP, FOLDER_JOIN, JOIN_OFFSET);
//				msg = timeWarpingScale(FOLDER_JOIN, FOLDER_TIMEWARPING_SCALE, N);
//				msg = addNoises(FOLDER_TIMEWARPING_SCALE, FOLDER_NOISE_ADD, NOISE_P, NOISE_RANGE1, NOISE_RANGE2);
//				msg = simulate(FOLDER_NOISE_ADD, FOLDER_OUTPUT);

				break;
			}
			case 1:{
				/*mode = 1: uniform scale 
				  params: 1 <input_seed_file> <output> <k>
				 * 
				 */
				String source = args[1];
				String dest = args[2];
				int k = Integer.parseInt(args[3]);
				msg = uniformScale(source, dest, k);
				
				break;
			}
			case 2:{
				/* mode =2: timewarping scale */
				String source = args[1];
				String dest = args[2];
				int n = Integer.parseInt(args[3]);
				msg = timeWarpingScale(source, dest, n);
			}
			case 3:{
				/*mode =3: extract PIP points*/
				
				String source = args[1];
				String dest = args[2];
				int n = Integer.parseInt(args[3]);
				msg = extractPIP(source, dest, n);
				break;
			}
			case 4:{
				/*Random walk
				parameters: 
				args[0]: mode = 4 
				args[1]:dest 
				args[2]: number of sequences to be generated 
				args[3]: length
				 * 
				 */
				String dest = args[1];
				int nSeq = Integer.parseInt(args[2]);
				int n = Integer.parseInt(args[3]);
				msg = randomWalk(dest, nSeq, n);
			}
			case 5:{
				/*generate input points */
				delDirs();
				msg = uniformScale(FOLDER_SEED, FOLDER_UNIFORM_SCALE, K);
				msg = extractPIP(FOLDER_UNIFORM_SCALE, FOLDER_PIP, NPIP);
			}
			default:{
				
			}
		}
		System.out.println(msg);
		System.out.println("Program end.");
	}
	
	/*delete all generated dir except seed */
	
	private static void delDirs(){
		File usDir = new File(FOLDER_UNIFORM_SCALE);
		File twDir = new File(FOLDER_TIMEWARPING_SCALE);
		File pipDir = new File (FOLDER_PIP);
		File naDir = new File(FOLDER_NOISE_ADD);
		File outDir = new File(FOLDER_OUTPUT);
		
		File[] dirs = {usDir, twDir, pipDir, naDir, outDir};
		boolean check;
		try {
			for(File dir : dirs){
				
				if(dir.exists()){
					FileUtils.deleteDirectory(dir);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Read parameters from the config file
	 * */
	private static void readConfig(){
		File f = new File(CONFIG_FILE);
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String line;
			
			while((line = br.readLine()) != null){
				
				/*Skip comments or empty lines*/
				if(line.length() == 0 || line.startsWith("//")){
					continue;
				}
				
				String[] fields = line.split("=");
				//indexing
				if(fields[0].compareTo("FOLDER_SEED") == 0){
					FOLDER_SEED = fields[1];
				}
				else if(fields[0].compareTo("FOLDER_UNIFORM_SCALE") == 0){
					FOLDER_UNIFORM_SCALE = fields[1];
				} 
				else if(fields[0].compareTo("FOLDER_JOIN") == 0){
					FOLDER_JOIN = fields[1];
				} 
				else if(fields[0].compareTo("FOLDER_TIMEWARPING_SCALE") == 0){
					FOLDER_TIMEWARPING_SCALE = fields[1];
				}
				else if(fields[0].compareTo("FOLDER_NOISE_ADD") == 0){
					FOLDER_NOISE_ADD = fields[1];
				}
				else if(fields[0].compareTo("FOLDER_PIP") == 0){
					FOLDER_PIP = fields[1];
				} 
				else if(fields[0].compareTo("FOLDER_OUTPUT") == 0){
					FOLDER_OUTPUT = fields[1];
				}
				else if(fields[0].compareTo("FILE_ID_NAME_MAPPING") == 0){
					FILE_ID_NAME_MAPPING = fields[1];
				}
				else if(fields[0].compareTo("K") == 0){
					K = Integer.parseInt(fields[1]);
				}
				else if(fields[0].compareTo("N") == 0){
					N = Integer.parseInt(fields[1]);
				}
				else if(fields[0].compareTo("NOISE_P") == 0){
					NOISE_P = Double.parseDouble(fields[1]);
				}
				else if(fields[0].compareTo("NOISE_RANGE1") == 0){
					NOISE_RANGE1 = Double.parseDouble(fields[1]);
				}
				else if(fields[0].compareTo("NOISE_RANGE2") == 0){
					NOISE_RANGE2 = Double.parseDouble(fields[1]);
				}
				else if(fields[0].compareTo("NPIP") == 0){
					NPIP = Integer.parseInt(fields[1]);
				}
				else if(fields[0].compareTo("TW_MODE") == 0){
					TW_MODE = Integer.parseInt(fields[1]);
				}
				else if(fields[0].compareTo("JOIN_OFFSET") == 0){
					JOIN_OFFSET = Integer.parseInt(fields[1]);
				}
				else if(fields[0].compareTo("NSIM_POINT") == 0){
					NSIM_POINT = Integer.parseInt(fields[1]);
				}
				else if(fields[0].compareTo("START_DATE") == 0){
					START_DATE = fields[1];
				}
			}//end while
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Uniform Scale. From N seed points that follow a pattern, generated finer granularity points between seed points.
	 * Input the first N seed points from the input file. 
	 * For each pair of point P1 and P2 from the seed points, generate additional K points between them using uniform scaling
	 * Finally, write (N - 1) * K  newly generated points and N seed points into the output file.  
	 * Params: 
	 *       source: seed file
	 *       dest: output result
	 *       k: The K value in Uniform scale algorithm
	 * Return: status of the task
	 * */
	private static String uniformScale(String source, String dest, int k){
		System.out.println("===== Uniform scale =====");
		String status = "";
		File parentDir = new File(source);
		
		if(!parentDir.exists() || !parentDir.isDirectory()){
			status = "Error! Directory doesn't exist or the input source is not a directory.";
			return status;
		}
		
		File outDir = new File(dest);
		if(!outDir.exists())
			outDir.mkdirs();
		
		try {
			File[] files = parentDir.listFiles();
			
			/* for each seed file*/
			for (File inFile : files) {
				String inFileName = inFile.getName();
				int ind = inFileName.lastIndexOf(".");
				String outFileName = dest + "/" + inFileName.substring(0, ind) + "_us" + k + ".txt";
//				
				/* Create the output directory */
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
				BufferedWriter bw = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(new File(outFileName))));

				/* read seed values */
				ArrayList<Point> seedPoints = new ArrayList<>();
				String line;
				
				while ((line = br.readLine()) != null) {
					if (line.length() > 0) {
						String fields[] = line.split("\t");
						double x = Double.parseDouble(fields[0]);
						double y = Double.parseDouble(fields[1]);
						Point p = new Point(x, y);
						seedPoints.add(p);
					}
				}
				/* generate uniform scale points */
				ArrayList<Point> resultPoints = new ArrayList();

				Point leftPoint = seedPoints.get(0);
				
				/*Add the first point*/
				resultPoints.add(new Point(leftPoint));

				for (int i = 1; i < seedPoints.size(); i++) {
					Point rightPoint = new Point(seedPoints.get(i));
					computeUniformScalePoints(leftPoint, rightPoint, k, resultPoints);
					resultPoints.add(new Point(rightPoint));

					leftPoint.copy(rightPoint);
				}
				// write result on file
				for (Point p : resultPoints) {
					bw.write("" + p.x + "\t" + p.y);
					bw.newLine();
				}
				br.close();
				bw.close();
//					System.out.printf("%s","Done\n");
			} // end for each file
			return MSG_TASK_SUCCESSFUL;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return MSG_READ_FILE_ERROR;
		}
	}
	/**
	 * compute and add the uniform scale points between two points (doesn't add the left point and right point
	 * Input:
	 * lP: left point
	 * rP: right point
	 * k: number of points will be generate between left point and right point
	 * Output:
	 * resultPoints
	 * */
	private static void computeUniformScalePoints(Point lP, Point rP, int k, ArrayList<Point> resultPoints){
		final double EPSILON = 0.001;
		double xOffset = (rP.x - lP.x) / (k + 1);
		double yOffset = (rP.y - lP.y) / (k + 1);
		double x = lP.x + xOffset;
		double y = lP.y + yOffset;
		//we are sure that lP.x <= rP.x
		while(rP.x - x  > EPSILON){
			resultPoints.add(new Point(x,y));
			x += xOffset;
			y += yOffset;
		}
	}
	
	//join pattern sequences into one
	private static String joinSequences(String source, String dest, int offset){
		System.out.println("===== Join Sequences =====");
		String status = "";
		File parentDir = new File(source);
		if(!parentDir.exists() || !parentDir.isDirectory()){
			status = "Error! Directory doesn't exist or the input source is not a directory.";
			return status;
		}
		File outDir = new File(dest);
		if(!outDir.exists())
			outDir.mkdirs();
		try {
			String outFileName = dest + "/" + "uni.txt";
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outFileName))));
			File[] files = parentDir.listFiles();
			
			double lastX = 0;
			double beginX = 0;
			for(File inFile : files){
				String inFileName = inFile.getName();
				int ind = inFileName.lastIndexOf(".");
				
//				System.out.printf("%s","reading " + inFileName + "...");
				//Create the output directory
				
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
					
					
					//read seed values
					ArrayList<Point> seedPoints = new ArrayList<>();
					String line;
					while((line = br.readLine()) != null){
						if(line.length() > 0){
							String fields[] = line.split("\t");
							double x = Double.parseDouble(fields[0]) + beginX;
							double y = Double.parseDouble(fields[1]);
							
							lastX = x;
							bw.write(""+ x + "\t" + y + "\n");
						}
					}
					beginX = lastX + offset;
					
					br.close();
					
//					System.out.printf("%s","Done\n");
			}//end for each file
			bw.close();
			
			return MSG_TASK_SUCCESSFUL;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return MSG_READ_FILE_ERROR;
		}
	}
	/**
	 * Params:
	 * source: File name of points that is the output of uniform scaling
	 * dest: File name to write the result on
	 * */
	private static String timeWarpingScale(String source, String dest, int n){
		System.out.println("===== Timewarping scale =====");
		String status = "";
		File parentDir = new File(source);
		
		if(!parentDir.exists() || !parentDir.isDirectory()){
			status = "Error! Directory doesn't exist or the input source is not a directory.";
			return status;
		}
		
		File outDir = new File(dest);
		if(!outDir.exists())
			outDir.mkdirs();
		
		try {
			File[] files = parentDir.listFiles();
			for(File inFile : files){
				String inFileName = inFile.getName();
				int ind = inFileName.lastIndexOf(".");
				
//				System.out.printf("%s","reading " + inFileName + "...");
				//Create the output directory
				
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));			

					/* read seed values */
					ArrayList<Point> seedPoints = new ArrayList<>();
					String line;
					while((line = br.readLine()) != null){
						if(line.length() > 0){
							String fields[] = line.split("\t");
							double x = Double.parseDouble(fields[0]);
							double y = Double.parseDouble(fields[1]);
							Point p = new Point(x, y);
							seedPoints.add(p);
						}
					}
					
					/* generate timewarping scale points */
//					ArrayList<Point> resultPoints = new ArrayList();
					Point[] resultPoints = new Point[seedPoints.size()];
					//for each sample
					for(int k = 0; k < n; k++){
//						resultPoints.clear();
						
						String outFileName = dest + "/" + inFileName.substring(0, ind)+ "_" +
						"ws" + (k + 1) + ".txt";
//						System.out.printf("%s","generate " + outFileName + "...");
						BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outFileName))));
						Point firstPoint = seedPoints.get(0);
						//add first point
//						resultPoints.add(new Point(firstPoint));
						for(int i = 0; i < seedPoints.size(); i++){
							resultPoints[i] = new Point(seedPoints.get(i));
						}
						
						
						for(int i = 1; i < seedPoints.size() - 1; i++){
							//computeTimeWarpingScale(seedPoints, i, resultPoints);
							computeTimeWarpingScale(resultPoints, i, TW_MODE);
						}
						//add last point
						//resultPoints.add(new Point(seedPoints.get(seedPoints.size() - 1)));
						
						//write result on file
						for(Point p : resultPoints){
							bw.write(""+p.x +  "\t" + p.y);
							bw.newLine();
						}
						bw.close();
//						System.out.printf("%s","Done\n");
					}
					
					br.close();
					
//					System.out.printf("%s","Done\n");
			}//end for each file
			return MSG_TASK_SUCCESSFUL;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return MSG_READ_FILE_ERROR;
		}
	}
	/**
	 * Move the point in sPoints with the distance w
	 * w is random number in range [ p[i].x - (p[i].x - p[i-1].x) / 3, p[i].x + (p[i + 1].x - p[i].x) / 3 ]
	 * Input:
	 * sPoints: source points
	 * curIndex: index of the current point that will be moved (1 <= curIndex < sPoints.size()-1)
	 * Output:
	 * resultPoints
	 * */
	private static void computeTimeWarpingScale(ArrayList<Point> sPoints, int curIndex, ArrayList<Point> resultPoints){
		//check valid index
		if(curIndex < 1 || curIndex >= sPoints.size() - 1){
			System.out.println("Index error in computeTimeWarpingScale");
			return;
		}
		
		
		double ONE_THIRD = 1.0/3;
		double TWO_THIRD = 2.0/3;
		
		double xl1 = sPoints.get(curIndex).x -
				(sPoints.get(curIndex).x - sPoints.get(curIndex - 1).x) * ONE_THIRD;
		double xl2 = sPoints.get(curIndex).x;
		double xr1 = sPoints.get(curIndex).x;
		double xr2 = sPoints.get(curIndex).x +
				(sPoints.get(curIndex + 1).x - sPoints.get(curIndex).x) * ONE_THIRD;
		double yl1 = sPoints.get(curIndex).y -
				(sPoints.get(curIndex).y - sPoints.get(curIndex - 1).y) * ONE_THIRD;
		double yl2 = sPoints.get(curIndex).y;
		double yr1 = sPoints.get(curIndex).y;
		double yr2 = sPoints.get(curIndex).y +
				(sPoints.get(curIndex + 1).y - sPoints.get(curIndex).y) * ONE_THIRD;
				
		
//		double xl1 = sPoints.get(curIndex).x -
//				(sPoints.get(curIndex).x - sPoints.get(curIndex - 1).x) * TWO_THIRD;
//		double xl2 = sPoints.get(curIndex).x -
//				(sPoints.get(curIndex).x - sPoints.get(curIndex - 1).x) * ONE_THIRD;
//		double xr1 = sPoints.get(curIndex).x +
//				(sPoints.get(curIndex + 1).x - sPoints.get(curIndex).x) * ONE_THIRD;
//		double xr2 = sPoints.get(curIndex).x +
//				(sPoints.get(curIndex + 1).x - sPoints.get(curIndex).x) * TWO_THIRD;
//		
//		double yl1 = sPoints.get(curIndex).y -
//				(sPoints.get(curIndex).y - sPoints.get(curIndex - 1).y) * TWO_THIRD;
//		double yl2 = sPoints.get(curIndex).y -
//				(sPoints.get(curIndex).y - sPoints.get(curIndex - 1).y) * ONE_THIRD;
//		double yr1 = sPoints.get(curIndex).y +
//				(sPoints.get(curIndex + 1).y - sPoints.get(curIndex).y) * ONE_THIRD;
//		double yr2 = sPoints.get(curIndex).y +
//				(sPoints.get(curIndex + 1).y - sPoints.get(curIndex).y) * TWO_THIRD;
		
//		double xl1 = sPoints.get(curIndex).x -
//				(sPoints.get(curIndex).x - sPoints.get(curIndex - 1).x) * 1;
//		double xl2 = sPoints.get(curIndex).x -
//				(sPoints.get(curIndex).x - sPoints.get(curIndex - 1).x) * TWO_THIRD;
//		double xr1 = sPoints.get(curIndex).x +
//				(sPoints.get(curIndex + 1).x - sPoints.get(curIndex).x) * TWO_THIRD;
//		double xr2 = sPoints.get(curIndex).x +
//				(sPoints.get(curIndex + 1).x - sPoints.get(curIndex).x) * 1;
//		
//		double yl1 = sPoints.get(curIndex).y -
//				(sPoints.get(curIndex).y - sPoints.get(curIndex - 1).y) * 1;
//		double yl2 = sPoints.get(curIndex).y -
//				(sPoints.get(curIndex).y - sPoints.get(curIndex - 1).y) * TWO_THIRD;
//		double yr1 = sPoints.get(curIndex).y +
//				(sPoints.get(curIndex + 1).y - sPoints.get(curIndex).y) * TWO_THIRD;
//		double yr2 = sPoints.get(curIndex).y +
//				(sPoints.get(curIndex + 1).y - sPoints.get(curIndex).y) * 1;
		
		
		//generate the random number in range (l1,l2) uniform (r1,r2)
		double newX1 = xl1 + Math.random() * (xl2 - xl1);
		double newX2 = xr1 + Math.random() * (xr2 - xr1);
		
		double newY1 = yl1 + Math.random() * (yl2 - yl1);
		double newY2 = yr1 + Math.random() * (yr2 - yr1);
		
		//random choose between x1 and x2
		int randBool = (int)(Math.random() * 10) % 2;
		double newX = randBool == 0 ? newX1 : newX2;
		double newY = randBool == 0 ? newY1 : newY2;
		
//		resultPoints.add(new Point(newX, sPoints.get(curIndex).y));
		resultPoints.add(new Point(newX, newY));
	}
	
	private static void computeTimeWarpingScale(Point[] resultPoints, int curIndex, int mode){
		//check valid index
		if(curIndex < 1 || curIndex >= resultPoints.length - 1){
			System.out.println("Index error in computeTimeWarpingScale");
			return;
		}
		
		
		double ONE_THIRD = 1.0/3;
		double TWO_THIRD = 2.0/3;
		
		double xl1=0, xl2=0, xr1=0, xr2=0, yl1=0, yl2=0, yr1=0, yr2=0;
		switch(mode){
			case 1:{
				xl1 = resultPoints[curIndex].x -
						(resultPoints[curIndex].x - resultPoints[curIndex - 1].x) * ONE_THIRD;
				xl2 = resultPoints[curIndex].x;
				xr1 = resultPoints[curIndex].x;
				xr2 = resultPoints[curIndex].x +
						(resultPoints[curIndex + 1].x - resultPoints[curIndex].x) * ONE_THIRD;
				yl1 = resultPoints[curIndex].y -
						(resultPoints[curIndex].y - resultPoints[curIndex - 1].y) * ONE_THIRD;
				yl2 = resultPoints[curIndex].y;
				yr1 = resultPoints[curIndex].y;
				yr2 = resultPoints[curIndex].y +
						(resultPoints[curIndex + 1].y - resultPoints[curIndex].y) * ONE_THIRD;
				break;
			}
			case 2:{
				xl1 = resultPoints[curIndex].x -
						(resultPoints[curIndex].x - resultPoints[curIndex - 1].x) * TWO_THIRD;
				xl2 = resultPoints[curIndex].x -
						(resultPoints[curIndex].x - resultPoints[curIndex - 1].x) * ONE_THIRD;
				xr1 = resultPoints[curIndex].x +
						(resultPoints[curIndex + 1].x - resultPoints[curIndex].x) * ONE_THIRD;
				xr2 = resultPoints[curIndex].x +
						(resultPoints[curIndex + 1].x - resultPoints[curIndex].x) * TWO_THIRD;
				
				yl1 = resultPoints[curIndex].y -
						(resultPoints[curIndex].y - resultPoints[curIndex - 1].y) * TWO_THIRD;
				yl2 = resultPoints[curIndex].y -
						(resultPoints[curIndex].y - resultPoints[curIndex - 1].y) * ONE_THIRD;
				yr1 = resultPoints[curIndex].y +
						(resultPoints[curIndex + 1].y - resultPoints[curIndex].y) * ONE_THIRD;
				yr2 = resultPoints[curIndex].y +
						(resultPoints[curIndex + 1].y - resultPoints[curIndex].y) * TWO_THIRD;
				break;
			}
			case 3:{
				xl1 = resultPoints[curIndex].x -
						(resultPoints[curIndex].x - resultPoints[curIndex - 1].x) * 1;
				xl2 = resultPoints[curIndex].x -
						(resultPoints[curIndex].x - resultPoints[curIndex - 1].x) * TWO_THIRD;
				xr1 = resultPoints[curIndex].x +
						(resultPoints[curIndex + 1].x - resultPoints[curIndex].x) * TWO_THIRD;
				xr2 = resultPoints[curIndex].x +
						(resultPoints[curIndex + 1].x - resultPoints[curIndex].x) * 1;
				
				yl1 = resultPoints[curIndex].y -
						(resultPoints[curIndex].y - resultPoints[curIndex - 1].y) * 1;
				yl2 = resultPoints[curIndex].y -
						(resultPoints[curIndex].y - resultPoints[curIndex - 1].y) * TWO_THIRD;
				yr1 = resultPoints[curIndex].y +
						(resultPoints[curIndex + 1].y - resultPoints[curIndex].y) * TWO_THIRD;
				yr2 = resultPoints[curIndex].y +
						(resultPoints[curIndex + 1].y - resultPoints[curIndex].y) * 1;
				break;
			}
			default:{
				
			}
		}
		
		//generate the random number in range (l1,l2) uniform (r1,r2)
		double newX1 = xl1 + Math.random() * (xl2 - xl1);
		double newX2 = xr1 + Math.random() * (xr2 - xr1);
		
		double newY1 = yl1 + Math.random() * (yl2 - yl1);
		double newY2 = yr1 + Math.random() * (yr2 - yr1);
		
		//random choose between x1 and x2
		int randBool = (int)(Math.random() * 10) % 2;
		double newX = randBool == 0 ? newX1 : newX2;
		double newY = randBool == 0 ? newY1 : newY2;
		
//		resultPoints.add(new Point(newX, sPoints.get(curIndex).y));
		resultPoints[curIndex].x = newX; 
//		resultPoints[curIndex].y = newY;
	}
	/**
	 * For each sequence S in source
	 * For each datapoint S[i]
	 * 		random generate p (0 < p < 1), If p < PROBABILITY
	 * 			random generate ampl, (RANGE1 < ampl < RANGE2)
	 * 				diff = (S[i+1] - S[i])*ampl
	 * 				S[i] = S[i] + diff
	 * */
	private static String addNoises(String source, String dest, double PROBABILITY, double RANGE1, double RANGE2){
		System.out.println("===== Adding noises =====");
		String status = "";
		File parentDir = new File(source);
		if(!parentDir.exists() || !parentDir.isDirectory()){
			status = "Error! Directory doesn't exist or the input source is not a directory.";
			return status;
		}
		File outDir = new File(dest);
		if(!outDir.exists())
			outDir.mkdirs();
		try {
			File[] files = parentDir.listFiles();
			for(File inFile : files){
				String inFileName = inFile.getName();
				int ind = inFileName.lastIndexOf(".");
				
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
				String outFileName = dest + "/" + inFileName.substring(0, ind)+ "_" +
						"an"+ ".txt";
//							System.out.printf("%s","generate " + outFileName + "...");
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outFileName))));
						
				//read input values
				ArrayList<Point> inPoints = new ArrayList<>();
				String line;
				while((line = br.readLine()) != null){
					if(line.length() > 0){
						String fields[] = line.split("\t");
						double x = Double.parseDouble(fields[0]);
						double y = Double.parseDouble(fields[1]);
						Point p = new Point(x, y);
						inPoints.add(p);
					}
				}
				//generate noise scale points
//					ArrayList<Point> resultPoints = new ArrayList();
				Point[] resultPoints = new Point[inPoints.size()];
				//for each sample
				for(int i = 0; i < inPoints.size() - 1; i++){
//					
					
					double newY = inPoints.get(i).y;
					double p = Math.random();
					if(p < PROBABILITY){
						double ampl = RANGE1 + (RANGE2 - RANGE1) * Math.random();
						double rand_ampl = 0 + (ampl - 0) * Math.random();
						double diff = (inPoints.get(i + 1).y - inPoints.get(i).y) * rand_ampl;
						newY = inPoints.get(i).y + diff;
						resultPoints[i] = new Point(inPoints.get(i).x, newY);
					}
//					//write result on file
					bw.write("" + inPoints.get(i).x + "\t" + newY+"\n");
				}
				//write the last point
				bw.write("" + inPoints.get(inPoints.size() - 1).x + "\t" + inPoints.get(inPoints.size() - 1).y);
				bw.close();
				br.close();
					
//					System.out.printf("%s","Done\n");
			}//end for each file
			return MSG_TASK_SUCCESSFUL;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return MSG_READ_FILE_ERROR;
		}
		
	}
	
	/**
	 * randomly generate nSeq sequence S = {s1, ..., sn}
	 * S[1] in [1,10]
	 * S[i+1] = S[1] + x, x in [-0.1,0.1]
	 * */
	private static String randomWalk(String dest, int nSeq, int n){
		System.out.println("===== Random walk =====");
		String status = "";
		final double min1 = 1, max1 = 10, min2 = -0.1, max2 = 0.1;
		double curVal, prevVal;
		File outDir = new File(dest);
		if(!outDir.exists())
			outDir.mkdirs();
		try {
			
			for(int k = 0; k < nSeq; k++){
				String outFileName = dest + "/" + "rw" + k + ".txt";
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outFileName))));
				//first value is random value in [1,10]
				prevVal = min1 + Math.random() * (max1 - min1 + 1);
				//write in file
				bw.write(""+prevVal);
				//reapeat n - 1 times
				for(int i = 1; i < n; i++){
					double randVal = min2 + Math.random() * (max2 - min2);
					curVal = prevVal + randVal;
					//write in file
					bw.write("\n"+curVal);
				}
				bw.close();
//					System.out.printf("%s","Done\n");
			}//end for each file
			System.out.printf("%s","Done\n");
			return MSG_TASK_SUCCESSFUL;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return MSG_READ_FILE_ERROR;
		}
	}
	private static String extractPIP(String source, String dest, int n){
		System.out.println("===== Extract PIP points =====");
		String status = "";
		File parentDir = new File(source);
		if(!parentDir.exists() || !parentDir.isDirectory()){
			status = "Error! Directory doesn't exist or the input source is not a directory.";
			return status;
		}
		File outDir = new File(dest);
		if(!outDir.exists())
			outDir.mkdirs();
		try {
			File[] files = parentDir.listFiles();
			for(File inFile : files){
				String inFileName = inFile.getName();
				int ind = inFileName.lastIndexOf(".");
				
//				System.out.printf("%s","reading " + inFileName + "...");
				//Create the output directory
				
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
					
					
					//read input values
					ArrayList<Point> seedPoints = new ArrayList<>();
					String line;
					while((line = br.readLine()) != null){
						if(line.length() > 0){
							String fields[] = line.split("\t");
							double x = Double.parseDouble(fields[0]);
							double y = Double.parseDouble(fields[1]);
							Point p = new Point(x, y);
							seedPoints.add(p);
						}
					}
					if(seedPoints.size() < n){
						//skip this file
						continue;
					}
					//generate PIP points
					String outFileName = dest + "/" + inFileName.substring(0, ind)+ "_" +
							"pip" + n + ".txt";
//					System.out.printf("%s","generate " + outFileName + "...");
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outFileName))));		
					ArrayList<Point> resultPoints = new ArrayList();
					ArrayList<Integer> pipInds = new ArrayList<>();
					
					SBTree sbTree = new SBTree(seedPoints);
					pipInds = sbTree.retrieveSubsequenceIndices(0, seedPoints.size() - 1, n);
					Collections.sort(pipInds);
					
					for(int i : pipInds){
						Point p = seedPoints.get(i);
						bw.write(""+p.x +  "\t" + p.y);
						bw.newLine();
					}
					bw.close();
//					System.out.printf("%s","Done\n");
					br.close();
					
//					System.out.printf("%s","Done\n");
			}//end for each file
			return MSG_TASK_SUCCESSFUL;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return MSG_READ_FILE_ERROR;
		}
	}
	//nSimPoint: extra point at the begin and the end of sequence, we need to add simPoint so that the
	//data sequence is longer than the query sequence.
	private static String simulate(String source, String dest, int nSimPoint){
		System.out.println("===== Simulate ouput file =====");
		String status = "";
		File parentDir = new File(source);
		if(!parentDir.exists() || !parentDir.isDirectory()){
			status = "Error! Directory doesn't exist or the input source is not a directory.";
			return status;
		}
		File outDir = new File(dest);
		if(!outDir.exists())
			outDir.mkdirs();
		try {
			File mapFile = new File(FILE_ID_NAME_MAPPING);
			BufferedWriter mapBw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapFile)));
			
			File[] files = parentDir.listFiles();
			for(File inFile : files){
				String inFileName = inFile.getName();
				int ind = inFileName.lastIndexOf(".");
				
//				System.out.printf("%s","reading " + inFileName + "...");
				//Create the output directory
				
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
					
					
					//read input values and write out file
					String outFileName = dest + "/" + inFileName.substring(0, ind)+ "_" +
							"out.txt";
//					System.out.printf("%s","generate " + outFileName + "...");
					
					mapBw.write(inFileName.substring(0, ind) + "\t" + inFileName.substring(0, ind));
					mapBw.newLine();
					
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outFileName))));
					bw.write("time startPrice highestPrice lowestPrice endPrice transactionCount");
					//write the begin sim points
					for(int i = 0; i < nSimPoint; i++){
						bw.newLine();
						bw.write("20130815\t123\t123\t123\t" + Math.random()*20 + "\t1234");
					}
					String line;
					while((line = br.readLine()) != null){
						if(line.length() > 0){
							String fields[] = line.split("\t");
							double y = Double.parseDouble(fields[1]);
							bw.newLine();
							bw.write("20130815\t123\t123\t123\t" + y + "\t1234");
						}
					}
					//write the end sim points
					for(int i = 0; i < nSimPoint; i++){
						bw.newLine();
						bw.write("20130815\t123\t123\t123\t" + Math.random()*20 + "\t1234");
					}
					
					bw.close();
//					System.out.printf("%s","Done\n");
					br.close();
					
//					System.out.printf("%s","Done\n");
			}//end for each file
			mapBw.close();
			return MSG_TASK_SUCCESSFUL;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return MSG_READ_FILE_ERROR;
		}
	}
}
