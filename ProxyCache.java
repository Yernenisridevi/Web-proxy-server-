import java.net.*;
import java.io.*;
import java.util.*;

public class ProxyCache {
    /** Port for the proxy */
    private static int port;
    /** Socket for client connections */
    private static ServerSocket socket;

    /** Create the ProxyCache object and the socket */
    public static void init(int p) {
	port = p;
	try {
		 socket =  new ServerSocket(port);
	} catch (IOException e) {
	    System.out.println("Error creating socket: " + e);
	    System.exit(-1);
	}
    }

    public static void handle(Socket client) {
	Socket server = null;
	HttpRequest request = null;
	HttpResponse response = null;

	/* Process request. If there are any exceptions, then simply
	 * return and end this request. This unfortunately means the
	 * client will hang for a while, until it timeouts. */

	/* Read request by using BufferedReader*/
	try {
		BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
	    request = new HttpRequest(fromClient);
	} catch (IOException e) {
	    System.out.println("Error reading request from client: " + e);
	    return;
	}
	/* Send request to server by using DataOutputStream and writeBytes */
	try {
	    /* Open socket and write request to socket */
		server = new Socket(InetAddress.getByName(request.getHost()), request.getPort()); 
	    DataOutputStream toServer = new DataOutputStream(server.getOutputStream()); 
	    toServer.writeBytes(request.toString());
	} catch (UnknownHostException e) {
	    System.out.println("Unknown host: " + request.getHost());
	    System.out.println(e);
	    return;
	} catch (IOException e) {
	    System.out.println("Error writing request to server: " + e);
	    return;
	}
	/* Read response and forward it to client by using DataInputStream and DataOutputStream */
	try {
		DataInputStream fromServer = new DataInputStream(server.getInputStream()); 
	    response = new HttpResponse(fromServer);
	    DataOutputStream toClient = new DataOutputStream(client.getOutputStream());
	    toClient.writeBytes(response.toString()); 
	    toClient.write(response.body); 
	    /* Write response to client. First headers by using writeBytes, then body by using write */
	    client.close();
	    server.close();
	} catch (IOException e) {
	    System.out.println("Error writing response to client: " + e);
	}
    }


    /** Read command line arguments and start proxy */
    public static void main(String args[]) {
	int myPort = 0;
	
	try {
	    myPort = Integer.parseInt(args[0]);
	} catch (ArrayIndexOutOfBoundsException e) {
	    System.out.println("Need port number as argument");
	    System.exit(-1);
	} catch (NumberFormatException e) {
	    System.out.println("Please give port number as integer.");
	    System.exit(-1);
	}
	
	init(myPort);

	/** Main loop. Listen for incoming connections and spawn a new
	 * thread for handling them */
	Socket client = null;
	
	while (true) {
	    try {
	    	client = socket.accept(); 
			handle(client);
	    } catch (IOException e) {
		System.out.println("Error reading request from client: " + e);
		/* Definitely cannot continue processing this request,
		 * so skip to next iteration of while loop. */
		continue;
	    }
	}

    }
}
