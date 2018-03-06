import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

//Servidor PhotoShareServer
public class PhotoShareServer {

	/**
	 * Verificar se o nome do ficheiro jah existe no Servidor
	 * @param listOfFiles - lista de ficheiros existentes
	 * @param nome - nome do ficheiros
	 * @return boolean de verificacao
	 */
	public static boolean existsNameFile(File[] listOfFiles,String nome) {
		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].getName().endsWith(".png") ||
					listOfFiles[i].getName().endsWith(".jpg")
					&& listOfFiles[i].getName().equals(nome)) {

				return true;
			} 
		}
		return false;
	}

	/**
	 * Get nome do file
	 * @param photo
	 * @return
	 */
	public static String getNameFile(String photo) {
		int ind = photo.indexOf(".");
		String str = photo.substring(0,ind);
		return str;

	}


	public static void main(String[] args) throws java.net.SocketException{
		File pasta = new File("servidor");
		if (!pasta.exists()) {
			pasta.mkdir();
		}
		System.out.println("Ligado , a espera de ligacao");
		PhotoShareServer server = new PhotoShareServer();
		server.startServer();
	}

	/**
	 * Trata da comunicacao com o servidor
	 */
	public void startServer (){
		ServerSocket sSoc = null;
		try {
			sSoc = new ServerSocket(23232);
		} catch (IOException e) {
			System.out.println("O servidor PhotoShare so aceita ligacoes do porto : 23232");
			System.exit(-1);
		}
		while(true) {
			try {
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		//sSoc.close();
	}

	//Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("Entrou novo cliente");
		}

		/**
		 * Run do server
		 */
		public void run(){
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
				String inUser = "";
				String inPasswd = "";

				try {
					//Autenticacao do user + password que vem do cliente
					inUser = (String)inStream.readObject();
					inPasswd = (String)inStream.readObject();			
					CatalogoUser catUser = new CatalogoUser();
					CatalogoPhotos photos = new CatalogoPhotos();
					autenticarUser(catUser,inUser, inPasswd, outStream, inStream);
					String operacao = (String)inStream.readObject();
					//operacao do client
					switch(operacao) {
					case "-a" :
						//argumento da foto
						String dirName = "servidor/"+inUser;
						File dir = new File(dirName);					
						String photo = (String) inStream.readObject();
						String temp = dirName+"/"+photo;
						boolean check = new File(temp).exists();
						File[] listOfFiles = dir.listFiles();

						if(check == false) {
							//Se nao existe, criar e guardar na pasta
							outStream.writeObject("NAO EXISTE");
							//receber o necessario do cliente e criar
							FileOutputStream outStream1 = new FileOutputStream(temp);
							OutputStream outStream2 = new BufferedOutputStream(outStream1);
							byte buffer[] = new byte [1024];
							int count;
							long size = (long) inStream.readObject();
							while((count = inStream.read(buffer, 0,(int) (size<1024 ? size:1024))) >0 ){
								outStream1.write(buffer, 0, count);
								size -=count;
								outStream2.flush();
							}
							dir.createNewFile();
							//Data da publicacao da foto 
							DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
							Date today = Calendar.getInstance().getTime();
							String reportDate = df.format(today);
							String dateToPrintToFile = reportDate;
							BufferedWriter writer = new BufferedWriter(new FileWriter("servidor/"+inUser+"/listaFotos.txt", true));
							writer.write(photo+":"+dateToPrintToFile);
							writer.newLine();
							writer.close();
							File like = new File("servidor/"+inUser+"/"+getNameFile(photo) + "Likes.txt");
							like.createNewFile();
							
						}else {
							//Se ja existe , envia para o cliente que ja existe e da exit
							if (existsNameFile(listOfFiles, photo)){
								outStream.writeObject("EXISTE");
							}
						}
						break; // optional

					case "-l" :
						
						String userPhotos = (String) inStream.readObject();
						File followers = new File("servidor/"+userPhotos+"/followers.txt");
						//catalogo para fotos
						User userPhoto = catUser.getUser(userPhotos);	 
						//popular os followers
						userPhoto.populateFollowers(followers);
						File photoList = new File("servidor/"+userPhotos+"/listaFotos.txt");
						//catalogo fotos
						
						//popular o catalogo das fotos
						photos.populate(photoList);
						ArrayList<Photo> fotos = photos.listaFotos();
						if(catUser.find(userPhotos) == true) {
							//System.out.println("aqui");
							if(userPhoto.existsFollower(inUser) == true) {
								//verificar este if nao entendo 
								//System.out.println(photos.listaFotos().size());
								outStream.writeObject(photos.listaFotos().size());
								outStream.writeObject("EXISTE");
								for (int i = 0; i < photos.listaFotos().size(); i++) {
									outStream.writeObject(fotos.get(i).getNome()+" - "+fotos.get(i).getData());			
								}
							}else {
									outStream.writeObject("NAO EXISTE");
							}
						}



						break; // optional

					case "-i" :
						// Statements
						break; // optional
					case "-g" :
						// Statements
						break; // optional

					case "-c" :
						// Statements
						break; // optional     

					case "-L" :
						String user = (String) inStream.readObject();
						String photoL = (String) inStream.readObject();
						User userLike = catUser.getUser(user);
						//verificar se e user
						if (catUser.find(user) ==true) {
							System.out.println("ENTREI IF 1");
							//verificamos se e follower
							if(userLike.existsFollower(inUser) == true) {
								System.out.println("ENTREI IF 2");
								BufferedWriter writer = new BufferedWriter(new FileWriter("servidor/"+inUser+"/"+getNameFile(photoL) + "Likes.txt", true)); 
								writer.write(user);
								writer.newLine();
								writer.close();
								outStream.writeObject("LIKE");
								
							}else {
								outStream.writeObject("NAO LIKE");
							}
						}
						
						
						
						//se for entao vai as fotos do outro e da like
						//se nao da erro
						
						break; // optional

					case "-D" :
						// Statements
						break; // optional      

					case "-f" :
						//ler o nome de quem da follow
						String followerAdd = (String) inStream.readObject();
						User uAdd = catUser.getUser(inUser);
						File follow = new File("servidor/"+inUser+"/followers.txt");
						uAdd.populateFollowers(follow);
						if(catUser.find(followerAdd) == true) {//encontrar se o user exist na lista users
							if (uAdd.existsFollower(followerAdd) ==true) {
								outStream.writeObject("Follower ja existe");
							}else{ 
								BufferedWriter writer = new BufferedWriter(new FileWriter("servidor/"+inUser+"/followers.txt", true)); 
								writer.write(followerAdd);
								writer.newLine();
								writer.close();
								outStream.writeObject("Follower adicionado");
							}
						}
						break; 

					case "-r" :
						//ler o nome de quem da follow
						String followerRemove = (String) inStream.readObject();
						User uRemove = catUser.getUser(inUser);								
						File followRem = new File("servidor/"+inUser+"/followers.txt");
						uRemove.populateFollowers(followRem);

						if(catUser.find(followerRemove) == true) { //encontrar se o user exist na lista user
							if (uRemove.existsFollower(followerRemove) ==true) {
								uRemove.removeFollowers(followRem,followerRemove);
								followRem.delete();
								File removidos = new File("servidor/"+inUser+"/followers.txt");
								removidos.createNewFile();
								uRemove.CreateFileRemoved(removidos, inUser);
								outStream.writeObject("Follower removido");
							}else {
								outStream.writeObject("Follower nao esta na lista");
							}


						} 	
						break;
					default : 

					}


				}catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}

				outStream.close();
				inStream.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Autenticacao do user
		 * @param inUser - user name
		 * @param inPasswd - user password
		 * @param outStream - stream out
		 * @param inStream - stream in
		 * @return frase - retorno a dar ao client
		 * @throws IOException
		 * @throws ClassNotFoundException 
		 */
		private void autenticarUser(CatalogoUser catUser ,String inUser, String inPasswd,ObjectOutputStream outStream,ObjectInputStream inStream) throws IOException, ClassNotFoundException {
			String frase="";
			BufferedReader reader = null;
			File utilizadores = new File ("info.txt");

			if(!utilizadores.exists())
				utilizadores.createNewFile();
			else
				catUser.populate(utilizadores);
			try {
				reader = new BufferedReader(new FileReader("info.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			//user existe
			if (catUser.find(inUser)) {
				if (catUser.pwdCerta(inUser,inPasswd)) {
					frase= "LOGGED";
					outStream.writeObject(frase);
				}else {// password errada
					frase= "WRONG";
					outStream.writeObject(frase);
					while (!catUser.getUserPwd(inUser).equals(inStream.readObject())){
						frase= "WRONG";
						outStream.writeObject(frase);
					}
					outStream.writeObject("LOGGED");


				}
			}else {//user nao existe
				frase= "CREATE";
				outStream.writeObject(frase);
				//String stream = (String) inStream.readObject();
				User user = new User(inUser, inPasswd);
				catUser.lista().add(user);
				BufferedWriter writer = new BufferedWriter(new FileWriter("info.txt", true)); 
				writer.write(inUser + ":" + inPasswd);
				writer.newLine();
				writer.close();
				File dir = new File("servidor/"+inUser);
				dir.mkdir();
				File followers = new File("servidor/"+inUser+"/"+"followers.txt");
				followers.createNewFile();
				File listPhotos = new File("servidor/"+inUser+"/"+"listaFotos.txt");
				listPhotos.createNewFile();
				
				File dislike = new File("servidor/"+inUser+"/"+"dislike.txt");
				dislike.createNewFile();
				File comentarios = new File("servidor/"+inUser+"/"+"comentarios.txt");
				comentarios.createNewFile();


			}

			reader.close();
		}

	}
}
