/*Name: Harshini Chandrasekar
  ID: 1001586563
 */

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
import java.util.HashMap;
import java.util.StringTokenizer;
import java.io.*;
import java.applet.*;
import java.awt.*;

/*Class : Contains Coordinator interface in which messages are sent to and received from the server */
public class ClientCoordinator extends Applet implements Runnable// 
{ 
   private static Socket socket = null; //Object for Socket class
   public static DataOutputStream streamOut = null; //Object for DataOutputStream
   private static ClientCoordinatorThread client    = null;  //Object for the ClientThread which Listens to and handles the message received from the server
   private static Thread       thread = null; // thread for timer 
   private boolean flag=false; 
 private static ClientCoordinator coord=new ClientCoordinator(); // Object for ClientCoordinator Class
   public static TextArea  display = new TextArea(); //Variables initialized for the chat interface
   public static TextField input   = new TextField(); 
   private Button    send    = new Button("Send"), connect = new Button("Connect"),
                     quit    = new Button("Log Off");
   
   private String    serverName = null; //Variable to store Server Name
   private int       serverPort = 0; //Variable to store Port Number
   
   public static HashMap<String,String> clientResponses=new HashMap<String,String>(); 
 //Variables initialized for implementing timer 
   public static volatile long startTimer = 0;
  
	public static String state = null;
	//Variables used for storing the successfully registered Client name
	
	public static int commitCount = 0;
	public static int abortCount=0;
	public static int ackCount = 0;

/*Function: Called by the browser or applet viewer to display the chat interface
 * Output: A chat interface where the client operations take place  */
public void init()
   {  Panel keys = new Panel(); keys.setLayout(new GridLayout(1,2)); //Container class which provides the space in which other components are added
      keys.add(quit); keys.add(connect); //Log off and Connect Button are added
      Panel south = new Panel(); south.setLayout(new BorderLayout()); // constructs new border Layout
      south.add("West", keys); south.add("Center", input);  south.add("East", send); // positions the buttons and text field inside the Layout
      Label title = new Label("Chat Room System", Label.CENTER); //label for the application
      title.setFont(new Font("Helvetica", Font.BOLD, 14)); // Font type
      setLayout(new BorderLayout());//This method changes layout-related information
      add("North", title); add("Center", display);  add("South",  south);
      quit.	setEnabled(false); //the Log off button is disabled before connection to the server 
      send.	setEnabled(false); //the Send button is disabled before connection to the server 
      getParameters(); //Function contains Server and port number
      }

/*Function: triggers action according to the button clicked(Connect, Send, Logoff)
  *Input: Event object which encapsulates events from the Graphical user Interface 
 *Output: Calls respective functions depending on the actions triggered */

   public boolean action(Event e, Object o)
   {  
	   if (e.target == quit)//When the Log Off button is clicked 
      {  	input.setText(".logoff"); // the value for the button is set as ".logoff"
      		try {
				send();  //sends message ".logoff" to the server
			} catch (IOException e1) { //catch block is executed when error occurs
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}  quit.setEnabled(true); send.setEnabled(true); connect.setEnabled(true);
      }
      else if (e.target == connect)//When Connection button is clicked
      {  	
    	  input.setText(".CoordinatorConnect"); 
    	  connect(serverName, serverPort); //Connects to the server 'localhost' in port '8080'
      }
      else if (e.target == send) // When send button is clicked
      { 
    	  try {
			send(); //sends messages enetered in the text Field to the server
	   } catch (IOException e1) {  //Catch Block executed when error occurs
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
    	  
      }
   
      return true; // returns true if any of the events are triggered
   }
  
   
   
  /*Function: Establishes connection with the server and is called when the connect button is clicked
   * Input: Server name(localhost) and port number(8080)
   * Output: Connects to the server using the given server name and port */
   public void connect(String serverName, int serverPort)
   {  
	    println("Establishing connection. Please wait ...");
      try
      {  socket = new Socket(serverName, serverPort); //ServerName and port number stored in the object of socket
         println("Connected: " + socket);
         
         println("------Coordinator is connected----");
         println("Send an arbitary string once the clients are connected");
         
         open(); //Function which opens input and output stream
         send.setEnabled(true); //send button enabled for client to start sending messages
         connect.setEnabled(false); //connect button is disabled as connection is already established
         quit.setEnabled(true); //Log off button enabled for client to leave the chat room
        // }
      }
      catch(UnknownHostException uhe) //catch block executed when the connection to server fails and other unexpected errors
      {  println("Host unknown: " + uhe.getMessage()); }
      catch(IOException ioe)
      {  println("Unexpected exception: " + ioe.getMessage()); } 
    }
  
  /*Function: Function to delay the send function in Coordinator (used in the Coordinator Crash scenario)
   */
   public static void timerDelay()
   {
	   long delayTime = System.currentTimeMillis();//Stores the system's current time in Milli seconds
	   long end = delayTime+5000;
	   while(System.currentTimeMillis() < end) //Condition to make the thread sleep until the delay time
	   {
		   try{
		    	 
		    	 Thread.sleep( 100 );
		      } catch(Exception e){
		    	 
		    	 
		     	}
	   }
   }
   
   /*Function: Instantiates a timer to track the time taken for the clients to respond for the arbitary string.
    * Output: start the timer when the coordinator sends an arbitary string and tracks the time taken for a client to respond  */
   public static void timer()
   {
	   println("Time left for clients to respond to the arbitary string is 30 Seconds");
	   startTimer = System.currentTimeMillis(); //Returns the current time of the system which is the starting time for the timer
	   
	   long end = startTimer+30000; //Timer runs for 65 seconds before which client needs to respond
	   while(System.currentTimeMillis() < end) //Loop executes until the time exceeds 65 seconds
	   {
	   	   if(state.equalsIgnoreCase("WAIT")) { // Checks if the current state is wait, if yes then the thread is in sleep till the end time
	     try{
	    	 //println("Wait state"+state);
	    	 Thread.sleep( 100 );
	    	

	    	//action(null, objectLock);
	     	}catch(Exception e){
	    	 
	    	 
	     	}
		   }
		   else if((state.equalsIgnoreCase("GLOBALCOMMIT"))||state.equalsIgnoreCase("GLOBALABORT"))// If the state is GLOBALCOMMIT or GLOBALABORT then we come out of the while loop and 
			           										         //Coordinator gets ready for next transaction
		  {                                                                                        
			 end = startTimer;
		  }
	   }
	     if (clientResponses.size()<=2) //the responses from the client stored in a hashmap is checked if all three clients have responded
	     {
	    	 println("One or more Clients did not respond within a finite time. Initiating GLOBAL ABORT");
				input.setText(".GLOBALABORT"); // text is set as GLOBALABORT if one or more clients do not respond within a finite time
				try {
					send(); // send the text "GLOBALABORT" to the Server
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	     }
	   	 //Re-initalizing variables after the transaction from coordinator is complete
	     clientResponses.clear();
	     commitCount=0;
	     state=null;
	 }
   /*Function : Run function for Timer thread 
    * Output: Calls the timer function*/
     public void run()
   {
	  
	  timer();
   }
   
   /*Function : Messages from the Coordinator is sent to the Server in POST method Format*/
   public static void send() throws IOException
   {  
	   
	   StringBuilder sb=new StringBuilder();
	   Date date = new Date(); //calculates the current time when the message is sent
	   String post="POST /Server HTTP/1.1"; 
	   String host="Host: http://localhost:8080/Server";
	   String accept="Accept: text/xml, text/html, text/plain, image/plain";
	   String acceptLanguage="Accept-Language: en-us,en";
	   String acceptEncode="Accept-Encoding: qzip";
	   String useragent="User-Agent: Mozilla/5.0";
	   String contenttype="Content-Type: application/x-www-form-urlencoded";
	   String contentLength="Content-Length:"+input.getText().length(); //calculates the length of the input
	   String currentdate= date.toString(); 
	   String connection="Connection: keep-alive";
	  
	 
		  try
		  {  
		   String inputtext=null;//Variables initialized to send messages to Server
		   String timer = null;
		 
		   if(input.getText().equals(".GLOBALCOMMIT"))  //checks if the input is only messages that a client sends to another connected client
		   {
			 //timer= timer(); //if it is input messages then the timer function is called and calculates the interval between messages sent
			 inputtext=".GLOBALCOMMIT"; //appends the time interval for the message with the actual message
			 
		   }
		   else if(input.getText().equals(".GLOBALABORT"))  //checks if the input is only messages that a client sends to another connected client
		   {
			 //timer= timer(); //if it is input messages then the timer function is called and calculates the interval between messages sent
			 inputtext=".GLOBALABORT"; //appends the time interval for the message with the actual message
			
		   }
		   else if(input.getText().equals(".ACK"))  //checks if the input is only messages that a client sends to another connected client
		   {
			   println("Sending acknowledgement from coordnitaor to clients");
			 //timer= timer(); //if it is input messages then the timer function is called and calculates the interval between messages sent
			 inputtext=".ACKNOWLEDGEMENT"; //appends the time interval for the message with the actual message
			
		   }
		   else if(input.getText().equals(".logoff")) //checks if the client is logging off from the chat room
		   {
			 inputtext=input.getText(); //only ".logoff" is sent as a message to the Server 
		   }
		   else 
		   {
			  	   
			   inputtext= input.getText();//Stores the arbitary string entered by the coordinator from the Textbox in the GUI
			   //Appends the string and sends it to the Server in HTTP POST Format
			   sb.append(post).append("\n").append(accept).append("\n").append(acceptLanguage).append("\n").append(acceptEncode).append("\n").append(useragent).append("\n").append(contenttype).append("\n").append(contentLength).append("\n").append(currentdate).append("\n").append(connection).append("\n").append(host).append("\n").append(inputtext);
			   streamOut.writeUTF(sb.toString()); //Writes the appended string to the output stream
			   streamOut.flush(); //Buffered output bytes are written to the outputStream
			   println("String sent by Coordinator to the clients: "+inputtext);
			   input.setText("");
			   state="WAIT"; // Coordinator is in WAIT Phase as it is waiting for the response from the clients(COMMIT or ABORT)
			   println("----COORDINATOR IN WAIT PHASE----");
			   thread = new Thread(coord); //Thread for timer
			   thread.start(); //calls the start function for the timer thread
			   			  
		   }
		   if (sb.toString().length()==0)//Checks if the string is not appended In the HTTP POST Format(.GLOBALCOMMIT, .GLOBALABORT, .logoff)
		   {
			   //Append the respective string in the HTTP POST Format
			   sb.append(post).append("\n").append(accept).append("\n").append(acceptLanguage).append("\n").append(acceptEncode).append("\n").append(useragent).append("\n").append(contenttype).append("\n").append(contentLength).append("\n").append(currentdate).append("\n").append(connection).append("\n").append(host).append("\n").append(inputtext);
		
			   streamOut.writeUTF(sb.toString()); //Writes the appended string to the output stream
			   streamOut.flush(); //Buffered output bytes are written to the outputStream
			   input.setText(""); //Sets the value of input component to blank after writing to the output stream	
			  

		   }
		  }
		  catch(IOException ioe) //catch block is executed when error occurs 
		  {  println("Sending error: " + ioe.getMessage()); close(); }
	   
	  }
	 
   
   
   /*Function: Reads messsages from Server and then Handles and Coordinator responds according to the responses from the clients which is sent to the Server
    *Input: Messages from Server
    *Output: Display messages on the chat Interface and responds to the server according to responses from the Clients */
   public void handle(String msg)
   {  
	if (msg.contains("has logged off"))// Checks if the Coordinator is logging off
			{
		println(msg); //print the log off message in the Coordinator GUI
			}
	else
	{
	   StringTokenizer st=new StringTokenizer(msg,"@");//TOkenizes the string from the Client
	   clientResponses.put(st.nextToken(), st.nextToken()); //stores the name and its response in a hashMap
	   
	   if (clientResponses.size()==3) //Checks if all the clients has responded with their responses
	   {
		   for (String value : clientResponses.values()) {

			if (value.equalsIgnoreCase(".PreCommit")) // checks the number of client who have responded with "Commit"
			{
				commitCount++; //increment the count if the client responded with "Commit"
			}
			if (value.equalsIgnoreCase(".Abort")) // checks the number of client who have responded with "Commit"
			{
				abortCount++; //increment the count if the client responded with "Commit"
			}
			else if (value.equalsIgnoreCase(".ACK")) // checks the number of client who have responded with "Commit"
			{
				ackCount++; //increment the count if the client responded with "Commit"
			}
			
		   }
		   
		  if (commitCount==3) // if the Commit count is equal to the number of clients then Coordinator initiates Global Commit
			 {
			  println("All three Clients rersponded with PREPARE COMMIT.");
			  println("Sending Acknowledgement to the participants");
			  input.setText(".ACK"); //Set the text value to GLOBALCOMMIT
				state = "PRECOMMIT"; // Set the state value to GLOBALCOMMIT
				try {
					timerDelay(); // Delay the time to send the Coord response to the server(Used in Coord Crash scenario)
					send(); // call send function to send the GLOBALCOMMIT response to the server
					} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
			 }
		  
		  else if(ackCount==3)
		  {
			  println("Initiating GLOBAL COMMIT");
			  
				input.setText(".GLOBALCOMMIT"); //Set the text value to GLOBALCOMMIT
				state = "GLOBALCOMMIT"; // Set the state value to GLOBALCOMMIT
				try {
					timerDelay(); // Delay the time to send the Coord response to the server(Used in Coord Crash scenario)
					send(); // call send function to send the GLOBALCOMMIT response to the server
					} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
			  
		  }
		  else if (abortCount>0) //if the commit count does not equal the number of clients then one or more client responded as GLOBALABORT
		  {
			  println("One or more Clients Aborted.");
			  println("Initiating GLOBAL ABORT");
				input.setText(".GLOBALABORT"); // Text is set to GLOBAL ABORT if one or more client respond with abort
				state = "GLOBALABORT"; // set the state to GLOBALABORT 
				try {
					timerDelay();// Delay the time to send the Coord response to the server(Used in Coord Crash scenario)
					send(); // call send function to send the GLOBALCOMMIT response to the server
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		  }
		  
		  clientResponses.clear();
		  commitCount=0;
		  abortCount=0;
		  ackCount=0;
		  
		}
	}
	}
   
   
   
  /*Function: Creates a DataOutputStream for writing messages to output stream */ 
   public void open()
   {  try
      {  streamOut = new DataOutputStream(socket.getOutputStream()); //opens the output stream for the current client
       if(input.getText().contains(".CoordinatorConnect")) //Checks if the Coordinator is connecting
       {
    	   String inputtext=null;
    	   inputtext=input.getText();
    	   streamOut.writeUTF(inputtext); //Writes the appended string to the output stream
    	   streamOut.flush(); //Buffered output bytes are written to the outputStream
    	   input.setText("");
       }
        client = new ClientCoordinatorThread(this,socket); //call to the Coordiantor Thread which listens to and handles the message received from the server
       
      }
      catch(IOException ioe) //catch block is executed when error occurs 
      {  println("Error opening output stream: " + ioe); } 
   }
  
     
   /*Function:Closes the InputStream , OutputStream and Socket*/
   //Close Function is called when the Server is stopped or when unexpected error occurs.   
   public static void close()
   {  try
      {  if (streamOut != null)  streamOut.close();
         if (socket    != null) {  socket.close();}
       }
      catch(IOException ioe)
      {  println("Error closing ..."); }
      client.close(); 
      //client.stopp();
   }
  
  //Function : Displays messages on the chat interface 
   public static void println(String msg)
   {  display.append(msg + "\n"); 
	
   }
  //Function: gets the server and port number where the server is connected
   public void getParameters()
   {  serverName = "localhost";
   	  serverPort = 8080; }
	}