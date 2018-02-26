import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import java.util.Scanner;
public class PhotoShare {
	private static final String IPPort ="(\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}):(\\d{1,5})";
	private static final Pattern PATTERN = Pattern.compile(IPPort);


	public static void main(String[] args){

		//socket,argumentos, arguments, serverAdress, scanner		
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

			if(var.equals("LOGGED")) {
				System.out.println("Bem Vindo!");
				
			}else if (var.equals("WRONG")) {
				while(!var.equals("WRONG")) {
					System.out.println("palavra passe incorrecta");
					String password = input.nextLine();
					out.writeObject(password);
				}
			}else {
				System.out.println("deseja criar uma nova conta? (y/n)");
				String confirmacao = input.nextLine();
				if(confirmacao.equals("y")) {
					out.writeObject("y");
				}else {
					out.writeObject("n");
				}
			}
			if(var.equals("CREATED")){
				System.out.println("Foi criado");
			}

			
			
			//TODO nao desligar o cliente
			//TODO tentar a criacao de um cliente novo
			//TODO Escrever no ficheiro
			//TODO
			//TODO
			//TODO horario , EC as tercas e CSS a quinta depois de seguranca
			 
			
			
			
			
			
			
			
			


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
		//Colocar objects no socket



		//1-envio de user e password para autenticacao


		//		File file = new File("slb.jpg");
		//		long size = file.length();
		//		out.writeObject(size);
		//		FileInputStream inStream = new FileInputStream(file);
		//		InputStream inStream1 = new BufferedInputStream(inStream);
		//		byte buffer [] = new byte [1024];
		//		int count = 1024;
		//
		//
		//		while((count = inStream.read(buffer, 0,(int) (size<1024 ? size:1024))) >0 ){
		//			out.write(buffer, 0, count);
		//			size -=count;
		//			out.flush();
		//		}





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

		in.close();
		return result;
	}





	/**
	 * validacao de pattern
	 * @param ip - ip introduzido
	 * @return
	 */
	public static boolean validate(final String ip) {
		return PATTERN.matcher(ip).matches();
	}



}