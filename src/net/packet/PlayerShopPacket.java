/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.packet;

import client.MapleClient;
import client.character.MapleCharacter;
import client.inventory.Item;
import constants.GameConstants;
import constants.Interaction;
import net.SendPacketOpcode;
import net.world.MapleCharacterLook;

import java.util.List;
import server.MerchItemPackage;
import server.stores.AbstractPlayerStore.BoughtItem;
import server.stores.HiredMerchant;
import server.stores.IMaplePlayerShop;
import server.stores.MapleMiniGame;
import server.stores.MaplePlayerShop;
import server.stores.MaplePlayerShopItem;
import tools.Pair;
import tools.data.MaplePacketWriter;

public class PlayerShopPacket {

    public static byte[] sendTitleBox() {
        return sendTitleBox(7); // SendOpenShopRequest
    }

    public static byte[] sendTitleBox(int mode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SEND_TITLE_BOX);
		mpw.write(mode);
        if ((mode == 8) || (mode == 16)) {
            mpw.writeInt(0);
            mpw.write(0);
        } else if (mode == 13) {
            mpw.writeInt(0);
        } else if (mode == 14) {
            mpw.write(0);
        } else if (mode == 18) {
            mpw.write(1);
            mpw.writeMapleAsciiString("");
        }

        return mpw.getPacket();
    }
    
    public static byte[] requestShopPic(final int oid) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SEND_TITLE_BOX);
		mpw.write(17);
        mpw.writeInt(oid);
        mpw.writeShort(0);
        mpw.writeLong(0L);

        return mpw.getPacket();
    }

    /**
     * 
     * @see CUser::OnMiniRoomBalloon
     */
    public static final byte[] addCharBox(final MapleCharacter c, final int type) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MINI_ROOM_BALLOON);
		mpw.writeInt(c.getID());
        PacketHelper.addAnnounceBox(mpw, c);

        return mpw.getPacket();
    }

    /**
    * @see CUser::OnMiniRoomBalloon
    */
    public static final byte[] removeCharBox(final MapleCharacter c) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MINI_ROOM_BALLOON);
		mpw.writeInt(c.getID());
        mpw.write(0);

        return mpw.getPacket();
    }

    /**
    * @see CUser::OnMiniRoomBalloon
    */
    public static final byte[] sendPlayerShopBox(final MapleCharacter c) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MINI_ROOM_BALLOON);
		mpw.writeInt(c.getID());
        PacketHelper.addAnnounceBox(mpw, c);

        return mpw.getPacket();
    }

    public static byte[] getHiredMerch(MapleCharacter chr, HiredMerchant merch, boolean firstTime) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(20);//was11
        mpw.write(6);
        mpw.write(7);
        mpw.writeShort(merch.getVisitorSlot(chr));
        mpw.writeInt(merch.getItemId());
        mpw.writeMapleAsciiString("Hired Merchant");
        for (Pair storechr : merch.getVisitors()) {
            mpw.write(((Byte) storechr.left).byteValue());
            PacketHelper.addCharLook(mpw, (MapleCharacterLook) storechr.right, false, false);
            mpw.writeMapleAsciiString(((MapleCharacter) storechr.right).getName());
            mpw.writeShort(((MapleCharacter) storechr.right).getJob());
        }
        mpw.write(-1);
        mpw.writeShort(0);
        mpw.writeMapleAsciiString(merch.getOwnerName());
        if (merch.isOwner(chr)) {
            mpw.writeInt(merch.getTimeLeft());
            mpw.write(firstTime ? 1 : 0);
            mpw.write(merch.getBoughtItems().size());
            for (final BoughtItem SoldItem : merch.getBoughtItems()) {
                mpw.writeInt(SoldItem.id);
                mpw.writeShort(SoldItem.quantity);
                mpw.writeLong(SoldItem.totalPrice);
                mpw.writeMapleAsciiString(SoldItem.buyer);
            }
            mpw.writeLong(merch.getMeso());
        }
        mpw.writeInt(263);
        mpw.writeMapleAsciiString(merch.getDescription());
        mpw.write(16);
        mpw.writeLong(merch.getMeso());
        mpw.write(merch.getItems().size());
        for (MaplePlayerShopItem item : merch.getItems()) {
            mpw.writeShort(item.bundles);
            mpw.writeShort(item.item.getQuantity());
            mpw.writeLong(item.price);
            PacketHelper.addItemInfo(mpw, item.item);
        }
        mpw.writeShort(0);

        return mpw.getPacket();
    }

    public static final byte[] getPlayerStore(final MapleCharacter chr, final boolean firstTime) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
        IMaplePlayerShop ips = chr.getPlayerShop();
        mpw.write(GameConstants.GMS ? 11 : 5);
        switch (ips.getShopType()) {
            case 2:
                mpw.write(4);
                mpw.write(4);
                break;
            case 3:
                mpw.write(2);
                mpw.write(2);
                break;
            case 4:
                mpw.write(1);
                mpw.write(2);
                break;
        }
        mpw.writeShort(ips.getVisitorSlot(chr));
        PacketHelper.addCharLook(mpw, ((MaplePlayerShop) ips).getMCOwner(), false, false);
        mpw.writeMapleAsciiString(ips.getOwnerName());
        mpw.writeShort(((MaplePlayerShop) ips).getMCOwner().getJob());
        for (final Pair<Byte, MapleCharacter> storechr : ips.getVisitors()) {
            mpw.write(storechr.left);
            PacketHelper.addCharLook(mpw, storechr.right, false, false);
            mpw.writeMapleAsciiString(storechr.right.getName());
            mpw.writeShort(storechr.right.getJob());
        }
        mpw.write(255);
        mpw.writeMapleAsciiString(ips.getDescription());
        mpw.write(10);
        mpw.write(ips.getItems().size());

        for (final MaplePlayerShopItem item : ips.getItems()) {
            mpw.writeShort(item.bundles);
            mpw.writeShort(item.item.getQuantity());
            mpw.writeInt(item.price);
            PacketHelper.addItemInfo(mpw, item.item);
        }
        return mpw.getPacket();
    }

    public static final byte[] shopChat(final String message, final int slot) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(24);//was15
        mpw.write(25);//was15
        mpw.write(slot);
        mpw.writeMapleAsciiString(message);

        return mpw.getPacket();
    }

    public static final byte[] shopErrorMessage(final int error, final int type) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(GameConstants.GMS ? 28 : 10);//was18
        mpw.write(type);
        mpw.write(error);

        return mpw.getPacket();
    }

    public static final byte[] spawnHiredMerchant(final HiredMerchant hm) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_HIRED_MERCHANT);
		mpw.writeInt(hm.getOwnerId());
        mpw.writeInt(hm.getItemId());
        mpw.writePos(hm.getTruePosition());
        mpw.writeShort(0);
        mpw.writeMapleAsciiString(hm.getOwnerName());
        PacketHelper.addInteraction(mpw, hm);
//        System.err.println(hm.getItemId());
        return mpw.getPacket();
    }

    public static final byte[] destroyHiredMerchant(final int id) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DESTROY_HIRED_MERCHANT);
		mpw.writeInt(id);

        return mpw.getPacket();
    }

    public static final byte[] shopItemUpdate(final IMaplePlayerShop shop) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(77);//was50
        if (shop.getShopType() == 1) {
            mpw.writeLong(0L);
        }
        mpw.write(shop.getItems().size());
        for (final MaplePlayerShopItem item : shop.getItems()) {
            mpw.writeShort(item.bundles);
            mpw.writeShort(item.item.getQuantity());
            mpw.writeLong(item.price);
            PacketHelper.addItemInfo(mpw, item.item);
        }
        mpw.writeShort(0);

        return mpw.getPacket();
    }

    public static final byte[] shopVisitorAdd(final MapleCharacter chr, final int slot) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(Interaction.VISIT.action);
//        mpw.write(19);//was10
        mpw.write(slot);
        PacketHelper.addCharLook(mpw, chr, false, false);
        mpw.writeMapleAsciiString(chr.getName());
        mpw.writeShort(chr.getJob());

        return mpw.getPacket();
    }

    public static final byte[] shopVisitorLeave(final byte slot) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(Interaction.EXIT.action);
        mpw.write(slot);

        return mpw.getPacket();
    } // Fix from RZ

    public static final byte[] Merchant_Buy_Error(final byte message) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);

        // 2 = You have not enough meso
		mpw.write(44);
        mpw.write(message);

        return mpw.getPacket();
    }

    public static final byte[] updateHiredMerchant(final HiredMerchant shop) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_HIRED_MERCHANT);
		mpw.writeInt(shop.getOwnerId());
        PacketHelper.addInteraction(mpw, shop);

        return mpw.getPacket();
    }

    public static final byte[] merchItem_Message(final int op) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MERCH_ITEM_MSG);
		mpw.write(op);

        return mpw.getPacket();
    }

    public static final byte[] merchItemStore(final byte op, final int days, final int fees) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MERCH_ITEM_STORE);

        // 40: This is currently unavailable.\r\nPlease try again later
		mpw.write(op);
        switch (op) {
            case 39:
                mpw.writeInt(999999999); // ? 
                mpw.writeInt(999999999); // mapid
                mpw.write(0); // >= -2 channel
                // if cc -1 or map = 999,999,999 : I don't think you have any items or money to retrieve here. This is where you retrieve the items and mesos that you couldn't get from your Hired Merchant. You'll also need to see me as the character that opened the Personal Store.
                //Your Personal Store is open #bin Channel %s, Free Market %d#k.\r\nIf you need me, then please close your personal store first before seeing me.
                break;
            case 38:
                mpw.writeInt(days); // % tax or days, 1 day = 1%
                mpw.writeInt(fees); // feees
                break;
        }

        return mpw.getPacket();
    }

    public static final byte[] merchItemStore_ItemData(final MerchItemPackage pack) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MERCH_ITEM_STORE);
		mpw.write(38);
        mpw.writeInt(9030000); // Fredrick
        mpw.write(16); // max items?
        mpw.writeLong(126L); // ?
        mpw.writeLong(pack.getMesos());
        mpw.write(0);
        mpw.write(pack.getItems().size());
        for (final Item item : pack.getItems()) {
            PacketHelper.addItemInfo(mpw, item);
        }
        mpw.writeZeroBytes(3);

        return mpw.getPacket();
    }

    public static byte[] getMiniGame(MapleClient c, MapleMiniGame minigame) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(10);
        mpw.write(minigame.getGameType());
        mpw.write(minigame.getMaxSize());
        mpw.writeShort(minigame.getVisitorSlot(c.getCharacter()));
        PacketHelper.addCharLook(mpw, minigame.getMCOwner(), false, false);
        mpw.writeMapleAsciiString(minigame.getOwnerName());
        mpw.writeShort(minigame.getMCOwner().getJob());
        for (Pair visitorz : minigame.getVisitors()) {
            mpw.write(((Byte) visitorz.getLeft()).byteValue());
            PacketHelper.addCharLook(mpw, (MapleCharacterLook) visitorz.getRight(), false, false);
            mpw.writeMapleAsciiString(((MapleCharacter) visitorz.getRight()).getName());
            mpw.writeShort(((MapleCharacter) visitorz.getRight()).getJob());
        }
        mpw.write(-1);
        mpw.write(0);
        addGameInfo(mpw, minigame.getMCOwner(), minigame);
        for (Pair visitorz : minigame.getVisitors()) {
            mpw.write(((Byte) visitorz.getLeft()).byteValue());
            addGameInfo(mpw, (MapleCharacter) visitorz.getRight(), minigame);
        }
        mpw.write(-1);
        mpw.writeMapleAsciiString(minigame.getDescription());
        mpw.writeShort(minigame.getPieceType());
        return mpw.getPacket();
    }

    public static byte[] getMiniGameReady(boolean ready) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(ready ? 56 : GameConstants.GMS ? 60 : ready ? 59 : 57);
        return mpw.getPacket();
    }

    public static byte[] getMiniGameExitAfter(boolean ready) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(ready ? 54 : GameConstants.GMS ? 58 : ready ? 57 : 55);
        return mpw.getPacket();
    }

    public static byte[] getMiniGameStart(int loser) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(GameConstants.GMS ? 62 : 59);
        mpw.write(loser == 1 ? 0 : 1);
        return mpw.getPacket();
    }

    public static byte[] getMiniGameSkip(int slot) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(GameConstants.GMS ? 64 : 61);

        mpw.write(slot);
        return mpw.getPacket();
    }

    public static byte[] getMiniGameRequestTie() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(GameConstants.GMS ? 51 : 48);
        return mpw.getPacket();
    }

    public static byte[] getMiniGameDenyTie() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(GameConstants.GMS ? 50 : 49);
        return mpw.getPacket();
    }

    public static byte[] getMiniGameFull() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.writeShort(GameConstants.GMS ? 10 : 5);
        mpw.write(2);
        return mpw.getPacket();
    }

    public static byte[] getMiniGameMoveOmok(int move1, int move2, int move3) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(GameConstants.GMS ? 65 : 62);
        mpw.writeInt(move1);
        mpw.writeInt(move2);
        mpw.write(move3);
        return mpw.getPacket();
    }

    public static byte[] getMiniGameNewVisitor(MapleCharacter c, int slot, MapleMiniGame game) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(GameConstants.GMS ? 9 : 4);
        mpw.write(slot);
        PacketHelper.addCharLook(mpw, c, false, false);
        mpw.writeMapleAsciiString(c.getName());
        mpw.writeShort(c.getJob());
        addGameInfo(mpw, c, game);
        return mpw.getPacket();
    }

    public static void addGameInfo(MaplePacketWriter mpw, MapleCharacter chr, MapleMiniGame game) {
        mpw.writeInt(game.getGameType());
        mpw.writeInt(game.getWins(chr));
        mpw.writeInt(game.getTies(chr));
        mpw.writeInt(game.getLosses(chr));
        mpw.writeInt(game.getScore(chr));
    }

    public static byte[] getMiniGameClose(byte number) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(GameConstants.GMS ? 18 : 10);
        mpw.write(1);
        mpw.write(number);
        return mpw.getPacket();
    }

    public static byte[] getMatchCardStart(MapleMiniGame game, int loser) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(GameConstants.GMS ? 62 : 59);
        mpw.write(loser == 1 ? 0 : 1);
        int times = game.getPieceType() == 2 ? 30 : game.getPieceType() == 1 ? 20 : 12;
        mpw.write(times);
        for (int i = 1; i <= times; i++) {
            mpw.writeInt(game.getCardId(i));
        }
        return mpw.getPacket();
    }

    public static byte[] getMatchCardSelect(int turn, int slot, int firstslot, int type) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(GameConstants.GMS ? 69 : 66);
        mpw.write(turn);
        mpw.write(slot);
        if (turn == 0) {
            mpw.write(firstslot);
            mpw.write(type);
        }
        return mpw.getPacket();
    }

    public static byte[] getMiniGameResult(MapleMiniGame game, int type, int x) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(GameConstants.GMS ? 63 : 60);
        mpw.write(type);
        game.setPoints(x, type);
        if (type != 0) {
            game.setPoints(x == 1 ? 0 : 1, type == 2 ? 0 : 1);
        }
        if (type != 1) {
            if (type == 0) {
                mpw.write(x == 1 ? 0 : 1);
            } else {
                mpw.write(x);
            }
        }
        addGameInfo(mpw, game.getMCOwner(), game);
        for (Pair visitorz : game.getVisitors()) {
            addGameInfo(mpw, (MapleCharacter) visitorz.right, game);
        }

        return mpw.getPacket();

    }

    public static final byte[] MerchantBlackListView(final List<String> blackList) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(39);
        mpw.writeShort(blackList.size());
        for (String visit : blackList) {
            mpw.writeMapleAsciiString(visit);
        }
        return mpw.getPacket();
    }

    public static final byte[] MerchantVisitorView(List<String> visitor) {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
		mpw.write(38);
        mpw.writeShort(visitor.size());
        for (String visit : visitor) {
            mpw.writeMapleAsciiString(visit);
            mpw.writeInt(1);
        }
        return mpw.getPacket();
    }
}
