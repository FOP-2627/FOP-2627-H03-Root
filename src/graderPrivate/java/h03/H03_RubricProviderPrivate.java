package h03;

import org.sourcegrade.jagr.api.rubric.Criterion;
import org.sourcegrade.jagr.api.rubric.JUnitTestRef;
import org.sourcegrade.jagr.api.rubric.Rubric;
import org.sourcegrade.jagr.api.rubric.RubricProvider;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;

import static org.tudalgo.algoutils.tutor.general.jagr.RubricUtils.criterion;

@SuppressWarnings({"unused"})
public class H03_RubricProviderPrivate implements RubricProvider {
    public static final Rubric RUBRIC = Rubric.builder()
        .title("H03 | Vier Gewinnt")
        .addChildCriteria(
            Criterion.builder()
                .shortDescription("H3.1 | Tribonacci mit 1D Array")
                .minPoints(0)
                .addChildCriteria(
                    Criterion.builder()
                        .shortDescription("H3.1.1 | Methode getStartingNumbers")
                        .minPoints(0)
                        .addChildCriteria(
                            criterion(
                                "Es wird ein Array der Länge 3 mit den ersten Zahlen der Tribonacci-Folge zurückgegeben.",
                                JUnitTestRef.ofClass(TribonacciNumbersTest.GetStartingNumbers.class)
                            )
                        ).build(),
                    Criterion.builder()
                        .shortDescription("H3.1.2 | Methode push")
                        .minPoints(0)
                        .addChildCriteria(
                            criterion(
                                "Das letzte Element des Ergebnis-Arrays ist das übergebene Element.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.Push.class.getDeclaredMethod(
                                        "lastElement",
                                        JsonParameterSet.class
                                    )
                                )
                            ),
                            criterion(
                                "Die Elemente des Ergebnis-Arrays sind korrekt.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.Push.class.getDeclaredMethod(
                                        "allElements",
                                        JsonParameterSet.class
                                    )
                                )
                            ),
                            criterion(
                                "Das Eingabe-Array wird nicht verändert.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.Push.class.getDeclaredMethod(
                                        "inputUnchanged",
                                        JsonParameterSet.class
                                    )
                                )
                            )
                        ).build(),
                    Criterion.builder()
                        .shortDescription("H3.1.3 | Methode calculateNextTribonacci")
                        .minPoints(0)
                        .addChildCriteria(
                            criterion(
                                "Das Ergebnis ist korrekt mit zwei positiven Zahlen.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.CalcNext.class.getDeclaredMethod(
                                        "positiveOnly",
                                        JsonParameterSet.class
                                    )
                                )
                            ),
                            criterion(
                                "Das Ergebnis ist korrekt mit beliebigen Eingaben.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.CalcNext.class.getDeclaredMethod(
                                        "arbitraryInput",
                                        JsonParameterSet.class
                                    )
                                )
                            ),
                            criterion(
                                "Die verbindlichen Anforderungen wurden eingehalten.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.CalcNext.class.getDeclaredMethod(
                                        "requirements",
                                        JsonParameterSet.class
                                    )
                                ),
                                -1
                            )
                        ).build(),
                    Criterion.builder()
                        .shortDescription("H3.1.4 | Methode tribonacci")
                        .minPoints(0)
                        .addChildCriteria(
                            criterion(
                                "Das Ergebnis ist korrekt für n < 3.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.Tribonacci.class.getDeclaredMethod(
                                        "smallerThanTwo",
                                        JsonParameterSet.class
                                    )
                                )
                            ),
                            criterion(
                                "Das Ergebnis ist korrekt für n >= 3.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.Tribonacci.class.getDeclaredMethod(
                                        "full",
                                        JsonParameterSet.class
                                    )
                                )
                            ),
                            criterion(
                                "Die verbindlichen Anforderungen wurden eingehalten.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.Tribonacci.class.getDeclaredMethod(
                                        "requirements"
                                    )
                                ),
                                -1
                            )
                        ).build()
                ).build(),
            Criterion.builder()
                .shortDescription("H3.2 | Vier Gewinnt")
                .minPoints(0)
                .addChildCriteria(
                    Criterion.builder()
                        .shortDescription("H3.2.1 | Methode validateInput")
                        .addChildCriteria(
                            criterion(
                                "Methode ist vollständig korrekt implementiert.",
                                JUnitTestRef.ofClass(ConnectFourTest.Validate.class)
                            )
                        ).build(),
                    Criterion.builder()
                        .shortDescription("H3.2.2 | Spielstein fallen lassen")
                        .minPoints(0)
                        .addChildCriteria(
                            Criterion.builder()
                                .shortDescription("Methode getNumberOfSteps")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Die Rückgabe ist korrekt, wenn ein freier Slot existiert.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Drop.NumSteps.class.getDeclaredMethod(
                                                "freeSlot",
                                                JsonParameterSet.class
                                            )
                                        )
                                    ),
                                    criterion(
                                        "Die Rückgabe ist korrekt, wenn kein freier Slot existiert.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Drop.NumSteps.class.getDeclaredMethod(
                                                "blockedSlot",
                                                JsonParameterSet.class
                                            )
                                        )
                                    ),
                                    criterion(
                                        "Die verbindlichen Anforderungen wurden eingehalten.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Drop.NumSteps.class.getDeclaredMethod(
                                                "requirements"
                                            )
                                        ),
                                        -1
                                    )
                                ).build(),
                            Criterion.builder()
                                .shortDescription("Methode dropStone")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Der Roboter wird mit korrekten Parametern erstellt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Drop.DropStone.class.getDeclaredMethod(
                                                "robotCreation",
                                                JsonParameterSet.class
                                            )
                                        )
                                    ),
                                    criterion(
                                        "getNumberOfSteps wird korrekt aufgerufen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Drop.DropStone.class.getDeclaredMethod(
                                                "numStepsCall"
                                            )
                                        )
                                    ),
                                    criterion(
                                        "Der Roboter führt die korrekte Bewegung aus.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Drop.DropStone.class.getDeclaredMethod(
                                                "robotMove",
                                                JsonParameterSet.class
                                            )
                                        )
                                    ),
                                    criterion(
                                        "Die verbindlichen Anforderungen wurden eingehalten.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Drop.DropStone.class.getDeclaredMethod(
                                                "requirements"
                                            )
                                        ),
                                        -1
                                    )
                                ).build()
                        ).build(),
                    Criterion.builder()
                        .shortDescription("H3.2.3 | Gewinnbedingungen prüfen")
                        .addChildCriteria(
                            Criterion.builder()
                                .shortDescription("Methode testAllInLine")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Die Rückgabe ist korrekt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.CheckWin.InLine.class.getDeclaredMethod(
                                                "check",
                                                String.class,
                                                String.class,
                                                int.class
                                            )
                                        )
                                    ),
                                    criterion(
                                        "Die Methode nutzt genau eine Schleife.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.CheckWin.InLine.class.getDeclaredMethod(
                                                "requirements"
                                            )
                                        ),
                                        1
                                    )
                                ).build(),
                            Criterion.builder()
                                .shortDescription("Methode testWinHorizontal")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Die Methode erkennt richtige horizontale Steinfolgen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.CheckWin.Horizontal.class.getDeclaredMethod(
                                                "goodInput",
                                                JsonParameterSet.class
                                            )
                                        ),
                                        2
                                    ),
                                    criterion(
                                        "Die Methode ruft testAllInLine korrekt auf.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.CheckWin.Horizontal.class.getDeclaredMethod(
                                                "inLineCall",
                                                JsonParameterSet.class
                                            )
                                        ),
                                        2
                                    ),
                                    criterion(
                                        "Die Methode erkennt keine falschen Steinfolgen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.CheckWin.Horizontal.class.getDeclaredMethod(
                                                "badInput"
                                            )
                                        ),
                                        -2
                                    )
                                ).build(),
                            Criterion.builder()
                                .shortDescription("Methode testWinVertical")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Die Methode erkennt richtige vertikale Steinfolgen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.CheckWin.Vertical.class.getDeclaredMethod(
                                                "goodInput",
                                                JsonParameterSet.class
                                            )
                                        ),
                                        2
                                    ),
                                    criterion(
                                        "Die Methode erkennt keine falschen Steinfolgen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.CheckWin.Vertical.class.getDeclaredMethod(
                                                "badInput"
                                            )
                                        ),
                                        -2
                                    )
                                ).build(),
                            Criterion.builder()
                                .shortDescription("Methode testWinConditions")
                                .addChildCriteria(
                                    criterion(
                                        "Die Rückgabe ist in allen Fällen korrekt.",
                                        JUnitTestRef.ofClass(ConnectFourTest.CheckWin.Conditions.class)
                                    )
                                ).build()
                        ).build(),
                    Criterion.builder()
                        .shortDescription("H3.2.4 | Game Loop")
                        .minPoints(0)
                        .addChildCriteria(
                            Criterion.builder()
                                .shortDescription("Methode nextPlayer")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Die Rückgabe ist vollständig korrekt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Loop.class.getDeclaredMethod(
                                                "nextPlayer",
                                                boolean.class
                                            )
                                        )
                                    )
                                ).build(),
                            Criterion.builder()
                                .shortDescription("Methode colorFieldBackground")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Das Spielfeld wird korrekt eingefärbt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Loop.class.getDeclaredMethod(
                                                "colorField",
                                                boolean.class
                                            )
                                        )
                                    )
                                ).build(),
                            Criterion.builder()
                                .shortDescription("Methoden writeDrawMessage, writeWinnerMessage")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Die Ausgabe in die Konsole ist korrekt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Loop.class.getDeclaredMethod(
                                                "writeMessage"
                                            )
                                        )
                                    )
                                ).build(),
                            Criterion.builder()
                                .shortDescription("Methode isGameBoardFull")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Die Methode ist vollständig korrekt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Loop.class.getDeclaredMethod(
                                                "gameBoardFull",
                                                JsonParameterSet.class
                                            )
                                        )
                                    )
                                ).build(),
                            Criterion.builder()
                                .shortDescription("Methode gameLoop")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Das Spielstein-Array und der aktuelle Spieler werden korrekt initialisiert.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Loop.GameLoop.class.getDeclaredMethod(
                                                "initialization"
                                            )
                                        )
                                    ),
                                    criterion(
                                        "nextPlayer wird korrekt aufgerufen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Loop.GameLoop.class.getDeclaredMethod(
                                                "nextPlayerCall"
                                            )
                                        )
                                    ),
                                    criterion(
                                        "dropStone wird korrekt aufgerufen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Loop.GameLoop.class.getDeclaredMethod(
                                                "dropStoneCall"
                                            )
                                        )
                                    ),
                                    criterion(
                                        "testWinConditions wird korrekt aufgerufen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Loop.GameLoop.class.getDeclaredMethod(
                                                "conditionCall"
                                            )
                                        )
                                    ),
                                    criterion(
                                        "writeWinnerMessage oder writeDrawMessage werden zum Spielende im korrekten Fall aufgerufen",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Loop.GameLoop.class.getDeclaredMethod(
                                                "output",
                                                String.class,
                                                boolean.class
                                            )
                                        )
                                    ),
                                    criterion(
                                        "Alle verbindlichen Anforderungen wurden eingehalten.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.Loop.GameLoop.class.getDeclaredMethod(
                                                "requirements"
                                            )
                                        ),
                                        -1
                                    )
                                ).build()
                        ).build()
                ).build()
        ).build();

    @Override
    public Rubric getRubric() {
        return RUBRIC;
    }
}
