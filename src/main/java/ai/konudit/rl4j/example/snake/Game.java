package ai.konudit.rl4j.example.snake;

import org.deeplearning4j.rl4j.space.Encodable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;
import java.util.Random;

public class Game implements Encodable {
    private final Random random;
    private final double maxDistance;
    public static final int radius = 3;
    private Tile[][] board;
    private int[] head;
    private int[] tail;

    private boolean gameOver = false;

    private int length = 0;

    private Game(Random random, double maxDistance, Tile[][] board, int[] head, int[] tail, boolean gameOver, int length) {
        this.random = random;
        this.maxDistance = maxDistance;
        this.board = board;
        this.head = head;
        this.tail = tail;
        this.gameOver = gameOver;
        this.length = length;
    }
    @Override
    public Encodable dup() {
        return new Game(random, maxDistance, Arrays.copyOf(board, board.length), Arrays.copyOf(head, head.length), Arrays.copyOf(tail, tail.length), gameOver, length);
    }

    @Override
    public boolean isSkipped() {
        return false;
    }


    public Game(int width, int height, Random random){
        this.random = random;

        board = new Tile[width][height];
        final SnakeCell cell = new SnakeCell(SnakeCell.Direction.random(random));
        final int x = random.nextInt(width);
        final int y = random.nextInt(height);
        head = tail = new int[]{x, y};
        board[x][y] = cell;

        maxDistance = distance(new int[]{0, 0}, new int[]{width, height});

        placeRandomApple();
    }

    private void placeRandomApple() {
        Tile occupied = null;
        do {
            final int x = random.nextInt(board.length);
            final int y = random.nextInt(board[0].length);
            occupied = board[x][y];
            if(occupied == null){
                board[x][y] = new Apple();
                break;
            }
        } while(true);
    }

    private SnakeCell head() {
        return (SnakeCell) board[head[0]][head[1]];
    }

    private SnakeCell tail() {
        return (SnakeCell) board[tail[0]][tail[1]];
    }

    public Game left(){
        head().turnLeft();
        return this;
    }


    public Game right(){
        head().turnRight();
        return this;
    }

    public boolean isGameOver(){
        return gameOver;
    }

    public Game step(){
        if(isGameOver()){ throw new IllegalStateException("Can not continue playing game that is over!"); }

        final SnakeCell.Direction headDirection = head().getDirection();
        final int[] next = headDirection.apply(head);
        if(isOutOfBounds(next)){
            // out of bounds, game over
            gameOver = true;
        }else {
            final Tile nextTile = board[next[0]][next[1]];
            if (nextTile == null) {
                // empty space, move
                int[] oldTail = this.tail;
                final SnakeCell tail = tail();
                board[next[0]][next[1]] = tail;
                this.tail = tail.getDirection().apply(this.tail);
                tail.setDirection(headDirection);
                head = next;
                board[oldTail[0]][oldTail[1]] = null;
            } else if (nextTile instanceof Apple) {
                // found an apple, grow
                board[next[0]][next[1]] = new SnakeCell(headDirection);
                head = next;
                placeRandomApple();
                length++;
            } else if (nextTile instanceof SnakeCell){
                // ran into itself, game over
                gameOver = true;
            }
        }

        return this;
    }

    private boolean isOutOfBounds(int[] next) {
        return next[0] >= board.length || next[1] >= board[0].length || next[0] < 0 || next[1] < 0;
    }

    private boolean isSnake(int[] next){
        final Tile nextTile = board[next[0]][next[1]];
        return nextTile instanceof SnakeCell;
    }

    public String board() {
        final StringBuilder builder = new StringBuilder("\n");
        for (int y = 0; y < board[0].length; y++){
            for (int x = 0; x < board.length; x++) {
                final Tile tile = board[x][y];
                if(tile == null){ builder.append("_"); }
                else { builder.append(tile.toString()); }
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    public int snakeLength(){
        return length;
    }

    @Override
    public String toString() {
        return "Game{" +
                "length=" + length +
                "random=" + random +
                ", board=" + board() +
                ", head=" + Arrays.toString(head) +
                ", tail=" + Arrays.toString(tail) +
                ", gameOver=" + gameOver +
                '}';
    }

    /*@Override
    public double[] toArray() {
        double[] observation = new double[3];

        int[] front;
        int[] left;
        int[] right;

        switch (head().getDirection()){
            case NORTH:
                front = NORTH.apply(head);
                left = WEST.apply(head);
                right = EAST.apply(head);
                break;
            case SOUTH:
                front = SOUTH.apply(head);
                left = EAST.apply(head);
                right = WEST.apply(head);
                break;
            case EAST:
                front = EAST.apply(head);
                left = NORTH.apply(head);
                right = SOUTH.apply(head);
                break;
            case WEST:
                front = WEST.apply(head);
                left = SOUTH.apply(head);
                right = EAST.apply(head);
                break;
            default:
                front = left = right = new int[]{-1, -1};
                break;
        }


        observation[0] = isOutOfBounds(front) || isSnake(front) ? -1 : appleAroma(front);
        observation[1] = isOutOfBounds(left) || isSnake(left) ? -1 : appleAroma(left);
        observation[2] = isOutOfBounds(right) || isSnake(right) ? -1 : appleAroma(right);

        return observation;
    }*/

    public static int observationSize(){
        return 2*radius*(radius + 1) + 4;
    }

    @Override
    public double[] toArray() {
        double[] observation = new double[observationSize()];
        observation[head().getDirection().ordinal()] = 1.0;


        double baseAroma = appleAroma(head);
        int i = 4;
        for(int x = -radius; x <= radius; x++){
            for(int y = -radius; y <= radius; y++){
                if(x == y && y == 0) continue;
                if(Math.abs(x) + Math.abs(y) > radius) continue;

                int[] position = {head[0] + x, head[1] + y};
                observation[i++] = isOutOfBounds(position) || isSnake(position) ? -1 : (appleAroma(position) - baseAroma);
            }
        }

        return observation;
    }
    @Override
    public INDArray getData() {
        return Nd4j.create(toArray());
    }



    private double distance(int[] pos, int[] apple){
        return Math.sqrt(Math.pow(pos[0] - apple[0], 2) + Math.pow(pos[1] - apple[1], 2));
    }

    private double appleAroma(int[] pos) {
        double appleAroma = 0;

        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                if(board[x][y] instanceof Apple){
                    appleAroma += 1 - (distance(pos, new int[]{x, y}) / (maxDistance));
                }
            }
        }

        return appleAroma;
    }


}
