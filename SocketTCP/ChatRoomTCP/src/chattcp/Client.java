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
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author andrea
 */
public class Client {

    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader inSystem;
    private Socket clientSocket;

    private String serverAddress;
    private int serverPort;
    private String username;
    private String password;

    public Client(String address, int port) {
        System.out.println("Connecting to server...");
        this.serverAddress = address;
        this.serverPort = port;
        try {
            this.clientSocket = new Socket(serverAddress, serverPort);
            System.out.println("Connection accepted " + clientSocket.getInetAddress() + "/" + clientSocket.getPort());

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            inSystem = new BufferedReader(new InputStreamReader(System.in));

            autentication();
            new ListenerFromServer().start();

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private boolean autentication() {
        try {
            System.out.println("Username");
            this.username = inSystem.readLine();
            System.out.println("Password");
            this.password = inSystem.readLine();

            out.println(username);
            out.println(password);

            if (in.readLine().equals("ok")) {
                System.out.println("Welcome " + username);
                return true;
            } else {
                System.out.println("Login Failed");
                System.out.println("Do you want to be registered?");
                String risposta = inSystem.readLine().toLowerCase();
                if (risposta.equals("si")) {
                    out.println("si");
                    while (true) {
                        if (in.readLine().equals("ok")) {
                            break;
                        }
                        System.out.println("username");
                        out.println(inSystem.readLine());
                        System.out.println("password");
                        out.println(inSystem.readLine());

                    }
                    System.out.println("You have been registered");
                } else {
                    out.println("no");
                    System.out.println("GoodBye");
                    clientSocket.close();
                    System.exit(0);
                }
            }

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return false;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public static void main(String[] args) {
        Scanner t = new Scanner(System.in);
        Client client = new Client("localhost", 1500);
        while (true) {
            System.out.print(">");
            String message = t.nextLine();
            if (message.equals("fine")) {
                client.sendMessage("fine");
                break;
            } else {
                client.sendMessage(message);
            }
        }
        try {
            System.out.println("GoodBye");
            client.clientSocket.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    class ListenerFromServer extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    String message = in.readLine();
                    System.out.println(message);
                    System.out.print(">");
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                    break;
                }
            }

        }
    }
}
