/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.packet;

import client.MapleClient;
import client.character.MapleCharacter;
import constants.WorldConstants;
import constants.WorldConstants.WorldOption;
import net.SendPacketOpcode;

import java.util.LinkedList;
import java.util.List;
import server.farm.MapleFarm;
import tools.Pair;
import tools.data.MaplePacketWriter;

/**
 *
 * @author Itzik
 */
public class FarmPacket {

    public static byte[] enterFarm(MapleClient c) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_OPEN);
        PacketHelper.addCharacterInfo(mpw, c.getCharacter());
        MapleFarm f = c.getFarm();
        long time = System.currentTimeMillis();
        /* Farm House positions:
         * 000 001 002 003 004
         * 025 026 027 028 029
         * 050 051 052 053 054
         * 075 076 077 078 079
         * 100 101 102 103 104 //104 is base position
         */
        List<Integer> house = new LinkedList();
        int houseBase = 104;
        int houseId = 4150001; //15x15 house need to code better houses
        for (int i = 0; i < 5; i++) { //5x5
            for (int j = 0; j < 5; j++) { //5x5
                house.add(houseBase - j - i); //104 base position
            }
        }
        for (int i = 0; i < 25 * 25; i++) { //2D building at every position
            boolean housePosition = house.contains(i);
            mpw.writeInt(housePosition ? houseId : 0); //building that the position contains
            mpw.writeInt(i == houseBase ? houseId : 0); //building that the position bases
            mpw.writeZeroBytes(5);
            mpw.writeLong(PacketHelper.getTime(time));
        }
        mpw.writeInt(14);
        mpw.writeInt(14);
        mpw.writeInt(0);
        mpw.writeLong(PacketHelper.getTime(time + 180000));

        return mpw.getPacket();
    }

    public static byte[] farmQuestData(List<Pair<Integer, String>> canStart, List<Pair<Integer, String>> completed) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_QUEST_DATA);
		mpw.writeInt(canStart.size());
        for (Pair<Integer, String> i : canStart) {
            mpw.writeInt(i.getLeft());
            mpw.writeMapleAsciiString(i.getRight());
        }
        mpw.writeInt(completed.size());
        for (Pair<Integer, String> i : completed) {
            mpw.writeInt(i.getLeft());
            mpw.writeMapleAsciiString(i.getRight());
        }

        return mpw.getPacket();
    }

    public static byte[] alertQuest(int questId, int status) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.QUEST_ALERT);
		mpw.writeInt(questId);
        mpw.write((byte) status);

        return mpw.getPacket();
    }

    public static byte[] updateMonsterInfo(List<Pair<Integer, Integer>> monsters) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_MONSTER_INFO);
		mpw.writeInt(monsters.size());
        for (Pair<Integer, Integer> i : monsters) {
            mpw.writeInt(i.getLeft());
            mpw.writeInt(i.getRight());
        }

        return mpw.getPacket();
    }

    public static byte[] updateAesthetic(int quantity) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.AESTHETIC_POINT);
		mpw.writeInt(quantity);

        return mpw.getPacket();
    }

    public static byte[] spawnFarmMonster1() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_FARM_MONSTER1);
		mpw.writeInt(0);
        mpw.write(1);
        mpw.writeInt(0); //if 1 then same as spawnmonster2 but last byte is 1

        return mpw.getPacket();
    }

    public static byte[] farmPacket1() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_PACKET1);
		mpw.writeZeroBytes(4);

        return mpw.getPacket();
    }

    public static byte[] farmPacket4() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_PACKET4);
		mpw.writeZeroBytes(4);

        return mpw.getPacket();
    }

    public static byte[] updateQuestInfo(int id, int mode, String data) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_QUEST_INFO);
		mpw.writeInt(id);
        mpw.write((byte) mode);
        mpw.writeMapleAsciiString(data);

        return mpw.getPacket();
    }

    public static byte[] farmMessage(String msg) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_MESSAGE);
		mpw.writeMapleAsciiString(msg);

        return mpw.getPacket();
    }

    public static byte[] updateItemQuantity(int id, int quantity) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_ITEM_GAIN);
		mpw.writeInt(id);
        mpw.writeInt(quantity);

        return mpw.getPacket();
    }

    public static byte[] itemPurchased(int id) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_ITEM_PURCHASED);
		mpw.writeInt(id);
        mpw.write(1);

        return mpw.getPacket();
    }

    public static byte[] showExpGain(int quantity, int mode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_EXP);
		mpw.writeInt(quantity);
        mpw.writeInt(mode);

        return mpw.getPacket();
    }

    public static byte[] updateWaru(int quantity) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_WARU);
		mpw.writeInt(quantity);

        return mpw.getPacket();
    }

    public static byte[] showWaruHarvest(int slot, int quantity) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.HARVEST_WARU);
		mpw.write(0);
        mpw.writeInt(slot);
        mpw.writeInt(quantity);

        return mpw.getPacket();
    }

    public static byte[] spawnFarmMonster(MapleClient c, int id) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_FARM_MONSTER2);
		mpw.writeInt(0);
        mpw.write(1);
        mpw.writeInt(1);
        mpw.writeInt(1);
        mpw.writeInt(c.getFarm().getId());
        mpw.writeInt(1);
        mpw.writeInt(id);
        mpw.writeMapleAsciiString(""); //monster.getName()
        mpw.writeInt(1); //level?
        mpw.writeInt(0);
        mpw.writeInt(15);
        mpw.writeInt(3); //monster.getNurturesLeft()
        mpw.writeInt(20); //monster.getPlaysLeft()
        mpw.writeInt(0);
        long time = System.currentTimeMillis(); //should be server time
        mpw.writeLong(PacketHelper.getTime(time));
        mpw.writeLong(PacketHelper.getTime(time + 25920000000000L));
        mpw.writeLong(PacketHelper.getTime(time + 25920000000000L));
        for (int i = 0; i < 4; i++) {
            mpw.writeLong(PacketHelper.getTime(time));
        }
        mpw.writeInt(-1);
        mpw.writeInt(-1);
        mpw.writeZeroBytes(12);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] updateMonster(List<Pair<Integer, Long>> monsters) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_MONSTER);
		mpw.write(monsters.size());
        for (Pair<Integer, Long> monster : monsters) {
            mpw.writeInt(monster.getLeft()); //mob id as regular monster
            mpw.writeLong(PacketHelper.getTime(monster.getRight())); //expire
        }

        return mpw.getPacket();
    }

    public static byte[] updateMonsterQuantity(int itemId, int monsterId) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_MONSTER_GAIN);
		mpw.write(0);
        mpw.writeInt(itemId);
        mpw.write(1);
        mpw.writeInt(monsterId);
        mpw.writeInt(1); //quantity?

        return mpw.getPacket();
    }

    public static byte[] renameMonster(int index, String name) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.RENAME_MONSTER);
		mpw.writeInt(0);
        mpw.writeInt(index);
        mpw.writeMapleAsciiString(name);

        return mpw.getPacket();
    }

    public static byte[] updateFarmFriends(List<MapleFarm> friends) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_FRIENDS);
		mpw.writeInt(friends.size());
        for (MapleFarm f : friends) {
            mpw.writeInt(f.getId());
            mpw.writeMapleAsciiString(f.getName());
            mpw.writeZeroBytes(5);
        }
        mpw.writeInt(0); //blocked?
        mpw.writeInt(0); //follower

        return mpw.getPacket();
    }

    public static byte[] updateFarmInfo(MapleClient c) {
        return updateFarmInfo(c, false);
    }

    public static byte[] updateFarmInfo(MapleClient c, boolean newname) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_INFO);
		mpw.writeInt(c.getFarm().getId()); //Farm ID
        mpw.writeInt(0);
        mpw.writeLong(0); //decodeMoney ._.

        //first real farm info
        PacketHelper.addFarmInfo(mpw, c, 2);
        mpw.write(0);

        //then fake farm info
        if (newname) {
            mpw.writeMapleAsciiString("Creating...");
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(0);

            mpw.write(2);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.writeInt(1);
        } else { //or real info again incase name wasn't chosen this time
            PacketHelper.addFarmInfo(mpw, c, 2);
        }
        mpw.write(0);

        mpw.writeInt(0);
        mpw.writeInt(-1);
        mpw.write(0);

        System.out.println(mpw.toString());
        return mpw.getPacket();
    }

    public static byte[] updateUserFarmInfo(MapleCharacter chr, boolean update) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_USER_INFO);
		mpw.write(update);
        if (update) {
            mpw.writeInt(chr.getWorld());
            mpw.writeMapleAsciiString(WorldConstants.getNameById(chr.getWorld()));
            mpw.writeInt(chr.getID()); //Not sure if character id or farm id
            mpw.writeMapleAsciiString(chr.getName());
        }

        return mpw.getPacket();
    }

    public static byte[] sendFarmRanking(MapleCharacter chr, List<Pair<MapleFarm, Integer>> rankings) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_RANKING);
		mpw.writeInt(0); //Visitors
        mpw.writeInt(0); //Playtime
        mpw.writeInt(0); //Combinations
        mpw.writeInt(rankings.size());
        int i = 0;
        for (Pair<MapleFarm, Integer> best : rankings) {
            mpw.writeInt(i); //Type; 0 = visitors 1 = playtime 2 = combinations
            mpw.writeInt(best.getLeft().getId());
            mpw.writeMapleAsciiString(best.getLeft().getName());
            mpw.writeInt(best.getRight()); //Value of type
            if (i < 2) {
                i++;
            }
        }
        mpw.write(0); //Boolean; enable or disable entry reward button

        return mpw.getPacket();
    }

    public static byte[] updateAvatar(Pair<WorldOption, MapleCharacter> from, Pair<WorldOption, MapleCharacter> to, boolean change) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FARM_AVATAR);
		mpw.write(change);
        mpw.writeInt(from.getLeft().getWorld());
        mpw.writeMapleAsciiString(WorldConstants.getNameById(from.getLeft().getWorld()));
        mpw.writeInt(from.getRight().getID());
        mpw.writeMapleAsciiString(from.getRight().getName());
        if (change) {
            mpw.writeInt(to.getLeft().getWorld());
            mpw.writeMapleAsciiString(WorldConstants.getNameById(to.getLeft().getWorld()));
            mpw.writeInt(to.getRight().getID());
            mpw.writeMapleAsciiString(to.getRight().getName());
        }

        return mpw.getPacket();
    }
}
