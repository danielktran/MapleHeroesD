package net.server.channel.handlers.inventory;

import client.MapleClient;
import client.character.MapleCharacter;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CSPacket;
import net.packet.CWvsContext.InventoryPacket;
import server.MapleInventoryManipulator;
import tools.data.LittleEndianAccessor;

public class UseAlienSocketHandler extends AbstractMaplePacketHandler {

	public UseAlienSocketHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		c.getCharacter().updateTick(lea.readInt());
        c.getCharacter().setScrolledPosition((short) 0);
        final Item alienSocket = c.getCharacter().getInventory(MapleInventoryType.USE).getItem((byte) lea.readShort());
        final int alienSocketId = lea.readInt();
        final Item toMount = c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem((byte) lea.readShort());
        if (alienSocket == null || alienSocketId != alienSocket.getItemId() || toMount == null || c.getCharacter().hasBlockedInventory()) {
            c.getSession().write(InventoryPacket.getInventoryFull());
            return;
        }
        // Can only use once-> 2nd and 3rd must use NPC.
        final Equip eqq = (Equip) toMount;
        if (eqq.getSocketState() != 0) { // Used before
            c.getCharacter().dropMessage(1, "This item already has a socket.");
        } else {
            c.getSession().write(CSPacket.useAlienSocket(false));
            eqq.setSocket1(0); // First socket, GMS removed the other 2
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, alienSocket.getPosition(), (short) 1, false);
            c.getCharacter().forceReAddItem(toMount, MapleInventoryType.EQUIP);
        }
        c.getSession().write(CSPacket.useAlienSocket(true));
        //c.getPlayer().fakeRelog();
        //c.getPlayer().dropMessage(1, "Added 1 socket successfully to " + toMount);
	}

}