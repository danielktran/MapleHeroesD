package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import server.MapleInventoryManipulator;
import tools.data.LittleEndianAccessor;

public class ItemMoveHandler extends AbstractMaplePacketHandler {

	public ItemMoveHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, final MapleCharacter chr) {
		if (c.getCharacter().hasBlockedInventory()) { //hack
            return;
        }
        c.getCharacter().setScrolledPosition((short) 0);
        c.getCharacter().updateTick(lea.readInt());
        final MapleInventoryType type = MapleInventoryType.getByType(lea.readByte());
        final short src = lea.readShort();
        final short dst = lea.readShort();
        final short quantity = lea.readShort();
        System.out.println("item move " + type.name() + " " + src + " " + dst + " " + quantity);

        if (src < 0 && dst > 0) {
            MapleInventoryManipulator.unequip(c, src, dst);
        } else if (dst < 0) {
            MapleInventoryManipulator.equip(c, src, dst);
        } else if (dst == 0) {
            MapleInventoryManipulator.drop(c, type, src, quantity);
        } else {
            MapleInventoryManipulator.move(c, type, src, dst);
        }
	}

}
