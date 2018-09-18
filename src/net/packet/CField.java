package net.packet;

import client.*;
import client.character.MapleCharacter;
import client.inventory.*;
import constants.GameConstants;
import constants.Interaction;
import constants.QuickMove.QuickMoveNPC;
import net.SendPacketOpcode;
import net.channel.handler.AttackInfo;
import net.world.World;
import net.world.guild.MapleGuild;
import net.world.guild.MapleGuildAlliance;

import java.awt.Point;
import java.util.*;
import server.MaplePackageActions;
import server.MapleTrade;
import server.Randomizer;
import server.events.MapleSnowball;
import server.life.MapleNPC;
import server.maps.*;
import server.movement.LifeMovementFragment;
import server.quest.MapleQuest;
import server.shops.MapleShop;
import tools.AttackPair;
import tools.HexTool;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketWriter;

public class CField {

    public static int DEFAULT_BUFFMASK = 0;
    public static final byte[] Nexon_IP = new byte[]{(byte) 8, (byte) 31, (byte) 99, (byte) 141};  //current ip
    public static final byte[] MapleTalk_IP = new byte[]{(byte) 8, (byte) 31, (byte) 99, (byte) 133};
    
    public static byte[] getPacketFromHexString(String hex) {
        return HexTool.getByteArrayFromHexString(hex);
    }

    public static byte[] getServerIP(int port, int worldID, int charID) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SERVER_IP);
		mpw.writeShort(0);
        mpw.write(Nexon_IP);
        mpw.writeShort(port);
        mpw.write(Nexon_IP); // MapleTalk IP
        mpw.writeShort(8785 + worldID); // 8785 + World_ID
        mpw.writeInt(charID);
        mpw.writeZeroBytes(15);

        return mpw.getPacket();
    }

    public static byte[] getChannelChange(MapleClient c, int port) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CHANGE_CHANNEL);
		mpw.write(1);
        mpw.write(Nexon_IP);
        mpw.writeShort(port);
        mpw.writeInt(0);

        return mpw.getPacket();
    }

    public static byte[] getPVPType(int type, List<Pair<Integer, String>> players1, int team, boolean enabled, int lvl) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_TYPE);
		mpw.write(type);
        mpw.write(lvl);
        mpw.write(enabled ? 1 : 0);
        mpw.write(0);
        if (type > 0) {
            mpw.write(team);
            mpw.writeInt(players1.size());
            for (Pair pl : players1) {
                mpw.writeInt(((Integer) pl.left).intValue());
                mpw.writeMapleAsciiString((String) pl.right);
                mpw.writeShort(2660);
            }
        }

        return mpw.getPacket();
    }

    public static byte[] getPVPTransform(int type) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_TRANSFORM);
		mpw.write(type);

        return mpw.getPacket();
    }

    public static byte[] getPVPDetails(List<Pair<Integer, Integer>> players) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_DETAILS);
		mpw.write(1);
        mpw.write(0);
        mpw.writeInt(players.size());
        for (Pair pl : players) {
            mpw.writeInt(((Integer) pl.left).intValue());
            mpw.write(((Integer) pl.right).intValue());
        }

        return mpw.getPacket();
    }

    public static byte[] enablePVP(boolean enabled) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_ENABLED);
		mpw.write(enabled ? 1 : 2);

        return mpw.getPacket();
    }

    public static byte[] getPVPScore(int score, boolean kill) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_SCORE);
		mpw.writeInt(score);
        mpw.write(kill ? 1 : 0);

        return mpw.getPacket();
    }

    public static byte[] getPVPResult(List<Pair<Integer, MapleCharacter>> flags, int exp, int winningTeam, int playerTeam) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_RESULT);
		mpw.writeInt(flags.size());
        for (Pair f : flags) {
            mpw.writeInt(((MapleCharacter) f.right).getID());
            mpw.writeMapleAsciiString(((MapleCharacter) f.right).getName());
            mpw.writeInt(((Integer) f.left).intValue());
            mpw.writeShort(((MapleCharacter) f.right).getTeam() + 1);
            mpw.writeInt(0);
            mpw.writeInt(0);
        }
        mpw.writeZeroBytes(24);
        mpw.writeInt(exp);
        mpw.write(0);
        mpw.writeShort(100);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.write(winningTeam);
        mpw.write(playerTeam);

        return mpw.getPacket();
    }

    public static byte[] getPVPTeam(List<Pair<Integer, String>> players) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_TEAM);
		mpw.writeInt(players.size());
        for (Pair pl : players) {
            mpw.writeInt(((Integer) pl.left).intValue());
            mpw.writeMapleAsciiString((String) pl.right);
            mpw.writeShort(2660);
        }

        return mpw.getPacket();
    }

    public static byte[] getPVPScoreboard(List<Pair<Integer, MapleCharacter>> flags, int type) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_SCOREBOARD);
		mpw.writeShort(flags.size());
        for (Pair f : flags) {
            mpw.writeInt(((MapleCharacter) f.right).getID());
            mpw.writeMapleAsciiString(((MapleCharacter) f.right).getName());
            mpw.writeInt(((Integer) f.left).intValue());
            mpw.write(type == 0 ? 0 : ((MapleCharacter) f.right).getTeam() + 1);
        }

        return mpw.getPacket();
    }

    public static byte[] getPVPPoints(int p1, int p2) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_POINTS);
		mpw.writeInt(p1);
        mpw.writeInt(p2);

        return mpw.getPacket();
    }

    public static byte[] getPVPKilled(String lastWords) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_KILLED);
		mpw.writeMapleAsciiString(lastWords);

        return mpw.getPacket();
    }

    public static byte[] getPVPMode(int mode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_MODE);
		mpw.write(mode);

        return mpw.getPacket();
    }

    public static byte[] getPVPIceHPBar(int hp, int maxHp) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_ICEKNIGHT);
		mpw.writeInt(hp);
        mpw.writeInt(maxHp);

        return mpw.getPacket();
    }

    public static byte[] getCaptureFlags(MapleMap map) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CAPTURE_FLAGS);
		mpw.writeRect(map.getArea(0));
        mpw.writeInt(((Point) ((Pair) map.getGuardians().get(0)).left).x);
        mpw.writeInt(((Point) ((Pair) map.getGuardians().get(0)).left).y);
        mpw.writeRect(map.getArea(1));
        mpw.writeInt(((Point) ((Pair) map.getGuardians().get(1)).left).x);
        mpw.writeInt(((Point) ((Pair) map.getGuardians().get(1)).left).y);

        return mpw.getPacket();
    }

    public static byte[] getCapturePosition(MapleMap map) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CAPTURE_POSITION);
        Point p1 = map.getPointOfItem(2910000);
        Point p2 = map.getPointOfItem(2910001);
		mpw.write(p1 == null ? 0 : 1);
        if (p1 != null) {
            mpw.writeInt(p1.x);
            mpw.writeInt(p1.y);
        }
        mpw.write(p2 == null ? 0 : 1);
        if (p2 != null) {
            mpw.writeInt(p2.x);
            mpw.writeInt(p2.y);
        }

        return mpw.getPacket();
    }

    public static byte[] resetCapture() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CAPTURE_RESET);

        return mpw.getPacket();
    }

    public static byte[] getMacros(SkillMacro[] macros) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SKILL_MACRO);
        int count = 0;
        for (int i = 0; i < 5; i++) {
            if (macros[i] != null) {
                count++;
            }
        }
        mpw.write(count);
        for (int i = 0; i < 5; i++) {
            SkillMacro macro = macros[i];
            if (macro != null) {
                mpw.writeMapleAsciiString(macro.getName());
                mpw.write(macro.getShout());
                mpw.writeInt(macro.getSkill1());
                mpw.writeInt(macro.getSkill2());
                mpw.writeInt(macro.getSkill3());
            }
        }

        return mpw.getPacket();
    }

    public static byte[] gameMsg(String msg) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GAME_MSG);
		mpw.writeAsciiString(msg);
        mpw.write(1);

        return mpw.getPacket();
    }

    public static byte[] innerPotentialMsg(String msg) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INNER_ABILITY_MSG);
		mpw.writeMapleAsciiString(msg);

        return mpw.getPacket();
    }

    public static byte[] updateInnerPotential(byte ability, int skill, int level, int rank) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ENABLE_INNER_ABILITY);
		mpw.write(1); //unlock
        mpw.write(1); //0 = no update
        mpw.writeShort(ability); //1-3
        mpw.writeInt(skill); //skill id (7000000+)
        mpw.writeShort(level); //level, 0 = blank inner ability
        mpw.writeShort(rank); //rank
        mpw.write(1); //0 = no update

        return mpw.getPacket();
    }

    public static byte[] innerPotentialResetMessage() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INNER_ABILITY_RESET_MSG);
		mpw.write(HexTool.getByteArrayFromHexString("26 00 49 6E 6E 65 72 20 50 6F 74 65 6E 74 69 61 6C 20 68 61 73 20 62 65 65 6E 20 72 65 63 6F 6E 66 69 67 75 72 65 64 2E 01"));

        return mpw.getPacket();
    }

    public static byte[] updateHonour(int honourLevel, int honourExp, boolean levelup) {
        /*
         * data:
         * 03 00 00 00
         * 69 00 00 00
         * 01
         */
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_HONOUR);
		mpw.writeInt(honourLevel);
        mpw.writeInt(honourExp);
        mpw.write(levelup ? 1 : 0); //shows level up effect

        return mpw.getPacket();
    }

    public static byte[] getCharInfo(MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WARP_TO_MAP);
		mpw.writeShort(2);
        mpw.writeLong(1L);
        mpw.writeLong(2L);
        mpw.writeInt(chr.getClient().getChannel() - 1);
        mpw.write(0); // bDev
        mpw.writeInt(0); // dwOldDriverId
        mpw.write(1); // bPopupDlg
        mpw.writeInt(0); // skip
        mpw.writeInt(0); // 45 05 00 00 nFieldWidth
        mpw.writeInt(0); // 49 03 00 00 nFieldHeight
        mpw.write(1); // bCharacterData
        mpw.writeShort(0);
        chr.CRand().connectData(mpw);
        PacketHelper.addCharacterInfo(mpw, chr);
        //PacketHelper.addLuckyLogoutInfo(mpw, false, null, null, null);
        mpw.writeZeroBytes(6); //lucky logout + another int
        mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mpw.writeInt(100);
        mpw.writeShort(0);
        mpw.write(1);
        mpw.writeZeroBytes(20);
        
        return mpw.getPacket();
    }

    public static byte[] getWarpToMap(MapleMap to, int spawnPoint, MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WARP_TO_MAP);
		mpw.writeShort(2);
        mpw.writeLong(1L);
        mpw.writeLong(2L);
        mpw.writeLong(chr.getClient().getChannel() - 1);
        mpw.write(0);
        mpw.write(2);//was8
        mpw.writeInt(0);
        mpw.writeInt(0); // 1298
        mpw.writeInt(0); // 330
        mpw.writeInt(0);
        mpw.writeInt(to.getId());
        mpw.write(spawnPoint);
        mpw.writeInt(chr.getStat().getHp());
        mpw.writeShort(0);
        mpw.write(0);
        mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mpw.writeInt(100);
        mpw.writeShort(0);//new143
        mpw.write(1);//new143
        mpw.writeZeroBytes(20);
       if (to.getFieldType().equals("63")) {
            mpw.write(0);
        }
       
       return mpw.getPacket();
    }

    public static byte[] removeBGLayer(boolean remove, int map, byte layer, int duration) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_BG_LAYER);
		mpw.write(remove ? 1 : 0); //Boolean show or remove
        mpw.writeInt(map);
        mpw.write(layer); //Layer to show/remove
        mpw.writeInt(duration);

        return mpw.getPacket();
    }

    public static byte[] setMapObjectVisible(List<Pair<String, Byte>> objects) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SET_MAP_OBJECT_VISIBLE);
		mpw.write(objects.size());
        for (Pair<String, Byte> object : objects) {
            mpw.writeMapleAsciiString(object.getLeft());
            mpw.write(object.getRight());
        }

        return mpw.getPacket();
    }

    public static byte[] spawnFlags(List<Pair<String, Integer>> flags) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CHANGE_BACKGROUND);
		mpw.write(flags == null ? 0 : flags.size());
        if (flags != null) {
            for (Pair f : flags) {
                mpw.writeMapleAsciiString((String) f.left);
                mpw.write(((Integer) f.right).intValue());
            }
        }

        return mpw.getPacket();
    }

    public static byte[] serverBlocked(int type) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SERVER_BLOCKED);
		mpw.write(type);

        return mpw.getPacket();
    }

    public static byte[] pvpBlocked(int type) {
        MaplePacketWriter mpw = new MaplePacketWriter();

        mpw.write(type);

        return mpw.getPacket();
    }

    public static byte[] showEquipEffect() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_EQUIP_EFFECT);

        return mpw.getPacket();
    }

    public static byte[] showEquipEffect(int team) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_EQUIP_EFFECT);
		mpw.writeShort(team);

        return mpw.getPacket();
    }

    public static byte[] multiChat(String name, String chattext, int mode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MULTICHAT);
		mpw.write(mode);
        mpw.writeMapleAsciiString(name);
        mpw.writeMapleAsciiString(chattext);

        return mpw.getPacket();
    }

    public static byte[] getFindReplyWithCS(String target, boolean buddy) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WHISPER);
		mpw.write(buddy ? 72 : 9);
        mpw.writeMapleAsciiString(target);
        mpw.write(2);
        mpw.writeInt(-1);

        return mpw.getPacket();
    }

    public static byte[] getWhisper(String sender, int channel, String text) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WHISPER);
		mpw.write(18);
        mpw.writeMapleAsciiString(sender);
        mpw.writeShort(channel - 1);
        mpw.writeMapleAsciiString(text);

        return mpw.getPacket();
    }

    public static byte[] getWhisperReply(String target, byte reply) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WHISPER);
		mpw.write(10);
        mpw.writeMapleAsciiString(target);
        mpw.write(reply);

        return mpw.getPacket();
    }

    public static byte[] getFindReplyWithMap(String target, int mapid, boolean buddy) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WHISPER);
		mpw.write(buddy ? 72 : 9);
        mpw.writeMapleAsciiString(target);
        mpw.write(3);//was1
        mpw.writeInt(mapid);//mapid);
//        mpw.writeZeroBytes(8);

        return mpw.getPacket();
    }

    public static byte[] getFindReply(String target, int channel, boolean buddy) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WHISPER);
		mpw.write(buddy ? 72 : 9);
        mpw.writeMapleAsciiString(target);
        mpw.write(3);
        mpw.writeInt(channel - 1);

        return mpw.getPacket();
    }

    public static byte[] MapEff(String path) {
        return environmentChange(path, 4);//was 3
    }

    public static byte[] MapNameDisplay(int mapid) {
        return environmentChange("maplemap/enter/" + mapid, 4);
    }

    public static byte[] Aran_Start() {
        return environmentChange("Aran/balloon", 4);
    }

    public static byte[] musicChange(String song) {
        return environmentChange(song, 7);//was 6
    }

    public static byte[] showEffect(String effect) {
        return environmentChange(effect, 4);//was 3
    }

    public static byte[] playSound(String sound) {
        return environmentChange(sound, 5);//was 4
    }

    public static byte[] environmentChange(String env, int mode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BOSS_ENV);
		mpw.write(mode);
        mpw.writeMapleAsciiString(env);
        mpw.writeInt(0);

        return mpw.getPacket();
    }

    public static byte[] trembleEffect(int type, int delay) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BOSS_ENV);
		mpw.write(1);
        mpw.write(type);
        mpw.writeInt(delay);
        mpw.writeShort(30);
        // mpw.writeInt(0);

        return mpw.getPacket();
    }

    public static byte[] environmentMove(String env, int mode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MOVE_ENV);
		mpw.writeMapleAsciiString(env);
        mpw.writeInt(mode);

        return mpw.getPacket();
    }

    public static byte[] getUpdateEnvironment(MapleMap map) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_ENV);
		mpw.writeInt(map.getEnvironment().size());
        for (Map.Entry mp : map.getEnvironment().entrySet()) {
            mpw.writeMapleAsciiString((String) mp.getKey());
            mpw.writeInt(((Integer) mp.getValue()).intValue());
        }

        return mpw.getPacket();
    }

    public static byte[] startMapEffect(String msg, int itemid, boolean active) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MAP_EFFECT);
        //mpw.write(active ? 0 : 1);

        mpw.writeInt(itemid);
        if (active) {
            mpw.writeMapleAsciiString(msg);
        }
        return mpw.getPacket();
    }

    public static byte[] removeMapEffect() {
        return startMapEffect(null, 0, false);
    }

    public static byte[] getGMEffect(int value, int mode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GM_EFFECT);
		mpw.write(value);
        mpw.writeZeroBytes(17);

        return mpw.getPacket();
    }

    public static byte[] showOXQuiz(int questionSet, int questionId, boolean askQuestion) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OX_QUIZ);
		mpw.write(askQuestion ? 1 : 0);
        mpw.write(questionSet);
        mpw.writeShort(questionId);

        return mpw.getPacket();
    }

    public static byte[] showEventInstructions() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GMEVENT_INSTRUCTIONS);
		mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] getPVPClock(int type, int time) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CLOCK);
		mpw.write(3);
        mpw.write(type);
        mpw.writeInt(time);

        return mpw.getPacket();
    }
    
        public static byte[] getBanBanClock(int time, int direction) {
        MaplePacketWriter outPacket = new MaplePacketWriter(SendPacketOpcode.CLOCK);
        outPacket.write(5);
        outPacket.write(direction); //0:?????? 1:????
        outPacket.writeInt(time);
        return outPacket.getPacket();
    }

    public static byte[] getClock(int time) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CLOCK);
		mpw.write(2);
        mpw.writeInt(time);

        return mpw.getPacket();
    }

    public static byte[] getClockTime(int hour, int min, int sec) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CLOCK);
		mpw.write(1);
        mpw.write(hour);
        mpw.write(min);
        mpw.write(sec);

        return mpw.getPacket();
    }

    public static byte[] boatPacket(int effect, int mode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BOAT_MOVE);
		mpw.write(effect);
        mpw.write(mode);

        return mpw.getPacket();
    }

    public static byte[] setBoatState(int effect) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BOAT_STATE);
		mpw.write(effect);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] stopClock() {
        return getPacketFromHexString(Integer.toHexString(SendPacketOpcode.STOP_CLOCK.getOpcode()) + " 00");
    }

    public static byte[] showAriantScoreBoard() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ARIANT_SCOREBOARD);

        return mpw.getPacket();
    }

    public static byte[] sendPyramidUpdate(int amount) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PYRAMID_UPDATE);
		mpw.writeInt(amount);

        return mpw.getPacket();
    }

    public static byte[] sendPyramidResult(byte rank, int amount) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PYRAMID_RESULT);
		mpw.write(rank);
        mpw.writeInt(amount);

        return mpw.getPacket();
    }

    public static byte[] quickSlot(String skil) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.QUICK_SLOT);
		mpw.write(skil == null ? 0 : 1);
        if (skil != null) {
            String[] slots = skil.split(",");
            for (int i = 0; i < 8; i++) {
                mpw.writeInt(Integer.parseInt(slots[i]));
            }
        }

        return mpw.getPacket();
    }

    public static byte[] getMovingPlatforms(MapleMap map) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MOVE_PLATFORM);
		mpw.writeInt(map.getPlatforms().size());
        for (MapleNodes.MaplePlatform mp : map.getPlatforms()) {
            mpw.writeMapleAsciiString(mp.name);
            mpw.writeInt(mp.start);
            mpw.writeInt(mp.SN.size());
            for (int x = 0; x < mp.SN.size(); x++) {
                mpw.writeInt((mp.SN.get(x)).intValue());
            }
            mpw.writeInt(mp.speed);
            mpw.writeInt(mp.x1);
            mpw.writeInt(mp.x2);
            mpw.writeInt(mp.y1);
            mpw.writeInt(mp.y2);
            mpw.writeInt(mp.x1);
            mpw.writeInt(mp.y1);
            mpw.writeShort(mp.r);
        }

        return mpw.getPacket();
    }

    public static byte[] sendPyramidKills(int amount) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PYRAMID_KILL_COUNT);
		mpw.writeInt(amount);

        return mpw.getPacket();
    }

    public static byte[] sendPVPMaps() {
        final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_INFO);
		mpw.write(3); //max amount of players
        for (int i = 0; i < 20; i++) {
            mpw.writeInt(10); //how many peoples in each map
        }
        mpw.writeZeroBytes(124);
        mpw.writeShort(150); ////PVP 1.5 EVENT!
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] gainForce(int oid, int count, int color) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
		mpw.write(1); // 0 = remote user?
        mpw.writeInt(oid);
        byte newcheck = 0;
        mpw.writeInt(newcheck); //unk
        if (newcheck > 0) {
            mpw.writeInt(0); //unk
            mpw.writeInt(0); //unk
        }
        mpw.write(0);
        mpw.writeInt(4); // size, for each below
        mpw.writeInt(count); //count
        mpw.writeInt(color); //color, 1-10 for demon, 1-2 for phantom
        mpw.writeInt(0); //unk
        mpw.writeInt(0); //unk
        return mpw.getPacket();
    }

    public static byte[] getAndroidTalkStyle(int npc, String talk, int... args) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
		mpw.write(4);
        mpw.writeInt(npc);
        mpw.writeShort(10);
        mpw.writeMapleAsciiString(talk);
        mpw.write(args.length);

        for (int i = 0; i < args.length; i++) {
            mpw.writeInt(args[i]);
        }
        return mpw.getPacket();
    }
    
    public static byte[] achievementRatio(int amount) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ACHIEVEMENT_RATIO);
		mpw.writeInt(amount);

        return mpw.getPacket();
    }

    public static byte[] getQuickMoveInfo(boolean show, List<QuickMoveNPC> qm) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.QUICK_MOVE);
		mpw.write(qm.size() <= 0 ? 0 : show ? qm.size() : 0);
        if (show && qm.size() > 0) {
            for (QuickMoveNPC qmn : qm) {
                mpw.writeInt(0);
                mpw.writeInt(qmn.getId());
                mpw.writeInt(qmn.getType());
                mpw.writeInt(qmn.getLevel());
                mpw.writeMapleAsciiString(qmn.getDescription());
                mpw.writeLong(PacketHelper.getTime(-2));
                mpw.writeLong(PacketHelper.getTime(-1));
            }
        }

        return mpw.getPacket();
    }
    
    public static byte[] differentIP(int minutesLeft) {
    	MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIFFERENT_IP);
		mpw.write(1); // Boolean
    	mpw.writeInt(minutesLeft);
    	
    	return mpw.getPacket();
    }

    /**
     * Note: To find MAX_BUFFSTAT, in an IDA go to 
     * CField::OnPacket -> CUserPool::OnUserEnterField -> 
     * CUserRemote::Init -> SecondaryStat::DecodeForRemote.
     * Find the first CInPacket::DecodeBuffer and the 
     * (field length) = MAX_BUFFSTAT * 4
     * @param chr
     * @return
     * 
     * @see CUserRemote::Init()
     */
    public static byte[] spawnPlayerMapobject(MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_PLAYER);
		mpw.writeInt(chr.getID());
        mpw.write(chr.getLevel());
        mpw.writeMapleAsciiString(chr.getName());
        MapleQuestStatus ultExplorer = chr.getQuestNoAdd(MapleQuest.getInstance(111111));
        if ((ultExplorer != null) && (ultExplorer.getCustomData() != null)) {
            mpw.writeMapleAsciiString(ultExplorer.getCustomData());
        } else {
            mpw.writeMapleAsciiString(""); // sParentName
        }
        if (chr.getGuildId() > 0) {
        	MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
        	if (gs != null) {
                mpw.writeMapleAsciiString(gs.getName());
                mpw.writeShort(gs.getLogoBG());
                mpw.write(gs.getLogoBGColor());
                mpw.writeShort(gs.getLogo());
                mpw.write(gs.getLogoColor());
            }
        } else {
        	mpw.writeZeroBytes(8);
        }
        mpw.write(chr.getGender());
        mpw.writeInt(chr.getFame());
        mpw.writeInt(chr.getClient().getFarm().getLevel());
        mpw.writeInt(0); // nNameTagMark 
//        
        final List<Pair<Integer, Integer>> buffvalue = new ArrayList<>();
        final List<Pair<Integer, Integer>> buffvaluenew = new ArrayList<>();
        int[] mask = new int[GameConstants.MAX_BUFFSTAT];
        
        mask[7] |= 0x1000000;
        mask[7] |= 0x800000;
        
        mask[10] |= 0x20;
        mask[10] |= 0x10;
        
        mask[12] |= 0x20000000;
        mask[12] |= 0x2000;
        mask[12] |= 0x800;
        mask[12] |= 0x40;
        
        mask[13] |= 0x100;
        mask[13] |= 0x1;
        
        mask[15] |= 0x10000000;
        mask[15] |= 0x8000000;
        mask[15] |= 0x4000000;
        mask[15] |= 0x2000000;
        mask[15] |= 0x1000000;
        mask[15] |= 0x800000;
        mask[15] |= 0x400000;
        mask[15] |= 0x200000;
        
        if ((chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null) || (chr.isHidden())) {
            mask[MapleBuffStat.DARKSIGHT.getPosition(true)] |= MapleBuffStat.DARKSIGHT.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
            mask[MapleBuffStat.SOULARROW.getPosition(true)] |= MapleBuffStat.SOULARROW.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.DAMAGE_ABSORBED) != null) {
            mask[MapleBuffStat.DAMAGE_ABSORBED.getPosition(true)] |= MapleBuffStat.DAMAGE_ABSORBED.getValue();
            buffvaluenew.add(new Pair(Integer.valueOf(1000), Integer.valueOf(2)));
            buffvaluenew.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.DAMAGE_ABSORBED)), Integer.valueOf(4)));
            buffvaluenew.add(new Pair(Integer.valueOf(9), Integer.valueOf(0)));
        }
        if (chr.getBuffedValue(MapleBuffStat.TEMPEST_BLADES) != null) {
            mask[MapleBuffStat.TEMPEST_BLADES.getPosition(true)] |= MapleBuffStat.TEMPEST_BLADES.getValue();
            buffvaluenew.add(new Pair(Integer.valueOf(chr.getTotalSkillLevel(chr.getTrueBuffSource(MapleBuffStat.TEMPEST_BLADES))), Integer.valueOf(2)));
            buffvaluenew.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.TEMPEST_BLADES)), Integer.valueOf(4)));
            buffvaluenew.add(new Pair(Integer.valueOf(5), Integer.valueOf(0)));
            buffvaluenew.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.TEMPEST_BLADES) == 61101002 ? 1 : 2), Integer.valueOf(4)));
            buffvaluenew.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.TEMPEST_BLADES) == 61101002 ? 3 : 5), Integer.valueOf(4)));
            buffvaluenew.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.TEMPEST_BLADES).intValue()), Integer.valueOf(4)));
            buffvaluenew.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.TEMPEST_BLADES) == 61101002 ? 3 : 5), Integer.valueOf(4)));
            if (chr.getTrueBuffSource(MapleBuffStat.TEMPEST_BLADES) != 61101002) {
                buffvaluenew.add(new Pair(Integer.valueOf(8), Integer.valueOf(0)));
            }
        }
        if ((chr.getBuffedValue(MapleBuffStat.COMBO) != null) && (chr.getBuffedValue(MapleBuffStat.TEMPEST_BLADES) == null)) {
            mask[MapleBuffStat.COMBO.getPosition(true)] |= MapleBuffStat.COMBO.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.COMBO).intValue()), Integer.valueOf(1)));
        }
        if (chr.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) {
            mask[MapleBuffStat.WK_CHARGE.getPosition(true)] |= MapleBuffStat.WK_CHARGE.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.WK_CHARGE).intValue()), Integer.valueOf(2)));
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffSource(MapleBuffStat.WK_CHARGE)), Integer.valueOf(3)));
        }
        if ((chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) && (chr.getBuffedValue(MapleBuffStat.TEMPEST_BLADES) == null)) {
            mask[MapleBuffStat.SHADOWPARTNER.getPosition(true)] |= MapleBuffStat.SHADOWPARTNER.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER).intValue()), Integer.valueOf(2)));
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffSource(MapleBuffStat.SHADOWPARTNER)), Integer.valueOf(3)));
        }
        //if ((chr.getBuffedValue(MapleBuffStat.MORPH) != null) && (chr.getBuffedValue(MapleBuffStat.TEMPEST_BLADES) == null)) {//TODO
        //    mask[MapleBuffStat.MORPH.getPosition(true)] |= MapleBuffStat.MORPH.getValue();
        //    buffvalue.add(new Pair(Integer.valueOf(chr.getStatForBuff(MapleBuffStat.MORPH).getMorph(chr)), Integer.valueOf(2)));
        //    buffvalue.add(new Pair(Integer.valueOf(chr.getBuffSource(MapleBuffStat.MORPH)), Integer.valueOf(3)));
        //}
        if (chr.getBuffedValue(MapleBuffStat.BERSERK_FURY) != null) {//works
            mask[MapleBuffStat.BERSERK_FURY.getPosition(true)] |= MapleBuffStat.BERSERK_FURY.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.DIVINE_BODY) != null) {
            mask[MapleBuffStat.DIVINE_BODY.getPosition(true)] |= MapleBuffStat.DIVINE_BODY.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.WIND_WALK) != null) {//TODO better
            mask[MapleBuffStat.WIND_WALK.getPosition(true)] |= MapleBuffStat.WIND_WALK.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.WIND_WALK).intValue()), Integer.valueOf(2)));
            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.WIND_WALK)), Integer.valueOf(3)));
        }
        if (chr.getBuffedValue(MapleBuffStat.PYRAMID_PQ) != null) {//TODO
            mask[MapleBuffStat.PYRAMID_PQ.getPosition(true)] |= MapleBuffStat.PYRAMID_PQ.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.PYRAMID_PQ).intValue()), Integer.valueOf(2)));
            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.PYRAMID_PQ)), Integer.valueOf(3)));
        }
        if (chr.getBuffedValue(MapleBuffStat.SOARING) != null) {//TODO
            mask[MapleBuffStat.SOARING.getPosition(true)] |= MapleBuffStat.SOARING.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.SOARING).intValue()), Integer.valueOf(1)));
        }
//        if (chr.getBuffedValue(MapleBuffStat.OWL_SPIRIT) != null) {//TODO
//            mask[MapleBuffStat.OWL_SPIRIT.getPosition(true)] |= MapleBuffStat.OWL_SPIRIT.getValue();
//            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.OWL_SPIRIT).intValue()), Integer.valueOf(2)));
//            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.OWL_SPIRIT)), Integer.valueOf(3)));
//        }
        if (chr.getBuffedValue(MapleBuffStat.FINAL_CUT) != null) {
            mask[MapleBuffStat.FINAL_CUT.getPosition(true)] |= MapleBuffStat.FINAL_CUT.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.FINAL_CUT).intValue()), Integer.valueOf(2)));
            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.FINAL_CUT)), Integer.valueOf(3)));
        }

        if (chr.getBuffedValue(MapleBuffStat.TORNADO) != null) {
            mask[MapleBuffStat.TORNADO.getPosition(true)] |= MapleBuffStat.TORNADO.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.TORNADO).intValue()), Integer.valueOf(2)));
            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.TORNADO)), Integer.valueOf(3)));
        }
        if (chr.getBuffedValue(MapleBuffStat.INFILTRATE) != null) {
            mask[MapleBuffStat.INFILTRATE.getPosition(true)] |= MapleBuffStat.INFILTRATE.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.MECH_CHANGE) != null) {
            mask[MapleBuffStat.MECH_CHANGE.getPosition(true)] |= MapleBuffStat.MECH_CHANGE.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.MECH_CHANGE).intValue()), Integer.valueOf(2)));
            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.MECH_CHANGE)), Integer.valueOf(3)));
        }
        if (chr.getBuffedValue(MapleBuffStat.DARK_AURA) != null) {
            mask[MapleBuffStat.DARK_AURA.getPosition(true)] |= MapleBuffStat.DARK_AURA.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.DARK_AURA).intValue()), Integer.valueOf(2)));
            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.DARK_AURA)), Integer.valueOf(3)));
        }
        if (chr.getBuffedValue(MapleBuffStat.BLUE_AURA) != null) {
            mask[MapleBuffStat.BLUE_AURA.getPosition(true)] |= MapleBuffStat.BLUE_AURA.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.BLUE_AURA).intValue()), Integer.valueOf(2)));
            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.BLUE_AURA)), Integer.valueOf(3)));
        }
        if (chr.getBuffedValue(MapleBuffStat.YELLOW_AURA) != null) {
            mask[MapleBuffStat.YELLOW_AURA.getPosition(true)] |= MapleBuffStat.YELLOW_AURA.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.YELLOW_AURA).intValue()), Integer.valueOf(2)));
            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.YELLOW_AURA)), Integer.valueOf(3)));
        }
        if ((chr.getBuffedValue(MapleBuffStat.WATER_SHIELD) != null) && (chr.getBuffedValue(MapleBuffStat.TEMPEST_BLADES) == null)) {
            mask[MapleBuffStat.WATER_SHIELD.getPosition(true)] |= MapleBuffStat.WATER_SHIELD.getValue();
            buffvaluenew.add(new Pair(Integer.valueOf(chr.getTotalSkillLevel(chr.getTrueBuffSource(MapleBuffStat.WATER_SHIELD))), Integer.valueOf(2)));
            buffvaluenew.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.WATER_SHIELD)), Integer.valueOf(4)));
            buffvaluenew.add(new Pair(Integer.valueOf(9), Integer.valueOf(0)));
        }
        if (chr.getBuffedValue(MapleBuffStat.GIANT_POTION) != null) {
            mask[MapleBuffStat.GIANT_POTION.getPosition(true)] |= MapleBuffStat.GIANT_POTION.getValue();
            buffvalue.add(new Pair(Integer.valueOf(chr.getBuffedValue(MapleBuffStat.GIANT_POTION).intValue()), Integer.valueOf(2)));
            buffvalue.add(new Pair(Integer.valueOf(chr.getTrueBuffSource(MapleBuffStat.GIANT_POTION)), Integer.valueOf(3)));
        }

        for (int i = 0; i < mask.length; i++) {
            mpw.writeInt(mask[i]);
        }
        mpw.writeInt(-1);
        
        mpw.write(0);
        for (Pair i : buffvalue) {
            if (((Integer) i.right).intValue() == 3) {
                mpw.writeInt(((Integer) i.left).intValue());
            } else if (((Integer) i.right).intValue() == 2) {
                mpw.writeShort(((Integer) i.left).shortValue());
            } else if (((Integer) i.right).intValue() == 1) {
                mpw.write(((Integer) i.left).byteValue());
            }
        }
        if (buffvaluenew.isEmpty()) {
            mpw.writeZeroBytes(10);
        } else {
            mpw.write(0);
            for (Pair i : buffvaluenew) {
                if (((Integer) i.right).intValue() == 4) {
                    mpw.writeInt(((Integer) i.left).intValue());
                } else if (((Integer) i.right).intValue() == 2) {
                    mpw.writeShort(((Integer) i.left).shortValue());
                } else if (((Integer) i.right).intValue() == 1) {
                    mpw.write(((Integer) i.left).byteValue());
                } else if (((Integer) i.right).intValue() == 0) {
                    mpw.writeZeroBytes(((Integer) i.left).intValue());
                }
            }
        }
        mpw.writeZeroBytes(72); // v174

        int charMagicSpawn = Randomizer.nextInt();
        mpw.write(1);
        mpw.writeInt(charMagicSpawn);
        mpw.writeLong(0); //v143 10->8
        mpw.write(1);
        mpw.writeInt(charMagicSpawn);
        mpw.writeZeroBytes(10);
        mpw.write(1);
        mpw.writeInt(charMagicSpawn);
        mpw.writeShort(0);
        int buffSrc = chr.getBuffSource(MapleBuffStat.MONSTER_RIDING);
        if (buffSrc > 0) {
            Item c_mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -118);
            Item mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18);
            if ((GameConstants.getMountItem(buffSrc, chr) == 0) && (c_mount != null) && (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -119) != null)) {
                mpw.writeInt(c_mount.getItemId());
            } else if ((GameConstants.getMountItem(buffSrc, chr) == 0) && (mount != null) && (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -19) != null)) {
                mpw.writeInt(mount.getItemId());
            } else {
                mpw.writeInt(GameConstants.getMountItem(buffSrc, chr));
            }
            mpw.writeInt(buffSrc);
        } else {
            mpw.writeLong(0L);
        }
        mpw.write(1);
        mpw.writeInt(charMagicSpawn);
        mpw.writeLong(0L);
        mpw.write(1);
        mpw.writeInt(charMagicSpawn);
        mpw.write(GameConstants.isZero(chr.getJob()) || GameConstants.isEvan(chr.getJob()) ? 0 : 1); //Shows the dragon in inventory it seems
        mpw.writeInt(0); // Maybe CRC
        mpw.writeZeroBytes(10);// There must be something in here... But what?...
        mpw.write(1);
        mpw.writeInt(charMagicSpawn);
        mpw.writeZeroBytes(16);
        mpw.write(1);
        mpw.writeInt(charMagicSpawn);
        mpw.writeZeroBytes(10);
        mpw.write(1);
        mpw.writeInt(charMagicSpawn);
        mpw.writeShort(0);
        
        mpw.writeShort(chr.getJob());
        mpw.writeShort(chr.getSubcategory());
        mpw.writeInt(0); // nTotalCHUC
        PacketHelper.addCharLook(mpw, chr, true, false);
        if (GameConstants.isZero(chr.getJob())) {
            PacketHelper.addCharLook(mpw, chr, true, false);
        }

        mpw.writeInt(0); // dwDriverID
        mpw.writeInt(0); // dwPassengerID 

        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        
        mpw.writeInt(Math.min(250, chr.getInventory(MapleInventoryType.CASH).countById(5110000))); //Valentine Effect
        mpw.writeInt(chr.getItemEffect()); // nActiveEffectItemID
        mpw.writeInt(0); // nMonkeyEffectItemID

        
        MapleQuestStatus stat = chr.getQuestNoAdd(MapleQuest.getInstance(124000));
        mpw.writeInt(stat != null && stat.getCustomData() != null ? Integer.parseInt(stat.getCustomData()) : 0); //title //head title? chr.getHeadTitle()
        mpw.writeInt(chr.getDamageSkin()); // nDamageSkin
        mpw.writeInt(chr.getDamageSkin()); // ptPos; repeated twice strangely, same value as damage skin
        mpw.writeInt(0); // nDemonWingID
        mpw.writeInt(0); // nKaiserWingID
        mpw.writeInt(0); // nKaiserTailID
        mpw.writeInt(0); // nCompleteSetID
        mpw.writeShort(-1); // nFieldSeatID
        mpw.writeInt(GameConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP ? chr.getChair() : 0); // nPortableChairID
        int check = 0;
        mpw.writeInt(check); // check
        if (check != 0) {
        	mpw.writeMapleAsciiString(""); // sPortableChairMsg
        }
        mpw.writeInt(0);
        mpw.writeInt(0);
        
        mpw.writePos(chr.getTruePosition());
        mpw.write(chr.getStance());
        mpw.writeShort(chr.getFH());
        mpw.writeZeroBytes(3);

        mpw.write(1);
        mpw.write(0);
        
        mpw.writeInt(chr.getMount().getLevel());
        mpw.writeInt(chr.getMount().getExp());
        mpw.writeInt(chr.getMount().getFatigue());

        PacketHelper.addAnnounceBox(mpw, chr);
        mpw.write((chr.getChalkboard() != null) && (chr.getChalkboard().length() > 0) ? 1 : 0);
        
       /* if (GameConstants.isKaiser(chr.getJob())) { //doesn't do shit?
            mpw.writeShort(0);
            mpw.write(0);
            mpw.writeInt(1);
            mpw.writeShort(0);
        }*/
        
        if ((chr.getChalkboard() != null) && (chr.getChalkboard().length() > 0)) {
            mpw.writeMapleAsciiString(chr.getChalkboard());
        }

        Triple rings = chr.getRings(false);
        addRingInfo(mpw, (List) rings.getLeft());
        addRingInfo(mpw, (List) rings.getMid());
        addMRingInfo(mpw, (List) rings.getRight(), chr);

        mpw.write(chr.getStat().Berserk ? 1 : 0); //mask
        mpw.writeInt(0); // nEvanDragonGlide_Riding

        if (GameConstants.isKaiser(chr.getJob())) {
            String x = chr.getOneInfo(12860, "extern");
            mpw.writeInt(x == null ? 0 : Integer.parseInt(x));
            x = chr.getOneInfo(12860, "inner");
            mpw.writeInt(x == null ? 0 : Integer.parseInt(x));
            x = chr.getOneInfo(12860, "primium");
            mpw.write(x == null ? 0 : Integer.parseInt(x));
        }

        mpw.writeInt(0); // nSkillID for CUser::SetMakingMeisterSkillEff

        PacketHelper.addFarmInfo(mpw, chr.getClient(), 0);
        for (int i = 0; i < 5; i++) {
            mpw.write(-1); // aActiveEventNameTag
        }

        int customizeEffectItemID = 0;
        mpw.writeInt(customizeEffectItemID); // nItemID for CUser::SetCustomizeEffect
        if (customizeEffectItemID > 0) {
        	mpw.writeMapleAsciiString(""); // sEffectInfo
        }
        mpw.write(1); // bSoulEffect
        
        // CUser::SetFlareBlink Start
        int unkBool2 = 0;
        mpw.write(unkBool2); // unk check
        if (unkBool2 > 0) {
        	int unkBool3 = 0;
        	mpw.write(unkBool3);
        	if (unkBool3 > 0) {
        		mpw.writeInt(0); // nSkillLV
        		mpw.writeInt(0); // skip
        		mpw.writePos(null); // ptPos
        	}
        }
        // CUser::SetFlareBlink End
        
        // CUser::StarPlanetRank::Decode Start
        int result = 0;
        mpw.write(result);
        if (result > 0) {
        	mpw.writeInt(0); // nRoundID
        	int round = 0;
        	mpw.write(round); 
        	if (round >= 10) {
        		for (int i = 0; i < 10; i++) {
        			mpw.writeInt(0); 
        			mpw.writeInt(0); // nRanking
        			mpw.writeInt(0); // time?
        		}
        	} else {
        		mpw.writeInt(0); // anPoint
        		mpw.writeInt(0); // anRanking
        		mpw.writeInt(0); // atLastCheckRank time
        	}
        	mpw.writeLong(0); // ftShiningStarExpiredTime
        	mpw.writeInt(0); // nShiningStarPickedCount
        	mpw.writeInt(0); // nRoundStarPoint
        }
        // CUser::StarPlanetRank::Decode End

        // CUser::DecodeStarPlanetTrendShopLook Start
        mpw.writeInt(0);
        mpw.writeInt(0); // suppose to be in a while loop
        // CUser::DecodeStarPlanetTrendShopLook End
        
        mpw.writeInt(0); // CUser:DecodeTextEquipInfo
        
        // FreezeAndHotEventInfo::Decode Start
        mpw.write(0); // nAccountType
        mpw.writeInt(0); // dwAccountID
        // FreezeAndHotEventInfo::Decode Start
        
        mpw.write(0); // bOnOff for CUserRemote::OnKinesisPsychicEnergyShieldEffect()
        mpw.write(1); // bBeastFormWingOnOff
        mpw.writeInt(0); // nMeso for CUser::SetMesoChairCount
        mpw.writeInt(1);
        mpw.writeInt(0);
        mpw.writeMapleAsciiString("");
        mpw.writeInt(0);
        
        int unkBool = 0;
        mpw.write(unkBool);
        if (unkBool > 0) {
        	int unkInt = 0;
        	mpw.writeInt(unkInt);
        	if (unkInt > 0) {
        		for (int i = 0; i < unkInt; i++) {
        			mpw.writeInt(0);
        		}
        	}
        }
        
        int v188 = 0;
        mpw.writeInt(v188);
        if (v188 > 0) {
        	mpw.writeInt(0);
        	mpw.writeInt(0);
        	mpw.writeInt(0);
        	mpw.writeShort(0);
        	mpw.writeShort(0);
        }
        
        mpw.writeInt(0);
        
        result = 0;
        mpw.writeInt(result);
        if (result > 0) {
        	for (int i = 0; i < result; i++) {
        		mpw.writeInt(0);
        	}
        }
        return mpw.getPacket();
    }

    public static byte[] removePlayerFromMap(int charid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_PLAYER_FROM_MAP);
		mpw.writeInt(charid);

        return mpw.getPacket();
    } 

    public static byte[] showNebuliteEffect(int chr, boolean success) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_NEBULITE_EFFECT);
		mpw.writeInt(chr);
        mpw.write(success ? 1 : 0);
        mpw.writeMapleAsciiString(success ? "Successfully mounted Nebulite." : "Failed to mount Nebulite.");

        return mpw.getPacket();
    }

    public static byte[] useNebuliteFusion(int cid, int itemId, boolean success) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_FUSION_EFFECT);
		mpw.writeInt(cid);
        mpw.write(success ? 1 : 0);
        mpw.writeInt(itemId);

        return mpw.getPacket();
    }

    public static byte[] pvpAttack(int cid, int playerLevel, int skill, int skillLevel, int speed, int mastery, int projectile, int attackCount, int chargeTime, int stance, int direction, int range, int linkSkill, int linkSkillLevel, boolean movementSkill, boolean pushTarget, boolean pullTarget, List<AttackPair> attack) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_ATTACK);
		mpw.writeInt(cid);
        mpw.write(playerLevel);
        mpw.writeInt(skill);
        mpw.write(skillLevel);
        mpw.writeInt(linkSkill != skill ? linkSkill : 0);
        mpw.write(linkSkillLevel != skillLevel ? linkSkillLevel : 0);
        mpw.write(direction);
        mpw.write(movementSkill ? 1 : 0);
        mpw.write(pushTarget ? 1 : 0);
        mpw.write(pullTarget ? 1 : 0);
        mpw.write(0);
        mpw.writeShort(stance);
        mpw.write(speed);
        mpw.write(mastery);
        mpw.writeInt(projectile);
        mpw.writeInt(chargeTime);
        mpw.writeInt(range);
        mpw.write(attack.size());
        mpw.write(0);
        mpw.writeInt(0);
        mpw.write(attackCount);
        mpw.write(0);
        for (AttackPair p : attack) {
            mpw.writeInt(p.objectid);
            mpw.writeInt(0);
            mpw.writePos(p.point);
            mpw.write(0);
            mpw.writeInt(0);
            for (Pair atk : p.attack) {
                mpw.writeInt(((Integer) atk.left).intValue());
                mpw.writeInt(0);
                mpw.write(((Boolean) atk.right).booleanValue() ? 1 : 0);
                mpw.writeShort(0);
            }
        }

        return mpw.getPacket();
    }

    public static byte[] getPVPMist(int cid, int mistSkill, int mistLevel, int damage) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_MIST);
		mpw.writeInt(cid);
        mpw.writeInt(mistSkill);
        mpw.write(mistLevel);
        mpw.writeInt(damage);
        mpw.write(8);
        mpw.writeInt(1000);

        return mpw.getPacket();
    }

    public static byte[] pvpCool(int cid, List<Integer> attack) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_COOL);
		mpw.writeInt(cid);
        mpw.write(attack.size());
        for (Iterator i$ = attack.iterator(); i$.hasNext();) {
            int b = ((Integer) i$.next()).intValue();
            mpw.writeInt(b);
        }

        return mpw.getPacket();
    }

    public static byte[] teslaTriangle(int cid, int sum1, int sum2, int sum3) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.TESLA_TRIANGLE);
		mpw.writeInt(cid);
        mpw.writeInt(sum1);
        mpw.writeInt(sum2);
        mpw.writeInt(sum3);

         mpw.writeZeroBytes(69);//test
        
        return mpw.getPacket();
    }

    public static byte[] followEffect(int initiator, int replier, Point toMap) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FOLLOW_CHARACTER);
		mpw.writeInt(initiator);
        mpw.writeInt(replier);
        mpw.writeLong(0);
        if (replier == 0) {
            mpw.write(toMap == null ? 0 : 1);
            if (toMap != null) {
                mpw.writeInt(toMap.x);
                mpw.writeInt(toMap.y);
            }
        }

        return mpw.getPacket();
    }

    public static byte[] showPQReward(int cid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_PQ_REWARD);
		mpw.writeInt(cid);
        for (int i = 0; i < 6; i++) {
            mpw.write(0);
        }

        return mpw.getPacket();
    }

    public static byte[] playerDamaged(int cid, int dmg) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_DAMAGED);
		mpw.writeInt(cid);
        mpw.writeInt(dmg);

        return mpw.getPacket();
    }

    public static byte[] showPyramidEffect(int chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NETT_PYRAMID);
		mpw.writeInt(chr);
        mpw.write(1);
        mpw.writeInt(0);
        mpw.writeInt(0);

        return mpw.getPacket();
    }

    public static byte[] pamsSongEffect(int cid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PAMS_SONG);
		mpw.writeInt(cid);
        return mpw.getPacket();
    }

    /**
     * @see CFoxMan::OnShowChangeEffect()
     */
    public static byte[] transformHakuEffect(int charID) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.HAKU_TRANSFORM_EFFECT);
		mpw.writeInt(charID);

        return mpw.getPacket();
    }

    /*
    public static byte[] spawnHaku_change1(MapleHaku d) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.HAKU_CHANGE_1);
		mpw.writeInt(d.getOwner());
        mpw.writePos(d.getPosition());
        mpw.write(d.getStance());
        mpw.writeShort(0);
        mpw.write(0);
        mpw.writeInt(0);

        return mpw.getPacket();
    }

    public static byte[] spawnHaku_bianshen(int cid, int oid, boolean change) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.HAKU_CHANGE);
		mpw.writeInt(cid);
        mpw.writeInt(oid);
        mpw.write(change ? 2 : 1);

        return mpw.getPacket();
    }

    public static byte[] hakuUnk(int cid, int oid, boolean change) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.HAKU_CHANGE);
		mpw.writeInt(cid);
        mpw.writeInt(oid);
        mpw.write(0);
        mpw.write(0);
        mpw.writeMapleAsciiString("lol");

        return mpw.getPacket();
    }
    */

    public static byte[] spawnHaku(MapleHaku d) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_HAKU);
		mpw.writeInt(d.getOwner());
        mpw.writeInt(d.getObjectId());
        mpw.writeInt(40020109);
        mpw.write(1);
        mpw.writePos(d.getPosition());
        mpw.write(0);
        mpw.writeShort(d.getStance());

        return mpw.getPacket();
    }

    public static byte[] moveHaku(int cid, int oid, Point pos, List<LifeMovementFragment> res) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.HAKU_MOVE);
		mpw.writeInt(cid);
        mpw.writeInt(oid);
        mpw.writeInt(0);
        mpw.writePos(pos);
        mpw.writeInt(0);
        PacketHelper.serializeMovementList(mpw, res);
        return mpw.getPacket();
    }

    public static byte[] removeFamiliar(int cid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_FAMILIAR);
		mpw.writeInt(cid);
        mpw.writeShort(0);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] spawnFamiliar(MonsterFamiliar mf, boolean spawn, boolean respawn) {
        MaplePacketWriter mpw = new MaplePacketWriter(respawn ? SendPacketOpcode.RESPAWN_FAMILIAR : SendPacketOpcode.SPAWN_FAMILIAR);
		mpw.writeInt(mf.getCharacterId());
        mpw.write(spawn ? 1 : 0);
        mpw.write(respawn ? 1 : 0);
        mpw.write(0);
        if (spawn) {
            mpw.writeInt(mf.getFamiliar());
            mpw.writeInt(mf.getFatigue());
            mpw.writeInt(mf.getVitality() * 300); //max fatigue
            mpw.writeMapleAsciiString(mf.getName());
            mpw.writePos(mf.getTruePosition());
            mpw.write(mf.getStance());
            mpw.writeShort(mf.getFh());
        }

        return mpw.getPacket();
    }

    public static byte[] moveFamiliar(int cid, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MOVE_FAMILIAR);
		mpw.writeInt(cid);
        mpw.write(0);
        mpw.writePos(startPos);
        mpw.writeInt(0);
        PacketHelper.serializeMovementList(mpw, moves);

        return mpw.getPacket();
    }

    public static byte[] touchFamiliar(int cid, byte unk, int objectid, int type, int delay, int damage) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.TOUCH_FAMILIAR);
		mpw.writeInt(cid);
        mpw.write(0);
        mpw.write(unk);
        mpw.writeInt(objectid);
        mpw.writeInt(type);
        mpw.writeInt(delay);
        mpw.writeInt(damage);

        return mpw.getPacket();
    }

    public static byte[] familiarAttack(int cid, byte unk, List<Triple<Integer, Integer, List<Integer>>> attackPair) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ATTACK_FAMILIAR);
		mpw.writeInt(cid);
        mpw.write(0);// familiar id?
        mpw.write(unk);
        mpw.write(attackPair.size());
        for (Triple<Integer, Integer, List<Integer>> s : attackPair) {
            mpw.writeInt(s.left);
            mpw.write(s.mid);
            mpw.write(s.right.size());
            for (int damage : s.right) {
                mpw.writeInt(damage);
            }
        }

        return mpw.getPacket();
    }

    public static byte[] renameFamiliar(MonsterFamiliar mf) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.RENAME_FAMILIAR);
		mpw.writeInt(mf.getCharacterId());
        mpw.write(0);
        mpw.writeInt(mf.getFamiliar());
        mpw.writeMapleAsciiString(mf.getName());

        return mpw.getPacket();
    }

    public static byte[] updateFamiliar(MonsterFamiliar mf) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_FAMILIAR);
		mpw.writeInt(mf.getCharacterId());
        mpw.writeInt(mf.getFamiliar());
        mpw.writeInt(mf.getFatigue());
        mpw.writeLong(PacketHelper.getTime(mf.getVitality() >= 3 ? System.currentTimeMillis() : -2L));

        return mpw.getPacket();
    }

    public static byte[] movePlayer(int charID, List<LifeMovementFragment> moves, Point startPos) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MOVE_PLAYER);
		mpw.writeInt(charID);
        mpw.writeInt(0);
        mpw.writePos(startPos);
        mpw.writeShort(0);
        mpw.writeShort(0);
        PacketHelper.serializeMovementList(mpw, moves);

        return mpw.getPacket();
    }

    public static byte[] closeRangeAttack(int charID, AttackInfo attack, short charLevel, byte mastery, boolean energy) {
    	return addAttackInfo(energy ? 4 : 0, charID, attack, charLevel, mastery);
    }
    
    public static byte[] rangedAttack(int charID, AttackInfo attack, short charLevel, byte mastery) {
		return addAttackInfo(1, charID, attack, charLevel, mastery);
	}
    
    public static byte[] strafeAttack(int charID, AttackInfo attack, short charLevel, byte mastery) {
		return addAttackInfo(2, charID, attack, charLevel, mastery);
	}
    
    public static byte[] magicAttack(int charID, AttackInfo attack, short charLevel, byte mastery) {
        return addAttackInfo(3, charID, attack, charLevel, mastery);
    }

    public static byte[] addAttackInfo(int type, int charID, AttackInfo attack, short charLevel, byte mastery) {
    	MaplePacketWriter mpw;
    	if (type == 0) {
    		mpw = new MaplePacketWriter(SendPacketOpcode.CLOSE_RANGE_ATTACK);
        } else if (type == 1 || type == 2) {
        	mpw = new MaplePacketWriter(SendPacketOpcode.RANGED_ATTACK);
        } else if (type == 3) {
        	mpw = new MaplePacketWriter(SendPacketOpcode.MAGIC_ATTACK);
        } else {
        	mpw = new MaplePacketWriter(SendPacketOpcode.ENERGY_ATTACK);
        }

    	final int skillLevel = attack.skillLevel, skillid = attack.skillid;
    	final byte unk = attack.unk;

        mpw.writeInt(charID);
        mpw.write(0);
        mpw.write(attack.tbyte);
        mpw.write(charLevel);
        
        if (skillLevel > 0) {
        	mpw.write(skillLevel);
            mpw.writeInt(skillid);
        } else {
        	mpw.write(0);
        }

        if (GameConstants.isZero(skillid / 10000) && skillid != 100001283) {
            short zero1 = 0;
            short zero2 = 0;
            mpw.write(zero1 > 0 || zero2 > 0); //boolean
            if (zero1 > 0 || zero2 > 0) {
                mpw.writeShort(zero1);
                mpw.writeShort(zero2);
                //there is a full handler so better not write zero
            }
        }

        /*
        if (type == 1 || type == 2) { // if RANGED_ATTACK  (Got this from the IDA but it's wrong?)
            mpw.write(ultLevel);
            if (ultLevel > 0) {
                mpw.writeInt(3220010);
            }
        }
        */
        
        if (skillid == 4121013) {  // if the skill can be affect by a hyper stat
        	mpw.write(attack.hyperLevel); // Hyper Skill Boolean/Level
        	if (attack.hyperLevel > 0) {
        		mpw.writeInt(0); // Hyper Skill ID
        	}
        }

        if (skillid == 80001850) {
        	mpw.write(skillLevel);
        	if (skillLevel > 0) {
        		mpw.writeInt(skillid);
        	}
        }
        
        mpw.write(attack.flag); // some flag
        mpw.write(unk); // flag
        mpw.writeInt(0); // nOption3 or nBySummonedID
        mpw.writeInt(0);
        
        if ((unk & 2) != 0) {
        	mpw.writeInt(skillid); // buckShotInfo.nSkillID
        	mpw.writeInt(skillLevel); // buckShotInfo.nSkillLV
        }
        
        if ((unk & 8) != 0) {
        	mpw.write(0); // nPassiveAddAttackCount
        }
        
        /*if (skillid == 40021185 || skillid == 42001006) {
            mpw.write(0); //boolean if true then int
        }*/

        
        mpw.writeShort(attack.display);
        mpw.writeInt(0);
        mpw.writeShort(0);
        mpw.write(0);
        mpw.write(attack.speed);
        mpw.write(mastery);
        mpw.writeInt(attack.projectileItemID > 0 ? attack.projectileItemID : 0); // Throwing Star/Bullet ID
        
        for (AttackPair oned : attack.allDamage) {
            if (oned.attack != null) {
                mpw.writeInt(oned.objectid);
                mpw.write(oned.unknownByte != 0 ? oned.unknownByte : 7);
                mpw.write(oned.unknownBool1); // some boolean
                mpw.write(oned.unknownBool2); // some boolean
                mpw.writeShort(oned.unknownShort != 0 ? oned.unknownShort : 256); // ??
                if (skillid == 42111002) {
                    mpw.write(oned.attack.size());
                    for (Pair eachd : oned.attack) {
                        mpw.writeInt(((Integer) eachd.left).intValue());
                    }
                } else {
                    for (Pair eachd : oned.attack) {
                        mpw.write(((Boolean) eachd.right).booleanValue() ? 1 : 0); // Show critical if true
                        mpw.writeInt(((Integer) eachd.left).intValue());
                    }
                }
            }
        }
        if (skillid == 2321001 || skillid == 2221052 || skillid == 11121052) {
            mpw.writeInt(0);
        } else if (skillid == 65121052 || skillid == 101000202 || skillid == 101000102) {
            mpw.writeInt(0);
            mpw.writeInt(0);
        }
        if (skillid == 42100007) {
            mpw.writeShort(0);
            mpw.write(0);
        }
        /*if (type == 1 || type == 2) {
            mpw.writePos(pos);
        } else */
        if (type == 3 && attack.charge > 0) {
            mpw.writeInt(attack.charge);
        }
        if (skillid == 5321000
                || skillid == 5311001
                || skillid == 5321001
                || skillid == 5011002
                || skillid == 5311002
                || skillid == 5221013
                || skillid == 5221017
                || skillid == 3120019
                || skillid == 3121015
                || skillid == 4121017) {
            mpw.writePos(attack.position);
        }

        return mpw.getPacket();
	}
    
    public static byte[] closeRangeAttack(int cid, int tbyte, int skill, int skillLevel, int display, byte speed, List<AttackPair> damage, boolean energy, int charLevel, byte mastery, byte unk, int charge) {
        return addAttackInfo(energy ? 4 : 0, cid, tbyte, skill, skillLevel, display, speed, damage, charLevel, mastery, unk, 0, null, 0);
    }

    public static byte[] rangedAttack(int cid, byte tbyte, int skill, int skillLevel, int display, byte speed, int itemid, List<AttackPair> damage, Point pos, int charLevel, byte mastery, byte unk) {
        return addAttackInfo(1, cid, tbyte, skill, skillLevel, display, speed, damage, charLevel, mastery, unk, itemid, pos, 0);
    }

	public static byte[] strafeAttack(int cid, byte tbyte, int skill, int skillLevel, int display, byte speed, int itemid, List<AttackPair> damage, Point pos, int charLevel, byte mastery, byte unk, int ultLevel) {
        return addAttackInfo(2, cid, tbyte, skill, skillLevel, display, speed, damage, charLevel, mastery, unk, itemid, pos, ultLevel);
    }

    public static byte[] magicAttack(int cid, int tbyte, int skill, int skillLevel, int display, byte speed, List<AttackPair> damage, int charge, int charLevel, byte unk) {
        return addAttackInfo(3, cid, tbyte, skill, skillLevel, display, speed, damage, charLevel, (byte) 0, unk, charge, null, 0);
    }

    public static byte[] addAttackInfo(int type, int cid, int tbyte, int skillid, int skillLevel, int display, byte speed, List<AttackPair> damage, int charLevel, byte mastery, byte unk, int charge, Point pos, int ultLevel) {
    	MaplePacketWriter mpw;
    	if (type == 0) {
    		mpw = new MaplePacketWriter(SendPacketOpcode.CLOSE_RANGE_ATTACK);
        } else if (type == 1 || type == 2) {
        	mpw = new MaplePacketWriter(SendPacketOpcode.RANGED_ATTACK);
        } else if (type == 3) {
        	mpw = new MaplePacketWriter(SendPacketOpcode.MAGIC_ATTACK);
        } else {
        	mpw = new MaplePacketWriter(SendPacketOpcode.ENERGY_ATTACK);
        }

        mpw.writeInt(cid);
        mpw.write(0);
        mpw.write(tbyte);
        mpw.write(charLevel);
        
        if (skillLevel > 0) {
        	mpw.write(skillLevel);
            mpw.writeInt(skillid);
        } else {
        	mpw.write(0);
        }

        if (GameConstants.isZero(skillid / 10000) && skillid != 100001283) {
            short zero1 = 0;
            short zero2 = 0;
            mpw.write(zero1 > 0 || zero2 > 0); //boolean
            if (zero1 > 0 || zero2 > 0) {
                mpw.writeShort(zero1);
                mpw.writeShort(zero2);
                //there is a full handler so better not write zero
            }
        }

        /*
        if (type == 1 || type == 2) { // if RANGED_ATTACK  (Got this from the IDA but it's wrong?)
            mpw.write(ultLevel);
            if (ultLevel > 0) {
                mpw.writeInt(3220010);
            }
        }
        */
        
        if (skillid == 4121013) {  // if the skill can be affect by a hyper stat
        	mpw.write(ultLevel); // Hyper Skill Boolean/Level
        	if (ultLevel > 0) {
        		mpw.writeInt(0); // Hyper Skill ID
        	}
        }

        if (skillid == 80001850) {
        	mpw.write(skillLevel);
        	if (skillLevel > 0) {
        		mpw.writeInt(skillid);
        	}
        }
        
        mpw.write(0); // some flag
        mpw.write(unk); // flag
        mpw.writeInt(0); // nOption3 or nBySummonedID
        
        if ((unk & 2) != 0) {
        	mpw.writeInt(skillid); // buckShotInfo.nSkillID
        	mpw.writeInt(skillLevel); // buckShotInfo.nSkillLV
        }
        
        if ((unk & 8) != 0) {
        	mpw.write(0); // nPassiveAddAttackCount
        }
        
        /*if (skillid == 40021185 || skillid == 42001006) {
            mpw.write(0); //boolean if true then int
        }*/

        
        mpw.writeShort(display);
        mpw.write(speed);
        mpw.write(mastery);
        mpw.writeInt(charge > 0 ? charge : 0); // Throwing Star ID
        
        for (AttackPair oned : damage) {
            if (oned.attack != null) {
                mpw.writeInt(oned.objectid);
                mpw.write(oned.unknownByte != 0 ? oned.unknownByte : 7);
                mpw.write(oned.unknownBool1); // some boolean
                mpw.write(oned.unknownBool2); // some boolean
                mpw.writeShort(oned.unknownShort != 0 ? oned.unknownShort : 256); // ??
                if (skillid == 42111002) {
                    mpw.write(oned.attack.size());
                    for (Pair eachd : oned.attack) {
                        mpw.writeInt(((Integer) eachd.left).intValue());
                    }
                } else {
                    for (Pair eachd : oned.attack) {
                        mpw.write(((Boolean) eachd.right).booleanValue() ? 1 : 0); // Show critical if true
                        mpw.writeInt(((Integer) eachd.left).intValue());
                    }
                }
            }
        }
        if (skillid == 2321001 || skillid == 2221052 || skillid == 11121052) {
            mpw.writeInt(0);
        } else if (skillid == 65121052 || skillid == 101000202 || skillid == 101000102) {
            mpw.writeInt(0);
            mpw.writeInt(0);
        }
        if (skillid == 42100007) {
            mpw.writeShort(0);
            mpw.write(0);
        }
        /*if (type == 1 || type == 2) {
            mpw.writePos(pos);
        } else */
        if (type == 3 && charge > 0) {
            mpw.writeInt(charge);
        }
        if (skillid == 5321000
                || skillid == 5311001
                || skillid == 5321001
                || skillid == 5011002
                || skillid == 5311002
                || skillid == 5221013
                || skillid == 5221017
                || skillid == 3120019
                || skillid == 3121015
                || skillid == 4121017) {
            mpw.writePos(pos);
        }

        System.out.println(mpw.toString());
        return mpw.getPacket();
    }

    public static byte[] skillEffect(MapleCharacter from, int skillId, byte level, short display, byte unk) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SKILL_EFFECT);
		mpw.writeInt(from.getID());
        mpw.writeInt(skillId);
        mpw.write(level);
        mpw.writeShort(display);
        mpw.write(unk);
        if (skillId == 13111020) {
            mpw.writePos(from.getPosition()); // Position
        }         if (skillId == 27101202) {
            mpw.writePos(from.getPosition()); // Position
        }

        return mpw.getPacket();
    }

    public static byte[] skillCancel(MapleCharacter from, int skillId) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANCEL_SKILL_EFFECT);
		mpw.writeInt(from.getID());
        mpw.writeInt(skillId);

        return mpw.getPacket();
    }

    public static byte[] damagePlayer(int cid, int type, int damage, int monsteridfrom, byte direction, int skillid, int pDMG, boolean pPhysical, int pID, byte pType, Point pPos, byte offset, int offset_d, int fake) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DAMAGE_PLAYER);
		mpw.writeInt(cid);
        mpw.write(type);
        mpw.writeInt(damage);
        mpw.write(0);
        if (type >= -1) {
            mpw.writeInt(monsteridfrom);
            mpw.write(direction);
            mpw.writeInt(skillid);
            mpw.writeInt(pDMG);
            mpw.write(0);
            if (pDMG > 0) {
                mpw.write(pPhysical ? 1 : 0);
                mpw.writeInt(pID);
                mpw.write(pType);
                mpw.writePos(pPos);
            }
            mpw.write(offset);
            if (offset == 1) {
                mpw.writeInt(offset_d);
            }
        }
        mpw.writeInt(damage);
        if ((damage <= 0) || (fake > 0)) {
            mpw.writeInt(fake);
        }

        return mpw.getPacket();
    }

    public static byte[] facialExpression(MapleCharacter from, int expression) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FACIAL_EXPRESSION);
		mpw.writeInt(from.getID());
        mpw.writeInt(expression);
        mpw.writeInt(-1);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] directionFacialExpression(int expression, int duration) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIRECTION_FACIAL_EXPRESSION);
		mpw.writeInt(expression);
            mpw.writeInt(duration);
            mpw.write(0); // bByItemOption


            /* Facial Expressions:
             * 0 - Normal 
             * 1 - F1
             * 2 - F2
             * 3 - F3
             * 4 - F4
             * 5 - F5
             * 6 - F6
             * 7 - F7
             * 8 - Vomit
             * 9 - Panic
             * 10 - Sweetness
             * 11 - Kiss
             * 12 - Wink
             * 13 - Ouch!
             * 14 - Goo goo eyes
             * 15 - Blaze
             * 16 - Star
             * 17 - Love
             * 18 - Ghost
             * 19 - Constant Sigh
             * 20 - Sleepy
             * 21 - Flaming hot
             * 22 - Bleh
             * 23 - No Face
             */
            return mpw.getPacket();
        } 

    
    public static byte[] itemEffect(int characterid, int itemid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_EFFECT);
		mpw.writeInt(characterid);
        mpw.writeInt(itemid);

        return mpw.getPacket();
    }

    public static byte[] showTitle(int characterid, int itemid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_TITLE);
		mpw.writeInt(characterid);
        mpw.writeInt(itemid);

        return mpw.getPacket();
    }

    public static byte[] showAngelicBuster(int characterid, int tempid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ANGELIC_CHANGE);
		mpw.writeInt(characterid);
        mpw.writeInt(tempid);

        return mpw.getPacket();
    }

    public static byte[] showChair(int charID, int itemid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_CHAIR);
		mpw.writeInt(charID);
        mpw.writeInt(itemid);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        
        return mpw.getPacket();
    }

    public static byte[] updateCharLook(MapleCharacter chr, boolean second) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_CHAR_LOOK);
		mpw.writeInt(chr.getID());
        mpw.write(1);
        PacketHelper.addCharLook(mpw, chr, false, second);
        Triple<List<MapleRing>, List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        addRingInfo(mpw, rings.getLeft());
        addRingInfo(mpw, rings.getMid());
        addMRingInfo(mpw, rings.getRight(), chr);
        mpw.writeInt(0); // -> charid to follow (4)
        mpw.writeInt(0);
        
        return mpw.getPacket();
    }

    public static byte[] updatePartyMemberHP(int cid, int curhp, int maxhp) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_PARTYMEMBER_HP);
		mpw.writeInt(cid);
        mpw.writeInt(curhp);
        mpw.writeInt(maxhp);

        return mpw.getPacket();
    }

    public static byte[] loadGuildName(MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LOAD_GUILD_NAME);
		mpw.writeInt(chr.getID());
        
        if (chr.getGuildId() <= 0) {
            mpw.writeShort(0);
        } else {
            MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mpw.writeMapleAsciiString(gs.getName());
            } else {
                mpw.writeShort(0);
            }
        }

        return mpw.getPacket();
    }

    public static byte[] loadGuildIcon(MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LOAD_GUILD_ICON);
		mpw.writeInt(chr.getID());
        
        if (chr.getGuildId() <= 0) {
            mpw.writeZeroBytes(6);
        } else {
            MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mpw.writeShort(gs.getLogoBG());
                mpw.write(gs.getLogoBGColor());
                mpw.writeShort(gs.getLogo());
                mpw.write(gs.getLogoColor());
            } else {
                mpw.writeZeroBytes(6);
            }
        }

        return mpw.getPacket();
    }

    public static byte[] changeTeam(int cid, int type) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LOAD_TEAM);
		mpw.writeInt(cid);
        mpw.write(type);

        return mpw.getPacket();
    }

    public static byte[] showHarvesting(int cid, int tool) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_HARVEST);
		mpw.writeInt(cid);
        if (tool > 0) {
            mpw.write(1);
            mpw.write(0);
            mpw.writeShort(0);
            mpw.writeInt(tool);
            mpw.writeZeroBytes(30);
        } else {
            mpw.write(0);
            mpw.writeZeroBytes(33);
        }

        return mpw.getPacket();
    }

    public static byte[] getPVPHPBar(int cid, int hp, int maxHp) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_HP);
		mpw.writeInt(cid);
        mpw.writeInt(hp);
        mpw.writeInt(maxHp);

        return mpw.getPacket();
    }

    public static byte[] cancelChair(int id) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANCEL_CHAIR);
        if (id == -1) {
            mpw.write(0);
        } else {
            mpw.write(1);
            mpw.writeShort(id);
        }

        return mpw.getPacket();
    }

    public static byte[] instantMapWarp(byte portal) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CURRENT_MAP_WARP);
		mpw.write(0);
        mpw.write(portal);
        if (portal <= 0) {
        	mpw.writeInt(0); // nIdx, map id? 
        } else {
        	mpw.writeInt(0); // dwCallerId, player id?
        	mpw.writeInt(0); // ptTarget, x,y point
        }

        return mpw.getPacket();
    }

    public static byte[] updateQuestInfo(MapleCharacter c, int questid, int npc, byte progress) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_QUEST_INFO);
		mpw.write(progress);
        mpw.writeInt(questid);
        mpw.writeInt(npc);
        mpw.writeInt(0);
        mpw.write(1);

        return mpw.getPacket();
    }

    public static byte[] updateQuestFinish(int questid, int npc, int nextquest) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_QUEST_INFO);
		mpw.write(11);
        mpw.writeInt(questid);
        mpw.writeInt(npc);  // uJobDemandLower
        mpw.writeInt(nextquest);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] sendHint(String hint, int width, int height) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_HINT);
		mpw.writeMapleAsciiString(hint);
        mpw.writeShort(width < 1 ? Math.max(hint.length() * 10, 40) : width);
        mpw.writeShort(Math.max(height, 5));
        mpw.write(1);

        return mpw.getPacket();
    }

    public static byte[] updateCombo(int value) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ARAN_COMBO);
		mpw.writeInt(value);

        return mpw.getPacket();
    }

    public static byte[] rechargeCombo(int value) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ARAN_COMBO_RECHARGE);
		mpw.writeInt(value);

        return mpw.getPacket();
    }

    public static byte[] getFollowMessage(String msg) {
        return getGameMessage((short) 11, msg);
    }

    public static byte[] getGameMessage(short color, String message) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GAME_MESSAGE);
		mpw.writeShort(color);
        mpw.writeMapleAsciiString(message);

        return mpw.getPacket();
    }

    public static byte[] getBuffZoneEffect(int itemId) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BUFF_ZONE_EFFECT);
		mpw.writeInt(itemId);

        return mpw.getPacket();
    }

    public static byte[] getTimeBombAttack() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.TIME_BOMB_ATTACK);
		mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(10);
        mpw.writeInt(6);

        return mpw.getPacket();
    }

    public static byte[] moveFollow(Point otherStart, Point myStart, Point otherEnd, List<LifeMovementFragment> moves) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FOLLOW_MOVE);
		mpw.writeInt(0);
        mpw.writePos(otherStart);
        mpw.writePos(myStart);
        PacketHelper.serializeMovementList(mpw, moves);
        mpw.write(17);
        for (int i = 0; i < 8; i++) {
            mpw.write(0);
        }
        mpw.write(0);
        mpw.writePos(otherEnd);
        mpw.writePos(otherStart);
        mpw.writeZeroBytes(100);

        return mpw.getPacket();
    }

    public static byte[] getFollowMsg(int opcode) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.FOLLOW_MSG);
		mpw.writeLong(opcode);

        return mpw.getPacket();
    }

    public static byte[] registerFamiliar(MonsterFamiliar mf) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REGISTER_FAMILIAR);
		mpw.writeLong(mf.getId());
        mf.writeRegisterPacket(mpw, false);
        mpw.writeShort(mf.getVitality() >= 3 ? 1 : 0);

        return mpw.getPacket();
    }

    public static byte[] createUltimate(int amount) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CREATE_ULTIMATE);
		mpw.writeInt(amount);

        return mpw.getPacket();
    }

    public static byte[] harvestMessage(int oid, int msg) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.HARVEST_MESSAGE);
		mpw.writeInt(oid);
        mpw.writeInt(msg);

        return mpw.getPacket();
    }

    public static byte[] openBag(int index, int itemId, boolean firstTime) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_BAG);
		mpw.writeInt(index);
        mpw.writeInt(itemId);
        mpw.writeShort(firstTime ? 1 : 0);

        return mpw.getPacket();
    }

    public static byte[] dragonBlink(int portalId) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DRAGON_BLINK);
		mpw.write(portalId);

        return mpw.getPacket();
    }

    public static byte[] getPVPIceGage(int score) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PVP_ICEGAGE);
		mpw.writeInt(score);

        return mpw.getPacket();
    }

    public static byte[] skillCooldown(int sid, int time) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.COOLDOWN);
		mpw.writeInt(sid);
        mpw.writeInt(time);

        return mpw.getPacket();
    }

    /*
    public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod) {
        MaplePacketLittleEndianWriter mpw = new MaplePacketLittleEndianWriter();

        mpw.writeShort(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT);
		mpw.write(mod);
        mpw.writeInt(drop.getObjectId());
        mpw.write(drop.getMeso() > 0 ? 1 : 0);
        mpw.writeInt(drop.getItemId());
        mpw.writeInt(drop.getOwner());
        mpw.write(drop.getDropType());
        mpw.writePos(dropto);
        mpw.writeInt(0);
        if (mod != 2) {
            mpw.writePos(dropfrom);
            mpw.writeShort(0);
//            mpw.write(0);//removed 143 or other0
        }
        mpw.write(0);
        if (drop.getMeso() == 0) {
            PacketHelper.addExpirationTime(mpw, drop.getItem().getExpiration());
        }
        mpw.writeShort(drop.isPlayerDrop() ? 0 : 1);
        mpw.writeZeroBytes(4);

        return mpw.getPacket();
    }
    */
    public static byte[] dropItemFromMapObject(MapleMapItem drop, Point dropfrom, Point dropto, byte mod) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT);
		mpw.write(0); // eDropType
        mpw.write(mod);
        mpw.writeInt(drop.getObjectId());
        mpw.write(drop.getMeso() > 0 ? 1 : 0);
        mpw.writeInt(0); // bDropMotionType
        mpw.writeInt(0); // bDropSpeed
        mpw.writeInt(0); // bNoMove
        mpw.writeInt(drop.getItemId());
        mpw.writeInt(drop.getOwner());
        mpw.write(drop.getDropType()); // nOwnType
        mpw.writePos(dropto);
        mpw.writeInt(0); // dwSourceID
        if (mod != 2) {
            mpw.writePos(dropfrom);
            mpw.writeInt(0); // Delay Time
        }
        mpw.write(0); // bExplosiveDrop
        mpw.write(0); // ??
        if (drop.getMeso() == 0) {
            PacketHelper.addExpirationTime(mpw, drop.getItem().getExpiration());
        }
        mpw.writeShort(drop.isPlayerDrop() ? 0 : 1);
        mpw.writeLong(0);
        mpw.write(0); // potential state (1 | 2 | 3 | 4)
        
        return mpw.getPacket();
    }

    public static byte[] explodeDrop(int oid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_ITEM_FROM_MAP);
		mpw.write(4);
        mpw.writeInt(oid);
        mpw.writeShort(655);

        return mpw.getPacket();
    }

    public static byte[] removeItemFromMap(int oid, int animation, int cid) {
        return removeItemFromMap(oid, animation, cid, 0);
    }

    public static byte[] removeItemFromMap(int oid, int animation, int cid, int slot) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_ITEM_FROM_MAP);
		mpw.write(animation);
        mpw.writeInt(oid);
        if (animation >= 2) {
            mpw.writeInt(cid);
            if (animation == 5) {
                mpw.writeInt(slot);
            }
        }
        return mpw.getPacket();
    }
    

    public static byte[] spawnClockMist(final MapleMist clock) {
        MaplePacketWriter outPacket = new MaplePacketWriter(SendPacketOpcode.SPAWN_MIST);
        outPacket.writeInt(clock.getObjectId());
        outPacket.write(1);
        outPacket.writeInt(clock.getMobOwner().getObjectId());
        outPacket.writeInt(clock.getMobSkill().getSkillId());
        outPacket.write(clock.getClockType());
        outPacket.writeShort(0x07);//clock.getSkillDelay());
        outPacket.writeInt(clock.getBox().x);
        outPacket.writeInt(clock.getBox().y);
        outPacket.writeInt(clock.getBox().x + clock.getBox().width);
        outPacket.writeInt(clock.getBox().y + clock.getBox().height);
        outPacket.writeInt(0);
        outPacket.writePos(clock.getMobOwner().getPosition());
        outPacket.writeInt(0);
        outPacket.writeInt(clock.getClockType() == 1 ? 15 : clock.getClockType() == 2 ? -15 : 0);
        outPacket.writeInt(0x78);
        //System.out.println(packet.toString());
        return outPacket.getPacket();
    }
    
        public static byte[] spawnObtacleAtomBomb(){
        MaplePacketWriter outPacket = new MaplePacketWriter(SendPacketOpcode.SPAWN_OBTACLE_ATOM);
        
        //Number of bomb objects to spawn.  You can also just send multiple packets instead of putting them all in one packet.
        outPacket.writeInt(500);
        
        //Unknown, this part is from IDA.
        byte unk = 0;
        outPacket.write(unk); //animation data or some shit
        if(unk == 1){
            outPacket.writeInt(300); //from Effect.img/BasicEff/ObtacleAtomCreate/%d
            outPacket.write(0); //rest idk
            outPacket.writeInt(0);
            outPacket.writeInt(0);
            outPacket.writeInt(0);
            outPacket.writeInt(0);
        }
        
            outPacket.write(1);
            outPacket.writeInt(1);
            outPacket.writeInt(1);
            outPacket.writeInt(900); //POSX
            outPacket.writeInt(-1347); //POSY
            outPacket.writeInt(25);
            outPacket.writeInt(3);
            outPacket.writeInt(0);
            outPacket.writeInt(25);
            outPacket.writeInt(-5);
            outPacket.writeInt(1000);
            outPacket.writeInt(800);
            outPacket.writeInt(80);
        return outPacket.getPacket();
    }  

    public static byte[] spawnMist(MapleMist mist) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_MIST);
		mpw.writeInt(mist.getObjectId());

        //mpw.write(mist.isMobMist() ? 0 : mist.isPoisonMist());
        mpw.write(0);
        mpw.writeInt(mist.getOwnerId());
        if (mist.getMobSkill() == null) {
            mpw.writeInt(mist.getSourceSkill().getId());
        } else {
            mpw.writeInt(mist.getMobSkill().getSkillId());
        }
        mpw.write(mist.getSkillLevel());
        mpw.writeShort(mist.getSkillDelay());
        mpw.writeRect(mist.getBox());
        mpw.writeInt(mist.isShelter() ? 1 : 0);
        mpw.writeInt(0);
        mpw.writePos(mist.getPosition());
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeInt(0);
        mpw.writeShort(0);
        mpw.writeShort(mist.getSkillDelay());
        mpw.writeShort(0);

        return mpw.getPacket();
    }

    public static byte[] removeMist(int oid, boolean eruption) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_MIST);
		mpw.writeInt(oid);
        mpw.write(eruption ? 1 : 0);

        return mpw.getPacket();
    }
    
    
    public static byte[] removeMist(final int oid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_MIST);
		mpw.writeInt(oid);
        mpw.write(0); //v181
        
        return mpw.getPacket();
    }

    public static byte[] spawnMysticDoor(int oid, int skillid, Point pos, boolean animation) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_MYSTIC_DOOR);
		mpw.write(animation ? 0 : 1);
        mpw.writeInt(oid);
        mpw.writeInt(skillid);
        mpw.writePos(pos);

        return mpw.getPacket();
    }

    public static byte[] removeMysticDoor(int oid, boolean animation) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_MYSTIC_DOOR);
		mpw.write(animation ? 0 : 1);
        mpw.writeInt(oid);

        return mpw.getPacket();
    }

    public static byte[] spawnKiteError() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_KITE_ERROR);

        return mpw.getPacket();
    }

    public static byte[] spawnKite(int oid, int id, Point pos) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_KITE);
		mpw.writeInt(oid);
        mpw.writeInt(0);
        mpw.writeMapleAsciiString("");
        mpw.writeMapleAsciiString("");
        mpw.writePos(pos);

        return mpw.getPacket();
    }

    public static byte[] destroyKite(int oid, int id, boolean animation) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DESTROY_KITE);
		mpw.write(animation ? 0 : 1);
        mpw.writeInt(oid);

        return mpw.getPacket();
    }

    public static byte[] spawnMechDoor(MechDoor md, boolean animated) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_MECH_DOOR);
		mpw.write(animated ? 0 : 1);
        mpw.writeInt(md.getOwnerId());
        mpw.writePos(md.getTruePosition());
        mpw.write(md.getId());
        mpw.writeInt(md.getPartyId());
        return mpw.getPacket();
    }

    public static byte[] removeMechDoor(MechDoor md, boolean animated) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_MECH_DOOR);
		mpw.write(animated ? 0 : 1);
        mpw.writeInt(md.getOwnerId());
        mpw.write(md.getId());

        return mpw.getPacket();
    }

    public static byte[] triggerReactor(MapleReactor reactor, int stance) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REACTOR_HIT);
		mpw.writeInt(reactor.getObjectId());
        mpw.write(reactor.getState());
        mpw.writePos(reactor.getTruePosition());
        mpw.writeInt(stance);
        mpw.writeInt(0);
        
        return mpw.getPacket();
    }
    
    public static byte[] triggerReactor(MapleReactor reactor, int stance, MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REACTOR_HIT);
		mpw.writeInt(reactor.getObjectId());
        mpw.write(reactor.getState());
        mpw.writePos(reactor.getTruePosition());
        mpw.writeInt(stance);
        mpw.writeInt(chr.getID());
        
        return mpw.getPacket();
    }

    public static byte[] spawnReactor(MapleReactor reactor) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REACTOR_SPAWN);
		mpw.writeInt(reactor.getObjectId());
        mpw.writeInt(reactor.getReactorId());
        mpw.write(reactor.getState());
        mpw.writePos(reactor.getTruePosition());
        mpw.write(reactor.getFacingDirection());
        mpw.writeMapleAsciiString(reactor.getName());

        return mpw.getPacket();
    }

    public static byte[] destroyReactor(MapleReactor reactor) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REACTOR_DESTROY);
		mpw.writeInt(reactor.getObjectId());
        mpw.write(reactor.getState());
        mpw.writePos(reactor.getPosition());

        return mpw.getPacket();
    }

    public static byte[] makeExtractor(int cid, String cname, Point pos, int timeLeft, int itemId, int fee) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_EXTRACTOR);
		mpw.writeInt(cid);
        mpw.writeMapleAsciiString(cname);
        mpw.writeInt(pos.x);
        mpw.writeInt(pos.y);
        mpw.writeShort(timeLeft);
        mpw.writeInt(itemId);
        mpw.writeInt(fee);

        return mpw.getPacket();
    }

    public static byte[] removeExtractor(int cid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_EXTRACTOR);
		mpw.writeInt(cid);
        mpw.writeInt(1);

        return mpw.getPacket();
    }

    public static byte[] rollSnowball(int type, MapleSnowball.MapleSnowballs ball1, MapleSnowball.MapleSnowballs ball2) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ROLL_SNOWBALL);
		mpw.write(type);
        mpw.writeInt(ball1 == null ? 0 : ball1.getSnowmanHP() / 75);
        mpw.writeInt(ball2 == null ? 0 : ball2.getSnowmanHP() / 75);
        mpw.writeShort(ball1 == null ? 0 : ball1.getPosition());
        mpw.write(0);
        mpw.writeShort(ball2 == null ? 0 : ball2.getPosition());
        mpw.writeZeroBytes(11);

        return mpw.getPacket();
    }

    public static byte[] enterSnowBall() {
        return rollSnowball(0, null, null);
    }

    public static byte[] hitSnowBall(int team, int damage, int distance, int delay) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.HIT_SNOWBALL);
		mpw.write(team);
        mpw.writeShort(damage);
        mpw.write(distance);
        mpw.write(delay);

        return mpw.getPacket();
    }

    public static byte[] snowballMessage(int team, int message) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SNOWBALL_MESSAGE);
		mpw.write(team);
        mpw.writeInt(message);

        return mpw.getPacket();
    }

    public static byte[] leftKnockBack() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LEFT_KNOCK_BACK);

        return mpw.getPacket();
    }

    public static byte[] hitCoconut(boolean spawn, int id, int type) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.HIT_COCONUT);
		mpw.writeInt(spawn ? 32768 : id);
        mpw.write(spawn ? 0 : type);

        return mpw.getPacket();
    }

    public static byte[] coconutScore(int[] coconutscore) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.COCONUT_SCORE);
		mpw.writeShort(coconutscore[0]);
        mpw.writeShort(coconutscore[1]);

        return mpw.getPacket();
    }

    public static byte[] updateAriantScore(List<MapleCharacter> players) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ARIANT_SCORE_UPDATE);
		mpw.write(players.size());
        for (MapleCharacter i : players) {
            mpw.writeMapleAsciiString(i.getName());
            mpw.writeInt(0);
        }

        return mpw.getPacket();
    }

    public static byte[] sheepRanchInfo(byte wolf, byte sheep) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHEEP_RANCH_INFO);
		mpw.write(wolf);
        mpw.write(sheep);

        return mpw.getPacket();
    }

    public static byte[] sheepRanchClothes(int cid, byte clothes) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHEEP_RANCH_CLOTHES);
		mpw.writeInt(cid);
        mpw.write(clothes);

        return mpw.getPacket();
    }

    public static byte[] updateWitchTowerKeys(int keys) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.WITCH_TOWER);
		mpw.write(keys);

        return mpw.getPacket();
    }

    public static byte[] showChaosZakumShrine(boolean spawned, int time) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CHAOS_ZAKUM_SHRINE);
		mpw.write(spawned ? 1 : 0);
        mpw.writeInt(time);

        return mpw.getPacket();
    }

    public static byte[] showChaosHorntailShrine(boolean spawned, int time) {
        return showHorntailShrine(spawned, time);
    }

    public static byte[] showHorntailShrine(boolean spawned, int time) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.HORNTAIL_SHRINE);
		mpw.write(spawned ? 1 : 0);
        mpw.writeInt(time);

        return mpw.getPacket();
    }

    public static byte[] getRPSMode(byte mode, int mesos, int selection, int answer) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.RPS_GAME);
		mpw.write(mode);
        switch (mode) {
            case 6:
                if (mesos == -1) {
                    break;
                }
                mpw.writeInt(mesos);
                break;
            case 8:
                mpw.writeInt(9000019);
                break;
            case 11:
                mpw.write(selection);
                mpw.write(answer);
        }

        return mpw.getPacket();
    }

    public static byte[] messengerInvite(String from, int messengerid) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MESSENGER);
		mpw.write(3);
        mpw.writeMapleAsciiString(from);
        mpw.write(1);//channel?
        mpw.writeInt(messengerid);
        mpw.write(0);

        return mpw.getPacket();
    }

    public static byte[] addMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MESSENGER);
		mpw.write(0);
        mpw.write(position);
        PacketHelper.addCharLook(mpw, chr, true, false);
        mpw.writeMapleAsciiString(from);
        mpw.write(channel);
        mpw.write(1); // v140
        mpw.writeInt(chr.getJob());

        return mpw.getPacket();
    }

    public static byte[] removeMessengerPlayer(int position) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MESSENGER);
		mpw.write(2);
        mpw.write(position);

        return mpw.getPacket();
    }

    public static byte[] updateMessengerPlayer(String from, MapleCharacter chr, int position, int channel) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MESSENGER);
		mpw.write(0); // v140.
        mpw.write(position);
        PacketHelper.addCharLook(mpw, chr, true, false);
        mpw.writeMapleAsciiString(from);
        mpw.write(channel);
        mpw.write(0); // v140.
        mpw.writeInt(chr.getJob()); // doubt it's the job, lol. v140.

        return mpw.getPacket();
    }

    public static byte[] joinMessenger(int position) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MESSENGER);
		mpw.write(1);
        mpw.write(position);

        return mpw.getPacket();
    }

    public static byte[] messengerChat(String charname, String text) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MESSENGER);
		mpw.write(6);
        mpw.writeMapleAsciiString(charname);
        mpw.writeMapleAsciiString(text);

        return mpw.getPacket();
    }

    public static byte[] messengerNote(String text, int mode, int mode2) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MESSENGER);
		mpw.write(mode);
        mpw.writeMapleAsciiString(text);
        mpw.write(mode2);

        return mpw.getPacket();
    }

    public static byte[] messengerOpen(byte type, List<MapleCharacter> chars) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MESSENGER_OPEN);
		mpw.write(type); //7 in messenger open ui 8 new ui
        if (chars.isEmpty()) {
            mpw.writeShort(0);
        }
        for (MapleCharacter chr : chars) {
            mpw.write(1);
            mpw.writeInt(chr.getID());
            mpw.writeInt(0); //likes
            mpw.writeLong(0); //some time
            mpw.writeMapleAsciiString(chr.getName());
            PacketHelper.addCharLook(mpw, chr, true, false);
        }

        return mpw.getPacket();
    }

    public static byte[] messengerCharInfo(MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MESSENGER);
		mpw.write(0x0B);
        mpw.writeMapleAsciiString(chr.getName());
        mpw.writeInt(chr.getJob());
        mpw.writeInt(chr.getFame());
        mpw.writeInt(0); //likes
        MapleGuild gs = World.Guild.getGuild(chr.getGuildId());
        mpw.writeMapleAsciiString(gs != null ? gs.getName() : "-");
        MapleGuildAlliance alliance = World.Alliance.getAlliance(gs.getAllianceId());
        mpw.writeMapleAsciiString(alliance != null ? alliance.getName() : "");
        mpw.write(2);

        return mpw.getPacket();
    }

    public static byte[] removeFromPackageList(boolean remove, int Package) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PACKAGE_OPERATION);
		mpw.write(24);
        mpw.writeInt(Package);
        mpw.write(remove ? 3 : 4);

        return mpw.getPacket();
    }

    public static byte[] sendPackageMSG(byte operation, List<MaplePackageActions> packages) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PACKAGE_OPERATION);
		mpw.write(operation);

        switch (operation) {
            case 9:
                mpw.write(1);
                break;
            case 10:
                mpw.write(0);
                mpw.write(packages.size());

                for (MaplePackageActions dp : packages) {
                    mpw.writeInt(dp.getPackageId());
                    mpw.writeAsciiString(dp.getSender(), 13);
                    mpw.writeInt(dp.getMesos());
                    mpw.writeLong(PacketHelper.getTime(dp.getSentTime()));
                    mpw.writeZeroBytes(205);

                    if (dp.getItem() != null) {
                        mpw.write(1);
                        PacketHelper.addItemInfo(mpw, dp.getItem());
                    } else {
                        mpw.write(0);
                    }
                }
                mpw.write(0);
        }

        return mpw.getPacket();
    }

    public static byte[] getKeymap(MapleKeyLayout layout) {
        MaplePacketWriter outPacket = new MaplePacketWriter(SendPacketOpcode.KEYMAP);
        layout.writeData(outPacket);

        return outPacket.getPacket();
    }

    public static byte[] petAutoHP(int itemId) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PET_AUTO_HP);
		mpw.writeInt(itemId);

        return mpw.getPacket();
    }

    public static byte[] petAutoMP(int itemId) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PET_AUTO_MP);
		mpw.writeInt(itemId);

        return mpw.getPacket();
    }

    public static byte[] petAutoCure(int itemId) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PET_AUTO_CURE);
		mpw.writeInt(itemId);

        return mpw.getPacket();
    }

    public static byte[] petAutoBuff(int skillId) {
        MaplePacketWriter mpw = new MaplePacketWriter();

        //mpw.writeShort(SendPacketOpcode.PET_AUTO_BUFF);
		mpw.writeInt(skillId);

        return mpw.getPacket();
    }

    public static void addRingInfo(MaplePacketWriter mpw, List<MapleRing> rings) {
        mpw.write(rings.size());
        for (MapleRing ring : rings) {
            mpw.writeInt(1);
            mpw.writeLong(ring.getRingId());
            mpw.writeLong(ring.getPartnerRingId());
            mpw.writeInt(ring.getItemId());
        }
    }

    public static void addMRingInfo(MaplePacketWriter mpw, List<MapleRing> rings, MapleCharacter chr) {
        mpw.write(rings.size());
        for (MapleRing ring : rings) {
            mpw.writeInt(1);
            mpw.writeInt(chr.getID());
            mpw.writeInt(ring.getPartnerChrId());
            mpw.writeInt(ring.getItemId());
        }
    }

    public static byte[] getBuffBar(long millis) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BUFF_BAR);
		mpw.writeLong(millis);

        return mpw.getPacket();
    }

    public static byte[] getBoosterFamiliar(int cid, int familiar, int id) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.BOOSTER_FAMILIAR);
		mpw.writeInt(cid);
        mpw.writeInt(familiar);
        mpw.writeLong(id);
        mpw.write(0);

        return mpw.getPacket();
    }

    static {
        DEFAULT_BUFFMASK |= MapleBuffStat.ENERGY_CHARGE.getValue();
        DEFAULT_BUFFMASK |= MapleBuffStat.DASH_SPEED.getValue();
        DEFAULT_BUFFMASK |= MapleBuffStat.DASH_JUMP.getValue();
        DEFAULT_BUFFMASK |= MapleBuffStat.MONSTER_RIDING.getValue();
        DEFAULT_BUFFMASK |= MapleBuffStat.SPEED_INFUSION.getValue();
        DEFAULT_BUFFMASK |= MapleBuffStat.HOMING_BEACON.getValue();
        DEFAULT_BUFFMASK |= MapleBuffStat.DEFAULT_BUFFSTAT.getValue();
    }

    public static byte[] viewSkills(MapleCharacter chr) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.TARGET_SKILL);
        List skillz = new ArrayList();
        for (Skill sk : chr.getSkills().keySet()) {
            if ((sk.canBeLearnedBy(chr.getJob())) && (GameConstants.canSteal(sk)) && (!skillz.contains(Integer.valueOf(sk.getId())))) {
                skillz.add(Integer.valueOf(sk.getId()));
            }
        }
        mpw.write(1);
        mpw.writeInt(chr.getID());
        mpw.writeInt(skillz.isEmpty() ? 2 : 4);
        mpw.writeInt(chr.getJob());
        mpw.writeInt(skillz.size());
        for (Iterator i$ = skillz.iterator(); i$.hasNext();) {
            int i = ((Integer) i$.next()).intValue();
            mpw.writeInt(i);
        }

        return mpw.getPacket();
    }

    public static class InteractionPacket {

        public static byte[] getTradeInvite(MapleCharacter c) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
            mpw.write(Interaction.INVITE_TRADE.action);
            mpw.write(4);//was 3
            mpw.writeMapleAsciiString(c.getName());
//            mpw.writeInt(c.getLevel());
            mpw.writeInt(c.getJob());
            return mpw.getPacket();
        }

        public static byte[] getTradeMesoSet(byte number, long meso) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
            mpw.write(Interaction.UPDATE_MESO.action);
            mpw.write(number);
            mpw.writeLong(meso);
            return mpw.getPacket();
        }
        
        public static byte[] gachaponMessage(Item item, String town, MapleCharacter player) {
        	final MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SERVERMESSAGE);
        	mpw.write(0x0B);
        	mpw.writeMapleAsciiString(player.getName() + " : got a(n)");
        	mpw.writeInt(0); //random?
        	mpw.writeMapleAsciiString(town);
        	PacketHelper.addItemInfo(mpw, item);
        	return mpw.getPacket();	
        }

        public static byte[] getTradeItemAdd(byte number, Item item) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
            mpw.write(Interaction.SET_ITEMS.action);
            mpw.write(number);
            mpw.write(item.getPosition());
            PacketHelper.addItemInfo(mpw, item);

            return mpw.getPacket();
        }

        public static byte[] getTradeStart(MapleClient c, MapleTrade trade, byte number) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
//            mpw.write(PlayerInteractionHandler.Interaction.START_TRADE.action);
//            if (number != 0){//13 a0
////                mpw.write(HexTool.getByteArrayFromHexString("13 01 01 03 FE 53 00 00 40 08 00 00 00 E2 7B 00 00 01 E9 50 0F 00 03 62 98 0F 00 04 56 BF 0F 00 05 2A E7 0F 00 07 B7 5B 10 00 08 3D 83 10 00 09 D3 D1 10 00 0B 13 01 16 00 11 8C 1F 11 00 12 BF 05 1D 00 13 CB 2C 1D 00 31 40 6F 11 00 32 6B 46 11 00 35 32 5C 19 00 37 20 E2 11 00 FF 03 B6 98 0F 00 05 AE 0A 10 00 09 CC D0 10 00 FF FF 00 00 00 00 13 01 16 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0B 00 4D 6F 6D 6F 6C 6F 76 65 73 4B 48 40 08"));
//                mpw.write(19);
//                mpw.write(1);
//                PacketHelper.addCharLook(mpw, trade.getPartner().getChr(), false);
//                mpw.writeMapleAsciiString(trade.getPartner().getChr().getName());
//                mpw.writeShort(trade.getPartner().getChr().getJob());
//            }else{
            mpw.write(20);
            mpw.write(4);
            mpw.write(2);
            mpw.write(number);

            if (number == 1) {
                mpw.write(0);
                PacketHelper.addCharLook(mpw, trade.getPartner().getChr(), false, false);
                mpw.writeMapleAsciiString(trade.getPartner().getChr().getName());
                mpw.writeShort(trade.getPartner().getChr().getJob());
            }
            mpw.write(number);
            PacketHelper.addCharLook(mpw, c.getCharacter(), false, false);
            mpw.writeMapleAsciiString(c.getCharacter().getName());
            mpw.writeShort(c.getCharacter().getJob());
            mpw.write(255);
//            }
            return mpw.getPacket();
        }

        public static byte[] getTradeConfirmation() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
            mpw.write(Interaction.CONFIRM_TRADE.action);

            return mpw.getPacket();
        }

        public static byte[] TradeMessage(byte UserSlot, byte message) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
            mpw.write(Interaction.EXIT.action);
//            mpw.write(25);//new v141
            mpw.write(UserSlot);
            mpw.write(message);

            return mpw.getPacket();
        }

        public static byte[] getTradeCancel(byte UserSlot, int unsuccessful) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAYER_INTERACTION);
            mpw.write(Interaction.EXIT.action);
            mpw.write(UserSlot);
            mpw.write(7);//was2

            return mpw.getPacket();
        }
    }

    public static class NPCPacket {

        public static byte[] spawnNPC(MapleNPC life, boolean show) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_NPC);
            mpw.writeInt(life.getObjectId());
            mpw.writeInt(life.getId());
            mpw.writeShort(life.getPosition().x);
            mpw.writeShort(life.getCy());
            mpw.write(0); //v174 - nMoveAction
            mpw.write(life.getF() == 1 ? 0 : 1);
            mpw.writeShort(life.getFh());
            mpw.writeShort(life.getRx0());
            mpw.writeShort(life.getRx1());
            mpw.write(show ? 1 : 0);
            mpw.writeInt(0);//new 143
            mpw.write(0);
            mpw.writeInt(-1);
            mpw.writeZeroBytes(11);
            mpw.writeInt(0);

            return mpw.getPacket();
        }
        
        public static byte[] getMapSelection(final int npcid, final String sel) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(4);
            mpw.writeInt(npcid);
            mpw.writeShort(GameConstants.GMS ? 0x11 : 0x10);
            mpw.writeInt(npcid == 2083006 ? 1 : 0); //neo city
            mpw.writeInt(npcid == 9010022 ? 1 : 0); //dimensional
            mpw.writeMapleAsciiString(sel);
            
            return mpw.getPacket();
        }

        public static byte[] removeNPC(int objectid) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_NPC);
            mpw.writeInt(objectid);

            return mpw.getPacket();
        }

        public static byte[] removeNPCController(int objectid) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER);
            mpw.write(0);
            mpw.writeInt(objectid);

            return mpw.getPacket();
        }

        public static byte[] spawnNPCRequestController(MapleNPC life, boolean MiniMap) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER);
            mpw.write(1);
            mpw.writeInt(life.getObjectId());
            mpw.writeInt(life.getId());
            mpw.writeShort(life.getPosition().x);
            mpw.writeShort(life.getCy());
            mpw.write(0); //v174 - nMoveAction
            mpw.write(life.getF() == 1 ? 0 : 1);
            mpw.writeShort(life.getFh());
            mpw.writeShort(life.getRx0());
            mpw.writeShort(life.getRx1());
            mpw.write(MiniMap ? 1 : 0);
            mpw.writeInt(0);//new 143
            mpw.write(0);
            mpw.writeInt(-1);
            mpw.writeZeroBytes(11);
            mpw.writeInt(0);
            
            return mpw.getPacket();
        }

        public static byte[] toggleNPCShow(int oid, boolean hide, boolean viewNameTag) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TOGGLE_VISIBLE);
            mpw.writeInt(oid);
            mpw.write(hide ? 0 : 1);        // bView
            mpw.write(viewNameTag ? 0 : 1); // bViewNameTag
            
            return mpw.getPacket();
        }

        public static byte[] setNPCSpecialAction(int oid, String action, int duration, boolean localAct) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_SET_SPECIAL_ACTION);
            mpw.writeInt(oid);
            mpw.writeMapleAsciiString(action); // sName
            mpw.writeInt(duration);            // tDuration
            mpw.write(localAct ? 0 : 1);       // bLocalAct
            
            return mpw.getPacket();
        }

        public static byte[] setNPCForceMove(int oid, int forceX, int moveX, int speedRate) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_SET_FORCE_MOVE);
            mpw.writeInt(oid);
            mpw.writeInt(forceX);    // nForceX
            mpw.writeInt(moveX);     // nMoveX
            mpw.writeInt(speedRate); // nSpeedRate
            
            return mpw.getPacket();
        }

        public static byte[] setNPCScriptable() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_SET_SCRIPT);

            List<Pair<Integer, String>> npcs = new LinkedList();
            npcs.add(new Pair<>(9070006, "Why...why has this happened to me? My knightly honor... My knightly pride..."));
            npcs.add(new Pair<>(9000021, "Are you enjoying the event?"));

            mpw.write(npcs.size());
            for (Pair<Integer, String> s : npcs) {
                mpw.writeInt(s.getLeft());
                mpw.writeMapleAsciiString(s.getRight());
                mpw.writeInt(0);
//                mpw.writeInt(Integer.MAX_VALUE);
                mpw.write(0);
            }
            return mpw.getPacket();
        }

        public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type) {
            return getNPCTalk(npc, msgType, talk, endBytes, type, npc);
        }

        public static byte[] getNPCTalk(int npc, byte msgType, String talk, String endBytes, byte type, int diffNPC) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(4);
            mpw.writeInt(npc);
            mpw.write(0);//v141, boolean if true another int
            mpw.write(msgType);
            mpw.writeShort(type); // mask
            if ((type & 0x4) != 0) {
                mpw.writeInt(diffNPC);
            }
            mpw.writeMapleAsciiString(talk);
            mpw.write(HexTool.getByteArrayFromHexString(endBytes));
            
            return mpw.getPacket();
        }

        public static byte[] getEnglishQuiz(int npc, byte type, int diffNPC, String talk, String endBytes) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(4);
            mpw.writeInt(npc);
            mpw.write(10); //not sure
            mpw.write(type);
            if ((type & 0x4) != 0) {
                mpw.writeInt(diffNPC);
            }
            mpw.writeMapleAsciiString(talk);
            mpw.write(HexTool.getByteArrayFromHexString(endBytes));

            return mpw.getPacket();
        }

        public static byte[] getAdviceTalk(String[] wzinfo) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(8);
            mpw.writeInt(0);
            mpw.write(1);
            mpw.write(1);
            mpw.write(wzinfo.length);
            for (String data : wzinfo) {
                mpw.writeMapleAsciiString(data);
            }
            return mpw.getPacket();
        }

        public static byte[] getSlideMenu(int npcid, int type, int lasticon, String sel) {
            //Types: 0 - map selection 1 - neo city map selection 2 - korean map selection 3 - tele rock map selection 4 - dojo buff selection
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(4); //slide menu
            mpw.writeInt(npcid);
            mpw.write(0);
            mpw.writeShort(0x11);//0x12
            mpw.writeInt(type); //menu type
            mpw.writeInt(type == 0 ? lasticon : 0); //last icon on menu
            mpw.writeMapleAsciiString(sel);

            return mpw.getPacket();
        }

        public static byte[] getNPCTalkStyle(int npc, String talk, int[] args, boolean second) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(4);
            mpw.writeInt(npc);
            mpw.write(0);
            mpw.writeShort(9);
            mpw.writeShort(second ? 1 : 0);//new143
            mpw.writeMapleAsciiString(talk);
            mpw.write(args.length);

            for (int i = 0; i < args.length; i++) {
                mpw.writeInt(args[i]);
            }
            return mpw.getPacket();
        }

        public static byte[] getNPCTalkNum(int npc, String talk, int def, int min, int max) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(4);
            mpw.writeInt(npc);
            mpw.write(0);//new 142
            mpw.writeShort(4);
            mpw.writeMapleAsciiString(talk);
            mpw.writeInt(def);
            mpw.writeInt(min);
            mpw.writeInt(max);
            mpw.writeInt(0);

            return mpw.getPacket();
        }

        public static byte[] getNPCTalkText(int npc, String talk) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(4);
            mpw.writeInt(npc);
            mpw.write(0);
            mpw.writeShort(3); //3 regular 6 quiz
           // mpw.write(0); //Removed in v144
            mpw.writeMapleAsciiString(talk);
            mpw.writeInt(0);
            mpw.writeInt(0);

            return mpw.getPacket();
        }

        public static byte[] getNPCTalkQuiz(int npc, String caption, String talk, int time) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(4);
            mpw.writeInt(npc);
            mpw.write(0);
            mpw.writeShort(6);
            mpw.write(0);
            mpw.writeMapleAsciiString(caption);
            mpw.writeMapleAsciiString(talk);
            mpw.writeShort(0);
            mpw.writeInt(0);
            mpw.writeInt(0xF); //no idea
            mpw.writeInt(time); //seconds

            return mpw.getPacket();
        }

        public static byte[] getSelfTalkText(String text) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(3);
            mpw.writeInt(0);
            mpw.writeInt(1);
            mpw.writeShort(0);
            mpw.write(17);
            mpw.write(0);
            mpw.writeMapleAsciiString(text);
            mpw.write(0);
            mpw.write(1);
            mpw.writeInt(0);
            return mpw.getPacket();
        }

        public static byte[] getNPCTutoEffect(String effect) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(3);
            mpw.writeInt(0);
            mpw.write(0);
            mpw.write(1);
            mpw.writeShort(257);
            mpw.writeMapleAsciiString(effect);
            return mpw.getPacket();
        }

        public static byte[] getCutSceneSkip() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(3);
            mpw.writeInt(0);
            mpw.write(1);
            mpw.writeInt(0);
            mpw.write(2);
            mpw.write(5);
            mpw.writeInt(9010000); //Maple administrator
            mpw.writeMapleAsciiString("Would you like to skip the tutorial cutscenes?");
            return mpw.getPacket();
        }

        public static byte[] getDemonSelection() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(3);
            mpw.writeInt(0);
            mpw.write(1);
            mpw.writeInt(2159311); //npc
            mpw.write(0x16);
            mpw.write(1);
            mpw.writeShort(1);
            mpw.writeZeroBytes(8);
            return mpw.getPacket();
        }

        public static byte[] getAngelicBusterAvatarSelect(int npc) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(4);
            mpw.writeInt(npc);
            mpw.write(0);
            mpw.writeShort(0x17);
            return mpw.getPacket();
        }

        public static byte[] getEvanTutorial(String data) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.NPC_TALK);
            mpw.write(8);
            mpw.writeInt(0);
            mpw.write(1);
            mpw.write(1);
            mpw.write(1);
            mpw.writeMapleAsciiString(data);

            return mpw.getPacket();
        }

        public static byte[] getNPCShop(MapleShop shop, MapleClient c) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_NPC_SHOP);
            int bPet = 0;
            mpw.write(bPet); // if statement for dwPetTemplateID
            if (bPet > 0) {
            	mpw.writeInt(0); // dwPetTemplateID
            }
            PacketHelper.addShopInfo(mpw, shop, c);

            return mpw.getPacket();
        }
        
        public static byte[] confirmShopTransaction(byte code, MapleShop shop, MapleClient c, int indexBought) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CONFIRM_SHOP_TRANSACTION);
            mpw.write(code);
            if (code == 8) {
                PacketHelper.addShopInfo(mpw, shop, c);
            } else {
                mpw.write(indexBought >= 0 ? 1 : 0);
                if (indexBought >= 0) {
                    mpw.writeInt(indexBought);
                    mpw.writeInt(0);
                    mpw.writeShort(0);
                } else {
                    mpw.writeInt(0);
                	mpw.write(0);
                }
            }

            return mpw.getPacket();
        }

        public static byte[] getStorage(int npcId, byte slots, Collection<Item> items, long meso) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_STORAGE);
            mpw.write(22);
            mpw.writeInt(npcId);
            mpw.write(slots);
            mpw.writeShort(126);
            mpw.writeShort(0);
            mpw.writeInt(0);
            mpw.writeLong(meso);
            mpw.writeShort(0);
            mpw.write((byte) items.size());
            for (Item item : items) {
                PacketHelper.addItemInfo(mpw, item);
            }
            mpw.writeZeroBytes(2);//4

            return mpw.getPacket();
        }

        public static byte[] getStorageFull() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_STORAGE);
            mpw.write(17);

            return mpw.getPacket();
        }

        public static byte[] mesoStorage(byte slots, long meso) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_STORAGE);
            mpw.write(19);
            mpw.write(slots);
            mpw.writeShort(2);
            mpw.writeShort(0);
            mpw.writeInt(0);
            mpw.writeLong(meso);

            return mpw.getPacket();
        }

        public static byte[] arrangeStorage(byte slots, Collection<Item> items, boolean changed) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_STORAGE);
            mpw.write(15);
            mpw.write(slots);
            mpw.write(124);
            mpw.writeZeroBytes(10);
            mpw.write(items.size());
            for (Item item : items) {
                PacketHelper.addItemInfo(mpw, item);
            }
            mpw.write(0);
            return mpw.getPacket();
        }

        public static byte[] storeStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_STORAGE);
            mpw.write(13);
            mpw.write(slots);
            mpw.writeShort(type.getBitfieldEncoding());
            mpw.writeShort(0);
            mpw.writeInt(0);
            mpw.write(items.size());
            for (Item item : items) {
                PacketHelper.addItemInfo(mpw, item);
            }
            return mpw.getPacket();
        }

        public static byte[] takeOutStorage(byte slots, MapleInventoryType type, Collection<Item> items) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_STORAGE);
            mpw.write(9);
            mpw.write(slots);
            mpw.writeShort(type.getBitfieldEncoding());
            mpw.writeShort(0);
            mpw.writeInt(0);
            mpw.write(items.size());
            for (Item item : items) {
                PacketHelper.addItemInfo(mpw, item);
            }
            return mpw.getPacket();
        }
    }

    public static class SummonPacket {

        public static byte[] spawnSummon(MapleSummon summon, boolean animated) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SPAWN_SUMMON);
            mpw.writeInt(summon.getOwnerId());
            mpw.writeInt(summon.getObjectId());
            mpw.writeInt(summon.getSkill());
            mpw.write(summon.getOwnerLevel() - 1);
            mpw.write(summon.getSkillLevel());
            mpw.writePos(summon.getPosition());
            mpw.write((summon.getSkill() == 32111006) || (summon.getSkill() == 33101005) ? 5 : 4);// Summon Reaper Buff - Call of the Wild
            if ((summon.getSkill() == 35121003) && (summon.getOwner().getMap() != null)) {//Giant Robot SG-88
                mpw.writeShort(summon.getOwner().getMap().getFootholds().findBelow(summon.getPosition()).getId());
            } else {
                mpw.writeShort(summon.getOwner().getMap().getFootholds().findBelow(summon.getPosition()).getId());
            }
            mpw.write(summon.getMovementType().getValue());
            mpw.write(summon.getSummonType());
            mpw.write(animated ? 1 : 0);
            mpw.writeInt(0); // dwMobID
            mpw.write(0); // bFlyMob
            mpw.write(1); // bBeforeFirstAttack
            mpw.writeInt(0); // nLookID
            mpw.writeInt(0); // nBulletID
            MapleCharacter chr = summon.getOwner();
            mpw.write((summon.getSkill() == 4341006) && (chr != null) ? 1 : 0); // Mirrored Target
            if ((summon.getSkill() == 4341006) && (chr != null)) { // Mirrored Target
                PacketHelper.addCharLook(mpw, chr, true, false);
            }
            if (summon.getSkill() == 35111002) {// Rock 'n Shock
                mpw.write(0);
            }
            if (summon.getSkill() == 42111003) {
                mpw.writeZeroBytes(8);
            }
            if (summon.getSkill() == 3121013) {
                chr.dropMessage(6,"no");
            }
            
            mpw.write(0); // bJaguarActive
            mpw.writeInt(0); // tSummonTerm

            return mpw.getPacket();
        }

        public static byte[] removeSummon(int ownerId, int objId) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_SUMMON);
            mpw.writeInt(ownerId);
            mpw.writeInt(objId);
            mpw.write(10);

            return mpw.getPacket();
        }

        public static byte[] removeSummon(MapleSummon summon, boolean animated) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REMOVE_SUMMON);
            mpw.writeInt(summon.getOwnerId());
            mpw.writeInt(summon.getObjectId());
            if (animated) {
                switch (summon.getSkill()) {
                    case 35121003:
                        mpw.write(10);
                        break;
                    case 33101008:
                    case 35111001:
                    case 35111002:
                    case 35111005:
                    case 35111009:
                    case 35111010:
                    case 35111011:
                    case 35121009:
                    case 35121010:
                    case 35121011:
                        mpw.write(5);
                        break;
                    default:
                        mpw.write(4);
                        break;
                }
            } else {
                mpw.write(1);
            }

            return mpw.getPacket();
        }

        public static byte[] moveSummon(int cid, int oid, Point startPos, List<LifeMovementFragment> moves) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MOVE_SUMMON);
            mpw.writeInt(cid);
            mpw.writeInt(oid);
            mpw.writeInt(0);
            mpw.writePos(startPos);
            mpw.writeInt(0);
            PacketHelper.serializeMovementList(mpw, moves);

            return mpw.getPacket();
        }

        public static byte[] summonAttack(int cid, int summonSkillId, byte animation, List<Pair<Integer, Integer>> allDamage, int level, boolean darkFlare) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SUMMON_ATTACK);
            mpw.writeInt(cid);
            mpw.writeInt(summonSkillId);
            mpw.write(level - 1);
            mpw.write(animation);
            mpw.write(allDamage.size());
            for (Pair attackEntry : allDamage) {
                mpw.writeInt(((Integer) attackEntry.left).intValue());
                mpw.write(7);
                mpw.writeInt(((Integer) attackEntry.right).intValue());
            }
            mpw.write(darkFlare ? 1 : 0);

            return mpw.getPacket();
        }

        public static byte[] pvpSummonAttack(int cid, int playerLevel, int oid, int animation, Point pos, List<AttackPair> attack) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SUMMON_PVP_ATTACK);
            mpw.writeInt(cid);
            mpw.writeInt(oid);
            mpw.write(playerLevel);
            mpw.write(animation);
            mpw.writePos(pos);
            mpw.writeInt(0);
            mpw.write(attack.size());
            for (AttackPair p : attack) {
                mpw.writeInt(p.objectid);
                mpw.writePos(p.point);
                mpw.write(p.attack.size());
                mpw.write(0);
                for (Pair atk : p.attack) {
                    mpw.writeInt(((Integer) atk.left).intValue());
                }
            }

            return mpw.getPacket();
        }

        public static byte[] summonSkill(int cid, int summonSkillId, int newStance) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SUMMON_SKILL);
            mpw.writeInt(cid);
            mpw.writeInt(summonSkillId);
            mpw.write(newStance);

            return mpw.getPacket();
        }

        public static byte[] damageSummon(int cid, int summonSkillId, int damage, int unkByte, int monsterIdFrom) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DAMAGE_SUMMON);
            mpw.writeInt(cid);
            mpw.writeInt(summonSkillId);
            mpw.write(unkByte);
            mpw.writeInt(damage);
            mpw.writeInt(monsterIdFrom);
            mpw.write(0);

            return mpw.getPacket();
        }
    }

    public static class UIPacket {

        public static byte[] getDirectionStatus(boolean enable) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIRECTION_STATUS);
            mpw.write(enable ? 1 : 0);

            return mpw.getPacket();
        }
        
        public static byte[] openUI(int type) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_UI);
            mpw.write(type);

            return mpw.getPacket();
        }

        public static byte[] sendRepairWindow(int npc) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_UI_OPTION);
            mpw.writeInt(33);
            mpw.writeInt(npc);
            mpw.writeInt(0);//new143

            return mpw.getPacket();
        }

        public static byte[] sendJewelCraftWindow(int npc) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_UI_OPTION);
            mpw.writeInt(104);
            mpw.writeInt(npc);
            mpw.writeInt(0);//new143

            return mpw.getPacket();
        }

        public static byte[] startAzwan(int npc) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_UI_OPTION);
            mpw.writeInt(70);
            mpw.writeInt(npc);
            mpw.writeInt(0);//new143
            return mpw.getPacket();
        }

        public static byte[] openUIOption(int type, int npc) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_UI_OPTION);
            mpw.writeInt(type);
            mpw.writeInt(npc);
            return mpw.getPacket();
        }

        public static byte[] sendDojoResult(int points) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_UI_OPTION);
            mpw.writeInt(0x48);
            mpw.writeInt(points);

            return mpw.getPacket();
        }

        public static byte[] sendAzwanResult() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_UI_OPTION);
            mpw.writeInt(0x45);
            mpw.writeInt(0);

            return mpw.getPacket();
        }

        public static byte[] DublStart(boolean dark) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            mpw.write(0x28);
            mpw.write(dark ? 1 : 0);

            return mpw.getPacket();
        }

        public static byte[] DublStartAutoMove() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MOVE_SCREEN);
            mpw.write(3);
            mpw.writeInt(2);

            return mpw.getPacket();
        }

        public static byte[] IntroLock(boolean enable) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INTRO_LOCK);
            mpw.write(enable ? 1 : 0);
            mpw.writeInt(0);

            return mpw.getPacket();
        }

        public static byte[] IntroEnableUI(int enable) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INTRO_ENABLE_UI);
            mpw.write(enable > 0 ? 1 : 0);
            if (enable > 0) {
                mpw.writeShort(enable);
                mpw.write(0);
            } else {
            	//mpw.write(enable < 0 ? 1 : 0);
            }
            
            System.out.println(mpw.toString());
            return mpw.getPacket();
        }

        public static byte[] IntroDisableUI(boolean enable) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.INTRO_DISABLE_UI);
            mpw.write(enable ? 1 : 0);

            return mpw.getPacket();
        }

        public static byte[] summonHelper(boolean summon) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SUMMON_HINT);
            mpw.write(summon ? 1 : 0);

            return mpw.getPacket();
        }

        public static byte[] summonMessage(int type) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SUMMON_HINT_MSG);
            mpw.write(1);
            mpw.writeInt(type);
            mpw.writeInt(7000);

            return mpw.getPacket();
        }

        public static byte[] summonMessage(String message) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SUMMON_HINT_MSG);
            mpw.write(0);
            mpw.writeMapleAsciiString(message);
            mpw.writeInt(200);
            mpw.writeShort(0);
            mpw.writeInt(10000);

            return mpw.getPacket();
        }

        public static byte[] getDirectionInfo(int type, int value, int x) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIRECTION_INFO);
             if (x > 0) {
                mpw.write(x);
            }
            mpw.write((byte) type);
            mpw.writeInt(value);

                        
            return mpw.getPacket();
        }

        public static byte[] getDirectionInfo(int type, int value) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIRECTION_INFO);
            mpw.write((byte) type);
            mpw.writeInt(value);
                        
            return mpw.getPacket();
        }
        
        /**
         * Delays map events for a {@code delay} of milliseconds.
         * @param delay
         * @return
         * @see CInGameDirectionEvent::OnInGameDirectionEvent
         */
        public static byte[] delayDirectionInfo(int delay) {
        	MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIRECTION_INFO);
        	mpw.write(1);
            mpw.writeInt(delay);
                        
            return mpw.getPacket();
        }
        
        /**
         * Forces the character to move in a certain direction during map events.
         * @param input
         * @return
         */
        public static byte[] forceMoveCharacter(int input) {
        	MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIRECTION_INFO);
        	mpw.write(3);
            mpw.writeInt(input);
                        
            return mpw.getPacket();
        }
        
        public static byte[] getDirectionInfo(String data, int value, int x, int y, int a, int b) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIRECTION_INFO);
            mpw.write(2);
            mpw.writeMapleAsciiString(data);
            mpw.writeInt(value);
            mpw.writeInt(x);
            mpw.writeInt(y);
            mpw.write(a);
            if (a > 0) {
                mpw.writeInt(0);
            }
            mpw.write(b);
            if (b > 1) {
                mpw.writeInt(0);
            }
            
            return mpw.getPacket();
        }

        public static byte[] getDirectionEffect(String data, int value, int x, int y) {
            return getDirectionEffect(data, value, x, y, 0);
        }

        public static byte[] getDirectionEffect(String data, int value, int x, int y, int npc) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIRECTION_INFO);
            mpw.write(2);
            mpw.writeMapleAsciiString(data);
            mpw.writeInt(value);
            mpw.writeInt(x);
            mpw.writeInt(y);
            mpw.write(1);
            mpw.writeInt(0);
            mpw.write(1);
            mpw.writeInt(npc); // dwNpcID
            mpw.write(1); // bNotOrigin
            mpw.write(0);

            return mpw.getPacket();
        }

        public static byte[] getDirectionInfoNew(byte x, int value, int a, int b) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIRECTION_INFO);
            mpw.write(5);
            mpw.write(x);
            mpw.writeInt(value);
            if (x == 0) {
                mpw.writeInt(a);
                mpw.writeInt(b);
            }


            return mpw.getPacket();
        }
        
        public static byte[] getDirectionInfoNew2(byte x, int value) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIRECTION_INFO);
            mpw.write(5);
            mpw.write(x);
            mpw.writeInt(value);


            return mpw.getPacket();
        }

        public static byte[] getDirectionEffect1(String data, int value, int x, int y, int npc) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIRECTION_INFO);
            mpw.write(2);
            mpw.writeAsciiString(data);
            mpw.writeInt(value);
            mpw.writeInt(x);
            mpw.writeInt(y);
            mpw.write(1);
            mpw.writeInt(npc);
            mpw.write(0);

            // Added for BeastTamer
            return mpw.getPacket();
        }

        public static byte[] getDirectionInfoNew(byte x, int value) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DIRECTION_INFO);
            mpw.write(5);
            mpw.write(x);
            mpw.writeInt(value);
            if (x == 0) {
                mpw.writeInt(value);
                mpw.writeInt(value);
            }

            return mpw.getPacket();
        }

        public static byte[] moveScreen(int x) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MOVE_SCREEN_X);
            mpw.writeInt(x);
            mpw.writeInt(0);
            mpw.writeInt(0);

            return mpw.getPacket();
        }

        public static byte[] screenDown() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.MOVE_SCREEN_DOWN);

            return mpw.getPacket();
        }

        public static byte[] resetScreen() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.RESET_SCREEN);

            return mpw.getPacket();
        }

        public static byte[] reissueMedal(int itemId, int type) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REISSUE_MEDAL);
            mpw.write(type);
            mpw.writeInt(itemId);

            return mpw.getPacket();
        }

        public static byte[] playMovie(String data, boolean show) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PLAY_MOVIE);
            mpw.writeMapleAsciiString(data);
            mpw.write(show ? 1 : 0);

            return mpw.getPacket();
        }

        public static byte[] setRedLeafStatus(int joejoe, int hermoninny, int littledragon, int ika) {
            //packet made to set status
            //should remove it and make a handler for it, it's a recv opcode
            /*
             * inPacket:
             * E2 9F 72 00
             * 5D 0A 73 01
             * E2 9F 72 00
             * 04 00 00 00
             * 00 00 00 00
             * 75 96 8F 00
             * 55 01 00 00
             * 76 96 8F 00
             * 00 00 00 00
             * 77 96 8F 00
             * 00 00 00 00
             * 78 96 8F 00
             * 00 00 00 00
             */
            MaplePacketWriter mpw = new MaplePacketWriter();

            //mpw.writeShort();
            mpw.writeInt(7512034); //no idea
            mpw.writeInt(24316509); //no idea
            mpw.writeInt(7512034); //no idea
            mpw.writeInt(4); //no idea
            mpw.writeInt(0); //no idea
            mpw.writeInt(9410165); //joe joe
            mpw.writeInt(joejoe); //amount points added
            mpw.writeInt(9410166); //hermoninny
            mpw.writeInt(hermoninny); //amount points added
            mpw.writeInt(9410167); //little dragon
            mpw.writeInt(littledragon); //amount points added
            mpw.writeInt(9410168); //ika
            mpw.writeInt(ika); //amount points added

            return mpw.getPacket();
        }

        public static byte[] sendRedLeaf(int points, boolean viewonly) {
            /*
             * inPacket:
             * 73 00 00 00
             * 0A 00 00 00
             * 01
             */
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.OPEN_UI_OPTION);
            mpw.writeInt(0x73);
            mpw.writeInt(points);
            mpw.write(viewonly ? 1 : 0); //if view only, then complete button is disabled

            return mpw.getPacket();
        }
    }

    public static class EffectPacket {

        public static byte[] showForeignEffect(int effect) {
            return showForeignEffect(-1, effect);
        }

        public static byte[] showForeignEffect(int charid, int effect) {
        	MaplePacketWriter mpw;
            if (charid == -1) {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            } else {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_FOREIGN_EFFECT);
                mpw.writeInt(charid);
            }
            mpw.write(effect);

            System.out.println("showForeignEffect");
            return mpw.getPacket();
        }

        public static byte[] showItemLevelupEffect() {
            return showForeignEffect(18);
        }

        public static byte[] showForeignItemLevelupEffect(int cid) {
            return showForeignEffect(cid, 18);
        }

        public static byte[] showOwnDiceEffect(int skillid, int effectid, int effectid2, int level) {
            return showDiceEffect(-1, skillid, effectid, effectid2, level);
        }

        public static byte[] showDiceEffect(int charid, int skillid, int effectid, int effectid2, int level) {
        	MaplePacketWriter mpw;
            if (charid == -1) {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            } else {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_FOREIGN_EFFECT);
                mpw.writeInt(charid);
            }
            mpw.write(3);
            mpw.writeInt(effectid);
            mpw.writeInt(effectid2);
            mpw.writeInt(skillid);
            mpw.write(level);
            mpw.write(0);
            mpw.writeZeroBytes(100);

            return mpw.getPacket();
        }

        public static byte[] useCharm(byte charmsleft, byte daysleft, boolean safetyCharm) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            mpw.write(8);
            mpw.write(safetyCharm ? 1 : 0);
            mpw.write(charmsleft);
            mpw.write(daysleft);
            if (!safetyCharm) {
                mpw.writeInt(0);
            }

            return mpw.getPacket();
        }

        public static byte[] Mulung_DojoUp2() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            mpw.write(10);

            return mpw.getPacket();
        }

        public static byte[] showOwnHpHealed(int amount) {
            return showHpHealed(-1, amount);
        }

        /**
         * Sends a packet that shows the amount of HP you healed.
         * Usually sends after you use a skill such as Recovery.
         * 
         * @param charid This is the character's ID.
         * @param amount This is the amount of HP to display.
         * @return
         */
        public static byte[] showHpHealed(int charid, int amount) {
        	MaplePacketWriter mpw;
            if (charid == -1) {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            } else {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_FOREIGN_EFFECT);
                mpw.writeInt(charid);
            }
            mpw.write(15); // This value changes between patches.
            mpw.write(amount);

            return mpw.getPacket();
        }

        public static byte[] showRewardItemAnimation(int itemId, String effect) {
            return showRewardItemAnimation(itemId, effect, -1);
        }

        public static byte[] showRewardItemAnimation(int itemId, String effect, int from_playerid) {
        	MaplePacketWriter mpw;
            if (from_playerid == -1) {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            } else {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_FOREIGN_EFFECT);
                mpw.writeInt(from_playerid);
            }
            mpw.write(17);
            mpw.writeInt(itemId);
            mpw.write((effect != null) && (effect.length() > 0) ? 1 : 0);
            if ((effect != null) && (effect.length() > 0)) {
                mpw.writeMapleAsciiString(effect);
            }

            return mpw.getPacket();
        }

        public static byte[] showCashItemEffect(int itemId) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            mpw.write(23);
            mpw.writeInt(itemId);

            return mpw.getPacket();
        }

        public static byte[] ItemMaker_Success() {
            return ItemMaker_Success_3rdParty(-1);
        }

        public static byte[] ItemMaker_Success_3rdParty(int from_playerid) {
        	MaplePacketWriter mpw;
            if (from_playerid == -1) {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            } else {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_FOREIGN_EFFECT);
                mpw.writeInt(from_playerid);
            }
            mpw.write(19);
            mpw.writeInt(0);

            return mpw.getPacket();
        }

        public static byte[] useWheel(byte charmsleft) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            mpw.write(24);
            mpw.write(charmsleft);

            return mpw.getPacket();
        }

        public static byte[] showOwnBuffEffect(int skillid, int effectid, int playerLevel, int skillLevel) {
            return showBuffEffect(-1, skillid, effectid, playerLevel, skillLevel, (byte) 3);
        }

        public static byte[] showOwnBuffEffect(int skillid, int effectid, int playerLevel, int skillLevel, byte direction) {
            return showBuffEffect(-1, skillid, effectid, playerLevel, skillLevel, direction);
        }

        public static byte[] showBuffEffect(int charid, int skillid, int effectid, int playerLevel, int skillLevel) {
            return showBuffEffect(charid, skillid, effectid, playerLevel, skillLevel, (byte) 3);
        }

        public static byte[] showBuffEffect(int charid, int skillid, int effectid, int playerLevel, int skillLevel, byte direction) {
        	MaplePacketWriter mpw;
            if (charid == -1) {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            } else {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_FOREIGN_EFFECT);
                mpw.writeInt(charid);
            }
            mpw.write(effectid);
            mpw.writeInt(skillid);
            if(effectid == 1) {
            	mpw.write(playerLevel);
            }
            /*
            if ((effectid == 2) && (skillid == 31111003)) {
                mpw.writeInt(0);
            }
            */
            mpw.write(skillLevel);
            
            /*
            if ((direction != 3) || (skillid == 1320006) || (skillid == 30001062) || (skillid == 30001061)) {
                mpw.write(direction);
            }

            if (skillid == 30001062) {
                mpw.writeInt(0);
            }
            */
            mpw.writeZeroBytes(10); // Not correct, just added so wouldn't dc.

            System.out.println("ShowBuffEffect");
            return mpw.getPacket();
        }

        /**
         * Sends a packet to display a WZ effect.
         * Ex: Maple Island Beginner Job Animation
         * 
         * @param data String directory to the WZ effect.
         * @return
         */
        public static byte[] ShowWZEffect(String data) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            mpw.writeLong(24); // Last updated: v172.1
            mpw.writeShort(0);
            mpw.writeMapleAsciiString(data);

            return mpw.getPacket();
        }

        public static byte[] showOwnCraftingEffect(String effect, byte direction, int time, int mode) {
            return showCraftingEffect(-1, effect, direction, time, mode);
        }

        public static byte[] showCraftingEffect(int charid, String effect, byte direction, int time, int mode) {
            MaplePacketWriter mpw;
            if (charid == -1) {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            } else {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_FOREIGN_EFFECT);
                mpw.writeInt(charid);
            }
            mpw.write(34); //v140
            mpw.writeMapleAsciiString(effect);
            mpw.write(direction);
            mpw.writeInt(time);
            mpw.writeInt(mode);
            if (mode == 2) {
                mpw.writeInt(0);
            }

            return mpw.getPacket();
        }

        public static byte[] TutInstructionalBalloon(String data) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            mpw.write(25);//was 26 in v140
            mpw.writeMapleAsciiString(data);
            mpw.writeInt(1);

            return mpw.getPacket();
        }

        public static byte[] showOwnPetLevelUp(byte index) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            mpw.write(6);
            mpw.write(0);
            mpw.write(index);

            return mpw.getPacket();
        }

        public static byte[] showOwnChampionEffect() {
            return showChampionEffect(-1);
        }

        public static byte[] showChampionEffect(int from_playerid) {
        	MaplePacketWriter mpw;
            if (from_playerid == -1) {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            } else {
            	mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_FOREIGN_EFFECT);
                mpw.writeInt(from_playerid);
            }
            mpw.write(34);
            mpw.writeInt(30000);

            return mpw.getPacket();
        }

        public static byte[] updateDeathCount(int deathCount) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.DEATH_COUNT);
            mpw.writeInt(deathCount);

            return mpw.getPacket();
        }
        
    }

    public static byte[] showWeirdEffect(String effect, int itemId) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
		mpw.write(0x20);
        mpw.writeMapleAsciiString(effect);
        mpw.write(1);
        mpw.writeInt(0);//weird high number is it will keep showing it lol
        mpw.writeInt(2);
        mpw.writeInt(itemId);
        return mpw.getPacket();
    }

    public static byte[] showWeirdEffect(int chrId, String effect, int itemId) {
    	MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_FOREIGN_EFFECT);
		mpw.writeInt(chrId);
        mpw.write(0x20);
        mpw.writeMapleAsciiString(effect);
        mpw.write(1);
        mpw.writeInt(0);//weird high number is it will keep showing it lol
        mpw.writeInt(2);//this makes it read the itemId
        mpw.writeInt(itemId);
        return mpw.getPacket();
    }

    public static byte[] enchantResult(int result) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.STRENGTHEN_UI);
		mpw.writeInt(result);//0=fail/1=sucess/2=idk/3=shows stats
        return mpw.getPacket();
    }

    public static byte[] sendSealedBox(short slot, int itemId, List<Integer> items) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SEALED_BOX);
		mpw.writeShort(slot);
        mpw.writeInt(itemId);
        mpw.writeInt(items.size());
        for (int item : items) {
            mpw.writeInt(item);
        }

        return mpw.getPacket();
    }
    
    public static byte[] sendBoxDebug(short opcode, int itemId, List<Integer> items) {
        System.out.println("sendBoxDebug\r\n" + opcode + "\r\n" + itemId + "\r\n");
        MaplePacketWriter mpw = new MaplePacketWriter();

        mpw.writeShort(opcode);
        mpw.writeShort(1);
        mpw.writeInt(itemId);
        mpw.writeInt(items.size());
        for (int item : items) {
            mpw.writeInt(item);
        }
        System.out.println("sendBoxDebug end");
        return mpw.getPacket();
    }
    
    public static byte[] getCassandrasCollection() {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CASSANDRAS_COLLECTION);
		mpw.write(6);

        return mpw.getPacket();
    }

    public static byte[] unsealBox(int reward) {
        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
		mpw.write(0x31);
        mpw.write(1);
        mpw.writeInt(reward);
        mpw.writeInt(1);

        return mpw.getPacket();
    }
    
    /**
     * Shows a Revive UI that sends the player to the nearest town after they have died.
     * @return <code>01 00 00 00 00 00 00 00 00</code> packet
     */
    public static byte[] showReviveUI() {
    	MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REVIVE_UI);
		mpw.write(1); // 1 to show; 0 to not show
    	mpw.writeLong(0);
    	
    	return mpw.getPacket();
    }

}
