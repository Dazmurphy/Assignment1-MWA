package assignment1;

public class Department {
	private String owner_department;
	private String account;
	private String type;
	private ExternalLink external_link;
	
	public static class ExternalLink {
		private String url;
		
		public ExternalLink(String url){
			if(url != null){
				this.url = url;
			}else{
				this.url = "";
			}
		}
		
		@Override
		public String toString(){
			return url;
		}
	}
	
	public Department(final String owner_department, final String account, final String type, final ExternalLink external_link) {
	    this.owner_department = owner_department;
	    this.account = account;
	    this.type = type;
	    this.external_link = external_link;
	  }
	
	public String getOwnerDept(){
		return owner_department;
	}
	
	public String getAccount(){
		return account;
	}
	
	public String getType(){
		return type;
	}
	
	public String getExternalLink(){
		if(external_link.url != null){
			return external_link.url;
		}
		
		return "";
	}
	
	@Override
	public String toString(){
		return owner_department + " - " + account + " - " + type + " - " + external_link;
	}
}
