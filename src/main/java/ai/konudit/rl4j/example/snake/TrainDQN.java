package ai.konudit.rl4j.example.snake;

import org.deeplearning4j.rl4j.learning.configuration.QLearningConfiguration;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.configuration.DQNDenseNetworkConfiguration;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.nd4j.linalg.learning.config.Nadam;

import java.io.IOException;
import java.util.Random;

public class TrainDQN {
    public static void main(String[] args) throws IOException, InterruptedException {
        int stepsPerEpoch = 1000;
        int maxGames = 500;

        final int height = 12;
        final int width = 12;

        final QLearningConfiguration DQN = QLearningConfiguration.builder()
                .seed(1L)
                .maxEpochStep(stepsPerEpoch)
                .maxStep(stepsPerEpoch * maxGames)
                .updateStart(0)
                .rewardFactor(1.0)
                .gamma(0.999)
                .errorClamp(1.0)
                .batchSize(16)
                .minEpsilon(0.0)
                .epsilonNbStep(128)
                .expRepMaxSize(128 * 16)
                .build();


        final DQNDenseNetworkConfiguration conf = DQNDenseNetworkConfiguration.builder()
                .updater(new Nadam(Math.pow(10, -3.5)))
                .numHiddenNodes(20)
                .numLayers(6)
                .build();

        MDP<Game, Integer, DiscreteSpace> mdp = new SnakeGameMdp(width, height, new Random(7));

        final QLearningDiscreteDense<Game> dqn = new QLearningDiscreteDense<>(mdp, conf, DQN);

        //start the training
        dqn.train();

        //useless on toy but good practice!
        mdp.close();

        dqn.getPolicy().save("snake-player-dqn.bin");
    }
}
