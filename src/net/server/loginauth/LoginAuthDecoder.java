package net.server.loginauth;

import java.nio.ByteOrder;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class LoginAuthDecoder extends CumulativeProtocolDecoder {

	long[] xorTable = new long[] {
			0x040FC1578, 0x0113B6C1F, 0x08389CA19, 0x0E2196CD8,
			0x074901489, 0x04AAB1566, 0x07B8C12A0, 0x00018FFCD,
			0x0CCAB704B, 0x07B5A8C0F, 0x0AA13B891, 0x0DE419807,
			0x012FFBCAE, 0x05F5FBA34, 0x010F5AC99, 0x0B1C1DD01
	};
	
	@Override
	protected boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
		// Header
		short packetSize = in.getShort();
		short opcode = in.getShort();
		//System.out.println("Header received. PacketSize " + packetSize + " Opcode " + opcode);

		// Body
		byte[] decryptedBuffer = new byte[packetSize - 16];
		
		byte checkCode = in.get();
		int length = readSmallSize(in);
		byte flag = in.get();
		int originalSize = readSmallSize(in);
		long xorValue = in.getUnsignedInt();
		in.getInt();
		in.get(decryptedBuffer);
		//System.out.println(checkCode + " " + length + " " + flag + " " + originalSize + " " + xorValue);
		
		if ((flag & 0x02) != 0) {
			decryptedBuffer = decrypt(decryptedBuffer, xorValue);
		}
		
		byte[] decodedPacket = new byte[decryptedBuffer.length + 2];
		decodedPacket[0] = (byte) opcode;
		System.arraycopy(decryptedBuffer, 0, decodedPacket, 2, decryptedBuffer.length);
		//System.out.println(HexTool.toString(decodedPacket));
		out.write(decodedPacket);
		return true;
	}
	
	private int readSmallSize(ByteBuffer in) {
		byte a = in.get();
		byte b = in.get();
		byte c = in.get();
		
		return (int) (((int) a) << 16 | ((int) b) << 8 | ((int) c) << 0);
	}
	
	private byte[] decrypt(byte[] buffer, long seed) {
		long temp = 0;
		long temp2 = 0;
		
		byte[] output = new byte[buffer.length];
		
		for (int i = 0; i < buffer.length/4; i++) {
			temp2 = Integer.toUnsignedLong(ByteBuffer.wrap(buffer, i*4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
			temp2 ^= Integer.toUnsignedLong((int)(temp ^ xorTable[i & 15] ^ seed));
			setBytes(temp2, output, i*4);
			temp = (int) temp2;
		}
		
		return output;
	}
	
	private void setBytes(long input, byte[] buffer, int offset) {
		buffer[offset + 0] = (byte) ((input >> 0) & 0xFF);
		buffer[offset + 1] = (byte) ((input >> 8) & 0xFF);
		buffer[offset + 2] = (byte) ((input >> 16) & 0xFF);
		buffer[offset + 3] = (byte) ((input >> 24) & 0xFF);
	}

}
