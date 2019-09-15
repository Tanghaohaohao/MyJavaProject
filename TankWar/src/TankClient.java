import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;

/**
 * 坦克游戏主窗口
 * @author 唐茂钦
 * 
 */

public class TankClient extends Frame{
	
	public static final int GAME_WIDTH = 800;
	public static final int GAME_HEIGHT = 600;
	
	Tank myTank = new Tank(50, 50, true, Tank.Direction.STOP, this);
	
	Wall w1 = new Wall(100, 200, 20, 150, this);
	Wall w2 = new Wall(300, 100, 300, 20, this);
	
	List<Explode> explodes = new ArrayList<Explode>();
	List<Missile> missiles = new ArrayList<Missile>();
	List<Tank> tanks = new ArrayList<Tank>();
	
	Image offScreenImage = null;
	
	Blood b = new Blood();
		
	public void paint(Graphics g) {
		/*
		 * 子弹数量
		 * 爆炸数量
		 * 坦克数量
		 * 坦克生命值
		 */
		g.drawString("missile  count:" + missiles.size(), 10, 50);
		g.drawString("explodes count:" + explodes.size(), 10, 70);
		g.drawString("tanks    count:" + tanks.size(), 10, 90);
		g.drawString("tanks    life:" + myTank.getLife(), 10, 110);
		
		if(tanks.size()<=0) {
			for(int i=0;i<5;i++) {
				tanks.add(new Tank(50+40*(i+1), 50, false, Tank.Direction.D,this));
			}
		}
		
		for(int i=0;i<missiles.size();i++) {
			Missile m = missiles.get(i);
			m.hitTanks(tanks);
			m.hitTank(myTank);
			m.hitWall(w1);
			m.hitWall(w2);
			m.draw(g);
			//if(!m.isLive()) missiles.remove(m);
			//m.draw(g);
		}
		
		for(int i=0;i<explodes.size();i++) {
			Explode e = explodes.get(i);
			e.draw(g);
		}
		
		for(int i=0;i<tanks.size();i++) {
			Tank t = tanks.get(i);
			t.collidesWithWall(w1);
			t.collidesWithWall(w2);
			t.collidesWithTanks(tanks);
			t.draw(g);
		}
		
		myTank.draw(g);
		myTank.eat(b);
		w1.draw(g);
		w2.draw(g);
		b.draw(g);
	}
	
    public void update(Graphics g) {
		if(offScreenImage == null) {
			offScreenImage = this.createImage(GAME_WIDTH, GAME_HEIGHT);
		}
		Graphics goffScreen = offScreenImage.getGraphics();
		Color c = goffScreen.getColor();
		goffScreen.setColor(Color.GREEN);
		goffScreen.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
		goffScreen.setColor(c);
		paint(goffScreen);
		g.drawImage(offScreenImage, 0, 0, null);
		
	}

	public void lauchFrame() {
		
		for(int i=0;i<10;i++) {
			tanks.add(new Tank(50+40*(i+1), 50, false, Tank.Direction.D,this));
		}
		
		this.setLocation(400, 300);
		this.setSize(GAME_WIDTH, GAME_HEIGHT);
		this.setTitle("TankWar");
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}			 
		});
		this.setResizable(false);
		this.setBackground(Color.GREEN);
		
		this.addKeyListener(new KeyMonitor());
		
		setVisible(true);
		
		new Thread(new PaintThread()).start();;
	}
	
	public static void main(String[] args) {		
		TankClient tc = new TankClient();
		tc.lauchFrame();
	}
	
	private class PaintThread implements Runnable{
		public void run() {
			while(true) {
				repaint();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class KeyMonitor extends KeyAdapter{

		public void keyReleased(KeyEvent e) {
			myTank.keyReleased(e);
		}

		public void keyPressed(KeyEvent e) {
			myTank.KeyPressed(e);							
		}
		
	}
}
