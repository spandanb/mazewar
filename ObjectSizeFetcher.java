import java.lang.instrument.Instrumentation;
import java.io.*;

public class ObjectSizeFetcher {
    /*
    private static Instrumentation instrumentation;
    public static void premain(String args, Instrumentation inst) { instrumentation = inst; }
    */

    public static long getObjectSize(Object obj) {
//        return instrumentation.getObjectSize(o);
        try {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);

            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            objectOutputStream.close();

            return byteOutputStream.toByteArray().length;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
