package h03;

import org.sourcegrade.jagr.api.rubric.Criterion;
import org.sourcegrade.jagr.api.rubric.JUnitTestRef;
import org.sourcegrade.jagr.api.rubric.Rubric;
import org.sourcegrade.jagr.api.rubric.RubricProvider;
import org.sourcegrade.jagr.api.testing.RubricConfiguration;
import org.tudalgo.algoutils.tutor.general.json.JsonParameterSet;

import static org.tudalgo.algoutils.tutor.general.jagr.RubricUtils.criterion;

public class H03_RubricProviderPrivate implements RubricProvider {

    public static final Rubric RUBRIC = Rubric.builder()
        .title("H03 | Vier Gewinnt")
        .addChildCriteria(
            Criterion.builder()
                .shortDescription("H03.1 | Tribonacci mit 1D Array")
                .minPoints(0)
                .addChildCriteria(
                    Criterion.builder()
                        .shortDescription("H03.1.1 | Methode getStartingNumbers")
                        .minPoints(0)
                        .addChildCriteria(
                            criterion(
                                "Es wird korrekt ein Array der Länge 3 mit den ersten Zahlen der Tribonacci-Folge zurückgegeben.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.GetStartingNumbers.class.getDeclaredMethod("check")
                                )
                            )
                        ).build(),
                    Criterion.builder()
                        .shortDescription("H03.1.2 | Methode push")
                        .minPoints(0)
                        .addChildCriteria(
                            criterion(
                                "Das letzte Element des Ergebnis-Arrays ist das übergebene Element.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.class.getDeclaredMethod(
                                        "testPushLastElementCorrect",
                                        JsonParameterSet.class
                                    )
                                )
                            ),
                            criterion(
                                "Die Elemente des Ergebnis-Arrays sind korrekt.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.class.getDeclaredMethod(
                                        "testPushAllElementsCorrect",
                                        JsonParameterSet.class
                                    )
                                )
                            ),
                            criterion(
                                "Das Eingabe-Array wird nicht verändert.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.class.getDeclaredMethod(
                                        "testPushOriginalArrayUnchanged",
                                        JsonParameterSet.class
                                    )
                                )
                            )
                        ).build(),
                    Criterion.builder()
                        .shortDescription("H03.1.3 | Methode calculateNextTribonacci")
                        .minPoints(0)
                        .addChildCriteria(
                            criterion(
                                "Das Ergebnis ist korrekt mit zwei positiven Zahlen.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.class.getDeclaredMethod(
                                        "testCalculateNextFibonacciPositiveOnly", JsonParameterSet.class)
                                )
                            ),
                            criterion(
                                "Das Ergebnis ist korrekt mit beliebigen Eingaben.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.class.getDeclaredMethod(
                                        "testCalculateNextFibonacciAllNumbers", JsonParameterSet.class)
                                )
                            ),
                            criterion(
                                "Eine verbindliche Anforderung wurde verletzt.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.class.getDeclaredMethod(
                                        "testCalculateNextFibonacciVanforderungen", JsonParameterSet.class)
                                ),
                                -1
                            )).build(),
                    Criterion.builder()
                        .shortDescription("H03.1.4 | Methode tribonacci")
                        .minPoints(0)
                        .addChildCriteria(
                            criterion(
                                "Das Ergebnis ist korrekt für n < 3.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.class.getDeclaredMethod(
                                        "testFibonacciSmallerThanTwo", JsonParameterSet.class)
                                )
                            ),
                            criterion(
                                "Das Ergebnis ist korrekt für n >= 2.",
                                JUnitTestRef.ofMethod(
                                    () -> TribonacciNumbersTest.class.getDeclaredMethod(
                                        "testFibonacciBigNumbers", JsonParameterSet.class)
                                )
                            ),
                            criterion(
                                "Eine Verbindliche Anforderung wurde verletzt.",
                                JUnitTestRef.and(
                                    JUnitTestRef.ofMethod(
                                        () -> TribonacciNumbersTest.class.getDeclaredMethod(
                                            "testFibonacciVanforderungen", JsonParameterSet.class)
                                    ),
                                    JUnitTestRef.ofMethod(
                                        () -> TribonacciNumbersTest.class.getDeclaredMethod(
                                            "testFibonacciNonIterativeVanforderungen")
                                    )
                                ),
                                -1
                            )).build()
                ).build()
        )
        .addChildCriteria(
            Criterion.builder()
                .shortDescription("H03.2 | Vier Gewinnt")
                .minPoints(0)
                .addChildCriteria(
                    Criterion.builder()
                        .shortDescription("H03.2.1 | Methode validateInput")
                        .addChildCriteria(
                            criterion(
                                "Methode ist vollständig korrekt implementiert.",
                                JUnitTestRef.and(
                                    JUnitTestRef.ofMethod(
                                        () -> ConnectFourTest.class.getDeclaredMethod(
                                            "testValidateInputEdgeCases", JsonParameterSet.class)
                                    ),
                                    JUnitTestRef.ofMethod(
                                        () -> ConnectFourTest.class.getDeclaredMethod(
                                            "testValidateInputRandomCases", JsonParameterSet.class)
                                    )
                                )
                            )
                        )
                        .build(),
                    Criterion.builder()
                        .shortDescription("H03.2.2 | Spielstein fallen lassen")
                        .minPoints(0)
                        .addChildCriteria(
                            Criterion.builder()
                                .shortDescription("Methode getNumberOfSteps")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Die Rückgabe ist korrekt, wenn ein freier Slot existiert.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testGetDestinationRowFreeSlot", JsonParameterSet.class)
                                        )
                                    ),
                                    criterion(
                                        "Die Rückgabe ist korrekt, wenn KEIN freier Slot existiert.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testGetDestinationRowBlockedSlot", JsonParameterSet.class)
                                        )
                                    ),
                                    criterion(
                                        "Verbindliche Anforderung 'genau eine Schleife' wurde verletzt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testGetDestinationRowVAnforderung")
                                        ),
                                        -1
                                    )).build(),
                            Criterion.builder()
                                .shortDescription("Methode dropStone")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Robot wird mit korrekten Parametern erstellt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testDropStoneRobotCorrect", JsonParameterSet.class)
                                        )
                                    ),
                                    criterion(
                                        "getNumberOfSteps wird korrekt aufgerufen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testDropStoneCallsGetDestinationRow")
                                        )
                                    ),
                                    criterion(
                                        "Robot führt die korrekte Bewegung aus.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testDropStoneMovementCorrect", JsonParameterSet.class)
                                        )
                                    ),
                                    criterion(
                                        "Verbindliche Anforderung 'genau eine Schleife' wurde verletzt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod("testDropStoneVAnforderung")
                                        ),
                                        -1
                                    )).build()
                        )
                        .build(),
                    Criterion.builder()
                        .shortDescription("H03.2.3 | Gewinnbedingung prüfen")
                        .addChildCriteria(
                            Criterion.builder()
                                .shortDescription("Methode testAllInLine")
                                .addChildCriteria(
                                    criterion(
                                        "Methode gibt korrekt true oder false abhängig vom Parameter zurück.",
                                        1
                                    ),
                                    criterion(
                                        "Methode nutzt genau eine Schleifen.",
                                        1
                                    )
                                ).minPoints(0).build(),
                            Criterion.builder()
                                .shortDescription("Methode testWinHorizontal")
                                .addChildCriteria(
                                    criterion(
                                        "Methode erkennt richtige horizontale Steinfolgen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testTestWinHorizontal", JsonParameterSet.class)
                                        ),
                                        2
                                    ),
                                    criterion(
                                        "Methode ruft testAllInLine korrekt auf.",
                                        2
                                    ),
                                    criterion(
                                        "Methode erkennt keine falschen Steinfolgen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod("testTestWinHorizontalVAnforderung2")
                                        ),
                                        -2
                                    )
                                ).minPoints(0).build(),
                            Criterion.builder()
                                .shortDescription("Methode testWinVertical")
                                .addChildCriteria(
                                    criterion(
                                        "Methode erkennt richtige vertikale Steinfolgen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testTestWinVertical", JsonParameterSet.class)
                                        ),
                                        2
                                    ),
                                    criterion(
                                        "Methode erkennt keine falschen Steinfolgen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testTestWinVerticalVAnforderung2")
                                        ),
                                        -2
                                    )
                                ).minPoints(0).build(),
                            Criterion.builder()
                                .shortDescription("Methode testWinConditions")
                                .addChildCriteria(
                                    criterion(
                                        "Die Rückgabe ist in allen Fällen korrekt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testTestWinConditions", int.class)
                                        )
                                    )
                                ).build()
                        )
                        .build(),
                    Criterion.builder()
                        .shortDescription("H03.2.4 | Game Loop")
                        .minPoints(0)
                        .addChildCriteria(
                            Criterion.builder()
                                .shortDescription("Methode nextPlayer")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Die Rückgabe für beide RobotFamily.SQUARE_BLUE und SQUARE_RED korrekt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testNextPlayer", boolean.class)
                                        )
                                    )).build()
                            ,
                            Criterion.builder()
                                .shortDescription("Methode colorFieldBackground")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Das Spielfeld wird korrekt eingefärbt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testColorFieldBackground", boolean.class)
                                        )
                                    )).build()
                            ,
                            Criterion.builder()
                                .shortDescription("Methoden writeDrawMessage, writeWinnerMessage")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "Die Ausgabe in die Konsole ist korrekt.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testWriteMessages")
                                        )
                                    )).build()
                            ,
                            Criterion.builder()
                                .shortDescription("Methode isGameBoardFull")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "nextPlayer wird mit korrektem Parameter aufgerufen.",
                                        1
                                    )).build()
                            ,
                            Criterion.builder()
                                .shortDescription("Methode gameLoop")
                                .minPoints(0)
                                .addChildCriteria(
                                    criterion(
                                        "das Spielstein Array stones und der aktuelle Spieler werden korrekt initialisiert.",
                                        1
                                    ),
                                    criterion(
                                        "nextPlayer wird mit korrektem Parameter aufgerufen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testGameLoopCallsNextPlayer")
                                        )
                                    ),
                                    criterion(
                                        "dropStone wird mit korrekten Parametern aufgerufen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testGameLoopCallsDropStone")
                                        )
                                    ),
                                    criterion(
                                        "testWinConditions wird mit korrekten Parametern aufgerufen.",
                                        JUnitTestRef.ofMethod(
                                            () -> ConnectFourTest.class.getDeclaredMethod(
                                                "testGameLoopCallsGetWinConditions")
                                        )
                                    ),
                                    criterion(
                                        "writeWinnerMessage oder writeDrawMessage werden zum Spielende im korrekten Fall aufgerufen",
                                        1
                                    )).build()
                        )
                        .build()
                )
                .build()
        )
        .build();

    @Override
    public Rubric getRubric() {
        return RUBRIC;
    }

    @Override
    public void configure(RubricConfiguration configuration) {
        configuration.addTransformer(new SubmissionTransformer());
    }
}
