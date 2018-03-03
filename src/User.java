import java.util.ArrayList;

public class User {

	private String UserName;
	private String password;
	private ArrayList<String> followers;

	/**
	 * Construtor
	 * @param userName
	 * @param password
	 */
	public User(String userName,String password ) {
		this.UserName = userName;
		this.password = password;
		followers = new ArrayList<String>();
		
	}
	
	/**
	 * Get nome do user
	 * @return UserName 
	 */
	public String getUserName() {
		return UserName;
	}

	/**
	 * Get password do user
	 * @return password
	 */
	public String getPassword() {
		return password;
		
	}
	/**
	 * Get followers do user
	 * @return followers
	 */
	public ArrayList<String> getFollowers() {
		return followers;	
	}
	
	
	
	
}
