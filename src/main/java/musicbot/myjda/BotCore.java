package musicbot.myjda;

import musicbot.vocalcord.MyVocal;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

public class BotCore {
    private static JDABuilder builder;
    private static MyListener myListener;
    private static MyVocal myVocal;
    private static MusicManager mm;

    private static List<Guild> guildList = new ArrayList<>();

    public void start() {
        /* Create a instance of Bag */

        builder = JDABuilder.createDefault(BotStrings.discordToken);
        builder.setActivity(Activity.watching(BotStrings.watchingActivity));

        mm = new MusicManager();
        myListener = new MyListener(mm);
        myVocal = new MyVocal(mm);

        builder.addEventListeners(myListener);
        builder.addEventListeners(myVocal);

        try {
            builder.build();
        }
        catch (LoginException le) {
            le.printStackTrace();
        }
    }


    public static void disconnectVoiceChannelFlag() {
        mm.disconnectIfQueueIsEmpty();
    }

    public static void addGuildToList(Guild g) {
        if (!guildList.contains(g)) {
            guildList.add(g);
        }
    }

    public static void removeGuildToList(Guild g) {
        guildList.remove(g);
    }

    public static List<Guild> getGuildList (){
        List<Guild> g = new ArrayList<>(guildList);
        return g;
    }


    public static void sendMessage(MessageChannel channel, String key) {
        channel.sendMessage(key).queue();
    }
    public static void sendMessage(MessageChannel channel, String key, String complement) {
        channel.sendMessage(key + " " + complement).queue();
    }
}
