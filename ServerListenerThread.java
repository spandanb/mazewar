import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.BlockingQueue;

public class ServerListenerThread implements Runnable {

    private ObjectInputStream in =  null;
    private BlockingQueue eventQueue = null;

    public ServerListenerThread( ObjectInputStream in, BlockingQueue eventQueue){
        this.in = in;
        this.eventQueue = eventQueue;
    }

    public void run() {
        MPacket received = null;
        System.out.println("Starting a listener");
        while(true){
            try{
                received = (MPacket) in.readObject();
                System.out.println("Received: " + received);
                eventQueue.put(received);    
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();    
            }catch(IOException e){
                Thread.currentThread().interrupt();
            }catch(ClassNotFoundException e){
                Thread.currentThread().interrupt();    
            }
            
        }
    }
}
