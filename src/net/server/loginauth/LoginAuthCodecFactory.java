package net.server.loginauth;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class LoginAuthCodecFactory implements ProtocolCodecFactory {

    private final ProtocolEncoder encoder = new LoginAuthEncoder();
    private final ProtocolDecoder decoder = new LoginAuthDecoder();

    @Override
    public ProtocolEncoder getEncoder() throws Exception {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder() throws Exception {
        return decoder;
    }
}
