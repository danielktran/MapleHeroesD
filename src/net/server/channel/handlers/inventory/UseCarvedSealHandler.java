package net.server.channel.handlers.inventory;

import java.util.LinkedList;
import java.util.List;

import client.MapleClient;
import client.character.MapleCharacter;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.InventoryHandler;
import net.packet.CField;
import net.packet.CWvsContext;
import net.packet.CWvsContext.InventoryPacket;
import net.packet.field.UserPacket;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.StructItemOption;
import tools.data.LittleEndianAccessor;

public class UseCarvedSealHandler extends AbstractMaplePacketHandler {

	public UseCarvedSealHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		//slea: [90 64 C8 14] [04 00] [0F 00]
        c.getCharacter().updateTick(lea.readInt());
        final short seal = lea.readShort();
        final short equip = lea.readShort();
        final Item toUse = c.getCharacter().getInventory(MapleInventoryType.USE).getItem(seal);
        final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem(equip);
        if (toUse.getItemId() / 100 != 20495
                || MapleItemInformationProvider.getInventoryType(item.getItemId()) != MapleInventoryType.EQUIP
                || MapleItemInformationProvider.getInstance().getEquipStats(toUse.getItemId()).containsKey("success")) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        final Integer success = MapleItemInformationProvider.getInstance().getEquipStats(toUse.getItemId()).get("success");
        if (success == null || Randomizer.nextInt(100) <= success) {
            if (item != null) {
                final Equip eq = (Equip) item;
                if (eq.getState() < 17) {
                    c.getCharacter().dropMessage(5, "This item's Potential cannot be reset.");
                    return;
                }
                if (eq.getBonusPotential3() != 0) {
                    c.getCharacter().dropMessage(5, "Cannot be used on this item.");
                    return;
                }
                int lines = 2; // default
                if (eq.getBonusPotential2() != 0) {
                    lines++;
                }
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final List<List<StructItemOption>> pots = new LinkedList<>(ii.getAllPotentialInfo().values());
                final int reqLevel = ii.getReqLevel(eq.getItemId()) / 10;
                int new_state = Math.abs(eq.getBonusPotential1());
                if (new_state > 20 || new_state < 17) { // incase overflow
                    new_state = 17;
                }
                while (eq.getBonusState() != new_state) {
                    //31001 = haste, 31002 = door, 31003 = se, 31004 = hb, 41005 = combat orders, 41006 = advanced blessing, 41007 = speed infusion
                    for (int i = 0; i < lines; i++) { // minimum 2 lines, max 3
                        boolean rewarded = false;
                        while (!rewarded) {
                            StructItemOption pot = pots.get(Randomizer.nextInt(pots.size())).get(reqLevel);
                            if (pot != null && pot.reqLevel / 10 <= reqLevel && GameConstants.optionTypeFits(pot.optionType, eq.getItemId()) && GameConstants.potentialIDFits(pot.opID, new_state, i)) { //optionType
                                if (InventoryHandler.isAllowedPotentialStat(eq, pot.opID)) {
                                    if (i == 0) {
                                        eq.setBonusPotential1(pot.opID);
                                    } else if (i == 1) {
                                        eq.setBonusPotential2(pot.opID);
                                    } else if (i == 2) {
                                        eq.setBonusPotential3(pot.opID);
                                    }
                                    rewarded = true;
                                }
                            }
                        }
                    }
                }
                c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), true, toUse.getItemId()));
                c.getSession().write(InventoryPacket.scrolledItem(toUse, MapleInventoryType.EQUIP, item, false, true, false));
                c.getCharacter().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
            }
        } else {
            c.getCharacter().getMap().broadcastMessage(UserPacket.showPotentialReset(c.getCharacter().getID(), false, toUse.getItemId()));
        }
        c.getSession().write(CWvsContext.enableActions());
	}

}
