import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class ServeConnection extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static List<String> toKill= new ArrayList<>();
    public ServeConnection(Socket s) throws IOException {
        socket = s;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }


    public void saveToDb(String name, Double value){
        try
        {
            // create a mysql database connection
            String myUrl = "jdbc:postgresql://localhost:5432/Sensor?user=postgres&password=";
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(myUrl, "root", "");


            // the mysql insert statement
            String query = " INSERT INTO public.sensor(" +
                    "name, measurement, time)" +
                    "VALUES ( ?, ?, ?);";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString (1, name);
            preparedStmt.setDouble    (2, value);
            preparedStmt.setLong    (3, System.currentTimeMillis());

            // execute the preparedstatement
            preparedStmt.execute();

            conn.close();
        }
        catch (Exception e)
        {
            System.err.println("Got an exception!");
            System.err.println(e.getMessage());
        }
    }



    @Override
    public void run() {
        try {
            System.out.println("Serving connection");
            while (true) {

                String purpose=in.readLine();
                if(purpose.equals("kill"))
                {
                    String toKillName=in.readLine();
                    toKill.add(toKillName);
                    return;
                }
                String name = in.readLine();
                String id = in.readLine();
                String value = in.readLine();
                if (toKill.contains(name)) {
                    System.out.println("Connection closing");
                    out.println("stop");
                    toKill.remove(name);
                    return;
                }
                System.out.println("Received:");
                System.out.println(name);
                System.out.println(id);
                System.out.println(value);

                saveToDb(name,Double.parseDouble(value));
                out.println("not-stop");
                try {
                    Thread.sleep(new Random().nextInt(1000)+2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("IO Exception");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Socket not closed");
            }
        }
    }
}

public class Server {

    public static void main(String[] args) throws IOException {

        try (ServerSocket s = new ServerSocket(6666)) {
            System.out.println("Server ready");
            while (true) {
                Socket socket = s.accept();
                try {
                    ServeConnection sc = new ServeConnection(socket);
                    sc.start();
                } catch (IOException e) {
                    System.err.println("IO Exception");
                }
            }
        }
    }

}
