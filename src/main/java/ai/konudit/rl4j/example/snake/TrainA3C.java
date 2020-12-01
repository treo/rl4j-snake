package ai.konudit.rl4j.example.snake;

import org.deeplearning4j.rl4j.learning.async.a3c.discrete.A3CDiscrete;
import org.deeplearning4j.rl4j.learning.async.a3c.discrete.A3CDiscreteDense;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.ac.ActorCriticFactorySeparateStdDense;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.nd4j.linalg.learning.config.Adam;

import java.io.IOException;
import java.util.Random;

public class TrainA3C {
    public static void main(String[] args) throws IOException, InterruptedException {
        int stepsPerEpoch = 1000;
        int maxGames = 500;

        final int height = 12;
        final int width = 12;

        A3CDiscrete.A3CConfiguration A3C =
                A3CDiscrete.A3CConfiguration.builder()
                        .seed(1)
                        .maxEpochStep(stepsPerEpoch)
                        .maxStep(stepsPerEpoch * maxGames)
                        .numThread(128)
                        .nstep(10)
                        .updateStart(0)
                        .rewardFactor(0.1)
                        .gamma(0.99)
                        .errorClamp(1.0)
                        .build();

        ActorCriticFactorySeparateStdDense.Configuration configuration = ActorCriticFactorySeparateStdDense.Configuration
                .builder().updater(new Adam(Math.pow(10, -3))).l2(0).numHiddenNodes(100).numLayer(3).build();

        MDP<Game, Integer, DiscreteSpace> mdp = new SnakeGameMdp(width, height, new Random(7));

        A3CDiscreteDense<Game> a3c = new A3CDiscreteDense<>(mdp, configuration, A3C);

        //start the training
        a3c.train();

        //useless on toy but good practice!
        mdp.close();

        a3c.getPolicy().save("snake-player-a3c-value-10.bin", "snake-player-a3c-policy-10.bin");
    }
}
