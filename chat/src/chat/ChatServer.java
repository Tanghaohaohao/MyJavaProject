package chat;

//import java.awt.List;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

public class ChatServer {
	
	boolean started = false;
	ServerSocket ss = null;
	List<Client> clients = new ArrayList<Client>();
	
	public static void main(String[] args) {
		new ChatServer().start();		
	}

	public void start() {
		try {
			ss = new ServerSocket(8888);
			started = true;
		}catch(BindException e) {
			System.out.println("端口使用中....");
			System.out.println("请关掉相关程序并重新运行服务器");
			System.exit(0);
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		try {			
			while(started) {
				Socket s = ss.accept();
				Client c = new Client(s);
System.out.println("a client connected!");
                new Thread(c).start(); 
                clients.add(c);
                //dis.close();
			}			
		} catch (IOException e) {			
			e.printStackTrace();			
		}finally {
			try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	class Client implements Runnable{
		private Socket s;
		private DataInputStream dis = null;
		private DataOutputStream dos = null;
		private boolean bconnected = false;
		
		public Client(Socket s) {
			this.s = s;
			try {
				dis = new DataInputStream(s.getInputStream());
				dos = new DataOutputStream(s.getOutputStream());
				bconnected = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void send(String str) throws IOException{
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				clients.remove(this);
				System.out.println("对方退出了！我从List里面去掉了！");
				//e.printStackTrace();
			}
		}
		
		public void run() {
			Client c = null;
			try {
				while(bconnected){
            	String str = dis.readUTF();
     System.out.println(str);
                for(int i=0;i<clients.size();i++) {
                	c = clients.get(i);
                	c.send(str);
                }
            }
			}catch (EOFException e) {
				System.out.println("Client closed!");
			}catch (IOException e) {			
				e.printStackTrace();			
			}finally {
				try {
					if(dis != null) dis.close();
					if(dos != null) dos.close();
					if(s != null) s.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}           
		}		
	}
}
