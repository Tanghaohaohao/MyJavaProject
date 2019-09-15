package chat;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ChatClient extends Frame{
	
	TextField tfText = new TextField();
	TextArea taContent = new TextArea();
	Socket s = null;
	DataOutputStream dos = null; 
	DataInputStream dis = null;
	private boolean bconneted = false;
	Thread tRecv = new Thread(new RecvThread());
 	
	public static void main(String[] args) {
		new ChatClient().launchFrame();
	}
	
	public void launchFrame() {
		setLocation(400, 300);
		this.setSize(300, 300);
		add(tfText, BorderLayout.SOUTH);
		add(taContent, BorderLayout.NORTH);
		pack();
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				disconnect();
				System.exit(0);
			}
			
		});
		tfText.addActionListener(new TFListener());
		setVisible(true);
		connect();
		
		tRecv.start();
	}
	
	public void connect() {
		try {
			s= new Socket("127.0.0.1", 8888);
			dos = new DataOutputStream(s.getOutputStream());
			dis = new DataInputStream(s.getInputStream());
System.out.println("connected");
            bconneted = true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		try {
			dos.close();
			dis.close();
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*try {
			bconneted = false;
			tRecv.join(); 					
		} catch(InterruptedException e) {
			e.printStackTrace();
		}finally {
			try {
				dos.close();
				dis.close();
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
				
		}*/
	}
	
	private class TFListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			String str = tfText.getText().trim();
			//taContent.setText(str);
			tfText.setText("");
			
			try {
				dos.writeUTF(str);
				dos.flush();
				//dos.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}			
			}		
	}
	
	private class RecvThread implements Runnable{
		public void run() {
			try {
				while(bconneted) {
					String str = dis.readUTF();
					//System.out.println(str);
					taContent.setText(taContent.getText() + str + '\n');
				}
			}catch(SocketException e) {
				System.out.println("退出了，bye！");
			}catch (EOFException e) {
				System.out.println("退出了，bye-bye！");
			}catch(IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
