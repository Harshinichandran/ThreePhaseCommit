/*Name: Harshini Chandrasekar
  ID: 1001586563 */

/*-------------------References-------------------------------------------------------------------------*/
/*1. Distributed Systems Principles and Paradigms Second edition by Andrew S. Tanenbaum Maarten van Steen -(Page 395-410)
2. http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html
3. http://javarevisited.blogspot.com/2015/06/how-to-create-http-server-in-java-serversocket-example.html
4. Secure Program with static analysis by Brian Chess & Jacob West- (Page – 319 & 320)
5. http://www.baeldung.com/java-write-to-file
6. https://regex101.com/
7. https://stackoverflow.com/questions/10820033/make-a-simple-timer-in-java/14323134

/*-------------------References---------------------------------------------------------------------------*/






import java.net.*;
import java.util.Date;
import java.io.*;

/*Class: Manages each client thread.*/
public class ServerAppletThread extends Thread
{  private ServerApplet       server    = null; //Object of Server class
   private Socket           socket    = null; //Object for Socket class
   private int              ID        = -1; 
   private DataInputStream  streamIn  =  null; //
   private DataOutputStream streamOut = null;
  
   /*Function: Constructor for ServerThread to manage each client operations
	 Input: Objects of Server and Socket class	  */
   public ServerAppletThread(ServerApplet _server, Socket _socket)
   {  super();
   	
      server = _server; //
      socket = _socket; //
      ID     = socket.getPort(); //Unique ID for each client
   }
 
   /*Function: messages from the server is sent to the client in GET method Format
    *Input : message that needs to sent to the clients */ 
   public void send(String msg)
   {   try
       {  
	 
	   StringBuilder sb=new StringBuilder();
	
	   String inputtext=msg;
	
	   Date date = new Date(); //calculates the current time when the message is sent
	   String post="POST /Server HTTP/1.1"; 
	   String host="Host: http://localhost:8080/Server";
	   String accept="Accept: text/xml, text/html, text/plain, image/plain";
	   String acceptLanguage="Accept-Language: en-us,en";
	   String acceptEncode="Accept-Encoding: qzip";
	   String useragent="User-Agent: Mozilla/5.0";
	   String contenttype="Content-Type: application/x-www-form-urlencoded";
	   String contentLength="Content-Length:"+inputtext.length(); //calculates the length of the input
	   String currentdate= date.toString(); 
	   String connection="Connection: keep-alive";
	   sb.append(post).append("\n").append(accept).append("\n").append(acceptLanguage).append("\n").append(acceptEncode).append("\n").append(useragent).append("\n").append(contenttype).append("\n").append(contentLength).append("\n").append(currentdate).append("\n").append(connection).append("\n").append(host).append("\n").append(inputtext);
	   	  streamOut.writeUTF(sb.toString()); // the appended message is written to the outputStream
          streamOut.flush(); //Buffered output bytes are written to the outputStream
       }
       catch(IOException ioe)  //catch block is executed when error occurs while sending the message to the clients
       {  
    	   System.out.println(ID + " ERROR sending: " + ioe.getMessage());
          server.remove(ID);
         
       }
   }
   
   /*Function: Returns the unique client ID */
   public int getID()
   {  
	   return ID;
   }
   
   /*Function: Reads messages continuously from a client
     Output: messages from client are read and sent to handle() in Server*/
   public void run()
   {  System.out.println("Server Thread " + ID + " running.");
      while (true) //Continuously listens for messages from a Client
      {  
    	  try
      {
    		  String input= streamIn.readUTF(); //Reads messages from the inputStream 
    		  server.handle(ID,input); // call to the handle function where the input messages are handled according to the content
      }
         catch(IOException ioe) // catch block executed when error occurs while reading input messages
         { 
        	 System.out.println(ID + " ERROR reading: " + ioe.getMessage());
            server.remove(ID);
            stop();
         }
      }
   }
   
   /*Function: Creates a DataInputStream and DataOutputStream for writing and reading messages
     */
   public void open() throws IOException
   {  streamIn = new DataInputStream(new 
                        BufferedInputStream(socket.getInputStream()));
      streamOut = new DataOutputStream(new
                        BufferedOutputStream(socket.getOutputStream()));
   }
   
   /*Function:Closes the InputStream , OutputStream and Socket*/
   //Close Function is called when the Server is stopped or when unexpected error occurs. 
   public void close() throws IOException
   {  if (socket != null)    socket.close();
      if (streamIn != null)  streamIn.close();
      if (streamOut != null) streamOut.close();
   }
}
