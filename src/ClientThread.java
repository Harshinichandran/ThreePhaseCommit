/*Name: Harshini Chandrasekar
  ID: 1001586563
 */

/*-------------------References-------------------------------------------------------------------------*/
/*1. Distributed Systems Principles and Paradigms Second edition by Andrew S. Tanenbaum Maarten van Steen -(Page 395-410)
2. http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html
3. http://javarevisited.blogspot.com/2015/06/how-to-create-http-server-in-java-serversocket-example.html
4. Secure Program with static analysis by Brian Chess & Jacob West- (Page � 319 & 320)
5. http://www.baeldung.com/java-write-to-file
6. https://regex101.com/
7. https://stackoverflow.com/questions/10820033/make-a-simple-timer-in-java/14323134


/*-------------------References---------------------------------------------------------------------------*/






import java.net.*;
import java.util.StringTokenizer;
import java.io.*;

/*Class: Manages messages received from Server.*/
public class ClientThread extends Thread
{  
   private Socket           socket   = null; //Object of Socket class
   private ClientApplet  client   = null; //Object of Client Applet
   public DataInputStream  streamIn = null, nameIn=null; //InputStream for reaading messages from stream
  
   /*Function: Constructor for ClientThread to manage messages received from Server
	 Input: Objects of ClientApplet and Socket class	  */
   public ClientThread(ClientApplet chatClientApplet, Socket _socket)
   {  client   = chatClientApplet;
      socket   = _socket; //Unique ID for each Client
      open();  //Function call to open input Stream for reading messages from Server for each connected client.
      start(); //Function call to continuously listen for message from connected client
   }
   
   /*Function: Opens InputStream for reading data from Stream(Data sent by server)*/
   public void open()
   {  try
      {  
	   	streamIn  = new DataInputStream(socket.getInputStream());
      }
      catch(IOException ioe) // catch block is executed when the input is not read correctly
      {  System.out.println("Error getting input stream: " + ioe);
         client.stop();
      }
   }
   /*Function:Closes the InputStream Socket*/
   //Close Function is called when the Client needs to be stopped or when unexpected error occurs.
   public void close()
   {  try
      {  if (streamIn != null) streamIn.close();
      }
      catch(IOException ioe)
      {  System.out.println("Error closing input stream: " + ioe);
      }
   }

   /*Function: Reads messages continuously from the server which is the GET method format
     Output: messages from server are read and sent to handle() in ClientApplet*/
   public void run()
   {    try
         { 
	   while (true)
	      {
    	 
		   String message=null;
		   //String inputbuffer=null;
		  // String buffer=null;
		   message=streamIn.readUTF();//reads the message in get Format from the Server
		   String input=null;
		   
		  /* StringTokenizer st=new StringTokenizer(buffer,"\n");
		   
		   inputbuffer=st.nextToken();
		   
		   	if(inputbuffer.contains("GET /ChatClient?message"))//checks if the message is in GET Format
		   	{
	
		   		
		   		StringTokenizer str=new StringTokenizer(inputbuffer,"\t");
		   		str.nextToken();
		   		message=str.nextToken();//extracts the actual message from the GET method
		   	
	 
		   	}*/
		   	
		 
		   System.out.println("\n");
		   System.out.println(message); // Prints the HTTP Post message sent by the client
		   StringTokenizer st=new StringTokenizer(message,"\n"); //extracts message which is sent in POST method format from the ClientApplet 
		   while(st.hasMoreTokens()) 
		   {
			   input=st.nextToken();// Extracts the last token which is the actual message sent by the client
		   }

    	  client.handle(input); // message extracted is sent to handle() in ClientApplet class
	      }
         }
         catch(IOException ioe) // catch block is executed when the client is not connected to the server properly to read messages
         {  
        	 client.stop();
        	// client.close();
        	 System.out.println("Listening error: " + ioe.getMessage());
            
         }
      
   }
}

