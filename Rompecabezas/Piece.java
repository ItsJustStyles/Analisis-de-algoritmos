public class Piece {
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
