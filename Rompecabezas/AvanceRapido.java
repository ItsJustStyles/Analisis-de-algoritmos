import java.util.*;

public class AvanceRapido {
    // Contadores para análisis de complejidad
    private long comparaciones = 0;
    private long asignaciones = 0;

    private Board board;
    private List<Piece> pieces;
    private Set<Integer> piezasUsadas;
    private Map<Integer, List<Piece>> piezasPorIzquierda = new HashMap<>();
    private Map<Integer, List<Piece>> piezasPorArriba = new HashMap<>();

    public AvanceRapido(Board board, List<Piece> pieces){
        this.board = board;
        this.pieces = pieces;
        this.piezasUsadas = new HashSet<>();

        for(Piece p : pieces){
            piezasPorArriba.putIfAbsent(p.top, new ArrayList<>());
            piezasPorArriba.get(p.top).add(p);

            piezasPorIzquierda.putIfAbsent(p.left, new ArrayList<>());
            piezasPorIzquierda.get(p.left).add(p);
        }


        this.asignaciones += 3; // Inicialización de variables de instancia
    }

    public boolean solve(){
        return solucionarAvanceRapido(0,0);
    }

    private boolean solucionarAvanceRapido(int fila, int columna){
        comparaciones++;
        if(fila == board.size){
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

        List<Piece> candidatos = pieces;
        if(columna > 0){
            int valorBuscado = board.grid[fila][columna - 1].right;
            candidatos = piezasPorIzquierda.getOrDefault(valorBuscado, new ArrayList<>());
        }else if(fila > 0){
            int valorBuscado = board.grid[fila - 1][columna].bottom;
            candidatos = piezasPorArriba.getOrDefault(valorBuscado, new ArrayList<>());
        }

        for(Piece p : candidatos){
            if(!piezasUsadas.contains(p.id) && board.canPlace(p, fila, columna)){
                board.place(p, fila, columna);
                piezasUsadas.add(p.id);
                if(solucionarAvanceRapido(siguienteFila, siguienteColumna)){
                    comparaciones++;
                    return true;
                }
                board.remove(fila, columna);
                piezasUsadas.remove(p.id);
                asignaciones += 2;
            }
        }
        return false;
    }

    // Getters para obtener los resultados desde el Main
    public long getComparaciones(){ 
        return comparaciones; 
    }
    public long getAsignaciones(){ 
        return asignaciones; 
    }
}