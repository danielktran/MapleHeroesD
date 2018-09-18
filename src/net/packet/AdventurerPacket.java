/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.packet;

import java.awt.Point;
import java.util.List;

import net.SendPacketOpcode;
import server.Randomizer;
import server.life.MapleMonster;
import tools.data.MaplePacketWriter;

/**
 *
 * @author Mally
 */
public class AdventurerPacket {
    
	public static class AssassinPacket {
		
		public static byte[] giveMarkOfTheif(int cid, int oid, int skillid, List<MapleMonster> monsters, Point p1, Point p2, int starid) {
			MaplePacketWriter mpw = new MaplePacketWriter(SendPacketOpcode.GAIN_FORCE);
			mpw.write(0);
			mpw.writeInt(cid);
			mpw.writeInt(oid);
			mpw.writeInt(11); //type
			mpw.write(1);
			mpw.writeInt(monsters.size());
			for (MapleMonster monster : monsters) {
			    mpw.writeInt(monster.getObjectId());
			}
			mpw.writeInt(skillid); //skillid
			for (int i = 0; i < monsters.size(); i++) {
			    mpw.write(1);
			    mpw.writeInt(Randomizer.rand(0x2A, 0x2D));
			    mpw.writeInt(2); 
			    mpw.writeInt(Randomizer.rand(0x2A, 0x2D));
			    mpw.writeInt(Randomizer.rand(0x03, 0x04));
			    mpw.writeInt(Randomizer.rand(0x43, 0xF5));
			    mpw.writeInt(200);
			    mpw.writeInt(0);
			    mpw.writeInt(0);
			    mpw.writeInt(Randomizer.nextInt());
			    mpw.writeInt(0);
			}
			mpw.write(0);
			mpw.writeInt(p1.x);
			mpw.writeInt(p1.y);
			mpw.writeInt(p2.x);
			mpw.writeInt(p2.y);
			mpw.writeInt(starid);
			return mpw.getPacket();	
		}
		
	}
	
}
