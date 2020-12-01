package ai.konudit.rl4j.example.snake;

import java.util.Arrays;
import java.util.Random;

public class SnakeCell implements Tile {
    enum Direction { NORTH(0, -1), SOUTH(0, 1), EAST(1, 0), WEST(-1, 0);

        private final int x;
        private final int y;

        Direction(int xDir, int yDir) {
            this.x = xDir;
            this.y = yDir;
        }

        public int[] apply(int[] point){
            if(point == null || point.length != 2) throw new IllegalArgumentException("Can not apply to point "+ Arrays.toString(point));

            return new int[]{point[0] + x, point[1] + y};
        }

        public static Direction random(){
            return random(new Random());
        }

        public static Direction random(Random random){
            return Direction.values()[random.nextInt(Direction.values().length)];
        }
    }

    private Direction direction;

    public SnakeCell(Direction direction){
        setDirection(direction);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if(direction == null) throw new IllegalArgumentException("NULL direction is illegal");
        this.direction = direction;
    }

    public void turnLeft(){
        switch (this.direction){
            case NORTH:
                setDirection(Direction.WEST);
                break;
            case SOUTH:
                setDirection(Direction.EAST);
                break;
            case EAST:
                setDirection(Direction.NORTH);
                break;
            case WEST:
                setDirection(Direction.SOUTH);
                break;
        }
    }

    public void turnRight(){
        switch (this.direction){
            case NORTH:
                setDirection(Direction.EAST);
                break;
            case SOUTH:
                setDirection(Direction.WEST);
                break;
            case EAST:
                setDirection(Direction.SOUTH);
                break;
            case WEST:
                setDirection(Direction.NORTH);
                break;
        }
    }

    @Override
    public String toString() {
        switch (this.direction){
            case NORTH:
                return "^";
            case SOUTH:
                return "v";
            case EAST:
                return ">";
            case WEST:
                return "<";
            default:
                return "THIS SHOULD NEVER HAPPEN";
        }
    }
}
