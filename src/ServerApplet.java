/*Name: Harshini Chandrasekar
  ID: 1001586563 */

/*-------------------References-------------------------------------------------------------------------*/
/*
 
1. Distributed Systems Principles and Paradigms Second edition by Andrew S. Tanenbaum Maarten van Steen -(Page 395-410)
2. http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html
3. http://javarevisited.blogspot.com/2015/06/how-to-create-http-server-in-java-serversocket-example.html
4. Secure Program with static analysis by Brian Chess & Jacob West- (Page – 319 & 320)
5. http://www.baeldung.com/java-write-to-file
6. https://regex101.com/
7. https://stackoverflow.com/questions/10820033/make-a-simple-timer-in-java/14323134

/*-------------------References---------------------------------------------------------------------------*/

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.applet.*;
import java.awt.*;

/*Server class implements Runnable interface which creates a thread for server to continuously listen for client connections*/
public class ServerApplet extends Applet implements Runnable
{
	private Socket socket = null; //Object for Socket class
	private DataOutputStream streamOut = null; //Object for DataOutputStream
	private ClientThread client    = null;  //Object for the ClientThread which Listens to and handles the message received from the server
	private int requestingIndex=-1; //Variable for finding ID of client while requesting for Status
	private ServerAppletThread clients[] = new ServerAppletThread[4]; //Class for managing each client thread
	 private ServerSocket server = null; //Server Socket
	 private Thread       thread = null; //Initialising Thread for timer
	 private int clientCount = 0; //Counter for number of clients
	 
	 private static int CoordinatorID = 0; //Iniatilise variable for storing CoordinatorID
	 
	 private String    serverName = null; //Variable to store Server Name
	   private int       port = 0; //Variable to store Port Number
	 
	 private static List<String> ClientNames = new ArrayList<String>();//To store client names
	 private HashMap<Integer,String> hm=new HashMap<Integer,String>();  //To map client ID to name
	 
	 private TextArea  display = new TextArea(); //Dispplay box for CoordinatorGUI
	 
	 
	 public void init()
	   { //Container class which provides the space in which other components are added
	   //Log off and Connect Button are added
	   		
	      Panel south = new Panel(); south.setLayout(new BorderLayout()); // constructs new border Layout
	      // positions the buttons and text field inside the Layout
	      Label title = new Label("Chat Room System", Label.CENTER); //label for the application
	      title.setFont(new Font("Helvetica", Font.BOLD, 18)); // Font type
	      setLayout(new BorderLayout());//This method changes layout-related information
	      add("North", title); add("Center", display); 
	      
	      getParameters(); //Function contains Server and port number
	      
	      
	      try
	      {  
		   	 //Server Socket created which is bound to the specified port.
		   	 println("Binding to port " + port + ", please wait  ...");
	         server = new ServerSocket(port);  
	         println("Server started: " + server);
	             
	         
	      }
		  //catch block is executed when the server socket is unable to bind to the port
	      catch(IOException ioe)
	      { 
	    	  println("Can not bind to port " + port + ": " + ioe.getMessage()); 
	      }
	      
	   }
	 
	 /*start function creates a new thread to listen continuously for client connections.*/
     public void start()  
     { 
	   if (thread == null) //If thread doesn't exist, instantiate and start a new thread to listen for client connections.
	   {  
		thread = new Thread(this); 
		thread.start();//thread.start(); calls the run function.
	   } 
	 }
   
   /*Function : run function is invoked from thread.start() and listens for a connection to be made from a client 
    * and calls addThread() which in turn creates a separate thread for each client. Thereby multi- threading is achieved*/
   public void run()
   {  while (thread != null)
      {  try
         {  println("Waiting for a client ..."); 
            addThread(server.accept()); 
         }
         catch(IOException ioe)   //catch block is executed when an error occurs and the thread created will be stopped and assigned to null
         {  println("Server accept error: " + ioe); stop(); }
      }
   }
   
   
   /*stop function is called when the thread needs to stopped or when an unexpected error occurs. */
   public void stop()   
   { 
	   if (thread != null)
	   {  
		   thread.stop(); 
		   thread = null;
	   }
   }
   
   /*Function: To return the nth client
    Input: Unique Client ID (socket.getPort()) during client instantiation
    Output: position of the client  */
   private int findClient(int ID)
   {  for (int i = 0; i < clientCount; i++)
         if (clients[i].getID() == ID)
            return i;
      return -1;
   }
   
   
   /*Function: To add a new thread for each client connection
    * Input: Socket */
   
   private void addThread(Socket socket)
   {  if (clientCount < clients.length) // TO check if number of clients exceeded the maximum limit
      {  println("Client accepted: " + socket);
         clients[clientCount] = new ServerAppletThread(this,socket);// ClientServerThread Object instantiation
         try
         {  clients[clientCount].open(); //Function call to open input/output stream for each connecting client
            clients[clientCount].start();  //Function call to continuously listen for message from connected client
            clientCount++; //Increase the client count after each successful connection
          }
         catch(IOException ioe)  //catch block is executed when an error occurs and new thread could not be created
         { println("Error opening thread: " + ioe); } }
      else
         println("Client refused: maximum " + clients.length + " reached."); // If No of clients > Maximum limit
   }
   
   /*Function: Reads messsages from ClientApplet and handles the function according to the messages *
    * Input: Unique ID for a client and the message sent by that client
    *Output: Handles messages according to the message received from Client*/ 
   public synchronized void handle(int ID,String input) throws IOException
   {  
	   
	   String inputName=null;
	   println("\n");
	   println(input); // Prints the HTTP Post message sent by the client
	   StringTokenizer st=new StringTokenizer(input,"\n"); //extracts message which is sent in POST method format from the ClientApplet 
	   while(st.hasMoreTokens()) 
	   {
		   inputName=st.nextToken();// Extracts the last token which is the actual message sent by the client
	   }
	   
	   if (inputName.equals(".logoff")) //Checks if client calling this function is logging off
      { 
      	for (int i = 0; i < clientCount; i++) //broadcasts messages to all the connected clients when the current client logs off
     	clients[i].send(hm.get(ID) +" has logged off.."); // Sends Logged off message to all the clients
		ClientNames.remove(ClientNames.indexOf(hm.get(ID))); //remove the current client's registered name 
      	remove(ID); //remove the client object from the client object array
      	
       }
	   else   if(inputName.contains(".CoordinatorConnect")) //Checks if the Coordinator is connecting
	   	{
		   String CName = "Coordinator"; //Save the name of the client as Coordinator
		   hm.put(ID, CName); //Map the name of the coordinator to its unique iD using Hash Map
		   CoordinatorID = ID; // Save unique ID of Coordinator in a variable
		   println("Coordinator is registered ");
		 }
   	else if (inputName.contains("Name:")) // Checks if the client is registering Name 
   	{  
   		boolean NotExist = true; //boolean to check if the name is already registered 
   		StringTokenizer str=new StringTokenizer(inputName,":"); //extracts message which is sent in POST method format from the ClientApplet
   		str.nextElement();
		String Name=str.nextElement().toString();
		
		for (String item : ClientNames) //checks if the Name given by the current client is already registered by other connected clients
		{ 
			    if (Name.equalsIgnoreCase(item))
			    {
			    	println(item);
			    	NotExist = false; //if Name already exists, then boolean is set to false
			    	
			    }
		}
		
		    if(NotExist) //is the name does not already exist then validate by regular expression
		    {
			
		    Pattern pattern = Pattern.compile("(^[a-zA-Z]([a-zA-Z]?[0-9]?){1,9}$)"); //Maximum 11 characters ; Starts with alphabet ; Accepts alpha-numeric
	  		Matcher matcher = pattern.matcher(Name); 
		 
			    if (matcher.matches()) //checks if the name given matches the regex
			    {
			    	 hm.put(ID, Name); // use hash map to map the unique ID of the client to the name provided by the client
					 println("Client "+findClient(ID)+" registered with name:"+hm.get(ID)); //Display on the server that client is registered
					 clients[findClient(ID)].send(hm.get(ID)+"successfully registered..");//find the position of the current client and send it to ClientApplet that the Client is successfully registered with the name provided
					 ClientNames.add(Name); //Add the current client's name to the clientNames List
			    }
			    else 
			    {	
			       	
			    	clients[findClient(ID)].send("Name is not in valid format.!");//display message if the name does not match the regular expression pattern.
			 
			    }
		    } 
			    
		   
		   else {
			   
			   clients[findClient(ID)].send("Name already exists .!"); //displays that name already exists when boolean notExist is false
			   
		   		}
		   
   	}
   	else if(ID!=CoordinatorID) // Condition to check for clients and allows only the clients inside the iF condition
   	{
   			if(inputName.equals(".PreCommit")) // Checks if the client has sent Commit 
   			{
	   			
		    		int CoordIndex= findClient(CoordinatorID); // find the coordinators index to send the "commit" from an individual client to the coordinator
		    		clients[CoordIndex].send(hm.get(ID)+"@"+inputName);//send the "commit" from an individual client to the coordinator
		    	   
   			}
	   
   			else if(inputName.equals(".ACK")) // Checks if the client has sent Commit 
   			{
	   			
		    		int CoordIndex= findClient(CoordinatorID); // find the coordinators index to send the "commit" from an individual client to the coordinator
		    		clients[CoordIndex].send(hm.get(ID)+"@"+inputName);//send the "commit" from an individual client to the coordinator
		    	   
   			}
	   
   			else if(inputName.equals(".Abort"))  // Checks if the client has sent Commit 
   			{
	   			int CoordIndex= findClient(CoordinatorID); // find the coordinators index to send the "abort" from an individual client to the coordinator
		   		clients[CoordIndex].send(hm.get(ID)+"@"+inputName);//send the "abort" from an individual client to the coordinator
		    	   
   			} 
   			else if (inputName.equals(".STATUS")) // Checks if the client is requesting for status from other clients in the case of coordinator crash
   			{
   				requestingIndex= findClient(ID); //find the index of the client which is requesting for the status of other clients
   				
   		   		int i=0;
   		   		
   		   			for (i=0;i < clientCount; i++)  //loop for finding the clients other than the requesting clients and the coordinator
   		   				if( i!=requestingIndex && i!=findClient(CoordinatorID))
   		   		   		{
   		   					println("Broadcasting information"); 
   		   					clients[i].send(".STATUS");// send the requesting status message to the clients except coordinator and requesting client
   		   		   		}
   			}
   			else if (inputName.contains("STATUS@")) //checks for the replied status from the clients 
   		   	{ 
   				
   		   	  	if (requestingIndex!=-1)
   				clients[requestingIndex].send(hm.get(ID)+"@"+inputName); //send the replied status from the clients to the requesting client
   		   	}
   	}
   	else if(ID==CoordinatorID) //Condtion to check if it is Coordinator
   	{
   	 for (int i = 0; i < clientCount; i++) //gets the count of the connected clients
    	 
     	if (i!=(findClient(CoordinatorID))) //send the arbitary string from the Coordinator to the clients to Commit and abort
         clients[i].send("Please press COMMIT or ABORT to proceed on the following string "+":"+inputName); 
   	}
	   
	
     else //Broadcasts current client's message to all the connected clients
     {
    	 		
         for (int i = 0; i < clientCount; i++) //gets the count of the connected clients
           clients[i].send(hm.get(ID)+":"+inputName); // broadcasts current client's message to all the connected clients
     }
   }
   
      
   /*Function: To remove a client after Log-Off  
    *Input: Unique Client ID (socket.getPort()) during client instantiation
    */
   public synchronized void remove(int ID)
   {  
	  int pos = findClient(ID); // Find position of a particular client by calling findClient(ID)
      
	  if (pos >= 0)
      {  ServerAppletThread toTerminate = clients[pos];
         println("Removing client thread " + ID + " at " + pos);
       
         if (pos < clientCount-1) // Checks if position of the client to be removed is not the last connected client 
            for (int i = pos+1; i < clientCount; i++)
              clients[i-1] = clients[i]; // Arrange subsequent client positions after a client is removed
         clientCount--; //Decrease client count after removing the client
         
         try
         { 
        	 toTerminate.close(); //Close the client object which is removed
         
         }
         
         catch(IOException ioe)
         {  
        	println("Error closing thread: " + ioe);
         }
         
         toTerminate.stop(); 
       }
   }
   
      
	 /* Function: function to print the messages GUI
   		output : print messages in the GUI*/
	  private void println(String msg)
	   {  
		  display.append(msg + "\n"); 
	   }
	  //Function: gets the server and port number where the server is connected
	   public void getParameters()
	   {  
		  serverName = "localhost";
	   	  port = 8080;
	   }
		

	   
	   
	   
	   

}
