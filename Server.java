import java.net.*;
import java.io.*;
import java.nio.channels.*;
import java.util.regex.Pattern;

/**
 * Purpose:
 *      File server to send requested file from available files to the client.
 *  
 * @version 1.0
 * @author Dylan Spence
 * @date 2020-12-02
 */
public class Server {


    private static final byte[] NOT_FOUND = "N".getBytes();
    private static final byte[] INVALID_SYMBOL = "I".getBytes();
    private static final byte[] READY = "R".getBytes();
    private static final String directory = "Images/";
    private static final int BUFFER_SIZE = 1024;
    protected int port;

    /**
     * Constructor
     * @param port : port for the server to listen on
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Purpose:
     *      Creates a FileInputStream to read the data in from the requested file. 
     *      If successful will send a READY flag.
     *      Reads data into a buffer of size BUFFER_SIZE (until the read() function returns -1)
     *      while sending the buffered data to the client in byte form.
     *  
     *  @param filename : The name of the requested file to send.
     *  @param outStream : A BufferedOutputStreamwith an established connection to the client.
     * 
     *  Returns:
     *      True if successful, false otherwise.
     * 
     * NOTES:
     *      If the file does not exist the server will respond with the NOT_FOUND flag to inform the client and return false.
     * 
     *  @exception IOException : when an I/O error occurs when creating the file input stream.
     *      
     */
    private boolean readFile(String filename, BufferedOutputStream outStream ){
        try(
            BufferedInputStream inFile = new BufferedInputStream(new FileInputStream(directory + filename), BUFFER_SIZE);
        ){
            outStream.write(READY, 0, READY.length);
            outStream.flush();

            byte[] buffer = new byte[BUFFER_SIZE];
            int done; 

            while((done = inFile.read(buffer)) != -1){
                outStream.write(buffer, 0, done);
                outStream.flush();
            }
            return true;

        } catch (IOException e){
            System.err.println(e);
        } 
        return false;
    }

    /**
     * Purpose:
     *      Creates socket on specified port and serves until manually closed.
     *      Establishes a connection to the client, read data from socket encoded in "utf-8" until a newline is received. 
     *      Server calls readFile to send the data from the requested file to the client. If the file does not exist, the server 
     *      will respond with the NOT_FOUND flag.
     * 
     * NOTES:
     *      If the message received from client contains any '/' characters, the server will respond with the INVALID_SYMBOL flag
     *      and close the connection.
     *      This connection is set to serve until manually closed.
     * 
     *  @exception IOException : when an I/O error occurs when waiting for a connection or if an error occurs while setting up the serverSocket.
     *  @exception SecurityException : when a security manager and its checkListen or checkAccept method refuse the operation.
     *  @exception IllegalArgumentException : when the port parameter is outside the valid range of port values 0-65535.
     *  @exception IllegalBlockingModeException : Thrown when the socket has an associated channel, the channel is in non-blocking mode, and there is no connection ready to be accepted.
     *      
     */
    public void serve() {
        try(
            ServerSocket serverSocket = new ServerSocket(port);
        ){
            while(true){
                try (
                    Socket clientSocket = serverSocket.accept();
                    BufferedOutputStream outStream = new BufferedOutputStream(clientSocket.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"), BUFFER_SIZE);
                ) {
                    String inputLine = in.readLine();
                    if (inputLine.contains("/")){
                        outStream.write(INVALID_SYMBOL, 0, INVALID_SYMBOL.length);
                    }
                    else if(!readFile(inputLine, outStream)){
                        outStream.write(NOT_FOUND, 0, NOT_FOUND.length);
                    }
                } catch (IOException e) {
                    System.err.println(e);
                } catch (SecurityException e) {
                    System.err.println(e);
                } catch (IllegalArgumentException e) {
                    System.err.println(e);
                } catch (IllegalBlockingModeException e) {
                    System.err.println(e);
                }
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(-1);
        } catch (SecurityException e) {
            System.err.println(e);
            System.exit(-2);
        }
    }

    public static void main(String[] args){
        final int PORT = 12345;
        Server server = new Server(PORT);
        server.serve();
    }
}