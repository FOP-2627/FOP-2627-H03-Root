package h03;

import fopbot.*;
import h03.template.ConnectFourInputHandler;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.annotation.SkipAfterFirstFailedTest;
import org.tudalgo.algoutils.tutor.general.assertions.Assertions2;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.assertions.PreCommentSupplier;
import org.tudalgo.algoutils.tutor.general.assertions.ResultOfObject;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLoop;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static h03.TestUtils.getCtMethod;
import static h03.TestUtils.iterateMethodStatements;
import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
@Timeout(
    value = TestConstants.TEST_TIMEOUT_IN_SECONDS,
    unit = TimeUnit.SECONDS,
    threadMode = Timeout.ThreadMode.SEPARATE_THREAD
)
@SkipAfterFirstFailedTest(TestConstants.SKIP_AFTER_FIRST_FAILED_TEST)
public class ConnectFourTest {

    private static final Consumer<Iterator<CtElement>> SINGLE_LOOP_VA = iterator -> {
        int loopStatements = 0;

        while (iterator.hasNext()) {
            if (iterator.next() instanceof CtLoop) {
                loopStatements++;
            }
        }
        assertEquals(1, loopStatements, emptyContext(), result -> "Method does not use exactly one loop");
    };

    private int worldHeight;
    private int worldWidth;
    private RobotFamily[][] stones;
    private RobotFamily currentPlayer;
    private Context.Builder<?> baseContextBuilder;

    private void setup(JsonParameterSet params) {
        worldHeight = params.getInt("worldHeight");
        worldWidth = params.getInt("worldWidth");
        List<List<String>> paramStones = params.get("gameBoard");
        stones = new RobotFamily[worldHeight][worldWidth];
        for (int row = 0; row < worldHeight; row++) {
            for (int col = 0; col < worldWidth; col++) {
                stones[row][col] = robotFamilyLookup(paramStones.get(row).get(col));
            }
        }
        baseContextBuilder = contextBuilder()
            .add("world height", worldHeight)
            .add("world width", worldWidth)
            .add("stones", stones);
        if (params.availableKeys().contains("currentPlayer")) {
            currentPlayer = robotFamilyLookup(params.get("currentPlayer"));
            baseContextBuilder.add("currentPlayer", currentPlayer);
        }

        World.setSize(worldWidth, worldHeight);
        World.setDelay(0);
    }

    private static RobotFamily robotFamilyLookup(String robotFamilyName) {
        if (robotFamilyName == null) {
            return null;
        }

        return switch (robotFamilyName) {
            case "SQUARE_RED" -> RobotFamily.SQUARE_RED;
            case "SQUARE_BLUE" -> RobotFamily.SQUARE_BLUE;
            default -> null;
        };
    }

    @Nested
    class Validate {
        public void testValidateInput(final JsonParameterSet params) {
            // get params
            final int paramColumn = params.getInt("column");
            final int paramWidth = params.getInt("width");
            final int paramHeight = params.getInt("height");
            final List<List<String>> paramStones = params.get("stones");
            final boolean expectedResult = params.getBoolean("expected result");

            // write params to context
            final var ParamsContext = params.toContext("expected result");
            final var cb = contextBuilder()
                .add(ParamsContext)
                .add("Method", "validateInput");

            // init the world size
            World.setSize(paramWidth, paramHeight);


            // parse array and calculate result
            RobotFamily[][] paramStonesArray = paramStones.stream()
                .map(innerList -> innerList.stream()
                    .map(str -> "EMPTY".equals(str) ? null : "SQUARE_RED".equals(str) ? RobotFamily.SQUARE_RED :
                                                             RobotFamily.SQUARE_BLUE)
                    .toArray(RobotFamily[]::new))
                .toArray(RobotFamily[][]::new);


            final boolean actualResult = Assertions2.callObject(
                () -> ConnectFour.validateInput(
                    paramStonesArray,
                    paramColumn
                ),
                cb.build(),
                r -> "An error occurred during execution."
            );

            // validate result
            Assertions2.assertEquals(
                expectedResult,
                actualResult,
                cb.build(),
                r -> "Invalid result."
            );
        }

        // TODO: there has to be a way to deduplicate this

        @ParameterizedTest
        @JsonParameterSetTest(value = "static/connect4/validate/input.json")
        public void edgeCases(final JsonParameterSet params) {
            testValidateInput(params);
        }

        @ParameterizedTest
        @JsonParameterSetTest(value = "generated/connect4/validate/randomCases.json")
        public void randomCases(final JsonParameterSet params) {
            testValidateInput(params);
        }
    }

    @Nested
    class Drop {
        final String randomBoard = "generated/connect4/drop/board.json";

        @Nested
        class NumSteps {
            private void testGetDestinationRow(JsonParameterSet params, boolean testFreeSlots) {
                setup(params);
                List<Integer> firstFreeIndex = params.get("firstFreeIndex");

                for (int i = 0; i < firstFreeIndex.size(); i++) {
                    int index = firstFreeIndex.get(i);
                    if ((testFreeSlots && index >= worldHeight) || (!testFreeSlots && index < worldHeight)) {
                        continue;
                    }

                    final int column = i;
                    int expected = testFreeSlots ? index : -1;
                    Context context = baseContextBuilder
                        .add("column", column)
                        .build();
                    int actual = callObject(() -> ConnectFour.getNumberOfSteps(stones, column), context, result ->
                        "An exception occurred while invoking method getDestinationRow");
                    assertEquals(expected, actual, context, result ->
                        "Method getDestinationRow returned an incorrect value");
                }
            }

            @ParameterizedTest
            @JsonParameterSetTest(randomBoard)
            public void freeSlot(JsonParameterSet params) {
                testGetDestinationRow(params, true);
            }

            @ParameterizedTest
            @JsonParameterSetTest(randomBoard)
            public void blockedSlot(JsonParameterSet params) {
                testGetDestinationRow(params, false);
            }

            @Test
            public void requirements() {
                iterateMethodStatements(ConnectFour.class,
                    "getDestinationRow",
                    new Class[]{int.class, RobotFamily[][].class},
                    SINGLE_LOOP_VA);
            }
        }

        @Nested
        class DropStone {
            @ParameterizedTest
            @JsonParameterSetTest(randomBoard)
            public void robotCreation(JsonParameterSet params) {
                setup(params);
                List<Integer> firstFreeIndex = params.get("firstFreeIndex");

                for (int col = 0; col < worldWidth; col++) {
                    if (firstFreeIndex.get(col) >= worldHeight) {
                        continue;
                    }

                    World.getGlobalWorld().reset();  // clear entities
                    RobotFamily currentPlayer = col % 2 == 0 ? RobotFamily.SQUARE_RED : RobotFamily.SQUARE_BLUE;
                    Context context = baseContextBuilder
                        .add("column", col)
                        .add("currentPlayer", currentPlayer)
                        .build();

                    try {
                        final int finalCol = col;
                        call(() -> ConnectFour.dropStone(stones, finalCol, currentPlayer), context, result ->
                            "An exception occurred while invoking method dropStone. Result may be salvageable, continuing...");
                    } catch (Throwable t) {
                        t.printStackTrace(System.err);
                    }

                    List<Robot> robots = World.getGlobalWorld()
                        .getAllFieldEntities()
                        .stream()
                        .filter(fieldEntity -> fieldEntity instanceof Robot)
                        .map(fieldEntity -> (Robot) fieldEntity)
                        .toList();
                    assertEquals(1, robots.size(), context, result ->
                        "Unexpected number of robots in world");

                    RobotTrace trace = World.getGlobalWorld().getTrace(robots.getFirst());
                    Robot robot = trace.getTransitions().getFirst().robot;
                    assertEquals(col, robot.getX(), context, result ->
                        "Robot was initialized with incorrect x coordinate");
                    assertEquals(worldHeight - 1, robot.getY(), context, result ->
                        "Robot was initialized with incorrect y coordinate");
                    assertEquals(Direction.DOWN, robot.getDirection(), context, result ->
                        "Robot was initialized with incorrect direction");
                    assertEquals(0, robot.getNumberOfCoins(), context, result ->
                        "Robot was initialized with incorrect number of coins");
                    assertEquals(currentPlayer, robot.getRobotFamily(), context, result ->
                        "Robot was initialized with incorrect robot family");
                }
            }

            @Test
            public void numStepsCall() {
                CtMethod<?> dropStoneCtMethod = getCtMethod(ConnectFour.class, "dropStone", int.class, RobotFamily[][].class, RobotFamily.class);
                CtExecutableReference<?> getDestinationRowCtExecRef = getCtMethod(ConnectFour.class, "getDestinationRow", int.class, RobotFamily[][].class)
                    .getReference();
                Iterator<CtElement> iterator = dropStoneCtMethod.descendantIterator();

                boolean getDestinationRowCalled = false;
                while (!getDestinationRowCalled && iterator.hasNext()) {
                    CtElement ctElement = iterator.next();
                    if (ctElement instanceof CtInvocation<?> ctInvocation) {
                        getDestinationRowCalled = ctInvocation.getExecutable().equals(getDestinationRowCtExecRef);
                    }
                }
                assertTrue(getDestinationRowCalled, emptyContext(), result ->
                    "Method dropStone does not call method getDestinationRow");
            }

            @ParameterizedTest
            @JsonParameterSetTest(randomBoard)
            public void robotMove(JsonParameterSet params) {
                setup(params);
                List<Integer> firstFreeIndex = params.get("firstFreeIndex");

                for (int col = 0; col < worldWidth; col++) {
                    if (firstFreeIndex.get(col) >= worldHeight) {
                        continue;
                    }

                    World.getGlobalWorld().reset();  // clear entities
                    RobotFamily currentPlayer = RobotFamily.SQUARE_RED;
                    Context context = baseContextBuilder
                        .add("column", col)
                        .add("currentPlayer", currentPlayer)
                        .build();

                    try {
                        final int finalCol = col;
                        call(() -> ConnectFour.dropStone(stones, finalCol, currentPlayer), context, result ->
                            "An exception occurred while invoking method dropStone. Result may be salvageable, continuing...");
                    } catch (Throwable t) {
                        t.printStackTrace(System.err);
                    }

                    List<Robot> robots = World.getGlobalWorld()
                        .getAllFieldEntities()
                        .stream()
                        .filter(Robot.class::isInstance)
                        .map(Robot.class::cast)
                        .toList();
                    assertEquals(1, robots.size(), context, result ->
                        "Unexpected number of robots in world");

                    List<Transition> transitions = World.getGlobalWorld().getTrace(robots.getFirst()).getTransitions();
                    int expectedTransitionsSize = worldHeight - 1 - firstFreeIndex.get(col) + 3;
                    for (int i = 0; i < expectedTransitionsSize; i++) {
                        Transition transition = transitions.get(i);
                        final int finalI = i;
                        PreCommentSupplier<ResultOfObject<Transition.RobotAction>> preCommentSupplier = result ->
                            "Robot did not perform the expected action (action number %d)".formatted(finalI);
                        if (i < expectedTransitionsSize - 3) {  // moving
                            assertEquals(Transition.RobotAction.MOVE, transition.action, context, preCommentSupplier);
                        } else if (i < expectedTransitionsSize - 1) {  // left turns
                            assertEquals(Transition.RobotAction.TURN_LEFT, transition.action, context, preCommentSupplier);
                        } else {  // last action (none)
                            assertEquals(Transition.RobotAction.NONE, transition.action, context, preCommentSupplier);
                        }
                    }
                }
            }

            @Test
            public void requirements() {
                iterateMethodStatements(ConnectFour.class,
                    "dropStone",
                    new Class[]{int.class, RobotFamily[][].class, RobotFamily.class},
                    SINGLE_LOOP_VA);
            }
        }
    }

    @Nested
    class CheckWin {
        private void testWinVAnforderung(String methodName) {
            List<CtLoop> loops = getCtMethod(ConnectFour.class, methodName, RobotFamily[][].class, RobotFamily.class)
                .filterChildren(CtLoop.class::isInstance)
                .list();

            assertEquals(2, loops.size(), emptyContext(), result ->
                "Method %s does not use exactly two loops".formatted(methodName));
            assertTrue(loops.get(0).getBody().equals(loops.get(1).getParent()), emptyContext(), result ->
                "Method %s does not use exactly two nested loops".formatted(methodName));
        }

        @Nested
        class InLine {
            @Test
            public void check() {
                fail(emptyContext(), r -> "TODO: write test");
            }

            @Test
            public void requirements() {
                fail(emptyContext(), r -> "TODO: write test");
            }
        }

        @Nested
        class Horizontal {
            @ParameterizedTest
            @JsonParameterSetTest("generated/connect4/checkWin/horizontal.json")
            public void goodInput(JsonParameterSet params) {
                setup(params);
                List<Map<String, Integer>> winningRowCoordinates = params.get("winningRowCoordinates");
                Context context = baseContextBuilder.build();
                boolean expected = !winningRowCoordinates.isEmpty();
                boolean actual = callObject(() -> ConnectFour.testWinHorizontal(stones, currentPlayer), context, result ->
                    "An exception occurred while invoking method testWinHorizontal");
                assertEquals(expected, actual, context, result ->
                    "Method testWinHorizontal did not return the correct value");
            }

            // TODO: check requirements
            @Test
            public void inLineCall() {
                testWinVAnforderung("testWinHorizontal");
            }

            @Test
            public void badInput() {
                int worldHeight = 5;
                int worldWidth = 5;
                RobotFamily[][] stones = new RobotFamily[worldHeight][worldWidth];
                for (int row = 0; row < 4; row++) {
                    stones[row][0] = RobotFamily.SQUARE_RED;
                }
                RobotFamily currentPlayer = RobotFamily.SQUARE_RED;
                Context context = contextBuilder()
                    .add("world height", worldHeight)
                    .add("world width", worldWidth)
                    .add("stones", stones)
                    .add("currentPlayer", currentPlayer)
                    .build();

                World.setSize(worldWidth, worldHeight);
                assertCallFalse(() -> ConnectFour.testWinHorizontal(stones, currentPlayer), context, result ->
                    "Method testWinHorizontal returned an incorrect value");
            }
        }

        @Nested
        class Vertical {
            @ParameterizedTest
            @JsonParameterSetTest("generated/connect4/checkWin/vertical.json")
            public void goodInput(JsonParameterSet params) {
                setup(params);
                List<Map<String, Integer>> winningColCoordinates = params.get("winningColCoordinates");
                Context context = baseContextBuilder.build();

                boolean expected = !winningColCoordinates.isEmpty();
                boolean actual = callObject(() -> ConnectFour.testWinVertical(stones, currentPlayer), context, result ->
                    "An exception occurred while invoking method testWinVertical");
                assertEquals(expected, actual, context, result ->
                    "Method testWinVertical did not return the correct value");
            }

            // TODO: what's up with this test
            @Test
            public void testTestWinVerticalVAnforderung1() {
                testWinVAnforderung("testWinVertical");
            }

            @Test
            public void badInput() {
                int worldHeight = 5;
                int worldWidth = 5;
                RobotFamily[][] stones = new RobotFamily[worldHeight][worldWidth];
                for (int col = 0; col < 4; col++) {
                    stones[0][col] = RobotFamily.SQUARE_RED;
                }
                RobotFamily currentPlayer = RobotFamily.SQUARE_RED;
                Context context = contextBuilder()
                    .add("world height", worldHeight)
                    .add("world width", worldWidth)
                    .add("stones", stones)
                    .add("currentPlayer", currentPlayer)
                    .build();

                World.setSize(worldWidth, worldHeight);
                assertCallFalse(() -> ConnectFour.testWinVertical(stones, currentPlayer), context, result ->
                    "Method testWinVertical returned an incorrect value");
            }
        }

        @Nested
        class Conditions {
            /**
             * Tests {@link ConnectFour#testWinConditions(RobotFamily[][], RobotFamily)}.
             * The parameter {@code flags} is used to determine the return value of the win condition methods,
             * where a set bit is interpreted as {@code true} and an unset bit as {@code false}.
             * Only the first three bits are evaluated and correspond to the methods as follows:
             * <ul>
             *     <li>Bit 0: {@link ConnectFour#testWinHorizontal(RobotFamily[][], RobotFamily)}</li>
             *     <li>Bit 1: {@link ConnectFour#testWinVertical(RobotFamily[][], RobotFamily)}</li>
             *     <li>Bit 2: {@link ConnectFour#testWinDiagonal(RobotFamily[][], RobotFamily)}</li>
             * </ul>
             *
             * @param flags the flags (2:0, 31:3 are unused)
             * @throws ReflectiveOperationException if methods testWinHorizontal, testWinVertical or testWinDiagonal are not found
             */
            @ParameterizedTest
            @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
            public void check(int flags) throws ReflectiveOperationException {
                Method testWinHorizontalMethod = ConnectFour.class.getDeclaredMethod("testWinHorizontal", RobotFamily[][].class, RobotFamily.class);
                Method testWinVerticalMethod = ConnectFour.class.getDeclaredMethod("testWinVertical", RobotFamily[][].class, RobotFamily.class);
                Method testWinDiagonalMethod = ConnectFour.class.getDeclaredMethod("testWinDiagonal", RobotFamily[][].class, RobotFamily.class);
                Answer<?> answer = invocation -> {
                    if (invocation.getMethod().equals(testWinHorizontalMethod)) {
                        return (flags & 1) == 1;
                    } else if (invocation.getMethod().equals(testWinVerticalMethod)) {
                        return (flags >> 1 & 1) == 1;
                    } else if (invocation.getMethod().equals(testWinDiagonalMethod)) {
                        return (flags >> 2 & 1) == 1;
                    } else {
                        return invocation.callRealMethod();
                    }
                };
                int worldHeight = 5;
                int worldWidth = 5;
                RobotFamily[][] stones = new RobotFamily[worldHeight][worldWidth];
                RobotFamily currentPlayer = RobotFamily.SQUARE_RED;
                Context context = contextBuilder()
                    .add("world height", worldHeight)
                    .add("world width", worldWidth)
                    .add("stones (ignored)", stones)
                    .add("currentPlayer (ignored)", currentPlayer.getName())
                    .add("testWinHorizontal (mocked) return value", (flags & 1) == 1)
                    .add("testWinVertical (mocked) return value", (flags >> 1 & 1) == 1)
                    .add("testWinDiagonal (mocked) return value", (flags >> 2 & 1) == 1)
                    .build();

                World.setSize(worldWidth, worldHeight);
                try (MockedStatic<ConnectFour> mock = Mockito.mockStatic(ConnectFour.class, answer)) {
                    assertCallEquals(flags != 0, () -> ConnectFour.testWinConditions(stones, currentPlayer), context, result ->
                        "Method testWinConditions did not return the correct value");
                }
            }

            @Test
            public void requirements() {
                iterateMethodStatements(ConnectFour.class, "testWinConditions", new Class[]{RobotFamily[][].class, RobotFamily.class}, iterator -> {
                    boolean callsTestWinHorizontal = false;
                    boolean callsTestWinVertical = false;
                    boolean callsTestWinDiagonal = false;

                    while (iterator.hasNext()) {
                        if (iterator.next() instanceof CtInvocation<?> ctInvocation) {
                            if (ctInvocation.getExecutable().getSimpleName().equals("testWinHorizontal")) {
                                callsTestWinHorizontal = true;
                            } else if (ctInvocation.getExecutable().getSimpleName().equals("testWinVertical")) {
                                callsTestWinVertical = true;
                            } else if (ctInvocation.getExecutable().getSimpleName().equals("testWinDiagonal")) {
                                callsTestWinDiagonal = true;
                            }
                        }
                    }

                    assertTrue(callsTestWinHorizontal, emptyContext(), result -> "Method testWinConditions did not call testWinHorizontal");
                    assertTrue(callsTestWinVertical, emptyContext(), result -> "Method testWinConditions did not call testWinVertical");
                    assertTrue(callsTestWinDiagonal, emptyContext(), result -> "Method testWinConditions did not call testWinDiagonal");
                });
            }
        }
    }

    @Nested
    class Loop {
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void nextPlayer(boolean isRedPlayer) {
            RobotFamily currentPlayer = isRedPlayer ? RobotFamily.SQUARE_RED : RobotFamily.SQUARE_BLUE;
            Context context = contextBuilder()
                .add("currentPlayer", currentPlayer)
                .build();

            RobotFamily expected = isRedPlayer ? RobotFamily.SQUARE_BLUE : RobotFamily.SQUARE_RED;
            assertCallEquals(expected, () -> ConnectFour.nextPlayer(currentPlayer), context, result ->
                "Method nextPlayer did not return the correct value");
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        public void colorField(boolean isRedPlayer) {
            int worldHeight = 5;
            int worldWidth = 5;
            RobotFamily winner = isRedPlayer ? RobotFamily.SQUARE_RED : RobotFamily.SQUARE_BLUE;
            Context context = contextBuilder()
                .add("world height", worldHeight)
                .add("world width", worldWidth)
                .add("winner", winner)
                .build();

            World.setSize(worldWidth, worldHeight);
            World.setDelay(0);
            call(() -> ConnectFour.colorFieldBackground(winner), context, result ->
                "An exception occurred while invoking colorFieldBackground");
            for (int row = 0; row < worldHeight; row++) {
                for (int col = 0; col < worldWidth; col++) {
                    int finalRow = row;
                    int finalCol = col;
                    assertEquals(winner.getColor(), World.getGlobalWorld().getFieldColor(col, row), context, result ->
                        "Color of field at row %d, column %d is incorrect".formatted(finalRow, finalCol));
                }
            }
        }

        @Test
        public void writeMessage() {
            PrintStream originalOut = System.out;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream, true);
            System.setOut(printStream);

            int worldHeight = 5;
            int worldWidth = 5;
            ConnectFour ConnectFourInstance = new ConnectFour(worldWidth, worldHeight, false, 0);
            Context.Builder<?> contextBuilder = contextBuilder()
                .add("world height", worldHeight)
                .add("world width", worldWidth);

            World.setSize(worldWidth, worldHeight);
            try {
                contextBuilder.add("method", "writeDrawMessage");
                ConnectFourInstance.writeDrawMessage();
                assertEquals(
                    "No valid columns found. Hence, game ends with a draw.",
                    outputStream.toString().strip(),
                    contextBuilder.build(),
                    result -> "Method did not print the correct string"
                );

                contextBuilder.add("method", "writeWinnerMessage");
                for (RobotFamily winner : new RobotFamily[]{RobotFamily.SQUARE_RED, RobotFamily.SQUARE_BLUE}) {
                    contextBuilder.add("winner", winner);
                    outputStream.reset();
                    ConnectFourInstance.writeWinnerMessage(winner);
                    assertEquals(
                        "Player %s wins the game!".formatted(winner),
                        outputStream.toString().strip(),
                        contextBuilder.build(),
                        result -> "Method did not print the correct string"
                    );
                }
            } finally {
                System.setOut(originalOut);
            }
        }

        @Test
        public void gameBoardFull() {
            fail(emptyContext(), r -> "TODO: write test");
        }

        @Nested
        class GameLoop {
            @Test
            public void initialization() {
                fail(emptyContext(), r -> "TODO: write test");
            }

            @Test
            public void nextPlayerCall() {
                List<RobotFamily> nextPlayerArgs = new ArrayList<>();
                Answer<?> answer = invocation -> {
                    Method method = invocation.getMethod();
                    if (method.getName().equals("nextPlayer") && Arrays.equals(method.getParameterTypes(), new Class[]{RobotFamily.class})) {
                        RobotFamily currentPlayer = invocation.getArgument(0);
                        nextPlayerArgs.add(currentPlayer);
                        return currentPlayer == RobotFamily.SQUARE_RED ? RobotFamily.SQUARE_BLUE : RobotFamily.SQUARE_RED;
                    } else if (method.getName().equals("dropStone") && Arrays.equals(method.getParameterTypes(), new Class[]{int.class, RobotFamily[][].class, RobotFamily.class})) {
                        return null;
                    } else if (method.getName().equals("testWinConditions") && Arrays.equals(method.getParameterTypes(), new Class[]{RobotFamily[][].class, RobotFamily.class})) {
                        return nextPlayerArgs.size() >= 5;
                    } else {
                        return invocation.callRealMethod();
                    }
                };
                try (MockedStatic<ConnectFour> mockedStatic = Mockito.mockStatic(ConnectFour.class, answer)) {
                    int worldHeight = 5;
                    int worldWidth = 5;
                    ConnectFour ConnectFour = new ConnectFour(worldWidth, worldHeight, false, 0);
                    ConnectFourInputHandler inputHandler = ConnectFour.getInputHandler();
                    for (int i = 0; i < 10; i++) {
                        inputHandler.addInput(0);
                    }

                    ConnectFour.startGame();
                    for (int i = 0; i < nextPlayerArgs.size(); i++) {
                        Context context = contextBuilder()
                            .add("world height", worldHeight)
                            .add("world width", worldWidth)
                            .add("game loop iteration", i + 1)
                            .build();

                        if (i % 2 == 0) {
                            assertEquals(RobotFamily.SQUARE_BLUE, nextPlayerArgs.get(i), context, result ->
                                "Method nextPlayer was not called with correct parameters");
                        } else {
                            assertEquals(RobotFamily.SQUARE_RED, nextPlayerArgs.get(i), context, result ->
                                "Method nextPlayer was not called with correct parameters");
                        }
                    }
                }
            }

            @Test
            public void dropStoneCall() {
                List<Integer> columnArgs = new ArrayList<>();
                List<RobotFamily[][]> stonesArgs = new ArrayList<>();
                List<RobotFamily> currentPlayerArgs = new ArrayList<>();
                Answer<?> answer = invocation -> {
                    Method method = invocation.getMethod();
                    if (method.getName().equals("nextPlayer") && Arrays.equals(method.getParameterTypes(), new Class[]{RobotFamily.class})) {
                        RobotFamily currentPlayer = invocation.getArgument(0);
                        return currentPlayer == RobotFamily.SQUARE_RED ? RobotFamily.SQUARE_BLUE : RobotFamily.SQUARE_RED;
                    } else if (method.getName().equals("dropStone") && Arrays.equals(method.getParameterTypes(), new Class[]{int.class, RobotFamily[][].class, RobotFamily.class})) {
                        columnArgs.add(invocation.getArgument(0));
                        stonesArgs.add(invocation.getArgument(1));
                        currentPlayerArgs.add(invocation.getArgument(2));
                        return null;
                    } else if (method.getName().equals("testWinConditions") && Arrays.equals(method.getParameterTypes(), new Class[]{RobotFamily[][].class, RobotFamily.class})) {
                        return currentPlayerArgs.size() >= 5;
                    } else {
                        return invocation.callRealMethod();
                    }
                };
                int worldHeight = 5;
                int worldWidth = 5;
                try (MockedStatic<ConnectFour> mockedStatic = Mockito.mockStatic(ConnectFour.class, answer)) {
                    ConnectFour ConnectFour = new ConnectFour(worldWidth, worldHeight, false, 0);
                    ConnectFourInputHandler inputHandler = ConnectFour.getInputHandler();
                    for (int i = 0; i < 10; i++) {
                        inputHandler.addInput(i % worldWidth);
                    }

                    ConnectFour.startGame();
                }

                RobotFamily[][] stones = null;
                for (int i = 0; i < currentPlayerArgs.size(); i++) {
                    if (stones == null) {
                        stones = stonesArgs.get(i);
                    }
                    Context context = contextBuilder()
                        .add("world height", worldHeight)
                        .add("world width", worldWidth)
                        .add("game loop iteration", i + 1)
                        .build();

                    assertEquals(i % worldWidth, columnArgs.get(i), context, result ->
                        "Method dropStone was not called with correct parameter column");
                    assertSame(stones, stonesArgs.get(i), context, result ->
                        "Method dropStone was not called with correct parameter stones");
                    if (i % 2 == 0) {
                        assertEquals(RobotFamily.SQUARE_RED, currentPlayerArgs.get(i), context, result ->
                            "Method dropStone was not called with correct parameter currentPlayer");
                    } else {
                        assertEquals(RobotFamily.SQUARE_BLUE, currentPlayerArgs.get(i), context, result ->
                            "Method dropStone was not called with correct parameter currentPlayer");
                    }
                }
            }

            @Test
            public void conditionCall() {
                List<RobotFamily[][]> stonesArgs = new ArrayList<>();
                List<RobotFamily> currentPlayerArgs = new ArrayList<>();
                Answer<?> answer = invocation -> {
                    Method method = invocation.getMethod();
                    if (method.getName().equals("nextPlayer") && Arrays.equals(method.getParameterTypes(), new Class[]{RobotFamily.class})) {
                        RobotFamily currentPlayer = invocation.getArgument(0);
                        return currentPlayer == RobotFamily.SQUARE_RED ? RobotFamily.SQUARE_BLUE : RobotFamily.SQUARE_RED;
                    } else if (method.getName().equals("dropStone") && Arrays.equals(method.getParameterTypes(), new Class[]{int.class, RobotFamily[][].class, RobotFamily.class})) {
                        return null;
                    } else if (method.getName().equals("testWinConditions") && Arrays.equals(method.getParameterTypes(), new Class[]{RobotFamily[][].class, RobotFamily.class})) {
                        stonesArgs.add(invocation.getArgument(0));
                        currentPlayerArgs.add(invocation.getArgument(1));
                        return currentPlayerArgs.size() >= 5;
                    } else {
                        return invocation.callRealMethod();
                    }
                };
                int worldHeight = 5;
                int worldWidth = 5;
                try (MockedStatic<ConnectFour> mockedStatic = Mockito.mockStatic(ConnectFour.class, answer)) {
                    ConnectFour ConnectFour = new ConnectFour(worldWidth, worldHeight, false, 0);
                    ConnectFourInputHandler inputHandler = ConnectFour.getInputHandler();
                    for (int i = 0; i < 10; i++) {
                        inputHandler.addInput(i % worldWidth);
                    }

                    ConnectFour.startGame();
                }

                RobotFamily[][] stones = null;
                for (int i = 0; i < currentPlayerArgs.size(); i++) {
                    if (stones == null) {
                        stones = stonesArgs.get(i);
                    }
                    Context context = contextBuilder()
                        .add("world height", worldHeight)
                        .add("world width", worldWidth)
                        .add("game loop iteration", i + 1)
                        .build();

                    assertSame(stones, stonesArgs.get(i), context, result ->
                        "Method getWinConditions was not called with correct parameter stones");
                    if (i % 2 == 0) {
                        assertEquals(RobotFamily.SQUARE_RED, currentPlayerArgs.get(i), context, result ->
                            "Method getWinConditions was not called with correct parameter currentPlayer");
                    } else {
                        assertEquals(RobotFamily.SQUARE_BLUE, currentPlayerArgs.get(i), context, result ->
                            "Method getWinConditions was not called with correct parameter currentPlayer");
                    }
                }
            }

            @Test
            public void output() {
                fail(emptyContext(), r -> "TODO: write test");
            }
        }
    }
}
