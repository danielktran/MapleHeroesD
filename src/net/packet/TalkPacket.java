package net.packet;

import net.SendPacketOpcode;
import tools.data.MaplePacketWriter;

public class TalkPacket {

	public static byte[] onPing() {
		MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PING_TALK);
				
		return mpw.getPacket();
	}
	
	public static byte[] onMigrateResponse() {
		MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MIGRATE_RESPONSE);
		mpw.write(0);
		
		return mpw.getPacket();
	}
	
	public static byte[] onTalkSessionID(int accountID) {
		MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UNK_RESPONSE);
		mpw.write(0);
		mpw.writeInt(accountID);
		
		return mpw.getPacket();
	}
	
	public static byte[] onGuildChat(int accountID, int charID, int guildID, String message) {
		MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GUILDCHAT);
		mpw.writeInt(accountID);
		mpw.writeInt(guildID);
		mpw.writeInt(accountID);
		mpw.writeInt(charID);
		mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
		mpw.writeMapleAsciiString(message);
		mpw.write(0);
		
		return mpw.getPacket();
	}
}
