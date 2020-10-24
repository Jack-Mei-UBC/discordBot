import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.util.ArrayList;



public class Bot extends ListenerAdapter
{
    public static String fileName = "data.txt";
    public static ArrayList<String> banList;
    public static void main(String[] args) throws LoginException{
        if (args.length < 1) {
            System.out.println("You have to provide a token as first argument!");
            System.exit(1);
        }
        // args[0] should be the token
        // We only need 2 intents in this bot. We only respond to messages in guilds and private channels.
        // All other events will be disabled.
        reload();
        JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new Bot())
//                .setActivity(Activity.playing("All my homies hate gaevon"))
                .build();
    }
    public static void dm() {
        User owner = User.fromId("219905033876013058");
        RestAction<PrivateChannel> channel = owner.openPrivateChannel();

    }
    // reloads all the data needed from the save
    public static void reload() {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
            banList = (ArrayList<String>) in.readObject();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            banList = new ArrayList<String>();
        }
    }
    // saves all the data we need before closing the bot
    public static void end() {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(banList);
            out.close();
            for (String s : banList)
                System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        Message msg = event.getMessage();
        User user = event.getAuthor();
        Member member = event.getMember();

        mute(msg, user, member, event);
        runServer(msg, user, member, event);
        checkEnd(msg, user, member, event);
    }
    // Checks if I end the program
    public void checkEnd(Message msg, User user, Member member, MessageReceivedEvent event){
        String input = msg.getContentRaw();
        if (user.getId().equals("219905033876013058"))
            if (input.equals("!exit") || input.equals("!quit") )
                end();
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
            if (user.getId().equals("219905033876013058")) {
                String mess = msg.getContentRaw();
                String banned_user = mess.substring(5).trim();
                if (!banList.contains(banned_user))
                    banList.add(banned_user);
                else
                    System.out.println("Already muted user");
                System.out.println("muted " + banned_user);
            }
            else {
                System.out.println(user.getAsTag());
                System.out.println(user);
                System.out.println(member);
                System.out.println(member.getId());
            }
        } else if (msg.getContentRaw().startsWith("!unmute")) {
            if (user.getId().equals("219905033876013058")) {
                String mess = msg.getContentRaw();
                String banned_user = mess.substring(7).trim();
                if (banList.contains(banned_user))
                    banList.remove(banList.indexOf(banned_user));
                System.out.println("unmuted " + banned_user);
            }
        }
        for (String s :banList) {
            if (s.contains(member.getId())) {
                event.getMessage().delete().queue();
                System.out.println("Removed message from : "+ user.getAsTag() + " " + event.getMessage().getContentDisplay());
            }
        }
    }
}
