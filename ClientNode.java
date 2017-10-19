import java.io.*;
import java.awt.*;
import java.net.*;
import java.sql.*;
import javax.imageio.*;
import javax.swing.filechooser.*;
import java.awt.image.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
class User																							//this class keeps the user data as an object
{
	String userName;
	String userNo;
	String userId;
	String userPass;
	User(String userName,String userNo,String userId,String userPass)					//constructor with 4 parameters used in registeration 
	{
		this.userName=userName;
		this.userNo=userNo;
		this.userId=userId;
		this.userPass=userPass;
	}
	User(String userNo,String userPass)											//constructor with 2 parameter used in login
	{
		this.userNo=userNo;
		this.userPass=userPass;
	}
}
class WelcomeScreen extends Frame												// the first window welcome windows asks for login or register
{
	public void userWelcomeScreen()
	{
		setTitle("ChatBox");
		setSize(400,200);
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
		Panel header=new Panel();
		header.setBackground(Color.YELLOW);
		header.add(new Label("Welcome to ChatBox.Connect with your loved ones"));
		Panel body=new Panel();
		body.setLayout(new GridLayout(2,2));
		body.setBackground(Color.GREEN);
		Label log=new Label("New user... register here");
		Label reg=new Label("Existing user?..Log in here");
		Button newUser=new Button("Log In");
		Button register=new Button("Register");
		register.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Register().userRegister();														// calling registration panel
				dispose();
			}
		});
		newUser.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Login().userLogin();														// calling login panel
				dispose();
			}
		});
		body.add(log);
		body.add(reg);
		body.add(register);
		body.add(newUser);
		add(header);
		add(Box.createRigidArea(new Dimension(0,30)));
		add(body);
		add(Box.createRigidArea(new Dimension(0,30)));
	}
}
class Register extends Frame
{
	public void userRegister()
	{
		setTitle("Register");
		setSize(400,300);
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	
		
		Panel header=new Panel();
		header.setBackground(Color.YELLOW);
		header.add(new Label("Please Enter the Valid Details"));
		Panel body=new Panel();
		body.setBackground(Color.GREEN);
		body.setLayout(new GridLayout(4,2));
		final TextField userName=new TextField();
		final TextField userNo=new TextField();
		final TextField userId=new TextField();
		final JPasswordField userPass=new JPasswordField();
		body.add(new Label("User Name"));									// getting 4 details for registration of user
		body.add(userName);													//creating the user object below
		body.add(new Label("Contact no."));									// triming the data so that no underscores or spaces filled in db
		body.add(userNo);
		body.add(new Label("User Email-Id"));
		body.add(userId);
		body.add(new Label("Password"));
		body.add(userPass);
		Button register=new Button("Register");
		
		/**********************
		in registration process we send five stings to server first is for the action type whether it is for registration or login
		as the login needs 4 values and login needs only two values. It is necessary to protect the confliction between
		reading streams
		***********************/
		
		register.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if((userName.getText().length()<1)||(userNo.getText().length()<10)||(userPass.getText().length()<1))
				{
					String re="invalid Details please check the following:\n contact should be without code \n all fields are manfatory";
					JOptionPane.showMessageDialog(null,re);
				}
				else
				{
					try
					{
						String psw=new String(userPass.getPassword());
						ClientNode.writeToServer.writeUTF("register");								
						ClientNode.writeToServer.writeUTF(userName.getText().trim());
						ClientNode.writeToServer.writeUTF(userNo.getText().trim());
						ClientNode.writeToServer.writeUTF(userId.getText().trim());
						ClientNode.writeToServer.writeUTF(psw);
						final User client=new User(userName.getText().trim(),userNo.getText().trim(),userId.getText().trim(),psw);
						new Varification().userVarification(client,"register");									//getting varification via registration mode
					}
					catch(Exception ex)
					{
						System.out.println(ex);
					}
					dispose();
				}
			}
		});
		add(header);
		add(Box.createRigidArea(new Dimension(0,20)));
		add(body);
		add(Box.createRigidArea(new Dimension(0,30)));
		add(register);
	}
}
class Login extends Frame
{
	User client;
	public void userLogin()
	{
		setTitle("Login");
		setSize(400,200);
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
		Panel header=new Panel();
		header.setBackground(Color.YELLOW);
		header.add(new Label("Please Enter the Valid Details"));
		Panel body=new Panel();
		body.setBackground(Color.GREEN);
		body.setLayout(new GridLayout(2,2));
		final TextField userNo=new TextField();
		final JPasswordField userPass=new JPasswordField();
		body.add(new Label("Contact no."));
		body.add(userNo);
		body.add(new Label("Password"));
		body.add(userPass);
		Button login=new Button("Login");
		
		login.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{	
					//char[] ps=userPass.getPassword();
					String psw=new String(userPass.getPassword());
					ClientNode.writeToServer.writeUTF("login");
					ClientNode.writeToServer.writeUTF(userNo.getText().trim());
					ClientNode.writeToServer.writeUTF(psw.trim());
					client=new User(userNo.getText().trim(),psw);
					System.out.println(psw);
					new Varification().userVarification(client,"login");
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
					dispose();
			}
		});
		add(header);
		add(Box.createRigidArea(new Dimension(0,20)));
		add(body);
		add(Box.createRigidArea(new Dimension(0,30)));
		add(login);
	}
}

/***************** 
varification is going to be done by two methods
1. if server reads the registration action it simply add new values in db and if write is succesfull it return the allow string.
2. if server reads the login request it check for the creadentials and if it is correct than it allows and deny if incorrect.
the varification in case of login requests for the extra string the name of user for further functions
******************/

class Varification extends Frame
{
	String permission;
	String userName;
	String msg;
	public String chatWith;
	int i=0;
	Boolean flag=false;
	int portRecieve;
	public void userVarification(final User client,String action)
	{
		setTitle("Varifying");
		setSize(400,200);
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
		Panel header=new Panel();
		header.setBackground(Color.YELLOW);
		header.add(new Label("Please wait while we are varifying your details"));
		
/*******************
the thread we created to recieve the upcoming messages from the server the server sends the messsage which is proceeded 
with name of sender. the reciever break the message in two parts
the first part is for the sender name and the second part is of message
if there is an active chat of that window is currently opened the mesage is written in that field 
otherwise it will create the new chat window
*******************/


		Thread recieveThread=new Thread()
		{
			public void run()
			{
				while(true)
				{
					try
					{
						msg=new String();
						msg=ClientNode.readFromServer.readUTF();
						StringTokenizer stz=new StringTokenizer(msg);
						chatWith=stz.nextToken();
						int i=0;
						for(i=0;i<ClientNode.contacts.size();i++)
						{
							if(ClientNode.contacts.elementAt(i).equals(chatWith))			// checking for user window currently opened or not
							{
								flag=true;
								break;
							}
						}
						msg=null;
						msg=stz.nextToken();
						if(msg.equals("file"))								// invoking image reading function
						{
							try
							{
								try {
									portRecieve=Integer.valueOf(stz.nextToken());
									new GreetingServer(portRecieve,chatWith);
									
								} catch (IOException exe) {
									// TODO Auto-generated catch block
									exe.printStackTrace();
								}
								finally {
								}
								
								//////////////////////////////////////////////////////////////////////
							}
							catch(Exception ex)
							{}
						}
						else										// this is simple text message
						{
							while(stz.hasMoreTokens())										// getting the message recieved
							{
								msg=msg+" "+stz.nextToken();
							}
							if(chatWith.equals("server"))									// checking whether the sender is server so than no need to add it in contact
							{
								JOptionPane.showMessageDialog(null,msg);
							}
							else
							{
								if(flag==true)									
								{
									TextArea t=(TextArea)ClientNode.textFields.elementAt(i);
									t.append(chatWith+" says: "+msg+"\n");
									flag=false;
								}
								else
								{
									flag=false;
									ChatWindow a=new ChatWindow();
									a.userChatWindow(client,chatWith);
									a.textArea.append(chatWith+" says: "+msg+"\n");
								}
							}
						}
					}
					catch(Exception ex)
					{
						System.out.println(ex);
					}
				}
			}
		};
		try
		{
			permission=ClientNode.readFromServer.readUTF();
			if(permission.equals("deny"))
			{
				String st=ClientNode.readFromServer.readUTF();												// reading the reason for access deny
				JOptionPane.showMessageDialog(null,"permission Denied :"+st);
				new WelcomeScreen().userWelcomeScreen();
				dispose();
			}
			else
			{
				JOptionPane.showMessageDialog(null,"permission granted");
				recieveThread.start();
				if(action.equals("register"))
				{
					try												//creating table of user if not exists if exist it will only throw exception and we will deal with it
					{
						String st=new String("CREATE TABLE "+client.userName+"(contact varchar(20),logfile varchar(20));");
						ClientNode.st.executeUpdate(st);
					}
					catch(Exception ex)
					{
						System.out.println(ex);
					}
					new ChatMenu().userChatMenu(client);
				}
				else
				{
					userName=ClientNode.readFromServer.readUTF();
					client.userName=userName;
					try												//creating table of user if not exists if exist it will only throw exception and we will deal with it
					{
						String st=new String("CREATE TABLE "+client.userName+"(contact varchar(20),logfile varchar(20));");
						ClientNode.st.executeUpdate(st);
					}
					catch(Exception ex)
					{
						System.out.println(ex);
					}
					new ChatMenu().userChatMenu(client);
				}
				dispose();
				
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
		add(header);
	}
}
class ChatMenu extends Frame
{
	public void userChatMenu(final User user)
	{
		setTitle("Chat Menu");
		setSize(500,300);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		Panel header=new Panel();
	//	header.setLayout(new FlowLayout(FlowLayout.LEFT));
		header.setBackground(Color.YELLOW);
		header.add(new Label("hey "+user.userName));
		header.add(new Label("Connect to a friend"));
		Panel body=new Panel();
		body.setLayout(new GridLayout(3,2,5,5));
		body.setBackground(Color.GREEN);
		body.add(new Label("Connect with your contacts"));
		body.add(new Label("Connect to new one"));
		final Choice chatWithUsers=new Choice();
		Button start1=new Button("Start");
		start1.setEnabled(false);
		Button start2=new Button("Start");
		Button logout=new Button("logout");
		try															// getting contact list  the database
		{
			ResultSet rs=ClientNode.st.executeQuery("SELECT contact FROM "+user.userName);
			while(rs.next())
			{
				chatWithUsers.add(rs.getString(1));
				start1.setEnabled(true);
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
		body.add(chatWithUsers);
		final TextField chatNewUser=new TextField();
		body.add(chatNewUser);
		body.add(start1);
		body.add(start2);
		Panel bottom=new Panel();
		bottom.add(logout);
		start1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					String xyz=chatWithUsers.getSelectedItem();
					new ChatWindow().userChatWindow(user,xyz);
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
				dispose();
			}
		});
		start2.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					String xyz=new String(chatNewUser.getText().trim());
					new ChatWindow().userChatWindow(user,xyz);
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
				dispose();
			}
		});
		logout.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					ClientNode.writeToServer.writeUTF(user.userName+" LOGOUT "+"sagDSA");
					callMain();
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
				dispose();
			}
		});
		add(header);
		add(Box.createRigidArea(new Dimension(0,70)));
		add(body);
		add(Box.createRigidArea(new Dimension(0,30)));
		add(bottom);
	}
	public void callMain()throws Exception
	{
		String []s=new String[]{"dsf","adsf"};
		ClientNode.main(s);
	}
}
class ChatWindow extends JFrame
{
	Boolean flag=false;
	TextArea textArea;
	TextField textField;
	int portSend=3456;
	int portRecieve;
	public void userChatWindow(final User user,final String chatWith)
	{	
		setTitle(chatWith);
		setSize(600,400);
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
		setLayout(new GridLayout(2,1));
		Panel body=new Panel();
		Button send=new Button("Send");
		Button close=new Button("close");
		Button sendImage=new Button("Send an Image");
		Button clearHistory=new Button("clear chat history");
		textField=new TextField(60);
		textArea=new TextArea(60,40);
		ClientNode.textFields.add(textArea);
		ClientNode.contacts.add(chatWith);
		textArea.setEditable(false);
		body.add(textField);
		body.add(send);
		body.add(close);
		body.add(sendImage);
		body.add(clearHistory);
		
		add(textArea);
		add(body);
		
	/*************************
	checking for the recipient whether exists in user contact list or not if flag = false than not a user
	and we will add him into the contact list if exists we will read its histoy file and set it into
	the textarea as a chat history
	**************************/
	
		try
		{
			
			ResultSet rs=ClientNode.st.executeQuery("SELECT * FROM "+user.userName+";");
			while(rs.next())
			{
				if(rs.getString(1).equals(chatWith))
				{
					flag=true;
					break;
				}
				else
				{
					flag=false;
				}
			}
			if(flag==false)
			{
				ClientNode.st.execute("INSERT INTO "+user.userName+"(contact,logfile) VALUES('"+chatWith+"','"+user.userName+chatWith+"log');");
				FileOutputStream fout=new FileOutputStream(user.userName+chatWith+"log.txt");
				fout.close();
			}
			else
			{
				String history=readLogFile(user.userName+chatWith+"log.txt");
				textArea.append(history);
				flag=false;	
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
		
/*******************
send button will send message to server with the message type of data so that
user can differentiate between message request and a logout request
and append the message in text area
*********************/

		send.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					textArea.append("You:=> "+textField.getText()+"\n");
					ClientNode.writeToServer.writeUTF(chatWith+" "+"data"+" "+textField.getText());
					textField.setText("");
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		
		clearHistory.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					FileOutputStream fout=new FileOutputStream(user.userName+chatWith+"log.txt");
					fout.close();
					textArea.setText("");
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
	
/*************
close button work is to send a logout request to server and read all the content of 
textArea and saves it the chat history ie user log
**************/


		close.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					for(int i=0;i<ClientNode.contacts.size();i++)
                    {
                        if(ClientNode.contacts.elementAt(i).equals(chatWith))
                        {
                            ClientNode.contacts.removeElementAt(i);
                            ClientNode.textFields.removeElementAt(i);
                            break;
                        }
                    }
					writeLogFile(user.userName+chatWith+"log.txt");
					new ChatMenu().userChatMenu(user);
					dispose();
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
				dispose();
			}
		});
		
/*****************************
while sending an image we created a file chooser with specific image file filter
it will send a request to server for checking whether the user is online or not if the user is online
server will send a feedback to client for status
now we check for its size if it is greater than 1 mb than a error will be shown else
	the selected image will be sent to server
********************************/
		sendImage.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					JFileChooser fileChooser = new JFileChooser();
					File selectedFile;
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
					fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Document", "docx"));
					fileChooser.setAcceptAllFileFilterUsed(false);
					JPanel p=new JPanel();
					int result = fileChooser.showOpenDialog(p);
					if (result == JFileChooser.APPROVE_OPTION) 
					{
						selectedFile = fileChooser.getSelectedFile();
						if(selectedFile.length()>1*1024*1024)
						{
							JOptionPane.showMessageDialog(null,"file size Exceeded please choose file less than 1 mb");
						}
						else
						{
							ClientNode.writeToServer.writeUTF(chatWith+" "+"file"+" "+portSend);
							try {
									try {
											
											new GreetingClient(selectedFile,portSend);
											portSend=portSend+1;
										}
										finally {
										}		
								} catch (Exception exe) {
									
									// TODO Auto-generated catch block
									exe.printStackTrace();
								}
						}
					}
					textField.setText("");
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		
	}
	public String readLogFile(String fileName)
	{
		String history=new String();
		try
		{
		//	FileOutputStream fout=new FileOutputStream(chatWith+"log.txt");
			//fout.close();
			FileInputStream fin=new FileInputStream(fileName);
			BufferedInputStream bin=new BufferedInputStream(fin);
			int i;
			while((i=bin.read())!=-1)
				{
					history=history+(char)i;	
				}
			bin.close();
			fin.close();
			flag=false;
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
		return(history);
	}
	public void writeLogFile(String fileName)
	{
		try
		{
		FileInputStream fin=new FileInputStream(fileName);
		BufferedInputStream bin=new BufferedInputStream(fin);
		int i;
		String history=new String();
		while((i=bin.read())!=-1)
			{
				history=history+(char)i;	
			}
		bin.close();
		fin.close();
		flag=false;
		FileOutputStream fout=new FileOutputStream(fileName);
		BufferedOutputStream bout=new BufferedOutputStream(fout);
		String s=textArea.getText();
		s=s+history;
		byte b[]=s.getBytes();
		bout.write(b);
		bout.flush();
		bout.close();
		fout.close();
		}
		catch(Exception ex)
		{}
	}
}

public class ClientNode
{
	public static Socket serverSocket;
	public static DataOutputStream writeToServer;
	public static DataInputStream readFromServer;
	public static Statement st;
	public static BufferedImage bufferedImage;
	public static Vector textFields;
	public static Vector contacts;
	public static void main(String [] args)throws Exception
	{
			textFields=new Vector();
			contacts=new Vector();
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			Connection con=DriverManager.getConnection("Jdbc:Odbc:client"," "," ");
			st=con.createStatement();
			serverSocket=new Socket("localhost",6126);
			writeToServer=new DataOutputStream(serverSocket.getOutputStream());
			readFromServer=new DataInputStream(serverSocket.getInputStream());
			new WelcomeScreen().userWelcomeScreen();
	}
}

class GreetingServer extends Thread
{
       public ServerSocket serverSocket;
      public Socket server;
		public String name;
		public int port;
       public GreetingServer(int port,String name) throws Exception
       {
		   this.port=port;
		  this.name=name;
		  System.out.println("creating server at "+port);
          //serverSocket.setSoTimeout(180000);
		  start();
       }

       public void run()
       {
		   try
		   {
			    serverSocket = new ServerSocket(port);
		   }
		   catch(Exception exp)
		   {}
           while(true)
          { 
               try
               {
                  server = serverSocket.accept();
				
                  BufferedImage img=ImageIO.read(ImageIO.createImageInputStream(server.getInputStream()));
                  File outputfile = new File(name+".jpg");
				ImageIO.write(img, "jpg", outputfile);
              }
             catch(SocketTimeoutException st)
             {
                   System.out.println("Socket timed out!");
                  break;
             }
             catch(IOException e)
             {
                  e.printStackTrace();
                  break;
             }
             catch(Exception ex)
            {
                  System.out.println(ex);
            }
          }
       }
}

class GreetingClient extends Thread
{
    public Image newimg;
    public static BufferedImage bimg;
    public byte[] bytes;
	public int port;
	public String serverName = "localhost";
	public File file;
    GreetingClient(File file,int portSend)
    {
		this.file=file;
        this.port = portSend;
		start();
    }
	public void run()
	{
		try
        {
			sleep(1000);
            Socket client = new Socket(serverName,port);
            bimg=ImageIO.read(file);
            ImageIO.write(bimg,"JPG",client.getOutputStream());
           
			try{
			//Thread.sleep(10000);
			}
			catch(Exception es)
			{}
			 client.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
	}
}
