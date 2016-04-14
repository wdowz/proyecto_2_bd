package space_invaders;

/**
 *
 * @author wdowz
 */
public class Bomb extends Laser {
    public Bomb(int x, int y){
        super(x, y);
    }

    @Override
    public void update(){
        y++;
    }
}
