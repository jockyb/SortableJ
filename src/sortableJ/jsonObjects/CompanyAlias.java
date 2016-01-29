package sortableJ.jsonObjects;

/**
 * 
 * @author Joshua Bowles
 *
 */
public class CompanyAlias {

	private String companyName;
	private String[] aliases;

	CompanyAlias() {

	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String[] getAliases() {
		return aliases;
	}

	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}

}
