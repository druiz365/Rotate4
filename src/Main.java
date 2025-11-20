import java.util.*;

public class Main {
    private static Game myGame;
    private static HashMap<String, List<Integer>> traceMap;

    public static void main(String[] args) {
        myGame = new Game();
//        testTrace("wrongWinner2");
        menu();
    }

    private static void testTrace(String traceID) {
        setupTraces();
        myGame.reset();
        List<Integer> trace = traceMap.get(traceID);

        for (int loc : trace) {
            myGame.play(loc, false);
        }
    }
    private static void setupTraces() {
        traceMap = new HashMap<>();
        List<Integer> allDirections =
                new ArrayList<>(Arrays.asList(3,4,3,4,3,3,2,3,2,3,2,3));
        List<Integer> middleTower =
                new ArrayList<>(Arrays.asList(3,4,3,4,3,4,3,4,3,4,3,4,3,4,3,4,3,4));
        List<Integer> zeroTower =
                new ArrayList<>(Arrays.asList(0,0,0,0,0,0,0,0,0,0));
        List<Integer> missingTexture =
                new ArrayList<>(Arrays.asList(0,1,1,2,1,2,1));
        List<Integer> missingPlay =
                new ArrayList<>(Arrays.asList(3,4,3,3,4,3,0,1,5,0,1,5,5,1));
        List<Integer> wrongWinner =
                new ArrayList<>(Arrays.asList(4,2,3,1,5,5,4,4,3,4,3,4,5,6,6,1));
        List<Integer> twoWinners =
                new ArrayList<>(Arrays.asList(0,1,0,1,2,2,2,3,3,3,0,0,4,5,4));
        List<Integer> doublePlay =
                new ArrayList<>(Arrays.asList(0,3,0,3,0,1,2));
        List<Integer> wrongWinner2 =
                new ArrayList<>(Arrays.asList(3,2,1,2,1,3,1,3,1,2,1,0,1));

        traceMap.put("allDirections", allDirections);
        traceMap.put("middleTower", middleTower);
        traceMap.put("zeroTower", zeroTower);
        traceMap.put("missingTexture", missingTexture);
        traceMap.put("missingPlay", missingPlay);
        traceMap.put("wrongWinner", wrongWinner);
        traceMap.put("twoWinners", twoWinners);
        traceMap.put("doublePlay", doublePlay);
        traceMap.put("wrongWinner2", wrongWinner2);
    }

    private static void menu() {
        Scanner scanner = new Scanner(System.in);
        int menuSelect;

        while (true) {
            System.out.println("\nWelcome to Rotate4!\n\tBy Diego Ruiz\n");
            System.out.println("\tPlay w/ Friends:\ttype 0");
            System.out.println("\tPlay against bot:\ttype 1");
            System.out.println("\tView tutorial:\t\ttype 2");

            try {
                menuSelect = scanner.nextInt();
                if (menuSelect >= 0 && menuSelect <= 2) {
                    break;
                }
            } catch (java.util.InputMismatchException j) {
                System.out.println("Invalid Input");
                scanner.next();
            }
        }
        if (menuSelect == 0) {
            gameLoop();
        } else if (menuSelect == 1) {
            bot();
        } else if (menuSelect == 2) {
            scanner.close();
            return;
        }
        scanner.close();

    }

    private static void gameLoop() {
        int index;
        Scanner scanner = new Scanner(System.in);
        myGame.reset();
        myGame.printState();

        while (true) {
            while (true) {
                System.out.println("Input width/depth:");
                try {
                    index = scanner.nextInt();
                    break;
                } catch (java.util.InputMismatchException j) {
                    System.out.println("Invalid Input");
                    scanner.next();
                }
            }

            if (index == -1) {
                int choice = - 1;
                while (true) {
                    System.out.println("\tPaused");
                    System.out.println("Resume:\t\t\ttype 1");
                    System.out.println("Quit:\t\t\ttype -1");
                    System.out.println("Reset:\t\t\ttype 0");
                    System.out.println("Undo:\t\t\ttype 2");
                    System.out.println("Play Against Bot:\ttype 3");

                    try {
                        choice = scanner.nextInt();
                        break;
                    } catch (java.util.InputMismatchException j) {
                        System.out.println("Invalid Input\n");
                        scanner.next();
                    }
                }

                if (choice == -1) {
                    break;
                } else if (choice == 0) {
                    myGame.reset();
                    myGame.printState();
                } else if (choice == 1) {
                    myGame.printState();
                } else if (choice == 2) {
                    myGame.undo(false);
                } else if (choice == 3) {
                    bot();
                    break;
                }
            } else {
                myGame.play(index, false);
            }
        }
        scanner.close();
    }

    private static void bot() {
        Scanner scanner = new Scanner(System.in);
        int playerTurn = -1;

        while (true) {
            System.out.println("Player 1 or 2?");
            try {
                playerTurn = scanner.nextInt();
                if (playerTurn == 1 || playerTurn == 2) {
                    break;
                }
                System.out.println("Invalid input.\n");
            } catch (java.util.InputMismatchException j) {
                System.out.println("Invalid Input\n");
                scanner.next();
            }
        }

        myGame.reset();
        Random rand = new Random();
        boolean playerFirst = (playerTurn == 1);
        List<Integer> playable = new ArrayList<>();

        while (true) {
            int index = -1;

            if (playerTurn == 1) {
                System.out.println("Input width/depth:");
                index = scanner.nextInt();
                myGame.play(index, false);
                playerTurn = 0;
            } else {
                int winIndex = -1;
                for (int testIndex = 0; testIndex < myGame.getCurrBounds(); testIndex++) {
                    myGame.play(testIndex, true);
                    if (myGame.isGameWon() == null) {
                        playable.add(testIndex);
                    } else if ((playerFirst && !myGame.isGameWon()) || (!playerFirst && myGame.isGameWon())) {
                        winIndex = testIndex;
                        myGame.undo(true);
                        break;
                    }
                    myGame.undo(true);
                }
                winIndex = (winIndex != -1) ? winIndex :
                        ((playable.isEmpty()) ? rand.nextInt(myGame.getCurrBounds()) : playable.getFirst());
                myGame.play(winIndex, false);
                playerTurn = 1;
            }
        }
        
    }
}