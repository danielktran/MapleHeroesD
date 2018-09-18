/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package custom;

import net.SendPacketOpcode;
import tools.data.MaplePacketWriter;

/**
 *
 * @author Itzik
 */
public class RedirectorPacket {

    public static byte[] redirectorCommand(String command) {
        MaplePacketWriter mplew = new MaplePacketWriter();

        mplew.writeShort(SendPacketOpcode.REDIRECTOR_COMMAND.getOpcode());
        mplew.writeMapleAsciiString(command);

        return mplew.getPacket();
    }
}
