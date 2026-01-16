import java.util.*;

public class PuzzleSolver {
    // ===================== PUZZLE GENERATOR =====================
    static class PuzzleGenerator {
        static Random rand = new Random();

        // Piezas fijas para 3x3 (rango 0-9)
        public static List<Piece> getFixed3x3() {
            List<Piece> pieces = new ArrayList<>();
            pieces.add(new Piece(0, 9, 1, 1, 4));
            pieces.add(new Piece(1, 6, 2, 9, 4));
            pieces.add(new Piece(2, 0, 7, 0, 1));
            pieces.add(new Piece(3, 8, 0, 6, 0));
            pieces.add(new Piece(4, 6, 0, 0, 9));
            pieces.add(new Piece(5, 0, 8, 8, 6));
            pieces.add(new Piece(6, 6, 8, 6, 8));
            pieces.add(new Piece(7, 0, 9, 4, 0));
            pieces.add(new Piece(8, 6, 0, 0, 8));
            return pieces;
        }

        // Generar piezas aleatorias válidas
        public static List<Piece> generateRandom(int size, int maxValue) {
            List<Piece> pieces = new ArrayList<>();
            int totalPieces = size * size;

            // Generar piezas aleatorias
            for (int i = 0; i < totalPieces; i++) {
                int top = rand.nextInt(maxValue + 1);
                int right = rand.nextInt(maxValue + 1);
                int bottom = rand.nextInt(maxValue + 1);
                int left = rand.nextInt(maxValue + 1);
                pieces.add(new Piece(i, top, right, bottom, left));
            }

            return pieces;
        }

        // Generar con solución garantizada
        public static List<Piece> generateWithSolution(int size, int maxValue) {
            List<Piece> pieces = new ArrayList<>();
            int[][] topEdges = new int[size][size];
            int[][] rightEdges = new int[size][size];
            int[][] bottomEdges = new int[size][size];
            int[][] leftEdges = new int[size][size];

            // Generar bordes aleatorios pero consistentes
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    // Top
                    if (i == 0) {
                        topEdges[i][j] = rand.nextInt(maxValue + 1);
                    } else {
                        topEdges[i][j] = bottomEdges[i - 1][j];
                    }

                    // Left
                    if (j == 0) {
                        leftEdges[i][j] = rand.nextInt(maxValue + 1);
                    } else {
                        leftEdges[i][j] = rightEdges[i][j - 1];
                    }

                    // Right y Bottom aleatorios
                    rightEdges[i][j] = rand.nextInt(maxValue + 1);
                    bottomEdges[i][j] = rand.nextInt(maxValue + 1);
                }
            }

            // Crear piezas en orden aleatorio
            List<int[]> positions = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    positions.add(new int[] { i, j });
                }
            }
            Collections.shuffle(positions);

            for (int idx = 0; idx < positions.size(); idx++) {
                int i = positions.get(idx)[0];
                int j = positions.get(idx)[1];
                pieces.add(new Piece(idx, topEdges[i][j], rightEdges[i][j],
                        bottomEdges[i][j], leftEdges[i][j]));
            }

            return pieces;
        }
    }

    // ===================== FITNESS EVALUATOR =====================
    static class FitnessEvaluator {
        public static int evaluate(Board board) {
            return board.countMatches();
        }

        public static int getMaxFitness(int size) {
            // Máximo de matches posibles
            return 2 * size * (size - 1);
        }
    }

    // ===================== MAIN =====================
    public static void main(String[] args) {
        System.out.println("=== PUZZLE SOLVER - PERSONA 1 ===\n");

        // Test 1: Piezas fijas 3x3
        System.out.println("--- Test 1: Piezas fijas 3x3 (rango 0-9) ---");
        List<Piece> pieces3x3 = PuzzleGenerator.getFixed3x3();
        System.out.println("Total piezas: " + pieces3x3.size());
        for (Piece p : pieces3x3) {
            System.out.println(p);
        }

        // Test 2: Tablero 3x3
        System.out.println("\n--- Test 2: Tablero 3x3 vacío ---");
        Board board = new Board(3);
        System.out.println("Tamaño: " + board.size);
        System.out.println("Fitness máximo posible: " + FitnessEvaluator.getMaxFitness(3));

        // Test 3: Colocar algunas piezas
        System.out.println("\n--- Test 3: Colocar piezas ---");
        board.place(pieces3x3.get(0), 0, 0);
        board.place(pieces3x3.get(1), 0, 1);
        board.print();
        System.out.println("Fitness actual: " + FitnessEvaluator.evaluate(board));
        System.out.println("¿Puede colocar pieza 2 en (0,2)? " + board.canPlace(pieces3x3.get(2), 0, 2));

        // Test 4: Generar piezas aleatorias 5x5
        System.out.println("\n--- Test 4: Piezas aleatorias 5x5 (rango 0-9) ---");
        List<Piece> pieces5x5 = PuzzleGenerator.generateRandom(5, 9);
        System.out.println("Total piezas: " + pieces5x5.size());
        System.out.println("Primeras 5 piezas:");
        for (int i = 0; i < 5; i++) {
            System.out.println(pieces5x5.get(i));
        }

        // Test 5: Generar con solución garantizada
        System.out.println("\n--- Test 5: Piezas con solución 3x3 (rango 0-9) ---");
        List<Piece> piecesValid = PuzzleGenerator.generateWithSolution(3, 9);
        System.out.println("Total piezas: " + piecesValid.size());
        for (Piece p : piecesValid) {
            System.out.println(p);
        }

        // Test 6: Tamaños del proyecto
        System.out.println("\n--- Test 6: Tamaños del proyecto ---");
        int[] sizes = { 3, 5, 10, 15, 30, 60, 100 };
        for (int size : sizes) {
            System.out.println("Tamaño " + size + "x" + size + ": " +
                    (size * size) + " piezas, fitness máximo = " +
                    FitnessEvaluator.getMaxFitness(size));
        }

        //Test fuerza bruta - Persona 2 xd
        System.out.println("\n--- Test fuerza bruta ---");
        Board boardFuerzaBruta = new Board(3);
        List<Piece> piezasFuerzaBruta = PuzzleGenerator.generateWithSolution(3, 9);
        System.out.println("\n--- Piezas ---");
        for (Piece p : piezasFuerzaBruta) {
            System.out.println(p);
        }
         BruteForceSolver solverForce = new BruteForceSolver(boardFuerzaBruta, piezasFuerzaBruta);
         if(solverForce.solve()){
            System.out.println("Se encontro solución:");
            boardFuerzaBruta.print();
         }else{
            System.out.println("No se encontro solución");
            boardFuerzaBruta.print();
         }
       
    }
}
