package client;

public class TCPClient {
    public static void main(String[] args) { // args[0] = ip, args[1] = num_clients, args[2] = num_iterations
        int num_clientes;
        int num_iterations;
        String ip;
        
        if(args.length < 3) {
            System.err.println("Error TCPClient: Not enough arguments");
            System.exit(1);
            return;
        }
        
        ip = args[0];
        num_clientes = Integer.parseInt(args[1]);
        num_iterations = Integer.parseInt(args[2]);

        for(int i = 0; i < num_clientes; i++){
            new Clientehilo(ip, num_iterations).start();
        }
    }
}