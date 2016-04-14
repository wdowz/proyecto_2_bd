package space_invaders;

import javax.swing.ImageIcon;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.Rectangle;

import java.util.ArrayList;

/**
 *
 * @author wdowz
 */
public class Ship {
    private Image image;
    private int x,y,dx, bullets;
    private final int SPEED = 2;
    private ArrayList<Laser> lasers;
    private boolean shot;
    
    public Ship(){
        ImageIcon ii = new ImageIcon(this.getClass().getResource("/ship.png"));
        image = ii.getImage();
        y = 250;
        x = 150-image.getWidth(null)/2;
        dx = 0;
        lasers = new ArrayList<Laser>();
        shot = true;
        bullets = 0;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public Image getImage(){
        return image;
    }

    public ArrayList<Laser> getLasers(){
        return lasers;
    }

    public int getBulletsShooted(){
    	return bullets;
    }
    
    public void logic(){
        if((x>17 && dx<0) || (x<229 && dx>0))
            x += dx;
    }

    public void keyPressed(KeyEvent e){
        int key = e.getKeyCode();

        if(key == KeyEvent.VK_RIGHT)
            dx = SPEED;
        if(key == KeyEvent.VK_LEFT)
            dx = SPEED * -1;
        if(key == KeyEvent.VK_SPACE && shot)
        {
            lasers.add(new Laser(x + image.getWidth(null)/2, y));
            shot = false;
            bullets += 1;
        }
    }

    public void keyReleased(KeyEvent e){
        int key = e.getKeyCode();

        if(key == KeyEvent.VK_LEFT && dx < 0)
            dx = 0;
        if(key == KeyEvent.VK_RIGHT && dx > 0)
            dx = 0;
        if(key == KeyEvent.VK_SPACE)
            shot = true;
    }

    public Rectangle getBounds(){
        return new Rectangle(x, y, image.getWidth(null), image.getHeight(null));
    }
}
