
//////////
import java.awt.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import javax.imageio.*;
import javax.swing.*;
import java.io.*;
import java.awt.image.*;
import javax.swing.filechooser.*;

import java.io.*;
class Client 
{
	String name;
	String contact;
	String email;
	String password;
	Socket clientSocket;
	Client(String name,String contact,String email,String password)
	{
		this.name=name;
		this.contact=contact;
		this.email=email;
		this.password=password;
	}
	Client(String name,String contact,String password)
	{
		this.name=name;
		this.contact=contact;
		this.password=password;
	}
}
class ServerNode
{
	static BufferedImage img;
    static Vector clientSockets;
    static Vector LoginNames;
	static Vector Clients;
	static Statement statement;
	public static void main(String args[]) throws Exception
    {    
		clientSockets=new Vector();
        LoginNames=new Vector();
		Clients=new Vector();  
		Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		Connection con=DriverManager.getConnection("Jdbc:Odbc:server"," "," ");
		statement=con.createStatement();
		ServerSocket serverSocket=new ServerSocket(6126);
		while(true)
        {			
            Socket clientSocket=serverSocket.accept();        
			new Validate().userValidate(clientSocket);
        }
    }
}
class Validate
{
	Client client;
	public void userValidate(Socket clientSocket)
	{	
		try
		{
			DataInputStream readFromClient=new DataInputStream(clientSocket.getInputStream());
			DataOutputStream writeToClient=new DataOutputStream(clientSocket.getOutputStream());
			String action=readFromClient.readUTF();
			if(action.equals("register"))
			{
				
				String name=readFromClient.readUTF();
				String contact=readFromClient.readUTF();
				String email=readFromClient.readUTF();
				String pass=readFromClient.readUTF();
				try
				{
					System.out.println("Query");
					ServerNode.statement.execute("INSERT INTO userData(userName,contact,email,password,logfile) values('"+name+"','"+contact+"','"+email+"','"+pass+"','"+name+"Log');");
					System.out.println("Query error free");
					client=new Client(name,contact,email,pass);
					writeToClient.writeUTF("allow");
					new AcceptClient().userAcceptClient(client,clientSocket);
				}
				catch(Exception ex)
				{
					writeToClient.writeUTF("deny");
					writeToClient.writeUTF("user of same details already exists please try changing contact no,");
					new Validate().userValidate(clientSocket);
				}
			}
			else
			{
				String contact=readFromClient.readUTF();
				String pass=readFromClient.readUTF();
				String name="error";
				ResultSet rs=ServerNode.statement.executeQuery("SELECT userName FROM userData WHERE contact='"+contact+"' AND password='"+pass+"';");
				while(rs.next())
				{
					name=rs.getString(1);
				}
				if(name.equals("error"))
				{
					writeToClient.writeUTF("deny");
					writeToClient.writeUTF("Invalid credentials or not a user");
					new Validate().userValidate(clientSocket);
				}
				else
				{
					client=new Client(name,contact,pass);
					writeToClient.writeUTF("allow");
					writeToClient.writeUTF(name);
					new AcceptClient().userAcceptClient(client,clientSocket);
				}
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex+" exception in varification");
		}
	}
}
class AcceptClient extends Thread
{
    Socket clientSocket;
    DataInputStream readFromClient;
    DataOutputStream writeToClient;
	Client client;
	int portSend=6010;
	int portRecieve;
    public void userAcceptClient(Client client,Socket clientSocket) throws Exception
    {
        this.clientSocket=clientSocket;
		this.client=client;
        readFromClient=new DataInputStream(clientSocket.getInputStream());
        writeToClient=new DataOutputStream(clientSocket.getOutputStream());
        String LoginName=client.name;
        System.out.println("User Logged In :" + LoginName);
        ServerNode.LoginNames.add(LoginName);
        ServerNode.clientSockets.add(clientSocket);
		ServerNode.Clients.add(client);
        start();
    }
    public void run()
    {

	/*********************
	this block is used for sending the pending messages to the newly logged in user 
	in this we will read the file of newly loged user read the content write it to client and then
	clear the file
	
	**********************/
	
		try
		{
			String s3=" ";
			try
			{
				//FileOutputStream fout=new FileOutputStream(client.name+"log.txt");
				//fout.close();
				FileInputStream fin=new FileInputStream(client.name+"log.txt");
				BufferedInputStream bin=new BufferedInputStream(fin);
				int i;
				s3=new String();
				while((i=bin.read())!=-1)
				{
					s3=s3+(char)i;
				}
				System.out.print(s3);
				bin.close();
				writeToClient.writeUTF(s3);
				FileOutputStream fot=new FileOutputStream(client.name+"log.txt");
				BufferedOutputStream bout=new BufferedOutputStream(fot);
				String history="";
				byte b[]=history.getBytes();
				bout.write(b);
				bout.flush();
				bout.close();
				fot.close();
				
			}
			catch(Exception ex)
			{
				System.out.println(ex);
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
        while(true)
        {
            
            try
            {
                String msgFromClient=new String();
                msgFromClient=readFromClient.readUTF();
                StringTokenizer st=new StringTokenizer(msgFromClient);
                String Sendto=st.nextToken();                
                String MsgType=st.nextToken();
                int iCount=0;
                if(MsgType.equals("LOGOUT"))
                {
                    for(iCount=0;iCount<ServerNode.LoginNames.size();iCount++)
                    {
                        if(ServerNode.LoginNames.elementAt(iCount).equals(Sendto))
                        {
                            ServerNode.LoginNames.removeElementAt(iCount);
                            ServerNode.clientSockets.removeElementAt(iCount);
                            ServerNode.Clients.removeElementAt(iCount);
                            System.out.println("User " + Sendto +" Logged Out ...");
                            break;
                        }
                    }
                }
				else if(MsgType.equals("file"))
				{
					/////////////////////////////////////////////////////
					portRecieve=Integer.valueOf(st.nextToken());
					new GreetingServer(portRecieve,Sendto);
					try
					{
						
					}
					catch(Exception r)
					{}
					/////////////////////////////////////////////////////
					for(iCount=0;iCount<ServerNode.LoginNames.size();iCount++)
                    {
                        if(ServerNode.LoginNames.elementAt(iCount).equals(Sendto))
                        {    
                            Socket tSoc=(Socket)ServerNode.clientSockets.elementAt(iCount);                            
                            DataOutputStream twriteToClient=new DataOutputStream(tSoc.getOutputStream());
							twriteToClient.writeUTF(client.name+" "+"file"+" "+portSend);
							System.out.println(client.name+" "+"file");
							try {
									try {
											
											new GreetingClient(Sendto,portSend);									//flushing socket
											//twriteToClient.writeUTF("server you recieved an image");
											portSend=portSend+1;
											writeToClient.writeUTF("server You Sent an image");
											twriteToClient.writeUTF("server you recieved an image");
										}
										finally {
										}		
								} catch (IOException exe) {
									
									// TODO Auto-generated catch block
									exe.printStackTrace();
								}
							/////////////////////////////////////////////
                            break;
                        }
                    }
					if(iCount==ServerNode.LoginNames.size())
					{
						writeToClient.writeUTF("server error while sending image user is offline");
					}
                   
				}
                else
                {
                    String msg="";
                    while(st.hasMoreTokens())
                    {
                        msg=msg+" " +st.nextToken();
                    }
                    for(iCount=0;iCount<ServerNode.LoginNames.size();iCount++)
                    {
                        if(ServerNode.LoginNames.elementAt(iCount).equals(Sendto))
                        {    
                            Socket tSoc=(Socket)ServerNode.clientSockets.elementAt(iCount);                            
                            DataOutputStream twriteToClient=new DataOutputStream(tSoc.getOutputStream());
                            twriteToClient.writeUTF(client.name+" "+msg);                            
                            break;
                        }
                    }
                    if(iCount==ServerNode.LoginNames.size())
                    {
						String s="error";
						ResultSet rs=ServerNode.statement.executeQuery("Select * from userData where userName='"+Sendto+"';");
						while(rs.next())
						{
							s=rs.getString(1);
						}
						if(s.equals("error"))
						{
							writeToClient.writeUTF("server No user with this name registererd with us");
						}
						else
						{
							String s3=" ";
							try
							{
								try
								{
									FileInputStream fin=new FileInputStream(Sendto+"log.txt");
									fin.close();
								}
								catch(Exception exp)
								{
									FileOutputStream fout=new FileOutputStream(Sendto+"log.txt");		// create table if not exists
									fout.close();
								}
								FileInputStream fin=new FileInputStream(Sendto+"log.txt");
								BufferedInputStream bin=new BufferedInputStream(fin);
								int i;
								s3=new String();
								while((i=bin.read())!=-1)
								{
									s3=s3+(char)i;
								}
								System.out.print(s3);
								bin.close();
								fin.close();
								FileOutputStream fout=new FileOutputStream(Sendto+"log.txt");
								BufferedOutputStream bout=new BufferedOutputStream(fout);
								String history=slient.name+" "+s3+msg;
								byte b[]=history.getBytes();
								bout.write(b);
								bout.flush();
								bout.close();
								fout.close();
								writeToClient.writeUTF(Sendto+" I am not availble right now. donn't worry your mag are stored in my pending box");
							}
							catch(Exception ex)
							{
								System.out.println(ex);
							}
						}
                    }
                    else
                    {
                        
                    }
                }
                if(MsgType.equals("LOGOUT"))
                {
                    break;
                }

            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }   
	}		
}
/*
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
*/

class GreetingServer extends Thread
{
       public ServerSocket serverSocket;
       public Socket server;
		public String name;
		public int port;
       public GreetingServer(int port,String fileName) throws IOException, SQLException, ClassNotFoundException, Exception
       {
          this.port=port;
		  this.name=fileName;
		  start();
       }

       public void run()
       {
		   try
               {
				     serverSocket = new ServerSocket(port);
                
              }
             catch(Exception st)
             {
             }
		 
           while(true)
          { 
               try
               {
                server = serverSocket.accept();
                BufferedImage img=ImageIO.read(ImageIO.createImageInputStream(server.getInputStream()));
                File outputfile = new File("D:\\"+name+".jpg");
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
	public String name;
	public String serverName = "localhost";
    public int port;

    GreetingClient(String name,int portSend)
    {
        this.serverName = "localhost";
        this.port = portSend;
		this.name=name;
		start();
       
    }
	public void run()
	{
		 try
        {
			System.out.println("requesting server at "+port+name);
			sleep(3000);
            Socket client = new Socket(serverName,port);
            BufferedImage img = ImageIO.read(new File("D:\\"+name+".jpg"));
            ImageIO.write(img,"JPG",client.getOutputStream());
			try{
		//	Thread.sleep(10000);
			}
			catch(Exception es)
			{}
			 client.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
	}
}
