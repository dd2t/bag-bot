package musicbot.myjda;

import musicbot.mylavaplayer.GuildMusicManager;
import musicbot.mylavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicManager {
    private final PlayerManager playerManager = PlayerManager.getInstance();

    public void skipSong(Guild guild) {
        /* This function skip a song from queue of guild input */

        GuildMusicManager guildMusicManager = playerManager.getGuildMusicManager(guild);
        guildMusicManager.scheduler.nextTrack();
    }

    public void stopQueue(Guild guild) {
        /* This function end the queue of guild input */

        GuildMusicManager guildMusicManager = playerManager.getGuildMusicManager(guild);
        guildMusicManager.player.stopTrack();
        disconnectAnyway(guild);
    }

    public void cmdPlaylist(@Nonnull MessageReceivedEvent event, String messageString) {
        /* This function play a playlist received as link */
        VoiceChannel voiceChannel = event
                .getMember().getVoiceState().getChannel();

        // Only user in voice channels can play songs
        if (!IsThisMemberInVoiceChannel(event.getChannel(), voiceChannel)) return;

        final String trackUrl = messageString.replace(BotStrings.prefix + BotStrings.cmdPlaylist + " ",
                "");

        loadAndPlay(event, trackUrl);
    }

    public void cmdPlay(@Nonnull MessageReceivedEvent event, String messageString) {
        /* This function does the preparation and put the song in message to play */

        VoiceChannel voiceChannel = event
                .getMember().getVoiceState().getChannel();

        // Only user in voice channels can play songs
        if (!IsThisMemberInVoiceChannel(event.getChannel(), voiceChannel)) return;

        // Clean the url message
        final String trackUrl = getTrackUrl(messageString, BotStrings.cmdPlay);

        // If there is nothing stop here
        if (trackUrl.contains(";play") || trackUrl.equals("")) return;

        loadAndPlay(event, trackUrl);
    }

    private void loadAndPlay(@Nonnull MessageReceivedEvent event, String trackUrl) {
        /* Put the song in the queue */

        playerManager.loadAndPlay((TextChannel) event.getChannel(), trackUrl);
        playerManager.getGuildMusicManager(event.getGuild()).player.setVolume(BotStrings.volume);
    }

    private boolean IsThisMemberInVoiceChannel(MessageChannel msgChannel, VoiceChannel voiceChannel) {
        /* Receive VoiceChannel of a member and return if there is one */

        if (voiceChannel == null)
        {
            BotCore.sendMessage(msgChannel, BotStrings.msgOutOfVoiceChannel);
            return false;
        }
        return true;
    }

    private String getTrackUrl(String messageString, String cmd) {
        /* This function receives the message, removes the prefix command and if it is not an youtube link
         *  adds a prefix to flag it */

        final String cmdPrefix = BotStrings.prefix + cmd + " ";
        if (messageString.contains(cmdPrefix)) {
            messageString = messageString.replace(cmdPrefix, "");
        }

        if (!isYTLink(messageString)) {
            messageString = "ytsearch: " + messageString;
        }

        return messageString;
    }

    private boolean isYTLink(String input) {
        final String YT_REG = BotStrings.YT_REGEX;

        Pattern compiledPattern = Pattern.compile(YT_REG, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(input);
        return matcher.find();
    }

    public void disconnectIfQueueIsEmpty() {
        List<Guild> guilds = BotCore.getGuildList();
        if (!guilds.isEmpty()) {
            for (Guild g : guilds) {
                if (playerManager.getGuildMusicManager(g).scheduler.isQueueEmpty()) {
                    g.getAudioManager().closeAudioConnection();
                    BotCore.removeGuildToList(g);
                }
            }
        }
    }

    public void disconnectAnyway(Guild guild) {
        playerManager.getGuildMusicManager(guild).scheduler.cleanQueue();
        guild.getAudioManager().closeAudioConnection();
        BotCore.removeGuildToList(guild);
    }
}
