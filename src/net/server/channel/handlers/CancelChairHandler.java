package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.field.UserPacket;
import tools.data.LittleEndianAccessor;

public class CancelChairHandler extends AbstractMaplePacketHandler {

	public CancelChairHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		final short id = lea.readShort();
		
		/*
		if (id == -1) {
            chr.cancelFishingTask();
            chr.setChair(0);
            c.getSession().write(CField.cancelChair(-1));
            if (chr.getMap() != null) {
                chr.getMap().broadcastMessage(chr, CField.showChair(chr.getId(), 0), false);
            }
        } else {
            chr.setChair(id);
            c.getSession().write(CField.cancelChair(id));
        }
        */
		if (id == -1) {
            chr.cancelFishingTask();
            chr.setChair(0);
            c.getSession().write(UserPacket.cancelChair(chr.getID()));
            if (chr.getMap() != null) {
                chr.getMap().broadcastMessage(chr, CField.showChair(chr.getID(), 0), false);
            }
        }
		
	}

}
