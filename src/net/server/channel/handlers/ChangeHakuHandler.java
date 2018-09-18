package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import tools.data.LittleEndianAccessor;

public class ChangeHakuHandler extends AbstractMaplePacketHandler {

	public ChangeHakuHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		int oid = lea.readInt();
        if (chr.getHaku() != null) {
            chr.getHaku().sendStats();
            chr.getMap().broadcastMessage(chr, CField.transformHakuEffect(chr.getID()), true);
            //chr.getMap().broadcastMessage(chr, CField.spawnHaku_change1(chr.getHaku()), true);
            //chr.getMap().broadcastMessage(chr, CField.spawnHaku_bianshen(chr.getID(), oid, chr.getHaku().getStats()), true);
        }
	}

}
