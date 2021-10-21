package nxt;

public class test {

	public static void main(String[] args) {
		long constantFee = 5000l;
		long feePerSize = 5000l;
		int unitSize = 32;
		int size = 0 ;
			
		for(size = 0; size <= 1000; size = size+20) {
			long totalFee = Math.addExact(constantFee, Math.multiplyExact((long) (size / unitSize), feePerSize));
			System.out.println("size= "+ size + " fee= " + totalFee);
		}
	}
}
