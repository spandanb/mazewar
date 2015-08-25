import java.net.*;
import java.io.*;

class SimpleClient{

    public static void main(String args[]) throws IOException{
             if(args.length != 2){
                System.out.println("Error: Invalid number of arguments.");
                System.out.println("Usage: java foo <server location> <server port>");
                System.exit(1);
             }
             String host = args[0];
             int port = Integer.parseInt(args[1]);
             Socket socket = new Socket(host, port);
             socket.close();
    }

}
