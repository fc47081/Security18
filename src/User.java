import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	
	
	public void populateFollowers(File follow) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(follow));
		String line="";	
		while((line = reader.readLine()) != null){
			followers.add(line);
		}
	}


	
	public void removeFollowers(File follow,String follower) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(follow));
		String line="";
		
		while((line = reader.readLine()) != null){
			if (follower.equals(line)) {
				followers.remove(line);
			}
			
		}
	}
	
	public void CreateFileRemoved(File removed,String inUser) throws IOException {
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("servidor/"+inUser+"/"+removed.getName(), true)); 
		
		for (int i = 0; i < followers.size(); i++) {
			writer.write(followers.get(i));
			writer.newLine();
		}
		
	
		writer.close();
	}
	
	
	
	public void imprime() {
		for (int i = 0; i < followers.size(); i++) {
			System.out.println(followers.get(i));
		}
		
		
	}
	

	
	
	
	

}
	
	
	
