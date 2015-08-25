import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;

//public class ClientSenderThread implements Runnable {
public class ClientSenderThread implements Runnable {

    private ObjectOutputStream serverStream = null;
    private BlockingQueue<MPacket> eventQueue = null;
    
    public ClientSenderThread(ObjectOutputStream serverStream,
                              BlockingQueue eventQueue){
        this.serverStream = serverStream;
        this.eventQueue = eventQueue;
    }
    
    public void run() {
        MPacket toServer = null;
        System.out.println("Starting ClientSenderThread");
        while(true){
            try{                
                //Take packet from queue
                toServer = (MPacket)eventQueue.take();
                System.out.println("Sending " + toServer);
                serverStream.writeObject(toServer);    
            }catch(IOException e){
                Thread.currentThread().interrupt();
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();    
            }
            
        }
    }
}
