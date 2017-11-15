/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatroomudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 *
 * @author andrea
 */
public class Server implements Runnable {

    private MulticastSocket socket;
    private InetAddress multicast;
    private int serverPort;

    public Server(int serverPort) {
        this.serverPort = serverPort;
        try {
            socket = new MulticastSocket(serverPort);
            multicast = InetAddress.getByName("224.100.100.1");
        } catch (UnknownHostException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void run() {
        System.out.println("The server is running...");
        while (true) {
            try {
                byte[] buff = new byte[1024];
                DatagramPacket recivePacket = new DatagramPacket(buff, buff.length);
                socket.receive(recivePacket);
                System.out.println("packet recived");
                byte[] data = recivePacket.getData();
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, multicast, serverPort);
                socket.send(sendPacket);
                System.out.println("packet sent");
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
    
    public static void main(String[]args){
        Server s=new Server(4446);
        s.run();
    }
}
