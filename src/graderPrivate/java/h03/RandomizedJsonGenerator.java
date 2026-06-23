package h03;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fopbot.RobotFamily;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static h03.TestConstants.TEST_ITERATIONS;

/**
 * generates json objects with randomized data to be consumed by the tests
 */
public class RandomizedJsonGenerator {
    public static void main(String[] args) throws IOException {
        // generate everything
        Trib.genPushNumbers();
        Connect4.genDropBoard();
        Connect4.genCheckFullBoards();
        for (boolean v : new boolean[]{true, false}) {
            Trib.genCalcNextNumbers(v);
            Trib.genTribonacciNumbers(v);
            Connect4.genCheckWinBoard(v);
        }
    }

    /**
     * A generator for JSON test data.
     */
    @FunctionalInterface
    public interface JsonGenerator {
        /**
         * Generates a JSON object node.
         *
         * @param mapper The object mapper to use.
         * @param index  The index of the object node.
         * @param rnd    The random number generator to use.
         * @return The generated JSON object node.
         */
        ObjectNode generateJson(ObjectMapper mapper, int index, Random rnd);

        /**
         * Generates and saves JSON test data.
         *
         * @param generator The generator to use.
         * @param amount    The amount of test data to generate.
         * @param fileName  The file name to save the test data to.
         * @throws IOException If an I/O error occurs.
         */
        static void generateJsonTestData(final JsonGenerator generator, final int amount, final String fileName) throws IOException {
            final var seed = TestConstants.RANDOM_SEED;
            final var random = new java.util.Random(seed);
            final ObjectMapper mapper = new ObjectMapper();
            final ArrayNode arrayNode = mapper.createArrayNode();
            System.out.println("Generating test data with seed: " + seed);
            for (int i = 0; i < amount; i++) {
                arrayNode.add(generator.generateJson(mapper, i, random));
            }

            // convert `ObjectNode` to pretty-print JSON
            // System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode));

            final var path = Paths.get(
                "src",
                "graderPrivate",
                "resources",
                "h03",
                "generated",
                fileName
            ).toAbsolutePath();
            System.out.printf("Saving to file: %s%n", path);
            final var file = path.toFile();
            boolean ignored = file.getParentFile().mkdirs();
            boolean ignored2 = file.createNewFile();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, arrayNode);
        }
    }

    static class Trib {
        public static void genPushNumbers() throws IOException {
            JsonGenerator.generateJsonTestData(
                (mapper, index, rnd) -> {
                    final ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("value", rnd.nextInt((int) -2e5, (int) 2e5));
                    final List<Integer> input = new ArrayList<>();
                    if (index < 99) {
                        for (int i = 0; i < rnd.nextInt(5, 10); i++) {
                            input.add(rnd.nextInt((int) -2e5, (int) 2e5));
                        }
                    }
                    final ArrayNode inputArrayNode = mapper.createArrayNode();
                    input.forEach(inputArrayNode::add);
                    objectNode.set("array", inputArrayNode);
                    input.add(objectNode.get("value").asInt());
                    final ArrayNode expectedArrayNode = mapper.createArrayNode();
                    input.forEach(expectedArrayNode::add);
                    objectNode.set("expected result", expectedArrayNode);
                    return objectNode;
                },
                TEST_ITERATIONS,
                "trib/push/randomNumbers.json"
            );
        }

        public static void genCalcNextNumbers(final boolean threePositivesOnly) throws IOException {
            JsonGenerator.generateJsonTestData(
                (mapper, index, rnd) -> {
                    final ObjectNode objectNode = mapper.createObjectNode();
                    final List<Integer> input = new ArrayList<>();
                    for (int i = 0; i < (threePositivesOnly ? 3 : rnd.nextInt(5, 10)); i++) {
                        input.add(rnd.nextInt(
                            threePositivesOnly ? 0 : (int) -2e5,
                            (int) 2e5
                        ));
                    }
                    final ArrayNode inputArrayNode = mapper.createArrayNode();
                    input.forEach(inputArrayNode::add);
                    objectNode.set("array", inputArrayNode);
                    final int nextFibonacci = Stream.of(1, 2, 3).mapToInt(i -> input.get(input.size() - i)).sum();
                    input.add(nextFibonacci);
                    final ArrayNode expectedArrayNode = mapper.createArrayNode();
                    input.forEach(expectedArrayNode::add);
                    objectNode.set("expected result", expectedArrayNode);
                    return objectNode;
                },
                TEST_ITERATIONS,
                "trib/calcNext/randomNumbers" + (threePositivesOnly ? "Positive" : "") + ".json"
            );
        }

        public static void genTribonacciNumbers(final boolean smallerThanThree) throws IOException {
            JsonGenerator.generateJsonTestData(
                (mapper, index, rnd) -> {
                    final int startIdx = smallerThanThree ? index : index + 3;
                    final var trib = Stream.iterate(new int[]{0, 1, 1}, a -> new int[]{a[1], a[2], a[0] + a[1] + a[2]}).limit(startIdx + 1).mapToInt(a -> a[0]).toArray();
                    final ObjectNode objectNode = mapper.createObjectNode();
                    objectNode.put("n", startIdx);
                    objectNode.put("expected result", trib[startIdx]);
                    final ArrayNode refArrayNode = mapper.createArrayNode();
                    for (int i = 0; i <= startIdx; i++) {
                        refArrayNode.add(trib[i]);
                    }
                    objectNode.set("reference array", refArrayNode);
                    return objectNode;
                }, smallerThanThree ? 3 : TEST_ITERATIONS, "trib/tribonacci/randomNumbers" + (smallerThanThree ? "Small" : "") + ".json");
        }
    }

    static class Connect4 {
        public static void genDropBoard() throws IOException {
            JsonGenerator.generateJsonTestData(
                (mapper, index, rnd) -> {
                    int worldHeight = rnd.nextInt(5, 10);
                    int worldWidth = rnd.nextInt(5, 10);
                    // SQUARE_RED <-> true, SQUARE_BLUE <-> false, null <-> null
                    RobotFamily[][] gameBoard = new RobotFamily[worldHeight][worldWidth];
                    List<Integer> numSteps = new ArrayList<>(worldWidth);
                    for (int col = 0; col < worldWidth; col++) {
                        int rowsToFill = rnd.nextInt(worldHeight);
                        for (int row = 0; row <= rowsToFill; row++) {
                            gameBoard[row][col] = rnd.nextBoolean() ? RobotFamily.SQUARE_RED : RobotFamily.SQUARE_BLUE;
                        }
                        numSteps.add(worldHeight - rowsToFill - 2);
                    }

                    ArrayNode stepsNode = mapper.createArrayNode();
                    numSteps.stream()
                        .map(mapper.getNodeFactory()::numberNode)
                        .forEach(stepsNode::add);

                    ArrayNode gameBoardNode = mapper.createArrayNode();
                    Arrays.stream(gameBoard)
                        .map(gameBoardRow -> Arrays.stream(gameBoardRow)
                            .map(rf -> mapper.getNodeFactory().textNode(rf != null ? rf.getName() : null))
                            .toList())
                        .forEach(gameBoardRow -> gameBoardNode.add(mapper.createArrayNode().addAll(gameBoardRow)));

                    ObjectNode objectNode = mapper.createObjectNode()
                        .put("worldHeight", worldHeight)
                        .put("worldWidth", worldWidth);
                    objectNode.set("numSteps", stepsNode);
                    objectNode.set("gameBoard", gameBoardNode);
                    return objectNode;
                },
                TEST_ITERATIONS,
                "connect4/drop/board.json"
            );
        }

        public static void genCheckWinBoard(boolean horizontal) throws IOException {
            JsonGenerator.generateJsonTestData(
                (mapper, index, rnd) -> {
                    int worldHeight = rnd.nextInt(5, 10);
                    int worldWidth = rnd.nextInt(5, 10);
                    RobotFamily currentPlayer = rnd.nextBoolean() ? RobotFamily.SQUARE_RED : RobotFamily.SQUARE_BLUE;
                    ObjectNode objectNode = mapper.createObjectNode()
                        .put("worldHeight", worldHeight)
                        .put("worldWidth", worldWidth)
                        .put("currentPlayer", currentPlayer.getName());

                    ArrayNode winningCoordinates = mapper.createArrayNode();
                    rnd.ints(rnd.nextInt(3), 0, horizontal ? worldHeight : worldWidth)
                        .distinct()
                        .forEach(i -> {
                            if (horizontal) {
                                winningCoordinates.add(mapper.createObjectNode()
                                    .put("x", rnd.nextInt(worldWidth - 4 + 1))
                                    .put("y", i));
                            } else {
                                winningCoordinates.add(mapper.createObjectNode()
                                    .put("x", i)
                                    .put("y", rnd.nextInt(worldHeight - 4 + 1)));
                            }
                        });
                    objectNode.set(horizontal ? "winningRowCoordinates" : "winningColCoordinates", winningCoordinates);

                    RobotFamily[][] gameBoard = new RobotFamily[worldHeight][worldWidth];
                    for (JsonNode node : winningCoordinates) {
                        for (int offset = 0; offset < 4; offset++) {
                            if (horizontal) {
                                gameBoard[node.get("y").intValue()][node.get("x").intValue() + offset] = currentPlayer;
                            } else {
                                gameBoard[node.get("y").intValue() + offset][node.get("x").intValue()] = currentPlayer;
                            }
                        }
                    }

                    ArrayNode gameBoardNode = mapper.createArrayNode();
                    Arrays.stream(gameBoard)
                        .map(gameBoardRow -> Arrays.stream(gameBoardRow)
                            .map(rf -> mapper.getNodeFactory().textNode(rf != null ? rf.getName() : null))
                            .toList())
                        .forEach(gameBoardRow -> gameBoardNode.add(mapper.createArrayNode().addAll(gameBoardRow)));
                    objectNode.set("gameBoard", gameBoardNode);

                    return objectNode;
                },
                TEST_ITERATIONS,
                "connect4/checkWin/" + (horizontal ? "horizontal" : "vertical") + ".json"
            );
        }

        public static void genCheckFullBoards() throws IOException {
            JsonGenerator.generateJsonTestData((mapper, index, rnd) -> {
                int worldHeight = rnd.nextInt(1, 10);
                int worldWidth = rnd.nextInt(1, 10);
                ObjectNode objectNode = mapper.createObjectNode().put("worldHeight", worldHeight).put("worldWidth", worldWidth);
                RobotFamily[][] gameBoard = new RobotFamily[worldHeight][worldWidth];
                for (int col = 0; col < worldWidth; col++) {
                    int rowsToFill = rnd.nextInt(worldHeight);
                    for (int row = 0; row <= rowsToFill; row++) {
                        gameBoard[row][col] = rnd.nextBoolean() ? RobotFamily.SQUARE_RED : RobotFamily.SQUARE_BLUE;
                    }
                }
                // make sure board is not full by setting a random topmost entry to null
                int col = rnd.nextInt(worldWidth);
                gameBoard[worldHeight - 1][col] = null;
                // serialize
                ArrayNode boardNode = mapper.createArrayNode();
                for (RobotFamily[] row : gameBoard) {
                    ArrayNode rowNode = mapper.createArrayNode();
                    for (RobotFamily r : row) {
                        if (r == null) rowNode.addNull();
                        else rowNode.add(r.getName());
                    }
                    boardNode.add(rowNode);
                }
                objectNode.set("gameBoard", boardNode);
                ArrayNode fullNode = mapper.createArrayNode();
                for (RobotFamily[] row : gameBoard) {
                    ArrayNode rowNode = mapper.createArrayNode();
                    for (RobotFamily r : row) {
                        if (r == null) r = rnd.nextBoolean() ? RobotFamily.SQUARE_RED : RobotFamily.SQUARE_BLUE;
                        rowNode.add(r.getName());
                    }
                    fullNode.add(rowNode);
                }
                objectNode.set("fullBoard", fullNode);
                return objectNode;
            }, TEST_ITERATIONS, "connect4/loop/boardFull.json");
        }
    }
}
