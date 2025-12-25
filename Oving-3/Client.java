import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class Client {
    private static final String server_host = "127.0.0.1";
    private static final int server_port = 22233;

    public static void main(String[] args){
        float[] vectorA = {1f, 2f, 3f};
        float[] vectorB = {4f, 5f, 6f};

        try(DatagramSocket socket = new DatagramSocket()){
            InetAddress serverAddress = InetAddress.getByName(server_host);

            ByteBuffer buffer = ByteBuffer.allocate(4 + vectorA.length * 8);
            buffer.putInt(vectorA.length);
            for(float a : vectorA){
                buffer.putFloat(a);
            }
            for(float b : vectorB){
                buffer.putFloat(b);
            }

            DatagramPacket requestPacket = new DatagramPacket(
                    buffer.array(),
                    buffer.capacity(),
                    serverAddress,
                    server_port
            );
            socket.send(requestPacket);

            byte[] responseBuffer = new byte[4];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.receive(responsePacket);

            float result = ByteBuffer.wrap(responseBuffer).getFloat();
            System.out.println("Dot product: " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
