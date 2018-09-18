package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.field.AndroidPacket;
import tools.data.LittleEndianAccessor;

public class AndroidFaceExpressionHandler extends AbstractMaplePacketHandler {

	public AndroidFaceExpressionHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final int emote = lea.readInt();
		
		if ((emote > 0) && (chr != null) && (chr.getMap() != null) && (!chr.isHidden()) && (emote <= 17) && (chr.getAndroid() != null)) {
            chr.getMap().broadcastMessage(AndroidPacket.showAndroidEmotion(chr.getID(), (byte) emote)); 
        }
	}

}
