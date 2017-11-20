/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chattcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.util.Pair;

/**
 *
 * @author andrea
 */
public class Server {

	public static final int DISPONIBILE = 1;
	public static final int OCCUPATO = 2;

	private ServerSocket serverSocket;
	private ArrayList<Pair<ClientThread, Integer>> clientConnessi;
	private int serverPort;
	private boolean isStopped;
	private SQLHelper database;
	private int nConnessi;

	public Server(int serverPort) throws ClassNotFoundException {
		this.serverPort = serverPort;
		try {
			this.serverSocket = new ServerSocket(serverPort);
			this.clientConnessi = new ArrayList<>();
			this.isStopped = false;
			this.nConnessi = 0;
			database = new SQLHelper();
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		}
	}

	public void start() {
		System.out.println("The Server is running...");
		ExecutorService threadPool = Executors.newFixedThreadPool(10);
		System.out.println("Server is attending to port: " + serverPort);
		while (!isStopped) {
			try {
				int app = nConnessi;
				Socket clientSocket = serverSocket.accept();
				System.out.println("Client-" + app + " just connected");
				ClientThread ct = new ClientThread(clientSocket, app);
				this.clientConnessi.add(new Pair<>(ct, OCCUPATO));
				threadPool.execute(ct);
				nConnessi = clientConnessi.size();
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
				isStopped = true;
			}
		}
	}

	public synchronized boolean blockClient(String clientName) {
		int pos = getPos(clientName);
		ClientThread ct = clientConnessi.get(pos).getKey();
		int isBlocked = clientConnessi.get(pos).getValue();
		if (ct.getUsername().equals(clientName) && isBlocked == DISPONIBILE) {
			clientConnessi.remove(pos);
			clientConnessi.add(new Pair<>(ct, OCCUPATO));
			return true;

		}
		return false;
	}

	public int getPos(String name) {
		for (int i = 0; i < clientConnessi.size(); i++) {
			ClientThread ct = clientConnessi.get(i).getKey();
			if (ct.getUsername().equals(name)) {
				return i;
			}

		}
		return -1;
	}

	private String getClientConnessi(int id) {
		String conn = "";
		for (int i = 0; i < clientConnessi.size(); i++) {
			ClientThread ct = clientConnessi.get(i).getKey();
			int isBlocked = clientConnessi.get(i).getValue();
			if (isBlocked == DISPONIBILE && id != i) {
				conn += ct.getUsername() + "|";
			}
		}

		return conn;
	}

	public static void main(String[] args) throws ClassNotFoundException {
		int serverPort = 8080;
		Server s = new Server(serverPort);
		s.start();
	}

	class ClientThread implements Runnable {

		private final Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		private String username;
		private boolean sconnesso;
		private final int id;

		public ClientThread(Socket socket, int id) {
			this.socket = socket;
			this.username = "";
			this.id = id;
			this.sconnesso = false;
		}

		public String getUsername() {
			return username;
		}

		@Override
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				autentication();
				if (!sconnesso) {
					connect();
				} else {
					socket.close();
				}
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
		}

		private void autentication() {
			try {
				this.username = in.readLine();
				String password = in.readLine();
				if (database.login(username, password)) {
					out.println("ok");
				} else {
					out.println("nok");
					if (socket != null && in.readLine().equals("si")) {
						out.println("nok");
						username = in.readLine();
						password = in.readLine();
						if (database.exist(username)) {
							while (database.exist(username)) {
								username = in.readLine();
								password = in.readLine();
								out.println("nok");
							}
							database.addUser(username, password);
							out.println("ok");
						} else {
							database.addUser(username, password);
							out.println("ok");
						}
					} else {
						System.out.println("Client-" + id + " removed");
						this.sconnesso = true;
					}

				}
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
		}

		private void connect() {
			try {
				String connessi = getClientConnessi(id);
				//invio client disponibili
				out.println(connessi);

				out.println("Digitare nome client con cui si decide di chattare o premere 1 per attendere. Se entro 20 secondi non avrai preso una decisione sarai disconnesso.");
				out.flush();

				Thread timer = new Thread(new Contatore());
				timer.start();
				boolean isInterrupted = false;
				while (!in.ready()) {
					if (isInterrupted != timer.isInterrupted()) {
						isInterrupted = true;
						break;
					}
				}
				if (isInterrupted) {
					System.out.println("Sono stato interrotto");
					socket.close();
					clientConnessi.remove(id);
				} else {
					System.out.println("non sono stato interrotto");
					String user = in.readLine();
					if (user.equals("1")) {
						clientConnessi.remove(id);
						clientConnessi.add(id, new Pair<>(this, DISPONIBILE));
					} else if (database.exist(user)) {

						Socket s1 = clientConnessi.get(getPos(user)).getKey().socket;
						BufferedReader input = new BufferedReader(new InputStreamReader(s1.getInputStream()));
						PrintWriter output = new PrintWriter(s1.getOutputStream(), true);

						output.println(username + " vuole chattare con te. Accetti?");
						if (input.readLine().toLowerCase().equals("si")) {
							blockClient(user);
							new Thread(new Chat(username, user)).start();
							new Thread(new Chat(user, username)).start();
						}
					}else{
						clientConnessi.remove(id);
						socket.close();
					}
				}

			} catch (SocketException ex) {
				System.out.println("Client-" + id + " has run out of time");
				clientConnessi.remove(id);
				Thread.currentThread().interrupt();
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
		}

		public void writeMessage(String message) {
			out.println(message);
		}

		class Chat implements Runnable {

			private final String user1;
			private final String user2;

			public Chat(String user1, String user2) {
				this.user1 = user1;
				this.user2 = user2;
			}

			@Override
			public void run() {
				Socket s1 = clientConnessi.get(getPos(user1)).getKey().socket;
				Socket s2 = clientConnessi.get(getPos(user2)).getKey().socket;
				try {
					BufferedReader input = new BufferedReader(new InputStreamReader(s1.getInputStream()));
					PrintWriter output = new PrintWriter(s2.getOutputStream(), true);

					while (true) {
						String messaggio = user1 + ">" + input.readLine();
						if (!messaggio.equals("fine")) {
							output.println(messaggio);
						} else {
							s1.close();
							s2.close();
						}
					}
				} catch (IOException ex) {
					ex.getMessage();
				}

			}

		}

	}

	class Contatore extends Thread {

		@Override
		public void run() {
			try {
				TimeUnit.SECONDS.sleep(20);
				Thread.currentThread().interrupt();
			} catch (InterruptedException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

}
