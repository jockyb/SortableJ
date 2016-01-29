package sortableJ.util;

import sortableJ.jsonObjects.Product;

/**
 * Object to track listings that have been matched to a product.
 * 
 * @author Joshua Bowles
 *
 */
public class MatchAndScore {

	// How well a listing matches a product (larger is better)
	private int matchScore;
	// What product is the current best match
	private Product matchedTo;
	// How many products a list has been matched to
	private int totalMatches;

	public MatchAndScore() {
		this.matchScore = 0;
		this.matchedTo = null;
		this.totalMatches = 0;
	}

	public MatchAndScore(int matchScore, Product matchedTo) {
		this.matchScore = matchScore;
		this.matchedTo = matchedTo;
		this.totalMatches = 1;
	}
	
	public MatchAndScore(int matchScore, Product matchedTo, int totalMatches) {
		this.matchScore = matchScore;
		this.matchedTo = matchedTo;
		this.totalMatches = totalMatches;
	}
	
	public MatchAndScore foundAnother() {
		this.totalMatches++;
		return this;
	}

	public int getMatchScore() {
		return matchScore;
	}

	public void setMatchScore(int matchScore) {
		this.matchScore = matchScore;
	}

	public Product getMatchedTo() {
		return matchedTo;
	}

	public void setMatchedTo(Product matchedTo) {
		this.matchedTo = matchedTo;
	}

	public int getTotalMatches() {
		return totalMatches;
	}

	public void setTotalMatches(int totalMatches) {
		this.totalMatches = totalMatches;
	}
	

}
