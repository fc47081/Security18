public class User {

	private String UserName;
	private String password;

	/**
	 * Construtor
	 * @param userName
	 * @param password
	 */
	public User(String userName,String password ) {
		this.UserName = userName;
		this.password = password;
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
}