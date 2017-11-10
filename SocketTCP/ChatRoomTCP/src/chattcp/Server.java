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
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author andrea
 */
public class Server {

    private ServerSocket serverSocket;
    private ArrayList<ClientThread> clientConnessi;
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
            nConnessi++;
            try {
                int app = nConnessi - 1;
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client-" + app + " just connected");
                ClientThread ct = new ClientThread(clientSocket, app);
                this.clientConnessi.add(ct);
                threadPool.execute(ct);

            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                isStopped = true;
            }
        }
    }

    public synchronized void broadcast(String message, int id) {
        for (int i = 0; i < clientConnessi.size(); i++) {
            ClientThread ct = this.clientConnessi.get(i);
            if (ct != null) {
                if (i != id) {
                    ct.writeMessage(message);
                }
            } 
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        int serverPort = 1500;
        Server s = new Server(serverPort);
        s.start();
    }

    class ClientThread implements Runnable {

        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;
        private boolean sconnesso;
        private String message;
        private int id;

        public ClientThread(Socket socket, int id) {
            this.socket = socket;
            this.username = "";
            this.message = "";
            this.id = id;
            this.sconnesso = false;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

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

                        broadcast(username + " just connected", id);
                    } else {
                        System.out.println("Client-" + id + " removed");
                        this.sconnesso = true;
                    }

                }
                if (!sconnesso) {
                    while (true) {
                        message = in.readLine();
                        if (message.equals("fine")) {
                            break;
                        } else {
                            broadcast(username + ": " + message, id);
                        }
                    }
                }
                socket.close();

            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }

        public void writeMessage(String message) {
            out.println(message);
        }

    }
}
