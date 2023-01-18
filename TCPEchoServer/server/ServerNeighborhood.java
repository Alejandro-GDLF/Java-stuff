package server;

import message.Message;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

public class ServerNeighborhood {
    private ServerThread[] threads;
    private long[] response_times;

    private int num_vecinos;

    private int id_neighborhood;

    private int contador_vecinos;
    private int ended_clients;

    public ServerNeighborhood(int num_vecinos, int neighborhood) {
        this.num_vecinos = num_vecinos;
        this.id_neighborhood = neighborhood;
        this.threads = new ServerThread[num_vecinos];
        this.contador_vecinos = 0;
        this.ended_clients = 0;
        
        this.response_times = new long[this.num_vecinos];
    }

    public boolean addThread(Socket socket) {
        if ( this.contador_vecinos >= this.num_vecinos ) {
            return false;
        }

        System.out.println("Client connected: "+ socket.getRemoteSocketAddress());
        this.threads[this.contador_vecinos] = new ServerThread(socket, this.contador_vecinos, this);
        this.contador_vecinos++;
        System.out.println("Client added to neighborhood: "+ (this.id_neighborhood));
        return true;
    }

    public void startSimulation() {
        for (ServerThread thread : this.threads) {
            thread.start();
        }
    }

    public int getClientsInNeighborhood() {
        return this.num_vecinos;
    }

    public void messageRoutine(Message message) {
        //System.out.println("Message recieved: " + message.toString() + " at neighborhood: " + this.id_neighborhood);

        //

            switch (message.message_type) {
                case ACKNOWLEDGE:
                    synchronized (this.threads[message.num_client]) {
                        this.threads[message.num_client].out_buffer.println(message.toString());
                    }
                    break;
                case BROADCAST:
                    for (ServerThread thread : this.threads) {
                        if (thread.getNumClient() != message.num_client) 
                            synchronized(thread) {
                                thread.out_buffer.println(message.toString());
                            }
                    }
                    break;
                case START:
                    synchronized (this.threads[message.num_client]) {
                        this.threads[message.num_client].out_buffer.println(message.toString());
                    }
                    break;
                case END:
                    
                    this.incrementEndedClients();

                    this.response_times[message.num_client] = Long.parseLong(message.message_content[0]);

                    if(this.ended_clients >= this.num_vecinos){
                        //System.out.println("Simulation ended");
                        for (ServerThread thread : this.threads) {
                            thread.close(this.id_neighborhood);
                        }

                        this.showNeighbourhoodTimes();
                        this.toFile();
                    }
                    break;
                default:
                    break;
            }
        //}
    }

    public synchronized void incrementEndedClients() {
        this.ended_clients++;
    }

    public int getId(){
        return this.id_neighborhood;
    }

    public void showNeighbourhoodTimes() {
        long cont = 0;
        String message = "";
        message += "---------------------------\n";
        message += "Neighborhood: " + this.id_neighborhood + "\n";
        for (int i = 0; i < this.num_vecinos; i++) {
            message += "Client: " + i + " Time: " + TimeUnit.NANOSECONDS.toMillis((long)(this.response_times[i])) + "ms\n";
            cont += this.response_times[i];
        }
        Long mean = TimeUnit.NANOSECONDS.toMillis(cont / this.num_vecinos);
        message += "\n\nMean response time: " + mean + "ms\n";
        message+= "---------------------------";

        System.out.println(message);

        Server.getServer().showTimeResults(mean, id_neighborhood);
    }

    public void toFile(){
        try{
            
            BufferedWriter f_writer = new BufferedWriter(new FileWriter("./stats/neighborhood_" + this.id_neighborhood + ".txt"));

            for ( int i = 0; i < this.num_vecinos; i++)
                f_writer.write("" + i + "," + this.response_times[i] + "\n");

            f_writer.close();
        }catch(IOException e){
            System.out.print(e.getMessage());
        }
    }
}
