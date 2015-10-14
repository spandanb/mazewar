import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;

//public class ClientSenderThread implements Runnable {
public class ClientSenderThread implements Runnable {

    private MSocket mSocket = null;
    private BlockingQueue<MPacket> eventQueue = null;
    
    public ClientSenderThread(MSocket mSocket,
                              BlockingQueue eventQueue){
        this.mSocket = mSocket;
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
                mSocket.writeObjectUnordered(toServer);    
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();    
            }
            
        }
    }
}
