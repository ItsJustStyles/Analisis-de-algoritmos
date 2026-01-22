package proyectoanalisis;

import java.util.*;

/**
 * PuzzleSolver.java
 *
 * Un solo archivo .java (un solo main) que incluye:
 * - Modelo: Piece, Board
 * - Generacion: PuzzleGenerator (incluye 3x3 "quemado")
 * - FitnessEvaluator
 * - Algoritmos: BruteForceSolver, AvanceRapido, GeneticSolver
 * - Medicion: tiempo, memoria, comparaciones y asignaciones
 * - Reporting genetico: cruces, mutaciones y top 3 al final
 *
 * Importante:
 * - La generacion de piezas NO se cuenta dentro de las mediciones.
 * - Para tamanos grandes, fuerza bruta y avance rapido pueden ser intratables,
 *   asi que se usan timeouts (el enunciado dice "cuando el tamano lo permita").
 */
public class PuzzleSolver {

    // ============================================================
    // ========================== SETTINGS =========================
    // ============================================================
    static final int[] SIZES = {3, 5, 10, 15, 30, 60, 100};

    // Timeouts (ms) para evitar cuelgues en tamanos grandes
    static final long MAX_MS_BRUTEFORCE = 4000;
    static final long MAX_MS_FAST       = 8000;
    static final long MAX_MS_GENETIC    = 12000;

    // Imprimir todas las piezas solo en puzzles pequenos
    static final int PRINT_PIECES_MAX_SIZE = 5;

    // Evitar StackOverflow por profundidad recursiva muy grande (p.ej. 100x100)
    static final int MAX_CELLS_RECURSIVE = 5000; // 60x60=3600 OK, 100x100=10000 se omite

    // ============================================================
    // ===================== PIECE (companeros) ====================
    // ============================================================
    static class Piece {
        int top, right, bottom, left;
        int id;

        public Piece(int id, int top, int right, int bottom, int left) {
            this.id = id;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.left = left;
        }

        @Override
        public String toString() {
            return String.format("[%d:%d,%d,%d,%d]", id, top, right, bottom, left);
        }
    }

    // ============================================================
    // ===================== BOARD (companeros) ====================
    // ============================================================
    static class Board {
        int size;
        Piece[][] grid;

        public Board(int size) {
            this.size = size;
            this.grid = new Piece[size][size];
        }

        public boolean canPlace(Piece piece, int row, int col) {
            // Verificar arriba
            if (row > 0 && grid[row - 1][col] != null) {
                if (grid[row - 1][col].bottom != piece.top)
                    return false;
            }
            // Verificar izquierda
            if (col > 0 && grid[row][col - 1] != null) {
                if (grid[row][col - 1].right != piece.left)
                    return false;
            }
            return true;
        }

        public void place(Piece piece, int row, int col) {
            grid[row][col] = piece;
        }

        public void remove(int row, int col) {
            grid[row][col] = null;
        }

        public int countMatches() {
            int matches = 0;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (grid[i][j] != null) {
                        // Derecha
                        if (j < size - 1 && grid[i][j + 1] != null) {
                            if (grid[i][j].right == grid[i][j + 1].left)
                                matches++;
                        }
                        // Abajo
                        if (i < size - 1 && grid[i + 1][j] != null) {
                            if (grid[i][j].bottom == grid[i + 1][j].top)
                                matches++;
                        }
                    }
                }
            }
            return matches;
        }

        public void print() {
            for (int i = 0; i < size; i++) {
                // Linea superior
                for (int j = 0; j < size; j++) {
                    if (grid[i][j] != null) {
                        System.out.printf("  %2d  ", grid[i][j].top);
                    } else {
                        System.out.print("  -  ");
                    }
                    System.out.print("  ");
                }
                System.out.println();

                // Linea media
                for (int j = 0; j < size; j++) {
                    if (grid[i][j] != null) {
                        System.out.printf("%2d[%2d]%2d", grid[i][j].left, grid[i][j].id, grid[i][j].right);
                    } else {
                        System.out.print(" [  ] ");
                    }
                }
                System.out.println();

                // Linea inferior
                for (int j = 0; j < size; j++) {
                    if (grid[i][j] != null) {
                        System.out.printf("  %2d  ", grid[i][j].bottom);
                    } else {
                        System.out.print("  -  ");
                    }
                    System.out.print("  ");
                }
                System.out.println("\n");
            }
        }
    }

    // ============================================================
    // ===================== PUZZLE GENERATOR ======================
    // (base de companeros + 3x3 "quemado" SOLUCIONABLE)
    // ============================================================
    static class PuzzleGenerator {
        static Random rand = new Random();

        // Piezas fijas para 3x3 (rango 0..9) - quemadas y SOLUCIONABLES
        public static List<Piece> getFixed3x3_0_9() {
            // Solucion (por filas):
            // (0,0) t=1 l=2 r=3 b=4
            // (0,1) t=5 l=3 r=6 b=7
            // (0,2) t=8 l=6 r=9 b=0
            // (1,0) t=4 l=1 r=2 b=3
            // (1,1) t=7 l=2 r=4 b=5
            // (1,2) t=0 l=4 r=6 b=7
            // (2,0) t=3 l=8 r=9 b=1
            // (2,1) t=5 l=9 r=0 b=2
            // (2,2) t=7 l=0 r=3 b=4
            List<Piece> pieces = new ArrayList<>();
            pieces.add(new Piece(0, 1, 3, 4, 2));
            pieces.add(new Piece(1, 5, 6, 7, 3));
            pieces.add(new Piece(2, 8, 9, 0, 6));
            pieces.add(new Piece(3, 4, 2, 3, 1));
            pieces.add(new Piece(4, 7, 4, 5, 2));
            pieces.add(new Piece(5, 0, 6, 7, 4));
            pieces.add(new Piece(6, 3, 9, 1, 8));
            pieces.add(new Piece(7, 5, 0, 2, 9));
            pieces.add(new Piece(8, 7, 3, 4, 0));
            return pieces;
        }

        // Piezas fijas para 3x3 (rango 0..15) - quemadas y SOLUCIONABLES
        public static List<Piece> getFixed3x3_0_15() {
            // Solucion (por filas):
            // (0,0) t=11 l=3 r=7 b=14
            // (0,1) t=5  l=7 r=2 b=9
            // (0,2) t=15 l=2 r=6 b=0
            // (1,0) t=14 l=1 r=12 b=8
            // (1,1) t=9  l=12 r=4 b=13
            // (1,2) t=0  l=4 r=10 b=7
            // (2,0) t=8  l=6 r=5 b=2
            // (2,1) t=13 l=5 r=1 b=11
            // (2,2) t=7  l=1 r=9 b=4
            List<Piece> pieces = new ArrayList<>();
            pieces.add(new Piece(0, 11, 7, 14, 3));
            pieces.add(new Piece(1, 5, 2, 9, 7));
            pieces.add(new Piece(2, 15, 6, 0, 2));
            pieces.add(new Piece(3, 14, 12, 8, 1));
            pieces.add(new Piece(4, 9, 4, 13, 12));
            pieces.add(new Piece(5, 0, 10, 7, 4));
            pieces.add(new Piece(6, 8, 5, 2, 6));
            pieces.add(new Piece(7, 13, 1, 11, 5));
            pieces.add(new Piece(8, 7, 9, 4, 1));
            return pieces;
        }

        // Generar piezas aleatorias (NO garantiza solucion)
        public static List<Piece> generateRandom(int size, int maxValue) {
            List<Piece> pieces = new ArrayList<>();
            int totalPieces = size * size;

            for (int i = 0; i < totalPieces; i++) {
                int top = rand.nextInt(maxValue + 1);
                int right = rand.nextInt(maxValue + 1);
                int bottom = rand.nextInt(maxValue + 1);
                int left = rand.nextInt(maxValue + 1);
                pieces.add(new Piece(i, top, right, bottom, left));
            }

            return pieces;
        }

        // Generar con solucion garantizada (companeros)
        public static List<Piece> generateWithSolution(int size, int maxValue) {
            List<Piece> pieces = new ArrayList<>();
            int[][] topEdges = new int[size][size];
            int[][] rightEdges = new int[size][size];
            int[][] bottomEdges = new int[size][size];
            int[][] leftEdges = new int[size][size];

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
                pieces.add(new Piece(idx, topEdges[i][j], rightEdges[i][j], bottomEdges[i][j], leftEdges[i][j]));
            }

            return pieces;
        }
    }

    // ============================================================
    // ===================== FITNESS EVALUATOR =====================
    // ============================================================
    static class FitnessEvaluator {
        public static int evaluate(Board board) {
            return board.countMatches();
        }

        public static int getMaxFitness(int size) {
            return 2 * size * (size - 1);
        }
    }

    // ============================================================
    // ===================== METRICS / UTIL ========================
    // ============================================================
    static class Metrics {
        long timeMs;
        long memoryBytes;
        long comparisons;
        long assignments;
        boolean solved;
        int bestFitness;
        String note;
    }

    static long usedMemoryBytes() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    // ============================================================
    // ===================== BRUTE FORCE SOLVER ====================
    // (codigo de companeros + timeout)
    // ============================================================
    static class BruteForceSolver {
        private long comparaciones = 0;
        private long asignaciones = 0;

        private final Board board;
        private final List<Piece> pieces;
        private final Set<Integer> piezasUsadas;

        private final long deadlineNano;

        public BruteForceSolver(Board board, List<Piece> pieces, long maxMs) {
            this.board = board;
            this.pieces = pieces;
            this.piezasUsadas = new HashSet<>();
            this.asignaciones += 3;
            this.deadlineNano = System.nanoTime() + (maxMs * 1_000_000L);
        }

        public boolean solve() {
            return solucionarRecursivo(0, 0);
        }

        private boolean timedOut() {
            return System.nanoTime() > deadlineNano;
        }

        private boolean solucionarRecursivo(int fila, int columna) {
            if (timedOut()) return false;

            comparaciones++;
            if (fila == board.size) {
                return true;
            }

            int siguienteFila;
            int siguienteColumna;

            comparaciones++;
            if (columna == board.size - 1) {
                siguienteFila = fila + 1;
                siguienteColumna = 0;
                asignaciones += 2;
            } else {
                siguienteFila = fila;
                siguienteColumna = columna + 1;
                asignaciones += 2;
            }

            for (Piece p : pieces) {
                asignaciones++;
                comparaciones++;

                comparaciones++;
                if (!piezasUsadas.contains(p.id)) {

                    comparaciones++;
                    if (board.canPlace(p, fila, columna)) {
                        board.place(p, fila, columna);
                        piezasUsadas.add(p.id);
                        asignaciones += 2;

                        comparaciones++;
                        if (solucionarRecursivo(siguienteFila, siguienteColumna)) {
                            return true;
                        }

                        board.remove(fila, columna);
                        piezasUsadas.remove(p.id);
                        asignaciones += 2;
                    }
                }

                if (timedOut()) return false;
            }

            return false;
        }

        public long getComparaciones() { return comparaciones; }
        public long getAsignaciones() { return asignaciones; }
        public boolean isTimedOut() { return timedOut(); }
    }

    // ============================================================
    // ===================== AVANCE RAPIDO =========================
    // (codigo de companeros + timeout)
    // ============================================================
    static class AvanceRapido {
        private long comparaciones = 0;
        private long asignaciones = 0;

        private final Board board;
        private final List<Piece> pieces;
        private final Set<Integer> piezasUsadas;
        private final Map<Integer, List<Piece>> piezasPorIzquierda = new HashMap<>();
        private final Map<Integer, List<Piece>> piezasPorArriba = new HashMap<>();

        private final long deadlineNano;

        public AvanceRapido(Board board, List<Piece> pieces, long maxMs) {
            this.board = board;
            this.pieces = pieces;
            this.piezasUsadas = new HashSet<>();
            this.deadlineNano = System.nanoTime() + (maxMs * 1_000_000L);

            for (Piece p : pieces) {
                piezasPorArriba.putIfAbsent(p.top, new ArrayList<>());
                piezasPorArriba.get(p.top).add(p);

                piezasPorIzquierda.putIfAbsent(p.left, new ArrayList<>());
                piezasPorIzquierda.get(p.left).add(p);
            }

            this.asignaciones += 3;
        }

        private boolean timedOut() {
            return System.nanoTime() > deadlineNano;
        }

        public boolean solve() {
            return solucionarAvanceRapido(0, 0);
        }

        private boolean solucionarAvanceRapido(int fila, int columna) {
            if (timedOut()) return false;

            comparaciones++;
            if (fila == board.size) {
                return true;
            }

            int siguienteFila;
            int siguienteColumna;

            comparaciones++;
            if (columna == board.size - 1) {
                siguienteFila = fila + 1;
                siguienteColumna = 0;
                asignaciones += 2;
            } else {
                siguienteFila = fila;
                siguienteColumna = columna + 1;
                asignaciones += 2;
            }

            List<Piece> candidatos = pieces;
            if (columna > 0 && board.grid[fila][columna - 1] != null) {
                int valorBuscado = board.grid[fila][columna - 1].right;
                candidatos = piezasPorIzquierda.getOrDefault(valorBuscado, new ArrayList<>());
            } else if (fila > 0 && board.grid[fila - 1][columna] != null) {
                int valorBuscado = board.grid[fila - 1][columna].bottom;
                candidatos = piezasPorArriba.getOrDefault(valorBuscado, new ArrayList<>());
            }

            for (Piece p : candidatos) {
                comparaciones += 2;
                if (!piezasUsadas.contains(p.id) && board.canPlace(p, fila, columna)) {
                    board.place(p, fila, columna);
                    piezasUsadas.add(p.id);
                    asignaciones += 2;

                    comparaciones++;
                    if (solucionarAvanceRapido(siguienteFila, siguienteColumna)) {
                        return true;
                    }

                    board.remove(fila, columna);
                    piezasUsadas.remove(p.id);
                    asignaciones += 2;
                }

                if (timedOut()) return false;
            }

            return false;
        }

        public long getComparaciones() { return comparaciones; }
        public long getAsignaciones() { return asignaciones; }
        public boolean isTimedOut() { return timedOut(); }
    }

// ============================================================
// ===================== GENETIC SOLVER (persona 3) ============
// ============================================================
static class GeneticSolver {

    static class Individual {
        int[] perm;   // permutacion de indices de piezas (0..n-1)
        int fitness;
        long hash;
    }

    private final int size;
    private final int n;
    private final List<Piece> pieces;
    private final int maxFitness;

    private final int populationSize;
    private final int childrenCount;

    private final Random rand;
    private final long deadlineNano;

    // contadores
    private long comparisons = 0;
    private long assignments = 0;

    // imprimir modo compacto para tamanos grandes
    private final boolean compact;

    public GeneticSolver(int size, List<Piece> pieces, int populationSize, int childrenCount, long maxMs) {
        this.size = size;
        this.n = size * size;
        this.pieces = pieces;
        this.maxFitness = FitnessEvaluator.getMaxFitness(size);
        this.populationSize = populationSize;
        this.childrenCount = childrenCount;
        this.rand = new Random(999 + size + n);
        this.deadlineNano = System.nanoTime() + (maxMs * 1_000_000L);
        this.compact = (size > 5);
    }

    private boolean timedOut() {
        return System.nanoTime() > deadlineNano;
    }

    public Metrics solveAndReport() {
        Metrics m = new Metrics();

        long memBefore = usedMemoryBytes();
        long start = System.nanoTime();

        // 1) poblacion inicial (sin repetidos)
        List<Individual> population = createInitialPopulation();
        for (Individual ind : population) {
            ind.fitness = fitnessOf(ind.perm);
            assignments++;
        }
        sortByFitnessDesc(population);

        // 2) 10 generaciones
        for (int gen = 1; gen <= 10; gen++) {
            if (timedOut()) break;

            System.out.println("\n[GENETIC] ===== Generacion " + gen + " =====");
            System.out.println("[GENETIC] Mejor fitness actual: " + population.get(0).fitness + " / " + maxFitness);

            List<Individual> children = new ArrayList<>(childrenCount);

            // evitar cromosomas repetidos en la misma generacion
            Set<Long> seen = new HashSet<>(populationSize + childrenCount * 2);
            for (Individual p : population) seen.add(p.hash);

            int produced = 0;
            int crossPrintLimit = compact ? 5 : 15;
            int printed = 0;

            while (produced < childrenCount && !timedOut()) {
                Individual parent1 = tournamentSelect(population, 3);
                Individual parent2 = tournamentSelect(population, 3);

                Individual[] kids = orderCrossoverOX(parent1, parent2);

                // evaluar antes de imprimir (asi NO sale -1)
                kids[0].fitness = fitnessOf(kids[0].perm);
                kids[1].fitness = fitnessOf(kids[1].perm);
                assignments += 2;

                // mejora local ligera (hace al genetico MUCHO mas estable en 3x3 y 5x5)
                kids[0] = localImprove(kids[0]);
                kids[1] = localImprove(kids[1]);

                // mutacion + salida de duplicados
                kids[0] = resolveDuplicateWithMutationIfNeeded(kids[0], seen);
                kids[1] = resolveDuplicateWithMutationIfNeeded(kids[1], seen);

                if (printed < crossPrintLimit) {
                    printCross(parent1, parent2, kids[0], kids[1]);
                    printed++;
                    if (printed == crossPrintLimit) {
                        System.out.println("[CRUCE] (se omiten mas cruces en esta generacion para no saturar la salida)");
                    }
                }

                children.add(kids[0]);
                children.add(kids[1]);
                produced += 2;
            }

            // reemplazo: padres + hijos, quedan los mejores populationSize
            List<Individual> combined = new ArrayList<>(populationSize + children.size());
            combined.addAll(population);
            combined.addAll(children);

            sortByFitnessDesc(combined);
            population = new ArrayList<>(combined.subList(0, Math.min(populationSize, combined.size())));

            comparisons++;
            if (population.get(0).fitness == maxFitness) {
                System.out.println("[GENETIC] Solucion perfecta encontrada (fitness maximo).");
                break;
            }
        }

        sortByFitnessDesc(population);
        System.out.println("\n[GENETIC] ===== TOP 3 Poblaciones Finales =====");
        for (int i = 0; i < Math.min(3, population.size()); i++) {
            System.out.println((i + 1) + ") Fitness = " + population.get(i).fitness + " / " + maxFitness
                    + "  Cromosoma=" + chromosomeToString(population.get(i).perm));
        }

        // tablero del mejor
        Board bestBoard = buildBoardFromPerm(population.get(0).perm);

        long end = System.nanoTime();
        long memAfter = usedMemoryBytes();

        m.timeMs = (end - start) / 1_000_000L;
        m.memoryBytes = Math.max(0, memAfter - memBefore);
        m.comparisons = comparisons;
        m.assignments = assignments;
        m.bestFitness = population.get(0).fitness;
        m.solved = (m.bestFitness == maxFitness);
        m.note = timedOut() ? "timeout" : "ok";

        System.out.println("[GENETIC] Mejor solucion (tablero) - fitness " + m.bestFitness + " / " + maxFitness);
        if (size <= 10) bestBoard.print();
        else System.out.println("[GENETIC] (Tablero omitido por tamano, modo compacto)");

        return m;
    }

    // ---- poblacion inicial ----
    private List<Individual> createInitialPopulation() {
        List<Individual> pop = new ArrayList<>();
        Set<Long> seen = new HashSet<>();

        int attempts = 0;
        while (pop.size() < populationSize && attempts < populationSize * 400 && !timedOut()) {
            attempts++;
            int[] perm = randomPermutation(n, rand);
            long h = hashPerm(perm);

            comparisons++;
            if (!seen.contains(h)) {
                Individual ind = new Individual();
                ind.perm = perm;
                ind.hash = h;
                ind.fitness = -1;
                pop.add(ind);
                seen.add(h);
                assignments += 4;
            }
        }
        return pop;
    }

    // ---- fitness (cuenta lados que calzan) ----
    private int fitnessOf(int[] perm) {
        int fit = 0;
        for (int r = 0; r < size; r++) {
            int base = r * size;
            for (int c = 0; c < size; c++) {
                int idx = base + c;
                Piece p = pieces.get(perm[idx]);

                // derecha
                comparisons++;
                if (c < size - 1) {
                    Piece pr = pieces.get(perm[idx + 1]);
                    comparisons++;
                    if (p.right == pr.left) fit++;
                }
                // abajo
                comparisons++;
                if (r < size - 1) {
                    Piece pb = pieces.get(perm[idx + size]);
                    comparisons++;
                    if (p.bottom == pb.top) fit++;
                }
            }
        }
        return fit;
    }

    private Board buildBoardFromPerm(int[] perm) {
        Board b = new Board(size);
        int k = 0;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                b.place(pieces.get(perm[k]), r, c);
                k++;
            }
        }
        return b;
    }

    // ---- seleccion ----
    private Individual tournamentSelect(List<Individual> pop, int k) {
        Individual best = null;
        for (int i = 0; i < k; i++) {
            int idx = rand.nextInt(pop.size());
            Individual cand = pop.get(idx);
            comparisons++;
            if (best == null || cand.fitness > best.fitness) {
                best = cand;
                assignments++;
            }
        }
        return best;
    }

    // ---- cruce valido: Order Crossover (OX) ----
    private Individual[] orderCrossoverOX(Individual p1, Individual p2) {
        int[] a = p1.perm;
        int[] b = p2.perm;

        int cut1 = rand.nextInt(n);
        int cut2 = rand.nextInt(n);
        comparisons++;
        if (cut1 > cut2) {
            int t = cut1;
            cut1 = cut2;
            cut2 = t;
            assignments += 3;
        }

        int[] child1 = new int[n];
        int[] child2 = new int[n];
        Arrays.fill(child1, -1);
        Arrays.fill(child2, -1);

        for (int i = cut1; i <= cut2; i++) {
            child1[i] = a[i];
            child2[i] = b[i];
            assignments += 2;
        }

        fillOX(child1, b, cut2);
        fillOX(child2, a, cut2);

        Individual k1 = new Individual();
        k1.perm = child1;
        k1.hash = hashPerm(child1);
        k1.fitness = -1;

        Individual k2 = new Individual();
        k2.perm = child2;
        k2.hash = hashPerm(child2);
        k2.fitness = -1;

        assignments += 6;
        return new Individual[]{k1, k2};
    }

    private void fillOX(int[] child, int[] donor, int startFrom) {
        boolean[] used = new boolean[n];
        for (int v : child) {
            comparisons++;
            if (v != -1) used[v] = true;
        }

        int pos = (startFrom + 1) % n;
        for (int i = 0; i < n; i++) {
            int gene = donor[(startFrom + 1 + i) % n];
            comparisons++;
            if (!used[gene]) {
                while (child[pos] != -1) {
                    pos = (pos + 1) % n;
                    comparisons++;
                }
                child[pos] = gene;
                used[gene] = true;
                assignments += 2;
            }
        }
    }

    // ---- mejora local ligera (swap si mejora o empata) ----
    private Individual localImprove(Individual ind) {
        // mas fuerte para 3x3 y 5x5, muy suave para grandes
        final int tries =
                (size <= 3) ? 220 :
                (size <= 5) ? 140 :
                (size <= 10) ? 35 : 4;

        int[] perm = ind.perm;
        int bestFit = (ind.fitness >= 0) ? ind.fitness : fitnessOf(perm);

        for (int t = 0; t < tries && !timedOut(); t++) {
            int i = rand.nextInt(n);
            int j = rand.nextInt(n);
            if (i == j) continue;

            int tmp = perm[i];
            perm[i] = perm[j];
            perm[j] = tmp;

            int fit = fitnessOf(perm);

            if (fit >= bestFit) { // aceptar mejora o empate (ayuda a salir de mesetas)
                bestFit = fit;
            } else {
                // revertir
                tmp = perm[i];
                perm[i] = perm[j];
                perm[j] = tmp;
            }
        }

        ind.fitness = bestFit;
        ind.hash = hashPerm(ind.perm);
        return ind;
    }

    // ---- mutacion: swap (si es duplicado SIEMPRE muta; si no, con probabilidad) ----
    private Individual resolveDuplicateWithMutationIfNeeded(Individual child, Set<Long> seen) {
        comparisons++;

        boolean duplicate = seen.contains(child.hash);

        final double mutationRate = (size <= 5) ? 0.35 : (size <= 10 ? 0.20 : 0.08);
        final int mutationTries   = (size <= 5) ? 18 : 8;

        boolean doMutate = duplicate || rand.nextDouble() < mutationRate;

        if (!doMutate) {
            seen.add(child.hash);
            assignments++;
            return child;
        }

        int[] original = child.perm;
        int fitO = (child.fitness >= 0) ? child.fitness : fitnessOf(original);

        int[] bestPerm = original;
        int bestFit = fitO;

        int[] firstMut = null;
        int firstFit = fitO;

        for (int t = 0; t < mutationTries && !timedOut(); t++) {
            int[] mutated = Arrays.copyOf(original, n);
            int i = rand.nextInt(n);
            int j = rand.nextInt(n);
            int tmp = mutated[i];
            mutated[i] = mutated[j];
            mutated[j] = tmp;

            int fitM = fitnessOf(mutated);

            if (t == 0) { firstMut = mutated; firstFit = fitM; }
            if (fitM > bestFit) {
                bestFit = fitM;
                bestPerm = mutated;
            }
        }

        // si era duplicado y no mejoro, igual cambia para escapar del duplicado
        if (bestPerm == original && duplicate && firstMut != null) {
            bestPerm = firstMut;
            bestFit = firstFit;
        }

        if (bestPerm != original) {
            printMutation(original, fitO, bestPerm, bestFit);

            Individual improved = new Individual();
            improved.perm = bestPerm;
            improved.fitness = bestFit;
            improved.hash = hashPerm(bestPerm);
            assignments += 4;

            // evitar duplicados (pocos intentos)
            int tries = 0;
            while (seen.contains(improved.hash) && tries < 6 && !timedOut()) {
                tries++;
                int a = rand.nextInt(n);
                int b = rand.nextInt(n);
                int tt = improved.perm[a];
                improved.perm[a] = improved.perm[b];
                improved.perm[b] = tt;
                improved.fitness = fitnessOf(improved.perm);
                improved.hash = hashPerm(improved.perm);
            }

            seen.add(improved.hash);
            return improved;
        }

        // no cambio (caso raro): igual lo marcamos como visto para no ciclar
        seen.add(child.hash);
        assignments++;
        return child;
    }

    private void sortByFitnessDesc(List<Individual> pop) {
        pop.sort((x, y) -> Integer.compare(y.fitness, x.fitness));
    }

    // ---- printing requerido ----
    private void printCross(Individual p1, Individual p2, Individual h1, Individual h2) {
        System.out.println("[CRUCE]");
        System.out.println("Padre 1 " + chromosomeToString(p1.perm) + " puntuacion " + p1.fitness);
        System.out.println("Padre 2 " + chromosomeToString(p2.perm) + " puntuacion " + p2.fitness);
        System.out.println("Hijo  1 " + chromosomeToString(h1.perm) + " puntuacion " + h1.fitness);
        System.out.println("Hijo  2 " + chromosomeToString(h2.perm) + " puntuacion " + h2.fitness);
    }

    private void printMutation(int[] original, int fitO, int[] mutated, int fitM) {
        if (!compact) {
            System.out.println("[MUTACION]");
            System.out.println("Individuo 1 " + chromosomeToString(original) + " puntuacion " + fitO);
            System.out.println("Mutacion   " + chromosomeToString(mutated) + " puntuacion " + fitM);
        } else {
            System.out.println("[MUTACION] (modo compacto: cromosoma truncado)");
            System.out.println("Individuo 1 " + chromosomeToString(original) + " puntuacion " + fitO);
            System.out.println("Mutacion   " + chromosomeToString(mutated) + " puntuacion " + fitM);
        }
    }

    private String chromosomeToString(int[] perm) {
        int limit = compact ? Math.min(25, perm.length) : perm.length;
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < limit; i++) {
            sb.append(perm[i]);
            if (i < limit - 1) sb.append(",");
        }
        if (limit < perm.length) sb.append(",...");
        sb.append("}");
        return sb.toString();
    }

    private static int[] randomPermutation(int n, Random r) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
        return a;
    }

    private static long hashPerm(int[] perm) {
        long h = 1125899906842597L;
        for (int v : perm) h = 31L * h + v;
        return h;
    }
}

    // ============================================================
    // ===================== TABLAS GENETICO =======================
    // ============================================================
    static int populationSizeFor(int size) {
        int cells = size * size;

        if (cells <= 9)   return 120; // 3x3
        if (cells <= 25)  return 160; // 5x5
        if (cells <= 100) return 80;  // 10x10
        if (cells <= 225) return 60;  // 15x15
        if (cells <= 900) return 40;  // 30x30
        return 30; // 60x60 y 100x100
    }

    static int childrenCountFor(int size) {
        return populationSizeFor(size) * 2;
    }

    // ============================================================
    // ============================= MAIN ==========================
    // ============================================================
    public static void main(String[] args) {
        System.out.println("=== PUZZLE SOLVER (TODO EN UN SOLO MAIN) ===\n");

        runAllForRange("0..9", 9);
        System.out.println("\n============================================================\n");
        runAllForRange("0..15", 15);
    }

    static void runAllForRange(String label, int maxValue) {
        System.out.println("=== VARIANTE NUMERICA: " + label + " ===\n");

        for (int size : SIZES) {
            System.out.println("------------------------------------------------------------");
            System.out.println("PUZZLE " + size + "x" + size + " (maxValue=" + maxValue + ")");
            System.out.println("Fitness maximo teorico = " + FitnessEvaluator.getMaxFitness(size));

            // Generacion (NO se cuenta para metricas)
            List<Piece> pieces;
            if (size == 3 && maxValue == 9) {
                pieces = PuzzleGenerator.getFixed3x3_0_9();
            } else if (size == 3 && maxValue == 15) {
                pieces = PuzzleGenerator.getFixed3x3_0_15();
            } else {
                pieces = PuzzleGenerator.generateWithSolution(size, maxValue);
            }

            if (size <= PRINT_PIECES_MAX_SIZE) {
                System.out.println("\nPiezas generadas (" + pieces.size() + "):");
                for (Piece p : pieces) System.out.println(p);
            } else {
                System.out.println("\nPiezas generadas: " + pieces.size() + " (impresion omitida por tamano)");
            }

            // Ejecutar los 3 algoritmos
            Metrics brute = runBruteForce(size, pieces);
            Metrics fast  = runFast(size, pieces);
            Metrics gen   = runGenetic(size, pieces);

            // Resumen
            System.out.println("\n>>> RESUMEN METRICAS (" + size + "x" + size + ", " + label + ")");
            printMetrics("Fuerza bruta", brute);
            printMetrics("Avance rapido", fast);
            printMetrics("Genetico", gen);
            System.out.println();
        }
    }

    static Metrics runBruteForce(int size, List<Piece> pieces) {
        System.out.println("\n--- ALGORITMO 1: FUERZA BRUTA (Backtracking) ---");
        Metrics m = new Metrics();

        if (size * size > MAX_CELLS_RECURSIVE) {
            m.solved = false;
            m.bestFitness = 0;
            m.note = "omitido (riesgo StackOverflow)";
            System.out.println("Resultado: PARCIAL/NO (" + m.note + ")");
            System.out.println("(Se omite por profundidad recursiva: " + (size * size) + " llamadas aprox.)");
            return m;
        }

        Board b = new Board(size);

        long memBefore = usedMemoryBytes();
        long start = System.nanoTime();

        try {
            BruteForceSolver solver = new BruteForceSolver(b, pieces, MAX_MS_BRUTEFORCE);
            boolean solved = solver.solve();

            long end = System.nanoTime();
            long memAfter = usedMemoryBytes();

            m.solved = solved;
            m.timeMs = (end - start) / 1_000_000L;
            m.memoryBytes = Math.max(0, memAfter - memBefore);
            m.comparisons = solver.getComparaciones();
            m.assignments = solver.getAsignaciones();
            m.bestFitness = b.countMatches();
            m.note = solver.isTimedOut() ? "timeout" : "ok";

            System.out.println("Resultado: " + (solved ? "SOLUCION" : "PARCIAL/NO") + " (" + m.note + ")");
            if (size <= 10) b.print();
            else System.out.println("(Tablero omitido por tamano)");

        } catch (StackOverflowError e) {
            m.solved = false;
            m.bestFitness = b.countMatches();
            m.note = "StackOverflow";
            System.out.println("Resultado: PARCIAL/NO (" + m.note + ")");
            System.out.println("(El tamano excede la profundidad recursiva segura. Se continua con los demas.)");
        }

        return m;
    }

    static Metrics runFast(int size, List<Piece> pieces) {
        System.out.println("\n--- ALGORITMO 2: AVANCE RAPIDO (Backtracking con candidatos) ---");
        Metrics m = new Metrics();

        if (size * size > MAX_CELLS_RECURSIVE) {
            m.solved = false;
            m.bestFitness = 0;
            m.note = "omitido (riesgo StackOverflow)";
            System.out.println("Resultado: PARCIAL/NO (" + m.note + ")");
            System.out.println("(Se omite por profundidad recursiva: " + (size * size) + " llamadas aprox.)");
            return m;
        }

        Board b = new Board(size);

        long memBefore = usedMemoryBytes();
        long start = System.nanoTime();

        try {
            AvanceRapido solver = new AvanceRapido(b, pieces, MAX_MS_FAST);
            boolean solved = solver.solve();

            long end = System.nanoTime();
            long memAfter = usedMemoryBytes();

            m.solved = solved;
            m.timeMs = (end - start) / 1_000_000L;
            m.memoryBytes = Math.max(0, memAfter - memBefore);
            m.comparisons = solver.getComparaciones();
            m.assignments = solver.getAsignaciones();
            m.bestFitness = b.countMatches();
            m.note = solver.isTimedOut() ? "timeout" : "ok";

            System.out.println("Resultado: " + (solved ? "SOLUCION" : "PARCIAL/NO") + " (" + m.note + ")");
            if (size <= 10) b.print();
            else System.out.println("(Tablero omitido por tamano)");

        } catch (StackOverflowError e) {
            m.solved = false;
            m.bestFitness = b.countMatches();
            m.note = "StackOverflow";
            System.out.println("Resultado: PARCIAL/NO (" + m.note + ")");
            System.out.println("(El tamano excede la profundidad recursiva segura. Se continua con los demas.)");
        }

        return m;
    }

    static Metrics runGenetic(int size, List<Piece> pieces) {
        System.out.println("\n--- ALGORITMO 3: GENETICO (Persona 3) ---");

        int pop = populationSizeFor(size);
        int kids = childrenCountFor(size);
        System.out.println("[GENETIC] Poblacion inicial = " + pop + " | Hijos = " + kids + " | Generaciones = 10");

        GeneticSolver solver = new GeneticSolver(size, pieces, pop, kids, MAX_MS_GENETIC);
        return solver.solveAndReport();
    }

    static void printMetrics(String name, Metrics m) {
        System.out.println(name + ":");
        System.out.println("  - Solucion completa: " + (m.solved ? "Si" : "No"));
        System.out.println("  - Fitness logrado: " + m.bestFitness);
        System.out.println("  - Tiempo (ms): " + m.timeMs);
        System.out.println("  - Memoria (bytes aprox): " + m.memoryBytes);
        System.out.println("  - Comparaciones: " + m.comparisons);
        System.out.println("  - Asignaciones: " + m.assignments);
        System.out.println("  - Nota: " + (m.note == null ? "" : m.note));
    }
}
