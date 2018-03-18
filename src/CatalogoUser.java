import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CatalogoUser {
	
	private  ArrayList<User> users; 
	
	/**
	 * Construtor
	 */
	public CatalogoUser(){
		users = new ArrayList<User>();
	}
	
	/**
	 * Lista de users
	 * @return users
	 */
	public ArrayList<User> lista() {
		return users;	
	}

	/**
	 * Find user name
	 * @param user - nome do user
	 * @return boolean de verificacao
	 */
	public boolean find(String user) {
		for (int i = 0; i < users.size(); i++) {
			if (user.equals(users.get(i).getUserName()))				
					return true;
		}
		return false;
	}
	
	/**
	 * Get nome do user
	 * @return UserName 
	 */
	public User getUser(String username) {
		for (int i = 0; i < users.size(); i++) {
			if (username.equals(users.get(i).getUserName()))
					return users.get(i);
		}
		return null;
	}
	
	/**
	 * Verifica se password esta correta
	 * @param user - user name
	 * @param pwd - passwrod
	 * @return boolean de verificacao
	 */
	public boolean pwdCerta(String user,String pwd) {
		for (int i = 0; i < users.size(); i++) {
			if (user.equals(users.get(i).getUserName())) {
				if (pwd.equals(users.get(i).getPassword())) 
					return true;	
			}
		}
		return false;
	}
	
	/**
	 * Get password do user user
	 * @param user - user name
	 * @return users.get(i).getPassword()
	 */
	public String getUserPwd(String user) {
		for (int i = 0; i < users.size(); i++) {
			if (user.equals(users.get(i).getUserName())) {
				return users.get(i).getPassword();
			}
		}
		return null;
	}
	
	/**
	 * Popular o catalogo com users
	 * @param utilizadores - ficheiro de users
	 * @throws IOException
	 */
	public void populate(File utilizadores) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(utilizadores));
		String line="";
		User user;
		
		while((line = reader.readLine()) != null){
			String[] split = line.split(":");
			user = new User(split[0], split[1]);
			users.add(user);
		}
		reader.close();
	}
}
