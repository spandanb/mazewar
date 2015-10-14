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
import java.lang.instrument.Instrumentation;


public class MSocket{
    /*
     * This class is used as a wrapper around sockets and streams.
     * In addition to allowing network communication,
     * it tracks statistics and can add delays and packet-drops (possibly).
     */

    //Constants
    //What are the odds there is any delay
    public final double DELAY_ODDS = 0.4;
    
    
    //The degree of unorder
    //0 means ordered
    public final double UNORDER_FACTOR = 0.5; 
     
    //For communication
    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    
    //For adding errors, like delays and packet reorders
    private Random random = null;

    //The queue of packets to send
    private BlockingQueue egressQueue = null;
    //The queue of packets received
    private BlockingQueue ingressQueue = null;
    
    private ScheduledExecutorService scheduler = null;
    
    //Counters for number packets sent or received
    private int rcvdCount;
    private int sentCount;
    private static Instrumentation instrumentation;
    //Amount of bytes sent and received
    private long rcvdSize;
    private long sentSize;
    
    /*
     *The following internal classes are needed for async operations
     */
    //A simple sender
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
    
    //A sender that reorders packets
    class SenderUnordered implements Runnable{
        private Object obj;
        
        public void run(){
            try{                
                int delay = getDelay();
                //System.out.println("Delay is " + delay);
                Thread.sleep(delay);

                //Hopefully another event was added to the queue    
                if(egressQueue.size() > 1){
                    //Swap the first two events
                    Object first = egressQueue.take();
                    Object second = egressQueue.take();
                    out.writeObject(second);
                    out.writeObject(first);
                    
                    //Execute the rest in order
                    //NOTE: This can be made more unordered
                    while(egressQueue.size() != 0){
                        out.writeObject(egressQueue.take());
                    }
                    
                }else if(egressQueue.size() == 1){
                    //Else just send it to the server
                    //- don't want to wait for too long
                    out.writeObject(egressQueue.take());
                }else{
                    //Queue is empty, do nothing
                }    
            }catch(IOException e){
                e.printStackTrace();
            }catch(InterruptedException e){
                System.out.println("ERROR: Thread was interrupted!");
            }
            
        }
    }
    
    private int getDelay(){    
        double r = random.nextDouble();
        if(r < DELAY_ODDS){
            //no delay
            return 0;
        }else if(r < 0.5){
            //Small delay
            return (int) (r * 100);
        }else{
            //large delay
            return (int) (r * 1000);
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
        
        egressQueue = new LinkedBlockingQueue();
        random = new Random(/*seed*/);
        
        scheduler = Executors.newScheduledThreadPool(10);
        
        rcvdCount = 0;
        sentCount = 0;
        rcvdSize = 0;
        sentSize = 0;
        
        //instrumentation.getObjectSize()
        
    }
    
    //Need this for instrumentation
    //TODO: Fix this; doesn't work
    /*
    public static void premain(String args, Instrumentation inst) {
        System.out.println("premain...");  
        instrumentation = inst;
    }
    public static void agentmain(String agentArgs, Instrumentation inst){  
      System.out.println("agentmain...");  
      instrumentation = inst;  
    }  
    */
  

    
        
    /*
     *This creates a server socket
     */    
    public MSocket(int port){
        
    }
    
    public void writeObject(Object o) throws IOException{        
        out.writeObject(o);
    }
    
    public Object readObject() throws IOException, ClassNotFoundException{
        System.out.println("Number of packets received: " + ++rcvdCount);
        //System.out.println("Number of bytes received: " + rcvdSize);
        return in.readObject();
    }
    
    public void writeObjectDelay(Object o){
        //Delays the write by 1000 ms
        scheduler.schedule(new Sender(o), 3000, MILLISECONDS);
    }
    
    public void writeObjectUnordered(Object o) throws InterruptedException{
        System.out.println("Number of packets sent: " + ++sentCount);
        
        //TODO: unable to import instrumentation package; need to fix this
        //sentSize += MSocket.instrumentation.getObjectSize(o);
        //System.out.println("Number of bytes sent: " + sentSize);
        
        //Changes the order of packets sent
        egressQueue.put(o);
        
        //NOTE: this creates a delay of 2000 ms. 
        //To create a random delay, randomly vary the amount
        //scheduler.schedule(new SenderUnordered(), 2000, MILLISECONDS);
        scheduler.schedule(new SenderUnordered(), 0, MILLISECONDS);
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