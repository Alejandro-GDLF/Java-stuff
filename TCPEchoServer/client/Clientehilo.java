package client;

import message.Message;
import message.Message.MessageType;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Clientehilo extends Thread{
    int num_iterations;         // Number of iterations
    int num_client;             // Number of this client
    int num_clients_total;      // Number of clients in the neighborhood
    boolean sended_end;
    long response_time;

    private int contador_perdidos = 0;

    int port = 5000;

    Socket socket;
    private BufferedReader in_buffer;
    public PrintStream out_buffer;

    public Clientehilo(String ip, int num_iterations){
        this.num_iterations = num_iterations;
        this.sended_end = false;
        this.response_time = 0;
        InetAddress ip_address = null;

        try{
            ip_address = Inet4Address.getByName(ip);
        }catch(UnknownHostException uhe){
            uhe.printStackTrace();
            System.out.println("\n\nError: Unknown host");
            System.exit(1);
        }

        try{
            socket = new Socket(ip_address, port);
            socket.setSoTimeout(20000);
            socket.setKeepAlive(true);
            System.out.println("Client connected to server");
        } catch (Exception e) {
            System.out.println("Error Clientehilo: " + e.getMessage());
            System.exit(1);
        }

        try{
            this.in_buffer = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out_buffer = new PrintStream(this.socket.getOutputStream());
        }catch(Exception e){
            System.out.println("Error Clientehilo: " + e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void run(){
        
        this.waitForStartSimulation();

        this.simulationRoutine();

        this.waitForEndSimulation();
        
        System.out.println("Client " + this.num_client + " finished ------------------------------ Lost packages: " + this.contador_perdidos);
    }

    public void waitForStartSimulation(){
        String message_s = null;

        try{
            socket.setSoTimeout(0);
        }catch(Exception e){
            System.out.println("Error Clientehilo: " + e.getMessage());
            System.exit(1);
        }

        while(this.socket.isConnected()){
            try{
                message_s = this.in_buffer.readLine();
                Message message = Message.parseMessage(message_s);

                if(message.message_type == Message.MessageType.START){
                    //System.out.println("Client " + message.num_client + " started" + "\nMessage content: "+message.message_content);
                    this.num_client = message.num_client;
                    try{
                        this.num_clients_total = Integer.parseInt(message.message_content[0]);
                    }catch(Exception e){
                        System.out.println("Error Clientehilo parsing Integer: " + e.getMessage() + "\n\t--- " + e.getClass());
                        System.exit(1);
                    }
                    break;
                }
            }catch(Exception e){
                System.out.println("Error Clientehilo wait routine: " + e.getMessage() + "\n\t---" + e.getClass());
                System.exit(1);
            }
        }
    }

    public void simulationRoutine(){
        int iteration_num = 0;
        int client_ack = 0;
        String message_s;

        try{
            socket.setSoTimeout(20000);
        }catch(Exception e){
            System.out.println("Error Clientehilo: " + e.getMessage());
            System.exit(1);
        }

        while(iteration_num < this.num_iterations) {

            Message message;

            int x = generateCoord();
            int y = generateCoord();
            int z = generateCoord();

            message = new Message(this.num_client, MessageType.BROADCAST, "["+x + "," + y + "," + z + "]" + Message.content_separator + iteration_num);

            this.out_buffer.println(message.toString());

            long start_time = System.nanoTime();

            while(client_ack < this.num_clients_total-1) {
                try{
                    message_s = this.in_buffer.readLine();
                    message = Message.parseMessage(message_s);

                    switch(message.message_type) {
                        case ACKNOWLEDGE:
                            if( message.message_content[0].equals(""+iteration_num) )
                                client_ack++;

                            else{
                                System.out.println("Error Clientehilo: ACKNOWLEDGE message with wrong iteration number");
                                this.contador_perdidos++;
                            }
                            
                            break;

                        case BROADCAST:

                            message = new Message(message.num_client, MessageType.ACKNOWLEDGE, message.message_content[1]);
                            this.out_buffer.println(message.toString());
                            break;

                        default:
                            System.out.println("Non-supported message type.\n\t-- Message:" + message.toString());
                    }

                }catch(java.net.SocketTimeoutException ste){
                    System.out.println("Cliente " + this.num_client + " ha esperao demasiao. Salta a la siguiente iteracion.");
                    break;
                }   
                catch(Exception e){
                    System.out.println("Error Clientehilo routine + " + this.num_client + ": " + e.getMessage() + "\n\t--- " + e.getClass());
                    System.exit(1);
                }
            }

            this.response_time += System.nanoTime() - start_time;
            iteration_num++;
            client_ack = 0;
        }
    }

    public void waitForEndSimulation(){
        String message_s = null;
        Message message;

        try{
            socket.setSoTimeout(0);
        }catch(Exception e){
            System.out.println("Error Clientehilo: " + e.getMessage());
            System.exit(1);
        }

        this.out_buffer.println(new Message(this.num_client, MessageType.END, ""+(this.response_time/this.num_iterations)).toString());

        while(this.socket.isConnected()) {
            try{
                message_s = this.in_buffer.readLine();
                message = Message.parseMessage(message_s);

                if ( message.message_type == MessageType.END ) {
                    break;
                }
                else if( message.message_type == MessageType.BROADCAST ){
                    message = new Message(message.num_client, MessageType.ACKNOWLEDGE, message.message_content[1]);
                    this.out_buffer.println(message.toString());
                }

            }catch(java.net.SocketTimeoutException ste){
                ;
            }
            catch(java.net.SocketException e){
                System.out.println("Connection closed: client " + this.num_client + "\n Message: " + e.getMessage() + "  " + e.getClass());
                break;
            }
            catch(Exception e){
                System.out.println("Error Clientehilo wait routine: " + e.getMessage() + "\n\t---" + e.getClass());
                System.exit(1);
            }
            
        }
    }

    public int generateCoord(){
        return (int)(Math.random() * 100);
    }
}