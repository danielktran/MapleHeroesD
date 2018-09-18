package net.server.channel.handlers;

import client.MapleClient;
import client.MonsterFamiliar;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import tools.data.LittleEndianAccessor;

public class SpawnFamiliarHandler extends AbstractMaplePacketHandler {

	public SpawnFamiliarHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		c.getCharacter().updateTick(lea.readInt());
        int mId = lea.readInt();
        c.getSession().write(CWvsContext.enableActions());
        c.getCharacter().removeFamiliar();
        if ((c.getCharacter().getFamiliars().containsKey(Integer.valueOf(mId))) && (lea.readByte() > 0)) {
            MonsterFamiliar mf = (MonsterFamiliar) c.getCharacter().getFamiliars().get(Integer.valueOf(mId));
            if (mf.getFatigue() > 0) {
                c.getCharacter().dropMessage(1, "Please wait " + mf.getFatigue() + " seconds to summon it.");
            } else {
                c.getCharacter().spawnFamiliar(mf, false);
            }
        }
	}

}
