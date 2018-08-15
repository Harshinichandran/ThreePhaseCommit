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

/*Class : Contains chat interface in which messages are sent to and received from the server */
public class ClientApplet extends Applet // 
{ 
	
   private Socket socket = null; //Object for Socket class
   private DataOutputStream streamOut = null; //Object for DataOutputStream
   private ClientThread client    = null;  //Object for the ClientThread which Listens to and handles the message received from the server

   private boolean flag=false; 
   /*---------Variables for the ClientGUI-----------*/
   private TextArea  display = new TextArea(40,40); //Variables initialized for the chat interface
   private TextField input   = new TextField();
   private Button    send    = new Button("Send"), connect = new Button("Connect"),
                     quit    = new Button("Log Off"), commit= new Button("PRECOMMIT"), abort=new Button("ABORT"), ack=new Button("ACK");
   /*------------------------------------------------------*/
   /*---------Variables to Connect to the Server-----------*/
   private String    serverName = null;
   private int       serverPort = 0;
   /*------------------------------------------------------*/
   public static HashMap<String,String> statusResponses=new HashMap<String,String>(); 
  //Variables initialized for implementing timer 
   private volatile long startTimer = 0;
   
	private String state=null;
	private String statusSend = null;
	private String statusCheck=null;
	private String clientNameStatus= null; 
	//String to potentiall commit
	
	/*----------------For Commit log----------*/
	 BufferedWriter bw = null; 
	 FileWriter fw = null;
	 /*----------------------------------------*/

	//Variables used for storing the successfully registered Client name
	private String potentialName=null; 
	private String actualName=null;
	private String stringReceived=null;
	private String stringToCommit=null; 
	private String FileName=null;
	
/*Function: Called by the browser or applet viewer to display the chat interface
 * Output: A chat interface where the client operations take place  */
public void init()
   {  Panel keys = new Panel(); keys.setLayout(new GridLayout(1,2)); //Container class which provides the space in which other components are added
   keys.add(commit);keys.add(abort);  
   keys.add(quit); keys.add(connect); keys.add(ack); //Log off and Connect Button are added
   		
      Panel south = new Panel(); south.setLayout(new BorderLayout()); // constructs new border Layout
      south.add("West", keys); south.add("Center", input);  south.add("East", send); // positions the buttons and text field inside the Layout
      Label title = new Label("Chat Room System", Label.CENTER); //label for the application
      title.setFont(new Font("Helvetica", Font.BOLD, 14)); // Font type
      setLayout(new BorderLayout());//This method changes layout-related information
      add("North", title); add("Center", display);  add("South",  south);
      quit.	setEnabled(false); //the Log off button is disabled before connection to the server 
      send.	setEnabled(false); //the Send button is disabled before connection to the server 
      commit.setEnabled(false);
      abort.setEnabled(false);
      ack.setEnabled(false);
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
    	  connect(serverName, serverPort); //Connects to the server 'localhost' in port '8080'
    	  try {
			registername(); //Registers name for the client at the server immediately after they are connected
		} catch (IOException e1) { //Catch Block executed when error occurs
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	 
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

	  else if (e.target == commit) // when commit button is clicked
	  {
		  try {
			  input.setText(".PreCommit"); // sets the value of the Commit button
			  enterReadyCommit(); //Call the function to assign Client to READY STATE
			  send(); //sends messages enetered in the text Field to the server
		  }catch (IOException e1) {
			  e1.printStackTrace();
		  }
		  
	  }
	   
	  else if (e.target == ack) // when ack button is clicked
	  {
		  try {
			  input.setText(".ACK"); // sets the value of the ack button
			   //Call the function to assign Client to PRECOMMIT STATE
			  send(); //sends messages enetered in the text Field to the server
		  }catch (IOException e1) {
			  e1.printStackTrace();
		  }
		  
	  }
	  else if (e.target == abort) //When abort button is clicked
	  {
		  try {
			  input.setText(".Abort"); // sets the value of the Abort button
			  enterReadyAbort(); //Call the function to assign Client to READY STATE
			  send(); //sends messages enetered in the text Field to the server
		  }catch (IOException e1) {
			  e1.printStackTrace();
		  }
		  
	  }
   
      return true; // returns true if any of the events are triggered
   }
  
   
   
  /*Function: Establishes connection with the server and is called when the connect button is clicked
   * Input: Server name(localhost) and port number(8080)
   * Output: Connects to the server using the given server name and port */
   public void connect(String serverName, int serverPort)
   {  println("Establishing connection. Please wait ...");
      try
      {  socket = new Socket(serverName, serverPort); //ServerName and port number stored in the object of socket
         println("Connected: " + socket);
         open(); //Function which opens input and output stream
         send.setEnabled(true); //send button enabled for client to start sending messages
         connect.setEnabled(false); //connect button is disabled as connection is already established
         quit.setEnabled(true); //Log off button enabled for client to leave the chat room
      }
      catch(UnknownHostException uhe) //catch block executed when the connection to server fails and other unexpected errors
      {  println("Host unknown: " + uhe.getMessage()); }
      catch(IOException ioe)
      {  println("Unexpected exception: " + ioe.getMessage()); } 
    }
  
   
   /*Function: Provides information in the applet about acceptable name formats and sets flag to TRUE indicating that name registration is yet to be done */
   public void registername() throws IOException
   {
	   println("Enter the name you want to register");
	   println("*----Valid format : Maximum 11 characters ; Starts with alphabet ; Accepts alpha-numeric------*");//Format for registering name; validated at Server using regex.		
	   flag=true;//Flag is true when client isn't registered yet

	}
   
 
    /*Function: Stores the arbitary string from the coordinator and assigns the client to the READY STATE when client clicks commit */
   private void enterReadyCommit() 
   {
	
	 println("------CLIENT IN READY PHASE------");
	commit.setEnabled(false);
	abort.setEnabled(false);
	stringToCommit=stringReceived; //Stores the arbitary string to local variable 
	state = "READY"; // assign state READY for the client once it sends response to the Coordinator
   }
   
   /*Function: This function is called when the coordinator sends a request for acknowledgement and the clients need to respond for ACK */
   private void prepareCommit() 
   {
		println("All the participants responded with PRECOMMIT");
		println("The coordinator has sent 'ACK'");
		println("Please respond by sending acknowledgement to the coordinator");
		 ack.setEnabled(true);
		 commit.setEnabled(false);
		 abort.setEnabled(false);
		 send.setEnabled(false);
		 stringToCommit=stringReceived; //Stores the arbitary string to local variable 
		 state = "PREPARE COMMIT"; // assign state READY for the client once it sends response to the Coordinator
   }
   
   
   /*Function: Store the arbitary string value to null from the coordinator and assigns the client to the READY STATE when client clicks Abort  */
   private void enterReadyAbort()
   {
	println("------CLIENT IN READY PHASE------");
	commit.setEnabled(false);
	abort.setEnabled(false);
	stringToCommit=null;  //  Stores the value of arbitary string to Null
	state = "READY"; // assign state READY for the client once it sends response to the Coordinator
	
   }
   
   /*Function: Save the arbitary string from the coordinator to the file when the state is GLOBALCOMMIT*/
   private void commit() throws IOException
   {
	  
	   state="GLOBALCOMMIT"; // assign state GLOBALCOMMIT  once the Coordinator initiates a global commit
	     
	   StringTokenizer st=new StringTokenizer(stringReceived,":");// Tokenize the string from the local variable where the arbitary string is stored
	    st.nextToken();
	    stringToCommit = st.nextToken(); // gets the arbitary string from the message sent by the coordinator
	       
	   
	    File file =new File(actualName+".txt"); // Creates a text file in the name of the Client
	    if (file.exists()) //Checks if file already exists in that name
	    { 
	    	fw = new FileWriter(file.getAbsolutePath(), true); // if file exists, it gets  text file from the path 
	    	bw = new BufferedWriter(fw); //Creates a buffered character-output stream for the Filewriter
	    	bw.append(stringToCommit); // appends the arbitary string from the coordinator to the file
	    	println("String "+stringToCommit+" is committed to the log fie");
	    	println("---TRANSACTION COMPLETE---");
	    	println("\n");
	    	println("---CLIENT IN INIT PHASE---");
	    	bw.write("\r\n"); 
	    	bw.close();
	    	stringToCommit=null; // assign the value of the arbitary string to null after it is saved to file
	    	
	    } 
	    else { // if the text file does not exists already
	    	    if (file.createNewFile()) // New file is created
	    	    {
	    	    	     
	    	    	try {
	    	    		fw = new FileWriter(file.getAbsolutePath(), true); //gets the Text file from its absolute path
	    	    		
	    	    	} 
	    	    	catch (IOException e1) 
	    	    	{
	    	    		// TODO Auto-generated catch block
	    	    		e1.printStackTrace();
	    	    	}
	    	    	bw = new BufferedWriter(fw); //Creates a buffered character-output stream for the Filewriter
	    	    	bw.append(stringToCommit); //appends the arbitary string from the coordinator to the file
	    	    	println("String "+stringToCommit+" is committed to the log fie");
	    	    	println("---TRANSACTION COMPLETE---");
	    	    	println("\n");
	    	    	println("---CLIENT IN INIT PHASE---");
	    	    	bw.write("\r\n");     		
	    	    	bw.close();
	    	    	stringToCommit=null; // assign the value of the arbitary string to null after it is saved to file
	    	    }
	    }
	  
   }
   
   /*Function: Displays messages and does not save the arbitary string when the state is GLOBALABORT*/
   private void abort()
   {
	   state="GLOBALABORT"; // assign state GLOBALABORT  once the Coordinator initiates a global abort
	  
	   commit.setEnabled(false);
	   abort.setEnabled(false);
	   println("String not saved to the Log File");
	   println("---TRANSACTION COMPLETE---");
	   println("\n");
	   println("---CLIENT IN INIT PHASE---");
   }
   
   
   
   /*Function : Messages from the client is sent to the Server in POST method Format*/
   private void send() throws IOException
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
	   
	   if (flag==true) //flag is true when client hasn't registered the name 
	  	{ 
		   try {		   
			   String inputtext="Name:"+input.getText(); //appends "Name:" along with the input(Name which client wants to register)
			   potentialName=input.getText(); //stores the client name in a potentialName string (used for disabling functions after client log off)
			    
			   /*appends the Name which the client want to register in the POST method format*/
			   sb.append(post).append("\n").append(accept).append("\n").append(acceptLanguage).append("\n").append(acceptEncode).append("\n").append(useragent).append("\n").append(contenttype).append("\n").append(contentLength).append("\n").append(currentdate).append("\n").append(connection).append("\n").append(host).append("\n").append(inputtext);
			   
			  
			
			  		streamOut.writeUTF(sb.toString()); //Writes the appended string to the output stream
			  		println("Given Name:"+input.getText()); //registered name is displayes on the interface
			  		streamOut.flush(); //Buffered output bytes are written to the outputStream
			  		input.setText(""); //Sets the value of input component to blank after writing to the output stream	
		  		}
			  		
		
		 catch (IOException e)  //catch block is executed when error occurs 
		   {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} 
	  	}
	  else if(flag==false) //flag is set to false when the user sends messages to other connected clients after sucessful registration
	  {
		  try
		  {  
		   String inputtext=null;//Variables initialized to send messages to Server
		  
		
		   if(input.getText().equals(".logoff")) //checks if the client is logging off from the chat room
		   {
			 inputtext=input.getText(); //only ".logoff" is sent as a message to the Server 
			 sb.append(post).append("\n").append(accept).append("\n").append(acceptLanguage).append("\n").append(acceptEncode).append("\n").append(useragent).append("\n").append(contenttype).append("\n").append(contentLength).append("\n").append(currentdate).append("\n").append(connection).append("\n").append(host).append("\n").append(inputtext);
				
			   streamOut.writeUTF(sb.toString()); //Writes the appended string to the output stream
			   streamOut.flush(); //Buffered output bytes are written to the outputStream
			   input.setText(""); //Sets the value of input component to blank after writing to the output stream	

		   }
		   else if(input.getText().equals(".PreCommit")) // when the client clicks the commit buttton this part of code is executed
		   {
			
			 inputtext= input.getText(); 
			 sb.append(post).append("\n").append(accept).append("\n").append(acceptLanguage).append("\n").append(acceptEncode).append("\n").append(useragent).append("\n").append(contenttype).append("\n").append(contentLength).append("\n").append(currentdate).append("\n").append(connection).append("\n").append(host).append("\n").append(inputtext);
				
			   streamOut.writeUTF(sb.toString()); //Writes the appended string to the output stream
			   streamOut.flush(); //Buffered output bytes are written to the outputStream
			   input.setText(""); //Sets the value of input component to blank after writing to the output stream	
			   timer();
		   }
		   else if(input.getText().equals(".ACK"))  // when the client clicks Acknowledgement button, this portion of code is executed
		   {
			
			 inputtext= input.getText(); 
			 sb.append(post).append("\n").append(accept).append("\n").append(acceptLanguage).append("\n").append(acceptEncode).append("\n").append(useragent).append("\n").append(contenttype).append("\n").append(contentLength).append("\n").append(currentdate).append("\n").append(connection).append("\n").append(host).append("\n").append(inputtext);
				
			   streamOut.writeUTF(sb.toString()); //Writes the appended string to the output stream
			   streamOut.flush(); //Buffered output bytes are written to the outputStream
			   input.setText(""); //Sets the value of input component to blank after writing to the output stream
			   ack.setEnabled(false);
			   timer_ack();
		   }
		   else if(input.getText().equals(".Abort"))  //when client clicks Abort button, this portion of code is executed
		   {
			  
			 inputtext= input.getText(); 
			 sb.append(post).append("\n").append(accept).append("\n").append(acceptLanguage).append("\n").append(acceptEncode).append("\n").append(useragent).append("\n").append(contenttype).append("\n").append(contentLength).append("\n").append(currentdate).append("\n").append(connection).append("\n").append(host).append("\n").append(inputtext);
				
			   streamOut.writeUTF(sb.toString()); //Writes the appended string to the output stream
			   streamOut.flush(); //Buffered output bytes are written to the outputStream
			   input.setText(""); //Sets the value of input component to blank after writing to the output stream	
			   timer();
			// println("------CLIENT IN READY PHASE------");
		   }
		   else if(input.getText().equalsIgnoreCase(".STATUS")) //executed when the coordinator crashes and client communicates with each other.
		   {
		   inputtext = input.getText();
		   sb.append(post).append("\n").append(accept).append("\n").append(acceptLanguage).append("\n").append(acceptEncode).append("\n").append(useragent).append("\n").append(contenttype).append("\n").append(contentLength).append("\n").append(currentdate).append("\n").append(connection).append("\n").append(host).append("\n").append(inputtext);
			
		   streamOut.writeUTF(sb.toString()); //Writes the appended string to the output stream
		   streamOut.flush(); //Buffered output bytes are written to the outputStream
		   input.setText(""); //Sets the value of input component to blank after writing to the output stream	

		   }
		  // println(inputtext);
		    /*appends the Name which the client want to register in the POST method format*/
		   else if(input.getText().contains("STATUS@")) 
		   {
	           inputtext=statusSend; 
	           sb.append(post).append("\n").append(accept).append("\n").append(acceptLanguage).append("\n").append(acceptEncode).append("\n").append(useragent).append("\n").append(contenttype).append("\n").append(contentLength).append("\n").append(currentdate).append("\n").append(connection).append("\n").append(host).append("\n").append(inputtext);
				
			   streamOut.writeUTF(sb.toString()); //Writes the appended string to the output stream
			   streamOut.flush(); //Buffered output bytes are written to the outputStream
			   input.setText("");
		   }
		   
		  }
		  catch(IOException ioe) //catch block is executed when error occurs 
		  {  println("Sending error: " + ioe.getMessage()); close(); }
	   
	  }
	 }
   
   
   /*Function: Reads messages from Server and then Handles and displays messages to the chat interface according to the given message from Server
    *Input: Messages from Server
    *Output: Display messages on the chat Interface according to messages from Server */
   public synchronized void handle(String msg) throws IOException
   {  
	if (msg.contains("has logged off..")) //Checks if a client has logged off 
      {  println(msg); //prints on the chat interface to all the connected clients when a client logs off
      	 
      	if(msg.contains(actualName)) //checks if the name from the Server and in Client are the same when the client logs off
      	{	
      	println("Close the Applet Window to Exit..");
      	close();
      	quit.	setEnabled(false); //Disable all the buttons and functionalities after a client logs off 
      	send.	setEnabled(false);
      	connect.setEnabled(false);
      	}
      }
   
   	else if(msg.contains("successfully registered..")) //Checks if a client is successfully registered
		{
			flag=false; //flag is set to false, as the Name for the client is successfully registered
			actualName=potentialName; // sets the client name into actual name to disable buttons and functionalities when a client logs off
			println("Name Registration Successful");
			File file=new File(actualName+".txt");
			//file.toString()=;
						 String line =null;
		
						//
			if(file.exists())// if the file already exists then display the contents of the file
			{
				FileReader fr = new FileReader(file.getAbsolutePath());
				BufferedReader br1 = new BufferedReader(fr);

			println("--------Log File Contents--------");// Log file contents are displayed
			line = br1.readLine();   // read line from the log file   	     	 
        	 
			
     		 
     			 while (line!=null)// Read until EOF 
     			 {
     				 println(line); //The log details from the ChatLog.text file is displayed on the server
     				 line=br1.readLine();
     			 }
     			 
     			println("----------------------------------");
     			println("\r\n");
     			println("------CLIENT IN INIT PHASE------"); // displays that client is in init phase
     			 br1.close();
     			 state="INIT";
     		  } 
			else
			{
				println("--------Log File Contents--------"); // if it is a new user then there is not any exsting information to display
				println("New User. File has no content. ");
				println("----------------------------------");
				println("\r\n");
     			println("------CLIENT IN INIT PHASE------"); 
     			state="INIT";
			}
     		 
			
			input.setEnabled(false);
			send.setEnabled(false);
			println("Welcome to the 3PC Application. You will receive a vote Request now...");
		}
		else if(msg.contains("Name is not in valid format.!") || msg.contains("Name already exists.!") ) //checks if the validation when client registers name has failed
		{
			System.out.println("Registration Unsuccessful"); //print the messages if the validation fails
			println(msg+" Please enter the name again:");
		}
		else if(msg.contains(".GLOBALCOMMIT")) // if the message contains global commit, call  the commit function
		{
			try {
				   println("Coordinator Initiated GLOBAL_COMMIT");
				commit();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(msg.contains(".GLOBALABORT")) // if the message contains global abort, call  the abort function
		{
			println("Coordinator Initiated GLOBAL_ABORT");
			abort();
		}
	
		else if(msg.contains(".STATUS")) // if the message contains status, each clients checks for the status from each other clients
		{
			statusSend = "STATUS@"+state;
			input.setText("STATUS@"+state);
			send();
		}
		else if(msg.contains(".ACK")) //if the message contains ACk, then the prepare to commit function is called
		{
			prepareCommit();
			//send();
		}
		else if(msg.contains("STATUS@")) //Checks for status from every client and displays it on the console
		{
			 StringTokenizer st=new StringTokenizer(msg,"@");
			    clientNameStatus =st.nextToken();
			    st.nextToken();
			    statusCheck = st.nextToken(); // displays the status of every client
			    println("Client "+clientNameStatus+" responded with status:"+statusCheck);
			    statusResponses.put(clientNameStatus,statusCheck);
			    statusCheck();
		}
      else 
    	  {
    	 
    	 // println("VOTE_REQUEST. Please press 'COMMIT' or ABORT to proceed : ");
    	  
    	  stringReceived=msg;
    	  state="INIT";
    	  println(msg);
    	  commit.setEnabled(true);
    	  abort.setEnabled(true);
    	  // print if it contains any other message
    	  }
	}
  
   public void statusCheck() throws IOException // checks for status from every client in the event of coordinate crash
   {
	   int readyCount=0; // variable initiation
	   int prepareCommitCount=0;
	 
	if (statusResponses.size()==2) // one client checks for the response of the other two clients
	{
		for (String value : statusResponses.values()) 
		{
			if(value.equals("INIT")||value.equals("GLOBALABORT"))// if one the status is INIT or GLOBAL ABORT then the GLOBAL ABORT is initiated locally
			   {
		   			println("One or more client's status was in INIT or GLOBALABORT state");
		   			println("----Transaction Aborted. Arbitary string not saved to the file----");
		   			abort(); // abort function to initiate global abort locally
		   			break;
		   			
			   }
			else if(value.equals("GLOBALCOMMIT")) // if one client has GLOBAL COMMIT has response then the global commit is initiated locally
			{
		   println("One or more client's status was GLOBALCOMMIT");
  			println("----Transaction Commited. Arbitary string saved to the file----");
  			commit();	// commit function is called when global commit needs to be initiated
  			break;
			}
			else if(value.equals("READY")) // if one of the client is in Ready state, then ready count is increased
			{
		  readyCount++;
			}
			else if(value.equals("PREPARE COMMIT")) // if one of the clients is in prepare commit, then the prepare commit count is increased
			{
		  prepareCommitCount++;
			}
					
	   
		}  
	   
	   if(readyCount==2 && state.equals("READY")) //if the ready count equals 2 and the state equals ready, then the global abort is initiated
	   {
		   println("All the clients are in READY state and COORDINATOR did not respond");
		   println("So, the client goes into ABORT State");
		   abort();
	   }
	   if( prepareCommitCount==2 && state.equals("PREPARE COMMIT"))//if the preparedCommitCount is two and state is prepare commit, then the global commit is intiated 
	   {
		   println("All the clients are in PREPARE COMMIT state and COORDINATOR did not respond");
		   println("So, the client goes into COMMIT State");
		   commit();
	   }
	   
	   readyCount=0;
	   prepareCommitCount=0;
	   statusResponses.clear(); // hash map is cleared here
	}
   }
   
   
  public void timer() //timer is intiated fro the coordinator to respond within a finite amount of time
   {
	    
	   println("Time left for Coordinator to send response to clients : 45 seconds");
	   startTimer = System.currentTimeMillis(); //Returns the current time in milliseconds only for the first message
	   
	   long end = startTimer+45000; // timer is initiated for 45 seconds
	  
	   while(System.currentTimeMillis() < end)  //when the timer is less than the current time, then wait for response from the coordinator
	   {
		   
	     // do something
	     // pause to avoid churning
		 if (state.equals("INIT")||state.equals("READY")) { //when the timer is less than the current time, then wait for response from the coordinator
	     try{
	    	 //println("Wait state"+state);
	    	 Thread.sleep( 100 );
	     	}catch(Exception e){
	    	 
	    	 
	     	}
		 }
		 else if(state.equals("GLOBALCOMMIT")||state.equals("GLOBALABORT")||state.equals("PREPARE COMMIT"))
		 {
			 end = startTimer;
		 }
		
		 }
		  
	   
	     if (!state.equals("GLOBALCOMMIT")&&!state.equals("GLOBALABORT")&&!state.equals("PREPARE COMMIT"))
	     {
	    	 println("COORDINATOR DID NOT REPLY WITHIN A FINITE TIME");
	    	 println("Requesting status of other clients to determine the course of action");
				input.setText(".STATUS");
				try {
					send();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	     }
	   	   
   }
  
  public void timer_ack()
  {
	    
	   println("Time left for Coordinator to send response to clients after sending acknowledgement : 45 seconds");
	   startTimer = System.currentTimeMillis(); //Returns the current time in milliseconds only for the first message
	   
	   long end = startTimer+45000;
	  
	   while(System.currentTimeMillis() < end) //Returns the current time in milliseconds only for the first message 
	   {
		   
	     // do something
	     // pause to avoid churning
		 if (state.equals("PREPARE COMMIT")) { //Returns the current time in milliseconds only for the first message
	     try{
	    	 //println("Wait state"+state);
	    	 Thread.sleep( 100 ); //Returns the current time in milliseconds only for the first message
	     	}catch(Exception e){ 
	    	 
	    	 
	     	}
		 }
		 else if(state.equals("GLOBALCOMMIT")||state.equals("GLOBALABORT"))//Returns the current time in milliseconds only for the first message
		 {
			 end = startTimer;
		 }
		
		 }
		  
	   
	     if (!state.equals("GLOBALCOMMIT")&&!state.equals("GLOBALABORT")) //Returns the current time in milliseconds only for the first message
	     {
	    	 println("COORDINATOR DID NOT REPLY WITHIN A FINITE TIME FOR THE ACKNOWLEDGEMENT");
	    	 println("Requesting status of other clients to determine the course of action");
				input.setText(".STATUS"); //Returns the current time in milliseconds only for the first message
				try {
					send();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	     }
	   	   
  }
  /*Function: Creates a DataOutputStream for writing messages to output stream */ 
   public void open()
   {  try
      {  streamOut = new DataOutputStream(socket.getOutputStream()); //opens the output stream for the current client
         client = new ClientThread(this,socket); //call to the ClientThread which listens to and handles the message received from the server
      }
      catch(IOException ioe) //catch block is executed when error occurs 
      {  println("Error opening output stream: " + ioe); } 
   }
  
     
   /*Function:Closes the InputStream , OutputStream and Socket*/
   //Close Function is called when the Server is stopped or when unexpected error occurs.   
   public void close()
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
   private void println(String msg)
   {  display.append(msg + "\n"); 
	
   }
  //Function: gets the server and port number where the server is connected
   public void getParameters()
   {  serverName = "localhost";
   	  serverPort = 8080; }
	}