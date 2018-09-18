/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.packet;

import client.character.MapleCharacter;
import net.SendPacketOpcode;
import server.maps.MapleMap;
import tools.data.MaplePacketWriter;

/**
 *
 * @author LEL
 */
public class EvolvingPacket {


    public static byte[] showEvolvingMessage(int action) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
        //24 00 1B 01 00
		mpw.writeShort(284);
        mpw.write(action);
        return mpw.getPacket();
    }

    public static byte[] partyCoreInfo(int[] core) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EVOLVING_ACTION);
        //AF 00 /00 /48 EF 36 00 /D3 FB 36 00 /00 00 00 00 /00 00 00 00/ 00 00 00 00/ 00 00 00 00 /00 00 00 00 /32 F3 36 00 /00 00 00 00 /00 00 00 00
        mpw.write(0);
        for (int i = 0; i < 10; i++) {
            mpw.writeInt(core[i]);
        }
        return mpw.getPacket();
    }

    public static byte[] showPartyConnect(MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EVOLVING_ACTION);//
        mpw.write(1);
        mpw.write(1);
        mpw.write(chr.getParty().getLeader().getId() == chr.getID() ? 1 : 0);
        return mpw.getPacket();
    }

    public static byte[] connectCancel() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EVOLVING_ACTION);//
        mpw.writeShort(1);
        return mpw.getPacket();
    }

    public static byte[] rewardCore(int itemid, int position) {
        //AF 00 02 01 00 00 00 00 00 D0 F2 36 00 01 00 00 00
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EVOLVING_ACTION);//
        mpw.write(2); //슬롯?
        mpw.write(1);
        mpw.writeInt(0);
        mpw.write(position);
        mpw.writeInt(itemid);
        mpw.writeInt(1);
        return mpw.getPacket();
    }

    public static byte[] showRewardCore(int itemid) {
        //24 00 1D 16 D0 F2 36 00 01 00 00 00
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
		mpw.writeShort(5662);
        mpw.writeInt(itemid);
        mpw.writeInt(1);
        return mpw.getPacket();
    }

    public static byte[] moveCore(byte equip, byte slot, byte move, byte to) {
        //AF 00 03 00 01 02 01 03
        //AF 00 03 00 01 03 01 04
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EVOLVING_ACTION);//
        mpw.write(3);
        mpw.write(0);
        mpw.write(equip);//무브, 장착해제 : 1, 장착 : 0
        mpw.write(slot);
        mpw.write(move);//장착, 무브 : 1, 장착해제 : 0
        mpw.write(to);
        return mpw.getPacket();
    }

    public static byte[] dropCore(byte position, short quantity) {
        //AF 00 04 01 /00 /01 00 /00 00
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.EVOLVING_ACTION);//
        mpw.write(4);
        mpw.write(1);
        mpw.write(position);
        mpw.writeShort(quantity);//1
        mpw.writeShort(0);
        return mpw.getPacket();
    }

    public static byte[] EvolvingWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WARP_TO_MAP);
        EvolvingWarpToMapInfo(mpw, chr, to, spawnPoint);
        mpw.writeInt(100);
        mpw.write(1);
        mpw.writeInt(0);
        mpw.writeMapleAsciiString("bgm");
        mpw.writeInt(0);
        mpw.writeShort(0);
        return mpw.getPacket();
    }

    public static void EvolvingWarpToMapInfo(MaplePacketWriter mpw, MapleCharacter player, MapleMap map, int sp) {
        mpw.writeLong(player.getClient().getChannel());
        mpw.write(0);
        mpw.write(2);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(map.getId()); //957010001
        mpw.write(sp);
        mpw.writeInt(player.getStat().getHp());
        mpw.writeShort(0);
        mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
    }

    public static byte[] spawnEvolvingMonster() {
        MaplePacketWriter mpw = new MaplePacketWriter();

        return mpw.getPacket();
    }
}

