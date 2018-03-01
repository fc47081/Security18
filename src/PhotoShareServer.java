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

//Servidor PhotoShareServer
public class PhotoShareServer {

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
	
	
//	public String getExtension(String nome) {
//		int indice = nome.indexOf(".");
//		String str = null;
//		for (int i = indice+1; i < nome.length(); i++) {
//			str+= nome.charAt(i);
//		}
//	return str;
//	}
	
	
	
	
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
					inUser = (String)inStream.readObject();
					inPasswd = (String)inStream.readObject();
					autenticarUser(inUser, inPasswd, outStream, inStream);
					
					
					
					//ler  a operação do ouro lado
				
					
				//String clienteAsw = (String) inStream.readObject();
				//recebe a confirmacao
				//outStream.writeObject(clienteAsw); 	
					String photo = (String) inStream.readObject();
					
					String operacao = (String)inStream.readObject();
					
				
					switch(operacao) {
					case "-a" :
						//lançar para o cliente a operação
						//outStream.writeObject(operacao);

						//argumento da foto
						String dirName = "servidor/"+inUser;
						File dir = new File(dirName);
						String temp = dirName+"/"+photo;
						boolean check = new File(temp).exists();
						File[] listOfFiles = dir.listFiles();
						
						//System.out.println(check);
						//boolean checkClient = inStream.readBoolean();
						//System.out.println("passei o read");
						//boolean client = inStream.readBoolean();
						if(check == false) {
							//outStream.writeObject(new Boolean(true));
							//lançar a foto do cliente para o servidor
							outStream.writeObject("NAO EXISTE");
							
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

						}else {
							
							if (existsNameFile(listOfFiles, photo)){
								outStream.writeObject("EXISTE");
							}
						}
							
						break; // optional

					case "-l" :
						// Statements
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
						// Statements
						break; // optional

					case "-r" :
						// Statements
						break; // optional
						// You can have any number of case statements.
					default : 
						
					}
					

				}catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
				//
				//				FileOutputStream outStream1 = new FileOutputStream("slbcopia.jpg");
				//				OutputStream outStream2 = new BufferedOutputStream(outStream1);
				//				byte buffer[] = new byte [1024];
				//				int count;
				//				long size = (long) inStream.readObject();
				//
				//				while((count = inStream.read(buffer, 0,(int) (size<1024 ? size:1024))) >0 ){
				//					outStream1.write(buffer, 0, count);
				//					size -=count;
				//					outStream2.flush();
				//				}

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
		private void autenticarUser(String inUser, String inPasswd,ObjectOutputStream outStream,ObjectInputStream inStream) throws IOException, ClassNotFoundException {
			String frase="";
			BufferedReader reader = null;
			File utilizadores = new File ("info.txt");
			CatalogoUser catUser = new CatalogoUser();

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
			}else {//user não existe
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
