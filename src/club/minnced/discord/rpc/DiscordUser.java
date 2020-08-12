// 
// Decompiled by Procyon v0.5.36
// 

package club.minnced.discord.rpc;

import java.util.Collections;
import java.util.Arrays;
import java.util.Objects;
import java.util.List;
import com.sun.jna.Structure;

public class DiscordUser extends Structure
{
    private static final List<String> FIELD_ORDER;
    public String userId;
    public String username;
    public String discriminator;
    public String avatar;
    
    public DiscordUser(final String encoding) {
        this.setStringEncoding(encoding);
    }
    
    public DiscordUser() {
        this("UTF-8");
    }
    
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DiscordUser)) {
            return false;
        }
        final DiscordUser that = (DiscordUser)o;
        return Objects.equals(this.userId, that.userId) && Objects.equals(this.username, that.username) && Objects.equals(this.discriminator, that.discriminator) && Objects.equals(this.avatar, that.avatar);
    }
    
    public int hashCode() {
        return Objects.hash(this.userId, this.username, this.discriminator, this.avatar);
    }
    
    protected List<String> getFieldOrder() {
        return DiscordUser.FIELD_ORDER;
    }
    
    static {
        FIELD_ORDER = Collections.unmodifiableList((List<? extends String>)Arrays.asList("userId", "username", "discriminator", "avatar"));
    }
}
