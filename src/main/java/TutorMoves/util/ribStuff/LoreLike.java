package TutorMoves.util.ribStuff;

import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;

public class LoreLike {
    private final List<Text> lore;

    public LoreLike() {
        this.lore = Collections.emptyList();
    }

    public LoreLike(List<Text> lore) {
        this.lore = lore;
    }

    public List<Text> getLore() {
        return lore;
    }

}

