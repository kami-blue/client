// 
// Decompiled by Procyon v0.5.36
// 

package the_fireplace.ias.gui;

import the_fireplace.iasencrypt.EncryptionTools;
import the_fireplace.ias.enums.EnumBool;
import the_fireplace.ias.tools.JavaTools;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AccountData;
import the_fireplace.ias.account.ExtendedAccountData;

class GuiEditAccount extends AbstractAccountGui
{
    private final ExtendedAccountData data;
    private final int selectedIndex;
    
    public GuiEditAccount(final int index) {
        super("ias.editaccount");
        this.selectedIndex = index;
        final AccountData data = AltDatabase.getInstance().getAlts().get(index);
        if (data instanceof ExtendedAccountData) {
            this.data = (ExtendedAccountData)data;
        }
        else {
            this.data = new ExtendedAccountData(data.user, data.pass, data.alias, 0, JavaTools.getJavaCompat().getDate(), EnumBool.UNKNOWN);
        }
    }
    
    @Override
    public void func_73866_w_() {
        super.func_73866_w_();
        this.setUsername(EncryptionTools.decode(this.data.user));
        this.setPassword(EncryptionTools.decode(this.data.pass));
    }
    
    @Override
    public void complete() {
        AltDatabase.getInstance().getAlts().set(this.selectedIndex, new ExtendedAccountData(this.getUsername(), this.getPassword(), this.hasUserChanged ? this.getUsername() : this.data.alias, this.data.useCount, this.data.lastused, this.data.premium));
    }
}
