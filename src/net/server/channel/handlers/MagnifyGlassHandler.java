package net.server.channel.handlers;

import java.util.LinkedList;
import java.util.List;

import client.MapleClient;
import client.MapleTrait.MapleTraitType;
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
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.StructItemOption;
import tools.data.LittleEndianAccessor;

public class MagnifyGlassHandler extends AbstractMaplePacketHandler {

	public MagnifyGlassHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		c.getCharacter().updateTick(lea.readInt());
        c.getCharacter().setScrolledPosition((short) 0);
        final byte src = (byte) lea.readShort();
        final boolean insight = src == 127 && c.getCharacter().getTrait(MapleTraitType.sense).getLevel() >= 30;
        final Item magnify = c.getCharacter().getInventory(MapleInventoryType.USE).getItem(src);
        byte eqSlot = (byte) lea.readShort();
        boolean equipped = eqSlot < 0;
        final Item toReveal = c.getCharacter().getInventory(equipped ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP).getItem(eqSlot);
        if (toReveal == null || c.getCharacter().hasBlockedInventory()) {
            c.getSession().write(InventoryPacket.getInventoryFull());
            System.out.println("Return 1");
            return;
        }
        final Equip eqq = (Equip) toReveal;
        final long price = GameConstants.getMagnifyPrice(eqq);
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final int reqLevel = ii.getReqLevel(eqq.getItemId()) / 10;
        if (eqq.getState() == 1 && (src == 0x7F && price != -1 && c.getCharacter().getMeso() >= price
                || insight || magnify.getItemId() == 2460003 || (magnify.getItemId() == 2460002 && reqLevel <= 12)
                || (magnify.getItemId() == 2460001 && reqLevel <= 7) || (magnify.getItemId() == 2460000 && reqLevel <= 3))) {
            final List<List<StructItemOption>> pots = new LinkedList<>(ii.getAllPotentialInfo().values());
            int lockedLine = 0;
            int locked = 0;
            if (Math.abs(eqq.getPotential1()) / 100000 > 0) {
                lockedLine = 1;
                locked = Math.abs(eqq.getPotential1());
            } else if (Math.abs(eqq.getPotential2()) / 100000 > 0) {
                lockedLine = 2;
                locked = Math.abs(eqq.getPotential2());
            } else if (Math.abs(eqq.getPotential3()) / 100000 > 0) {
                lockedLine = 3;
                locked = Math.abs(eqq.getPotential3());
            }
            int new_state = Math.abs(eqq.getPotential1());
            if (lockedLine == 1) {
                new_state = locked / 10000 < 1 ? 17 : 16 + locked / 10000;
            }
            if (new_state > 20 || new_state < 17) { // incase overflow
                new_state = 17;
            }
            int lines = 2; // default
            if (eqq.getPotential2() != 0) {
                lines++;
            }
            while (eqq.getState() != new_state) {
                //31001 = haste, 31002 = door, 31003 = se, 31004 = hb, 41005 = combat orders, 41006 = advanced blessing, 41007 = speed infusion
                for (int i = 0; i < lines; i++) { // minimum 2 lines, max 5
                    boolean rewarded = false;
                    while (!rewarded) {
                        StructItemOption pot = pots.get(Randomizer.nextInt(pots.size())).get(reqLevel);
                        if (pot != null && pot.reqLevel / 1 <= reqLevel && GameConstants.optionTypeFits(pot.optionType, eqq.getItemId()) && GameConstants.potentialIDFits(pot.opID, new_state, i)) { //optionType
                            //have to research optionType before making this truely official-like
                            if (InventoryHandler.isAllowedPotentialStat(eqq, pot.opID)) {
                                if (i == 0) {
                                    eqq.setPotential1(pot.opID);
                                } else if (i == 1) {
                                    eqq.setPotential2(pot.opID);
                                } else if (i == 2) {
                                    eqq.setPotential3(pot.opID);
                                } else if (i == 3) {
                                    eqq.setPotential4(pot.opID);
                                }
                                rewarded = true;
                            }
                        }
                    }
                }
            }
            switch (lockedLine) {
                case 1:
                    eqq.setPotential1(Math.abs(locked - lockedLine * 100000));
                    break;
                case 2:
                    eqq.setPotential2(Math.abs(locked - lockedLine * 100000));
                    break;
                case 3:
                    eqq.setPotential3(Math.abs(locked - lockedLine * 100000));
                    break;
            }
            c.getCharacter().getTrait(MapleTraitType.insight).addExp((src == 0x7F && price != -1 ? 10 : insight ? 10 : ((magnify.getItemId() + 2) - 2460000)) * 2, c.getCharacter());
            c.getCharacter().getMap().broadcastMessage(UserPacket.showMagnifyingEffect(c.getCharacter().getID(), eqq.getPosition()));
            if (!insight && src != 0x7F) {
                c.getSession().write(InventoryPacket.scrolledItem(magnify, equipped ? MapleInventoryType.EQUIPPED : MapleInventoryType.EQUIP, toReveal, false, true, equipped));
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, magnify.getPosition(), (short) 1, false);
                System.out.println("Return 2");
            } else {
                if (price != -1 && !insight) {
                    c.getCharacter().gainMeso(-price, false);
                }
                c.getCharacter().forceReAddItem(toReveal, eqSlot >= 0 ? MapleInventoryType.EQUIP : MapleInventoryType.EQUIPPED);
                System.out.println("Return 3");
            }
            c.getSession().write(CWvsContext.enableActions());
        } else {
            c.getSession().write(InventoryPacket.getInventoryFull());
            System.out.println("Return 4");
        }
	}

}
