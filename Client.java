import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Purpose:
 *      Client for retrieving specified file from the file server
 *  
 * @version 1.0
 * @author Dylan Spence
 * @date 2020-12-02
 */
public class Client {

    private static final byte[] NOT_FOUND = "N".getBytes();
    private static final byte[] INVALID_SYMBOL = "I".getBytes();
    private static final byte[] READY = "R".getBytes();
    private static final int BUFFER_SIZE = 1024;

    protected String serverName;
    protected int serverPort;
    protected String filename;

    /**
     *  Constructor
     *  @param serverName = IP address of server to connect to
     *  @param serverPort = port of server to connect to
     *  @param filename   = name of file to retrieve from the server
     */
    public Client(String serverName, int serverPort, String filename) {
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.filename = filename;
    }

    /**
     *  Purpose:
     *      Writes file data received from server to a file of the same name in the working directory, requested in chunks of 
     *      BUFFER_SIZE until the value returned from read equals -1. If an error occurs with writing the file, the program closes.
     * 
     *  @param filename  = name of the file to write.
     *  @param in        = BufferedInputStream with an established connection to the server.
     * 
     *  NOTES:
     *  @exception IOException : when something goes wrong when reading from the connection or writing to the file.
     * 
     */
    private void writeFile(String filename, BufferedInputStream in){
        try(
            BufferedOutputStream outFile = new BufferedOutputStream( new FileOutputStream(filename), BUFFER_SIZE);
        ){
            byte[] buffer = new byte[BUFFER_SIZE];
            int done;

            while((done = in.read( buffer)) != -1){
                outFile.write(buffer, 0, done);
            }
            
        } catch (IOException e){
            System.err.println(e);
            System.exit(-2);
        }
    }

    /**
     *  Purpose:
     *      Establish a connection to the server and send the filename to retrieve from the server, followed by a newline. 
     *      If the response received from server is;
     *            - READY : Calls writeFile to retrieve and write the files data.
     *            - NOT_FOUND : Displays file not found message and closes the connection.
     *            - INVALID_SYMBOL : Displays an invalid symbol message and closes the connection.
     *      Upon error the program closes.
     * 
     *  NOTES:
     *  @exception UnknownHostException : when the IP of the server could not be determined.
     *  @exception IOException : when an I/O error occurs when creating the socket.
     *  @exception SecurityException : when a security manager and its checkConnect method refuses the operation.
     *  @exception IllegalArgumentException : when the port parameter is outside the valid range of port values.
     *      
     */
    public void connect(){
        try(
            Socket socket = new Socket(serverName, serverPort);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream(), BUFFER_SIZE);
        ){
            out.println(filename);
            byte[] response = new byte[1];
            in.read(response);

            if(Arrays.equals(response, READY)){
                writeFile(filename, in);
            }
            else if(Arrays.equals(response, NOT_FOUND)){
                System.out.println("File not found: " + filename);
            }
            else if(Arrays.equals(response, INVALID_SYMBOL)){
                System.out.println("Invalid Symbol in filename: " + filename);
            }
        } catch(UnknownHostException e){
            System.err.println(e);
            System.exit(-1);
        } catch(IOException e){
            System.err.println(e);
            System.exit(-2);
        } catch(SecurityException e){
            System.err.println(e);
            System.exit(-3);
        } catch(IllegalArgumentException e){
            System.err.println(e);
            System.exit(-4);
        }
    }

    public static void main(String[] args){
        {
            if (args.length == 0){
                System.out.println("Requires one argument.");
                System.exit(0);
            }
            Client client = new Client("localhost", 12345, args[0]);
            client.connect();
        }
    }
}


