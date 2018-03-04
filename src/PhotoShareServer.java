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
					autenticarUser(catUser,inUser, inPasswd, outStream, inStream);
					String operacao = (String)inStream.readObject();
					//operacao do client
					switch(operacao) {
					case "-a" :
						//argumento da foto
						String dirName = "servidor/"+inUser;
						File dir = new File(dirName);					
						String photo = (String) inStream.readObject();
						String temp = dirName+"/"+ photo;
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
							File ficheiro = new File(dirName+"/"+getNameFile(photo)+".txt");
							ficheiro.createNewFile();
							//Data da publicacao da foto 
							DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
							Date today = Calendar.getInstance().getTime();
							String reportDate = df.format(today);
							String dateToPrintToFile = reportDate;
							BufferedWriter writer = new BufferedWriter(new FileWriter(dirName+"/"+getNameFile(photo)+".txt", true));
							writer.write("Data:"+dateToPrintToFile);
							//Fim data
							writer.newLine();
							writer.close();
						}else {
							//Se ja existe , envia para o cliente que ja existe e da exit
							if (existsNameFile(listOfFiles, photo)){
								outStream.writeObject("EXISTE");
							}
						}
						break; // optional

					case "-l" :
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
						// Statements
						break; // optional

					case "-D" :
						// Statements
						break; // optional      

					case "-f" :
						//ler o nome de quem da follow
						String followerAdd = (String) inStream.readObject();
						if(catUser.find(followerAdd) == true) {//encontrar se o user exist na lista users
							User uAdd = catUser.getUser(inUser);
							uAdd.Follower();
							if (uAdd.existsFollower(followerAdd) ==true) {
								System.out.println("ENTREI NO EXISTSFOLLOWER");
								outStream.writeObject("Follower ja existe");
							}else{ 
								uAdd.getFollowersList().add(followerAdd);
								outStream.writeObject("Follower adicionado");
							}
						}
					break; 

				case "-r" :
					//ler o nome de quem da follow
					System.out.println("ENTREI NA CONA DA MARIA");
					String followerRemove = (String) inStream.readObject();
					User uRemove = catUser.getUser(inUser);								
					if(catUser.find(followerRemove)) { //encontrar se o user exist na lista users
						uRemove.getFollowersList().remove((followerRemove));
						outStream.writeObject("Follower removido");
					}else if (!uRemove.existsFollower(followerRemove)) {
						outStream.writeObject("Follower nao esta na lista");
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
		}

		reader.close();
	}

}
}
