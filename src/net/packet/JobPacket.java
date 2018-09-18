package net.packet;

import client.MapleBuffStat;
import client.character.MapleCharacter;
import net.SendPacketOpcode;

import java.awt.Point;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import server.Randomizer;
import server.MapleStatEffect;
import server.MapleStatInfo;
import tools.HexTool;
import tools.data.MaplePacketWriter;

/**
 *
 * @author Itzik
 */
public class JobPacket {
    
    public static class WindArcherPacket {
    	
    	public static byte[] giveWindArcherBuff(int buffid, int bufflength, Map<MapleBuffStat, Integer> statups, MapleStatEffect effect, MapleCharacter chr) {
    		MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
    		PacketHelper.writeBuffMask(mpw, statups);
    		try {
                byte count=0;
                StringBuilder statValue= new StringBuilder();
                Map.Entry[] stat= new Map.Entry[statups.size()];
                for (Map.Entry temp : statups.entrySet()) {
  		        	stat[count]= temp;
  		        	statValue.append((int)stat[count].getValue()).append(" - ");
  		        	count++;
  		        }
                switch(buffid){
                	case 13001022:
	  		    	  mpw.writeShort((int)stat[1].getValue());
	  		    	  mpw.writeInt(buffid);
			    	  mpw.writeInt(bufflength);
			    	  
			    	  mpw.writeZeroBytes(9);
  			    	  mpw.writeInt(1);
  			    	  mpw.writeInt(buffid);
  			    	  mpw.writeInt((int)stat[0].getValue());
  			    	  mpw.write(HexTool.getByteArrayFromHexString("10 00 32 23 00 00 00 00"));
  			    	  mpw.writeInt(bufflength);
  			    	  mpw.write(HexTool.getByteArrayFromHexString("00 00 00 00 01 00 00 00 00"));
	  		    	  break;
  			      case 13101024://Sylvan Aid now fiex :3
  			    	  mpw.writeShort((int)stat[0].getValue());
  			    	  mpw.writeInt(buffid);
  			    	  mpw.writeInt(bufflength);
  			    	  
  			    	  mpw.writeShort((int)stat[2].getValue());
  			    	  mpw.writeInt(buffid);
  			    	  mpw.writeInt(bufflength);
  			    	  
  			    	  mpw.writeZeroBytes(9);
  			    	  mpw.writeInt(1);
  			    	  mpw.writeInt(buffid);
  			    	  mpw.writeInt((int)stat[1].getValue());
  			    	  mpw.writeLong(0);
  			    	  mpw.writeInt(bufflength);
  			    	  mpw.writeInt(0);
  			    	  mpw.write(1);
  			    	  mpw.writeInt(0);
  			    	  break;
                  case 13111023://albatross work
                  case 13120008://max albatross NOW WORK - When albatross finished, you active again skill, else give DC and Error38
  			    	  mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
  			    	  mpw.write(HexTool.getByteArrayFromHexString("00 00 00 00 00 00 00 00 00 40 00 00 30 00 04 00 00 00 00 00 00 01 00 82 00 00 00 00 00 14 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "));
  			    	  int skillLevel= chr.getSkillLevel(13111023);
  			    	  int skill13120008Level= chr.getSkillLevel(13120008);
  			    	  int skill13111023Level= chr.getSkillLevel(13111023);
  			    	  
  			    	  mpw.writeShort(skill13120008Level/2);
  			    	  mpw.writeInt(13120008);
  			    	  mpw.writeInt(205999);
  			    	  
  			    	  mpw.writeShort(skill13120008Level);
  			    	  mpw.writeInt(13120008);
  			    	  mpw.writeInt(205999);
  			    	  
  			    	  mpw.writeZeroBytes(13);
  			    	  mpw.writeInt(2);
  			    	  
                      mpw.writeInt(13120008);
  			    	  mpw.writeInt((skill13120008Level*5)/3);
  			    	  mpw.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
  			    	  mpw.writeInt(bufflength);
  			    	  
  			    	  mpw.writeInt(1);
  			    	  mpw.writeInt(13120008);
  			    	  mpw.writeInt((skillLevel*300)/4);
  			    	  mpw.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
  			    	  mpw.writeInt(bufflength);
  			    	  
  			    	  mpw.writeInt(1);
  			    	  mpw.writeInt(13120008);
  			    	  mpw.writeInt(-2);
  			    	  mpw.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
  			    	  mpw.writeInt(bufflength);
  			    	  
  			    	  mpw.writeInt(2);
  			    	  mpw.writeInt(13111023);
  			    	  mpw.writeInt(skill13111023Level);
  			    	  mpw.write(HexTool.getByteArrayFromHexString("10 00 32 23 47 9F 01 00"));
  			    	  mpw.writeInt(bufflength);
  			    	  
  			    	  mpw.writeInt(13120008);
  			    	  mpw.writeInt((skill13120008Level/2)+10);
  			    	  mpw.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
  			    	  mpw.writeInt(bufflength);
  			    	  
  			    	  mpw.writeInt(1);
  			    	  mpw.writeInt(13120008);
  			    	  mpw.writeInt((skill13120008Level/2));
  			    	  mpw.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
  			    	  mpw.writeInt(bufflength);
  			    	  
  			    	  mpw.writeInt(1);
  			    	  mpw.writeInt(13120008);
  			    	  mpw.writeInt((skill13120008Level/2));
  			    	  mpw.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
  			    	  mpw.writeInt(bufflength);
  			    	  
  			    	  mpw.writeInt(1);
  			    	  mpw.writeInt(13120008);
  			    	  mpw.writeInt((skill13120008Level/2)+10);
  			    	  mpw.write(HexTool.getByteArrayFromHexString("56 9F 33 23 01 00 00 00"));
  			    	  mpw.writeInt(bufflength);
  			    	  
  			    	  mpw.writeInt(0);
  			    	  mpw.write(1);
  			    	  mpw.writeInt(0);
  			    	  break;   
  			      case 13121004://touch of the wind skill work
  			    	  mpw.writeShort((int)stat[0].getValue());
  			    	  mpw.writeInt(buffid);
			    	  mpw.writeInt(bufflength);
			    	  
			    	  mpw.writeShort((int)stat[1].getValue());
			    	  mpw.writeInt(buffid);
			    	  mpw.writeInt(bufflength);
			    	  
			    	  mpw.writeShort((int)stat[2].getValue());
			    	  mpw.writeInt(buffid);
			    	  mpw.writeInt(bufflength);
			    	  
			    	  mpw.writeZeroBytes(9);
			    	  mpw.writeInt(1);
			    	  mpw.writeInt(buffid);
			    	  mpw.writeInt((int)stat[3].getValue());
			    	  mpw.write(HexTool.getByteArrayFromHexString("C3 BF BB 33 00 00 00 00"));
			    	  mpw.writeInt(bufflength);
			    	  mpw.writeInt(0);
			    	  mpw.write(1);
			    	  mpw.writeInt(0);
  			    	  break;
  		      }
  	      } catch (Exception e ) { 
  	    	  e.printStackTrace();  
  	      }
  	      return mpw.getPacket();
  	  }
    	
    	public static byte [] TrifleWind(int cid, int skillid, int ga, int oid, int gu) {
    		MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
    		mpw.write(0);
    		mpw.writeInt(cid);
    		mpw.writeInt(7);
    		mpw.write(1);
    		mpw.writeInt(gu);
    		mpw.writeInt(oid);
    		mpw.writeInt(skillid);
    		for (int i = 1; i < ga; i++) {
    			mpw.write(1);
    			mpw.writeInt(2 + i);
    			mpw.writeInt(1);
    			mpw.writeInt(Randomizer.rand(0x2A, 0x2F));
    			mpw.writeInt(7 + i);
    			mpw.writeInt(Randomizer.rand(5, 0xAB));
    			mpw.writeInt(Randomizer.rand(0, 0x37));
    			mpw.writeLong(0);
    			mpw.writeInt(Randomizer.nextInt());
    			mpw.writeInt(0);
    		}
    		mpw.write(0);
    		mpw.writeZeroBytes(69); //for no dc goodluck charm! >:D xD
    		
    		return mpw.getPacket();
    	}
    }

    public static class PhantomPacket {
    	public static byte[] FinalJudgement() {
    		MaplePacketWriter packet = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
	        packet.writeLong(0);
	        packet.writeLong(0);
	        packet.writeLong(0);
	        packet.writeLong(0);
	        packet.writeLong(0x8000);
	        packet.writeLong(0);
	        packet.writeShort(1);
	        packet.writeInt(24121054);
	        packet.writeInt(30000);
	        packet.writeZeroBytes(9);
	        packet.writeInt(1000);
	        packet.write(1);
	        packet.writeInt(0);
	        return packet.getPacket();
    	}
            
        public static byte[] addStolenSkill(int jobNum, int index, int skill, int level) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_STOLEN_SKILLS);
            mpw.write(1);
            mpw.write(0);
            mpw.writeInt(jobNum);
            mpw.writeInt(index);
            mpw.writeInt(skill);
            mpw.writeInt(level);
            mpw.writeInt(0);
            mpw.write(0);

            return mpw.getPacket();
        }

        public static byte[] removeStolenSkill(int jobNum, int index) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UPDATE_STOLEN_SKILLS);
            mpw.write(1);
            mpw.write(3);
            mpw.writeInt(jobNum);
            mpw.writeInt(index);
            mpw.write(0);

            return mpw.getPacket();
        }

        public static byte[] replaceStolenSkill(int base, int skill) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.REPLACE_SKILLS);
            mpw.write(1);
            mpw.write(skill > 0 ? 1 : 0);
            mpw.writeInt(base);
            mpw.writeInt(skill);

            return mpw.getPacket();
        }

        public static byte[] gainCardStack(int oid, int runningId, int color, int skillid, int damage, int times) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
            mpw.write(0);
            mpw.writeInt(oid);
            mpw.writeInt(1);
            mpw.writeInt(damage);
            mpw.writeInt(skillid);
            for (int i = 0; i < times; i++) {
                mpw.write(1);
                mpw.writeInt(damage == 0 ? runningId + i : runningId);
                mpw.writeInt(color);
                mpw.writeInt(Randomizer.rand(15, 29));
                mpw.writeInt(Randomizer.rand(7, 11));
                mpw.writeInt(Randomizer.rand(0, 9));
            }
            mpw.write(0);
            
            mpw.writeZeroBytes(69); //for no DC it requires this do not remove

            return mpw.getPacket();
        }

        public static byte[] updateCardStack(final int total) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.PHANTOM_CARD);
            mpw.write(total);

            return mpw.getPacket();
        }

        public static byte[] getCarteAnimation(int cid, int oid, int job, int total, int numDisplay) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
            mpw.write(0);
            mpw.writeInt(cid);
            mpw.writeInt(1);

            mpw.writeInt(oid);
            mpw.writeInt(job == 2412 ? 24120002 : 24100003);
            mpw.write(1);
            for (int i = 1; i <= numDisplay; i++) {
                mpw.writeInt(total - (numDisplay - i));
                mpw.writeInt(job == 2412 ? 2 : 0);

                mpw.writeInt(15 + Randomizer.nextInt(15));
                mpw.writeInt(7 + Randomizer.nextInt(5));
                mpw.writeInt(Randomizer.nextInt(4));

                mpw.write(i == numDisplay ? 0 : 1);
            }

            return mpw.getPacket();
        }

        public static byte[] giveAriaBuff(int bufflevel, int buffid, int bufflength) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.DAMAGE_RATE, 0);
            statups.put(MapleBuffStat.DAMAGE_PERCENT, 0);
            PacketHelper.writeBuffMask(mpw, statups);
            for (int i = 0; i < 2; i++) {
                mpw.writeShort(bufflevel);
                mpw.writeInt(buffid);
                mpw.writeInt(bufflength);
            }
            mpw.writeZeroBytes(3);
            mpw.writeShort(0);
            mpw.write(0);
            return mpw.getPacket();
        }
    }

    public static class AngelicPacket {

        public static byte[] showRechargeEffect() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            mpw.write(0x2D);
            return mpw.getPacket();
        }

        public static byte[] RechargeEffect() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_SPECIAL_EFFECT);
            mpw.write(0x2D);
            return mpw.getPacket();
        }

        public static byte[] DressUpTime(byte type) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.SHOW_STATUS_INFO);
            mpw.write(type);
            mpw.writeShort(7707);
            mpw.write(2);
            mpw.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
            return mpw.getPacket();
        }

        public static byte[] updateDress(int transform, MapleCharacter chr) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.ANGELIC_CHANGE);
            mpw.writeInt(chr.getID());
            mpw.writeInt(transform);
            return mpw.getPacket();
        }

        public static byte[] lockSkill(int skillid) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LOCK_CHARGE_SKILL);
            mpw.writeInt(skillid);
            return mpw.getPacket();
        }

        public static byte[] unlockSkill() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.UNLOCK_CHARGE_SKILL);
            mpw.writeInt(0);
            return mpw.getPacket();
        }

        public static byte[] absorbingSoulSeeker(int characterid, int size, Point essence1, Point essence2, int skillid, boolean creation) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
            mpw.write(!creation ? 0 : 1);
            mpw.writeInt(characterid);
            if (!creation) {
                // false
                mpw.writeInt(3);
                mpw.write(1);
                mpw.write(size);
                mpw.writeZeroBytes(3);
                mpw.writeShort(essence1.x);
                mpw.writeShort(essence1.y);
                mpw.writeShort(essence2.y);
                mpw.writeShort(essence2.x);
            } else {
                // true
                mpw.writeShort(essence1.x);
                mpw.writeShort(essence1.y);
                mpw.writeInt(4);
                mpw.write(1);
                mpw.writeShort(essence1.y);
                mpw.writeShort(essence1.x);
            }
            mpw.writeInt(skillid);
            if (!creation) {
                for (int i = 0; i < 2; i++) {
                    mpw.write(1);
                    mpw.writeInt(Randomizer.rand(19, 20));
                    mpw.writeInt(1);
                    mpw.writeInt(Randomizer.rand(18, 19));
                    mpw.writeInt(Randomizer.rand(20, 23));
                    mpw.writeInt(Randomizer.rand(36, 55));
                    mpw.writeInt(540);
                    mpw.writeShort(0);//new 142
                    mpw.writeZeroBytes(6);//new 143
                }
            } else {
                mpw.write(1);
                mpw.writeInt(Randomizer.rand(6, 21));
                mpw.writeInt(1);
                mpw.writeInt(Randomizer.rand(42, 45));
                mpw.writeInt(Randomizer.rand(4, 7));
                mpw.writeInt(Randomizer.rand(267, 100));
                mpw.writeInt(0);//540
                mpw.writeInt(0);
                mpw.writeInt(0);
            }
            mpw.write(0);
            return mpw.getPacket();
        }

        public static byte[] SoulSeekerRegen(MapleCharacter chr, int sn) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
            mpw.write(1);
            mpw.writeInt(chr.getID());
            mpw.writeInt(sn);
            mpw.writeInt(4);
            mpw.write(1);
            mpw.writeInt(sn);
            mpw.writeInt(65111007); // hide skills
            mpw.write(1);
            mpw.writeInt(Randomizer.rand(0x06, 0x10));
            mpw.writeInt(1);
            mpw.writeInt(Randomizer.rand(0x28, 0x2B));
            mpw.writeInt(Randomizer.rand(0x03, 0x04));
            mpw.writeInt(Randomizer.rand(0xFA, 0x49));
            mpw.writeInt(0);
            mpw.writeLong(0);
            mpw.write(0);
            return mpw.getPacket();
        }

        public static byte[] SoulSeeker(MapleCharacter chr, int skillid, int sn, int sc1, int sc2) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
            mpw.write(0);
            mpw.writeInt(chr.getID());
            mpw.writeInt(3);
            mpw.write(1);
            mpw.writeInt(sn);
            if (sn >= 1) {
                mpw.writeInt(sc1);//SHOW_ITEM_GAIN_INCHAT
                if (sn == 2) {
                    mpw.writeInt(sc2);
                }
            }
            mpw.writeInt(65111007); // hide skills
            for (int i = 0; i < 2; i++) {
                mpw.write(1);
                mpw.writeInt(i + 2);
                mpw.writeInt(1);
                mpw.writeInt(Randomizer.rand(0x0F, 0x10));
                mpw.writeInt(Randomizer.rand(0x1B, 0x22));
                mpw.writeInt(Randomizer.rand(0x1F, 0x24));
                mpw.writeInt(540);
                mpw.writeInt(0);//wasshort new143
                mpw.writeInt(0);//new143
            }
            mpw.write(0);
            return mpw.getPacket();
        }
    }

    public static class LuminousPacket {

        public static byte[] updateLuminousGauge(int darktotal, int lighttotal, int darktype, int lighttype) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.LUMINOUS_COMBO);
            mpw.writeInt(darktotal);
            mpw.writeInt(lighttotal);
            mpw.writeInt(darktype);
            mpw.writeInt(lighttype);
            mpw.writeInt(281874974);//1210382225
            
            mpw.writeZeroBytes(69); //for no dc

            return mpw.getPacket();
        }

        public static byte[] giveLuminousState(int skill, int light, int dark, int duration) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);

            PacketHelper.writeSingleMask(mpw, MapleBuffStat.LUMINOUS_GAUGE);

            mpw.writeShort(1);
            mpw.writeInt(skill); //20040217
            mpw.writeInt(duration);
            mpw.writeZeroBytes(5);
            mpw.writeInt(skill); //20040217
            mpw.writeInt(483195070);
            mpw.writeZeroBytes(8);
            mpw.writeInt(Math.max(light, -1)); //light gauge
            mpw.writeInt(Math.max(dark, -1)); //dark gauge
            mpw.writeInt(1);
            mpw.writeInt(1);//was2
            mpw.writeInt(283183599);
            mpw.writeInt(0);
            mpw.writeInt(0);//new143
            mpw.writeInt(1);
            mpw.write(0);
            
            mpw.writeZeroBytes(69); //for no dc

            return mpw.getPacket();
        }
    }

    public static class XenonPacket {

        public static byte[] giveXenonSupply(short amount) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            PacketHelper.writeSingleMask(mpw, MapleBuffStat.SUPPLY_SURPLUS);

            mpw.writeShort(amount);
            mpw.writeInt(30020232); //skill id
            mpw.writeInt(-1); //duration
            mpw.writeZeroBytes(18);

            return mpw.getPacket();
        }

        public static byte[] giveAmaranthGenerator() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.SUPPLY_SURPLUS, 0);
            statups.put(MapleBuffStat.AMARANTH_GENERATOR, 0);
            PacketHelper.writeBuffMask(mpw, statups);

            mpw.writeShort(20); //gauge fill
            mpw.writeInt(30020232); //skill id
            mpw.writeInt(-1); //duration

            mpw.writeShort(1);
            mpw.writeInt(36121054); //skill id
            mpw.writeInt(10000); //duration

            mpw.writeZeroBytes(5);
            mpw.writeInt(1000);
            mpw.writeInt(1);
            mpw.writeZeroBytes(1);
            
            mpw.writeZeroBytes(69); //for no dc

            return mpw.getPacket();
        }
        
        public static byte[] PinPointRocket(int cid, List<Integer> moblist) {
        	MaplePacketWriter packet = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
	        packet.write(0);
	        packet.writeInt(cid);
	        packet.writeInt(6);
	        packet.write(1);
	        packet.writeInt(moblist.size());
	        for (int i = 0; i < moblist.size(); i++) {
	            packet.writeInt(moblist.get(i));
	        }
	        packet.writeInt(36001005);
	        for (int i = 1; i <= moblist.size(); i++) {
	            packet.write(1);
	            packet.writeInt(i + 7);
	            packet.writeInt(0);
	            packet.writeInt(Randomizer.rand(10, 20));
	            packet.writeInt(Randomizer.rand(20, 40));
	            packet.writeInt(Randomizer.rand(40, 200));
	            packet.writeInt(Randomizer.rand(500, 2000));
	            packet.writeLong(0); //v196
	            packet.writeInt(Randomizer.nextInt());
	            packet.writeInt(0);
	        }
	        packet.write(0);
	        
	        packet.writeZeroBytes(69); //for no dc
	        return packet.getPacket();    
        }
        
        public static byte[] MegidoFlameRe(int cid, int oid) {
	        MaplePacketWriter outPacket = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
	        outPacket.write(0);
	        outPacket.writeInt(cid);
	        outPacket.writeInt(3);
	        outPacket.write(1);
	        outPacket.writeInt(1);
	        outPacket.writeInt(oid);
	        outPacket.writeInt(2121055);
	        outPacket.write(1);
	        outPacket.writeInt(2);
	        outPacket.writeInt(2);
	        outPacket.writeInt(Randomizer.rand(10, 17));
	        outPacket.writeInt(Randomizer.rand(10, 16));
	        outPacket.writeInt(Randomizer.rand(40, 52));
	        outPacket.writeInt(20);
	        outPacket.writeLong(0);
	        outPacket.writeLong(0);
	        outPacket.write(0);
	        
	        outPacket.writeZeroBytes(69); //for no dc
	        
	        return outPacket.getPacket();
        }

	    public static byte[] ShieldChacingRe(int cid, int unkwoun, int oid, int unkwoun2, int unkwoun3) {
	        MaplePacketWriter packet = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
	        packet.write(1);
	        packet.writeInt(cid);
	        packet.writeInt(unkwoun);
	        packet.writeInt(4);
	        packet.write(1);
	        packet.writeInt(oid);
	        packet.writeInt(31221014);
	
	        packet.write(1);
	        packet.writeInt(unkwoun2 + 1);
	        packet.writeInt(3);
	        packet.writeInt(unkwoun3);
	        packet.writeInt(3);
	        packet.writeInt(Randomizer.rand(36, 205));
	        packet.writeInt(0);
	        packet.writeLong(0);
	        packet.writeInt(Randomizer.nextInt());
	        packet.writeInt(0);
	        packet.write(0);
	        
	        packet.writeZeroBytes(69); //for no dc
	        
	        return packet.getPacket();
	    }
	
	    public static byte[] ShieldChacing(int cid, List<Integer> moblist, int skillid) {
	        MaplePacketWriter packet = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
	        packet.write(0);
	        packet.writeInt(cid);
	        packet.writeInt(3);
	        packet.write(1);
	        packet.writeInt(moblist.size());
	        for (int i = 0; i < moblist.size(); i++) {
	            packet.writeInt(((Integer) moblist.get(i)).intValue());
	        }
	        packet.writeInt(skillid);
	        for (int i = 1; i <= moblist.size(); i++) {
	            packet.write(1);
	            packet.writeInt(1 + i);
	            packet.writeInt(3);
	            packet.writeInt(Randomizer.rand(1, 20));
	            packet.writeInt(Randomizer.rand(20, 50));
	            packet.writeInt(Randomizer.rand(50, 200));
	            packet.writeInt(skillid == 2121055 ? 720 : 660);
	            packet.writeLong(0);
	            packet.writeInt(Randomizer.nextInt());
	            packet.writeInt(0);
	        }
	        packet.write(0);
	         
	        packet.writeZeroBytes(69); //for no dc
	        return packet.getPacket();
	    }
	
	    public static byte[] EazisSystem(int cid, int oid) {
	        MaplePacketWriter packet = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
	        packet.write(0);
	        packet.writeInt(cid);
	        packet.writeInt(5);
	        packet.write(1);
	        packet.writeInt(oid);
	        packet.writeInt(36110004);
	        for (int i = 0; i < 3; i++) {
	            packet.write(1);
	            packet.writeInt(i + 2);
	            packet.writeInt(0);
	            packet.writeInt(0x23);
	            packet.writeInt(5);
	            packet.writeInt(Randomizer.rand(80, 100));
	            packet.writeInt(Randomizer.rand(200, 300));
	            packet.writeLong(0); //v196
	            packet.writeInt(Randomizer.nextInt());
	            packet.writeInt(0);
	        }
	        packet.write(0);
	         
	        packet.writeZeroBytes(69); //for no dc
	        return packet.getPacket();
	    }
    }

    public static class AvengerPacket {

        public static byte[] giveAvengerHpBuff(int hp) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            
            PacketHelper.writeSingleMask(mpw, MapleBuffStat.LUNAR_TIDE);
            mpw.writeShort(3);
            mpw.writeInt(0);
            mpw.writeInt(2100000000);
            mpw.writeZeroBytes(5);
            mpw.writeInt(hp);
            mpw.writeZeroBytes(9);
            
            mpw.writeZeroBytes(69); //for no dc
            
            return mpw.getPacket();
        }

        public static byte[] giveExceed(short amount) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            PacketHelper.writeSingleMask(mpw, MapleBuffStat.EXCEED);

            mpw.writeShort(amount);
            mpw.writeInt(30010230); //skill id
            mpw.writeInt(-1); //duration
            mpw.writeZeroBytes(14);

            mpw.writeZeroBytes(69); //for no dc
            
            return mpw.getPacket();
        }

        public static byte[] giveExceedAttack(int skill, short amount) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            PacketHelper.writeSingleMask(mpw, MapleBuffStat.EXCEED_ATTACK);

            mpw.writeShort(amount);
            mpw.writeInt(skill); //skill id
            mpw.writeInt(15000); //duration
            mpw.writeZeroBytes(14);
            
            mpw.writeZeroBytes(69); //for no dc

            return mpw.getPacket();
        }
        
        public static byte[] cancelExceed() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANCEL_BUFF);
            
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.EXCEED, 0);
            statups.put(MapleBuffStat.EXCEED_ATTACK, 0);
            PacketHelper.writeBuffMask(mpw, statups);
            
            return mpw.getPacket();
        }
    }

    public static class DawnWarriorPacket {

        public static byte[] giveMoonfallStance(int level) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.CRITICAL_PERCENT_UP, 0);
            statups.put(MapleBuffStat.MOON_STANCE2, 0);
            statups.put(MapleBuffStat.WARRIOR_STANCE, 0);
            PacketHelper.writeBuffMask(mpw, statups);

            mpw.writeShort(level);
            mpw.writeInt(11101022);
            mpw.writeInt(Integer.MAX_VALUE);
            mpw.writeShort(1);
            mpw.writeInt(11101022);
            mpw.writeInt(Integer.MAX_VALUE);
            mpw.writeInt(0);
            mpw.write(5);
            mpw.write(1);
            mpw.writeInt(1);
            mpw.writeInt(11101022);
            mpw.writeInt(level);
            mpw.writeInt(Integer.MAX_VALUE);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.write(1);
            mpw.writeInt(0);
            
            mpw.writeZeroBytes(69); //for no dc

            return mpw.getPacket();
        }

        public static byte[] giveSunriseStance(int level) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.BOOSTER, 0);
            statups.put(MapleBuffStat.DAMAGE_PERCENT, 0);
            statups.put(MapleBuffStat.WARRIOR_STANCE, 0);
            PacketHelper.writeBuffMask(mpw, statups);

            mpw.writeShort(level);
            mpw.writeInt(11111022);
            mpw.writeInt(Integer.MAX_VALUE);
            mpw.writeInt(0);
            mpw.write(5);
            mpw.write(1);
            mpw.writeInt(1);
            mpw.writeInt(11111022);
            mpw.writeInt(-1);
            mpw.writeInt(Integer.MAX_VALUE);
            mpw.writeInt(0);
            mpw.writeInt(1);
            mpw.writeInt(11111022);
            mpw.writeInt(0x19);
            mpw.writeInt(Integer.MAX_VALUE);
            mpw.writeInt(0);
            mpw.writeInt(0);
            mpw.write(1);
            mpw.writeInt(0);

            mpw.writeZeroBytes(69); //for no dc

            return mpw.getPacket();
        }

        public static byte[] giveEquinox_Moon(int level, int duration) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.CRITICAL_PERCENT_UP, 0);
            statups.put(MapleBuffStat.MOON_STANCE2, 0);
            statups.put(MapleBuffStat.EQUINOX_STANCE, 0);
            PacketHelper.writeBuffMask(mpw, statups);

            mpw.writeShort(level);
            mpw.writeInt(11121005);
            mpw.writeInt(duration);
            mpw.writeShort(1);
            mpw.writeInt(11121005);
            mpw.writeInt(duration);
            mpw.writeInt(0);
            mpw.write(5);
            mpw.writeInt(1);
            mpw.writeInt(11121005);
            mpw.writeInt(level);
            mpw.writeInt(duration);
            mpw.writeInt(duration);
            mpw.writeInt(0);
            mpw.write(1);
            mpw.writeInt(0);

            mpw.writeZeroBytes(69); //for no dc

            return mpw.getPacket();
        }

        public static byte[] giveEquinox_Sun(int level, int duration) {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.BOOSTER, 0);
            statups.put(MapleBuffStat.DAMAGE_PERCENT, 0);
            statups.put(MapleBuffStat.EQUINOX_STANCE, 0);
            PacketHelper.writeBuffMask(mpw, statups);

            mpw.writeShort(level);
            mpw.writeInt(11121005);
            mpw.writeInt(duration);
            mpw.writeInt(0);
            mpw.write(5);
            mpw.writeInt(1);
            mpw.writeInt(11121005);
            mpw.writeInt(-1);
            mpw.writeInt(duration);
            mpw.writeInt(duration);
            mpw.writeInt(1);
            mpw.writeInt(11121005);
            mpw.writeInt(0x19);
            mpw.writeInt(duration);
            mpw.writeInt(duration);
            mpw.writeInt(0);
            mpw.write(1);
            mpw.writeInt(0);

            mpw.writeZeroBytes(69); //for no dc

            return mpw.getPacket();
        }
    }
    
    public static class BeastTamerPacket {
        
	    public static byte[] ModeCancel() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.CANCEL_BUFF);
            
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.ANIMAL_SELECT, 0);
            PacketHelper.writeBuffMask(mpw, statups);
            
            return mpw.getPacket();
	    }    
        
	    public static byte[] BearMode() {
            MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
            
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.ANIMAL_SELECT, 0);
            PacketHelper.writeBuffMask(mpw, statups);
            mpw.writeShort(1);
            mpw.writeInt(110001501);
            mpw.writeInt(-419268850);
            mpw.writeLong(0);
            mpw.writeInt(0);
            mpw.write(0);
            mpw.write(1);
            mpw.writeInt(0);
            
            return mpw.getPacket();
	    }
    
	    public static byte[] LeopardMode() {
	        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
	        
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.ANIMAL_SELECT, 0);
            PacketHelper.writeBuffMask(mpw, statups);
            mpw.writeShort(2);
            mpw.writeInt(110001502);
            mpw.writeInt(-419263978);
            mpw.writeLong(0);
            mpw.writeInt(0);
            mpw.write(0);
            mpw.write(1);
            mpw.writeInt(0);
	            
	        return mpw.getPacket();
	    }
    
	    public static byte[] HawkMode() {
	        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
	        
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.ANIMAL_SELECT, 0);
            PacketHelper.writeBuffMask(mpw, statups);
            mpw.writeShort(3);
            mpw.writeInt(110001503);
            mpw.writeInt(-419263978);
            mpw.writeLong(0);
            mpw.writeInt(0);
            mpw.write(0);
            mpw.write(1);
            mpw.writeInt(0);
	            
	        return mpw.getPacket();
	    }
	    
	    public static byte[] CatMode() {
	        MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
	        
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.ANIMAL_SELECT, 0);
            PacketHelper.writeBuffMask(mpw, statups);
            mpw.writeShort(4);
            mpw.writeInt(110001504);
            mpw.writeInt(-419263978);
            mpw.writeLong(0);
            mpw.writeInt(0);
            mpw.write(0);
            mpw.write(1);
            mpw.writeInt(0);
 
	        return mpw.getPacket();
	    }
	    
        public static byte[] LeopardRoar() {
        	MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GIVE_BUFF);
        
            Map<MapleBuffStat, Integer> statups = new EnumMap<>(MapleBuffStat.class);
            statups.put(MapleBuffStat.DAMAGE_CAP_INCREASE, statups.get(MapleStatInfo.indieDamR));
            statups.put(MapleBuffStat.DAMAGE_PERCENT, statups.get(MapleStatInfo.indieMaxDamageOver));
            PacketHelper.writeBuffMask(mpw, statups);
            mpw.writeShort(4);
            mpw.writeInt(110001504);
            mpw.writeInt(-419263978);
            mpw.writeLong(0);
            mpw.writeInt(0);
            mpw.write(0);
            mpw.write(1);
            mpw.writeInt(0);
            
            return mpw.getPacket();
        }
        
    }
}
