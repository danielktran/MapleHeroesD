package net.server.talk.handlers;


import java.util.Iterator;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.TalkPacket;
import net.world.guild.MapleGuild;
import net.world.guild.MapleGuildCharacter;
import net.world.World;
import net.world.World.Guild;
import tools.data.LittleEndianAccessor;

public class GuildChatHandler extends AbstractMaplePacketHandler {

	public GuildChatHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final int charID = lea.readInt();
		final int guildID = lea.readInt();
		final String message = lea.readMapleAsciiString();
		
		final MapleGuild guild = Guild.getGuild(guildID);
		if (guild != null) {
			Iterator<MapleGuildCharacter> itr = guild.getMembers().iterator();
			while (itr.hasNext()) {
				MapleGuildCharacter guildChr = itr.next();
				if (guildChr.isOnline()) { // Send message only to everyone who is online
					MapleCharacter mChr = World.getCharacterFromPlayerStorage(guildChr.getId());
					mChr.getClient().getTalkSession().sendPacket(TalkPacket.onGuildChat(c.getAccountID(), charID, guildID, message));
				}
			}
		}
	}
	
	@Override
	public boolean validateState(final MapleClient c) {
		return true;
	}

}
