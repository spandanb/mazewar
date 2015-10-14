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

	//TODO: This class should be a singleton, 
	//that coordinates all the errors
	
    /*************Constants*************/
    //The probability that there is any delay
    public final double DELAY_ODDS = 0.4;
    
    //The degree of "unorderedness" 
    //0 means ordered
    //NOTE: This is unused, but should be 
    //used in more complex models 
    public final double UNORDER_FACTOR = 0.5; 
     
    /*************Member objects for communication*************/
    private Socket socket = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private ServerSocket serverSocket = null;
    
    /*************Member objects for other tasks*************/
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
    
    //Amount of bytes sent and received
    //FIX: Instrumentation package cannot be imported
    private static Instrumentation instrumentation;
    private long rcvdSize;
    private long sentSize;
    
    /*************Sender Thread Classes*************/
    /*
     *The following inner classes are needed for asynchronous 
     *operations, such as send a packet after some delay
     */
    
    //A simple sender that sends packets as-is without inducing 
    //any errors
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
    
    //A sender that reorders packets and adds a delay
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
    
    /*************Constructors*************/
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
        
        
    }
    
        
    /*
     *TODO: Implement this 
     *This creates a server socket
     *Should be very similar to the above constructor 
     */    
    public MSocket(int port){
        
    }
    
    /*************Helpers*************/
    
    //Generate a random delay 
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
    
    /*************Public Methods*************/
    
    //Read's the packet as-is
    public Object readObject() throws IOException, ClassNotFoundException{
        System.out.println("Number of packets received: " + ++rcvdCount);
        return in.readObject();
    }
    
    //TODO: The following three methods should be merged into one
    //The behavior of the method should be determined by
    //the constants DELAY_ODDS and UNORDER_FACTOR
    
    //Writes the object as-is
    public void writeObject(Object o) throws IOException{        
        out.writeObject(o);
    }
    
    //Writes the object, i.e. the packet after some delay
    public void writeObjectDelay(Object o){
        //Delays the write by 3000 ms
        scheduler.schedule(new Sender(o), 3000, MILLISECONDS);
    }
    
    //Writes the object, while adding delay and unordering the packets
    public void writeObjectUnordered(Object o) throws InterruptedException{
        System.out.println("Number of packets sent: " + ++sentCount);
                
        //Changes the order of packets sent
        egressQueue.put(o);
        
        //NOTE: this creates a delay of n milliseconds.
        //To create a random delay, randomly vary this amount
        int delay = 0; //2000;
        
        scheduler.schedule(new SenderUnordered(), delay, MILLISECONDS);
    }
    
    //Closes network objects, i.e. sockets, InputObjectStreams, 
    // OutputObjectStream
    public void close() throws IOException{
        in.close();
        out.close();
        socket.close();
    }

}