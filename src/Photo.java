
public class Photo {

	private String nome;
	private String data;
	private int likes;
	private int dislikes;
	
	public Photo(String nome,String data) {
		this.nome = nome;
		this.data = data;
		this.likes = 0;
		this.dislikes=0;
		
	}
	
	public String getNome() {
		return nome;
	}
	
	public String getData() {
		return data;
	}
	
	public int getLikes() {
		return likes;
	}
	
	public int getDislikes() {
		return dislikes;
	}
	
	
}
