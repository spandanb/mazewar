import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
        
    private static final int MAX_CLIENTS = 2;
    private ServerSocket serverSocket = null;
    private int clientCount; //The number of clients before game starts
    private ObjectOutputStream[] outputStreamList = null; //A list of sockets
    private BlockingQueue eventQueue = null; //A list of events
    
    /*
    * Constructor
    */
    public Server(int port) throws IOException{
        clientCount = 0; 
        serverSocket = new ServerSocket(port);
        System.out.println("Listening on port: " + port);
        outputStreamList = new ObjectOutputStream[MAX_CLIENTS];
        eventQueue = new LinkedBlockingQueue<MPacket>();
    }
    
    public void startThreads() throws IOException{
        //Listen for new clients
        while(clientCount < MAX_CLIENTS){
            //Start a new thread for new client connection
            Socket socket = serverSocket.accept();
            
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            new Thread(new ServerListenerThread(in, eventQueue)).start();
            
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            outputStreamList[clientCount] = out;                            
            
            clientCount++;
        }
        
        //Start a new sender thread 
        new Thread(new ServerSenderThread(outputStreamList, eventQueue)).start();    
    }

        
    /*
    * Entry point for server
    */
    public static void main(String args[]) throws IOException {
        System.out.println("Starting the server");
        int port = Integer.parseInt(args[0]);
        Server server = new Server(port);
                
        server.startThreads();    

    }
}
