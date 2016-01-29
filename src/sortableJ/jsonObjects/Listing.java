package sortableJ.jsonObjects;

/**
 * 
 * @author Joshua Bowles
 *
 */
public class Listing {

	private String title;
	private String manufacturer;
	private String currency;
	private String price;

	Listing() {

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	/*
	 * "title": String // description of product for sale "manufacturer": String
	 * // who manufactures the product for sale "currency": String // currency
	 * code, e.g. USD, CAD, GBP, etc. "price": String // price, e.g. 19.99,
	 * 100.00
	 */

}
