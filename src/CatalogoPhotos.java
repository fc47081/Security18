import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CatalogoPhotos {

	private ArrayList<Photo> fotos;
	
	public CatalogoPhotos() {
		fotos = new ArrayList<Photo>();
			
	}
	
	
	public void populate(File photos) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(photos));
		String line="";
		Photo photo;
		
		while((line = reader.readLine()) != null){
			String[] split = line.split(":");
			photo = new Photo(split[0], split[1]+":"+split[2]+":"+split[3]);
			fotos.add(photo);
		}
	}
	
	
	public ArrayList<Photo> listaFotos() {
		return fotos;
		
	}
	
	public boolean existsPhoto(String foto) throws IOException {
		for (int i = 0; i < fotos.size(); i++) {
			if (fotos.get(i).getNome().equals(foto)) {
				return true;
			}
		}
		return false;
	}
	
	
	
}
