package net.justrotem.lobby.skins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Collectors;

public class SkinFetcher {

    public static SkinData fetchSkin(String username) throws IOException, URISyntaxException {
        // Step 1: Get UUID from username
        String uuidResponse = readUrl("https://api.mojang.com/users/profiles/minecraft/" + username);
        if (uuidResponse == null || uuidResponse.isEmpty()) return null;

        String uuid = uuidResponse.split("\"id\" : \"")[1].split("\"")[0];
        String name = uuidResponse.split("\"name\" : \"")[1].split("\"")[0];

        // Step 2: Get skin info
        String profileResponse = readUrl("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
        String value = profileResponse.split("\"value\" : \"")[1].split("\"")[0];
        String signature = profileResponse.split("\"signature\" : \"")[1].split("\"")[0];

        // Step 3: Create SkinData record and adding to IdentityHashMap
        return new SkinData(name, value, signature);
    }

    private static String readUrl(String urlString) throws IOException, URISyntaxException {
        URI uri = new URI(urlString);
        URL url = uri.toURL();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.lines().collect(Collectors.joining());
        }

    }
}
