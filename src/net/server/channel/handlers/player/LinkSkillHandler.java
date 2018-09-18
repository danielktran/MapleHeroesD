package net.server.channel.handlers.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.MapleClient;
import client.character.MapleCharacter;
import database.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import tools.data.LittleEndianAccessor;

public class LinkSkillHandler extends AbstractMaplePacketHandler {

	public LinkSkillHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		int skillTo = 0, skillRank = 0, accid = 0;
		final short level = chr.getLevel();
		final int skillFrom = lea.readInt();
		final int charID = lea.readInt();
	
		switch(skillFrom) {
			case 110:
				skillTo = 80000000;
				break;
			case 20021110:
				skillTo = 80001040;
				break;
			case 20030204:
				skillTo = 80000002;
				break;
			case 20040218:
				skillTo = 80000005;
				break;
			case 30010112:
				skillTo = 80000001;
				break;
			case 50001214:
				skillTo = 80001140;
				break;
			case 60000222:
				skillTo = 80000006; 
				break;
			case 60011219:
				skillTo = 80001155; 
				break;
		}

		Connection con = DatabaseConnection.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			ps = con.prepareStatement("SELECT accountid FROM characters where id = " + charID);
			rs = ps.executeQuery();
			
			if (rs.next()) {
				accid = rs.getInt(1);
			}
			ps.close();
			rs.close();
		} catch(Exception ex) { 
			ex.printStackTrace();
		}
		
		try {
			ps = con.prepareStatement("DELETE FROM skills WHERE skillid= " + skillTo + " AND characterid IN (SELECT id FROM characters WHERE accountid= " + accid + ")");
			ps.execute();
			ps.close();
			rs.close();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		try {
			ps = con.prepareStatement("INSERT INTO skills(skillid,characterid,skilllevel,masterlevel,expiration,victimid) " +
					"VALUES(?,?,?,?,?,?)");
			ps.setInt(1, skillTo);
			ps.setInt(2, charID);
			ps.setInt(3, c.getCharacter().getSkillLevel(skillFrom));
			ps.setInt(4, c.getCharacter().getMasterLevel(skillFrom));
			ps.setInt(5, -1);
			ps.setInt(6, charID);
			
			ps.executeUpdate();
			ps.close();
			rs.close();
			
			con.commit();
		} catch(Exception ex) { 
			ex.printStackTrace();
		} finally {
			try { 
				con.setAutoCommit(true);
			}
			catch(SQLException ex) {
				ex.printStackTrace();
			}
		}
		
		c.getCharacter().dropMessage(1, "Link skill updated!");
	}

}
