package me.zeroeightsix.kami.util;

import com.google.gson.Gson;
import me.zeroeightsix.kami.KamiMod;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Dewy on 09/04/2020
 * Updated by d1gress/Qther on 13 April 2020
 */
public class WebUtils {

    public static void openWebLink(URI url) {
        try {
            Desktop.getDesktop().browse(url);
        } catch (IOException e) {
            KamiMod.log.error("Couldn't open link: " + url.toString());
        }
    }

    public static java.util.List<GithubUser> getContributors() {
        // log attempt
        KamiMod.log.info("Attempting to get contributors from github api...");

        //initialize list
        java.util.List<GithubUser> contributorsAsList = new LinkedList<>(Collections.emptyList());

        new Thread(() -> {
            try {
                // connect to https://api.github.com/repos/kami-blue/client/contributors
                HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.github.com/repos/kami-blue/client/contributors").openConnection();
                connection.connect();

                // then parse it
                GithubUser[] contributors = new Gson().fromJson(new InputStreamReader(connection.getInputStream()), GithubUser[].class);

                // disconnect from api
                connection.disconnect();

                // add contributors to the list
                contributorsAsList.addAll(Arrays.asList(contributors));

            } catch (Throwable t) {
                // throw error
                KamiMod.log.error("Attempt to get contributors from github api failed.\nError:\n\n" + t.toString());
            }
        }).start();


        return contributorsAsList;
    }

    public static java.util.List<GithubUser> getContributors(java.util.List<Integer> exceptions) {
            // log attempt
            KamiMod.log.info("Attempting to get contributors from github api...");

            //initialize list
            java.util.List<GithubUser> contributorsAsList = new LinkedList<>(Collections.emptyList());

            new Thread(() -> {
                try {
                    // connect to https://api.github.com/repos/kami-blue/client/contributors
                    HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.github.com/repos/kami-blue/client/contributors").openConnection();
                    connection.connect();

                    // then parse it
                    GithubUser[] contributors = new Gson().fromJson(new InputStreamReader(connection.getInputStream()), GithubUser[].class);

                    // disconnect from api
                    connection.disconnect();

                    // add contributors to the list
                    for (GithubUser githubUser : contributors) {
                        contributorsAsList.add(githubUser);
                        for (int exception : exceptions) {
                            if (githubUser.id == exception) {
                                contributorsAsList.remove(githubUser);
                            }
                        }
                    }

                } catch (Throwable t) {
                    // throw error
                    KamiMod.log.error("Attempt to get contributors from github api failed.\nError:\n\n" + t.toString());
                    MessageSendHelper.sendErrorMessage("Attempt to get contributors from github api failed.\nError:\n\n" + t.toString());
                }
            }).start();

        return contributorsAsList;
    }

    public class GithubUser {
        public String login;
        public int id;
        public String contributions;
    }
}
