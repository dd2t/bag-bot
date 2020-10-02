package musicbot.mylavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import musicbot.myjda.BotCore;
import musicbot.myjda.BotStrings;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private Guild guild;

    private PlayerManager() {
        this.playerManager = new DefaultAudioPlayerManager();
        this.musicManagers = new HashMap<>();

        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    public synchronized GuildMusicManager getGuildMusicManager(Guild guild) {
        long guildId = guild.getIdLong();
        this.guild = guild;
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void loadAndPlay(TextChannel channel, String trackUrl) {
        GuildMusicManager musicManager = getGuildMusicManager(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                BotCore.sendMessage(channel, BotStrings.msgAddingToQueue, track.getInfo().title);
                play(musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (playlist.isSearchResult()) {
                    if (firstTrack == null) {
                        firstTrack = playlist.getTracks().get(0);
                    }

                    final String playlistName = playlist.getName()
                            .replace("Search results for: ", "");
                    BotCore.sendMessage(channel, BotStrings.msgAddingToQueue,
                            BotStrings.addingPlaylist + playlistName);

                    play(musicManager, firstTrack);
                }
                else {
                    for (AudioTrack a : playlist.getTracks()) {

                        if (a == null) {
                            continue;
                        }

                        BotCore.sendMessage(channel, BotStrings.msgAddingToQueue,
                                BotStrings.addingPlaylist + a.getInfo().uri);

                        play(musicManager, a);
                    }
                }
            }

            @Override
            public void noMatches() {
                BotCore.sendMessage(channel, BotStrings.msgSongNotFound, trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                BotCore.sendMessage(channel, BotStrings.msgCouldNotPlay, exception.getMessage());
            }
        });

    }

    private void play(GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());
        musicManager.scheduler.queue(track);
    }

    public static synchronized PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                audioManager.openAudioConnection(voiceChannel);
                break;
            }
        }
    }
}
