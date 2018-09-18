package net.server.channel.handlers.inventory;

import client.MapleClient;
import client.character.MapleCharacter;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.CWvsContext;
import server.MapleItemInformationProvider;
import server.Randomizer;
import tools.data.LittleEndianAccessor;

public class UseAbyssScrollHandler extends AbstractMaplePacketHandler {

	public UseAbyssScrollHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		c.getCharacter().updateTick(lea.readInt());
        final byte scroll = (byte) lea.readShort();
        final byte equip = (byte) lea.readShort();
        lea.readByte(); //idk
        final Item toUse = c.getCharacter().getInventory(MapleInventoryType.USE).getItem(scroll);
        final Item item = c.getCharacter().getInventory(MapleInventoryType.EQUIP).getItem(equip);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (toUse.getItemId() / 100 != 20485 && toUse.getItemId() / 100 != 20486
                || !GameConstants.isEquip(item.getItemId())
                || !ii.getEquipStats(toUse.getItemId()).containsKey("success")
                || !ii.getEquipStats(item.getItemId()).containsKey("reqLevel")) {
            System.out.println("error1 abyss scroll " + toUse.getItemId());
            c.getSession().write(CField.enchantResult(0));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (item == null) {
            c.getSession().write(CField.enchantResult(0));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        final Integer success = ii.getEquipStats(toUse.getItemId()).get("success");
        if (success == null || Randomizer.nextInt(100) <= success) {
            final Equip eq = (Equip) item;
            if (toUse.getItemId() / 100 == 20485) {
                if (eq.getYggdrasilWisdom() > 0) {
                    System.out.println("error2 abyss scroll " + toUse.getItemId());
                    c.getSession().write(CField.enchantResult(0));
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                int minLevel = 0;
                int maxLevel = 0;
                if (eq.getItemId() >= 2048500 && eq.getItemId() < 2048504) {
                    minLevel = 120;
                    maxLevel = 200;
                } else if (eq.getItemId() >= 2048504 && eq.getItemId() < 2048508) {
                    minLevel = 70;
                    maxLevel = 120;
                }
                int level = ii.getEquipStats(eq.getItemId()).get("reqLevel");
                if (level < minLevel || level > maxLevel) {
                    System.out.println("error3 abyss scroll " + toUse.getItemId());
                    c.getSession().write(CField.enchantResult(0));
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                int stat = (eq.getItemId() % 10) + 1;
                if (stat > 4) {
                    stat -= 4;
                }
                eq.setYggdrasilWisdom((byte) stat);
                if (stat == 1) {
                    eq.setStr((short) (eq.getStr() + 3));
                } else if (stat == 2) {
                    eq.setDex((short) (eq.getDex() + 3));
                } else if (stat == 3) {
                    eq.setInt((short) (eq.getInt() + 3));
                } else if (stat == 4) {
                    eq.setLuk((short) (eq.getLuk() + 3));
                }
            } else if (toUse.getItemId() / 100 == 20486) {
                eq.setFinalStrike(true);
            }
            c.getSession().write(CField.enchantResult(1));
        } else {
            c.getSession().write(CField.enchantResult(0));
        }
        c.getSession().write(CWvsContext.enableActions());
	}

}
