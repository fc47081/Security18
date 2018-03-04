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
import java.util.Scanner;
public class PhotoShare {

	private static final String IPPort ="(\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}):(\\d{1,5})";
	private static final Pattern PATTERN = Pattern.compile(IPPort);

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
				System.out.println("Bem Vindo!");
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

			System.out.println("Deseja realizar opera��es ? (y/n)" );
			String confirmacao = input.nextLine();
			if (confirmacao.equals("y")) {
				System.out.println("escolha uma operacao:");
				System.out.println( "[ -a <photos> | -l <userId> | -i <userId> <photo> | -g <userId> \n"
						+ "| -c <comment> <userId> <photo> | -L <userId> <photo> | \n -D <userId> <photo> | -f <followUserIds> | -r <followUserIds> ]");

				String operacao = input.nextLine();
				String[]  operacoesArgs = operacao.split(" ");
				switch(operacoesArgs[0]) {
				case "-a" :
					//envia nome do ficheiro(exemplo: a.jpg)
					out.writeObject(operacoesArgs[1]);
					//argumento da foto
					File foto = new File("Clientes/"+ arguments[0]+"/" +operacoesArgs[1]);
					long size = foto.length();
					if(foto.exists()) {
						out.writeObject(operacoesArgs[0]);
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
						}else{
							System.out.println("ja existe a foto");
						}
					}else {
						//envia uma operacao que nao existe para poder dar exit
						out.writeObject("--");
						System.out.println("A foto que quer enviar nao existe");

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
					//envia nome do user a dar follow
					out.writeObject(operacoesArgs[1]);
					//Ler reposta do server : adicionado ou ja existente
					String respostaAdd = (String)in.readObject();
					if(respostaAdd.equals("Follower adicionado") ) 
						System.out.println(respostaAdd);
					else
						System.out.println(respostaAdd);
					break; // optional

				case "-r" :
					//envia nome do user a remover
					out.writeObject(operacoesArgs[1]);
					//Ler reposta do server : removido ou nunca existiu
					String respostaRem = (String)in.readObject();
					if(respostaRem.equals("Follower removido") ) 
						System.out.println(respostaRem);
					else
						System.out.println(respostaRem);
					break; // optional
				default : 
				}
			}else {

				System.out.println("Nao pode realizar mais operacoes");

			}
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
