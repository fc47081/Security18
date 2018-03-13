import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import javax.swing.plaf.FontUIResource;

import java.util.Scanner;
public class PhotoShare {

	private static final String IPPort ="(\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}):(\\d{1,5})";
	private static final Pattern PATTERN = Pattern.compile(IPPort);

	public  static String concatenateComment(String[] args) {
		String comentario = "";
		for (int i = 1; i < args.length-2; i++) {
			comentario+= args[i]+" ";
		}

		return comentario;
	}


	public static void main(String[] args){

		File pasta = new File("Clientes");
		if (!pasta.exists()) {
			pasta.mkdir();
		}
		//socket, argumentos, arguments, serverAdress
		Socket listeningSocket = null;
		String [] argumentos = args;
		Scanner input = new Scanner(System.in);
		String [] arguments = verificaArgs(argumentos,input);
		String [] serverAdress = arguments[2].split(":");
		//System.out.println(serverAdress[1]);
		try {
			listeningSocket = new Socket(serverAdress[0],Integer.parseInt(serverAdress[1]));

		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}

		try {
			ObjectInputStream in = new ObjectInputStream(listeningSocket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(listeningSocket.getOutputStream());
			out.writeObject(arguments[0]);
			out.writeObject(arguments[1]);
			String var = (String) in.readObject();

			// user e pass -> OK
			if(var.equals("LOGGED")) {
				System.out.println("Bem Vindo "+ arguments [0]  + "!");
				//user OK mas password NOK	
			}else if (var.equals("WRONG")) {
				//pede a pass ate ficar correta
				System.out.println("palavra passe incorrecta");
				String password = input.nextLine();
				out.writeObject(password);
				while (!((String)in.readObject()).equals("LOGGED")) {
					System.out.println("falhei");
					password = input.nextLine();
					out.writeObject(password);
				}

				//user NOK , entao criar um user novo
			}else if(var.equals("CREATE")) {
				System.out.println(" O user novo foi criado");
				String dirName = "Clientes/"+ arguments[0];
				File dir = new File(dirName);
				dir.mkdir();
			}

			System.out.println("Deseja realizar operacoes ? (y/n)" );
			String confirmacao = input.nextLine();
			out.writeObject(confirmacao);


			while(confirmacao.equals("y")) {
				System.out.println("escolha uma operacao:");
				System.out.println( "[ -a <photos> | -l <userId> | -i <userId> <photo> | -g <userId> \n"
						+ "| -c <comment> <userId> <photo> | -L <userId> <photo> | \n -D <userId> <photo> | -f <followUserIds> | -r <followUserIds> ]");

				String operacao = input.nextLine();
				String[]  operacoesArgs = operacao.split(" ");
				//String comentario = 
				switch(operacoesArgs[0]) {
				case "-a" :
					//envia nome do ficheiro(exemplo: a.jpg)
					//argumento da foto
					File foto = new File("Clientes/"+arguments[0]+"/"+operacoesArgs[1]);
					long size = foto.length();
					if(foto.exists()) {
						out.writeObject(operacoesArgs[0]);
						out.writeObject(operacoesArgs[1]);
						String existe = (String) in.readObject();
						if (existe.equals("NAO EXISTE")) {
							FileInputStream inStream1 = new FileInputStream(foto);
							InputStream inStream2 = new BufferedInputStream(inStream1);
							byte buffer[] = new byte[1024];
							int count=1024;
							out.writeObject(foto.length());
							while((count = inStream1.read(buffer, 0,(int) (size<1024 ? size:1024))) >0 ){
								out.write(buffer, 0, count);
								size -=count;
								out.flush();	
							}
							String transfer = (String)in.readObject();
							if(transfer.equals("TRANSFERIDA")) {
								System.out.println("A foto foi transferida com sucesso");
							}

						}else{
							out.writeObject(operacoesArgs[0]);

							System.out.println("ja existe a foto");
						}
					}else {
						//envia uma operacao que nao existe para poder dar exit
						//out.writeObject("--");
						System.out.println("A foto que quer enviar nao existe");

					}
					break;

				case "-l" :
					out.writeObject(operacoesArgs[0]);
					out.writeObject(operacoesArgs[1]);
					//System.out.println(operacoesArgs[1]);
					//System.out.println("tamanho "+tamanho);
					String segue = (String) in.readObject();
					if (segue.equals("EXISTE")) {
						int tamanho =(int) in.readObject();
						System.out.println("Lista de fotos:");
						for (int i = 0; i < tamanho; i++) {
							String nomeData = (String) in.readObject(); 
							System.out.println(nomeData);
						}

					}else if (segue.equals("NAO EXISTE USER")) {
						System.out.println("O user que introduziu nao existe");						

					}else {
						System.out.println("Nao é follower");
					}



					break;

				case "-i" :
					//operacaco
					out.writeObject(operacoesArgs[0]);
					//user
					out.writeObject(operacoesArgs[1]);
					//photo
					out.writeObject(operacoesArgs[2]);
					String mostra = (String) in.readObject();
					if (mostra.equals("MOSTRA")) {

						int commentSize = (int) in.readObject();
						System.out.println("Lista de comentarios:");
						for (int i = 0; i < commentSize; i++) {
							String comentario = (String) in.readObject();
							System.out.println(comentario);
						}

						int likesSize = (int) in.readObject();

						int dislikeSize = (int) in.readObject();

						System.out.println("Numero de likes: " + likesSize);
						System.out.println("Numero de dislikes: " +dislikeSize);

					}else if (mostra.equals("NAO FOTO")) {
						System.out.println("Nao existe a foto");


					}else if(mostra.equals("NAO FOLLOWER")) {
						System.out.println("Nao e follower deste user");

					}else{
						System.out.println("User inválido");

					}



					break; // optional
				case "-g" :

					//op
					out.writeObject(operacoesArgs[0]);		
					//user
					out.writeObject(operacoesArgs[1]);

					int vector = in.read();
					//System.out.println(vector);
					//System.out.println(vector);
					String msg= (String) in.readObject();
					//System.out.println("mandei isto "+msg);
					if (msg.equals("Fotos enviadas")) {


						for (int i = 0; i < vector; i++) {
							String fotografia = (String) in.readObject();
							//System.out.println(fotografia);
							FileOutputStream outStream1 = new FileOutputStream("Clientes/"+arguments[0]+"/"+fotografia);
							OutputStream outStream2 = new BufferedOutputStream(outStream1);
							byte buffer[] = new byte [1024];
							int count;
							long leng =(long) in.readObject();
							while((count = in.read(buffer, 0,(int) (leng<1024 ? leng:1024))) >0 ){
								System.out.println(count);
								outStream1.write(buffer, 0, count);
								leng -=count;
								outStream2.flush();
							}

						}
						System.out.println("Transferencia efectuada com sucesso");
					}else if (msg.equals("Nao Follower")) {
						System.out.println("Nao e follower");
					}else {
						System.out.println("Nao e user");
					}
					break; // optional

				case "-c" :
					String comment = concatenateComment(operacoesArgs);
					int len = operacoesArgs.length;
					out.writeObject(operacoesArgs[0]);
					//user
					out.writeObject(comment);

					out.writeObject(operacoesArgs[len-2]);
					//photo
					out.writeObject(operacoesArgs[len-1]);


					String comentario = (String) in.readObject();

					if (comentario.equals("COMMENT")) {
						System.out.println("Comentario efectuado com sucesso");
					}else if(comentario.equals("NAO FOLLOWER")){
						System.out.println("Nao e follower deste user");
					}else if(comentario.equals("NAO FOTO")){
						System.out.println("Nao existe a foto que pretende");
					}else {
						System.out.println("User inválido");
					}	
					break; // optional     

				case "-L" :
					//operacao
					out.writeObject(operacoesArgs[0]);
					//user
					out.writeObject(operacoesArgs[1]);
					//photo
					out.writeObject(operacoesArgs[2]);
					String like = (String) in.readObject();
					if (like.equals("LIKE")) {
						System.out.println("Like efectuado com sucesso");

					}else if(like.equals("JADEULIKE")){
						System.out.println("Já deu like anteriormente");		

					}else if(like.equals("NAO LIKE")){
						System.out.println("Nao e follower deste user");

					}else if(like.equals("NAO FOTO")){
						System.out.println("Nao existe a foto que pretende");
					}else {
						System.out.println("User inválido");
					}

					break; // optional

				case "-D" :
					out.writeObject(operacoesArgs[0]);
					//user
					out.writeObject(operacoesArgs[1]);
					//photo
					out.writeObject(operacoesArgs[2]);
					String dislike = (String) in.readObject();
					if (dislike.equals("DISLIKE")) {
						System.out.println("Dislike efectuado com sucesso");

					}else if(dislike.equals("JADEUDISLIKE")){
						System.out.println("Já deu dislike anteriormente");		

					}else if(dislike.equals("NAO DISLIKE")){
						System.out.println("Nao e follower deste user");

					}else if(dislike.equals("NAO FOTO")){
						System.out.println("Nao existe a foto que pretende");
					}else {
						System.out.println("User inválido");
					}
					break; // optional      

				case "-f" :
					//envia nome do user a dar follow + op
					out.writeObject(operacoesArgs[0]);
					out.writeObject(operacoesArgs[1]);
					//Ler reposta do server : adicionado ou ja existente
					String respostaAdd = (String)in.readObject();
					if(respostaAdd.equals("Follower adicionado") ) {
						System.out.println(respostaAdd);
					}else if (respostaAdd.equals("Follower ja existe")) {
						System.out.println(respostaAdd);
					}else {
						System.out.println(respostaAdd);
					}

					break; // optional

				case "-r" :
					//envia nome do user a remover
					out.writeObject(operacoesArgs[0]);
					out.writeObject(operacoesArgs[1]);
					//Ler reposta do server : removido ou nunca existiu
					String respostaRem = (String)in.readObject();
					//System.out.println(respostaRem);
					if(respostaRem.equals("Follower removido") ) 
						System.out.println(respostaRem);
					else if (respostaRem.equals("Follower nao esta na lista")) {
						System.out.println(respostaRem);
					}else {
						System.out.println(respostaRem);
					}

					break;
				default : 
				}

				System.out.println("Deseja realizar operacoes ? (y/n)" );
				confirmacao = input.nextLine();
				out.writeObject(confirmacao);

			}

			System.out.println("Nao pode realizar mais operacoes");


			input.close();
			in.close();
			out.close();
		} catch (Exception e) {
			try {
				listeningSocket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Verifica argumentos de entrada
	 * @param args - argumentos na consola
	 * @param password - password do user
	 * @param localUserID - username
	 * @param serverAdress - ip:porto para ligacao
	 * @param in - scanner de leitura
	 */
	private static String[] verificaArgs(String[] args,Scanner in){
		String[] result = new String [3];
		if(args.length < 3){ //nao contiver o necessario
			if(args.length <= 1){
				System.out.println(" PhotoShare <localUserID> <password> <serverAdress>");
			}else if(args.length == 2){
				result[0] = args[0];
				if(validate(args[1])){
					result[2] = args[1];
					String pass = "";
					while(pass.equals("")){
						System.out.println("Falta a password volte a inserir:");
						pass = in.nextLine();
					}
					result[1] = pass;
				}else{
					System.out.println("Falta Ip:Port");

					result[2] =	in.nextLine();
				}
			}
		}else{
			result[0] = args[0];
			result[1] = args[1];
			result[2] = args[2];
		}
		return result;
	}

	/**
	 * Validacao de pattern
	 * @param ip - ip introduzido
	 * @return pattern
	 */
	public static boolean validate(final String ip) {
		return PATTERN.matcher(ip).matches();
	}





}
