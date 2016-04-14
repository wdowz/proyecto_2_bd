package space_invaders;

import javax.swing.JFrame;

/**
 *
 * @author wdowz
 */
public class Main extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Main(){
        add(new Board());
        setTitle("Space Invaders");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450,300);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
    }

    public static void main(String[] args) {
        new Main();
    }

}
