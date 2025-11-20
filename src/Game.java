import java.util.*;

public class Game {
    private final int BOARD_DEPTH = 6;
    private final int BOARD_WIDTH = 7;
    private Boolean[][] board;
    private boolean isPlayer1;
    private boolean gameWon;
    private String gravity;
    private HashMap<Integer, Integer> playMap;
    private HashMap<Integer, Integer> widthMap;
    private HashMap<Integer, Integer> depthMap;

    private HashMap<Integer, Integer> prev_map;
    private Boolean[][] prev_board;
    private String prev_gravity;
    public Game() {
        reset();
    }

    public boolean undo(boolean silent) {
        if (Arrays.deepEquals(prev_board, board)) {
            System.out.println("Undo failure.");
            return false;
        }
        isPlayer1 = !isPlayer1;

        board = new Boolean[prev_board.length][];
        for (int i = 0; i < prev_board.length; i++) {
            board[i] = prev_board[i].clone();  // copies each row
        }
        gravity = prev_gravity;
        playMap = new HashMap<>(prev_map);
        gameWon = false;

        if (!silent) {
            printState();
        }
        return true;
    }

    public boolean play(int cur, boolean silent) {
        if (gameWon) {
            if (!silent) {
                printState();
            }
            return true;
        }

        if (cur < 0 || cur >= getCurrBounds()) {
            System.out.println(STR."Width cannot be negative or greater than \{getCurrBounds()}.");
            return false;
        }

        prev_board = new Boolean[board.length][];
        for (int i = 0; i < board.length; i++) {
            prev_board[i] = board[i].clone();  // copies each row
        }
        prev_gravity = gravity;
        prev_map = new HashMap<>(playMap);

        Boolean check;
        if (gravity.equals("S") || gravity.equals("N")) {
            check = playWidth(cur);
        } else {
            check = playDepth(cur);
        }

        if (check != null) {
            isPlayer1 = check;
            gameWon = true;
        } else {
            isPlayer1 = !isPlayer1;
        }
        if (!silent) {
            printState();
        }
        return true;
    }

    private Boolean playWidth(int curWidth) {
        int curDepth = playMap.get(curWidth);
        if (curDepth == -1) {
            System.out.println("Column is currently full.");
            return false;
        }
        board[curWidth][curDepth] = isPlayer1;
        if (gravity.equals("S")) {
            playMap.put(curWidth, curDepth - 1);
        } else {
            playMap.put(curWidth, curDepth + 1);
        }

        return checkState(curWidth, curDepth, true);
    }

    private Boolean playDepth(int curDepth) {
        int curWidth = playMap.get(curDepth);
        if (curWidth == -1) {
            System.out.println("Column is currently full.");
            return false;
        }
        board[curWidth][curDepth] = isPlayer1;

        if (gravity.equals("E")) {
            playMap.put(curDepth, curWidth - 1);
        } else {
            playMap.put(curDepth, curWidth + 1);
        }

        return checkState(curWidth, curDepth, true);
    }
    private Boolean checkState(int curWidth, int curDepth, boolean shouldRotate) {

        int NWsum = checkDirectional(curWidth - 1, curDepth - 1, "NW")
                + checkDirectional(curWidth + 1, curDepth + 1, "SE") + 1;

        int NEsum = checkDirectional(curWidth + 1, curDepth - 1, "NE")
                + checkDirectional(curWidth - 1, curDepth + 1, "SW") + 1;

        int Wsum = checkDirectional(curWidth - 1, curDepth, "W")
                + checkDirectional(curWidth + 1, curDepth, "E") + 1;

        int Ssum = checkDirectional(curWidth, curDepth + 1, "S")
                + checkDirectional(curWidth, curDepth - 1, "N") + 1;

        if (NWsum >= 4 || NEsum >= 4 || Wsum >= 4 || Ssum >= 4) {
            return board[curWidth][curDepth];
        } else if (shouldRotate && (NWsum == 3 || NEsum == 3 || Wsum == 3 || Ssum == 3)) {
            rotate();
            ArrayList<Boolean> winners = new ArrayList<>(2);
            for (int w = 0; w < BOARD_WIDTH; w++) {
                Boolean result =  checkState(w, 2, false);
                if (result != null && !winners.contains(result)) {
                    winners.add(result);
                }
                if (winners.size() == 2) {
                    return !isPlayer1;
                }
            }
             // Need to track current location.
            for (int d = 0; d < BOARD_DEPTH; d++) {
                Boolean result =  checkState(3, d, false);
                if (result != null && !winners.contains(result)) {
                    winners.add(result);
                }
                if (winners.size() == 2) {
                    return !isPlayer1;
                }
            }
            return (winners.isEmpty()) ? null : winners.getFirst();
        }
        return null;
    }

//    private Boolean checkWinState(int curWidth, int curDepth) {
//
//        int NWsum = checkDirectional(curWidth - 1, curDepth - 1, "NW")
//                + checkDirectional(curWidth + 1, curDepth + 1, "SE") + 1;
//
//        int NEsum = checkDirectional(curWidth + 1, curDepth - 1, "NE")
//                + checkDirectional(curWidth - 1, curDepth + 1, "SW") + 1;
//
//        int Wsum = checkDirectional(curWidth - 1, curDepth, "W")
//                + checkDirectional(curWidth + 1, curDepth, "E") + 1;
//
//        int Ssum = checkDirectional(curWidth, curDepth + 1, "S")
//                + checkDirectional(curWidth, curDepth - 1, "N") + 1;
//
//        if (NWsum >= 4 || NEsum >= 4 || Wsum >= 4 || Ssum >= 4) {
//            return board[curWidth][curDepth];
//        }
//        return null;
//    }

    private void rotate() {
        changeGravity();
        switch (gravity) {
            case "N":
                fallNorth();
                break;
            case "S":
                fallSouth();
                break;
            case "E":
                fallEast();
                break;
            case "W":
                fallWest();
                break;
            default:
        }
    }

    private void fallEast() {
        depthMap.clear();
        for (int d = 0; d < BOARD_DEPTH; d++) {
            int nextWidth = BOARD_WIDTH - 1;
            for (int w = BOARD_WIDTH - 1; w >= 0; w--) {
                if (board[w][d] == null || nextWidth == -1) {
                    continue;
                }
                Boolean state = board[w][d];
                board[w][d] = null;
                board[nextWidth--][d] = state;
            }
            depthMap.put(d, nextWidth);
        }
        playMap = depthMap;
    }

    private void fallWest() {
        depthMap.clear();
        for (int d = 0; d < BOARD_DEPTH; d++) {
            int nextWidth = 0;
            for (int w = 0; w < BOARD_WIDTH; w++) {
                if (board[w][d] == null || nextWidth == BOARD_WIDTH) {
                    continue;
                }
                Boolean state = board[w][d];
                board[w][d] = null;
                board[nextWidth++][d] = state;
            }
            depthMap.put(d, nextWidth);
        }
        playMap = depthMap;
    }

    private void fallNorth() {
        widthMap.clear();
        for (int w = 0; w < BOARD_WIDTH; w++) {
            int nextDepth = 0;
            for (int d = 0; d < BOARD_DEPTH; d++) {
                if (board[w][d] == null || nextDepth == BOARD_DEPTH) {
                    continue;
                }
                Boolean state = board[w][d];
                board[w][d] = null;
                board[w][nextDepth++] = state;
            }
            widthMap.put(w, nextDepth);
        }
        playMap = widthMap;
    }

    private void fallSouth() {
        widthMap.clear();
        for (int w = 0; w < BOARD_WIDTH; w++) {
            int nextDepth = BOARD_DEPTH - 1;
            for (int d = BOARD_DEPTH - 1; d >= 0; d--) {
                if (board[w][d] == null || nextDepth == -1) {
                    continue;
                }
                Boolean state = board[w][d];
                board[w][d] = null;
                board[w][nextDepth--] = state;
            }
            widthMap.put(w, nextDepth);
        }
        playMap = widthMap;
    }
    private void changeGravity() {
        gravity = getNextGravity(false);
    }

    private int checkDirectional(int curWidth, int curDepth, String direction) {
        if (curWidth < 0 || curDepth < 0 || curDepth >= BOARD_DEPTH
                || curWidth >= BOARD_WIDTH || board[curWidth][curDepth] == null) {
            return 0;
        }
        Boolean cur = board[curWidth][curDepth];
        switch (direction) {
            case "S":
                if (cur != board[curWidth][curDepth - 1]) {
                    return 0;
                }
                curDepth++;
                break;
            case "W":
                if (cur != board[curWidth + 1][curDepth]) {
                    return 0;
                }
                curWidth--;
                break;
            case "E":
                if (cur != board[curWidth - 1][curDepth]) {
                    return 0;
                }
                curWidth++;
                break;
            case "N":
                if (cur != board[curWidth][curDepth + 1]) {
                    return 0;
                }
                curDepth--;
                break;
            case "NE":
                if (cur != board[curWidth - 1][curDepth + 1]) {
                    return 0;
                }
                curWidth++;
                curDepth--;
                break;
            case "NW":
                if (cur != board[curWidth + 1][curDepth + 1]) {
                    return 0;
                }
                curWidth--;
                curDepth--;
                break;
            case "SE":
                if (cur != board[curWidth - 1][curDepth - 1]) {
                    return 0;
                }
                curWidth++;
                curDepth++;
                break;
            case "SW":
                if (cur != board[curWidth + 1][curDepth - 1]) {
                    return 0;
                }
                curWidth--;
                curDepth++;
                break;
            default:
                return Integer.MIN_VALUE;

        }
        return 1 + checkDirectional(curWidth, curDepth, direction);
    }

    public void printState() {
        int FRAME_LEN = 65;

        String currGravity = switch (gravity) {
            case "N" -> "North (^)";
            case "E" -> "East  (>)";
            case "S" -> "South (v)";
            case "W" -> "West  (<)";
            default -> "ERR";
        };

        String nextGravity = getNextGravity(true);

        System.out.println("=".repeat(FRAME_LEN));
        System.out.println("-".repeat(FRAME_LEN));

        if (gameWon) {
            System.out.println(STR."\t\t\t\t\t*** Player \{isPlayer1 ? "X" : "O"} has won! ***");
        } else {
            System.out.println(STR."\t\t\t\t\t   Player \{isPlayer1 ? "X" : "O"}'s turn.");
        }
        System.out.println(STR."\t\t\t\t\tCurr Gravity:\t\{currGravity}");
        System.out.println(STR."\t\t\t\t\tNext shift:\t\t\{nextGravity}");

        System.out.println(this);

        System.out.println("-".repeat(FRAME_LEN));
        System.out.println("=".repeat(FRAME_LEN));
        System.out.println();
    }

    private String getNextGravity(boolean extended) {
        return switch (gravity) {
            case "N" -> (!extended) ? "E" : "East  (>)";
            case "E" -> (!extended) ? "S" : "South (v)";
            case "S" -> (!extended) ? "W" : "West  (<)";
            case "W" -> (!extended) ? "N" : "North (^)";
            default -> "ERR";
        };
    }

    public void reset() {
        board = new Boolean[BOARD_WIDTH][BOARD_DEPTH];
        prev_board = new Boolean[BOARD_WIDTH][BOARD_DEPTH];
        isPlayer1 = true;
        playMap = new HashMap<>();
        widthMap = new HashMap<>();
        depthMap = new HashMap<>();
        gameWon = false;
        gravity = "S";
        for (int i = 0; i < BOARD_WIDTH; i++) {
            playMap.put(i, BOARD_DEPTH - 1);
        }
    }
    @Override
    public String toString() {
        int FRAME_LEN = 57;
        StringBuilder retStr = new StringBuilder();
        for (int curD = -1; curD < BOARD_DEPTH; curD++) {
            retStr.append("\t");
            retStr.append("-".repeat(FRAME_LEN));
            retStr.append("\n");
            retStr.append("\t");
            if (curD != -1) {
                if (gravity.equals("W") || gravity.equals("E")) {
                    retStr.append(STR."[\{curD}]\t");
                } else {
                    retStr.append("[-]\t");
                }
            } else {
                retStr.append("\t");
            }
            for (int curW = 0; curW < BOARD_WIDTH; curW++) {
                if (curD == -1) {
                    if (gravity.equals("N") || gravity.equals("S")) {
                        retStr.append(STR."[ \{curW} ]\t");
                    } else {
                        retStr.append("[ - ]\t");
                    }
                } else {
                    if (board[curW][curD] == null) {
                        retStr.append("(   )\t");
                    } else if (board[curW][curD]) {
                        retStr.append("( X )\t");
                    } else {
                        retStr.append("( O )\t");
                    }
                }
            }
            retStr.append("\n");
        }
        retStr.append("\t");
        retStr.append("-".repeat(FRAME_LEN));
        retStr.append("\n");
        return retStr.toString();
    }

    public int getCurrBounds() {
        return (gravity.equals("N") || gravity.equals("S")) ? BOARD_WIDTH : BOARD_DEPTH;
    }

    public Boolean isGameWon() {
        return (gameWon) ? isPlayer1 : null;
    }
}
