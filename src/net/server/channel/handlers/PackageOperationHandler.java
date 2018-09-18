package net.server.channel.handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import client.MapleCharacterUtil;
import client.MapleClient;
import client.character.MapleCharacter;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.ItemLoader;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import net.AbstractMaplePacketHandler;
import net.RecvPacketOpcode;
import net.packet.CField;
import net.packet.CWvsContext;
import net.server.channel.ChannelServer;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MaplePackageActions;
import tools.Pair;
import tools.data.LittleEndianAccessor;

public class PackageOperationHandler extends AbstractMaplePacketHandler {

	public PackageOperationHandler(RecvPacketOpcode recv) {
		super(recv);
	}

	@Override
	public void handlePacket(LittleEndianAccessor lea, MapleClient c, MapleCharacter chr) {
		final byte operation = lea.readByte();

        switch (operation) {
            case 1: { // Start Donald, 13 digit AS
                final String AS13Digit = lea.readMapleAsciiString();
                //int unk = slea.readInt(); // Theres an int here, value = 1
                //9 = error
                final int conv = c.getCharacter().getConversation();
                if (conv == 2) { // Donald
                    c.getSession().write(CField.sendPackageMSG((byte) 10, loadItems(c.getCharacter())));
                }
                break;
            }
            case 3: { // Send Item
                if (c.getCharacter().getConversation() != 2) {
                    return;
                }
                final byte inventId = lea.readByte();
                final short itemPos = lea.readShort();
                final short amount = lea.readShort();
                final int mesos = lea.readInt();
                final String recipient = lea.readMapleAsciiString();
                boolean quickdelivery = lea.readByte() > 0;

                final int finalcost = mesos + GameConstants.getTaxAmount(mesos) + (quickdelivery ? 0 : 5000);

                if (mesos >= 0 && mesos <= 100000000 && c.getCharacter().getMeso() >= finalcost) {
                    final int accid = MapleCharacterUtil.getIdByName(recipient);
                    if (accid != -1) {
                        if (accid != c.getAccountID()) {
                            boolean recipientOn = false;
                            MapleClient rClient = null;
                            try {
                                //int channel = c.getChannelServer().getWorldInterface().find(recipient);
                                int channel = c.getChannelServer().getPlayerStorage().getCharacterByName(recipient).getClient().getChannel();
                                if (channel > -1) {
                                    recipientOn = true;
                                    ChannelServer rcserv = ChannelServer.getInstance(channel);
                                    rClient = rcserv.getPlayerStorage().getCharacterByName(recipient).getClient();
                                }
                            } catch (Exception e) {
                                //c.getChannelServer().reconnectWorld();
                            }

                            if (inventId > 0) {
                                final MapleInventoryType inv = MapleInventoryType.getByType(inventId);
                                final Item item = c.getCharacter().getInventory(inv).getItem((byte) itemPos);
                                if (item == null) {
                                    c.getSession().write(CField.sendPackageMSG((byte) 17, null)); // Unsuccessfull
                                    return;
                                }
                                final short flag = item.getFlag();
                                if (ItemFlag.UNTRADABLE.check(flag) || ItemFlag.LOCK.check(flag)) {
                                    c.getSession().write(CWvsContext.enableActions());
                                    return;
                                }
                                if (c.getCharacter().getItemQuantity(item.getItemId(), false) >= amount) {
                                    final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                                    if (!ii.isDropRestricted(item.getItemId()) && !ii.isAccountShared(item.getItemId())) {
                                        if (addItemToDB(item, amount, mesos, c.getCharacter().getName(), accid, recipientOn)) {
                                            if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
                                                MapleInventoryManipulator.removeFromSlot(c, inv, (byte) itemPos, item.getQuantity(), true);
                                            } else {
                                                MapleInventoryManipulator.removeFromSlot(c, inv, (byte) itemPos, amount, true, false);
                                            }
                                            c.getCharacter().gainMeso(-finalcost, false);
                                            c.getSession().write(CField.sendPackageMSG((byte) 19, null)); // Successfull
                                        } else {
                                            c.getSession().write(CField.sendPackageMSG((byte) 17, null)); // Unsuccessful
                                        }
                                    } else {
                                        c.getSession().write(CField.sendPackageMSG((byte) 17, null)); // Unsuccessfull
                                    }
                                } else {
                                    c.getSession().write(CField.sendPackageMSG((byte) 17, null)); // Unsuccessfull
                                }
                            } else {
                                if (addMesoToDB(mesos, c.getCharacter().getName(), accid, recipientOn)) {
                                    c.getCharacter().gainMeso(-finalcost, false);

                                    c.getSession().write(CField.sendPackageMSG((byte) 19, null)); // Successfull
                                } else {
                                    c.getSession().write(CField.sendPackageMSG((byte) 17, null)); // Unsuccessfull
                                }
                            }
                            //if (recipientOn && rClient != null) {
                            //    rClient.getSession().write(CField.sendPackageMSG(Actions.PACKAGE_MSG.getCode()));
                            //}
                        } else {
                            c.getSession().write(CField.sendPackageMSG((byte) 15, null)); // Same acc error
                        }
                    } else {
                        c.getSession().write(CField.sendPackageMSG((byte) 14, null)); // Name does not exist
                    }
                } else {
                    c.getSession().write(CField.sendPackageMSG((byte) 12, null)); // Not enough mesos
                }
                break;
            }
            case 5: { // Recieve Package
                if (c.getCharacter().getConversation() != 2) {
                    return;
                }
                final int packageid = lea.readInt();
                //System.out.println("Item attempted : " + packageid);
                final MaplePackageActions dp = loadSingleItem(packageid, c.getCharacter().getID());
                if (dp == null) {
                    return;
                }
                if (dp.getItem() != null && !MapleInventoryManipulator.checkSpace(c, dp.getItem().getItemId(), dp.getItem().getQuantity(), dp.getItem().getOwner())) {
                    c.getSession().write(CField.sendPackageMSG((byte) 16, null)); // Not enough Space
                    return;
                } else if (dp.getMesos() < 0 || (dp.getMesos() + c.getCharacter().getMeso()) < 0) {
                    c.getSession().write(CField.sendPackageMSG((byte) 17, null)); // Unsuccessfull
                    return;
                }
                removeItemFromDB(packageid, c.getCharacter().getID()); // Remove first
                //System.out.println("Item removed : " + packageid);
                if (dp.getItem() != null) {
                    MapleInventoryManipulator.addFromDrop(c, dp.getItem(), false);
                }
                if (dp.getMesos() != 0) {
                    c.getCharacter().gainMeso(dp.getMesos(), false);
                }
                c.getSession().write(CField.removeFromPackageList(false, packageid));
                break;
            }
            case 6: { // Remove package
                if (c.getCharacter().getConversation() != 2) {
                    return;
                }
                final int packageid = lea.readInt();
                removeItemFromDB(packageid, c.getCharacter().getID());
                c.getSession().write(CField.removeFromPackageList(true, packageid));
                break;
            }
            case 8: { // Close Package Deliverer
                c.getCharacter().setConversation(0);
                break;
            }
            default: {
                System.out.println("Unhandled Package operation : " + lea.toString());
                break;
            }
        }
	}
	
	public static List<MaplePackageActions> loadItems(final MapleCharacter chr) {
        List<MaplePackageActions> packages = new LinkedList<>();
        Connection con = DatabaseConnection.getConnection();
        try {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM dueypackages WHERE RecieverId = ?")) {
                ps.setInt(1, chr.getID());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        MaplePackageActions pack = getItemByPID(rs.getInt("packageid"));
                        pack.setSender(rs.getString("SenderName"));
                        pack.setMesos(rs.getInt("Mesos"));
                        pack.setSentTime(rs.getLong("TimeStamp"));
                        packages.add(pack);
                    }
                }
            }
            return packages;
        } catch (SQLException se) {
            return null;
        }
    }
	
	public static MaplePackageActions loadSingleItem(final int packageid, final int charid) {
        List<MaplePackageActions> packages = new LinkedList<>();
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM dueypackages WHERE PackageId = ? and RecieverId = ?");
            ps.setInt(1, packageid);
            ps.setInt(2, charid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                MaplePackageActions pack = getItemByPID(packageid);
                pack.setSender(rs.getString("SenderName"));
                pack.setMesos(rs.getInt("Mesos"));
                pack.setSentTime(rs.getLong("TimeStamp"));
                packages.add(pack);
                rs.close();
                ps.close();
                return pack;
            } else {
                rs.close();
                ps.close();
                return null;
            }
        } catch (SQLException se) {
            return null;
        }
    }
	
	private static boolean addMesoToDB(final int mesos, final String sName, final int recipientID, final boolean isOn) {
        Connection con = DatabaseConnection.getConnection();
        try {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO dueypackages (RecieverId, SenderName, Mesos, TimeStamp, Checked, Type) VALUES (?, ?, ?, ?, ?, ?)")) {
                ps.setInt(1, recipientID);
                ps.setString(2, sName);
                ps.setInt(3, mesos);
                ps.setLong(4, System.currentTimeMillis());
                ps.setInt(5, isOn ? 0 : 1);
                ps.setInt(6, 3);

                ps.executeUpdate();
            }

            return true;
        } catch (SQLException se) {
            return false;
        }
    }
	
	private static boolean addItemToDB(final Item item, final int quantity, final int mesos, final String sName, final int recipientID, final boolean isOn) {
        Connection con = DatabaseConnection.getConnection();
        try {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO dueypackages (RecieverId, SenderName, Mesos, TimeStamp, Checked, Type) VALUES (?, ?, ?, ?, ?, ?)", DatabaseConnection.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, recipientID);
                ps.setString(2, sName);
                ps.setInt(3, mesos);
                ps.setLong(4, System.currentTimeMillis());
                ps.setInt(5, isOn ? 0 : 1);

                ps.setInt(6, item.getType());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        ItemLoader.PACKAGE.saveItems(Collections.singletonList(new Pair<>(item, GameConstants.getInventoryType(item.getItemId()))), rs.getInt(1));
                    }
                }
            }

            return true;
        } catch (SQLException se) {
            return false;
        }
    }
	
	private static void removeItemFromDB(final int packageid, final int charid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM dueypackages WHERE PackageId = ? and RecieverId = ?")) {
                ps.setInt(1, packageid);
                ps.setInt(2, charid);
                ps.executeUpdate();
            }
        } catch (SQLException se) {
        }
    }

    private static MaplePackageActions getItemByPID(final int packageid) {
        try {
            Map<Long, Pair<Item, MapleInventoryType>> iter = ItemLoader.PACKAGE.loadItems(false, packageid);
            if (iter != null && iter.size() > 0) {
                for (Pair<Item, MapleInventoryType> i : iter.values()) {
                    return new MaplePackageActions(packageid, i.getLeft());
                }
            }
        } catch (Exception se) {
        }
        return new MaplePackageActions(packageid);
    }

}
