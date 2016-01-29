package sortableJ.jsonObjects;

/**
 * 
 * @author Joshua Bowles
 *
 */
public class Product {

	private String product_name;
	private String manufacturer;
	private String family;
	private String model;
	private String announced_date;

	Product() {

	}

	public String getProduct_name() {
		return product_name;
	}

	public void setProduct_name(String product_name) {
		this.product_name = product_name;
	}

	public String getAnnounced_date() {
		return announced_date;
	}

	public void setAnnounced_date(String announced_date) {
		this.announced_date = announced_date;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getFamily() {
		return family;
	}

	public void setFamily(String family) {
		this.family = family;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	/*
	 * "product_name": String // A unique id for the product "manufacturer":
	 * String "family": String // optional grouping of products "model": String
	 * "announced-date": String // ISO-8601 formatted date string, e.g.
	 * 2011-04-28T19:00:00.000-05:00
	 */

}
