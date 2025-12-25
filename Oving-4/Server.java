import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

public class Server {
    public static final int port = 22233;
    public static final int UDP_message_size = 0xFFFF - 20 - 8;


    private static void handleClient(DatagramSocket socket, DatagramPacket packet){
        try{
            ByteBuffer buffer = ByteBuffer.wrap(packet.getData());

            int length = buffer.getInt();

            float[] vectorA = new float[length];
            float[] vectorB = new float[length];

            for (int i = 0; i < length; i++) {
                vectorA[i] = buffer.getFloat();
            }
            for (int i = 0; i < length; i++) {
                vectorB[i] = buffer.getFloat();
            }

            float result = calculateDot(vectorA, vectorB);

            ByteBuffer responseBuffer = ByteBuffer.allocate(4);
            responseBuffer.putFloat(result);

            DatagramPacket responsePacket = new DatagramPacket(
                    responseBuffer.array(),
                    responseBuffer.capacity(),
                    packet.getAddress(),
                    packet.getPort()
            );
            socket.send(responsePacket);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private static float calculateDot(float[] vectorA, float[] vectorB){
        float product = 0;

        for (int i = 0; i < vectorA.length; i++) {
            product = product + vectorA[i] * vectorB[i];
        }
        return product;
    }

    public static void main(String[] args){
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("UDP server kjører på port: " + port);

            byte[] receiveBuffer = new byte[UDP_message_size];
            DatagramPacket packet = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            while(true){
                socket.receive(packet);
                handleClient(socket, packet);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
