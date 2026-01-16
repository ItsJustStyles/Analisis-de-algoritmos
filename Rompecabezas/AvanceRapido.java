import java.util.*;

public class AvanceRapido {
    private Board board;
    private List<Piece> pieces;
    Set<Integer> piezasUsadas;

    public AvanceRapido(Board board, List<Piece> pieces){
        this.board = board;
        this.pieces = pieces;
        this.piezasUsadas = new HashSet<>();
    }

    public boolean solve(){
        return solucionarAvanceRapido(0,0);
    }

    private boolean solucionarAvanceRapido(int fila, int columna){
        if(fila == board.size){
            return true;
        }

        for(Piece p : pieces){
            System.out.println(p);
        }
        return false;
    }
}
