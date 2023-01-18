package server;

import java.util.Scanner;

public class TCPServer {
    
    int port = 3000;
    int contador = 0;
    
    public static void main(String[] args) {
        Scanner entrada = new Scanner(System.in);

        System.out.println("Introduzca el numero de clientes: ");
        int numClientes = entrada.nextInt();

        System.out.println("Introduzca el numero de vecindarios: ");
        int numVecinos = entrada.nextInt();

        while(numClientes % numVecinos != 0){
            System.out.println("Introduzca el numero de vecindarios (El numero de clientes debe ser divisible por el numero de vecindarios):");
            numVecinos = entrada.nextInt();
        }

        Server.startServer(numClientes, numVecinos);

        entrada.close();
    }
}