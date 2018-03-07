import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Photo {

	private String nome;
	private String data;
	private ArrayList<String> likes;
	public Photo(String nome,String data) {
		this.nome = nome;
		this.data = data;
		likes = new ArrayList<String>();
	}
	
	public String getNome() {
		return nome;
	}
	
	public String getData() {
		return data;
	}
	
	
	public void populateLikes(File userLikes) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(userLikes));
		String line="";
		while((line = reader.readLine()) != null){
			likes.add(line);
		}
			
		
		
		
	}
		
	public ArrayList<String> getlistUserLikes(){		
		return likes;
	}
	
	
	
	
	public boolean deuLike(String user) throws IOException {
		for (int i = 0; i < likes.size(); i++) {
			if (likes.get(i).equals(user)) {
				return true;
			}
		
		}
	
		return false;

	}
	
	public void imprime() {
		for (int i = 0; i < likes.size(); i++) {
			System.out.println("pos"+i+" "+likes.get(i));
		}
		
	}
	
	
	
}
