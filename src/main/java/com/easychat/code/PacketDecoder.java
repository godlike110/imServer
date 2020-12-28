package com.easychat.code;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.List;

/**
 * @author Zed
 * date: 2019/08/19.
 * description: 解码
 */
public class PacketDecoder extends MessageToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
//        if (msg instanceof TextWebSocketFrame) {
//            return;
//        }
        out.add(PacketCode.INSTANCE.decode((ByteBuf)msg));
    }
}
