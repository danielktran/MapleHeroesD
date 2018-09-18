package net.server.channel.handlers;

import java.util.Arrays;

import client.MapleClient;
import client.character.MapleCharacter;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.Interaction;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.channel.handler.HiredMerchantHandler;
import net.packet.CWvsContext;
import net.packet.PlayerShopPacket;
import net.world.World;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleTrade;
import server.maps.FieldLimitType;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.stores.HiredMerchant;
import server.stores.IMaplePlayerShop;
import server.stores.MapleMiniGame;
import server.stores.MaplePlayerShop;
import server.stores.MaplePlayerShopItem;
import tools.data.LittleEndianAccessor;

public class PlayerInteractionHandler extends AbstractMaplePacketHandler {

	public PlayerInteractionHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(final LittleEndianAccessor lea, final MapleClient c, final MapleCharacter chr) {
		final Interaction action = Interaction.getByAction(lea.readByte());
        if (chr == null || action == null) {
            return;
        }
        c.getCharacter().setScrolledPosition((short) 0);
        //System.err.println(action);
        switch (action) { // Mode
            case CREATE: {
                if (chr.getPlayerShop() != null || c.getChannelServer().isShutdown() || chr.hasBlockedInventory()) {
                    System.err.println("amq");
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                final byte createType = lea.readByte();
                if (createType == 4) { // trade//was 3
                    MapleTrade.startTrade(chr);
                } else if (createType == 1 || createType == 2 || createType == 3 || createType == 5 || createType == 6) { // shop
                    //if (createType == 4 && !chr.isIntern()) { //not hired merch... blocked playershop
                    //    c.getSession().write(CWvsContext.enableActions());
                    //    return;
                    //}
                    if (!chr.getMap().getMapObjectsInRange(chr.getTruePosition(), 20000, Arrays.asList(MapleMapObjectType.SHOP, MapleMapObjectType.HIRED_MERCHANT)).isEmpty() || !chr.getMap().getPortalsInRange(chr.getTruePosition(), 20000).isEmpty()) {
                        chr.dropMessage(1, "You may not establish a store here.");
                        c.getSession().write(CWvsContext.enableActions());
                        return;
                    } else if (createType == 1 || createType == 2) {
                        if (FieldLimitType.Minigames.check(chr.getMap().getFieldLimit()) || chr.getMap().allowPersonalShop()) {
                            chr.dropMessage(1, "You may not use minigames here.");
                            c.getSession().write(CWvsContext.enableActions());
                            return;
                        }
                    }
                    final String desc = lea.readMapleAsciiString();
                    String pass = "";
                    if (lea.readByte() > 0) {
                        pass = lea.readMapleAsciiString();
                    }
                    if (createType == 1 || createType == 2) {
                        final int piece = lea.readByte();
                        final int itemId = createType == 1 ? (4080000 + piece) : 4080100;
                        if (!chr.haveItem(itemId) || (c.getCharacter().getMapId() >= 910000001 && c.getCharacter().getMapId() <= 910000022)) {
                            return;
                        }
                        MapleMiniGame game = new MapleMiniGame(chr, itemId, desc, pass, createType); //itemid
                        game.setPieceType(piece);
                        chr.setPlayerShop(game);
                        game.setAvailable(true);
                        game.setOpen(true);
                        game.send(c);
                        chr.getMap().addMapObject(game);
                        game.update();
                    } else if (chr.getMap().allowPersonalShop()) {
                        Item shop = c.getCharacter().getInventory(MapleInventoryType.CASH).getItem((byte) lea.readShort());
                        if (shop == null || shop.getQuantity() <= 0 || shop.getItemId() != lea.readInt() || c.getCharacter().getMapId() < 910000001 || c.getCharacter().getMapId() > 910000022) {
                            return;
                        }
                        if (createType == 3) {
                            //MaplePlayerShop mps = new MaplePlayerShop(chr, shop.getItemId(), desc);
                            //chr.setPlayerShop(mps);
                            //chr.getMap().addMapObject(mps);
                            //c.getSession().write(PlayerShopPacket.getPlayerStore(chr, true));
                        } else if (HiredMerchantHandler.UseHiredMerchant(chr.getClient(), false)) {
                            final HiredMerchant merch = new HiredMerchant(chr, shop.getItemId(), desc);
                            chr.setPlayerShop(merch);
                            chr.getMap().addMapObject(merch);
                            c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merch, true));
                        }
                    }
                }
                break;
            }
            case INVITE_TRADE: {
                if (chr.getMap() == null) {
                    return;
                }
                MapleCharacter chrr = chr.getMap().getCharacterById(lea.readInt());
                if (chrr == null || c.getChannelServer().isShutdown() || chrr.hasBlockedInventory()) {
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                MapleTrade.inviteTrade(chr, chrr);
                break;
            }
            case DENY_TRADE: {
                MapleTrade.declineTrade(chr);
                break;
            }
            case VISIT: {
                if (c.getChannelServer().isShutdown()) {
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                if (chr.getTrade() != null && chr.getTrade().getPartner() != null && !chr.getTrade().inTrade()) {
                    MapleTrade.visitTrade(chr, chr.getTrade().getPartner().getChr());
                } else if (chr.getMap() != null && chr.getTrade() == null) {
                    final int obid = lea.readInt();
                    MapleMapObject ob = chr.getMap().getMapObject(obid, MapleMapObjectType.HIRED_MERCHANT);
                    if (ob == null) {
                        ob = chr.getMap().getMapObject(obid, MapleMapObjectType.SHOP);
                    }

                    if (ob instanceof IMaplePlayerShop && chr.getPlayerShop() == null) {
                        final IMaplePlayerShop ips = (IMaplePlayerShop) ob;

                        if (ob instanceof HiredMerchant) {
                            final HiredMerchant merchant = (HiredMerchant) ips;
                            /*if (merchant.isOwner(chr) && merchant.isOpen() && merchant.isAvailable()) {
                             merchant.setOpen(false);
                             merchant.removeAllVisitors((byte) 16, (byte) 0);
                             chr.setPlayerShop(ips);
                             c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merchant, false));
                             } else {*/
                            if (!merchant.isOpen() || !merchant.isAvailable()) {
                                chr.dropMessage(1, "This shop is in maintenance, please come by later.");
                            } else {
                                if (ips.getFreeSlot() == -1) {
                                    chr.dropMessage(1, "This shop has reached it's maximum capacity, please come by later.");
                                } else if (merchant.isInBlackList(chr.getName())) {
                                    chr.dropMessage(1, "You have been banned from this store.");
                                } else {
                                    chr.setPlayerShop(ips);
                                    merchant.addVisitor(chr);
                                    c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merchant, false));
                                }
                            }
                            //}
                        } else {
                            if (ips instanceof MaplePlayerShop && ((MaplePlayerShop) ips).isBanned(chr.getName())) {
                                chr.dropMessage(1, "You have been banned from this store.");
                            } else {
                                if (ips.getFreeSlot() < 0 || ips.getVisitorSlot(chr) > -1 || !ips.isOpen() || !ips.isAvailable()) {
                                    c.getSession().write(PlayerShopPacket.getMiniGameFull());
                                } else {
                                    if (lea.available() > 0 && lea.readByte() > 0) { //a password has been entered
                                        String pass = lea.readMapleAsciiString();
                                        if (!pass.equals(ips.getPassword())) {
                                            c.getCharacter().dropMessage(1, "The password you entered is incorrect.");
                                            return;
                                        }
                                    } else if (ips.getPassword().length() > 0) {
                                        c.getCharacter().dropMessage(1, "The password you entered is incorrect.");
                                        return;
                                    }
                                    chr.setPlayerShop(ips);
                                    ips.addVisitor(chr);
                                    if (ips instanceof MapleMiniGame) {
                                        ((MapleMiniGame) ips).send(c);
                                    } else {
                                        c.getSession().write(PlayerShopPacket.getPlayerStore(chr, false));
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }
            case HIRED_MERCHANT_MAINTENANCE: {
                if (c.getChannelServer().isShutdown() || chr.getMap() == null || chr.getTrade() != null) {
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                lea.skip(1); // 9?
                byte type = lea.readByte(); // 6?
                if (type != 6) {
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                final String password = lea.readMapleAsciiString();
                if (!c.CheckSecondPassword(password) || password.length() < 6 || password.length() > 16) {
                	chr.dropMessage(5, "Please enter a valid PIC.");
                	c.getSession().write(CWvsContext.enableActions());
                	return;
                }				
                final int obid = lea.readInt();
                MapleMapObject ob = chr.getMap().getMapObject(obid, MapleMapObjectType.HIRED_MERCHANT);
                if (ob == null || chr.getPlayerShop() != null) {
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                if (ob instanceof IMaplePlayerShop && ob instanceof HiredMerchant) {
                    final IMaplePlayerShop ips = (IMaplePlayerShop) ob;
                    final HiredMerchant merchant = (HiredMerchant) ips;
                    if (merchant.isOwner(chr) && merchant.isAvailable()) {
                        merchant.setOpen(false);
                        merchant.removeAllVisitors((byte) 16, (byte) 0);
                        chr.setPlayerShop(ips);
                        c.getSession().write(PlayerShopPacket.getHiredMerch(chr, merchant, false));
                    } else {
                        c.getSession().write(CWvsContext.enableActions());
                    }
                }
                break;
            }
            case CHAT: {
                chr.updateTick(lea.readInt());
                final String message = lea.readMapleAsciiString();
                if (chr.getTrade() != null) {
                	try {
                		chr.getTrade().chat(message);
                	} catch (Exception ex) {
                		
                	}
                } else if (chr.getPlayerShop() != null) {
                    final IMaplePlayerShop ips = chr.getPlayerShop();
                    ips.broadcastToVisitors(PlayerShopPacket.shopChat(chr.getName() + " : " + message, ips.getVisitorSlot(chr)));
                    if (chr.getClient().isMonitored()) { //Broadcast info even if it was a command.
                        World.Broadcast.broadcastGMMessage(CWvsContext.broadcastMsg(6, chr.getName() + " said in " + ips.getOwnerName() + " shop : " + message));
                    }
                }
                break;
            }
            case EXIT: {
                if (chr.getTrade() != null) {
                    MapleTrade.cancelTrade(chr.getTrade(), chr.getClient(), chr);
                } else {
                    final IMaplePlayerShop ips = chr.getPlayerShop();
                    if (ips == null) { //should be null anyway for owners of hired merchants (maintenance_off)
                        return;
                    }
                    if (ips.isOwner(chr) && ips.getShopType() != 1) {
                        ips.closeShop(false, ips.isAvailable()); //how to return the items?
                    } else {
                        ips.removeVisitor(chr);
                    }
                    chr.setPlayerShop(null);
                }
                break;
            }
            case OPEN3:
            case OPEN2:
            case OPEN1: {
                // c.getPlayer().haveItem(mode, 1, false, true)
                final IMaplePlayerShop shop = chr.getPlayerShop();
                if (shop != null && shop.isOwner(chr) && shop.getShopType() < 3 && !shop.isAvailable()) {
                    if (chr.getMap().allowPersonalShop()) {
                        if (c.getChannelServer().isShutdown()) {
                            chr.dropMessage(1, "The server is about to shut down.");
                            c.getSession().write(CWvsContext.enableActions());
                            shop.closeShop(shop.getShopType() == 1, false);
                            return;
                        }

                        if (shop.getShopType() == 1 && HiredMerchantHandler.UseHiredMerchant(chr.getClient(), false)) {
                            final HiredMerchant merchant = (HiredMerchant) shop;
                            merchant.setStoreid(c.getChannelServer().addMerchant(merchant));
                            merchant.setOpen(true);
                            merchant.setAvailable(true);
                            chr.getMap().broadcastMessage(PlayerShopPacket.spawnHiredMerchant(merchant));
                            chr.setPlayerShop(null);

                        } else if (shop.getShopType() == 2) {
                            shop.setOpen(true);
                            shop.setAvailable(true);
                            shop.update();
                        }
                    } else {
                        c.getSession().close();
                    }
                }

                break;
            }
            case SET_ITEMS4:
            case SET_ITEMS3:
            case SET_ITEMS2:
            case SET_ITEMS: {
//            case SET_ITEMS: {
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final MapleInventoryType ivType = MapleInventoryType.getByType(lea.readByte());
                final Item item = chr.getInventory(ivType).getItem((byte) lea.readShort());
                final short quantity = lea.readShort();
                final byte targetSlot = lea.readByte();

                if (chr.getTrade() != null && item != null) {
                    if ((quantity <= item.getQuantity() && quantity >= 0) || GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
                        chr.getTrade().setItems(c, item, targetSlot, quantity);
                    }
                }
                break;
            }
//            case UPDATE_MESO:
//            case SET_MESO:
//            case SET_MESO_1:
//            case SET_MESO_2: {
            case SET_MESO4:
            case SET_MESO3:
            case SET_MESO2:
            case UPDATE_MESO: {
                final MapleTrade trade = chr.getTrade();
                if (trade != null) {
                    trade.setMeso((int) lea.readLong());
                }
                break;
            }
//            case PLAYER_SHOP_ADD_ITEM:
//            case ADD_ITEM: {
            case ADD_ITEM4:
            case ADD_ITEM3:
            case ADD_ITEM2:
            case ADD_ITEM1: {
                final MapleInventoryType type = MapleInventoryType.getByType(lea.readByte());
                final byte slot = (byte) lea.readShort();
                final short bundles = lea.readShort(); // How many in a bundle
                final short perBundle = lea.readShort(); // Price per bundle
                final int price = lea.readInt();

                if (price <= 0 || bundles <= 0 || perBundle <= 0) {
                    return;
                }
                final IMaplePlayerShop shop = chr.getPlayerShop();

                if (shop == null || !shop.isOwner(chr) || shop instanceof MapleMiniGame) {
                    return;
                }
                final Item ivItem = chr.getInventory(type).getItem(slot);
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                if (ivItem != null) {
                    long check = bundles * perBundle;
                    if (check > 32767 || check <= 0) { //This is the better way to check.
                        return;
                    }
                    final short bundles_perbundle = (short) (bundles * perBundle);
//                    if (bundles_perbundle < 0) { // int_16 overflow
//                        return;
//                    }
                    if (ivItem.getQuantity() >= bundles_perbundle) {
                        final short flag = ivItem.getFlag();
                        if (ItemFlag.UNTRADABLE.check(flag) || ItemFlag.LOCK.check(flag)) {
                            c.getSession().write(CWvsContext.enableActions());
                            return;
                        }
                        if (ii.isDropRestricted(ivItem.getItemId()) || ii.isAccountShared(ivItem.getItemId())) {
                            if (!(ItemFlag.KARMA_EQ.check(flag) || ItemFlag.KARMA_USE.check(flag))) {
                                c.getSession().write(CWvsContext.enableActions());
                                return;
                            }
                        }
                        if (bundles_perbundle >= 50 && ivItem.getItemId() == 2340000) {
                            c.setMonitored(true); //hack check
                        }
                        if (GameConstants.getLowestPrice(ivItem.getItemId()) > price) {
                            c.getCharacter().dropMessage(1, "The lowest you can sell this for is " + GameConstants.getLowestPrice(ivItem.getItemId()));
                            c.getSession().write(CWvsContext.enableActions());
                            return;
                        }
                        if (GameConstants.isThrowingStar(ivItem.getItemId()) || GameConstants.isBullet(ivItem.getItemId())) {
                            // Ignore the bundles
                            MapleInventoryManipulator.removeFromSlot(c, type, slot, ivItem.getQuantity(), true);

                            final Item sellItem = ivItem.copy();
                            shop.addItem(new MaplePlayerShopItem(sellItem, (short) 1, price));
                        } else {
                            MapleInventoryManipulator.removeFromSlot(c, type, slot, bundles_perbundle, true);

                            final Item sellItem = ivItem.copy();
                            sellItem.setQuantity(perBundle);
                            shop.addItem(new MaplePlayerShopItem(sellItem, bundles, price));
                        }
                        c.getSession().write(PlayerShopPacket.shopItemUpdate(shop));
                    }
                }
                break;
            }
//            case CONFIRM_TRADE:
//            case BUY_ITEM_PLAYER_SHOP:
//            case BUY_ITEM_STORE:
//            case BUY_ITEM_HIREDMERCHANT: { // Buy and Merchant buy
            case CONFIRM_TRADE:
            case CONFIRM_TRADE2:
            case CONFIRM_TRADE_MESO:
            case CONFIRM_TRADE_MESO2:
            case BUY_ITEM_STORE3:
            case BUY_ITEM_STORE2:
            case BUY_ITEM_STORE1:
            case BUY_ITEM_HIREDMERCHANT: { // Buy and Merchant buy
                if (chr.getTrade() != null) {
                    MapleTrade.completeTrade(chr);
                    break;
                }
                final int item = lea.readByte();
                final short quantity = lea.readShort();
                //slea.skip(4);
                final IMaplePlayerShop shop = chr.getPlayerShop();

                if (shop == null || shop.isOwner(chr) || shop instanceof MapleMiniGame || item >= shop.getItems().size()) {
                    return;
                }
                final MaplePlayerShopItem tobuy = shop.getItems().get(item);
                if (tobuy == null) {
                    return;
                }
                long check = tobuy.bundles * quantity;
                long check2 = tobuy.price * quantity;
                long check3 = tobuy.item.getQuantity() * quantity;
                if (check <= 0 || check2 > 9999999999L || check2 <= 0 || check3 > 32767 || check3 < 0) { //This is the better way to check.
                    return;
                }
                if (tobuy.bundles < quantity || (tobuy.bundles % quantity != 0 && GameConstants.isEquip(tobuy.item.getItemId())) // Buying
                        || chr.getMeso() - (check2) < 0 || chr.getMeso() - (check2) > 9999999999L || shop.getMeso() + (check2) < 0 || shop.getMeso() + (check2) > 9999999999L) {
                    return;
                }
                if (quantity >= 50 && tobuy.item.getItemId() == 2340000) {
                    c.setMonitored(true); //hack check
                }
                shop.buy(c, item, quantity);
                shop.broadcastToVisitors(PlayerShopPacket.shopItemUpdate(shop));
                break;
            }
            case REMOVE_ITEM: {
                lea.skip(1); // ?
                int slot = lea.readShort(); //0
                final IMaplePlayerShop shop = chr.getPlayerShop();

                if (shop == null || !shop.isOwner(chr) || shop instanceof MapleMiniGame || shop.getItems().size() <= 0 || shop.getItems().size() <= slot || slot < 0) {
                    return;
                }
                final MaplePlayerShopItem item = shop.getItems().get(slot);

                if (item != null) {
                    if (item.bundles > 0) {
                        Item item_get = item.item.copy();
                        long check = item.bundles * item.item.getQuantity();
                        if (check < 0 || check > 32767) {
                            return;
                        }
                        item_get.setQuantity((short) check);
                        if (item_get.getQuantity() >= 50 && item.item.getItemId() == 2340000) {
                            c.setMonitored(true); //hack check
                        }
                        if (MapleInventoryManipulator.checkSpace(c, item_get.getItemId(), item_get.getQuantity(), item_get.getOwner())) {
                            MapleInventoryManipulator.addFromDrop(c, item_get, false);
                            item.bundles = 0;
                            shop.removeFromSlot(slot);
                        }
                    }
                }
                c.getSession().write(PlayerShopPacket.shopItemUpdate(shop));
                break;
            }
            case MAINTANCE_OFF: {
                final IMaplePlayerShop shop = chr.getPlayerShop();
                if (shop != null && shop instanceof HiredMerchant && shop.isOwner(chr) && shop.isAvailable()) {
                    shop.setOpen(true);
                    shop.removeAllVisitors(-1, -1);
                }
                break;
            }
            case MAINTANCE_ORGANISE: {
                final IMaplePlayerShop imps = chr.getPlayerShop();
                if (imps != null && imps.isOwner(chr) && !(imps instanceof MapleMiniGame)) {
                    for (int i = 0; i < imps.getItems().size(); i++) {
                        if (imps.getItems().get(i).bundles == 0) {
                            imps.getItems().remove(i);
                        }
                    }
                    if (chr.getMeso() + imps.getMeso() > 0) {
                        chr.gainMeso(imps.getMeso(), false);
                        imps.setMeso(0);
                    }
                    c.getSession().write(PlayerShopPacket.shopItemUpdate(imps));
                }
                break;
            }
            case CLOSE_MERCHANT: {
                final IMaplePlayerShop merchant = chr.getPlayerShop();
                if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr) && merchant.isAvailable()) {
                    merchant.removeVisitor(chr);
                    merchant.removeAllVisitors(-1, -1);
                    chr.setPlayerShop(null);
                    merchant.closeShop(true, true);
                    c.getSession().write(CWvsContext.broadcastMsg(1, "Please visit Fredrick for your items."));
                    c.getSession().write(CWvsContext.enableActions());
                }
                break;
            }
            case ADMIN_STORE_NAMECHANGE: { // Changing store name, only Admin
                // 01 00 00 00
                break;
            }
            case VIEW_MERCHANT_VISITOR: {
                final IMaplePlayerShop merchant = chr.getPlayerShop();
                if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
                    ((HiredMerchant) merchant).sendVisitor(c);
                }
                break;
            }
            case VIEW_MERCHANT_BLACKLIST: {
                final IMaplePlayerShop merchant = chr.getPlayerShop();
                if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
                    ((HiredMerchant) merchant).sendBlackList(c);
                }
                break;
            }
            case MERCHANT_BLACKLIST_ADD: {
                final IMaplePlayerShop merchant = chr.getPlayerShop();
                if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
                    ((HiredMerchant) merchant).addBlackList(lea.readMapleAsciiString());
                }
                break;
            }
            case MERCHANT_BLACKLIST_REMOVE: {
                final IMaplePlayerShop merchant = chr.getPlayerShop();
                if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr)) {
                    ((HiredMerchant) merchant).removeBlackList(lea.readMapleAsciiString());
                }
                break;
            }
            case GIVE_UP: {
                final IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame) {
                    MapleMiniGame game = (MapleMiniGame) ips;
                    if (game.isOpen()) {
                        break;
                    }
                    game.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game, 0, game.getVisitorSlot(chr)));
                    game.nextLoser();
                    game.setOpen(true);
                    game.update();
                    game.checkExitAfterGame();
                }
                break;
            }
//            case EXPEL: {
//                final IMaplePlayerShop ips = chr.getPlayerShop();
//                if (ips != null && ips instanceof MapleMiniGame) {
//                    if (!((MapleMiniGame) ips).isOpen()) {
//                        break;
//                    }
//                    ips.removeAllVisitors(3, 1); //no msg
//                }
//                break;
//            }
//            case READY:
//            case UN_READY: {
//                final IMaplePlayerShop ips = chr.getPlayerShop();
//                if (ips != null && ips instanceof MapleMiniGame) {
//                    MapleMiniGame game = (MapleMiniGame) ips;
//                    if (!game.isOwner(chr) && game.isOpen()) {
//                        game.setReady(game.getVisitorSlot(chr));
//                        game.broadcastToVisitors(PlayerShopPacket.getMiniGameReady(game.isReady(game.getVisitorSlot(chr))));
//                    }
//                }
//                break;
//            }
            case START: {
                final IMaplePlayerShop ips = chr.getPlayerShop();
                if (ips != null && ips instanceof MapleMiniGame) {
                    MapleMiniGame game = (MapleMiniGame) ips;
                    if (game.isOwner(chr) && game.isOpen()) {
                        for (int i = 1; i < ips.getSize(); i++) {
                            if (!game.isReady(i)) {
                                return;
                            }
                        }
                        game.setGameType();
                        game.shuffleList();
                        if (game.getGameType() == 1) {
                            game.broadcastToVisitors(PlayerShopPacket.getMiniGameStart(game.getLoser()));
                        } else {
                            game.broadcastToVisitors(PlayerShopPacket.getMatchCardStart(game, game.getLoser()));
                        }
                        game.setOpen(false);
                        game.update();
                    }
                }
                break;
            }
//            case REQUEST_TIE: {
//                final IMaplePlayerShop ips = chr.getPlayerShop();
//                if (ips != null && ips instanceof MapleMiniGame) {
//                    MapleMiniGame game = (MapleMiniGame) ips;
//                    if (game.isOpen()) {
//                        break;
//                    }
//                    if (game.isOwner(chr)) {
//                        game.broadcastToVisitors(PlayerShopPacket.getMiniGameRequestTie(), false);
//                    } else {
//                        game.getMCOwner().getClient().getSession().write(PlayerShopPacket.getMiniGameRequestTie());
//                    }
//                    game.setRequestedTie(game.getVisitorSlot(chr));
//                }
//                break;
//            }
//            case ANSWER_TIE: {
//                final IMaplePlayerShop ips = chr.getPlayerShop();
//                if (ips != null && ips instanceof MapleMiniGame) {
//                    MapleMiniGame game = (MapleMiniGame) ips;
//                    if (game.isOpen()) {
//                        break;
//                    }
//                    if (game.getRequestedTie() > -1 && game.getRequestedTie() != game.getVisitorSlot(chr)) {
//                        if (slea.readByte() > 0) {
//                            game.broadcastToVisitors(PlayerShopPacket.getMiniGameResult(game, 1, game.getRequestedTie()));
//                            game.nextLoser();
//                            game.setOpen(true);
//                            game.update();
//                            game.checkExitAfterGame();
//                        } else {
//                            game.broadcastToVisitors(PlayerShopPacket.getMiniGameDenyTie());
//                        }
//                        game.setRequestedTie(-1);
//                    }
//                }
//                break;
//            }
//            case SKIP: {
//                final IMaplePlayerShop ips = chr.getPlayerShop();
//                if (ips != null && ips instanceof MapleMiniGame) {
//                    MapleMiniGame game = (MapleMiniGame) ips;
//                    if (game.isOpen()) {
//                        break;
//                    }
//                    if (game.getLoser() != ips.getVisitorSlot(chr)) {
//                        ips.broadcastToVisitors(PlayerShopPacket.shopChat("Turn could not be skipped by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + ips.getVisitorSlot(chr), ips.getVisitorSlot(chr)));
//                        return;
//                    }
//                    ips.broadcastToVisitors(PlayerShopPacket.getMiniGameSkip(ips.getVisitorSlot(chr)));
//                    game.nextLoser();
//                }
//                break;
//            }
//            case MOVE_OMOK: {
//                final IMaplePlayerShop ips = chr.getPlayerShop();
//                if (ips != null && ips instanceof MapleMiniGame) {
//                    MapleMiniGame game = (MapleMiniGame) ips;
//                    if (game.isOpen()) {
//                        break;
//                    }
//                    if (game.getLoser() != game.getVisitorSlot(chr)) {
//                        game.broadcastToVisitors(PlayerShopPacket.shopChat("Omok could not be placed by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr), game.getVisitorSlot(chr)));
//                        return;
//                    }
//                    game.setPiece(slea.readInt(), slea.readInt(), slea.readByte(), chr);
//                }
//                break;
//            }
//            case SELECT_CARD: {
//                final IMaplePlayerShop ips = chr.getPlayerShop();
//                if (ips != null && ips instanceof MapleMiniGame) {
//                    MapleMiniGame game = (MapleMiniGame) ips;
//                    if (game.isOpen()) {
//                        break;
//                    }
//                    if (game.getLoser() != game.getVisitorSlot(chr)) {
//                        game.broadcastToVisitors(PlayerShopPacket.shopChat("Card could not be placed by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr), game.getVisitorSlot(chr)));
//                        return;
//                    }
//                    if (slea.readByte() != game.getTurn()) {
//                        game.broadcastToVisitors(PlayerShopPacket.shopChat("Omok could not be placed by " + chr.getName() + ". Loser: " + game.getLoser() + " Visitor: " + game.getVisitorSlot(chr) + " Turn: " + game.getTurn(), game.getVisitorSlot(chr)));
//                        return;
//                    }
//                    final int slot = slea.readByte();
//                    final int turn = game.getTurn();
//                    final int fs = game.getFirstSlot();
//                    if (turn == 1) {
//                        game.setFirstSlot(slot);
//                        if (game.isOwner(chr)) {
//                            game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, turn), false);
//                        } else {
//                            game.getMCOwner().getClient().getSession().write(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, turn));
//                        }
//                        game.setTurn(0); //2nd turn nao
//                        return;
//                    } else if (fs > 0 && game.getCardId(fs + 1) == game.getCardId(slot + 1)) {
//                        game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, game.isOwner(chr) ? 2 : 3));
//                        game.setPoints(game.getVisitorSlot(chr)); //correct.. so still same loser. diff turn tho
//                    } else {
//                        game.broadcastToVisitors(PlayerShopPacket.getMatchCardSelect(turn, slot, fs, game.isOwner(chr) ? 0 : 1));
//                        game.nextLoser();//wrong haha
//
//                    }
//                    game.setTurn(1);
//                    game.setFirstSlot(0);
//
//                }
//                break;
//            }
//            case EXIT_AFTER_GAME:
//            case CANCEL_EXIT: {
//                final IMaplePlayerShop ips = chr.getPlayerShop();
//                if (ips != null && ips instanceof MapleMiniGame) {
//                    MapleMiniGame game = (MapleMiniGame) ips;
//                    if (game.isOpen()) {
//                        break;
//                    }
//                    game.setExitAfter(chr);
//                    game.broadcastToVisitors(PlayerShopPacket.getMiniGameExitAfter(game.isExitAfter(chr)));
//                }
//                break;
//            }
            default: {
                //some idiots try to send huge amounts of data to this (:
                //System.out.println("Unhandled interaction action by " + chr.getName() + " : " + action + ", " + slea.toString());
                //19 (0x13) - 00 OR 01 -> itemid(maple leaf) ? who knows what this is
                break;
            }
        }
	}

}
