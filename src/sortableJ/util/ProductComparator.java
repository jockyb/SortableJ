package sortableJ.util;

import java.util.Comparator;

import sortableJ.jsonObjects.Product;

/**
 * 
 * @author Joshua Bowles
 *
 */
public class ProductComparator implements Comparator<Product> {
	@Override
	public int compare(Product o1, Product o2) {
		int familySize1 = 0;
		int familySize2 = 0;
		if (o1.getFamily() != null) {
			familySize1 = o1.getFamily().length();
		}
		if (o2.getFamily() != null) {
			familySize2 = o2.getFamily().length();
		}
		// Multiply the model string length by 10 to give greater value over
		// family name size
		// TODO may not be needed anymore with new match score
		return (o2.getModel().length() * 10 + familySize2) - (o1.getModel().length() * 10 + familySize1);
	}
}
