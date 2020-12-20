package me.zeroeightsix.kami.gui.rgui.component;

/**
 * Created by 086 on 4/08/2017.
 */
public class AlignedComponent extends AbstractComponent {
    Alignment alignment;

    public enum Alignment {
        LEFT(0), CENTER(1), RIGHT(2);

        int index;

        Alignment(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }
}
