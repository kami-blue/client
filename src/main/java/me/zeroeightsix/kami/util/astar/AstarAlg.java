package me.zeroeightsix.kami.util.astar;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.*;

/**
 * Created by fred41 on 18/07/2020.
 * <p>
 * https://en.wikipedia.org/wiki/A*_search_algorithm
 * Variable names are taken from the page as well
 */
public class AstarAlg {

    private static PriorityQueue<AstarNode> openlist;
    private static HashMap<BlockPos, AstarNode> closedlist;
    private static Vec3i targetVec;
    private static int addlayer;
    private static double lowestf;
    private static AstarNode lowestfNode;

    private static AstarGrid grid;

    public static List<AstarNode> findPath(BlockPos start, BlockPos target, int adiitionallayer) {
        grid = new AstarGrid();
        AstarAlg.addlayer = adiitionallayer;
        lowestf = Double.POSITIVE_INFINITY;

        closedlist = new HashMap<>();
        targetVec = new Vec3i(target.getX(), target.getY(), target.getZ());
        openlist = new PriorityQueue<>(Comparator.comparingDouble(o -> o.f));
        AstarNode startnode = new AstarNode(start, adiitionallayer);
        startnode.f = Math.pow(startnode.pos.getX() - targetVec.getX(), 2) + Math.pow(startnode.pos.getZ() - targetVec.getZ(), 2);
        lowestfNode = startnode;
        grid.nodes.put(start, startnode);
        openlist.add(startnode);
        int i = 0;
        do {
            i++;
            AstarNode currentnode = openlist.remove();
            if (currentnode.pos.equals(target)) {
                Astarpathfinder.nopathfound = 0;
                return buildPath(currentnode);
            } else {
                closedlist.put(currentnode.pos, currentnode);
                expandNode(currentnode);
            }
        } while (!openlist.isEmpty() && i < 7000);
        if (i >= 7000) {
            System.out.println("no short path");
        }
        if (Math.sqrt(lowestfNode.pos.distanceSq(target)) > 10) {
            Astarpathfinder.nopathfound++;
        }
        return buildPath(lowestfNode);

    }

    private static List<AstarNode> buildPath(AstarNode endnode) {
        ArrayList<AstarNode> path = new ArrayList<>();
        path.add(endnode);
        AstarNode pathnode = endnode;
        while (pathnode.pred != null) {
            pathnode = pathnode.pred;
            path.add(pathnode);
        }
        Collections.reverse(path);
        return path;
    }

    private static void expandNode(AstarNode currentnode) {
        AstarNode successor = null;

        for (int i = 0; i < 8; i++) {
            switch (i) {
                case 0:
                    if ((successor = grid.nodes.get(currentnode.pos.add(1, 0, 0))) == null) {
                        successor = new AstarNode(currentnode.pos.add(1, 0, 0), addlayer);
                    }
                    break;
                case 1:
                    if ((successor = grid.nodes.get(currentnode.pos.add(0, 0, 1))) == null) {
                        successor = new AstarNode(currentnode.pos.add(0, 0, 1), addlayer);
                    }
                    break;
                case 2:
                    if ((successor = grid.nodes.get(currentnode.pos.add(-1, 0, 0))) == null) {
                        successor = new AstarNode(currentnode.pos.add(-1, 0, 0), addlayer);

                    }
                    break;
                case 3:
                    if ((successor = grid.nodes.get(currentnode.pos.add(0, 0, -1))) == null) {
                        successor = new AstarNode(currentnode.pos.add(0, 0, -1), addlayer);
                    }
                    break;
                case 4:
                    if ((successor = grid.nodes.get(currentnode.pos.add(1, 0, -1))) == null) {
                        successor = new AstarNode(currentnode.pos.add(1, 0, -1), addlayer);
                    }
                    if (grid.nodes.get(currentnode.pos.add(1, 0, 0)).blocked || grid.nodes.get(currentnode.pos.add(0, 0, -1)).blocked)
                        continue;
                    break;
                case 5:
                    if ((successor = grid.nodes.get(currentnode.pos.add(-1, 0, -1))) == null) {
                        successor = new AstarNode(currentnode.pos.add(-1, 0, -1), addlayer);
                    }
                    if (grid.nodes.get(currentnode.pos.add(-1, 0, 0)).blocked || grid.nodes.get(currentnode.pos.add(0, 0, -1)).blocked)
                        continue;
                    break;
                case 6:
                    if ((successor = grid.nodes.get(currentnode.pos.add(-1, 0, 1))) == null) {
                        successor = new AstarNode(currentnode.pos.add(-1, 0, 1), addlayer);
                    }
                    if (grid.nodes.get(currentnode.pos.add(-1, 0, 0)).blocked || grid.nodes.get(currentnode.pos.add(0, 0, 1)).blocked)
                        continue;
                    break;
                case 7:
                    if ((successor = grid.nodes.get(currentnode.pos.add(1, 0, 1))) == null) {
                        successor = new AstarNode(currentnode.pos.add(1, 0, 1), addlayer);
                    }
                    if (grid.nodes.get(currentnode.pos.add(1, 0, 0)).blocked || grid.nodes.get(currentnode.pos.add(0, 0, 1)).blocked)
                        continue;
                    break;

            }

            if (!closedlist.containsKey(successor.pos)) {
                grid.nodes.put(successor.pos, successor);
                if (!successor.blocked) {

                    double tentative_g = currentnode.g + (i < 4 ? 1 : 1.41421);
                    if (!(openlist.contains(successor) && tentative_g >= successor.g)) {
                        successor.pred = currentnode;
                        successor.g = tentative_g;
                        double f = tentative_g + (targetVec.distanceSq(successor.pos));
                        if (f < lowestf) {
                            lowestf = f;
                            lowestfNode = successor;
                        }

                        successor.f = f;
                        openlist.remove(successor);
                        openlist.add(successor);
                    }
                }
            }
        }
    }
}
