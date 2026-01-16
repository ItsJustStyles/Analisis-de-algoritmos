import java.util.*;

public class BruteForceSolver {
    private Board board;
    private List<Piece> pieces;
    private Set<Integer> piezasUsadas;

    public BruteForceSolver(Board board, List<Piece> pieces){
        this.board = board;
        this.pieces = pieces;
        this.piezasUsadas = new HashSet<>();
    }


    public boolean solve(){
        return solucionarRecursivo(0, 0);
    }

    private boolean solucionarRecursivo(int fila, int columna){
        //Caso base, que la fila sea igual al tamaño del tablero:
        if (fila == board.size){
            return true;
        }

        //Moverse:
        int siguienteFila;
        int siguienteColumna;

        if(columna == board.size - 1){
            siguienteFila = fila + 1;
            siguienteColumna = 0;
        }else{
            siguienteFila = fila;
            siguienteColumna = columna + 1;
        }

        //Resolver:
        for(Piece p : pieces){
            if(!piezasUsadas.contains(p.id)){
                if(board.canPlace(p, fila, columna)){
                    board.place(p, fila, columna);
                    piezasUsadas.add(p.id);

                    if(solucionarRecursivo(siguienteFila, siguienteColumna)){
                        return true;
                    }

                    board.remove(fila, columna);
                    piezasUsadas.remove(p.id);

                }
            }
        }

        return false;
    }
}
