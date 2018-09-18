package net.server.channel.handlers;

import client.MapleClient;
import client.character.MapleCharacter;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import server.shops.MapleShop;
import tools.data.LittleEndianAccessor;

public class NPCShopHandler extends AbstractMaplePacketHandler {

	public NPCShopHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, MapleCharacter chr) {
		byte bmode = lea.readByte();
		
        if (chr == null) {
            return;
        }

        switch (bmode) {
            case 0: {
                MapleShop shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                short slot = lea.readShort();
                slot++;
                int itemId = lea.readInt();
                short quantity = lea.readShort();
                // int unitprice = slea.readInt();
                shop.buy(c, slot, itemId, quantity);
                break;
            }
            case 1: {
                MapleShop shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                byte slot = (byte) lea.readShort();
                int itemId = lea.readInt();
                short quantity = lea.readShort();
                shop.sell(c, GameConstants.getInventoryType(itemId), slot, quantity);
                break;
            }
            case 2: {
                MapleShop shop = chr.getShop();
                if (shop == null) {
                    return;
                }
                byte slot = (byte) lea.readShort();
                shop.recharge(c, slot);
                break;
            }
            default:
                chr.setConversation(0);
        }
	}

}
