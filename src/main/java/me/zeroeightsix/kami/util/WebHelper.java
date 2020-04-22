package me.zeroeightsix.kami.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author balusc (StackOverflow ID 157882)
 *
 * https://stackoverflow.com/questions/3584210/preferred-java-way-to-ping-an-http-url-for-availability#3584332
 */
public class WebHelper {
    public static boolean isDown(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return false;
        } catch (IOException e) {
            return true; // Either timeout or unreachable or failed DNS lookup.
        }
    }
}
