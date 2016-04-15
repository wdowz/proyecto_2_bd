package space_invaders;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;

// timer
import javax.swing.Timer;

import org.apache.commons.lang3.time.StopWatch;
import org.postgis.PGgeometry;
import java.awt.Toolkit;

// teclas y timer
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

// teclas
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.awt.Font;

/**
 *
 * @author wdowz
 */
public class Board extends JPanel implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Timer timer;
    private Ship ship;
    private Alien alien[][];
    private final int ALIENCOLUMNS, ALIENROWS, ALIEN_STARTX, ALIEN_STARTY, ALIEN_PADDING;
    private Font font;
    private String distance, coordinates, area, points, bullets, collision, status, color_ship, color_alien, aliens, time, game, ship_table, alien_table, ship_polygon, left_rect_polygon, right_rect_polygon, alien_polygon, figure_table, count_collisions;
    private int aliensLeft, pts, POINTS, xx, yy, xxw, yyh, elapsedTime, countcollision;
    private boolean gameEnded, collisioned, gameWon;
    private StopWatch sw;
    private Rectangle game_rect, aliens_rect;
    private float  result_distance, result_area;
    
    private PreparedStatement pst_ship = null;
    private PreparedStatement pst_alien = null;
    PreparedStatement pst_figure = null;
    private Connection c = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    
    public Board(){
        setDoubleBuffered(true);
        setBackground(Color.white);
        setFocusable(true);
        addKeyListener(new Listener());
        
        ship = new Ship();
        sw = new StopWatch();
        
        // Aliens
        ALIENCOLUMNS = 6;
        ALIENROWS = 3;
        ALIEN_STARTX = 20;
        ALIEN_STARTY = 20;
        ALIEN_PADDING = 3;
        POINTS = 10000;
        pts = 0;
        result_distance = 0;
        
        alien = new Alien[ALIENCOLUMNS][ALIENROWS];
        for(int i = 0; i < ALIENCOLUMNS; i++)
            for(int j = 0; j < ALIENROWS; j++){
                alien[i][j] = new Alien(ALIEN_STARTX, ALIEN_STARTY);
                alien[i][j].setX(ALIEN_STARTX + i*alien[i][j].getImage().getWidth(null) + i*ALIEN_PADDING);
                alien[i][j].setY(ALIEN_STARTY + j*alien[i][j].getImage().getHeight(null) + j*ALIEN_PADDING);
            }

        aliensLeft = ALIENCOLUMNS * ALIENROWS;
        
        gameEnded = false;
        gameWon = false;
        collisioned = false;
        timer = new Timer(15, this);
        timer.start();
        sw.start();
        
        font = new Font("Verdana", Font.PLAIN, 12);
        coordinates = "Coordinates: " + ship.getX() + ", " + ship.getY();
        status = "Game over: " + gameEnded;
        bullets = "Total bullets shooted: " + ship.getBulletsShooted();
        aliens = "Aliens left: " + aliensLeft;
        collision = "Collision: " + collisioned;
        time = "Time elapsed: " + sw.getTime() / 1000;
        game = "Game won: " + gameWon;
        points = "Total points: " + POINTS;
        
        game_rect = new Rectangle(17, 15, 241, 255);
        
        color_ship = "Red";
        color_alien = "Red";
        distance = "Distance between ship and aliens: " + result_distance;
        area = "Area of the ship is: " + result_area;
        count_collisions = "Count collisions of ship with rectangles: " + countcollision;
        
        left_rect_polygon = "SRID=4326;POLYGON((" + game_rect.x + " " + game_rect.y + ", " + game_rect.x + " " + 600 + ", " + -400 + " " + 600 + ", " + -400 + " " + game_rect.y + ", " + game_rect.x + " " + game_rect.y + "))";
        right_rect_polygon = "SRID=4326;POLYGON((" + (game_rect.x + game_rect.width) + " " + game_rect.y + ", " + 700 + " " + game_rect.y + ", " + 700 + " " + 600 + ", " + (game_rect.x + game_rect.width) + " " + 600 + ", " + (game_rect.x + game_rect.width) + " " + game_rect.y + "))";
        
        figure_table = "INSERT INTO figure (polygon) values (?)";
        
        try {
        	c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/space_invaders", "postgres", "123456789");
        	
        	//Figure table
        	pst_figure = c.prepareStatement(figure_table);
			pst_figure.setObject(1, new PGgeometry(left_rect_polygon));
			pst_figure.executeUpdate();
			
			
		} catch (SQLException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} finally {
			try {
                if (pst_figure != null) {
                	pst_figure.close();
                }
                if (c != null) {
                    c.close();
                }

            } catch (SQLException ex) {
                
            }
		}
        
        try {
        	c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/space_invaders", "postgres", "123456789");
        	
        	//Figure table
        	pst_figure = c.prepareStatement(figure_table);
			pst_figure.setObject(1, new PGgeometry(right_rect_polygon));
			pst_figure.executeUpdate();
			
			
		} catch (SQLException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} finally {
			try {
                if (pst_figure != null) {
                	pst_figure.close();
                }
                if (c != null) {
                    c.close();
                }

            } catch (SQLException ex) {
                
            }
		}
        
        t.start();
    }

    public Thread t = new Thread(){
    	@Override
    	public void run(){
    		while(true){
    			try{
    				
    				//Database connection
    		        ship_table = "INSERT INTO ship (polygon, color, total_points, x_cor, y_cor, coordinates, elapsed_time) values (?, ?, ?, ?, ?, ?, ?)";
    		        alien_table = "INSERT INTO alien (polygon, color, total, remaining) values (?, ?, ?, ?)";

    		        try {
    		        	c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/space_invaders", "postgres", "123456789");
    		        	
    		        	//Ship table
    					pst_ship = c.prepareStatement(ship_table);
    					pst_ship.setObject(1, new PGgeometry(ship_polygon));
    					pst_ship.setString(2, color_ship);
    					pst_ship.setInt(3, pts);
    					pst_ship.setInt(4, ship.getX());
    					pst_ship.setInt(5, ship.getY());
    					pst_ship.setString(6, coordinates);
    					pst_ship.setInt(7, elapsedTime);
    					pst_ship.executeUpdate();
    					
    					//Alien table
    					pst_alien = c.prepareStatement(alien_table);
    					pst_alien.setObject(1, new PGgeometry(alien_polygon));
    					pst_alien.setString(2, color_alien);
    					pst_alien.setInt(3, (ALIENCOLUMNS * ALIENROWS));
    					pst_alien.setInt(4, aliensLeft);
    					pst_alien.executeUpdate();

    			        stmt = c.createStatement();
    			        rs = stmt.executeQuery("SELECT ST_DISTANCE(a.polygon, s.polygon) as Distance FROM ship s, alien a;");
    			        while ( rs.next() ) {
    			           result_distance = rs.getFloat("Distance");
    			        }
    			        rs = stmt.executeQuery("SELECT ST_AREA(polygon) as SHIP_AREA from ship;");
    			        while(rs.next()){
    		 	           result_area = rs.getFloat("SHIP_AREA");
    			        }
    			        rs = stmt.executeQuery("SELECT f.name, count(ST_TOUCHES(s.polygon, f.polygon)) as cint from ship s, figure f where ST_TOUCHES(s.polygon, f.polygon) GROUP BY f.name;");
    			        while(rs.next()){
    			        	countcollision = rs.getInt("cint");
    			        }
    					stmt.close();
    				} catch (SQLException ex) {
    					// TODO Auto-generated catch block
    					ex.printStackTrace();
    				} finally {
    					try {
    		                if (pst_ship != null && pst_alien != null) {
    		                    pst_ship.close();
    		                    pst_alien.close();
    		                }
    		                if (c != null) {
    		                    c.close();
    		                }

    		            } catch (SQLException ex) {
    		                
    		            }
    				}
    				try {
    					rs.close();
    				} catch (SQLException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}
    				Thread.sleep(1000);
    			}catch(InterruptedException ie){
    				
    			}
    		}
    	}
    };
    
    public void paint(Graphics g){
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;

        // Draw
        g2d.drawImage(ship.getImage(), ship.getX(), ship.getY(), this);

        // Draw Lasers
        ArrayList<Laser> lasers = ship.getLasers();
        for(int i = 0; i < lasers.size(); i++){
            Laser l = lasers.get(i);
            g2d.drawImage(l.getImage(), l.getX(), l.getY(), this);
        }

        // Draw aliens
        for(int i = 0; i < ALIENCOLUMNS; i++)
            for(int j = 0; j < ALIENROWS; j++){
                if(alien[i][j].isVisible())
                    g2d.drawImage(alien[i][j].getImage(), alien[i][j].getX(),
                            alien[i][j].getY(), this);
                ArrayList<Bomb> bombs = alien[i][j].getBombs();
                for(int k=0; k < bombs.size(); k++){
                    Bomb bomb = bombs.get(k);
                    g2d.drawImage(bomb.getImage(), bomb.getX(), bomb.getY(), this);
                }
            }

        
        // Draw text
        g2d.setColor(Color.black);
        g2d.setFont(font);
        
        g2d.drawString(time, 265, 24);
        g2d.drawString(coordinates, 265, 40);
        g2d.drawString(status, 265, 56);
        g2d.drawString(bullets, 265, 72);
        g2d.drawString(aliens, 265, 88);
        g2d.drawString(collision, 265, 104);
        g2d.drawString(game, 265, 120);
        g2d.drawString(points, 265, 136);
        
        g2d.drawString(distance, 18, 290);
        g2d.drawString(area, 18, 306);
        g2d.drawString(count_collisions, 18, 322);
    
        g2d.draw(game_rect);
        g2d.drawLine(17, 250, 258, 250);

        Toolkit.getDefaultToolkit().sync();
        g.dispose();
    }

    public void actionPerformed(ActionEvent e){
    	
        // Updates
        ship.logic();
        // Lasers
        ArrayList<Laser> lasers = ship.getLasers();
        for(int i = 0; i < lasers.size(); i++){
            Laser l = lasers.get(i);
            if(l.isVisible())
                l.update();
            else
                lasers.remove(i);
        }

        // Aliens
        for(int i = 0; i < ALIENCOLUMNS; i++){
            for(int j = 0; j < ALIENROWS; j++){
                alien[i][j].update();
                if(alien[i][j].getY() >= 250)
                    gameOver(0);

                // Aliens Bombs
                ArrayList<Bomb> bombs = alien[i][j].getBombs();
                for(int k=0; k < bombs.size(); k++){
                    Bomb bomb = bombs.get(k);
                    if(bomb.isVisible())
                        bomb.update();
                    else{
                        bombs.remove(k);
                        pts += POINTS;
                    }

                    if(bomb.getBounds().intersects(ship.getBounds()))
                    {
                        bomb.setVisible(false);
                        gameOver(0);
                        collisioned = true;
                    }
                }

                //Hit Test
                for(int li = 0; li < lasers.size(); li++){
                    Laser l = lasers.get(li);
                    if(l.getBounds().intersects(alien[i][j].getBounds()) && l.isVisible() && alien[i][j].isVisible()){
                        alien[i][j].setVisible(false);
                        l.setVisible(false);
                        aliensLeft--;
                        pts += POINTS;
                        if(aliensLeft <= 0)
                            gameOver(1);
                    }
                }
            }
        }
        
        //update points
        POINTS = (int) (POINTS - (sw.getTime() / 1000) * 2);
        
        //update time
        elapsedTime = (int) (sw.getTime() / 1000);
        
        if(!gameEnded) {
		}
        
        coordinates = "Coordinates: " + ship.getX() + ", " + ship.getY();
        status = "Game over: " + gameEnded;
        bullets = "Total bullets shooted: " + ship.getBulletsShooted();
        aliens = "Aliens left: " + aliensLeft;
        collision = "Collision: " + collisioned;
        time = "Time elapsed: " + elapsedTime;
        game = "Game won: " + gameWon;
        points = "Total points: " + pts;
        distance = "Distance between ship and aliens: " + result_distance;
        area = "Area of the ship is: " + result_area;
        count_collisions = "Count collisions of ship with rectangles: " + countcollision;
        repaint();
        
        xx = ship.getBounds().x;
        yy = ship.getBounds().y;
        xxw = ship.getBounds().width;
        yyh = ship.getBounds().height;
        
        aliens_rect = new Rectangle(alien[0][0].getBounds().x, alien[0][0].getBounds().y, (alien[0][0].getBounds().x * ALIENROWS), (alien[0][0].getBounds().y * ALIENCOLUMNS));
        
        ship_polygon = "SRID=4326;POLYGON((" + xx + " " + yy + ", " + xx + " " + (yy + yyh) + ", " + (xx + xxw) + " " + (yy + yyh) + ", " + (xx + xxw) + " " + yy + ", " + xx + " " + yy + "))";
        alien_polygon = "SRID=4326;POLYGON((" + aliens_rect.x + " " + aliens_rect.y + ", " + (aliens_rect.x + aliens_rect.width) + " " + aliens_rect.y + ", " + (aliens_rect.x + aliens_rect.width) + " " + (aliens_rect.y + aliens_rect.height) + ", " + aliens_rect.x + " " + (aliens_rect.y + aliens_rect.height) + ", " + aliens_rect.x + " " + aliens_rect.y + "))";
        
        
    }
    
    public void update(){
    	

    }
    
    private class Listener extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e){
            ship.keyPressed(e);
            if(e.getKeyCode() == KeyEvent.VK_ENTER){
                if(gameEnded){
                    for(int i = 0; i < ALIENCOLUMNS; i++)
                        for(int j = 0; j < ALIENROWS; j++){
                            alien[i][j] = null;
                            alien[i][j] = new Alien(ALIEN_STARTX, ALIEN_STARTY);
                            alien[i][j].setX(ALIEN_STARTX + i*alien[i][j].getImage().getWidth(null) + i*ALIEN_PADDING);
                            alien[i][j].setY(ALIEN_STARTY + j*alien[i][j].getImage().getHeight(null) + j*ALIEN_PADDING);
                        }
                    gameEnded = false;
                    aliensLeft = ALIENCOLUMNS * ALIENROWS;
                    timer.start();
                }
            }
        }
        @Override
        public void keyReleased(KeyEvent e){
            ship.keyReleased(e);
        }
    }

    public void gameOver(int status){
        gameEnded = true;
        if(status == 0){
            gameWon = false;
        }else{
            gameWon = true;
        }
        timer.stop();
    }
}
