package server;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;

public class Server {
    int port = 5000;
    ServerSocket serverSocket;

    int num_neighborhoods;
    int num_clients;
    ArrayList<ServerNeighborhood> server_neighborhoods;

    Long[] neighborhood_means;
    int neighborhood_ended;

    private static Server server;

    private Server(int num_clients, int num_neighborhoods) {
        try{
            serverSocket = new ServerSocket(this.port);
            this.num_clients = num_clients;
            this.num_neighborhoods = num_neighborhoods;
            this.neighborhood_means = new Long[this.num_neighborhoods];
            this.neighborhood_ended = 0;

            server_neighborhoods = new ArrayList<ServerNeighborhood>(this.num_neighborhoods);
            
            server_neighborhoods.add(new ServerNeighborhood(this.num_clients / this.num_neighborhoods, this.server_neighborhoods.size()));

            int client_counter = 0;

            System.out.println("Server started in "+ this.serverSocket.getLocalSocketAddress());
            System.out.println("Waiting for clients...");
            while( client_counter < this.num_clients ){
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(0);
                socket.setKeepAlive(true);

                if( !server_neighborhoods.get(server_neighborhoods.size() - 1).addThread(socket) ){
                    server_neighborhoods.add(new ServerNeighborhood(this.num_clients / this.num_neighborhoods, this.server_neighborhoods.size()));
                    server_neighborhoods.get(server_neighborhoods.size() - 1).addThread(socket);
                }
                client_counter++;
            }

            startSimulation();
        } catch (Exception e) {
            System.out.println("Error Server: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void startServer(int num_clients, int num_neighborhoods){
        server = new Server(num_clients, num_neighborhoods);
    }

    public static Server getServer(){
        return server;
    }

    public void showTimeResults(Long result, int neighborhood_id){
        this.neighborhood_means[neighborhood_id] = result;
        this.neighborhood_ended++;

        if( this.neighborhood_ended == this.num_neighborhoods ){
            Long mean = 0L;
            for (Long neighborhood_mean : this.neighborhood_means) {
                mean += neighborhood_mean;
            }
            mean /= this.num_neighborhoods;
            System.out.println("\n");
            System.out.println("------------------------");
            System.out.println("Overall mean time: " + mean + " ms");
            System.out.println("------------------------");
        }
    }

    public void startSimulation() {
        System.out.println("--------------------\nStart simulation\n--------------------");
        for (ServerNeighborhood server_neighborhood : this.server_neighborhoods) {
            server_neighborhood.startSimulation();
        }
    }
}
