package musicbot.vocalcord;

import com.google.cloud.texttospeech.v1beta1.SsmlVoiceGender;
import musicbot.myjda.BotCore;
import musicbot.myjda.BotStrings;
import musicbot.myjda.MusicManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import vocalcord.CommandChain;
import vocalcord.UserStream;
import vocalcord.VocalCord;

public class MyVocal extends ListenerAdapter implements VocalCord.Callbacks {
    private final VocalCord cord;
    private Message message;
    private MessageReceivedEvent lastEvent;
    private MusicManager musicManager;


    public MyVocal(MusicManager mm) {
        this.musicManager = mm;
        this.cord = VocalCord.newConfig(this)
                .withWakeDetection(BotStrings.jniLocation,
                        BotStrings.porcupineLocation,
                        BotStrings.porcupineParams,
                        0.5f,
                        BotStrings.wakePhrasePath)
                .withTTS(SsmlVoiceGender.MALE, true)
                .build();
    }

    @Override
    public CommandChain onTranscribed() {
        return new CommandChain.Builder().addPhrase(BotStrings.cmdHelp, (user, transcript, args) -> {
            sendMessage(BotStrings.helpAnswer);
        })
                .addPhrase(BotStrings.cmdPlay, (user, transcript, args) -> {
                    // TODO;
                    if (lastEvent != null) {
                        musicManager.cmdPlay(lastEvent, BotStrings.prefix + transcript);
                    }
                })
                .addPhrase(BotStrings.cmdSkip, (user, transcript, args) -> {
                    musicManager.skipSong(lastEvent.getGuild());
                })
                .addPhrase(BotStrings.cmdStop, (user, transcript, args) -> {
                    musicManager.stopQueue(lastEvent.getGuild());
                })
                .addPhrase(BotStrings.cmdLeave, (user, transcript, args) -> {
                    musicManager.disconnectAnyway(lastEvent.getGuild());
                })
                .withFallback(((user, transcript, args) -> {
                    sendMessage(BotStrings.msgBotDidNotUnderstand);
                })).withMinThreshold(0.5f).build();
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) return;

        lastEvent = event;
        message = event.getMessage();
        String content = message.getContentRaw();
        if (content.equals(BotStrings.prefix + BotStrings.botVoiceModuleSummon)) {

            sendMessage(message.getAuthor().getName() + " " + BotStrings.msgWhenSomeoneSummon);

            try {
                VoiceChannel authorVoiceChannel = event.getMember().getVoiceState().getChannel();
                cord.connect(authorVoiceChannel);

            } catch (Exception e) {
                e.printStackTrace();
            }
            cord.say(BotStrings.saySummon);
        }
    }


    @Override
    public boolean canWakeBot(User user) {
        return true;
    }

    @Override
    public void onWake(UserStream userStream, int keywordIndex) {
        sendMessage(BotStrings.onBotWakeUp);
    }

    private void sendMessage(String s) {
        if (s.equals("") || message == null) return;
        BotCore.sendMessage(message.getChannel(), s);
    }
}
