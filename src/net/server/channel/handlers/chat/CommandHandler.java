package net.server.channel.handlers.chat;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.CWvsContext;
import net.server.channel.ChannelServer;
import net.world.World;
import tools.data.LittleEndianAccessor;

public class CommandHandler extends AbstractMaplePacketHandler
{
	public CommandHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		final byte mode = lea.readByte();
        lea.readInt(); //ticks
        switch (mode) {
            case 68: //buddy
            case 5: { // Find

                final String recipient = lea.readMapleAsciiString();
                MapleCharacter player = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient);
                if (player != null) {
                    if (!player.isIntern() || c.getCharacter().isIntern() && player.isIntern()) {

                        c.getSession().write(CField.getFindReplyWithMap(player.getName(), player.getMap().getId(), mode == 72));//68
                    } else {
                        c.getSession().write(CField.getWhisperReply(recipient, (byte) 0));
                    }
                } else { // Not found
                    int ch = World.Find.findChannel(recipient);
                    if (ch > 0) {
                        player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipient);
                        if (player == null) {
                            break;
                        }
                        if (player != null) {
                            if (!player.isIntern() || (c.getCharacter().isIntern() && player.isIntern())) {
                                c.getSession().write(CField.getFindReply(recipient, (byte) ch, mode == 68));
                            } else {
                                c.getSession().write(CField.getWhisperReply(recipient, (byte) 0));
                            }
                            return;
                        }
                    }
                    if (ch == -10) {
                        c.getSession().write(CField.getFindReplyWithCS(recipient, mode == 68));
                    } else {
                        c.getSession().write(CField.getWhisperReply(recipient, (byte) 0));
                    }
                }
                break;
            }
            case 6: { // Whisper
                if (c.getCharacter() == null || c.getCharacter().getMap() == null) {
                    return;
                }
                if (!c.getCharacter().getCanTalk()) {
                    c.getSession().write(CWvsContext.broadcastMsg(6, "You have been muted and are therefore unable to talk."));
                    return;
                }
                c.getCharacter().getCheatTracker().checkMsg();
                final String recipient = lea.readMapleAsciiString();
                final String text = lea.readMapleAsciiString();
                final int ch = World.Find.findChannel(recipient);
                if (!c.getCharacter().isIntern() && text.length() >= 80) {
                    return;
                }
                if (ch > 0) {
                    MapleCharacter player = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(recipient);
                    if (player == null) {
                        break;
                    }
                    player.getClient().getSession().write(CField.getWhisper(c.getCharacter().getName(), c.getChannel(), text));
                    if (!c.getCharacter().isIntern() && player.isIntern()) {
                        c.getSession().write(CField.getWhisperReply(recipient, (byte) 0));
                    } else {
                        c.getSession().write(CField.getWhisperReply(recipient, (byte) 1));
                    }
                    if (c.isMonitored()) {
                        World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, c.getCharacter().getName() + " whispered " + recipient + " : " + text));
                    } else if (player.getClient().isMonitored()) {
                        World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, c.getCharacter().getName() + " whispered " + recipient + " : " + text));
                    }
                } else {
                    c.getSession().write(CField.getWhisperReply(recipient, (byte) 0));
                }
            }
            break;
        }
	}
}
