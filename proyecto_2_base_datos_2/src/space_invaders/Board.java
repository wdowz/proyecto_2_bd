package space_invaders;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

// timer
import javax.swing.Timer;

import org.apache.commons.lang3.time.StopWatch;

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
import java.sql.SQLException;
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
    private String msg, coordinates, points, bullets, collision, status, aliens, time, game, stm;
    private int aliensLeft, pts, POINTS;
    private boolean gameEnded, collisioned, gameWon;
    private StopWatch sw;
    PreparedStatement pst;
    
    Connection c;
    
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
        
        try {
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/space_invaders", "postgres", "123456789");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        stm = "INSERT INTO game(ship_coordinates, ship, left_rect, right_rect, time_elapsed, game_over, total_bullets, aliens_left, collision, game_won, total_points) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try {
			pst = c.prepareStatement(stm);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

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
        g2d.drawString(msg, 3, 12);
        
        g2d.drawString(time, 265, 24);
        g2d.drawString(coordinates, 265, 40);
        g2d.drawString(status, 265, 56);
        g2d.drawString(bullets, 265, 72);
        g2d.drawString(aliens, 265, 88);
        g2d.drawString(collision, 265, 104);
        g2d.drawString(game, 265, 120);
        g2d.drawString(points, 265, 136);
    
        g2d.drawRect(17, 15, 241, 300);
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
        POINTS = (int) (POINTS - (sw.getTime() / 1000));
        
        if(!gameEnded)
            msg = "Time: ";
        
        coordinates = "Coordinates: " + ship.getX() + ", " + ship.getY();
        status = "Game over: " + gameEnded;
        bullets = "Total bullets shooted: " + ship.getBulletsShooted();
        aliens = "Aliens left: " + aliensLeft;
        collision = "Collision: " + collisioned;
        time = "Time elapsed: " + sw.getTime() / 1000;
        game = "Game won: " + gameWon;
        points = "Total points: " + pts;
        repaint();
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
            msg = "You lose!";
        	gameWon = false;
        }else{
            msg = "You won!";
            gameWon = true;
        }
        msg += " - Press [ENTER] to play again.";
        timer.stop();
    }
}
