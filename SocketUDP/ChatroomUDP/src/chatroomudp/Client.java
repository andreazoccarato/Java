/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatroomudp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 *
 * @author andrea
 */
public class Client {

    private MulticastSocket socket;
    private InetAddress address;
    private final int serverPort;
    private BufferedReader inSystem;
    private String username;

    public Client(int serverPort) {
        this.serverPort = serverPort;
        try {
            this.socket = new MulticastSocket(4446);
            address = InetAddress.getByName("224.100.100.1");
            socket.joinGroup(address);
            inSystem = new BufferedReader(new InputStreamReader(System.in));
            this.username = "";
        } catch (UnknownHostException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

    }

    public void start() {
        try {
            System.out.println("Insert username");
            username = inSystem.readLine();
            System.out.println("Now you can write message");
            new Thread(new InThread(inSystem, username)).start();
            new Thread(new OutThread()).start();
//            socket.leaveGroup(address);
//            socket.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
//--------------------------------------------------------------------------------------------------------------------------------------------------

    class InThread implements Runnable {

        private byte[] sendData;
        private DatagramPacket sendPacket;
        private final BufferedReader inSystem;
        private final String username;

        public InThread(BufferedReader inSystem, String username) {
            this.inSystem = inSystem;
            this.username = username;
        }

        @Override
        public void run() {

            while (true) {
                try {
                    String message = "[ " + username + " ]" + inSystem.readLine();

                    sendData = message.getBytes();
                    sendPacket = new DatagramPacket(sendData, sendData.length, address, serverPort);
                    socket.send(sendPacket);

                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }

        }
    }
//--------------------------------------------------------------------------------------------------------------------------------------------------

    class OutThread implements Runnable {

        private byte[] data;
        private DatagramPacket recivedPacket;

        @Override
        public void run() {

            while (true) {
                try {
                    byte buff[] = new byte[1024];
                    recivedPacket = new DatagramPacket(buff, buff.length);
                    socket.receive(recivedPacket);

                    data = recivedPacket.getData();
                    String message = new String(data);
                    System.out.println(message);

                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }

        }
    }
//--------------------------------------------------------------------------------------------------------------------------------------------------

    public static void main(String[] args) {
        Client c = new Client(4446);
        c.start();
    }
}
