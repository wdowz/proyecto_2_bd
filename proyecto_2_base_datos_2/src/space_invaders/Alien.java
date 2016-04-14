package space_invaders;

import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.ImageIcon;
import java.util.Random;
import java.util.ArrayList;

/**
 *
 * @author wdowz
 */
public class Alien {
    private Image image;
    private int x, y, speed, direction, movedX, movedY, bombChance;
    private final int RANGE;
    private boolean visible, goDown;
    private Random random;
    private ArrayList<Bomb> bombs;
    public Alien(int x, int y){
        ImageIcon ii = new ImageIcon(this.getClass().getResource("/alien.png"));
        image = ii.getImage();
        this.x = x;
        this.y = y;
        speed = 1;
        RANGE = 100;
        movedX = 0;
        direction = 1; // 1 = derecha, -1 = izquierda
        visible = true;
        goDown = false;
        movedY = 0;
        random = new Random();
        bombChance = 700; // 1 in 40
        bombs = new ArrayList<Bomb>();
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public Image getImage(){
        return image;
    }

    public int getSpeed(){
        return speed;
    }

    public void setSpeed(int speed){
        this.speed = speed;
    }

    public void setVisible(boolean visible){
        this.visible = visible;
    }
    
    public boolean isVisible(){
        return visible;
    }

    public Rectangle getBounds(){
        return new Rectangle(x, y, image.getWidth(null), image.getHeight(null));
    }

    public void update(){
        if(movedX>RANGE){
            movedX = 0;
            goDown = true;
            direction*=-1;
        }

        if(goDown){
            y++;
            movedY++;
            if(movedY > image.getHeight(null))
            {
                goDown = false;
                movedY = 0;
            }
        } else {
            x += speed * direction;
            movedX+= speed;
        }

        if(random.nextInt()%bombChance==1 && y < 150)
            bombs.add(new Bomb(x, y));
    }

    public ArrayList<Bomb> getBombs(){
        return bombs;
    }
}
