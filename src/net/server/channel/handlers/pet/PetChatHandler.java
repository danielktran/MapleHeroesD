package net.server.channel.handlers.pet;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.PetPacket;
import tools.data.LittleEndianAccessor;

public class PetChatHandler extends AbstractMaplePacketHandler {

	public PetChatHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		if (lea.available() < 12) {
            return;
        }
        final int petid = c.getCharacter().getPetIndex((int) lea.readLong());
        c.getCharacter().updateTick(lea.readInt());
        final short command = lea.readShort();
        final String text = lea.readMapleAsciiString();
        
        if (chr == null || chr.getMap() == null || chr.getPet(petid) == null) {
            return;
        }
        chr.getMap().broadcastMessage(chr, PetPacket.petChat(chr.getID(), command, text, (byte) petid), true);
	}

}
