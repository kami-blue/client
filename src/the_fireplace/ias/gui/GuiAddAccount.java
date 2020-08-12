// 
// Decompiled by Procyon v0.5.36
// 

package the_fireplace.ias.gui;

import the_fireplace.ias.account.ExtendedAccountData;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;

public class GuiAddAccount extends AbstractAccountGui
{
    public GuiAddAccount() {
        super("ias.addaccount");
    }
    
    @Override
    public void complete() {
        AltDatabase.getInstance().getAlts().add(new ExtendedAccountData(this.getUsername(), this.getPassword(), this.getUsername()));
    }
}
