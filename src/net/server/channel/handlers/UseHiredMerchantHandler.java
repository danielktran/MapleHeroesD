package net.server.channel.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import client.MapleClient;
import client.character.MapleCharacter;
import database.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.PlayerShopPacket;
import net.world.World;
import tools.data.LittleEndianAccessor;

public class UseHiredMerchantHandler extends AbstractMaplePacketHandler {

	public UseHiredMerchantHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final boolean packet = true;
		
		if (c.getCharacter().getMap() != null && c.getCharacter().getMap().allowPersonalShop()) {
            final byte state = checkExistance(c.getCharacter().getAccountID(), c.getCharacter().getID());

            switch (state) {
                case 1:
                    c.getCharacter().dropMessage(1, "Please claim your items from Fredrick first.");
                    break;
                case 0:
                    boolean merch = World.hasMerchant(c.getCharacter().getAccountID(), c.getCharacter().getID());
                    if (!merch) {
                        if (c.getChannelServer().isShutdown()) {
                            c.getCharacter().dropMessage(1, "The server is about to shut down.");
                            return;
                        }
                        if (packet) {
                            c.getSession().write(PlayerShopPacket.sendTitleBox());
                        }
                        return;
                    } else {
                        c.getCharacter().dropMessage(1, "Please close the existing store and try again.");
                    }
                    break;
                default:
                    c.getCharacter().dropMessage(1, "An unknown error occured.");
                    break;
            }
        } else {
            c.getSession().close();
        }
	}

	private static byte checkExistance(final int accid, final int cid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            try (PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where accountid = ? OR characterid = ?")) {
                ps.setInt(1, accid);
                ps.setInt(2, cid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ps.close();
                        rs.close();
                        return 1;
                    }
                }
            }
            return 0;
        } catch (SQLException se) {
            return -1;
        }
    }
}
