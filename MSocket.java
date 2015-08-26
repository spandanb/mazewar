import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.*;
import java.util.Random;



public class MSocket{
    /*
     * This class is used as a wrapper around sockets and streams.
     * In addition to allowing network communication,
     * it tracks statistics and can add delays and packet drops (possibly).
     */

    //For communication
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    
    //For adding errors, like delays and packet reorders
    private Random random = null;

    //The queue of packets to send
    private BlockingQueue sendingQueue = null;
    
    
    private ScheduledExecutorService scheduler = null;
    
    /*
     *The following internal classes are needed for async operations
     */
    class Sender implements Runnable{
        private Object obj;
        
        public Sender(Object o){
            this.obj = o;
        }
        
        public void run(){
            try{
                //Access the outer classes' out
                out.writeObject(obj);        
            }catch(IOException e){
                e.printStackTrace();
            }
            
        }
    }
    

    
        
    /*
     *This creates a regular socket
     */
    public MSocket(String host, int port) throws IOException{
        socket = new Socket(host, port);
        //Be careful: outputStream should be initialized before
        //inputStream, else it will block
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        
        sendingQueue = new LinkedBlockingQueue();
        random = new Random(/*seed*/);
        
        scheduler = Executors.newScheduledThreadPool(10);
    }
        
    /*
     *This creates a server socket
     */    
    public MSocket(int port){
        
    }
    
    public void writeObject(Object o) throws IOException{        
        out.writeObject(o);
    }
    
    public Object readObject() throws IOException, ClassNotFoundException{
        return in.readObject();
    }
    
    public void writeObjectDelay(Object o){
        //Delays the write by 1000 ms
        scheduler.schedule(new Sender(o), 3000, MILLISECONDS);
    }
    
    public void writeObjectUnordered(Object o){
        //Changes the order of packets sent
        scheduler.schedule(new Sender(o), 1000, MILLISECONDS);
    }
    
    
    
    
    
    
    public void close() throws IOException{
        in.close();
        out.close();
        socket.close();
    }
    
    private void addDelay(){
        //This block the whole code
        try {
            System.out.println("Sleeping");
            Thread.sleep(1000);
            System.out.println("Waking up");
        } catch(InterruptedException ex) {
            //System.out.println("Waking up");
            //Thread.currentThread().interrupt();
        }
    }
    

}