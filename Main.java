import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        MazeSolver mazeSolver = new MazeSolver();
        mazeSolver.run();
    }
}

class MazeSolver {

    int startRow;
    int startCol;
    String[] maze;
    List<Coordinate> path;
    Coordinate goal;
    CoordinateComparator comparator;

    public void run() {
        try {
            comparator = new CoordinateComparator();
            path = new ArrayList<>();
            String content = new String(Files.readAllBytes(Paths.get("Maze2.txt")));
            content = content.replaceAll("\r", "");
            maze = content.split("\n");
            maze = translateWalls(maze);
            Coordinate startCoordinate = new Coordinate(startRow, startCol);
            printMaze();
            heuristicSearch(startCoordinate);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] translateWalls(String[] maze) {
        ArrayList<String> newMaze = new ArrayList<>();
        for (int row = 0; row < maze.length; row++) {
            char[] charArray = maze[row].toCharArray();
            StringBuilder newRow = new StringBuilder();
            for (int col = 0; col <= charArray.length - 1; col++) {
                boolean north, south, east, west;
                if (maze[row].toCharArray()[col] != '#') {
                    if (maze[row].toCharArray()[col] == 'S') {
                        startRow = row;
                        startCol = col;
                    }
                    if(maze[row].toCharArray()[col] == 'G'){
                        goal = new Coordinate(row, col);
                    }
                    newRow.append(maze[row].toCharArray()[col]);
                }
                else {

                    if (row == 0)
                        north = false;
                    else north = maze[row - 1].toCharArray()[col] == '#';

                    if (row == maze.length - 1)
                        south = false;
                    else south = maze[row + 1].toCharArray()[col] == '#';

                    if (col == maze[row].length() - 1)
                        east = false;
                    else east = maze[row].toCharArray()[col + 1] == '#';

                    if (col == 0)
                        west = false;
                    else west = maze[row].toCharArray()[col - 1] == '#';

                    if (!north && south && east && !west) {
                        newRow.append('L');
                    }else
                    if (!north && !south) {
                        newRow.append('H');
                    } else if (!north && west && east) {
                        newRow.append('D');
                    } else if (!north && west) {
                        newRow.append('R');
                    } else if (!east && !west) {
                        newRow.append('V');
                    } else if (!south && !east) {
                        newRow.append('r');
                    } else if (!south && !west) {
                        newRow.append('l');
                    } else if (south && east && !west) {
                        newRow.append('b');
                    } else if (south && !east) {
                        newRow.append('d');
                    } else if (south) {
                        newRow.append('+');
                    } else {
                        newRow.append('u');
                    }
                }
            }
            newMaze.add(String.valueOf(newRow));
        }
        for (String s : newMaze) {
            System.out.println(s);
        }
        return newMaze.toArray(new String[0]);
    }

    private String charParser(char c) {
        return switch (c) {
            case 'L' -> "┌─";
            case 'H' -> "──";
            case 'D' -> "┬─";
            case 'R' -> "┐ ";
            case 'V' -> "│ ";
            case 'S' -> "S ";
            case 'G' -> "G ";
            case 'r' -> "┘ ";
            case 'l' -> "└─";
            case 'b' -> "├─";
            case '+' -> "┼─";
            case 'u' -> "┴─";
            case 'd' -> "┤ ";
            case ' ' -> "  ";
            case '.' -> "\u001B[32m. \u001B[0m";
            default -> "\u001B[31m? \u001B[0m";
        };
    }

    private boolean heuristicSearch(Coordinate currentCoordinate){
        if (currentCoordinate.isEqual(goal)) {
            System.out.println(path);
            showSolution(path);
            System.out.println("Goal found in " + path.size() + " steps");
            return true;
        }
        List<Coordinate> nextSteps = findAllNextSteps(currentCoordinate);
        if (nextSteps.size() == 0) {
            path.remove(currentCoordinate);
            return false;
        }
        nextSteps.sort(new CoordinateComparator());
        for (Coordinate nextStep : nextSteps) {
            path.add(nextStep);
            if (heuristicSearch(nextStep)) {
                return true;
            }
        }
        path.remove(currentCoordinate);
        return false;

    }

    private boolean depthFirstSearch(Coordinate currentCoordinate) {
        int row = currentCoordinate.row;
        int col = currentCoordinate.col;
        if (maze[row].charAt(col) == 'G') {
            System.out.println(path);
            showSolution(path);
            System.out.println("Goal found in " + path.size() + " steps");
            return true;
        }
        List<Coordinate> nextSteps = findAllNextSteps(currentCoordinate);
        if (nextSteps.size() == 0) {
            path.remove(currentCoordinate);
            return false;
        }
        for (Coordinate nextStep : nextSteps) {
            path.add(nextStep);
            if (depthFirstSearch(nextStep)) {
                return true;
            }
        }
        path.remove(currentCoordinate);
        return false;
    }

    private void showSolution(List<Coordinate> path) {
        for (Coordinate coordinate : path) {
            StringBuilder stringBuilder = new StringBuilder(maze[coordinate.row]);
            if (maze[coordinate.row].charAt(coordinate.col) != 'G')
                stringBuilder.setCharAt(coordinate.col, '.');
            maze[coordinate.row] = String.valueOf(stringBuilder);
        }

        printMaze();
    }

    private List<Coordinate> findAllNextSteps(Coordinate coordinate) {
        List<Coordinate> path = coordinate.cameFrom;
        path.add(coordinate);
        int row = coordinate.row;
        int col = coordinate.col;
        List<Coordinate> neighbors = new ArrayList<>();
        if (col != maze[row].length() - 1 && (maze[row].charAt(col + 1) == ' ' || maze[row].charAt(col + 1) == 'G')) {
            Coordinate nextStep = new Coordinate(row, col + 1, path);
            if (!coordinate.cameFrom(nextStep))
                neighbors.add(nextStep);
        }
        if (row != 0 && (maze[row - 1].charAt(col) == ' ' || maze[row - 1].charAt(col) == 'G')) {
            Coordinate nextStep = new Coordinate(row - 1, col, path);
            if (!coordinate.cameFrom(nextStep))
                neighbors.add(nextStep);
        }
        if (row != maze.length && (maze[row + 1].charAt(col) == ' ' || maze[row + 1].charAt(col) == 'G')) {
            Coordinate nextStep = new Coordinate(row + 1, col, path);
            if (!coordinate.cameFrom(nextStep))
                neighbors.add(nextStep);
        }
        if (col != 0 && (maze[row].charAt(col - 1) == ' ' || maze[row].charAt(col - 1) == 'G')) {
            Coordinate nextStep = new Coordinate(row, col - 1, path);
            if (!coordinate.cameFrom(nextStep))
                neighbors.add(nextStep);
        }
        return neighbors;
    }

    private void printMaze() {

        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[i].toCharArray().length; j++) {
                char c = maze[i].charAt(j);
                System.out.print(charParser(c));
            }
            System.out.println();
        }

    }


    static class Coordinate {
        int row, col;
        List<Coordinate> cameFrom;

        public Coordinate(int row, int col) {
            this.row = row;
            this.col = col;
            cameFrom = new ArrayList<>();
        }

        public Coordinate(int row, int col, List<Coordinate> cameFrom) {
            this.row = row;
            this.col = col;
            this.cameFrom = cameFrom;
        }

        public boolean isEqual(Coordinate other) {
            return other.row == this.row && other.col == this.col;
        }

        @Override
        public String toString() {
            return String.format("(%d, %d)", row, col);
        }

        public boolean cameFrom(Coordinate nextStep) {
            if (this.cameFrom == null)
                cameFrom = new ArrayList<>();
            for (Coordinate coordinate : cameFrom) {
                if (nextStep.row == coordinate.row && nextStep.col == coordinate.col)
                    return true;
            }
            return false;
        }
    }

    class CoordinateComparator implements Comparator<MazeSolver.Coordinate>{
        @Override
        public int compare(MazeSolver.Coordinate o1, MazeSolver.Coordinate o2) {
            return Math.abs(o1.row - goal.row) - Math.abs(o1.col - goal.col);
        }
    }
}
