import java.io.InvalidObjectException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.Random;

public class ServerSenderThread implements Runnable {

    private ObjectOutputStream[] outputStreamList = null;
    private BlockingQueue eventQueue = null;
    private int globalSequenceNumber; 
    
    public ServerSenderThread(ObjectOutputStream[] outputStreamList,
                              BlockingQueue eventQueue){
        this.outputStreamList = outputStreamList;
        this.eventQueue = eventQueue;
        this.globalSequenceNumber = 0;
    }

    /*
     *Handle the initial joining of players including 
      position initialization
     */
    public void handleHello(){
        
        //The number of players
        int playerCount = outputStreamList.length;
        Random randomGen = null;
        Player[] players = new Player[playerCount];
        System.out.println("In handleHello");
        MPacket hello = null;
        try{        
            for(int i=0; i<playerCount; i++){
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
                
                //Start them all facing North
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
            e.printStackTrace();
            Thread.currentThread().interrupt();    
        }catch(IOException e){
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        System.out.println("Exiting handleHello");
    }
    
    public void run() {
        MPacket toBroadcast = null;
        
        handleHello();
        
        while(true){
            try{
                //Take packet from queue to broadcast
                //to all clients
                toBroadcast = (MPacket)eventQueue.take();
                //Tag packet with sequence number and increment sequence number
                //NOTE: This approach won't work in the distributed case
                toBroadcast.sequenceNumber = this.globalSequenceNumber++;
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
