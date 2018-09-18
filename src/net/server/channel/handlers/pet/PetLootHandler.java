package net.server.channel.handlers.pet;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import client.MapleClient;
import client.anticheat.CheatingOffense;
import client.character.MapleCharacter;
import client.inventory.MaplePet;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.InventoryHandler;
import net.packet.CWvsContext;
import net.packet.CWvsContext.InventoryPacket;
import net.world.MaplePartyCharacter;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.life.MapleMonster;
import server.maps.MapleMapItem;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.data.LittleEndianAccessor;

public class PetLootHandler extends AbstractMaplePacketHandler {

	public PetLootHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		if (chr == null) {
            return;
        }
        //System.out.println("PETS: " + slea.toString());
        c.getCharacter().setScrolledPosition((short) 0);
        final byte petz = (byte) c.getCharacter().getPetIndex((int) lea.readLong());
        final MaplePet pet = chr.getPet(petz);
        lea.skip(1); // [4] Zero, [4] Seems to be tickcount, [1] Always zero
        chr.updateTick(lea.readInt());
        final Point Client_Reportedpos = lea.readPos();
        final MapleMapObject ob = chr.getMap().getMapObject(lea.readInt(), MapleMapObjectType.ITEM);

        if (ob == null || pet == null) {
            //System.out.println("Ob or pet is null");
            return;
        }
        final MapleMapItem mapitem = (MapleMapItem) ob;
        final Lock lock = mapitem.getLock();
        lock.lock();
        try {
            if (mapitem.isPickedUp()) {
                c.getSession().write(InventoryPacket.getInventoryFull());
//                System.err.println("Return 1");
                return;
            }
            if (mapitem.getOwner() != chr.getID() && mapitem.isPlayerDrop()) {
//                System.err.println("Return 2");
                return;
            }
            if (mapitem.getOwner() != chr.getID() && ((!mapitem.isPlayerDrop() && mapitem.getDropType() == 0) || (mapitem.isPlayerDrop() && chr.getMap().getEverlast()))) {
//                System.err.println("Return 3");
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            if (!mapitem.isPlayerDrop() && mapitem.getDropType() == 1 && mapitem.getOwner() != chr.getID() && (chr.getParty() == null || chr.getParty().getMemberById(mapitem.getOwner()) == null)) {
//                System.err.println("Return 4");
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            final double Distance = Client_Reportedpos.distanceSq(mapitem.getPosition());
            if (Distance > 10000 && (mapitem.getMeso() > 0 || mapitem.getItemId() != 4001025)) {
                chr.getCheatTracker().registerOffense(CheatingOffense.PET_ITEMVAC_CLIENT, String.valueOf(Distance));
            } else if (pet.getPos().distanceSq(mapitem.getPosition()) > 640000.0) {
                chr.getCheatTracker().registerOffense(CheatingOffense.PET_ITEMVAC_SERVER);

            }
//            System.err.println("Petdrop 5");
            if (mapitem.getMeso() > 0) {
                if (chr.getParty() != null && mapitem.getOwner() != chr.getID()) {
                    final List<MapleCharacter> toGive = new LinkedList<>();
                    final int splitMeso = mapitem.getMeso() * 40 / 100;
                    for (MaplePartyCharacter z : chr.getParty().getMembers()) {
                        MapleCharacter m = chr.getMap().getCharacterById(z.getId());
                        if (m != null && m.getID() != chr.getID()) {
                            toGive.add(m);
                        }
                    }
                    for (final MapleCharacter m : toGive) {
                        m.gainMeso(splitMeso / toGive.size(), true);
                    }
                    chr.gainMeso(mapitem.getMeso() - splitMeso, true);
                } else {
                    chr.gainMeso(mapitem.getMeso(), true);
                }
//                System.err.println("Return 8");
                InventoryHandler.removeItem_Pet(chr, mapitem, petz);
            } else {
                if (MapleItemInformationProvider.getInstance().isPickupBlocked(mapitem.getItemId()) || mapitem.getItemId() / 10000 == 291) {
                    c.getSession().write(CWvsContext.enableActions());
                } else if (InventoryHandler.useItem(c, mapitem.getItemId())) {
                	InventoryHandler.removeItem_Pet(chr, mapitem, petz);
                } else if (MapleInventoryManipulator.checkSpace(c, mapitem.getItemId(), mapitem.getItem().getQuantity(), mapitem.getItem().getOwner())) {
                    if (mapitem.getItem().getQuantity() >= 50 && mapitem.getItemId() == 2340000) {
                        c.setMonitored(true); //hack check
                    }
//                    System.err.println("Return 12");
                    MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true, mapitem.getDropper() instanceof MapleMonster);
                    InventoryHandler.removeItem_Pet(chr, mapitem, petz);
                }
            }
        } finally {
            lock.unlock();
        }
	}

}
