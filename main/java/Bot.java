import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.ArrayList;

public class Bot extends ListenerAdapter
{
    public ArrayList<String> banList = new ArrayList<>();
    public static void main(String[] args) throws LoginException
    {
        if (args.length < 1) {
            System.out.println("You have to provide a token as first argument!");
            System.exit(1);
        }
        // args[0] should be the token
        // We only need 2 intents in this bot. We only respond to messages in guilds and private channels.
        // All other events will be disabled.
        JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new Bot())
                .setActivity(Activity.playing("All my homies hate gaevon"))
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        Message msg = event.getMessage();
        User user = event.getAuthor();
        Member member = event.getMember();
        mute(msg, user, member, event);
        runServer(msg, user, member, event);
    }
    public void test(){

    }
    public void runServer(Message msg, User user, Member member, MessageReceivedEvent event) {
        //RUNS THE MC SERVER
        if (event.getChannel().getName().equals("mc-server") &&
                msg.getContentRaw().startsWith("!startServer")) {
            Runtime rt = Runtime.getRuntime();
            try {
                Process pr = rt.exec("java -Xmx4096M -cp server -jar server.jar --nogui");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void mute(Message msg, User user, Member member, MessageReceivedEvent event) {
        if (msg.getContentRaw().equals("!ping")) {
            System.out.println("pinged");
            MessageChannel channel = event.getChannel();
            long time = System.currentTimeMillis();
            channel.sendMessage("Pong!") /* => RestAction<Message> */
                    .queue(response /* => Message */ -> {
                        response.editMessageFormat("Pong: %d ms", System.currentTimeMillis() - time).queue();
                    });
        } else if (msg.getContentRaw().startsWith("!mute")) {
            if (user.getAsTag().equals("Nice#6029")) {
                String mess = msg.getContentRaw();
                String banned_user = mess.substring(6);
                banList.add(banned_user);
                System.out.println("banned " + banned_user);
            }
            else {
                System.out.println(user.getAsTag());
                System.out.println(user);
                System.out.println(member);
                System.out.println(member.getId());
            }
        } else if (msg.getContentRaw().startsWith("!unmute")) {
            if (user.getAsTag().equals("Nice#6029")) {
                String mess = msg.getContentRaw();
                String banned_user = mess.substring(8);
                if (banList.contains(banned_user))
                    banList.remove(banList.indexOf(banned_user));
                System.out.println("unbanned " + banned_user);
            }
        }
        for (String s :banList) {
            if (s.contains(member.getId())) {
                event.getMessage().delete().queue();
                System.out.println("Removed message from : "+ user.getAsTag() + " " + event.getMessage().getContentRaw());
            }
        }
    }
}
