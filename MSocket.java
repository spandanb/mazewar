import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class MSocket{

    private Socket socket = null;
    private ServerSocket serverSocket = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    
    /*
     *This creates a regular socket
     */
    public MSocket(String host, int port) throws IOException{
        System.out.println("Entering MSocket constr");
        socket = new Socket(host, port);
        System.out.println("Here 1");
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Here 2");
        out = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("Leaving MSocket constr");
    }

    /*
     *This creates a server socket
     */    
    public MSocket(int port){
        
    }
    
    public void writeObject(Object o) throws IOException{
        System.out.println("MSocket:writeObject " + o);
        out.writeObject(o);
    }
    
    public Object readObject() throws IOException, ClassNotFoundException{
        System.out.println("MSocket:readObject ");
        return in.readObject();
    }
    
    public void close() throws IOException{
        in.close();
        out.close();
        socket.close();
    }
    
    

}