package server;

import message.Message;
import message.Message.MessageType;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.InputStreamReader;

import java.net.Socket;

public class ServerThread extends Thread {
    private Socket socket_client;
    private BufferedReader in_buffer;
    public PrintStream out_buffer;
    private int num_client;
    private int num_clients_in_neighborhood;

    private ServerNeighborhood server_neighborhood;

    public ServerThread(Socket socket, int num_client, ServerNeighborhood server_neighborhood){
        this.num_client = num_client;
        this.socket_client = socket;
        this.num_clients_in_neighborhood = server_neighborhood.getClientsInNeighborhood();
        try{
            this.in_buffer = new BufferedReader(new InputStreamReader(this.socket_client.getInputStream()));
            this.out_buffer = new PrintStream(this.socket_client.getOutputStream());
        }catch(Exception e){
            System.out.println("Error ServerThread init: " + e.getMessage());
            System.exit(1);
        }
        this.server_neighborhood = server_neighborhood;
    }

    public int getNumClient() {
        return this.num_client;
    }

    public void run(){
        Message message = new Message(num_client, MessageType.START, "" + this.num_clients_in_neighborhood);
        this.out_buffer.println(message.toString());

        while(socket_client.isConnected() && !this.socket_client.isClosed()){
            try{
                String message_s = this.in_buffer.readLine();

                this.server_neighborhood.messageRoutine(Message.parseMessage(message_s));
            }catch(Exception e){
                //System.out.println("Error ServerThread run: " + e.getMessage());
                break;
            }
        }

        //System.out.println("Client disconnected");
    }

    public void close(int num_neigh) {
        this.out_buffer.println(new Message(this.num_client, MessageType.END, ""+num_neigh).toString());
        try{
            this.socket_client.close();
        }catch(Exception e){
            //System.out.println("Error ServerThread END: " + e.getMessage());
        }

    }
}
