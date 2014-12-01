package brightedge;

public class Results {
	private String title;
	private String price;
	private String vendor;

	public Results(String title, String price, String vendor){
		this.title = title;
		this.price = price;
		this.vendor = vendor;
	}
	
	public void outputResults(){
		System.out.println("Title: " + this.title);
		System.out.println("Price: " + this.price);
		System.out.println("Vendor: " + this.vendor);
	}
	
	
}
