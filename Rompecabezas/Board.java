public class Board {
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

    public boolean isFull() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == null)
                    return false;
            }
        }
        return true;
    }

    public int countMatches() {
        int matches = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] != null) {
                    // Verificar derecha
                    if (j < size - 1 && grid[i][j + 1] != null) {
                        if (grid[i][j].right == grid[i][j + 1].left)
                            matches++;
                    }
                    // Verificar abajo
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
            // Línea superior de cada fila
            for (int j = 0; j < size; j++) {
                if (grid[i][j] != null) {
                    System.out.printf("  %2d  ", grid[i][j].top);
                } else {
                    System.out.print("  -  ");
                }
                System.out.print("  ");
            }
            System.out.println();

            // Línea media con left, id, right
            for (int j = 0; j < size; j++) {
                if (grid[i][j] != null) {
                    System.out.printf("%2d[%2d]%2d", grid[i][j].left, grid[i][j].id, grid[i][j].right);
                } else {
                    System.out.print(" [  ] ");
                }
                System.out.print(" ");
            }
            System.out.println();

            // Línea inferior
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