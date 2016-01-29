package sortableJ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

//TODO look into replacing GSON with Jackson to use schema validation
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import sortableJ.jsonObjects.CompanyAlias;
import sortableJ.jsonObjects.Listing;
import sortableJ.jsonObjects.ParsingRegex;
import sortableJ.jsonObjects.Product;
import sortableJ.jsonObjects.ResultItem;
import sortableJ.util.MatchAndScore;
import sortableJ.util.ProductComparator;

public class main {

	public static void main(String[] args) {
		// Known Issue: Can't match Roman numerals with Arabic counterparts (ie.
		// IV != 4), would only give minimal improvements with better chances of
		// false matches
		
		// TODO add support for multiple languages and externalize strings

		// Disable HTML Encoding to match input encoding better
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		
		HashMap<String, List<Product>> prodMap = new HashMap<String, List<Product>>();
		HashMap<String, List<Listing>> listingMap = new HashMap<String, List<Listing>>();

		HashMap<String, CompanyAlias> aliasMap = new HashMap<String, CompanyAlias>();

		// Map results to make removing low score matches faster
		HashMap<String, List<ResultItem>> results = new HashMap<String, List<ResultItem>>();

		String strongRegex = "[,\"'`/()\\[\\]]";
		String weakRegex = "[-]";
		
		// Default values
		String inputProdFile = "input" + File.separator + "products.txt";
		String inputListingFile = "input" + File.separator + "listings.txt";
		String inputAliasFile = "settings" + File.separator + "CompanyAlias.json";
		String inputRegexFile = "settings" + File.separator + "ParsingRegex.json";
		String outputDir = "output";

		// Optional file name overwrites
		if (System.getProperty("products") != null) {
			inputProdFile = System.getProperty("products");
		}
		if (System.getProperty("listings") != null) {
			inputListingFile = System.getProperty("listings");
		}
		if (System.getProperty("aliases") != null) {
			inputAliasFile = System.getProperty("aliases");
		}
		if (System.getProperty("regex") != null) {
			inputRegexFile = System.getProperty("regex");
		}
		if (System.getProperty("outputDir") != null) {
			outputDir = System.getProperty("outputDir");
		}

		File inputListings = new File(inputListingFile);
		File inputProds = new File(inputProdFile);

		File inputAlias = new File(inputAliasFile);
		File inputRegex = new File(inputRegexFile);

		System.out.println("Input file " + inputListings.getAbsolutePath() + " exists : " + inputListings.exists());
		System.out.println("Input file " + inputProds.getAbsolutePath() + " exists : " + inputProds.exists());
		System.out.println("Input file " + inputAlias.getAbsolutePath() + " exists : " + inputAlias.exists());
		System.out.println("Input file " + inputRegex.getAbsolutePath() + " exists : " + inputRegex.exists());

		FileReader prodFr, listingFr, aliasFr, regexFr;
		try {
			prodFr = new FileReader(inputProds);
			listingFr = new FileReader(inputListings);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		// Populate alias values
		try {
			aliasFr = new FileReader(inputAlias);
			
			try (BufferedReader br = new BufferedReader(aliasFr)) {
				String line;
				int lineCount = 0;
				while ((line = br.readLine()) != null) {
					lineCount++;
					try {
						CompanyAlias alias = gson.fromJson(line, CompanyAlias.class);

						// Blank line
						if (alias == null) {
							continue;
						}

						// Check for invalid input JSON entry
						if (alias.getCompanyName() == null) {
							System.out.println("Error parsing JSON file " + inputAlias + " on line " + lineCount
									+ ". Could not set companyName");
							return;
						}

						aliasMap.put(alias.getCompanyName(), alias);

					} catch (Exception e) {
						System.out.println("Error parsing JSON file " + inputAliasFile + " on line " + lineCount + " : "
								+ e.getMessage());
						return;
					}

				}
				br.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
			aliasFr.close();
		} catch (FileNotFoundException e) {
			System.out.println("Could not find Company Alias file, no aliases will be used");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		
		// Populate regex values
		try {
			regexFr = new FileReader(inputRegex);
			
			try (BufferedReader br = new BufferedReader(regexFr)) {
				String line;
				int lineCount = 0;
				while ((line = br.readLine()) != null) {
					lineCount++;
					try {

						ParsingRegex regex = gson.fromJson(line, ParsingRegex.class);

						// Blank line
						if (regex == null) {
							continue;
						}

						// Characters that should always be replaced with whitespace
						// to find matches eg. /
						try {
							strongRegex = "["+regex.getStrongRegex().replaceAll("(\\[|\\])", "\\\\$1")+"]";

							Pattern.compile(strongRegex);
						} catch (PatternSyntaxException exception) {
							strongRegex = "[,\"'`/()\\[\\]]";
							System.err.println(exception.getDescription() + "\nstrongRegex Defaulting to hardcoded regex "
									+ strongRegex);
						}
						// Characters that can be removed entirely or replaced with
						// whitespace to find matches eg. -
						try {
							weakRegex = "["+regex.getWeakRegex().replaceAll("(\\[|\\])", "\\\\$1")+"]";

							Pattern.compile(weakRegex);
						} catch (PatternSyntaxException exception) {
							weakRegex = "[-]";
							System.err.println(
									exception.getDescription() + "\nweakRegex Defaulting to hardcoded regex " + weakRegex);
						}

					} catch (Exception e) {
						System.out.println(
								"Error parsing JSON file " + inputRegex + " on line " + lineCount + " : " + e.getMessage());
						return;
					}
				}
				br.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
			
			regexFr.close();
		} catch (FileNotFoundException e2) {
			System.out.println("Could not find Regex file, default values will be used");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// Populate products map
		try (BufferedReader br = new BufferedReader(prodFr)) {
			String line;
			int lineCount = 0;
			while ((line = br.readLine()) != null) {

				lineCount++;

				// TODO find better way to do this
				// format json to match variable names
				line = line.replace("announced-date", "announced_date");

				try {
					Product product = gson.fromJson(line, Product.class);

					// Blank line
					if (product == null) {
						continue;
					}

					// Check for invalid input JSON entry
					if (product.getProduct_name() == null || product.getManufacturer() == null
							|| product.getModel() == null) {
						System.out.println("Error parsing JSON file " + inputProdFile + " on line " + lineCount
								+ " invalid product missing name model or manufacturer");
						return;
					}

					if (prodMap.containsKey(product.getManufacturer().toLowerCase())) {
						prodMap.get(product.getManufacturer().toLowerCase()).add(product);
					} else {
						List<Product> newList = new ArrayList<Product>();
						newList.add(product);
						prodMap.put(product.getManufacturer().toLowerCase(), newList);
					}
				} catch (Exception e) {
					System.out.println("Error parsing JSON file " + inputProdFile + " on line " + lineCount + " : "
							+ e.getMessage());
					return;
				}
			}
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		// Populate listings map
		try (BufferedReader br = new BufferedReader(listingFr)) {
			String line;
			int lineCount = 0;
			while ((line = br.readLine()) != null) {

				try {
					Listing listing = gson.fromJson(line, Listing.class);
					lineCount++;
					// Blank line
					if (listing == null) {
						continue;
					}

					// Check for invalid input JSON entry
					if (listing.getManufacturer() == null || listing.getTitle() == null) {
						System.out.println("Error parsing JSON file " + inputListingFile + " on line " + lineCount);
						return;
					}

					if (listingMap.containsKey(listing.getManufacturer().toLowerCase())) {
						listingMap.get(listing.getManufacturer().toLowerCase()).add(listing);
					} else {
						List<Listing> newList = new ArrayList<Listing>();
						newList.add(listing);
						listingMap.put(listing.getManufacturer().toLowerCase(), newList);
					}
				} catch (Exception e) {
					System.out.println("Error parsing JSON file " + inputListingFile + " on line " + lineCount + " : "
							+ e.getMessage());
					return;
				}

			}
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		try {
			prodFr.close();
			listingFr.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		// Loop through each manufacturer
		Iterator<String> manufacturers = prodMap.keySet().iterator();
		while (manufacturers.hasNext()) {

			String thisManufacture = manufacturers.next();

			List<Product> theseProds = prodMap.get(thisManufacture);

			// Sort products by length of model and family strings to find
			// longer strings first
			// eg. to avoid matching model e300 to model e30
			// TODO this isn't as useful as it used to be, can be removed but
			// leaving it in to make output easier to read
			Collections.sort(theseProds, new ProductComparator());

			Iterator<Product> ourProducts = prodMap.get(thisManufacture).iterator();
			List<Listing> listingsToCheck = listingMap.get(thisManufacture);
			List<String> manufactureVariations = new ArrayList<String>();

			// Initialize list if there is no matching manufacturer in the
			// listings input
			// Add manufacturer to list of keys to check otherwise
			if (listingsToCheck == null) {
				listingsToCheck = new ArrayList<Listing>();
			} else {
				manufactureVariations.add(thisManufacture);
			}

			// Deal with edge cases for manufacturers
			// TODO matches in various should also be made to contain target
			// manufacturer
			if (aliasMap.containsKey("all")) {

				CompanyAlias thisAlias = aliasMap.get("all");

				for (int i = 0; i < thisAlias.getAliases().length; i++) {
					if (listingMap.containsKey(thisAlias.getAliases()[i])
							&& !prodMap.containsKey(thisAlias.getAliases()[i])) {
						listingsToCheck.addAll(listingMap.get(thisAlias.getAliases()[i]));
						manufactureVariations.add(thisAlias.getAliases()[i]);
					}
				}
			}

			// Add variations of this Manufacturer from list of aliases if any
			if (aliasMap.containsKey(thisManufacture)) {

				CompanyAlias thisAlias = aliasMap.get(thisManufacture);

				for (int i = 0; i < thisAlias.getAliases().length; i++) {
					if (listingMap.containsKey(thisAlias.getAliases()[i])
							&& !prodMap.containsKey(thisAlias.getAliases()[i])
							&& !manufactureVariations.contains(thisAlias.getAliases()[i])
							) {
						listingsToCheck.addAll(listingMap.get(thisAlias.getAliases()[i]));
						manufactureVariations.add(thisAlias.getAliases()[i]);
					}
				}
			}

			// Check for manufacturers using long form names eg "Samsung Camera"
			Iterator<String> listKeys = listingMap.keySet().iterator();
			while (listKeys.hasNext()) {
				String thisKey = listKeys.next();

				if (!thisKey.toLowerCase().equals(thisManufacture.toLowerCase())
						&& thisKey.toLowerCase().contains(thisManufacture.toLowerCase())
						&& !manufactureVariations.contains(thisKey)
						) {
					listingsToCheck.addAll(listingMap.get(thisKey));
					manufactureVariations.add(thisKey);
				}

			}

			HashMap<Listing, MatchAndScore> foundListings = new HashMap<Listing, MatchAndScore>();

			while (ourProducts.hasNext()) {

				Product thisProduct = ourProducts.next();
				String prodName = thisProduct.getProduct_name();

				ResultItem thisResult = new ResultItem(prodName);

				for (int i = 0; i < listingsToCheck.size(); i++) {
					Listing thisListing = listingsToCheck.get(i);
					String listTitle = thisListing.getTitle().toLowerCase();

					// TODO check if model=DMC-TZ10 is the same as model=TZ10
					// (Panasonic Lumix)

					// Olympus Ultra-Zoom = UZ, waterproof = WP
					// NOTE Olympus-X650-WP doesn't match the model number X560,
					// assuming a type-o with product_name

					// TODO Epson ### Zoom seems to equal ###Z postfix or has
					// zoom else where in title
					// TODO Sony family Alpha seems to prefix A's instead of
					// using the family name
					// TODO add a modelAlias file to deal with above issues,
					// currently out of scope

					// TODO find out the difference between
					// Kyocera_Yashica_Finecam_3300 and Kyocera_Finecam_3300 may
					// be a duplicate product entry

					// Adding spaces after model and family to prevent matching
					// model substrings eg. E-5 and E-50
					String moddedTitle = (" "
							+ listTitle.toLowerCase().replaceAll(weakRegex, " ").replaceAll(strongRegex, " ") + " ")
									.replaceAll("\\s{2,}", " ");
					String moddedSpacelessTitle = (" "
							+ listTitle.toLowerCase().replaceAll(weakRegex, "").replaceAll(strongRegex, "") + " ")
									.replaceAll("\\s{2,}", " ");
					String moddedModel = (" " + thisProduct.getModel().toLowerCase().replaceAll(weakRegex, " ")
							.replaceAll(strongRegex, " ") + " ").replaceAll("\\s{2,}", " ");
					String moddedSpacelessModel = (" " + thisProduct.getModel().toLowerCase().replaceAll(weakRegex, "")
							.replaceAll(strongRegex, "").replaceAll(" ", "") + " ").replaceAll("\\s{2,}", " ");

					String moddedFamily = "";
					String moddedSpacelessFamily = "";

					if (thisProduct.getFamily() != null) {
						moddedFamily = (" " + thisProduct.getFamily().toLowerCase().replaceAll(weakRegex, " ")
								.replaceAll(strongRegex, " ") + " ").replaceAll("\\s{2,}", " ");
						moddedSpacelessFamily = (" " + thisProduct.getFamily().toLowerCase().replaceAll(weakRegex, "")
								.replaceAll(strongRegex, "").replaceAll(" ", "") + " ").replaceAll("\\s{2,}", " ");
					}

					if ((moddedTitle.contains(moddedModel) || moddedSpacelessTitle.contains(moddedModel)
							|| moddedSpacelessTitle.contains(moddedSpacelessModel)
							|| moddedTitle.contains(moddedSpacelessModel)) &&
							(moddedFamily.equals("") || moddedTitle.contains(moddedFamily)
									|| moddedSpacelessTitle.contains(moddedFamily)
									|| moddedSpacelessTitle.contains(moddedSpacelessFamily)
									|| moddedTitle.contains(moddedSpacelessFamily))) {

						// Set match score for a matching listing
						int matchIndex = (moddedTitle.indexOf(moddedModel) != -1) ? moddedTitle.indexOf(moddedModel)
								: moddedTitle.indexOf(moddedSpacelessModel);
						
						int familyIndex = 0;
						int familyDelta = 0;
						int totalLength = moddedSpacelessModel.length();
						if (!(moddedFamily.equals("") && moddedSpacelessFamily.equals(""))) {

							familyIndex = (moddedTitle.indexOf(moddedFamily) != -1) ? moddedTitle.indexOf(moddedFamily)
									: moddedTitle.indexOf(moddedSpacelessFamily);

							// Add 3 to the delta to offset the spaces added to
							// model
							familyDelta = (matchIndex < familyIndex)
									? Math.abs(matchIndex - familyIndex) - moddedSpacelessModel.length() + 3
									: Math.abs(matchIndex - familyIndex) - moddedSpacelessFamily.length() + 3;

							// May as well use the spaceless word lengths
							totalLength += moddedSpacelessFamily.length();

							// Use earliest match
							matchIndex = (matchIndex > familyIndex) ? familyIndex : matchIndex;
						}

						// Match score is length of matched strings (model +
						// family) minus location of earliest match in target
						// title
						// minus distance between family and model strings in
						// target title (if there is a family to match, 0
						// otherwise)
						// TODO fiddle with multipliers to get better results
						int matchScore = totalLength - matchIndex - familyDelta;

						if (!foundListings.containsKey(thisListing)) {

							foundListings.put(thisListing, new MatchAndScore(matchScore, thisProduct));
							thisResult.getListings().add(thisListing);

						} else if (foundListings.get(thisListing).getTotalMatches() == 2) {
							// If a listing has been matched to 3 products it could be an accessory
							// Remove it from results
							
							Product toRemove = foundListings.get(thisListing).getMatchedTo();
							
							System.out.println("Possible accessory found " + thisListing.getTitle() + ", removing");

							foundListings.put(thisListing, foundListings.get(thisListing).foundAnother());

							for (int q = 0; q < results.get(thisManufacture).size(); q++) {
								if (results.get(thisManufacture).get(q).getProdName()
										.equals(toRemove.getProduct_name())) {
									// Search current results to remove the accessory
									results.get(thisManufacture).get(q).getListings().remove(thisListing);
									break;
								}
							}

						} else if (foundListings.get(thisListing).getTotalMatches() > 2) {
							// DO NOTHING
						} else if (matchScore > foundListings.get(thisListing).getMatchScore()) {
							Product toRemove = foundListings.get(thisListing).getMatchedTo();

							foundListings.put(thisListing, new MatchAndScore(matchScore, thisProduct, foundListings.get(thisListing).getTotalMatches()+1));
							thisResult.getListings().add(thisListing);

							for (int q = 0; q < results.get(thisManufacture).size(); q++) {
								if (results.get(thisManufacture).get(q).getProdName()
										.equals(toRemove.getProduct_name())) {
									// Search current results to remove the
									// lower scoring match
									results.get(thisManufacture).get(q).getListings().remove(thisListing);
									break;
								}
							}
						} else {
							// Equal match scores should go to first match which
							// should have the longer model string
							// warn of possible duplicate product
							if (matchScore == foundListings.get(thisListing).getMatchScore()) {
								System.out.println("Possible duplicate products " + thisProduct.getProduct_name()
										+ " and " + foundListings.get(thisListing).getMatchedTo().getProduct_name() 
										+ " both equally match " + listTitle);
							}
						}
					}
				}

				if (results.containsKey(thisManufacture)) {
					results.get(thisManufacture).add(thisResult);
				} else {
					ArrayList<ResultItem> tmp = new ArrayList<ResultItem>();
					tmp.add(thisResult);
					results.put(thisManufacture, tmp);
				}

			}

			// Remove matched listings to create list of unmatched listings
			// "A single price listing may match at most one product" free to
			// remove entries once matched to all in a company
			// TODO may want to exempt variations especially "various"
			// manufactures
			for (int j = 0; j < manufactureVariations.size(); j++) {
				listingMap.get(manufactureVariations.get(j)).removeAll(foundListings.keySet());
			}

		}

		PrintWriter writer;

		new File(outputDir).mkdirs();

		try {
			writer = new PrintWriter(outputDir + File.separator + "results.txt", "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}

		// TODO find out why Diamond has a Sony camera in it's listings
		// TODO find better way of separating accessories and kits from results

		Iterator<String> resultSet = results.keySet().iterator();

		while (resultSet.hasNext()) {
			List<ResultItem> thisResultList = results.get(resultSet.next());

			for (int i = 0; i < thisResultList.size(); i++) {
				writer.println(gson.toJson(thisResultList.get(i)));
			}
		}

		writer.close();
		
		System.out.println("Finished writing output to " + outputDir + File.separator + "results.txt");

	}

}
