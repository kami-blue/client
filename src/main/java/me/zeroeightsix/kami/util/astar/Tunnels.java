package me.zeroeightsix.kami.util.astar;

import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fred41 on 18/07/2020.
 */
public class Tunnels {
    public static final List<Vec3i> TUNNEL_X = new ArrayList<Vec3i>() {{
        add(new Vec3i(2, 0, 0));
        add(new Vec3i(1, 0, 0));
        add(new Vec3i(0, 0, 0));
        add(new Vec3i(-1, 0, 0));
        add(new Vec3i(-2, 0, 0));
        add(new Vec3i(2, 1, 0));
        add(new Vec3i(1, 1, 0));
        add(new Vec3i(0, 1, 0));
        add(new Vec3i(-1, 1, 0));
        add(new Vec3i(-2, 1, 0));
    }};
    public static final List<Vec3i> TUNNEL_Z = new ArrayList<Vec3i>() {{
        add(new Vec3i(0, 0, 2));
        add(new Vec3i(0, 0, 1));
        add(new Vec3i(0, 0, 0));
        add(new Vec3i(0, 0, -1));
        add(new Vec3i(0, 0, -2));
        add(new Vec3i(0, 1, 2));
        add(new Vec3i(0, 1, 1));
        add(new Vec3i(0, 1, 0));
        add(new Vec3i(0, 1, -1));
        add(new Vec3i(0, 1, -2));
    }};
    public static final List<Vec3i> TUNNEL_POS_POS = new ArrayList<Vec3i>() {{
        add(new Vec3i(0, 0, 0));
        add(new Vec3i(1, 0, 0));
        add(new Vec3i(1, 0, 1));
        add(new Vec3i(2, 0, 1));
        add(new Vec3i(2, 0, 2));
        add(new Vec3i(3, 0, 2));
        add(new Vec3i(0, 0, -1));
        add(new Vec3i(-1, 0, -1));
        add(new Vec3i(-1, 0, -2));
        add(new Vec3i(-2, 0, -2));
        add(new Vec3i(-2, 0, -3));

        add(new Vec3i(0, 1, 0));
        add(new Vec3i(1, 1, 0));
        add(new Vec3i(1, 1, 1));
        add(new Vec3i(2, 1, 1));
        add(new Vec3i(2, 1, 2));
        add(new Vec3i(3, 1, 2));
        add(new Vec3i(0, 1, -1));
        add(new Vec3i(-1, 1, -1));
        add(new Vec3i(-1, 1, -2));
        add(new Vec3i(-2, 1, -2));
        add(new Vec3i(-2, 1, -3));

    }};
    public static final List<Vec3i> TUNNEL_POS_NEG = new ArrayList<Vec3i>() {{
        add(new Vec3i(0, 0, 0));
        add(new Vec3i(1, 0, 0));
        add(new Vec3i(1, 0, -1));
        add(new Vec3i(2, 0, -1));
        add(new Vec3i(2, 0, -2));
        add(new Vec3i(3, 0, -2));
        add(new Vec3i(0, 0, 1));
        add(new Vec3i(-1, 0, 1));
        add(new Vec3i(-1, 0, 2));
        add(new Vec3i(-2, 0, 2));
        add(new Vec3i(-2, 0, 3));

        add(new Vec3i(0, 1, 0));
        add(new Vec3i(1, 1, 0));
        add(new Vec3i(1, 1, -1));
        add(new Vec3i(2, 1, -1));
        add(new Vec3i(2, 1, -2));
        add(new Vec3i(3, 1, -2));
        add(new Vec3i(0, 1, 1));
        add(new Vec3i(-1, 1, 1));
        add(new Vec3i(-1, 1, 2));
        add(new Vec3i(-2, 1, 2));
        add(new Vec3i(-2, 1, 3));
    }};
}
