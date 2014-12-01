package brightedge;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Class that holds main methods and fields.
 * @author ryanhall
 */
public class MainProg {

	private String keyword; //provided by command line
	private String pagenumber; //provided by command line
	private String base_url = "http://www.sears.com/";
	private String query_url = "search=";
	private String url; //sears full search url
	private String redirect_url; //sears may redirect after search is performed
	private String page_url = "?pageNum=";
	private String page_url_add = "&pageNum=";
	private Results[] results;
	
	//start program
	static public void main(String args[]) throws IOException {
		//check args length
		if (args.length == 1) {
			MainProg mainprog = new MainProg(args[0]);
		}
		else if (args.length == 2){
			MainProg mainprog = new MainProg(args[0],args[1]);
		} else {
			throw new IOException("Must supply keyword as an argument. May in addition supply page number.");
		}	
	}

	/**
	 * Constructor for first task. When supplied a keyword
	 * will instantiate class, executing the necessary methods to
	 * output item count to the screen.
	 * 
	 * @param	keyword		string of object to search for				
	 */
	public MainProg(String keyword){
		this.setKeyword(keyword);
		this.createURL();
		this.getRedirectURL();
		this.getItemCount(this.getSearchDocument());
	}

	/**
	 * Constructor override for the second task. When supplied
	 * and keyword and a pagenumber will do an initial run of methods
	 * in order to print a series of results to the screen.
	 * 
	 * @param	keyword		string of object to search for
	 * @param	pagenumber	string of page number you want to return results from
	 */
	public MainProg(String keyword, String pagenumber){
		this.setKeyword(keyword);
		this.setPagenumber(pagenumber);
		this.createURL();
		this.getRedirectURL();
		this.getPageURL();
		results = this.getPageInfo(this.getSearchDocument());
		this.printOutResults();
	}

	/**
	 * Get method for keyword.
	 * 
	 * @return keyword class' keyword variable.
	 */
	public String getKeyword(){
		return this.keyword;
	}

	/**
	 * Set method for keyword. Replaces 
	 * 
	 * @param keyword string with search "keyword".
	 */
	public void setKeyword(String keyword){
		//switch out spaces for %20
		String keyword_clean = keyword.replace(" ", "%20");
		//set internal keyword
		this.keyword = keyword_clean;
	}

	/**
	 * Get method for pagenumber.
	 * 
	 * @return pagenumber identifies which pages to return results from.
	 */
	public String getPagenumber(){
		return this.pagenumber;
	}

	/**
	 *  Set method for pagenumber.
	 *  
	 *  @param pagenumber identifies which pages to return results from.
	 */
	public void setPagenumber(String pagenumber){
		this.pagenumber = pagenumber;
	}
	
	/**
	 * Creates and sets standard search url. This is used to check for redirect urls.
	 * 
	 */
	public void createURL(){
		//Concatenate separate portions
		this.url = new String(this.base_url + this.query_url + this.keyword);
	}
	
	/**
	 * Needed because Sears parses search input and redirects user.
	 * We need the new URL where Sears redirects the user.
	 * In the case where there is no new URL, redirect_url is set
	 * to the url created in createURL().
	 * 
	 * @return redirect_url the url used to access the search page
	 */
	public String getRedirectURL(){
		try {
			Document doc = Jsoup.connect(this.url).timeout(10000).get();
			//print out document for testing...
			String docstring = doc.toString();
			String[] lines = docstring.split("\n");
			for (int i = 0; i < lines.length; i++){
				if (lines[i].contains("var url = ")){
					//grab the substring
					String url_sub = lines[i].substring(14, lines[i].length() - 3);
					//remove "\" character
					this.redirect_url = url_sub.replace("\\","");
					//remove anything after "?"; helps when using pagenumber
					//this.redirect_url = this.redirect_url.replaceAll("\\?+.*", "");
					this.redirect_url = this.base_url + this.redirect_url;
					//System.out.println("redirect url : " + this.redirect_url);
					//no need to continue
					return this.redirect_url; 
				}
			}
		} catch (IOException e) {
			//Problem with creating url
			System.out.println("Problem with getting Redirect URL. If a timeout, try again.");
			e.printStackTrace();
		}	
		//If "var url = " is not found, then no redirection. Set redirect_url = url
		this.redirect_url = this.url;
		//System.out.println("redirect url: " + this.redirect_url);
		return this.redirect_url;
	}
	
	/**
	 * This method properly formats the URL for task 2.
	 * 
	 */
	public void getPageURL(){
		//checks to see if there are already arguments
		if (this.redirect_url.contains("?")){
			//if there are arguments, make sure to add "&" 
			this.redirect_url = new String(this.redirect_url + this.page_url_add + this.pagenumber);
		} else{
			//if there aren't arguments, make sure to add "?"
			this.redirect_url = new String(this.redirect_url + this.page_url + this.pagenumber);
		//System.out.println("Page URL: " + this.redirect_url);
		}
	}
	
	/**
	 * Returns the search document.
	 * 
	 * @return doc Document giving by redirect_url
	 */
	public Document getSearchDocument(){
		Document doc = null;
		System.out.println("Accessing: " + this.redirect_url);
		try {
			//System.out.println(this.redirect_url);
			doc = Jsoup.connect(this.redirect_url).timeout(10000).get();
			//String docstring = doc.toString();
			//System.out.println(docstring);
			return doc;
		} catch (IOException e) {
			System.out.println("Problem with getting document using redirected url. If a timeout, try again.");
			e.printStackTrace();
		}
		return doc;
	}
	
	/**
	 * Returns the number of items showns on Sears' search page.
	 * 
	 * @param doc Document returned from search url.
	 * @return item_count A string containing the number of search items.
	 */
	public String getItemCount(Document doc){
		String item_count = "0";
		//first count is total; second is Sear's only.
		try {
			Element tab_filters_count = doc.select(".tab-filters-count").first();
			item_count = tab_filters_count.text();
			//remove parentheses
			item_count = item_count.substring(1,item_count.length()-1);
		} //Have to catch Null Element
		catch (NullPointerException e){
			System.out.println("No items returned.");
		}
		System.out.println("Item Count: " + item_count);
		return item_count;
	}
	
	/**
	 * Returns an array of Results objects. Parses the doc that is given,
	 * and cleans up the data for viewing.
	 * 
	 * @param doc document to grab title, price, vendor information from.
	 * @return results an array of Results objects
	 */
	public Results[] getPageInfo(Document doc){
		Results[] results = null;
		String json_info = "";
		
		//title info
		Elements titles = doc.select("h2[itemprop=name]");
		//clean up titles and place into an array
		int titles_length = titles.size();
		String[] titles_str = new String[titles_length];
		for (int i = 0; i < titles_str.length; i++){
			String title_clean = Jsoup.parse(titles.get(i).toString()).text();
			titles_str[i] = title_clean;
			//System.out.println(titles_str[i]);
		}
		
		//price info
		Elements prices = doc.select("span[itemprop=price]");
		//clean pricing data and place into array
		int prices_length = prices.size();
		String[] prices_str = new String[prices_length];
		for (int i = 0; i < prices_str.length; i++){
			//String prices_clean = prices.get(i).toString().replaceAll("<[^>]*>", "");
			String prices_clean = Jsoup.parse(prices.get(i).toString()).text();
			prices_str[i] = prices_clean;
			//System.out.println(prices_str[i]);
		}
		
		//seller info
		//going to grab seller data from json at top (price data in this is not accurate)
		String docstring = doc.toString();
		String[] lines = docstring.split("\n");
		for (int i = 0; i < lines.length; i++){
			//found json
			if (lines[i].contains("var jsonSPURefactor")){
				json_info = lines[i];
				//no need to continue
				break;
			}
		}
		//split into fields
		String[] json_split = json_info.split(",");
		String[] json_sellers = new String[50];
		int seller_num = 0;
		//only need sellerName field
		for (int i = 0; i < json_split.length; i++){
			if (json_split[i].contains("sellerName")){
				json_sellers[seller_num] = json_split[i].substring(14, json_split[i].length() - 1);
				seller_num++;
			}
		}
		//can now create result objects
		//using titles_str[], prices_str[], and json_sellers[]
		results = new Results[titles_str.length];
		for (int i = 0; i < titles_str.length; i ++){
			results[i] = new Results(titles_str[i],prices_str[i],json_sellers[i]);
		}
		return results;
	}
	
	/**
	 * Prints out values from each Results object within the Results[] array.
	 * 
	 */
	public void printOutResults(){
		for (int i = 0; i < this.results.length; i++){
			results[i].outputResults();
		}
		//may not have any results
		if (this.results.length == 0){
			System.out.println("No results on this page.");
		}
	}
}