package net.server.loginauth;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class LoginAuthEncoder implements ProtocolEncoder {

	@Override
	public void dispose(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		byte[] input = (byte[]) message;
		short size = (short) (1 + 3 + 1 + 3 + 4 + 4 + input.length - 2);
		
		ByteBuffer buffer = ByteBuffer.allocate(size + 4);
		buffer.putShort(size);
		buffer.putShort(input[0]);
		buffer.put((byte) 0x18);
		buffer.put(writeSmallSize(size));
		buffer.put((byte) 0);
		buffer.put(writeSmallSize(size - 12));
		buffer.putInt(0xDEADB00B);
		buffer.putInt(0xAABBCCDD);
		buffer.put(input, 2, input.length - 2);

		//System.out.println(HexTool.toString(buffer.array()));

		out.write(ByteBuffer.wrap(buffer.array()));
	}
	
	private byte[] writeSmallSize(int input) {
		return new byte[] {(byte)(input >> 16), (byte)(input >> 8), (byte)(input >> 0) };
	}


}
