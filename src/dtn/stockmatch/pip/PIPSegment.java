package dtn.stockmatch.pip;

public class PIPSegment {
	public int start;
	public int end;
	public int maxVDId;
	public double maxVD;
	
	public PIPSegment(int start, int end, int maxVDId, double maxVD){
		this.start = start;
		this.end = end;
		this.maxVDId = maxVDId;
		this.maxVD = maxVD;
	}
}
