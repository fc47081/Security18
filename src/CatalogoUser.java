+import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CatalogoUser {
	
	private  ArrayList<user> users; 
	
	/**
	 * Construtor
	 */
	public CatalogoUser(){
		users = new ArrayList<user>();
	}
	
	
	public ArrayList<user> lista() {
		return users;
		
	}
	

	public boolean find(String user) {
		
		for (int i = 0; i < users.size(); i++) {

			if (user.equals(users.get(i).getUserName())) {
				
					return true;
			}	
		}
		return false;
		
		
	}
	
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
	 * Popular o catalogo com users
	 * @param utilizadores - ficheiro de users
	 * @throws IOException
	 */
	public void populate(File utilizadores) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(utilizadores));
		String line="";

		user user;
		while((line = reader.readLine()) != null){
			String[] split = line.split(":");
			user = new user(split[0], split[1]);
			users.add(user);
		}
	}
}