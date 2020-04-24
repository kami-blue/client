package net.shadowfacts.forgelin.preloader;

import net.minecraftforge.fml.relauncher.IFMLCallHook;

import java.util.Map;

/**
 * @author shadowfacts
 */
public class ForgelinSetup implements IFMLCallHook {

    @Override
    public void injectData(Map<String, Object> data) {
        ClassLoader loader = (ClassLoader)data.get("classLoader");
        try {
            loader.loadClass("net.shadowfacts.forgelin.KotlinAdapter");
        } catch (ClassNotFoundException e) {
            // this should never happen
            throw new RuntimeException("Couldn't find Forgelin langague adapter, this shouldn't be happening", e);
        }
    }

    @Override
    public Void call() throws Exception {
        return null;
    }

}