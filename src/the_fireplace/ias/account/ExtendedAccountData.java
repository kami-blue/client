// 
// Decompiled by Procyon v0.5.36
// 

package the_fireplace.ias.account;

import java.util.Arrays;
import the_fireplace.ias.tools.JavaTools;
import the_fireplace.ias.enums.EnumBool;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AccountData;

public class ExtendedAccountData extends AccountData
{
    private static final long serialVersionUID = -909128662161235160L;
    public EnumBool premium;
    public int[] lastused;
    public int useCount;
    
    public ExtendedAccountData(final String user, final String pass, final String alias) {
        super(user, pass, alias);
        this.useCount = 0;
        this.lastused = JavaTools.getJavaCompat().getDate();
        this.premium = EnumBool.UNKNOWN;
    }
    
    public ExtendedAccountData(final String user, final String pass, final String alias, final int useCount, final int[] lastused, final EnumBool premium) {
        super(user, pass, alias);
        this.useCount = useCount;
        this.lastused = lastused;
        this.premium = premium;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ExtendedAccountData other = (ExtendedAccountData)obj;
        return Arrays.equals(this.lastused, other.lastused) && this.premium == other.premium && this.useCount == other.useCount && this.user.equals(other.user) && this.pass.equals(other.pass);
    }
}
