package h03;

import fopbot.Direction;
import fopbot.Robot;
import fopbot.RobotFamily;
import fopbot.World;
import h03.template.ConnectFourInputHandler;
import org.tudalgo.algoutils.student.annotation.DoNotTouch;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

/**
 * The {@link ConnectFour} class represents the main class of the FourWins game.
 */
@SuppressWarnings({"ManualArrayCopy", "ForLoopReplaceableByForEach"})
public class ConnectFour {
    /**
     * The width of the game board.
     */
    private final int width;
    /**
     * The height of the game board.
     */
    private final int height;
    /**
     * Indicates whether the game is currently running.
     */
    private boolean isRunning = false;
    /**
     * The input handler for this exercise. It checks and waits for the next click in the game window.
     * (You do not need to understand its it inner workings)
     */
    private final ConnectFourInputHandler inputHandler = new ConnectFourInputHandler(this);

    /**
     * Creates a new {@link ConnectFour} instance with the given width and height.
     *
     * @param width  the width of the game board
     * @param height the height of the game board
     */
    ConnectFour(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Validates if a given column can accept a new stone. The column is valid if it is within bounds and
     * the top cell of the column is empty (not fully occupied).
     *
     * @param stones 2D array representing the game board, where each cell contains a RobotFamily color indicating the
     *               player that has placed a stone in that position.
     * @param column The column index to validate.
     * @return true if the column is within bounds and has at least one unoccupied cell at the top; false otherwise.
     */
    @StudentImplementationRequired("H3.2.1")
    public static boolean validateInput(final RobotFamily[][] stones, final int column) {
        return column >= 0 && column < World.getWidth() && stones[World.getHeight() - 1][column] == null;
    }

    /**
     * Calculates the number of steps needed for a stone to fall in the specified column.
     * This represents how far down the stone needs to move to reach the first unoccupied slot.
     *
     * @param stones 2D array representing the game board, where each cell contains a RobotFamily object indicating the
     *               player that has placed a stone in that position.
     * @param column The column index where the stone is to be dropped.
     * @return The number of steps the stone needs to fall. Returns -1 if the column is full.
     */
    @StudentImplementationRequired("H3.2.2")
    public static int getNumberOfSteps(final RobotFamily[][] stones, final int column) {
        // Starting at the bottom (row = 0) and going up row++ until a field is empty (== null).
        for (int row = 0; row < stones.length; row++) {
            if (stones[row][column] == null) {
                // This calculates the number directly, but you could count with an incrementor variable just as well.
                return stones.length - 1 - row;
            }
        }
        return -1;
    }

    /**
     * Drops a stone into the specified column of the game board, simulating a falling animation. This method gets the
     * number of steps the stone needs to fall using the {@code getNumberOfSteps} method. It creates a new Robot
     * instance to represent the stone with the currentPlayer's {@link RobotFamily} in the given column at the top row.
     * After that it simulates the stone's fall by moving it down until it reaches its destination row. Once
     * the stone reaches its destination, the method updates the stones array (a 2D array of {@link RobotFamily} colors) to
     * mark the slot as occupied by the currentPlayer.
     *
     * @param stones        2D array representing the game board, where each cell contains a RobotFamily object
     *                      indicating the player that has placed a stone in that position.
     * @param column        The column index where the stone is to be dropped.
     * @param currentPlayer The RobotFamily object representing the current player dropping the stone.
     */
    @StudentImplementationRequired("H3.2.2")
    public static void dropStone(final RobotFamily[][] stones, final int column, final RobotFamily currentPlayer) {
        // spawn stone
        final Robot stone = new Robot(column, World.getHeight() - 1, Direction.DOWN, 0, currentPlayer);

        // let stone fall
        final int nSteps = getNumberOfSteps(stones, column);
        for (int currStep = 0; currStep < nSteps; currStep++) {
            stone.move();
        }

        // turn stone up
        stone.turnLeft();
        stone.turnLeft();

        // set slot as occupied
        final int row = World.getHeight() - 1 - nSteps;
        stones[row][column] = currentPlayer;
    }

    /**
     * Checks if all elements in the given line array represent stones of the current player.
     *
     * @param line          An array of {@link RobotFamily} values representing a line of the game board.
     * @param currentPlayer The {@link RobotFamily} color representing the current player to check for.
     * @return true if all elements in the line match the currentPlayer; false otherwise.
     */
    @StudentImplementationRequired("H3.2.3")
    public static boolean testAllInLine(final RobotFamily[] line, final RobotFamily currentPlayer) {
        for (int i = 0; i < line.length; i++) {
            if (line[i] != currentPlayer) return false;
        }
        return true;
    }

    /**
     * Checks if the current player has won by any condition. The conditions can be a horizontal, vertical, or
     * diagonal line of at least four consecutive stones.
     *
     * @param stones        2D array representing the game board, where each cell contains a RobotFamily color
     *                      indicating the player that has placed a stone in that position.
     * @param currentPlayer The RobotFamily color representing the current player to check for a win.
     * @return true if the current player has formed a line of at least four stones in any direction; false otherwise.
     */
    @StudentImplementationRequired("H3.2.3")
    public static boolean testWinConditions(final RobotFamily[][] stones, final RobotFamily currentPlayer) {
        return testWinVertical(stones, currentPlayer) || testWinHorizontal(stones, currentPlayer) || testWinDiagonal(stones, currentPlayer);
    }

    /**
     * Checks if the current player has won by forming a horizontal line of at least four consecutive stones.
     *
     * @param stones        2D array representing the game board, where each cell contains a RobotFamily color
     *                      indicating the player that has placed a stone in that position.
     * @param currentPlayer The RobotFamily color representing the current player to check for a win.
     * @return true if the current player has formed a horizontal line of at least four consecutive stones; false otherwise.
     */
    @StudentImplementationRequired("H3.2.3")
    public static boolean testWinHorizontal(final RobotFamily[][] stones, final RobotFamily currentPlayer) {
        final RobotFamily[] line = new RobotFamily[4];

        for (int row = 0; row < World.getHeight(); row++) {
            for (int column = 0; column < World.getWidth() - line.length + 1; column++) {
                for (int i = 0; i < line.length; i++) {
                    line[i] = stones[row][column + i];
                }
                if (testAllInLine(line, currentPlayer)) return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current player has won by forming a vertical line of at least four consecutive stones.
     *
     * @param stones        2D array representing the game board, where each cell contains a RobotFamily color
     *                      indicating the player that has placed a stone in that position.
     * @param currentPlayer The RobotFamily color representing the current player to check for a win.
     * @return true if the current player has formed a vertical line of at least four consecutive stones; false otherwise.
     */
    @StudentImplementationRequired("H3.2.3")
    public static boolean testWinVertical(final RobotFamily[][] stones, final RobotFamily currentPlayer) {
        final RobotFamily[] line = new RobotFamily[4];

        for (int column = 0; column < World.getWidth(); column++) {
            for (int row = 0; row < World.getHeight() - line.length + 1; row++) {
                for (int i = 0; i < line.length; i++) {
                    line[i] = stones[row + i][column];
                }
                if (testAllInLine(line, currentPlayer)) return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current player has won by forming a diagonal line of at least consecutive four stones.
     *
     * @param stones        2D array representing the game board, where each cell contains a RobotFamily color
     *                      indicating the player that has placed a stone in that position.
     * @param currentPlayer The RobotFamily color representing the current player to check for a win.
     * @return true if the current player has formed a diagonal line of at least four stones; false otherwise.
     */
    @DoNotTouch
    public static boolean testWinDiagonal(final RobotFamily[][] stones, final RobotFamily currentPlayer) {
        final int MAX_STONES = 4;

        final int WIDTH = World.getWidth();
        final int HEIGHT = World.getHeight();
        int[] direction = new int[]{1, 1};

        // for every field
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {

                // for every direction
                for (int nthDirection = 0; nthDirection < 4; nthDirection++) {
                    final int[] pos = {x, y};

                    // test for consecutive coins
                    int coinCount = 0; // start counting at 0
                    while (pos[0] >= 0 && pos[0] < WIDTH && pos[1] >= 0 && pos[1] < HEIGHT && stones[pos[1]][pos[0]] == currentPlayer) {
                        coinCount++; // count every stone that has currentPlayer's color
                        if (coinCount >= MAX_STONES) {
                            return true;
                        }
                        pos[0] += direction[0];
                        pos[1] += direction[1];
                    }

                    direction = new int[]{direction[1], -direction[0]}; // next direction (rotate by 90 deg)
                }
            }
        }

        return false;
    }

    /**
     * Switches the player for each turn. If the current player is SQUARE_BLUE, SQUARE_RED is returned as the next
     * player. If the current player is SQUARE_RED, SQUARE_BLUE is returned as the next player.
     *
     * @param currentPlayer The player color of the current player.
     * @return The player color of the next player.
     */
    @StudentImplementationRequired("H3.2.4")
    public static RobotFamily nextPlayer(final RobotFamily currentPlayer) {
        return currentPlayer == RobotFamily.SQUARE_BLUE ? RobotFamily.SQUARE_RED : RobotFamily.SQUARE_BLUE;
    }

    /**
     * Displays the winner of the game by filling the entire game board with the color that is representing the winner.
     *
     * @param winner The {@link RobotFamily} color of the winner.
     */
    @StudentImplementationRequired("H3.2.4")
    public static void colorFieldBackground(final RobotFamily winner) {
        for (int x = 0; x < World.getWidth(); x++) {
            for (int y = 0; y < World.getHeight(); y++) {
                setFieldColor(x, y, winner);
            }
        }
    }

    /**
     * Checks if all columns of the game board are fully occupied.
     *
     * @param stones 2D array representing the game board, where each cell contains a RobotFamily
     * @return true if all columns of the game board are fully occupied; false otherwise.
     */
    @StudentImplementationRequired("H3.2.4")
    public static boolean isGameBoardFull(final RobotFamily[][] stones) {
        for (int x = 0; x < World.getWidth(); x++) {
            if (ConnectFour.validateInput(stones, x)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Displays a Message in the console indicating the game is drawn.
     */
    @StudentImplementationRequired("H3.2.4")
    public void writeDrawMessage() {
        System.out.println("No valid columns found. Hence, game ends with a draw.");
    }

    /**
     * Displays a Message in the console indicating the game is won by a player.
     *
     * @param winner {@link RobotFamily} of the winning player
     */
    @StudentImplementationRequired("H3.2.4")
    public void writeWinnerMessage(final RobotFamily winner) {
        System.out.println("Player " + winner + " wins the game!");
    }

    /**
     * Executes the main game loop, handling player turns, stone drops, and win condition checks. This method
     * initializes the game board as a 2D array of RobotFamily colors, representing the slots that can be filled with
     * players' stones. It starts with SQUARE_BLUE as the current player (note: {@code nextPlayer()} is called first,
     * so SQUARE_RED actually goes first). Each iteration of the loop gets player input for a column to drop a stone,
     * switches the current player, drops the stone in the selected column, and checks for win conditions. If a win
     * condition is met or the board is full, the loop ends and the result is displayed.
     */
    @StudentImplementationRequired("H3.2.4")
    void gameLoop() {
        final RobotFamily[][] stones = new RobotFamily[World.getHeight()][World.getWidth()];
        RobotFamily currentPlayer = RobotFamily.SQUARE_BLUE;

        boolean draw = false;

        do {
            currentPlayer = nextPlayer(currentPlayer);

            final int column = waitForNextInput(stones, currentPlayer);
            if (column == -1) draw = true;

            dropStone(stones, column, currentPlayer);
        } while (!draw && !testWinConditions(stones, currentPlayer));

        if (draw) {
            writeDrawMessage();
            colorFieldBackground(RobotFamily.SQUARE_ORANGE);
        } else {
            writeWinnerMessage(currentPlayer);
            colorFieldBackground(currentPlayer);
        }
    }

    /**
     * Starts the game by setting up the world and executing the game loop.
     */
    @DoNotTouch
    void startGame() {
        setupWorld();
        gameLoop();
    }

    /**
     * Stops the game by closing the world and window
     */
    @DoNotTouch
    void stopGame() {
        inputHandler.waitForCloseInput();
        isRunning = false;
        World.setVisible(false);
    }

    /**
     * Sets up the world and installs the {@link ConnectFourInputHandler}.
     */
    @DoNotTouch
    void setupWorld() {
        World.setSize(width, height);
        World.setDelay(10);
        World.setVisible(true);
        inputHandler.install();
        isRunning = true;
    }

    /**
     * Sets the background color of a field at the specified coordinates. The color is derived from the
     * {@link RobotFamily} parameter, such as SQUARE_BLUE or SQUARE_RED.
     *
     * @param x     the x coordinate of the field
     * @param y     the y coordinate of the field
     * @param color the {@link RobotFamily} corresponding to the field color to set
     */
    @DoNotTouch
    public static void setFieldColor(final int x, final int y, final RobotFamily color) {
        World.getGlobalWorld().setFieldColor(x, y, color.getColor());
    }

    @DoNotTouch
    public int waitForNextInput(final RobotFamily[][] stones, final RobotFamily currentPlayer) {
        if (stones == null || currentPlayer == null) {
            throw new NullPointerException("stones and/or currentPlayer cannot be null when calling waitForNextInput");
        }
        return inputHandler.getNextInput(stones, currentPlayer);
    }

    /**
     * Returns this instance's {@link ConnectFourInputHandler}.
     *
     * @return the input handler
     */
    @DoNotTouch
    public ConnectFourInputHandler getInputHandler() {
        return inputHandler;
    }

    /**
     * Returns {@code true} when the game is finished, {@code false} otherwise.
     *
     * @return whether the game is finished.
     */
    @DoNotTouch
    public boolean isRunning() {
        return isRunning;
    }

}
