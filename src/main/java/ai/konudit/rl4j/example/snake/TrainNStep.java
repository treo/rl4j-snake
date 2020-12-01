package ai.konudit.rl4j.example.snake;

import org.deeplearning4j.rl4j.learning.async.nstep.discrete.AsyncNStepQLearningDiscrete;
import org.deeplearning4j.rl4j.learning.async.nstep.discrete.AsyncNStepQLearningDiscreteDense;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.nd4j.linalg.learning.config.Adam;

import java.io.IOException;
import java.util.Random;

public class TrainNStep {
    public static void main(String[] args) throws IOException {
        int stepsPerEpoch = 1000;
        int maxGames = 500;

        final int height = 12;
        final int width = 12;

        AsyncNStepQLearningDiscrete.AsyncNStepQLConfiguration NSTEP =
                AsyncNStepQLearningDiscrete.AsyncNStepQLConfiguration.builder()
                        .seed(1)
                        .maxEpochStep(stepsPerEpoch)
                        .maxStep(stepsPerEpoch * maxGames)
                        .numThread(4)
                        .nstep(10)
                        .updateStart(0)
                        .rewardFactor(0.1)
                        .gamma(0.99)
                        .errorClamp(1.0)
                        .epsilonNbStep(9000)
                        .minEpsilon(0f)
                        .targetDqnUpdateFreq(100)
                        .build();

        DQNFactoryStdDense.Configuration NET_NSTEP =
                DQNFactoryStdDense.Configuration.builder()
                        .updater(new Adam(0.001)).numHiddenNodes(100).numLayer(3).build();

        MDP<Game, Integer, DiscreteSpace> mdp = new SnakeGameMdp(width, height, new Random(7));

        AsyncNStepQLearningDiscreteDense<Game> dql = new AsyncNStepQLearningDiscreteDense<Game>(mdp, NET_NSTEP, NSTEP);

        //train
        dql.train();

        //useless on toy but good practice!
        mdp.close();

        ((DQNPolicy<Game>)dql.getPolicy()).save("snake-player-nstep.bin");
    }
}
