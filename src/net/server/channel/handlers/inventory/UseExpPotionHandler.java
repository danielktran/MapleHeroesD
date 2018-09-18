package net.server.channel.handlers.inventory;

import client.MapleClient;
import client.character.MapleCharacter;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CWvsContext;
import server.MapleInventoryManipulator;
import tools.data.LittleEndianAccessor;

public class UseExpPotionHandler extends AbstractMaplePacketHandler {

	public UseExpPotionHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		//slea: [F5 4F D6 2E] [60 00] [F4 06 22 00]
        System.err.println("eror");
        c.getCharacter().updateTick(lea.readInt());
        final byte slot = (byte) lea.readShort();
        int itemid = lea.readInt();
        final Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if (toUse == null || toUse.getQuantity() < 1
                || toUse.getItemId() != itemid || chr.getLevel() >= 250
                || chr.hasBlockedInventory() || itemid / 10000 != 223) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (itemid != 2230004) { //for now
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        int level = chr.getLevel();
        chr.gainExp(chr.getNeededExp() - chr.getExp(), true, true, false);
        boolean first = false;
        boolean last = false;
        int potionDstLevel = 18;
        if (!chr.getInfoQuest(7985).contains("2230004=")) {
            first = true;
        } else {
            if (chr.getInfoQuest(7985).equals("2230004=" + potionDstLevel + "#384")) {
                last = true;
            }
        }
        c.getSession().write(CWvsContext.updateExpPotion(last ? 0 : 2, chr.getID(), itemid, first, level, potionDstLevel));
        if (first) {
            chr.updateInfoQuest(7985, "2230004=" + level + "#384");
        }
        if (last) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
        }
        c.getSession().write(CWvsContext.enableActions());
	}

}
