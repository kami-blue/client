package me.zeroeightsix.kami.gui.kami.component;

import me.zeroeightsix.kami.gui.rgui.component.use.Button;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.poof.use.Poof;

/**
 * Created by 086 on 8/08/2017.
 */
public class EnumButton extends Button {

    String[] modes;
    int index;

    public EnumButton(String name, String description, String[] modes) {
        super(name, description);
        this.modes = modes;
        this.index = 0;

        addPoof(new ButtonPoof<EnumButton, ButtonPoof.ButtonInfo>() {
            @Override
            public void execute(EnumButton component, ButtonInfo info) {
                if (info.getButton() == 0) {
                    double p = (double) info.getX() / (double) component.getWidth();
                    if (p <= 0.5) { // left
                        EnumButton.this.increaseIndex(-1);
                    } else { // right
                        EnumButton.this.increaseIndex(1);
                    }
                }
            }
        });
    }

    public void setModes(String[] modes) {
        this.modes = modes;
    }

    protected void increaseIndex(int amount) {
        int old = index;
        int newI = index + amount;
        if (newI < 0) {
            newI = modes.length - Math.abs(newI);
        } else if (newI >= modes.length) {
            newI = Math.abs(newI - modes.length);
        }
        index = Math.min(modes.length, Math.max(0, newI));

        callPoof(EnumbuttonIndexPoof.class, new EnumbuttonIndexPoof.EnumbuttonInfo(old, index));
    }

    public int getIndex() {
        return index;
    }

    public String[] getModes() {
        return modes;
    }

    public String getIndexMode() {
        return modes[index];
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public static abstract class EnumbuttonIndexPoof<T extends Button, S extends EnumbuttonIndexPoof.EnumbuttonInfo> extends Poof<T, S> {
        ButtonPoof.ButtonInfo info;

        public static class EnumbuttonInfo extends PoofInfo {
            int oldIndex;
            int newIndex;

            public EnumbuttonInfo(int oldIndex, int newIndex) {
                this.oldIndex = oldIndex;
                this.newIndex = newIndex;
            }

            public int getNewIndex() {
                return newIndex;
            }

            public void setNewIndex(int newIndex) {
                this.newIndex = newIndex;
            }

            public int getOldIndex() {
                return oldIndex;
            }
        }
    }

}
