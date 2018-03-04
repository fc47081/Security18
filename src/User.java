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
	public ArrayList<String> getFollowersList() {//String UserName
		return followers;	
	}

	/**
	 * existsFollower
	 * @param username - nome do user a procurar
	 * @return true or false
	 */
	public boolean existsFollower(String username) {
		for (int i = 0; i < followers.size(); i++) {
			if (username.equals(followers.get(i)))
				return true;
		}
		return false;
	}
	public void Follower() {
		for (int i = 0; i < followers.size(); i++) {
			System.out.println("LISTA:   " + followers.get(i));
		}

	}
}