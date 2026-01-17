import java.util.*;

public class BruteForceSolver {
    // Contadores para el análisis comparativo
    private long comparaciones = 0;
    private long asignaciones = 0;

    private Board board;
    private List<Piece> pieces;
    private Set<Integer> piezasUsadas;

    public BruteForceSolver(Board board, List<Piece> pieces){
        this.board = board;
        this.pieces = pieces;
        this.piezasUsadas = new HashSet<>();
        this.asignaciones += 3; // Inicialización de variables de instancia
    }

    public boolean solve(){
        return solucionarRecursivo(0, 0);
    }

    private boolean solucionarRecursivo(int fila, int columna){
        // Caso base
        comparaciones++; // if (fila == board.size)
        if (fila == board.size){
            return true;
        }

        // Moverse:
        int siguienteFila;
        int siguienteColumna;

        comparaciones++; // if(columna == board.size - 1)
        if(columna == board.size - 1){
            siguienteFila = fila + 1;
            siguienteColumna = 0;
            asignaciones += 2;
        } else {
            siguienteFila = fila;
            siguienteColumna = columna + 1;
            asignaciones += 2;
        }

        // Resolver:
        for(Piece p : pieces){
            asignaciones++; // Asignación implícita de p en el for
            comparaciones++; // Verificación de iteración del for

            comparaciones++; // if(!piezasUsadas.contains)
            if(!piezasUsadas.contains(p.id)){
                
                comparaciones++; // if(board.canPlace)
                if(board.canPlace(p, fila, columna)){
                    // Acción: colocar
                    board.place(p, fila, columna);
                    piezasUsadas.add(p.id);
                    asignaciones += 2;

                    if(solucionarRecursivo(siguienteFila, siguienteColumna)){
                        comparaciones++; // Del if de la llamada recursiva
                        return true;
                    }
                    comparaciones++;

                    // Backtracking: quitar
                    board.remove(fila, columna);
                    piezasUsadas.remove(p.id);
                    asignaciones += 2;
                }
            }
        }

        return false;
    }

    // Métodos para obtener los resultados en el Main
    public long getComparaciones() { return comparaciones; }
    public long getAsignaciones() { return asignaciones; }
}