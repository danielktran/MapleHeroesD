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
package net.channel.handler;

import client.*;
import client.anticheat.CheatTracker;
import client.anticheat.CheatingOffense;
import client.anticheat.ReportType;
import client.character.MapleCharacter;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MapleRing;
import constants.GameConstants;
import net.packet.CField;
import net.packet.CWvsContext;
import net.packet.EvolvingPacket;
import net.packet.JobPacket;
import net.packet.CWvsContext.Reward;
import net.world.MaplePartyCharacter;
import net.world.World;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.mina.common.WriteFuture;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.event.EventInstanceManager;
import scripting.event.EventManager;
import scripting.reactor.ReactorScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.MapleStatEffect;
import server.Randomizer;
import server.events.MapleCoconut;
import server.events.MapleCoconut.MapleCoconuts;
import server.events.MapleEventType;
import server.life.MapleMonsterInformationProvider;
import server.life.MonsterDropEntry;
import server.life.MonsterGlobalDropEntry;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleReactor;
import server.maps.MapleSummon;
import server.maps.MechDoor;
import server.maps.SummonMovementType;
import server.quest.MapleQuest;
import tools.AttackPair;
import tools.FileoutputUtil;
import tools.Pair;
import tools.Triple;
import tools.data.LittleEndianAccessor;

public class PlayersHandler {

    public static void Note(final LittleEndianAccessor slea, final MapleCharacter chr) {
        final byte type = slea.readByte();

        switch (type) {
            case 0:
                String name = slea.readMapleAsciiString();
                String msg = slea.readMapleAsciiString();
                boolean fame = slea.readByte() > 0;
                slea.readInt(); //0?
                Item itemz = chr.getCashInventory().findByCashId((int) slea.readLong());
                if (itemz == null || !itemz.getGiftFrom().equalsIgnoreCase(name) || !chr.getCashInventory().canSendNote(itemz.getUniqueId())) {
                    return;
                }
                try {
                    chr.sendNote(name, msg, fame ? 1 : 0);
                    chr.getCashInventory().sendedNote(itemz.getUniqueId());
                } catch (Exception e) {
                }
                break;
            case 1:
                short num = slea.readShort();
                if (num < 0) { // note overflow, shouldn't happen much unless > 32767 
                    num = 32767;
                }
                slea.skip(1); // first byte = wedding boolean? 
                for (int i = 0; i < num; i++) {
                    final int id = slea.readInt();
                    chr.deleteNote(id, slea.readByte() > 0 ? 1 : 0);
                }
                break;
            default:
                System.out.println("Unhandled note action, " + type + "");
        }
    }

    public static void DressUpRequest(final MapleCharacter chr, LittleEndianAccessor slea) {
        int code = slea.readInt();
        switch (code) {
            case 5010093:
                chr.getClient().getSession().write(JobPacket.AngelicPacket.updateDress(code, chr));
                chr.getClient().getSession().write(CField.updateCharLook(chr, true));
                break;
            case 5010094:
                chr.getClient().getSession().write(JobPacket.AngelicPacket.updateDress(code, chr));
                chr.getClient().getSession().write(CField.updateCharLook(chr, true));
                break;
        }
    }

    public static void TransformPlayer(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        // D9 A4 FD 00
        // 11 00
        // A0 C0 21 00
        // 07 00 64 66 62 64 66 62 64
        chr.updateTick(slea.readInt());
        final byte slot = (byte) slea.readShort();
        final int itemId = slea.readInt();
        final String target = slea.readMapleAsciiString();

        final Item toUse = c.getCharacter().getInventory(MapleInventoryType.USE).getItem(slot);

        if (toUse == null || toUse.getQuantity() < 1 || toUse.getItemId() != itemId) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        switch (itemId) {
            case 2212000:
                final MapleCharacter search_chr = chr.getMap().getCharacterByName(target);
                if (search_chr != null) {
                    MapleItemInformationProvider.getInstance().getItemEffect(2210023).applyTo(search_chr);
                    search_chr.dropMessage(6, chr.getName() + " has played a prank on you!");
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                }
                break;
        }
    }
    
    
        public static void startEvo(LittleEndianAccessor slea, MapleCharacter player, MapleClient c) {
        
   /*     final List<Integer> maps = new ArrayList<>();
        switch (mapid) {
            case 0:
                maps.add(960010100);
                maps.add(960010101);
                maps.add(960010102);
                break;
            case 1:
                maps.add(960020100);
                maps.add(960020101);
                maps.add(960020102);
                maps.add(960020103);
                break;
            case 2:
                maps.add(960030100);
                break;
            case 3:
                maps.add(689000000);
                maps.add(689000010);
                break;
            default:
                
        }     */
      //  player.getClient().getChannelServer().getEventSM().getEventManager("EvolutionLab"); //Coming Soon
                final EventManager em = c.getChannelServer().getEventSM().getEventManager("EvolutionLab");
                final EventInstanceManager eim = em.getInstance(("EvolutionLab"));
                 MapleMap map = player.getClient().getChannelServer().getMapFactory().getMap(957010000);
                 MaplePortal portal = map.getPortal("sp");
             //   eim.unregisterPlayer(c.getPlayer());
              
                player.changeEvolvingMap(map, portal, "Bgm25/CygnusGarden", 957010000);
                  eim.registerPlayer(c.getCharacter());
                eim.startEventTimer(45000);
                c.getCharacter().getMap().startMapEffect("Work together and defeat Pink Zakum!.", 5120039);
    }
        
            public static void HOLLY(MapleClient c, LittleEndianAccessor slea) {
       final MapleMapObject obj = c.getCharacter().getMap().getMapObject(slea.readInt(), MapleMapObjectType.SUMMON);
        int skillid = slea.readInt();
        if (skillid == 3121013) {
            final MapleSummon sum = (MapleSummon) obj;
            Point poss = c.getCharacter().getPosition();
                    final MapleSummon tosummon = new MapleSummon(c.getCharacter(), SkillFactory.getSkill(3121013).getEffect(sum.getSkillLevel()), new Point(sum.getTruePosition().x, sum.getTruePosition().y), SummonMovementType.STATIONARY);
                    c.getCharacter().getMap().spawnSummon(tosummon);
                    c.getCharacter().addSummon(tosummon);
            return;
        }
        int HP = SkillFactory.getSkill(3121013).getEffect(c.getCharacter().getSkillLevel(3121013)).getX();
        int hp = c.getCharacter().getStat().getMaxHp() * HP / 100;
        c.getCharacter().addHP(hp);
    }
        

    public static void hitCoconut(LittleEndianAccessor slea, MapleClient c) {
        /*CB 00 A6 00 06 01
         * A6 00 = coconut id
         * 06 01 = ?
         */
        int id = slea.readShort();
        String co = "coconut";
        MapleCoconut map = (MapleCoconut) c.getChannelServer().getEvent(MapleEventType.Coconut);
        if (map == null || !map.isRunning()) {
            map = (MapleCoconut) c.getChannelServer().getEvent(MapleEventType.CokePlay);
            co = "coke cap";
            if (map == null || !map.isRunning()) {
                return;
            }
        }
        //System.out.println("Coconut1");
        MapleCoconuts nut = map.getCoconut(id);
        if (nut == null || !nut.isHittable()) {
            return;
        }
        if (System.currentTimeMillis() < nut.getHitTime()) {
            return;
        }
        //System.out.println("Coconut2");
        if (nut.getHits() > 2 && Math.random() < 0.4 && !nut.isStopped()) {
            //System.out.println("Coconut3-1");
            nut.setHittable(false);
            if (Math.random() < 0.01 && map.getStopped() > 0) {
                nut.setStopped(true);
                map.stopCoconut();
                c.getCharacter().getMap().broadcastMessage(CField.hitCoconut(false, id, 1));
                return;
            }
            nut.resetHits(); // For next event (without restarts)
            //System.out.println("Coconut4");
            if (Math.random() < 0.05 && map.getBombings() > 0) {
                //System.out.println("Coconut5-1");
                c.getCharacter().getMap().broadcastMessage(CField.hitCoconut(false, id, 2));
                map.bombCoconut();
            } else if (map.getFalling() > 0) {
                //System.out.println("Coconut5-2");
                c.getCharacter().getMap().broadcastMessage(CField.hitCoconut(false, id, 3));
                map.fallCoconut();
                if (c.getCharacter().getTeam() == 0) {
                    map.addMapleScore();
                    //c.getPlayer().getMap().broadcastMessage(CWvsContext.broadcastMsg(5, c.getPlayer().getName() + " of Team Maple knocks down a " + co + "."));
                } else {
                    map.addStoryScore();
                    //c.getPlayer().getMap().broadcastMessage(CWvsContext.broadcastMsg(5, c.getPlayer().getName() + " of Team Story knocks down a " + co + "."));
                }
                c.getCharacter().getMap().broadcastMessage(CField.coconutScore(map.getCoconutScore()));
            }
        } else {
            //System.out.println("Coconut3-2");
            nut.hit();
            c.getCharacter().getMap().broadcastMessage(CField.hitCoconut(false, id, 1));
        }
    }

    public static void FollowRequest(final LittleEndianAccessor slea, final MapleClient c) {
        MapleCharacter tt = c.getCharacter().getMap().getCharacterById(slea.readInt());
        if (slea.readByte() > 0) {
            //1 when changing map
            tt = c.getCharacter().getMap().getCharacterById(c.getCharacter().getFollowId());
            if (tt != null && tt.getFollowId() == c.getCharacter().getID()) {
                tt.setFollowOn(true);
                c.getCharacter().setFollowOn(true);
            } else {
                c.getCharacter().checkFollow();
            }
            return;
        }
        if (slea.readByte() > 0) { //cancelling follow
            tt = c.getCharacter().getMap().getCharacterById(c.getCharacter().getFollowId());
            if (tt != null && tt.getFollowId() == c.getCharacter().getID() && c.getCharacter().isFollowOn()) {
                c.getCharacter().checkFollow();
            }
            return;
        }
        if (tt != null && tt.getPosition().distanceSq(c.getCharacter().getPosition()) < 10000 && tt.getFollowId() == 0 && c.getCharacter().getFollowId() == 0 && tt.getID() != c.getCharacter().getID()) { //estimate, should less
            tt.setFollowId(c.getCharacter().getID());
            tt.setFollowOn(false);
            tt.setFollowInitiator(false);
            c.getCharacter().setFollowOn(false);
            c.getCharacter().setFollowInitiator(false);
            tt.getClient().getSession().write(CWvsContext.followRequest(c.getCharacter().getID()));
        } else {
            c.getSession().write(CWvsContext.broadcastMsg(1, "You are too far away."));
        }
    }

    public static void FollowReply(final LittleEndianAccessor slea, final MapleClient c) {
        if (c.getCharacter().getFollowId() > 0 && c.getCharacter().getFollowId() == slea.readInt()) {
            MapleCharacter tt = c.getCharacter().getMap().getCharacterById(c.getCharacter().getFollowId());
            if (tt != null && tt.getPosition().distanceSq(c.getCharacter().getPosition()) < 10000 && tt.getFollowId() == 0 && tt.getID() != c.getCharacter().getID()) { //estimate, should less
                boolean accepted = slea.readByte() > 0;
                if (accepted) {
                    tt.setFollowId(c.getCharacter().getID());
                    tt.setFollowOn(true);
                    tt.setFollowInitiator(false);
                    c.getCharacter().setFollowOn(true);
                    c.getCharacter().setFollowInitiator(true);
                    c.getCharacter().getMap().broadcastMessage(CField.followEffect(tt.getID(), c.getCharacter().getID(), null));
                } else {
                    c.getCharacter().setFollowId(0);
                    tt.setFollowId(0);
                    tt.getClient().getSession().write(CField.getFollowMsg(5));
                }
            } else {
                if (tt != null) {
                    tt.setFollowId(0);
                    c.getCharacter().setFollowId(0);
                }
                c.getSession().write(CWvsContext.broadcastMsg(1, "You are too far away."));
            }
        } else {
            c.getCharacter().setFollowId(0);
        }
    }
    
    
     //   public static void HOLLY(MapleClient c,  LittleEndianAccessor slea) {
   //     int skillid = slea.readInt();
   //     if (skillid == 3121013) {
    //        Point poss = c.getPlayer().getPosition();
    ///        owner == MapleCharacter;
   //         MapleSummon summons = new MapleSummon(summon.OwnerId(), skillid, poss, SummonMovementType.STATIONARY);
    //        c.getPlayer().getMap().spawnSummon(summons);
    //        return;
    //    }
   //     }

    public static void DoRing(final MapleClient c, final String name, final int itemid) {
        final int newItemId = itemid == 2240000 ? 1112803 : (itemid == 2240001 ? 1112806 : (itemid == 2240002 ? 1112807 : (itemid == 2240003 ? 1112809 : (1112300 + (itemid - 2240004)))));
        final MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
        int errcode = 0;
        if (c.getCharacter().getMarriageId() > 0) {
            errcode = 0x17;
        } else if (chr == null) {
            errcode = 0x12;
        } else if (chr.getMapId() != c.getCharacter().getMapId()) {
            errcode = 0x13;
        } else if (!c.getCharacter().haveItem(itemid, 1) || itemid < 2240000 || itemid > 2240015) {
            errcode = 0x0D;
        } else if (chr.getMarriageId() > 0 || chr.getMarriageItemId() > 0) {
            errcode = 0x18;
        } else if (!MapleInventoryManipulator.checkSpace(c, newItemId, 1, "")) {
            errcode = 0x14;
        } else if (!MapleInventoryManipulator.checkSpace(chr.getClient(), newItemId, 1, "")) {
            errcode = 0x15;
        }
        if (errcode > 0) {
            c.getSession().write(CWvsContext.sendEngagement((byte) errcode, 0, null, null));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        c.getCharacter().setMarriageItemId(itemid);
        WriteFuture write = chr.getClient().getSession().write(CWvsContext.sendEngagementRequest(c.getCharacter().getName(), c.getCharacter().getID()));
    }

    public static void RingAction(final LittleEndianAccessor slea, final MapleClient c) {
        final byte mode = slea.readByte();
        if (mode == 0) {
            DoRing(c, slea.readMapleAsciiString(), slea.readInt());
            //1112300 + (itemid - 2240004)
        } else if (mode == 1) {
            c.getCharacter().setMarriageItemId(0);
        } else if (mode == 2) { //accept/deny proposal
            final boolean accepted = slea.readByte() > 0;
            final String name = slea.readMapleAsciiString();
            final int id = slea.readInt();
            final MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(name);
            if (c.getCharacter().getMarriageId() > 0 || chr == null || chr.getID() != id || chr.getMarriageItemId() <= 0 || !chr.haveItem(chr.getMarriageItemId(), 1) || chr.getMarriageId() > 0 || !chr.isAlive() || chr.getEventInstance() != null || !c.getCharacter().isAlive() || c.getCharacter().getEventInstance() != null) {
                c.getSession().write(CWvsContext.sendEngagement((byte) 0x1D, 0, null, null));
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            if (accepted) {
                final int itemid = chr.getMarriageItemId();
                final int newItemId = itemid == 2240000 ? 1112803 : (itemid == 2240001 ? 1112806 : (itemid == 2240002 ? 1112807 : (itemid == 2240003 ? 1112809 : (1112300 + (itemid - 2240004)))));
                if (!MapleInventoryManipulator.checkSpace(c, newItemId, 1, "") || !MapleInventoryManipulator.checkSpace(chr.getClient(), newItemId, 1, "")) {
                    c.getSession().write(CWvsContext.sendEngagement((byte) 0x15, 0, null, null));
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
                try {
                    final int[] ringID = MapleRing.makeRing(newItemId, c.getCharacter(), chr);
                    Equip eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(newItemId, ringID[1]);
                    MapleRing ring = MapleRing.loadFromDb(ringID[1]);
                    if (ring != null) {
                        eq.setRing(ring);
                    }
                    MapleInventoryManipulator.addbyItem(c, eq);

                    eq = (Equip) MapleItemInformationProvider.getInstance().getEquipById(newItemId, ringID[0]);
                    ring = MapleRing.loadFromDb(ringID[0]);
                    if (ring != null) {
                        eq.setRing(ring);
                    }
                    MapleInventoryManipulator.addbyItem(chr.getClient(), eq);

                    MapleInventoryManipulator.removeById(chr.getClient(), MapleInventoryType.USE, chr.getMarriageItemId(), 1, false, false);

                    chr.getClient().getSession().write(CWvsContext.sendEngagement((byte) 0x10, newItemId, chr, c.getCharacter()));
                    chr.setMarriageId(c.getCharacter().getID());
                    c.getCharacter().setMarriageId(chr.getID());

                    chr.fakeRelog();
                    c.getCharacter().fakeRelog();
                } catch (Exception e) {
                    FileoutputUtil.outputFileError(FileoutputUtil.PacketEx_Log, e);
                }

            } else {
                chr.getClient().getSession().write(CWvsContext.sendEngagement((byte) 0x1E, 0, null, null));
            }
            c.getSession().write(CWvsContext.enableActions());
            chr.setMarriageItemId(0);
        } else if (mode == 3) { //drop, only works for ETC
            final int itemId = slea.readInt();
            final MapleInventoryType type = GameConstants.getInventoryType(itemId);
            final Item item = c.getCharacter().getInventory(type).findById(itemId);
            if (item != null && type == MapleInventoryType.ETC && itemId / 10000 == 421) {
                MapleInventoryManipulator.drop(c, type, item.getPosition(), item.getQuantity());
            }
        }
    }

    public static void Solomon(final LittleEndianAccessor slea, final MapleClient c) {
        c.getSession().write(CWvsContext.enableActions());
        c.getCharacter().updateTick(slea.readInt());
        Item item = c.getCharacter().getInventory(MapleInventoryType.USE).getItem(slea.readShort());
        if (item == null || item.getItemId() != slea.readInt() || item.getQuantity() <= 0 || c.getCharacter().getGachExp() > 0 || c.getCharacter().getLevel() > 50 || MapleItemInformationProvider.getInstance().getItemEffect(item.getItemId()).getEXP() <= 0) {
            return;
        }
        c.getCharacter().setGachExp(c.getCharacter().getGachExp() + MapleItemInformationProvider.getInstance().getItemEffect(item.getItemId()).getEXP());
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, item.getPosition(), (short) 1, false);
        c.getCharacter().updateSingleStat(MapleStat.GACHAPONEXP, c.getCharacter().getGachExp());
    }

    public static void GachExp(final LittleEndianAccessor slea, final MapleClient c) {
        c.getSession().write(CWvsContext.enableActions());
        c.getCharacter().updateTick(slea.readInt());
        if (c.getCharacter().getGachExp() <= 0) {
            return;
        }
        c.getCharacter().gainExp(c.getCharacter().getGachExp() * GameConstants.getExpRate_Quest(c.getCharacter().getLevel()), true, true, false);
        c.getCharacter().setGachExp(0);
        c.getCharacter().updateSingleStat(MapleStat.GACHAPONEXP, 0);
    }

    public static void Report(final LittleEndianAccessor slea, final MapleClient c) {
        //0 = success 1 = unable to locate 2 = once a day 3 = you've been reported 4+ = unknown reason
        MapleCharacter other;
        ReportType type;
        type = ReportType.getById(slea.readByte());
        other = c.getCharacter().getMap().getCharacterByName(slea.readMapleAsciiString());
        //then,byte(?) and string(reason)
        if (other == null || type == null || other.isIntern()) {
            c.getSession().write(CWvsContext.report(4));
            return;
        }
        final MapleQuestStatus stat = c.getCharacter().getQuestNAdd(MapleQuest.getInstance(GameConstants.REPORT_QUEST));
        if (stat.getCustomData() == null) {
            stat.setCustomData("0");
        }
        final long currentTime = System.currentTimeMillis();
        final long theTime = Long.parseLong(stat.getCustomData());
        if (theTime + 7200000 > currentTime && !c.getCharacter().isIntern()) {
            c.getSession().write(CWvsContext.enableActions());
            c.getCharacter().dropMessage(5, "You may only report every 2 hours.");
        } else {
            stat.setCustomData(String.valueOf(currentTime));
            other.addReport(type);
            c.getSession().write(CWvsContext.report(2));
        }
    }

    public static void exitSilentCrusadeUI(final LittleEndianAccessor slea, final MapleClient c) {
        c.getCharacter().updateInfoQuest(1652, "alert=-1"); //Hide Silent Crusade icon
    }

    public static void claimSilentCrusadeReward(final LittleEndianAccessor slea, final MapleClient c) {
        short chapter = slea.readShort();
        if (c.getCharacter() == null || !c.getCharacter().getInfoQuest(1648 + chapter).equals("m0=2;m1=2;m2=2;m3=2;m4=2")) {
            System.out.println("[Silent Crusade] " + c.getCharacter().getName() + "has tried to exploit the reward of chapter " + (chapter + 1));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        final int use = c.getCharacter().getInventory(MapleInventoryType.USE).getNumFreeSlot();
        final int setup = c.getCharacter().getInventory(MapleInventoryType.SETUP).getNumFreeSlot();
        final int etc = c.getCharacter().getInventory(MapleInventoryType.ETC).getNumFreeSlot();
        if (use < 1 || setup < 1 || etc < 1) {
            c.getSession().write(CWvsContext.getSilentCrusadeMsg((byte) 2));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        switch (chapter) {
            case 0:
                c.getCharacter().gainItem(3700031, 1);
                c.getCharacter().gainItem(4310029, 10);
                c.getCharacter().updateInfoQuest(1648, "m0=2;m1=2;m2=2;m3=2;m4=2;r=1"); //Show Reward Claimed
                break;
            case 1:
                c.getCharacter().gainItem(3700032, 1);
                c.getCharacter().gainItem(2430669, 1);
                c.getCharacter().gainItem(4310029, 15);
                c.getCharacter().updateInfoQuest(1649, "m0=2;m1=2;m2=2;m3=2;m4=2;r=1"); //Show Reward Claimed
                break;
            case 2:
                c.getCharacter().gainItem(3700033, 1);
                c.getCharacter().gainItem(2430668, 1);
                c.getCharacter().gainItem(4310029, 20);
                c.getCharacter().updateInfoQuest(1650, "m0=2;m1=2;m2=2;m3=2;m4=2;r=1"); //Show Reward Claimed
                break;
            case 3:
                c.getCharacter().gainItem(3700034, 1);
                c.getCharacter().gainItem(2049309, 1);
                c.getCharacter().gainItem(4310029, 30);
                c.getCharacter().updateInfoQuest(1651, "m0=2;m1=2;m2=2;m3=2;m4=2;r=1"); //Show Reward Claimed
                break;
            default:
                System.out.println("New Silent Crusade Chapter found: " + (chapter + 1));
        }
        c.getSession().write(CWvsContext.enableActions());
    }

    public static void buySilentCrusade(final LittleEndianAccessor slea, final MapleClient c) {
        //ui window is 0x49
        //slea: [00 00] [4F 46 11 00] [01 00]
        short slot = slea.readShort(); //slot of item in the silent crusade window
        int itemId = slea.readInt();
        short quantity = slea.readShort();
        int tokenPrice = 0, potentialGrade = 0;
        final MapleDataProvider prov = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));
        MapleData data = prov.getData("CrossHunterChapter.img");
        int currItemId = 0;
        for (final MapleData wzdata : data.getChildren()) {
            if (wzdata.getName().equals("Shop")) {
                for (final MapleData wzdata2 : wzdata.getChildren()) {
                    for (MapleData wzdata3 : wzdata2.getChildren()) {
                        switch (wzdata3.getName()) {
                            case "itemId":
                                currItemId = MapleDataTool.getInt(wzdata3);
                                break;
                            case "tokenPrice":
                                if (currItemId == itemId) {
                                    tokenPrice = MapleDataTool.getInt(wzdata3);
                                }
                                break;
                            case "potentialGrade":
                                if (currItemId == itemId) {
                                    potentialGrade = MapleDataTool.getInt(wzdata3);
                                }
                                break;
                        }
                    }
                }
            }
        }
        if (tokenPrice == 0) {
            System.out.println("[Silent Crusade] " + c.getCharacter().getName() + " has tried to exploit silent crusade shop.");
            c.getSession().write(CWvsContext.getSilentCrusadeMsg((byte) 3));
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (c.getCharacter().getInventory(GameConstants.getInventoryType(itemId)).getNumFreeSlot() >= quantity) {
            if (c.getCharacter().itemQuantity(4310029) < tokenPrice) {
                c.getSession().write(CWvsContext.getSilentCrusadeMsg((byte) 1));
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            if (MapleInventoryManipulator.checkSpace(c, itemId, quantity, "")) {
                MapleInventoryManipulator.removeById(c, MapleInventoryType.ETC, 4310029, tokenPrice, false, false);
                if (itemId < 2000000 && potentialGrade > 0) {
                    Equip equip = (Equip) MapleItemInformationProvider.getInstance().getEquipById(itemId);
                    equip.setQuantity((short) 1);
                    equip.setGMLog("BUY_SILENT_CRUSADE");
                    equip.setPotential1(-potentialGrade);
                    if (!MapleInventoryManipulator.addbyItem(c, equip)) {
                        c.getSession().write(CWvsContext.getSilentCrusadeMsg((byte) 2));
                        c.getSession().write(CWvsContext.enableActions());
                        return;
                    }
                } else {
                    if (!MapleInventoryManipulator.addById(c, itemId, (short) quantity, "BUY_SILENT_CRUSADE")) {
                        c.getSession().write(CWvsContext.getSilentCrusadeMsg((byte) 2));
                        c.getSession().write(CWvsContext.enableActions());
                        return;
                    }
                }
                c.getSession().write(CWvsContext.getSilentCrusadeMsg((byte) 0));
                c.getSession().write(CWvsContext.enableActions());
            } else {
                c.getSession().write(CWvsContext.getSilentCrusadeMsg((byte) 2));
                c.getSession().write(CWvsContext.enableActions());
            }
        } else {
            c.getSession().write(CWvsContext.getSilentCrusadeMsg((byte) 2));
            c.getSession().write(CWvsContext.enableActions());
        }
    }

    public static void UpdatePlayerInformation(final LittleEndianAccessor slea, final MapleClient c) {
        byte mode = slea.readByte(); //01 open ui 03 save info
        if (mode == 1) {
            if (c.getCharacter().getQuestStatus(GameConstants.PLAYER_INFORMATION) > 0) {
                try {
                    String[] info = c.getCharacter().getQuest(MapleQuest.getInstance(GameConstants.PLAYER_INFORMATION)).getCustomData().split(";");
                    c.getSession().write(CWvsContext.loadInformation((byte) 2, Integer.parseInt(info[0]), Integer.parseInt(info[1]), Integer.parseInt(info[2]), Integer.parseInt(info[3]), true));
                } catch (NumberFormatException ex) {
                    c.getSession().write(CWvsContext.loadInformation((byte) 4, 0, 0, 0, 0, false));
                    System.out.println("Failed to update account information: " + ex);
                }
            }
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (mode != 3) {
            System.out.println("new account information mode found: " + mode);
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        int country = slea.readInt();
        int birthday = slea.readInt();
        int favoriteAction = slea.readInt(); //kind of mask
        int favoriteLocation = slea.readInt(); //kind of mask
        c.getCharacter().getQuestNAdd(MapleQuest.getInstance(GameConstants.PLAYER_INFORMATION)).setCustomData("location=" + country + ";birthday=" + birthday + ";favoriteaction=" + favoriteAction + ";favoritelocation=" + favoriteLocation);
    }

    public static void FindFriends(final LittleEndianAccessor slea, final MapleClient c) {
        byte mode = slea.readByte();
        switch (mode) {
            case 5:
                if (c.getCharacter().getQuestStatus(GameConstants.PLAYER_INFORMATION) == 0) {
                    c.getSession().write(CWvsContext.findFriendResult((byte) 6, null, 0, null));
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
            case 7:
                List<MapleCharacter> characters = new LinkedList();
                for (MapleCharacter chr : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                    if (chr != c.getCharacter()) {
                        if (c.getCharacter().getQuestStatus(GameConstants.PLAYER_INFORMATION) == 0 || characters.isEmpty()) {
                            characters.add(chr);
                        } else {
                            if (chr.getQuestStatus(GameConstants.PLAYER_INFORMATION) == 0 && characters.isEmpty()) {
                                continue;
                            }
                            String[] info = c.getCharacter().getQuest(MapleQuest.getInstance(GameConstants.PLAYER_INFORMATION)).getCustomData().split(";");
                            String[] info2 = chr.getQuest(MapleQuest.getInstance(GameConstants.PLAYER_INFORMATION)).getCustomData().split(";");
                            if (info[0].equals(info2[0]) || info[1].equals(info2[1]) || info[2].equals(info2[2]) || info[3].equals(info2[3])) {
                                characters.add(chr);
                            }
                        }
                    }
                }
                if (characters.isEmpty()) {
                    c.getSession().write(CWvsContext.findFriendResult((byte) 9, null, 12, null));
                } else {
                    c.getSession().write(CWvsContext.findFriendResult((byte) 8, characters, 0, null));
                }
                break;
        }
    }

    public static void LinkSkill(final LittleEndianAccessor slea, final MapleClient c, final MapleCharacter chr) {
        //slea: [76 7F 31 01] [35 00 00 00]
         c.getCharacter().dropMessage(1, "Beginning link skill.");
        int skill = slea.readInt();
        int cid = slea.readInt();
        boolean found = false;
        for (MapleCharacter chr2 : c.loadCharacters(c.getCharacter().getWorld())) {
            if (chr2.getID() == cid) {
                found = true;
            }
        }
        if (GameConstants.getLinkSkillByJob(chr.getJob()) != skill || !found || chr.getLevel() > 70) {
            c.getCharacter().dropMessage(1, "An error has occured.");
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        MapleCharacter.addLinkSkill(cid, skill);
    }

    public static void reviveAzwan(LittleEndianAccessor slea, MapleClient c) {
        if (c.getCharacter() == null) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        if (!GameConstants.isAzwanMap(c.getCharacter().getMapId())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        c.getCharacter().changeMap(c.getCharacter().getMapId(), 0);
        c.getCharacter().getStat().recalcLocalStats(c.getCharacter());
        c.getCharacter().getStat().heal(c.getCharacter());
    }

    public static void magicWheel(LittleEndianAccessor slea, MapleClient c) {
        final byte mode = slea.readByte(); // 0 = open 2 = start 4 = receive reward
        if (mode == 2) {
            slea.readInt(); //4
            final short toUseSlot = slea.readShort();
            slea.readShort();
            final int tokenId = slea.readInt();
            if (c.getCharacter().getInventory(MapleInventoryType.ETC).getItem(toUseSlot).getItemId() != tokenId) {
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            for (byte inv = 1; inv <= 5; inv++) {
                if (c.getCharacter().getInventory(MapleInventoryType.getByType(inv)).getNumFreeSlot() < 2) {
                    c.getSession().write(CWvsContext.magicWheel((byte) 7, null, null, 0));
                    c.getSession().write(CWvsContext.enableActions());
                    return;
                }
            }
            List<Integer> items = new LinkedList();
            GameConstants.loadWheelRewards(items, tokenId);
            int end = Randomizer.nextInt(10);
            String data = "Magic Wheel";
            c.getCharacter().setWheelItem(items.get(end));
            if (!MapleInventoryManipulator.removeFromSlot_Lock(c, GameConstants.getInventoryType(tokenId), toUseSlot, (short) 1, false, false)) {
                c.getSession().write(CWvsContext.magicWheel((byte) 9, null, null, 0));
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            c.getSession().write(CWvsContext.magicWheel((byte) 3, items, data, end));
        } else if (mode == 4) {
            final String data = slea.readMapleAsciiString();
            int item;
            //try {
            //item = Integer.parseInt(data) / 2;
            item = c.getCharacter().getWheelItem();
            if (item == 0 || !MapleInventoryManipulator.addById(c, item, (short) 1, null)) {
                c.getSession().write(CWvsContext.magicWheel((byte) 0xA, null, null, 0));
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            //} catch (Exception ex) {
            //    c.getSession().write(CWvsContext.magicWheel((byte) 0xA, null, null, 0));
            //    c.getSession().write(CWvsContext.enableActions());
            //    return;
            //}
            c.getCharacter().setWheelItem(0);
            c.getSession().write(CWvsContext.magicWheel((byte) 5, null, null, 0));
        }
    }

    public static void onReward(LittleEndianAccessor slea, MapleClient c) throws SQLException {
        System.err.println("onReward");
        int id = slea.readInt();
        int type = slea.readInt();
        int itemId = slea.readInt();
        slea.readInt(); //might be item quantity
        slea.readInt(); //no idea
        slea.readLong(); //no idea
        slea.readInt(); //no idea
        int mp = slea.readInt();
        int meso = slea.readInt();
        int exp = slea.readInt();
        slea.readInt(); //no idea
        slea.readInt(); //no idea
        slea.readMapleAsciiString(); //no idea
        slea.readMapleAsciiString(); //no idea
        slea.readMapleAsciiString(); //no idea
        byte mode = slea.readByte();
        if (mode == 2) { //Decline
            c.getCharacter().deleteReward(id);
            c.getSession().write(CWvsContext.enableActions());
            return;
        } else if (mode == 1) { //Accept
            if (type < 0 || type > 5) {
                System.out.println("[Hacking Attempt] " + c.getCharacter().getName() + " has tried to receive reward with unavailable type.");
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            MapleReward reward = c.getCharacter().getReward(id);
            if (reward == null) {
                c.getSession().write(Reward.receiveReward(id, (byte) 0x15, 0));
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            if (reward.getType() != type || reward.getItem() != itemId
                    || reward.getMaplePoints() != mp || reward.getMeso() != meso
                    || reward.getExp() != exp) {
                System.out.println("[Hacking Attempt] " + c.getCharacter().getName() + " has tried to exploit the reward receive.");
                c.getSession().write(CWvsContext.enableActions());
                return;
            }
            byte msg = 0x15;
            int quantity = 0;
            switch (type) {
                case 1:
                    if (MapleInventoryManipulator.checkSpace(c, itemId, 1, "")) {
                        c.getCharacter().gainItem(itemId, 1);
                        c.getCharacter().deleteReward(id);
                        quantity = 1;
                        msg = 0x0C;
                    } else {
                        msg = 0x16;
                    }
                    break;
                case 3:
                    if (c.getCharacter().getCSPoints(2) + mp >= 0) {
                        c.getCharacter().modifyCSPoints(2, mp, false);
                        c.getCharacter().deleteReward(id);
                        quantity = mp;
                        msg = 0x0B;
                    } else {
                        msg = 0x14;
                    }
                    break;
                case 4:
                    if (c.getCharacter().getMeso() + meso < Integer.MAX_VALUE
                            && c.getCharacter().getMeso() + meso > 0) {
                        c.getCharacter().gainMeso(meso, true, true);
                        c.getCharacter().deleteReward(id);
                        quantity = meso;
                        msg = 0x0E;
                    } else {
                        msg = 0x17;
                    }
                    break;
                case 5:
                    int maxlevel = GameConstants.isKOC(c.getCharacter().getJob()) ? 120 : 200;
                    if (c.getCharacter().getLevel() < maxlevel) {
                        c.getCharacter().gainExp(exp, true, true, true);
                        c.getCharacter().deleteReward(id);
                        quantity = exp;
                        msg = 0x0F;
                    } else {
                        msg = 0x18;
                    }
                    break;
                default:
                    System.out.println("New reward type found: " + type);
                    break;
            }
            c.getSession().write(Reward.receiveReward(id, msg, quantity));
        }
        if (mode < 0 || mode > 2) {
            System.out.println("New reward mode found: " + mode);
        }
    }

    public static void blackFriday(LittleEndianAccessor slea, MapleClient c) {
        SimpleDateFormat sdfGMT = new SimpleDateFormat("yyyy-MM-dd");
        sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
        c.getCharacter().updateInfoQuest(5604, sdfGMT.format(Calendar.getInstance().getTime()).replaceAll("-", ""));
        System.out.println(sdfGMT.format(Calendar.getInstance().getTime()).replaceAll("-", ""));
    }

    public static void updateRedLeafHigh(LittleEndianAccessor slea, MapleClient c) { //not finished yet
        //TODO: load and set red leaf high in sql
        slea.readInt(); //questid or something
        slea.readInt(); //joe joe quest
        int joejoe = slea.readInt();
        slea.readInt(); //hermoninny quest
        int hermoninny = slea.readInt();
        slea.readInt(); //little dragon quest
        int littledragon = slea.readInt();
        slea.readInt(); //ika quest
        int ika = slea.readInt();
        if (joejoe + hermoninny + littledragon + ika != c.getCharacter().getFriendShipToAdd()) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        c.getCharacter().setFriendShipPoints(joejoe, hermoninny, littledragon, ika);
    }

    public static void StealSkill(LittleEndianAccessor slea, MapleClient c) {
        if (c.getCharacter() == null || c.getCharacter().getMap() == null || !GameConstants.isPhantom(c.getCharacter().getJob())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        final int skill = slea.readInt();
        final int cid = slea.readInt();

        //then a byte, 0 = learning, 1 = removing, but it doesnt matter since we can just use cid
        if (cid <= 0) {
            c.getCharacter().removeStolenSkill(skill);
        } else {
            final MapleCharacter other = c.getCharacter().getMap().getCharacterById(cid);
            if (other != null && other.getID() != c.getCharacter().getID() && other.getTotalSkillLevel(skill) > 0) {
                c.getCharacter().addStolenSkill(skill, other.getTotalSkillLevel(skill));
            }
        }
    }

    public static void ChooseSkill(LittleEndianAccessor slea, MapleClient c) {
        if (c.getCharacter() == null || c.getCharacter().getMap() == null || !GameConstants.isPhantom(c.getCharacter().getJob())) {
            c.getSession().write(CWvsContext.enableActions());
            return;
        }
        final int base = slea.readInt();
        final int skill = slea.readInt();
        if (skill <= 0) {
            c.getCharacter().unchooseStolenSkill(base);
        } else {
            c.getCharacter().chooseStolenSkill(skill);
        }
    }

    public static void viewSkills(final LittleEndianAccessor slea, final MapleClient c) {
        int victim = slea.readInt();
        int jobid = c.getChannelServer().getPlayerStorage().getCharacterById(victim).getJob();
        List<Integer> list = SkillFactory.getSkillsByJob(jobid);
        if (!c.getChannelServer().getPlayerStorage().getCharacterById(victim).getSkills().isEmpty() && GameConstants.isAdventurer(jobid)) {
            c.getSession().write(CField.viewSkills(c.getChannelServer().getPlayerStorage().getCharacterById(victim)));
        } else {
            c.getCharacter().dropMessage(6, "You cannot take skills off non-adventurer's");
        }
    }

    public static boolean inArea(MapleCharacter chr) {
        for (Rectangle rect : chr.getMap().getAreas()) {
            if (rect.contains(chr.getTruePosition())) {
                return true;
            }
        }
        for (MapleMist mist : chr.getMap().getAllMistsThreadsafe()) {
            if (mist.getOwnerId() == chr.getID() && mist.isPoisonMist() == 2 && mist.getBox().contains(chr.getTruePosition())) {
                return true;
            }
        }
        return false;
    }
    
    public static void CassandrasCollection(LittleEndianAccessor slea, MapleClient c) {
        c.getSession().write(CField.getCassandrasCollection());  
    }
    
    public static void calcHyperSkillPointCount(MapleClient c) {
        for (int i = 0; i < 3; i++) {
            c.getSession().write(CWvsContext.updateHyperSp(i, c.getCharacter().getRemainingHSps()[i]));
        }
    }
}
