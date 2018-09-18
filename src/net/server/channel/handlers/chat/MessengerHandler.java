package net.server.channel.handlers.chat;

import client.MapleCharacterUtil;
import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.CWvsContext;
import net.world.MapleMessenger;
import net.world.MapleMessengerCharacter;
import net.world.World;
import tools.data.LittleEndianAccessor;

public class MessengerHandler extends AbstractMaplePacketHandler {
	public MessengerHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		String input;
        MapleMessenger messenger = c.getCharacter().getMessenger();

        switch (lea.readByte()) {
            case 0x00: // open
                System.out.println("0");
                if (messenger == null) {
                    System.out.println("1");
                    lea.readByte();
                    byte mode = lea.readByte();
                    int messengerid = lea.readInt();
                    if (messengerid == 0) { // create
                        System.out.println("2");
                        c.getCharacter().setMessenger(World.Messenger.createMessenger(new MapleMessengerCharacter(c.getCharacter())));
                    } else { // join
                        System.out.println("3");
                        messenger = World.Messenger.getMessenger(messengerid);
                        if (messenger != null) {
                            System.out.println("4");
                            final int position = messenger.getLowestPosition();
                            if (position > -1 && position < 7) {
                                System.out.println("5");
                                c.getCharacter().setMessenger(messenger);
                                World.Messenger.joinMessenger(messenger.getId(), new MapleMessengerCharacter(c.getCharacter()), c.getCharacter().getName(), c.getChannel());
                            }
                        }
                    }
                }
                break;
            case 0x02: // exit
                if (messenger != null) {
                    final MapleMessengerCharacter messengerplayer = new MapleMessengerCharacter(c.getCharacter());
                    World.Messenger.leaveMessenger(messenger.getId(), messengerplayer);
                    c.getCharacter().setMessenger(null);
                }
                break;
            case 0x03: // invite
                if (messenger != null) {
                    final int position = messenger.getLowestPosition();
                    if (position <= -1 || position >= 7) {
                        return;
                    }
                    input = lea.readMapleAsciiString();
                    final MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(input);

                    if (target != null) {
                        if (target.getMessenger() == null){
                            if (!target.isIntern() || c.getCharacter().isIntern()){
                                c.getSession().write(CField.messengerNote(input, 4, 1));
                                target.getClient().getSession().write(CField.messengerInvite(c.getCharacter().getName(), messenger.getId()));
                            } else {
                                c.getSession().write(CField.messengerNote(input, 4, 0));
                            }
                        } else {
                            c.getSession().write(CField.messengerChat(c.getCharacter().getName(), " : " + target.getName() + " is already using Maple Messenger."));
                        }
                    } else {
                        if (World.isConnected(input)) {
                            World.Messenger.messengerInvite(c.getCharacter().getName(), messenger.getId(), input, c.getChannel(), c.getCharacter().isIntern());
                        } else {
                            c.getSession().write(CField.messengerNote(input, 4, 0));
                        }
                    }
                }
                break;
            case 0x05: // decline
                final String targeted = lea.readMapleAsciiString();
                final MapleCharacter target = c.getChannelServer().getPlayerStorage().getCharacterByName(targeted);
                if (target != null) { // This channel
                    if (target.getMessenger() != null) {
                        target.getClient().getSession().write(CField.messengerNote(c.getCharacter().getName(), 5, 0));
                    }
                } else { // Other channel
                    if (!c.getCharacter().isIntern()) {
                        World.Messenger.declineChat(targeted, c.getCharacter().getName());
                    }
                }
                break;
            case 0x06: // message
                if (messenger != null) {
                    final String charname = lea.readMapleAsciiString();
                    final String text = lea.readMapleAsciiString();
                    if (!c.getCharacter().isIntern() && text.length() >= 1000) {
                        return;
                    }
                    final String chattext = charname + "" + text;
                    World.Messenger.messengerChat(messenger.getId(), charname, text, c.getCharacter().getName());
                    if (messenger.isMonitored() && chattext.length() > c.getCharacter().getName().length() + 3) { //name : NOT name0 or name1
                    	World.Broadcast.broadcastGMMessage(
                                CWvsContext.broadcastMsg(
                                        6, "[GM Message] " + MapleCharacterUtil.makeMapleReadable(c.getCharacter().getName()) + "(Messenger: "
                                        + messenger.getMemberNamesDEBUG() + ") said: " + chattext));
                    }
                }
                break;
            case 0x09: //like
                if (messenger != null) {
                    String charname = lea.readMapleAsciiString();
                    //todo send like packet here
                }
                break;
            case 0x0A: //guidance
                if (messenger != null) {
                    lea.readByte();
                    String charname = lea.readMapleAsciiString();
                    String targetname = lea.readMapleAsciiString();
                    //todo send guide packet here
                }
                break;
            case 0x0B: //char info
                if (messenger != null) {
                    String charname = lea.readMapleAsciiString();
                    MapleCharacter character = c.getChannelServer().getPlayerStorage().getCharacterByName(charname);
                    c.getSession().write(CField.messengerCharInfo(character));
                }
                break;
            case 0x0E: //whisper
                if (messenger != null) {
                    String charname = lea.readMapleAsciiString();
                    //todo send whisper packet here
                }
                break;
        }
	}
}
