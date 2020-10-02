package musicbot.myjda;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class MyListener extends ListenerAdapter {
    private final MusicManager musicManager;

    public MyListener(MusicManager mm) {
        this.musicManager = mm;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        /* Forward messages caught from discord to correct module */


        super.onMessageReceived(event);


        // Discard messages from bots
        if (event.getAuthor().isBot()) return;


        // Add guild to list
        BotCore.addGuildToList(event.getGuild());

        String messageString = getMessageString(event);


        String prefix = BotStrings.prefix;
        if (messageString.startsWith(prefix + BotStrings.cmdPlay))
        {
            musicManager.cmdPlay(event, messageString);
        }
        else if (messageString.startsWith(prefix + BotStrings.cmdPlaylist)) {
            musicManager.cmdPlaylist(event, messageString);
        }
        else if (messageString.startsWith(prefix + BotStrings.cmdStop))
        {
            musicManager.stopQueue(event.getGuild());
        }
        else if (messageString.startsWith(prefix + BotStrings.cmdSkip))
        {
            musicManager.skipSong(event.getGuild());
        }
        else if (messageString.startsWith(prefix + BotStrings.cmdLeave))
        {
            musicManager.disconnectAnyway(event.getGuild());
        }
    }


    @NotNull
    private String getMessageString(@Nonnull MessageReceivedEvent event) {
        /* This function receives the message event and returns the string of it */

        Message message = event.getMessage();
        return message.getContentDisplay();
    }
}
