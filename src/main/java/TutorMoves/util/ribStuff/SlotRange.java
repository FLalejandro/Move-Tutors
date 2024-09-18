package TutorMoves.util.ribStuff;

public class SlotRange {
    private int start;
    private int end;

    public SlotRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean contains(int slot) {
        return start <= slot && slot <= end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int size() {
        return end - start + 1;
    }
}
