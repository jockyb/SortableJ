package sortableJ.jsonObjects;

import java.util.ArrayList;

/**
 * 
 * @author Joshua Bowles
 *
 */
public class ResultItem {

	private String prodName;
	private ArrayList<Listing> listings;

	ResultItem() {

	}

	public ResultItem(String prodName) {
		this.prodName = prodName;
		listings = new ArrayList<Listing>();
	}

	public String getProdName() {
		return prodName;
	}

	public void setProdName(String prodName) {
		this.prodName = prodName;
	}

	public ArrayList<Listing> getListings() {
		return listings;
	}

	public void setListings(ArrayList<Listing> listings) {
		this.listings = listings;
	}

}
