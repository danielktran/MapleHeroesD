package net.server.channel.handlers.chat;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import client.MapleCharacterUtil;
import constants.ServerConstants.CommandType;
import net.world.World;
import tools.data.LittleEndianAccessor;
import server.commands.CommandProcessor;

public class PartyChatHandler extends AbstractMaplePacketHandler
{
	public PartyChatHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		final int type = lea.readByte();
	    final byte numRecipients = lea.readByte();
	    if (numRecipients <= 0) {
	        return;
	    }
	    int recipients[] = new int[numRecipients];

	    for (byte i = 0; i < numRecipients; i++) {
	        recipients[i] = lea.readInt();
	    }
	    final String chattext = lea.readMapleAsciiString();
	    if (chr == null || !chr.getCanTalk()) {
	        c.getSession().write(CWvsContext.broadcastMsg(6, "You have been muted and are therefore unable to talk."));
	        return;
	    }

	    if (c.isMonitored()) {
	        String chattype = "Unknown";
	        switch (type) {
	            case 0:
	                chattype = "Buddy";
	                break;
	            case 1:
	                chattype = "Party";
	                break;
	            case 2:
	                chattype = "Guild";
	                break;
	            case 3:
	                chattype = "Alliance";
	                break;
	            case 4:
	                chattype = "Expedition";
	                break;
	        }
	        World.Broadcast.broadcastGMMessage(
	                CWvsContext.broadcastMsg(6, "[GM Message] " + MapleCharacterUtil.makeMapleReadable(chr.getName())
	                        + " said (" + chattype + "): " + chattext));

	    }
	    if (chattext.length() <= 0 || CommandProcessor.processCommand(c, chattext, CommandType.NORMAL)) {
	        return;
	    }
	    chr.getCheatTracker().checkMsg();
	    switch (type) {
	        case 0:
	            World.Buddy.buddyChat(recipients, chr.getID(), chr.getName(), chattext);
	            break;
	        case 1:
	            if (chr.getParty() == null) 
	            {
	                break;
	            }
	            World.Party.partyChat(chr.getParty().getId(), chattext, chr.getName());
	            break;
	        case 2:
	            if (chr.getGuildId() <= 0) 
	            {
	                break;
	            }
	            World.Guild.guildChat(chr.getGuildId(), chr.getName(), chr.getID(), chattext);
	            break;
	        case 3:
	            if (chr.getGuildId() <= 0) 
	            {
	                break;
	            }
	            World.Alliance.allianceChat(chr.getGuildId(), chr.getName(), chr.getID(), chattext);
	            break;
	        case 4:
	            if (chr.getParty().getExpeditionId() <= 0) 
	            {
	                break;
	            }
	            World.Party.expedChat(chr.getParty().getExpeditionId(), chattext, chr.getName());
	            break;
	    }
	}
}
