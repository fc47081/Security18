package com.sc010.server;

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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import com.sc010.server.CatalogoUser;
import com.sc010.utils.Utils;

//Servidor PhotoShareServer
public class PhotoShareServer {
	private  static byte [] salt = new byte [16];
	private static  byte [] ivBytes = { 0x11, 0x37, 0x69, 0x1F, 0x3D, 0x5A, 0x04, 0x18, 0x23, 0x6B, 0x1F, 0x03, 0x1D, 0x1E, 0x1F, 0x20 };
	private static final String  pwdAdmin= "Tree Math Water";

	public static void main(String[] args) throws java.net.SocketException {
		File pasta = new File("servidor");
		if (!pasta.exists()) {
			pasta.mkdir();
		}
		// Setup keysure
		System.setProperty("java.security.policy", "server.policy");
		System.setSecurityManager(new SecurityManager());
		System.setProperty("javax.net.ssl.trustStore", "myServer.keyStore");
		System.setProperty("javax.net.ssl.trustStorePassword", "paparuco");

		System.out.println("Ligado , a espera de ligacao");
		PhotoShareServer server = new PhotoShareServer();
		server.startServer();
	}

	/**
	 * Trata da comunicacao com o servidor
	 */
	public void startServer() {
		SSLServerSocket ss = null;
		try {

			Scanner sc = new Scanner(System.in);
			System.out.println("Introduza password de acesso");
			String pw = sc.nextLine();
			Utils.createMac("Users/users.mac", pw);
			sc.close();
			ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
			ss = (SSLServerSocket) ssf.createServerSocket(23232);
			// sSoc = new ServerSocket(23232);
			// sSoc.getLocalPort();
			// System.out.println(sSoc.getLocalPort());

		} catch (IOException e) {
			System.out.println("O servidor PhotoShare so aceita ligacoes do porto : 23232");
			e.printStackTrace();
			System.exit(-1);
		}
		while (true) {
			try {
				ServerThread newServerThread = new ServerThread(ss.accept());
				newServerThread.start();
				// sSoc.close();
			} catch (IOException e) {
				e.printStackTrace();
				if (ss != null && !ss.isClosed()) {
					try {
						ss.close();
					} catch (IOException err) {
						err.printStackTrace();
					}
				}
			}
		}

	}

	// Threads utilizadas para comunicacao com os clientes
	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket socket2) {
			socket = socket2;
			System.out.println("Entrou novo cliente");
		}

		/**
		 * Run do server
		 */
		public void run() {
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				String inUser = "";
				String inPasswd = "";

				try {
					// Autenticacao do user + password que vem do cliente
					inUser = (String) inStream.readObject();
					inPasswd = (String) inStream.readObject();
					CatalogoUser catUser = new CatalogoUser();
					CatalogoPhotos photos = new CatalogoPhotos();
					autenticarUser(catUser, inUser, inPasswd, outStream, inStream);

					String operacao = (String) inStream.readObject();
					switch (operacao) {
					case "-a":
						operationA(inUser, inStream, outStream);
						break; 
					case "-l":
						operationMiniL(inStream, outStream, catUser, inUser, photos);
						break;

					case "-i":
						operationI(inStream, outStream, catUser, inUser, photos);
						break;
					case "-g":
						operationG(inStream, outStream, catUser, inUser);
						break;
					case "-c":
						operationC(inStream, outStream, catUser, inUser, photos);
						break; 
					case "-L":
						operationL(inStream, outStream, catUser, inUser, photos);
						break; 
					case "-D":
						operationD(inStream, outStream, catUser, inUser, photos);
						break; 
					case "-f":
						operationF(inStream, inUser, catUser, outStream);
						break;
					case "-r":
						operatioR(inStream, outStream, catUser, inUser);
						break;
					default:

					}
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
				outStream.close();
				inStream.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Autenticacao do user
	 * 
	 * @param inUser
	 *            - user name
	 * @param inPasswd
	 *            - user password
	 * @param outStream
	 *            - stream out
	 * @param inStream
	 *            - stream in
	 * @return frase - retorno a dar ao client
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void autenticarUser(CatalogoUser catUser, String inUser, String inPasswd, ObjectOutputStream outStream,
			ObjectInputStream inStream) {
		try {
			String frase = "";
			BufferedReader reader = null;
			File utilizadores = new File("Users/users.txt");
			if (!utilizadores.exists())
				utilizadores.createNewFile();
			else
				catUser.populate(utilizadores);
			try {
				reader = new BufferedReader(new FileReader(utilizadores));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			// user existe
			if (catUser.find(inUser)) {
				if (catUser.pwdCerta(inUser, inPasswd)) {
					frase = "LOGGED";
					outStream.writeObject(frase);
				} else {// password errada
					frase = "WRONG";
					outStream.writeObject(frase);
					while (!catUser.getUserPwd(inUser).equals(inStream.readObject())) {
						frase = "WRONG";
						outStream.writeObject(frase);
					}
					outStream.writeObject("LOGGED");
				}
			}
			reader.close();
		} catch (Exception e) {
			System.err.println("erro de autenticacao");
		}

	}

	/**
	 * Operacao -a
	 * 
	 * @param inUser
	 *            - user recebido na operacao
	 * @param inStream
	 *            - reader do cliente
	 * @param outStream
	 *            - writer para o cliente
	 */
	public static void operationA(String inUser, ObjectInputStream inStream, ObjectOutputStream outStream) {
		try {
			//Gerar a chave aleatori k com AES
			KeyGenerator k = KeyGenerator.getInstance("AES");
			k.init(256); // for example
			SecretKey secretKey = k.generateKey();

			Cipher c = Cipher.getInstance("AES");
			c.init(Cipher.ENCRYPT_MODE, secretKey);


			String dirName = "servidor/" + inUser;
			File dir = new File(dirName);
			String photo = (String) inStream.readObject();
			String temp = dirName + "/" + photo;
			boolean check = new File(temp).exists();
			File[] listOfFiles = dir.listFiles();

			if (check == false) {
				// Se nao existe, criar e guardar na pasta
				outStream.writeObject("NAO EXISTE");
				// receber o necessario do cliente e criar
				FileOutputStream outStream1 = new FileOutputStream(temp);
				CipherOutputStream cos = new CipherOutputStream(outStream1, c);
				byte buffer[] = new byte[1024];
				int count;
				long size = (long) inStream.readObject();
				while ((count = inStream.read(buffer, 0, (int) (size < 1024 ? size : 1024))) > 0) {
					cos.write(buffer, 0, count);
					size -= count;
					cos.flush();
				}
				PBEKeySpec keySpec = new PBEKeySpec("Tree Math Water".toCharArray());
				SecretKeyFactory kf;
				try {
					kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");

					SecretKey key = kf.generateSecret(keySpec);

					IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
					PBEParameterSpec spec = new PBEParameterSpec(salt,20, ivSpec);

					Cipher c1 = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
					c1.init(Cipher.ENCRYPT_MODE, key, spec);
					
					FileOutputStream outStream2 = new FileOutputStream(new File(temp + ".key"));
					CipherOutputStream cos2 = new CipherOutputStream(outStream1, c);
					
					cos2.write(c1.doFinal(key.getEncoded()));
					cos2.close();
					outStream2.close();
				} catch (InvalidKeySpecException e) {
					e.printStackTrace();
				} catch (InvalidAlgorithmParameterException e) {
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					e.printStackTrace();
				} catch (BadPaddingException e) {
					e.printStackTrace();
				}
				dir.createNewFile();
				// Data da publicacao da foto
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				Date today = Calendar.getInstance().getTime();
				String reportDate = df.format(today);
				String dateToPrintToFile = reportDate;
				BufferedWriter writer = new BufferedWriter(
						new FileWriter("servidor/" + inUser + "/listaFotos.txt", true));
				writer.write(photo + ":" + dateToPrintToFile);
				writer.newLine();
				writer.close();
				File like = new File("servidor/" + inUser + "/" + getNameFile(photo) + "Likes.txt");
				like.createNewFile();
				File dislike = new File("servidor/" + inUser + "/" + getNameFile(photo) + "Dislikes.txt");
				dislike.createNewFile();
				File comments = new File("servidor/" + inUser + "/" + getNameFile(photo) + "Comments.txt");
				comments.createNewFile();
				outStream.writeObject("TRANSFERIDA");
				cos.close();
			} else {
				// Se ja existe , envia para o cliente que ja existe e da exit
				if (existsNameFile(listOfFiles, photo)) {
					System.out.println("existe o ficheiro");
					outStream.writeObject("EXISTE");
				}
			}
		} catch (IOException e) {
			System.err.println("erro de leitura");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Operação -f
	 * 
	 * @param inUser
	 *            - user recebido na operacao
	 * @param inStream
	 *            - reader do cliente
	 * @param outStream
	 *            - writer para o cliente
	 * @param catUser
	 *            - catalogo de users
	 */
	public static void operationF(ObjectInputStream inStream, String inUser, CatalogoUser catUser,
			ObjectOutputStream outStream) {
		try {
			String followerAdd = (String) inStream.readObject();
			User uAdd = catUser.getUser(inUser);
			File follow = new File("servidor/" + inUser + "/followers.txt");
			uAdd.populateFollowers(follow);
			if (catUser.find(followerAdd) == true) {// encontrar se o user exist na lista users
				if (uAdd.existsFollower(followerAdd) == true) {
					outStream.writeObject("Follower ja existe");
				} else {
					BufferedWriter writer = new BufferedWriter(
							new FileWriter("servidor/" + inUser + "/followers.txt", true));
					writer.write(followerAdd);
					writer.newLine();
					writer.close();
					outStream.writeObject("Follower adicionado");
				}
			} else {
				outStream.writeObject("follower nao existe");
			}
		} catch (IOException e) {
			System.err.println("erro de leitura");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Operacao -r
	 * 
	 * @param inStream-
	 *            reader do cliente
	 * @param outStream-
	 *            writer para o cliente
	 * @param catUser-
	 *            catalogo de users
	 * @param inUser-
	 *            user recebido na operacao
	 */
	public static void operatioR(ObjectInputStream inStream, ObjectOutputStream outStream, CatalogoUser catUser,
			String inUser) {
		try {
			String followerRemove = (String) inStream.readObject();
			System.out.println(followerRemove);
			User uRemove = catUser.getUser(inUser);
			File followRem = new File("servidor/" + inUser + "/followers.txt");
			uRemove.populateFollowers(followRem);
			if (catUser.find(followerRemove) == true) { // encontrar se o user exist na lista user
				if (uRemove.existsFollower(followerRemove) == true) {
					System.out.println("follow:");
					uRemove.imprime();
					uRemove.removeFollowers(followRem, followerRemove);
					System.out.println("removido:");
					uRemove.imprime();
					followRem.delete();
					File removidos = new File("servidor/" + inUser + "/followers.txt");
					removidos.createNewFile();
					uRemove.CreateFileRemoved(removidos, inUser);
					outStream.writeObject("Follower removido");
				} else {
					outStream.writeObject("Follower nao esta na lista");
				}
			} else {
				outStream.writeObject("Follower nao existe");
			}
		} catch (IOException e) {
			System.err.println("erro de leitura");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Operacao -L
	 * 
	 * @param inStream
	 *            - reader do cliente
	 * @param outStream
	 *            - writer para o cliente
	 * @param catUser
	 *            - catalogo de users
	 * @param inUser
	 *            - user recebido na operacao
	 * @param photos
	 *            - catalogo de fotos
	 */
	public static void operationL(ObjectInputStream inStream, ObjectOutputStream outStream, CatalogoUser catUser,
			String inUser, CatalogoPhotos photos) {
		try {
			String user = (String) inStream.readObject();
			String photoL = (String) inStream.readObject();
			File followLike = new File("servidor/" + user + "/followers.txt");
			File listaFotos = new File("servidor/" + user + "/listaFotos.txt");
			// verificar se e user
			if (catUser.find(user) == true) {
				User userLike = catUser.getUser(user);
				userLike.populateFollowers(followLike);
				System.out.println(userLike.existsFollower(inUser));
				// verificamos se e follower
				if (userLike.existsFollower(inUser) == true) {

					photos.populate(listaFotos);
					if (photos.existsPhoto(photoL) == true) {
						Photo phototemp = photos.getPhoto(photoL);
						File ficheiroLikes = new File("servidor/" + user + "/" + getNameFile(photoL) + "Likes.txt");
						phototemp.populateLikes(ficheiroLikes);
						if (phototemp.deuLike(inUser) == false) {
							BufferedWriter writer = new BufferedWriter(
									new FileWriter("servidor/" + user + "/" + getNameFile(photoL) + "Likes.txt", true));
							writer.write(inUser);
							writer.newLine();
							writer.close();
							outStream.writeObject("LIKE");
						} else {
							outStream.writeObject("JADEULIKE");
						}
					} else {
						outStream.writeObject("NAO FOTO");
					}
				} else {
					outStream.writeObject("NAO LIKE");
				}
			} else {
				outStream.writeObject("NAO USER");
			}
		} catch (IOException e) {
			System.err.println("erro de leitura");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Operacao -D
	 * 
	 * @param inStream
	 *            - reader do cliente
	 * @param outStream
	 *            - writer para o cliente
	 * @param catUser
	 *            - catalogo de users
	 * @param inUser
	 *            - user recebido na operacao
	 * @param photos
	 *            - catalogo de fotos
	 */
	public static void operationD(ObjectInputStream inStream, ObjectOutputStream outStream, CatalogoUser catUser,
			String inUser, CatalogoPhotos photos) {
		try {
			String userD = (String) inStream.readObject();
			String photoD = (String) inStream.readObject();
			File followDislike = new File("servidor/" + userD + "/followers.txt");
			File listFotos = new File("servidor/" + userD + "/listaFotos.txt");
			photos.populate(listFotos);
			// verificar se e user
			if (catUser.find(userD) == true) {
				User userDislike = catUser.getUser(userD);
				userDislike.populateFollowers(followDislike);
				// verificamos se e follower
				if (userDislike.existsFollower(inUser) == true) {
					if (photos.existsPhoto(photoD) == true) {
						Photo phototemp = photos.getPhoto(photoD);
						File ficheiroDislikes = new File(
								"servidor/" + userD + "/" + getNameFile(photoD) + "Dislikes.txt");
						phototemp.populateDislikes(ficheiroDislikes);
						if (phototemp.deuDislike(inUser) == false) {
							BufferedWriter writer = new BufferedWriter(new FileWriter(
									"servidor/" + userD + "/" + getNameFile(photoD) + "Dislikes.txt", true));
							writer.write(inUser);
							writer.newLine();
							writer.close();
							outStream.writeObject("DISLIKE");
						} else {
							outStream.writeObject("JADEUDISLIKE");
						}
					} else {
						outStream.writeObject("NAO FOTO");
					}
				} else {
					outStream.writeObject("NAO DISLIKE");
				}
			} else {
				outStream.writeObject("NAO USER");
			}
		} catch (IOException e) {
			System.err.println("erro de leitura");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Operacao -c
	 * 
	 * @param inStream
	 *            - reader do cliente
	 * @param outStream
	 *            - writer para o cliente
	 * @param catUser
	 *            - catalogo de users
	 * @param inUser
	 *            - user recebido na operacao
	 * @param photos
	 *            - catalogo de fotos
	 */
	public static void operationC(ObjectInputStream inStream, ObjectOutputStream outStream, CatalogoUser catUser,
			String inUser, CatalogoPhotos photos) {
		try {

			String comentario = (String) inStream.readObject();
			String userC = (String) inStream.readObject();
			String photoC = (String) inStream.readObject();
			User userCcomment = catUser.getUser(userC);
			// verificar se e user
			if (catUser.find(userC) == true) {
				File followC = new File("servidor/" + userC + "/followers.txt");
				userCcomment.populateFollowers(followC);
				// verificamos se e follower
				if (userCcomment.existsFollower(inUser) == true) {
					File listaFotosC = new File("servidor/" + userC + "/listaFotos.txt");
					photos.populate(listaFotosC);
					if (photos.existsPhoto(photoC) == true) {
						BufferedWriter writer = new BufferedWriter(
								new FileWriter("servidor/" + userC + "/" + getNameFile(photoC) + "Comments.txt", true));
						writer.write(comentario);
						writer.newLine();
						writer.close();
						outStream.writeObject("COMMENT");
					} else {
						outStream.writeObject("NAO FOTO");
					}
				} else {
					outStream.writeObject("NAO FOLLOWER");
				}
			} else {
				outStream.writeObject("NAO USER");
			}
		} catch (IOException e) {
			System.err.println("erro de leitura");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Operacao -i
	 * 
	 * @param inStream
	 *            - reader do cliente
	 * @param outStream
	 *            - writer para o cliente
	 * @param catUser
	 *            - catalogo de users
	 * @param inUser
	 *            - user recebido na operacao
	 * @param photos
	 *            - catalogo de fotos
	 */
	public static void operationI(ObjectInputStream inStream, ObjectOutputStream outStream, CatalogoUser catUser,
			String inUser, CatalogoPhotos photos) {
		try {
			String userID = (String) inStream.readObject();
			String foto = (String) inStream.readObject();
			User userCatalog = catUser.getUser(userID);

			if (catUser.find(userID) == true) {
				File followC = new File("servidor/" + userID + "/followers.txt");
				userCatalog.populateFollowers(followC);
				if (userCatalog.existsFollower(inUser) == true) {
					File listaFotosC = new File("servidor/" + userID + "/listaFotos.txt");
					photos.populate(listaFotosC);
					if (photos.existsPhoto(foto) == true) {
						System.out.println("Mostra");
						outStream.writeObject("MOSTRA");
						// populate Likes
						Photo phototempL = photos.getPhoto(foto);
						File ficheiroLikes = new File("servidor/" + userID + "/" + getNameFile(foto) + "Likes.txt");
						phototempL.populateLikes(ficheiroLikes);
						// populate Dislikes
						Photo phototempD = photos.getPhoto(foto);
						File ficheiroDislikes = new File(
								"servidor/" + userID + "/" + getNameFile(foto) + "Dislikes.txt");
						phototempD.populateDislikes(ficheiroDislikes);
						// populate comentarios
						Photo phototempC = photos.getPhoto(foto);
						File ficheiroComments = new File(
								"servidor/" + userID + "/" + getNameFile(foto) + "Comments.txt");
						phototempC.populateComments(ficheiroComments);
						ArrayList<String> comentarios = phototempC.getlistPhotoComments();
						outStream.writeObject(phototempC.tamanholistPhotoComments());
						for (int i = 0; i < comentarios.size(); i++) {
							outStream.writeObject(comentarios.get(i));
						}
						outStream.writeObject(phototempL.tamanholistPhotoLikes());
						outStream.writeObject(phototempD.tamanholistPhotoDislikes());

					} else {
						outStream.writeObject("NAO FOTO");
					}
				} else {
					outStream.writeObject("NAO FOLLOWER");
				}
			} else {
				outStream.writeObject("NAO USER");
			}
		} catch (IOException e) {
			System.err.println("erro de leitura");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Operacao -l
	 * 
	 * @param inStream
	 *            - reader do cliente
	 * @param outStream
	 *            - writer para o cliente
	 * @param catUser
	 *            - catalogo de users
	 * @param inUser
	 *            - user recebido na operacao
	 * @param photos
	 *            - catalogo de fotos
	 */
	public static void operationMiniL(ObjectInputStream inStream, ObjectOutputStream outStream, CatalogoUser catUser,
			String inUser, CatalogoPhotos photos) {
		try {
			String userPhotos = (String) inStream.readObject();
			// catalogo para fotos
			User userList = catUser.getUser(userPhotos);
			if (catUser.find(userPhotos) == true) {
				File followers = new File("servidor/" + userPhotos + "/followers.txt");
				userList.populateFollowers(followers);
				if (userList.existsFollower(inUser) == true) {
					outStream.writeObject("EXISTE");
					ArrayList<Photo> fotos = photos.listaFotos();
					File photoList = new File("servidor/" + userPhotos + "/listaFotos.txt");
					photos.populate(photoList);
					System.out.println("TAMANHO DO SIZE: " + photos.listaFotos().size());
					outStream.writeObject(photos.listaFotos().size());
					for (int i = 0; i < photos.listaFotos().size(); i++) {
						outStream.writeObject(fotos.get(i).getNome() + " - " + fotos.get(i).getData());
					}
				} else {
					outStream.writeObject("NAO EXISTE");
				}
			} else {
				outStream.writeObject("NAO EXISTE USER");
			}

		} catch (IOException e) {
			System.err.println("erro de leitura");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Operacao -g
	 * 
	 * @param inStream
	 *            - reader do cliente
	 * @param outStream
	 *            - writer para o cliente
	 * @param catUser
	 *            - catalogo de users
	 * @param inUser
	 *            - user recebido na operacao
	 */
	public static void operationG(ObjectInputStream inStream, ObjectOutputStream outStream, CatalogoUser catUser,
			String inUser) {
		try {

			String userG = (String) inStream.readObject();
			User u = catUser.getUser(userG);
			if (catUser.find(userG) == true) {
				File followC = new File("servidor/" + userG + "/followers.txt");
				u.populateFollowers(followC);
				u.imprime();
				if (u.existsFollower(inUser) == true) {
					File folderDir = new File("servidor/" + userG);
					String[] folderFiles = folderDir.list();
					System.out.println(folderFiles + "FOLDER FILES!!!");
					ArrayList<String> listaDeFotos = getPhotoFiles(folderFiles);
					outStream.write(listaDeFotos.size());
					// TA A ENVIAR 1 A MAIS!
					System.out.println("tamanho da lista de fotos:" + listaDeFotos.size());
					outStream.writeObject("Fotos enviadas");
					for (int i = 0; i < listaDeFotos.size(); i++) {
						outStream.writeObject(listaDeFotos.get(i));
						File file = new File("servidor/" + userG + "/" + listaDeFotos.get(i));
						long size = file.length();
						FileInputStream inStream1 = new FileInputStream(file);
						// InputStream inStream2 = new BufferedInputStream(inStream1);
						byte buffer[] = new byte[1024];
						int count = 1024;
						outStream.writeObject(file.length());
						while ((count = inStream1.read(buffer, 0, (int) (size < 1024 ? size : 1024))) > 0) {
							outStream.write(buffer, 0, count);
							size -= count;
							outStream.flush();
						}
						inStream1.close();
					}
				} else {
					outStream.writeObject("Nao Follower");
				}
			} else {
				outStream.writeObject("Nao e user");
			}
		} catch (IOException e) {
			System.err.println("erro de leitura");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Adiciona ficheiros imagem para um arrayList
	 * 
	 * @param ficheiros
	 * @return files
	 */
	private static ArrayList<String> getPhotoFiles(String[] ficheiros) {
		ArrayList<String> files = new ArrayList<String>();
		for (int i = 0; i < ficheiros.length; i++) {
			if (ficheiros[i].endsWith(".jpeg") || ficheiros[i].endsWith(".jpg")
					|| ficheiros[i].endsWith("Comments.txt")) {
				files.add(ficheiros[i]);
			}

		}
		return files;
	}

	/**
	 * Verificar se o nome do ficheiro jah existe no Servidor
	 * 
	 * @param listOfFiles
	 *            - lista de ficheiros existentes
	 * @param nome
	 *            - nome do ficheiros
	 * @return true or false
	 */
	private static boolean existsNameFile(File[] listOfFiles, String nome) {
		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].getName().endsWith(".png") || listOfFiles[i].getName().endsWith(".jpg")
					|| listOfFiles[i].getName().endsWith(".jpeg") && listOfFiles[i].getName().equals(nome)) {

				return true;
			}
		}
		return false;
	}

	/**
	 * Get nome do file
	 * 
	 * @param photo
	 * @return str
	 */
	private static String getNameFile(String photo) {
		int ind = photo.indexOf(".");
		String str = photo.substring(0, ind);
		return str;

	}
}
