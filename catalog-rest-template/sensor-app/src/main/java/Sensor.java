import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;

public class Sensor extends Thread {

    private final String sensorName;
    private final Integer sensorId;
    private final Double lowerBound;
    private final Double upperBound;

    public Sensor(String sensorName, Integer sensorId, Double lowerBound, Double upperBound) {
        this.sensorName = sensorName;
        this.sensorId = sensorId;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public static void main(String[] args) {
        Sensor sensor = readSensor();

        sensor.start();

        try {
            sensor.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        InetAddress address;
        Random random = new Random();
        try {
            address = InetAddress.getByName(null);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host\nTerminating");
            return;
        }
        Boolean good = true;

        try(Socket socket = new Socket(address,6666)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);
            while (good) {
                Double randomValue = (Math.random() * (upperBound - lowerBound)) + lowerBound;
                System.out.println("Sending: " + randomValue);
                out.println("info");
                out.println(sensorName);
                out.println(sensorId);
                out.println(randomValue);
                String response=in.readLine();
                if(response.equals("stop"))
                {
                    return ;
                }
                try {

                    Thread.sleep(new Random().nextInt(1000)+3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        } catch (IOException exception) {
            System.out.println("Error with read and write\nTerminating...");
        }

    }

    private static Sensor readSensor(){

        Scanner scanner = new Scanner(System.in);
        Sensor sensor = null;
        System.out.println("Enter data for the sensor{sensorName, id, lowerBound. upperBound}");
        try {
            System.out.println("Input sensorName: ");
            String sensorName = scanner.nextLine();
            System.out.println("Input id: ");
            Integer id = Integer.parseInt(scanner.nextLine());
            System.out.println("Input lowerBound: ");
            Double lowerBound = Double.parseDouble(scanner.nextLine());
            System.out.println("Input upperBound: ");
            Double upperBound = Double.parseDouble(scanner.nextLine());
            sensor = new Sensor(sensorName,id,lowerBound,upperBound);

        }
        catch(Exception exception)
        {
            System.out.println(exception);
        }

        return sensor;

    }



}
