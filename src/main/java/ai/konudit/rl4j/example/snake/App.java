package ai.konudit.rl4j.example.snake;


import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.nd4j.linalg.factory.Nd4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Random;

public class App extends JPanel
{
    private final Game game;
    private DQNPolicy<Game> policy;
    //private ACPolicy<Game> policy;

    public App(){
        game = new Game(12, 12, new Random());
        setDoubleBuffered(true);
        setFocusable(true);
        requestFocus();

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) { }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()){
                    case 37:
                        game.left();
                        break;
                    case 39:
                        game.right();
                        break;
                    case 76:
                        if(policy == null) {
                            try {
                                policy = DQNPolicy.load("snake-player-dqn.bin");
                                //policy = ACPolicy.load("snake-player-a3c-value-10.bin", "snake-player-a3c-policy-10.bin");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        SwingUtilities.invokeLater(() -> autoRun());
                        break;
                    default:
                        game.step();
                }

                SwingUtilities.invokeLater(() -> repaint());
            }

            @Override
            public void keyReleased(KeyEvent e) { }
        });

    }

    public void autoRun(){
        try {
            Integer action = policy.nextAction(Nd4j.expandDims(Nd4j.create(game.toArray()), 0));
            switch (action){
                case 1:
                    game.left();
                    break;
                case 2:
                    game.right();
                    break;
                default:
                    break;
            }
            game.step();
            repaint();
            Thread.sleep(64);
            SwingUtilities.invokeLater(this::autoRun);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);

        String board = game.board();
        int lineHeight = 16;
        int charWidth = 16;
        int y = 0;
        int x = 0;
        for (String tile : board.split("")) {
            if(tile.equals("\n")){
                y += lineHeight;
                x = charWidth;
            }else{
                x += charWidth;
                if(!tile.equals("_")){
                    g.drawString(tile, x, y);
                }
            }
        }
    }

    public static void main(String[] args )
    {
        JFrame frame = new JFrame();
        App app = new App();
        frame.add(app);
        frame.setSize(250, 250);
        frame.setVisible(true);
    }

}
