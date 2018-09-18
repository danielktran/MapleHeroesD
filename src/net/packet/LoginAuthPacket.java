package net.packet;

import net.SendPacketOpcode;
import tools.data.MaplePacketWriter;

public class LoginAuthPacket {
	
	public static byte[] handleLogin(String username) {
		MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LOGIN_AUTH_RESPONSE1);
		mpw.writeInt(8);
		mpw.writeInt(0);
		mpw.writeShort(0);
		mpw.writeLoginAuthString(username);
		mpw.writeInt(0);
		mpw.writeInt(0);
		mpw.writeInt(87);
		mpw.writeShort(0);
		
		return mpw.getPacket();
	}
	
	public static byte[] handleLogin2() {
		MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LOGIN_AUTH_RESPONSE2);
		mpw.writeInt(0);
		mpw.writeShort(0);
		mpw.writeMapleNullTerminatedCharString("RANDOM STUFF");
		mpw.writeMapleNullTerminatedCharString("RANDOM NAME");
		mpw.writeInt(1234); // Account ID
		mpw.writeShort(0x16);
		mpw.writeInt(1);
		mpw.write(0);
		mpw.writeInt(0); // Token part 1
		mpw.writeInt(0); // Token part 2
		mpw.writeInt(0); // Token part 3
		mpw.writeInt(0); // Token part 4
		
		return mpw.getPacket();
	}

	public static byte[] handleLogin3() {
		MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LOGIN_AUTH_RESPONSE3);
		mpw.writeInt(2);
		mpw.writeInt(0);
		mpw.writeShort(0);
		
		return mpw.getPacket();
	}
}
