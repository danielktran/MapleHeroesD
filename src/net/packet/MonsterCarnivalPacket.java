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

import net.SendPacketOpcode;

import java.util.List;

import client.character.MapleCharacter;
import server.MapleCarnivalParty;
import tools.data.MaplePacketWriter;

public class MonsterCarnivalPacket {

    public static byte[] startMonsterCarnival(final MapleCharacter chr, final int enemyavailable, final int enemytotal) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_START);
        final MapleCarnivalParty friendly = chr.getCarnivalParty();
        mpw.write(friendly.getTeam());
        mpw.writeInt(chr.getAvailableCP());
        mpw.writeInt(chr.getTotalCP());
        mpw.writeInt(friendly.getAvailableCP()); // ??
        mpw.writeInt(friendly.getTotalCP()); // ??
        mpw.write(0); // ??

        return mpw.getPacket();
    }

    public static byte[] playerDiedMessage(String name, int lostCP, int team) { //CPQ
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_DIED);
		mpw.write(team); //team
        mpw.writeMapleAsciiString(name);
        mpw.write(lostCP);

        return mpw.getPacket();
    }

    public static byte[] playerLeaveMessage(boolean leader, String name, int team) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_LEAVE);
		mpw.write(leader ? 7 : 0);
        mpw.write(team); // 0: red, 1: blue
        mpw.writeMapleAsciiString(name);

        return mpw.getPacket();
    }

    public static byte[] CPUpdate(boolean party, int curCP, int totalCP, int team) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_OBTAINED_CP);
		mpw.writeInt(curCP);
        mpw.writeInt(totalCP);

        return mpw.getPacket();
    }

    public static byte[] showMCStats(int left, int right) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_STATS);
		mpw.writeInt(left);
        mpw.writeInt(right);

        return mpw.getPacket();
    }

    public static byte[] playerSummoned(String name, int tab, int number) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_SUMMON);
		mpw.write(tab);
        mpw.write(number);
        mpw.writeMapleAsciiString(name);

        return mpw.getPacket();
    }

    public static byte[] showMCResult(int mode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_RESULT);
		mpw.write(mode);

        return mpw.getPacket();
    }

    public static byte[] showMCRanking(List<MapleCharacter> players) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_RANKING);
		mpw.writeShort(players.size());
        for (MapleCharacter i : players) {
            mpw.writeInt(i.getID());
            mpw.writeMapleAsciiString(i.getName());
            mpw.writeInt(10); // points
            mpw.write(0); // team
        }

        return mpw.getPacket();
    }

    public static byte[] startCPQ(byte team, int usedcp, int totalcp) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_START);
		mpw.write(0); //team
        mpw.writeShort(0); //Obtained CP - Used CP
        mpw.writeShort(0); //Total Obtained CP
        mpw.writeShort(0); //Obtained CP - Used CP of the team
        mpw.writeShort(0); //Total Obtained CP of the team
        mpw.writeShort(0); //Obtained CP - Used CP of the team
        mpw.writeShort(0); //Total Obtained CP of the team
        mpw.writeShort(0); //Probably useless nexon shit
        mpw.writeLong(0); //Probably useless nexon shit
        return mpw.getPacket();
    }

    public static byte[] obtainCP() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_OBTAINED_CP);
		mpw.writeShort(0); //Obtained CP - Used CP
        mpw.writeShort(0); //Total Obtained CP
        return mpw.getPacket();
    }

    public static byte[] obtainPartyCP() {
        MaplePacketWriter mpw = new MaplePacketWriter();
        //mpw.writeShort(SendPacketOpcode.MONSTER_CARNIVAL_PARTY_CP);
		mpw.write(0); //Team where the points are given to.
        mpw.writeShort(0); //Obtained CP - Used CP
        mpw.writeShort(0); //Total Obtained CP
        return mpw.getPacket();
    }

    public static byte[] CPQSummon() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_SUMMON);
		mpw.write(0); //Tab
        mpw.write(0); //Number of summon inside the tab
        mpw.writeMapleAsciiString(""); //Name of the player that summons
        return mpw.getPacket();
    }

    public static byte[] CPQDied() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_SUMMON);
		mpw.write(0); //Team
        mpw.writeMapleAsciiString(""); //Name of the player that died
        mpw.write(0); //Lost CP
        return mpw.getPacket();
    }

    /**
     * Sends a CPQ Message
     *
     * Possible values for <code>message</code>:<br>
     * 1: You don't have enough CP to continue. 2: You can no longer summon the
     * Monster. 3: You can no longer summon the being. 4: This being is already
     * summoned. 5: This request has failed due to an unknown error.
     *
     * @param message Displays a message inside Carnival PQ
     * @return 
     *
     */
    public static byte[] CPQMessage(byte message) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_MESSAGE);
		mpw.write(message); //Message
        return mpw.getPacket();
    }

    public static byte[] leaveCPQ() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MONSTER_CARNIVAL_LEAVE);
		mpw.write(0); //Something?
        mpw.write(0); //Team
        mpw.writeMapleAsciiString(""); //Player name
        return mpw.getPacket();
    }
}
