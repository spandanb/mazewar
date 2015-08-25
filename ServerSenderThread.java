import java.io.InvalidObjectException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.Random;

public class ServerSenderThread implements Runnable {

    private ObjectOutputStream[] outputStreamList = null;
    private BlockingQueue eventQueue = null;
    
    public ServerSenderThread(ObjectOutputStream[] outputStreamList,
                              BlockingQueue eventQueue){
        this.outputStreamList = outputStreamList;
        this.eventQueue = eventQueue;
    }

    /*
     *Handle the position initialization
     */
    public void handleHello(){
        
        //The number of players
        int count = outputStreamList.length;
        Random randomGen = null;
        Player[] players = new Player[count];
        System.out.println("In handleHello");
        MPacket hello = null;
        try{        
            for(int i=0; i<count; i++){
                hello = (MPacket)eventQueue.take();
                //Sanity check 
                if(hello.type != MPacket.HELLO){
                    throw new InvalidObjectException("Expecting HELLO Packet");
                }
                if(randomGen == null){
                   randomGen = new Random(hello.mazeSeed); 
                }
                //Get a random location for player
                Point point =
                    new Point(randomGen.nextInt(hello.mazeWidth),
                          randomGen.nextInt(hello.mazeHeight));
                
                //Start them all facing North for now    
                Player player = new Player(hello.name, point, Player.North);
                players[i] = player;
            }
            
            hello.event = MPacket.HELLO_RESP;
            hello.players = players;
            //Now broadcast the HELLO
            System.out.println("Sending " + hello);
            for(ObjectOutputStream out: outputStreamList){
                out.writeObject(hello);   
            }
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();    
        }catch(IOException e){
            Thread.currentThread().interrupt();
        }
        System.out.println("Exiting handleHello");
    }
    
    public void run() {
        MPacket toBroadcast = null;
        
        handleHello();
        
        while(true){
            try{
                //Take packet from queue
                toBroadcast = (MPacket)eventQueue.take();
                System.out.println("Sending " + toBroadcast);
                //Send it to all clients
                for(ObjectOutputStream out: outputStreamList){
                    out.writeObject(toBroadcast);
                }
            }catch(InterruptedException e){
                System.out.println("Throwing Interrupt");
                Thread.currentThread().interrupt();    
            }catch(IOException e){
                System.out.println("Throwing Interrupt");
                Thread.currentThread().interrupt();
            }
            
        }
    }
}
