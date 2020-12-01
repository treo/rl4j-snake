package ai.konudit.rl4j.example.snake;

import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;

import java.util.Random;

public class SnakeGameMdp implements MDP<Game, Integer, DiscreteSpace> {

    private final int width;
    private final int height;
    private final Random r;
    private Game game;
    private ArrayObservationSpace<Game> gameArrayObservationSpace;
    private DiscreteSpace discreteSpace = new DiscreteSpace(3);
    private int age = 0;

    public SnakeGameMdp(int width, int height, Random r) {
        this.width = width;
        this.height = height;
        this.r = r;
        gameArrayObservationSpace = new ArrayObservationSpace<>(new int[]{Game.observationSize()});
    }

    @Override
    public ObservationSpace<Game> getObservationSpace() {
        return gameArrayObservationSpace;
    }

    @Override
    public DiscreteSpace getActionSpace() {
        return discreteSpace;
    }

    @Override
    public Game reset() {
        game = new Game(width, height, r);
        age = 0;
        return game;
    }

    @Override
    public void close() {

    }

    @Override
    public StepReply<Game> step(Integer action) {
        int lengthBefore = game.snakeLength();
        doAction(action);
        int lengthAfter = game.snakeLength();

        age++;
        double reward = -1 / 100.0;
        if(lengthBefore != lengthAfter){
            reward += 1;
            age = 0;
        }

        if(game.isGameOver()){
            reward = -1;
        }

        return new StepReply<>(game, reward, isDone(), null);
    }

    public void doAction(int action){
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
    }

    @Override
    public boolean isDone() {
        return game.isGameOver();
    }

    @Override
    public MDP<Game, Integer, DiscreteSpace> newInstance() {
        return new SnakeGameMdp(width, height, r);
    }
}
