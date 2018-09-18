package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import tools.data.LittleEndianAccessor;

public class SpawnPetHandler extends AbstractMaplePacketHandler {

	public SpawnPetHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		chr.updateTick(lea.readInt());
        chr.spawnPet(lea.readByte(), lea.readByte() > 0);
	}

}
