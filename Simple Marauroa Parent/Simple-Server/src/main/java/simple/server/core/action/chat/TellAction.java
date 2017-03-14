package simple.server.core.action.chat;

import java.util.StringTokenizer;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.SlotOwner;
import marauroa.server.game.rp.IRPRuleProcessor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import simple.common.Grammar;
import simple.common.game.ClientObjectInterface;
import simple.server.core.action.ActionProvider;
import simple.server.core.action.CommandCenter;
import static simple.server.core.action.WellKnownActionConstant.TARGET;
import static simple.server.core.action.WellKnownActionConstant.TEXT;
import simple.server.core.action.admin.AdministrationAction;
import simple.server.core.engine.SimpleRPRuleProcessor;
import simple.server.core.entity.RPEntityInterface;
import simple.server.core.event.LoginListener;

/**
 * handles /tell-action (/msg-action).
 */
@ServiceProvider(service = ActionProvider.class)
public class TellAction implements ActionProvider {

    protected String text;
    protected String senderName;
    protected String receiverName;
    protected RPEntityInterface sender;
    protected RPEntityInterface receiver;
    public static final String TELL = "tell";

    protected void init(RPEntityInterface player, RPAction action) {
        text = action.get(TEXT).trim();
        senderName = player.getName();
        receiverName = action.get(TARGET);
        sender = player;
        receiver = ((SimpleRPRuleProcessor) Lookup.getDefault()
                .lookup(IRPRuleProcessor.class)).getPlayer(receiverName);
    }

    protected boolean validateAction(RPAction action) {
        return action.has(TARGET) && action.has(TEXT);
    }

    protected boolean checkOnline() {
        if ((receiver == null) || (receiver.isGhost()
                && (sender.getAdminLevel() < AdministrationAction
                .getLevelForCommand("ghostmode")))) {
            sender.sendPrivateText("No player named \"" + receiverName
                    + "\" is currently active.");
            return false;
        }
        return true;
    }

    protected boolean checkIgnoreList(ClientObjectInterface player) {
        // check ignore list
        String reply = receiver.getIgnore(senderName);
        if (reply != null) {
            // sender is on ignore list
            if (reply.length() == 0) {
                tellIgnorePostman(player, Grammar.suffix_s(receiverName)
                        + " mind is not attuned to yours, so you cannot "
                        + "reach them.");
            } else {
                tellIgnorePostman(player, receiverName
                        + " is ignoring you: " + reply);
            }
            return false;
        }
        return true;
    }

    protected String createFullMessageText() {
        if (senderName.equals(receiverName)) {
            return "You mutter to yourself: " + text;
        } else {
            return senderName + " tells you: " + text;
        }
    }

    protected void extractRealSenderFromMessageInCaseItWasSendViaPostman() {
        // HACK: extract sender from postman messages
        StringTokenizer st = new StringTokenizer(text, " ");
        if (senderName.equals("postman") && (st.countTokens() > 2)) {
            String temp = st.nextToken();
            String command = st.nextToken();
            if (command.equals("asked")) {
                senderName = temp;
            }
        }
    }

    protected void tellAboutAwayStatusIfNeccessary() {
        // Handle /away messages
        String away = receiver.getAwayMessage();
        if (away != null) {
            if (receiver.isAwayNotifyNeeded(senderName)) {
                // Send away response
                tellIgnorePostman(sender, receiverName + " is away: " + away);
            }
        }
    }

    protected void tellIgnorePostman(RPEntityInterface receiver,
            String message) {
        if (!receiver.getName().equals("postman")) {
            receiver.sendPrivateText(message);
        }
    }

    @Override
    public void onAction(RPObject rpo, RPAction action) {
        if (rpo instanceof ClientObjectInterface) {
            ClientObjectInterface player = (ClientObjectInterface) rpo;
            if (Lookup.getDefault().lookup(LoginListener.class)
                    .checkIsGaggedAndInformPlayer(player)) {
                return;
            }

            if (!validateAction(action)) {
                return;
            }

            init(player, action);

            /*
             * If the receiver is not logged in or if it is a ghost and you
             * don't have the level to see ghosts...
             */
            if (!checkOnline()) {
                return;
            }

            String message = createFullMessageText();
            extractRealSenderFromMessageInCaseItWasSendViaPostman();

            if (!checkIgnoreList(player)) {
                return;
            }

            // check grumpiness
            if (!checkGrumpy()) {
                return;
            }

            // transmit the message
            receiver.sendPrivateText(message);

            if (!senderName.equals(receiverName)) {
                player.sendPrivateText("You tell " + receiverName + ": "
                        + text);
            }

            tellAboutAwayStatusIfNeccessary();

            receiver.setLastPrivateChatter(senderName);
            ((SimpleRPRuleProcessor) Lookup.getDefault()
                    .lookup(IRPRuleProcessor.class))
                    .addGameEvent(player.getName(), "chat",
                            receiverName, Integer.toString(text.length()),
                            text.substring(0, Math.min(text.length(), 1000)));
        }
    }

    protected boolean checkGrumpy() {
        String grumpy = receiver.getGrumpyMessage();
        if (grumpy != null && ((SlotOwner) receiver).getSlot("!buddy")
                .size() > 0) {
            RPObject buddies = ((SlotOwner) receiver).getSlot("!buddy")
                    .iterator().next();
            boolean senderFound = false;
            for (String buddyName : buddies) {
                if (buddyName.charAt(0) == '_') {
                    buddyName = buddyName.substring(1);
                }
                if (buddyName.equals(senderName)) {
                    senderFound = true;
                    break;
                }
            }
            if (!senderFound) {
                // sender is not a buddy
                if (grumpy.length() == 0) {
                    tellIgnorePostman(sender,
                            receiverName + " has a closed mind, and is seeking"
                            + " solitude from all but close friends");
                } else {
                    tellIgnorePostman(sender,
                            receiverName + " is seeking solitude from all but "
                            + "close friends: " + grumpy);
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public void register() {
        CommandCenter.register(TELL, new TellAction());
    }
}