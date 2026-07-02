package h03;

import fopbot.*;
import h03.template.ConnectFourInputHandler;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.stubbing.Answer;
import org.sourcegrade.jagr.api.rubric.TestForSubmission;
import org.tudalgo.algoutils.tutor.general.SpoonUtils;
import org.tudalgo.algoutils.tutor.general.annotation.SkipAfterFirstFailedTest;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.assertions.PreCommentSupplier;
import org.tudalgo.algoutils.tutor.general.assertions.ResultOfObject;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSetTest;
import org.tudalgo.algoutils.tutor.general.match.MatchingUtils;
import org.tudalgo.algoutils.tutor.general.reflections.BasicMethodLink;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.tudalgo.algoutils.tutor.general.assertions.Assertions2.*;

@TestForSubmission
@Timeout(
    value = TestConstants.TEST_TIMEOUT_IN_SECONDS,
    unit = TimeUnit.SECONDS,
    threadMode = Timeout.ThreadMode.SEPARATE_THREAD
)
@SkipAfterFirstFailedTest
public class ConnectFourTest {
    /**
     * Returns the Spoon representation of the given method.
     *
     * @param clazz      the method's owner
     * @param methodName the method name
     * @param paramTypes the method's formal parameter types, if any
     * @return the Spoon representation of the given method
     */
    public static CtMethod<?> getCtMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        var type = SpoonUtils.getType(clazz.getName());
        var params = Arrays.stream(paramTypes)
            .map(x -> type.getFactory().Type().get(x).getReference())
            .toArray(CtTypeReference<?>[]::new);
        return type.getMethod(methodName, params);
    }

    /**
     * Applies the given consumer to the body and its descendants of the given method.
     * See also: {@link #getCtMethod(Class, String, Class[])}.
     *
     * @param clazz      the method's owner
     * @param methodName the method name
     * @param paramTypes the method's formal parameter types, if any
     * @param consumer   the consumer to apply
     */
    public static void iterateMethodStatements(Consumer<Iterator<CtElement>> consumer, Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Iterator<CtElement> iterator = getCtMethod(clazz, methodName, paramTypes)
            .getBody()
            .descendantIterator();
        consumer.accept(iterator);
    }

    private static final Consumer<Iterator<CtElement>> REQUIRE_SINGLE_LOOP = iterator -> {
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
            default -> throw new IllegalArgumentException("unexpected robot family: " + robotFamilyName);
        };
    }

    @Nested
    class Validate {
        // just using random values
        @CartesianTest
        public void cartesianBoards(
            @CartesianTest.Values(ints = {1, 2, 6, 7, 8, 1111}) int height,
            @CartesianTest.Values(ints = {1, 2, 3, 7, 8, 9, 10, 100}) int width,
            @CartesianTest.Values(ints = {-42, -1, 0, 1, 2, 6, 7, 67}) int column,
            @CartesianTest.Values(booleans = {true, false}) boolean fill
        ) {
            // given
            World.setSize(width, height);
            stones = new RobotFamily[height][width];

            if (fill) {
                for (int row = 0; row < height; row++)
                    for (int col = 0; col < width; col++)
                        stones[row][col] = (row + col) % 2 == 0 ? RobotFamily.SQUARE_BLUE : RobotFamily.SQUARE_RED;
            }

            var cb = contextBuilder()
                .add("world height", height)
                .add("world width", width)
                .add("column", column)
                .add("stones", stones);
            // when
            boolean actual = callObject(() -> ConnectFour.validateInput(stones, column), cb.build(), result ->
                "An exception occurred while invoking method validateInput");
            // then
            boolean expected = column >= 0 && column < World.getWidth() && stones[World.getHeight() - 1][column] == null;
            assertEquals(expected, actual, cb.build(), r -> "incorrect value returned");
        }

        // i see no reason to generate a separate board for this test
        @ParameterizedTest
        @JsonParameterSetTest("generated/connect4/drop/board.json")
        public void testWithDropBoard(JsonParameterSet params) {
            // given
            setup(params);

            for(int column = 0; column < World.getWidth(); column++) {
                baseContextBuilder.add("column", column);
                // when
                int finalColumn = column;
                boolean actual = callObject(() -> ConnectFour.validateInput(stones, finalColumn), baseContextBuilder.build(), result ->
                    "An exception occurred while invoking method validateInput");
                // then
                boolean expected = stones[World.getHeight() - 1][column] == null;
                assertEquals(expected, actual, baseContextBuilder.build(), r -> "incorrect value returned");
            }
        }

        @Test
        public void requirements() {
            boolean noLoopInMth = getCtMethod(ConnectFour.class, "validateInput", RobotFamily[][].class, int.class)
                .getBody()
                .getStatements()
                .stream()
                .noneMatch(CtLoop.class::isInstance);
            assertTrue(noLoopInMth, emptyContext(), result -> "Method contains a loop.");
        }
    }

    @Nested
    class Drop {
        final String randomBoard = "generated/connect4/drop/board.json";

        @Nested
        class NumSteps {
            private void check(JsonParameterSet params, boolean testFreeSlots) {
                setup(params);
                List<Integer> numSteps = params.get("numSteps");

                for (int i = 0; i < numSteps.size(); i++) {
                    int steps = numSteps.get(i);
                    if ((testFreeSlots && steps >= worldHeight) || (!testFreeSlots && steps < worldHeight)) {
                        continue;
                    }

                    final int column = i;
                    int expected = testFreeSlots ? steps : -1;
                    Context context = baseContextBuilder
                        .add("column", column)
                        .build();
                    int actual = callObject(() -> ConnectFour.getNumberOfSteps(stones, column), context, result ->
                        "An exception occurred while invoking method getNumberOfSteps");
                    assertEquals(expected, actual, context, result ->
                        "Method numberOfSteps returned an incorrect value");
                }
            }

            @Test
            public void checkEmptyColumn() {
                // given
                worldHeight = 42;
                worldWidth = 1;
                stones = new RobotFamily[worldHeight][worldWidth];
                baseContextBuilder = contextBuilder()
                    .add("world height", worldHeight)
                    .add("world width", worldWidth)
                    .add("stones", stones);
                World.setSize(worldWidth, worldHeight);
                World.setDelay(0);
                // when
                final int column = 0;
                int expected = worldHeight - 1;
                Context context = baseContextBuilder
                    .add("column", column)
                    .build();
                // then
                int actual = callObject(() -> ConnectFour.getNumberOfSteps(stones, column), context, result ->
                    "An exception occurred while invoking method getNumberOfSteps");
                assertEquals(expected, actual, context, result ->
                    "Method numberOfSteps returned an incorrect value");
            }

            @ParameterizedTest
            @JsonParameterSetTest(randomBoard)
            public void freeSlot(JsonParameterSet params) {
                check(params, true);
            }

            @ParameterizedTest
            @JsonParameterSetTest(randomBoard)
            public void blockedSlot(JsonParameterSet params) {
                check(params, false);
            }

            @Test
            public void requirements() {
                iterateMethodStatements(
                    REQUIRE_SINGLE_LOOP,
                    ConnectFour.class,
                    "getNumberOfSteps",
                    RobotFamily[][].class,
                    int.class
                );
            }
        }

        @Nested
        class DropStone {
            /**
             * resets the world, calls dropStone and sets up a context.
             *
             * @param col           the column to drop the stone in
             * @param currentPlayer the robot family to drop
             * @return a basic context
             */
            private Context callDropStone(int col, RobotFamily currentPlayer) {
                Context context = baseContextBuilder
                    .add("column", col)
                    .add("currentPlayer", currentPlayer)
                    .build();
                World.getGlobalWorld().reset();

                Answer<?> answer = invocation -> {
                    Method method = invocation.getMethod();
                    if (method.getName().equals("getNumberOfSteps")) {
                        RobotFamily[][] stones = invocation.getArgument(0);
                        int column = invocation.getArgument(1);
                        // implementation copied from solution
                        for (int row = 0; row < stones.length; row++) {
                            if (stones[row][column] == null) {
                                return stones.length - 1 - row;
                            }
                        }
                        return -1;
                    }
                    return invocation.callRealMethod();
                };

                try (MockedStatic<ConnectFour> ignored = Mockito.mockStatic(ConnectFour.class, answer)) {
                    final int finalCol = col;
                    call(() -> ConnectFour.dropStone(stones, finalCol, currentPlayer), context, result ->
                        "An exception occurred while invoking method dropStone. Result may be salvageable, continuing...");
                } catch (Throwable t) {
                    t.printStackTrace(System.err);
                }
                return context;
            }

            /**
             * asserts that there is only one robot in the world and returns it
             *
             * @param context context for the assertion
             * @return the robot if the assertion succeeds
             */
            private Robot getOnlyRobot(Context context) {
                List<Robot> robots = World.getGlobalWorld()
                    .getAllFieldEntities()
                    .stream()
                    .filter(Robot.class::isInstance)
                    .map(Robot.class::cast)
                    .toList();
                assertEquals(1, robots.size(), context, result ->
                    "Unexpected number of robots in world");
                return robots.getFirst();
            }

            @ParameterizedTest
            @JsonParameterSetTest(randomBoard)
            public void robotCreation(JsonParameterSet params) {
                setup(params);
                List<Integer> numSteps = params.get("numSteps");

                for (int col = 0; col < worldWidth; col++) {
                    if (numSteps.get(col) >= worldHeight) {
                        continue;
                    }

                    RobotFamily currentPlayer = col % 2 == 0 ? RobotFamily.SQUARE_RED : RobotFamily.SQUARE_BLUE;
                    Context context = callDropStone(col, currentPlayer);
                    Robot robot = getOnlyRobot(context);

                    // get the trace of the robot
                    RobotTrace trace = World.getGlobalWorld().getTrace(robot);
                    // get the first transition in the trace to make assertions on the initial robot state
                    robot = trace.getTransitions().getFirst().robot;
                    assertEquals(col, robot.getX(), context, result -> "Robot was initialized with incorrect x coordinate");
                    assertEquals(worldHeight - 1, robot.getY(), context, result -> "Robot was initialized with incorrect y coordinate");
                    assertEquals(Direction.DOWN, robot.getDirection(), context, result -> "Robot was initialized with incorrect direction");
                    assertEquals(0, robot.getNumberOfCoins(), context, result -> "Robot was initialized with incorrect number of coins");
                    assertEquals(currentPlayer, robot.getRobotFamily(), context, result -> "Robot was initialized with incorrect robot family");
                }
            }

            @Test
            public void numStepsCall() {
                CtMethod<?> dropStoneCtMethod = getCtMethod(ConnectFour.class, "dropStone", RobotFamily[][].class, int.class, RobotFamily.class);
                CtExecutableReference<?> numStepsRef = getCtMethod(ConnectFour.class, "getNumberOfSteps", RobotFamily[][].class, int.class).getReference();
                Iterator<CtElement> iterator = dropStoneCtMethod.descendantIterator();

                boolean numStepsCalled = false;
                while (!numStepsCalled && iterator.hasNext()) {
                    CtElement ctElement = iterator.next();
                    if (ctElement instanceof CtInvocation<?> ctInvocation) {
                        numStepsCalled = ctInvocation.getExecutable().equals(numStepsRef);
                    }
                }
                assertTrue(numStepsCalled, emptyContext(), result -> "Method dropStone does not call method getNumberOfSteps");
            }

            @ParameterizedTest
            @JsonParameterSetTest(randomBoard)
            public void robotMove(JsonParameterSet params) {
                setup(params);
                List<Integer> numSteps = params.get("numSteps");

                for (int col = 0; col < worldWidth; col++) {
                    if (numSteps.get(col) <= 0) {
                        continue;
                    }

                    RobotFamily currentPlayer = col % 2 == 0 ? RobotFamily.SQUARE_RED : RobotFamily.SQUARE_BLUE;
                    Context context = callDropStone(col, currentPlayer);
                    Robot robot = getOnlyRobot(context);
                    List<Transition> transitions = World.getGlobalWorld().getTrace(robot).getTransitions();

                    int expectedTransitionsSize = numSteps.get(col) + 3;
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
                    // assert final board state
                    assertEquals(currentPlayer, stones[worldHeight - numSteps.get(col) - 1][col], context, r -> "Game board is incorrect after calling dropStone.");
                }
            }

            @Test
            public void requirements() {
                iterateMethodStatements(
                    REQUIRE_SINGLE_LOOP,
                    ConnectFour.class,
                    "dropStone",
                    RobotFamily[][].class,
                    int.class,
                    RobotFamily.class
                );
            }
        }
    }

    @Nested
    class CheckWin {
        @Nested
        class InLine {
            @CartesianTest
            public void check(
                @CartesianTest.Values(strings = {"SQUARE_RED", "SQUARE_BLUE"}) String c,
                @CartesianTest.Values(strings = {"SQUARE_RED", "SQUARE_BLUE"}) String l,
                @CartesianTest.Values(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}) int length
            ) {
                RobotFamily currentPlayer = robotFamilyLookup(c);
                RobotFamily lineFamily = robotFamilyLookup(l);
                RobotFamily[] line = Stream.generate(() -> lineFamily).limit(length).toArray(RobotFamily[]::new);
                boolean actual = ConnectFour.testAllInLine(line, currentPlayer);
                var context = contextBuilder()
                    .add("currentPlayer", currentPlayer)
                    .add("line", line);
                assertEquals(currentPlayer == lineFamily, actual, context.build(), r -> "Returned value is incorrect.");

                // check some permutations that shouldn't pass
                // all cases for them being even have already been checked
                if (currentPlayer == lineFamily) return;
                for (int i = 2; i < 10; ++i) {
                    final int finalI = i;
                    line = IntStream.range(0, length - 1 + i).mapToObj(v -> v % finalI == 0 ? currentPlayer : lineFamily).toArray(RobotFamily[]::new);
                    context = context.add("line", line);
                    assertFalse(ConnectFour.testAllInLine(line, currentPlayer), context.build(), r -> "Returned value is incorrect.");
                }
            }

            @Test
            public void requirements() {
                iterateMethodStatements(
                    REQUIRE_SINGLE_LOOP,
                    ConnectFour.class,
                    "testAllInLine",
                    RobotFamily[].class,
                    RobotFamily.class
                );
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
                boolean actual = callObject(() -> ConnectFour.testWinHorizontal(stones, currentPlayer), context, result -> "An exception occurred while invoking method testWinHorizontal");
                assertEquals(expected, actual, context, result -> "Method testWinHorizontal did not return the correct value");
            }

            @ParameterizedTest
            @JsonParameterSetTest("generated/connect4/checkWin/horizontal.json")
            public void inLineCall(JsonParameterSet params) throws Throwable {
                setup(params);
                List<Map<String, Integer>> winningRowCoordinates = params.get("winningRowCoordinates");
                var cb = baseContextBuilder.add("Method", "testWinHorizontal");
                Answer<?> answer = invocation -> {
                    Method method = invocation.getMethod();
                    if (method.getName().equals("testAllInLine")) {
                        RobotFamily[] line = invocation.getArgument(0);
                        RobotFamily actualPlayer = invocation.getArgument(1);
                        cb.add("line", line);
                        assertEquals(4, line.length, cb.build(), r -> "Encountered line of unexpected length.");
                        cb.add("current player", currentPlayer).add("actual player", actualPlayer);
                        assertEquals(currentPlayer, actualPlayer, cb.build(), r -> "Method called with wrong player argument");
                    }
                    return invocation.callRealMethod();
                };
                try (MockedStatic<ConnectFour> mock = Mockito.mockStatic(ConnectFour.class, answer)) {
                    callObject(() -> ConnectFour.testWinHorizontal(stones, currentPlayer), cb.build(), result -> "An exception occurred while invoking method testWinHorizontal");
                    RobotFamily[] arr = Stream.generate(() -> winningRowCoordinates.isEmpty() ? null : currentPlayer).limit(4).toArray(RobotFamily[]::new);
                    mock.verify(() -> ConnectFour.testAllInLine(arr, currentPlayer), Mockito.atLeastOnce());
                } catch (MockitoException e) {
                    // dubious workaround for https://github.com/mockito/mockito/issues/2576
                    if (e.getMessage().contains("An unexpected error occurred while verifying a static stub"))
                        throw e.getCause();
                    throw e;
                }
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
             */
            @ParameterizedTest
            @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
            public void check(int flags) {
                Answer<?> answer = invocation -> {
                    if (invocation.getMethod().equals(ConnectFour.class.getDeclaredMethod("testWinHorizontal", RobotFamily[][].class, RobotFamily.class))) {
                        return (flags & 1) == 1;
                    } else if (invocation.getMethod().equals(ConnectFour.class.getDeclaredMethod("testWinVertical", RobotFamily[][].class, RobotFamily.class))) {
                        return (flags >> 1 & 1) == 1;
                    } else if (invocation.getMethod().equals(ConnectFour.class.getDeclaredMethod("testWinDiagonal", RobotFamily[][].class, RobotFamily.class))) {
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
                try (MockedStatic<ConnectFour> ignored = Mockito.mockStatic(ConnectFour.class, answer)) {
                    assertCallEquals(flags != 0, () -> ConnectFour.testWinConditions(stones, currentPlayer), context, result ->
                        "Method testWinConditions did not return the correct value");
                }
            }

            @Test
            public void requirements() {
                Consumer<Iterator<CtElement>> consumer = iterator -> {
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
                };
                iterateMethodStatements(
                    consumer,
                    ConnectFour.class,
                    "testWinConditions",
                    RobotFamily[][].class,
                    RobotFamily.class
                );
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
            call(() -> ConnectFour.colorBackground(winner), context, result ->
                "An exception occurred while invoking colorBackground");
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
                String expected = "No valid columns found. Hence, game ends with a draw.";
                String actual = outputStream.toString().strip();
                contextBuilder.add("expected", expected);
                contextBuilder.add("actual", actual);
                assertTrue(
                    MatchingUtils.similarity(expected, actual) > 0.8,
                    contextBuilder.build(),
                    result -> "Printed String was not similar enough."
                );

                contextBuilder.add("method", "writeWinnerMessage");
                for (RobotFamily winner : new RobotFamily[]{RobotFamily.SQUARE_RED, RobotFamily.SQUARE_BLUE}) {
                    contextBuilder.add("winner", winner);
                    outputStream.reset();
                    ConnectFourInstance.writeWinnerMessage(winner);
                    expected = "Player %s wins the game!".formatted(winner);
                    actual = outputStream.toString().strip();
                    contextBuilder.add("expected", expected);
                    contextBuilder.add("actual", actual);
                    assertTrue(
                        MatchingUtils.similarity(expected, actual) > 0.8,
                        contextBuilder.build(),
                        result -> "Printed String was not similar enough."
                    );
                }
            } finally {
                System.setOut(originalOut);
            }
        }

        @ParameterizedTest
        @JsonParameterSetTest("generated/connect4/loop/boardFull.json")
        public void gameBoardFull(JsonParameterSet params) {
            setup(params);
            assertFalse(ConnectFour.isGameBoardFull(stones), baseContextBuilder.build(), r -> "isGameBoardFull returned an incorrect value");
            stones = new RobotFamily[World.getHeight()][World.getWidth()];
            baseContextBuilder.add("cb", stones);
            assertFalse(ConnectFour.isGameBoardFull(stones), baseContextBuilder.build(), r -> "isGameBoardFull returned an incorrect value");
            List<List<String>> fullBoard = params.get("fullBoard");
            for (int row = 0; row < worldHeight; row++) {
                for (int col = 0; col < worldWidth; col++) {
                    stones[row][col] = robotFamilyLookup(fullBoard.get(row).get(col));
                }
            }
            baseContextBuilder.add("cb", stones);
            assertTrue(ConnectFour.isGameBoardFull(stones), baseContextBuilder.build(), r -> "isGameBoardFull returned an incorrect value");
        }

        @Nested
        class GameLoop {
            private void mockInputHandler(ConnectFour connectFour) {
                ConnectFourInputHandler inputHandler = Mockito.spy(new ConnectFourInputHandler(connectFour));
                try {
                    // field is 'private final', so we bend the rules
                    Field f = ConnectFour.class.getDeclaredField("inputHandler");
                    // shouldn't fail in practice
                    f.setAccessible(true);
                    f.set(connectFour, inputHandler);
                } catch (ReflectiveOperationException e) {
                    // rethrow, but unchecked <3
                    throw new RuntimeException(e);
                }
                Mockito.doAnswer(invocation -> {
                    if (invocation.getMethod().getName().equals("install")) {
                        // do nothing, we don't need it for headless tests
                        return null;
                    }
                    return invocation.callRealMethod();
                }).when(inputHandler).install();
            }

            @Test
            public void initialization() throws NoSuchMethodException {
                var cb = contextBuilder();
                // afaik there is no way to do a proper data flow analysis in spoon
                // so we just collect all variable declarations and assignments and make sure the appropriate values occur
                final var mth = BasicMethodLink.of(ConnectFour.class.getDeclaredMethod("gameLoop")).getCtElement();
                final var allStmts = mth.getBody().getStatements();
                // all statements until loop (if students have no loop, they'll probably fail to score multiple other points so we're generous
                var stmtsUntilLoop = allStmts.stream().takeWhile(new Predicate<>() {
                    boolean foundLoop = false;

                    @Override
                    public boolean test(CtStatement ctStatement) {
                        if (ctStatement instanceof CtLoop) foundLoop = true;
                        return !foundLoop;
                    }
                }).toList();
                // find all rvalues
                var assignedVals = stmtsUntilLoop
                    .stream()
                    .map(stmt -> {
                        if (stmt instanceof CtLocalVariable<?> v) return v.getAssignment();
                        else if (stmt instanceof CtAssignment<?, ?> a) return a.getAssignment();
                        return null;
                    })
                    .filter(Objects::nonNull) // store brand filterMap
                    .toList();
                cb.add("Values assigned before first loop", assignedVals);
                // find current player initial value
                assertTrue(assignedVals.stream().anyMatch(x -> x.toString().contains(RobotFamily.SQUARE_BLUE.toString())), cb.build(), r -> "Value " + RobotFamily.SQUARE_BLUE + " was never assigned to a variable.");

                @SuppressWarnings({"unchecked"}) // see below for reasoning
                List<CtNewArray<RobotFamily[][]>> newArrayExpressions = assignedVals.stream()
                    .filter(x ->
                        x instanceof CtNewArray<?> && x.getType().getActualClass() == RobotFamily[][].class
                    ) // the following cast is unchecked. spoon's api is just weird and i'm not sure whether there is a way to get around an unchecked operation here. the filter above should in theory check this cast to be safe
                    .map(x -> (CtNewArray<RobotFamily[][]>) x)
                    .toList();
                assertEquals(newArrayExpressions.size(), 1, cb.build(), r -> "Found incorrect number of assigned RobotFamily[][] values.");

                // assert World.getHeight and World.getWidth were called
                // we assume that if they were called and an array expression exists, the construction was fine
                List<CtExpression<Integer>> dims = newArrayExpressions.getFirst().getDimensionExpressions();
                final var invocations = Stream.of(assignedVals, dims)
                    .flatMap(List::stream)
                    .filter(CtInvocation.class::isInstance)
                    .map(CtInvocation.class::cast)
                    .toList();
                cb.add("invocations", invocations);
                for (String mthName : new String[]{"getHeight", "getWidth"}) {
                    assertTrue(invocations.stream().anyMatch(invocation -> {
                        try {
                            return invocation.getExecutable().getActualMethod().equals(World.class.getMethod(mthName));
                        } catch (NoSuchMethodException e) {
                            // can't have checked exception in lambda
                            throw new RuntimeException(e);
                        }
                    }), cb.build(), r -> "World." + mthName + " was not called.");
                }
            }

            @Test
            public void nextPlayerCall() {
                List<RobotFamily> nextPlayerArgs = new ArrayList<>();
                Answer<?> answer = invocation -> {
                    switch (invocation.getMethod().getName()) {
                        case "nextPlayer" -> {
                            RobotFamily currentPlayer = invocation.getArgument(0);
                            nextPlayerArgs.add(currentPlayer);
                            return currentPlayer == RobotFamily.SQUARE_RED ? RobotFamily.SQUARE_BLUE : RobotFamily.SQUARE_RED;
                        }
                        case "dropStone" -> {
                            return null;
                        }
                        case "testWinConditions" -> {
                            return nextPlayerArgs.size() >= 5;
                        }
                        default -> {
                            return invocation.callRealMethod();
                        }
                    }
                };
                try (MockedStatic<ConnectFour> ignored = Mockito.mockStatic(ConnectFour.class, answer)) {
                    int worldHeight = 5;
                    int worldWidth = 5;
                    ConnectFour connectFour = new ConnectFour(worldWidth, worldHeight, false, 0);
                    mockInputHandler(connectFour);
                    ConnectFourInputHandler inputHandler = connectFour.getInputHandler();
                    for (int i = 0; i < 10; i++) {
                        inputHandler.addInput(0);
                    }

                    connectFour.startGame();
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
                    switch (invocation.getMethod().getName()) {
                        case "nextPlayer" -> {
                            RobotFamily currentPlayer = invocation.getArgument(0);
                            return currentPlayer == RobotFamily.SQUARE_RED ? RobotFamily.SQUARE_BLUE : RobotFamily.SQUARE_RED;
                        }
                        case "dropStone" -> {
                            stonesArgs.add(invocation.getArgument(0));
                            columnArgs.add(invocation.getArgument(1));
                            currentPlayerArgs.add(invocation.getArgument(2));
                            return null;
                        }
                        case "testWinConditions" -> {
                            return currentPlayerArgs.size() >= 5;
                        }
                        default -> {
                            return invocation.callRealMethod();
                        }
                    }
                };
                int worldHeight = 5;
                int worldWidth = 5;
                try (MockedStatic<ConnectFour> ignored = Mockito.mockStatic(ConnectFour.class, answer)) {
                    ConnectFour connectFour = new ConnectFour(worldWidth, worldHeight, false, 0);
                    mockInputHandler(connectFour);
                    ConnectFourInputHandler inputHandler = connectFour.getInputHandler();
                    for (int i = 0; i < 10; i++) {
                        inputHandler.addInput(i % worldWidth);
                    }

                    connectFour.startGame();
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
                    switch (invocation.getMethod().getName()) {
                        case "nextPlayer" -> {
                            RobotFamily currentPlayer = invocation.getArgument(0);
                            return currentPlayer == RobotFamily.SQUARE_RED ? RobotFamily.SQUARE_BLUE : RobotFamily.SQUARE_RED;
                        }
                        case "dropStone" -> {
                            return null;
                        }
                        case "testWinConditions" -> {
                            stonesArgs.add(invocation.getArgument(0));
                            currentPlayerArgs.add(invocation.getArgument(1));
                            return currentPlayerArgs.size() >= 5;
                        }
                        default -> {
                            return invocation.callRealMethod();
                        }
                    }
                };
                int worldHeight = 5;
                int worldWidth = 5;
                try (MockedStatic<ConnectFour> ignored = Mockito.mockStatic(ConnectFour.class, answer)) {
                    ConnectFour connectFour = Mockito.spy(new ConnectFour(worldWidth, worldHeight, false, 0));
                    mockInputHandler(connectFour);
                    AtomicInteger i = new AtomicInteger();
                    Mockito.doAnswer(invocation -> i.getAndUpdate(n -> n + 1 % worldWidth)).when(connectFour).waitForNextInput(Mockito.any(), Mockito.any());
                    connectFour.startGame();
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

            @CartesianTest
            public void output(
                @CartesianTest.Values(strings = {"SQUARE_RED", "SQUARE_BLUE"}) String family,
                @CartesianTest.Values(booleans = {true, false}) boolean testDraw
            ) {
                // given
                final RobotFamily currentPlayer = robotFamilyLookup(family);
                final int worldHeight = 5;
                final int worldWidth = 5;
                try (MockedStatic<ConnectFour> mock = Mockito.mockStatic(ConnectFour.class)) {
                    mock.when(() -> ConnectFour.testWinConditions(Mockito.any(), Mockito.any())).thenReturn(!testDraw);
                    mock.when(() -> ConnectFour.nextPlayer(Mockito.any())).thenReturn(currentPlayer);
                    ConnectFour connectFour = Mockito.spy(new ConnectFour(worldWidth, worldHeight, false, 0));
                    // need to use doReturn because the method throws
                    Mockito.doReturn(testDraw ? -1 : 0).when(connectFour).waitForNextInput(Mockito.any(), Mockito.any());
                    mockInputHandler(connectFour);
                    // when
                    connectFour.startGame();
                    // then
                    final var verification = Mockito.verify(connectFour, Mockito.times(1));
                    if (testDraw) verification.writeDrawMessage();
                    else verification.writeWinnerMessage(currentPlayer);
                }
            }

            @Test
            public void requirements() {
                iterateMethodStatements(
                    REQUIRE_SINGLE_LOOP,
                    ConnectFour.class,
                    "gameLoop"
                );
            }
        }
    }
}
