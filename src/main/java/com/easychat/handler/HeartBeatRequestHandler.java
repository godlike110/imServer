package com.easychat.handler;

import com.alibaba.fastjson.JSON;
import com.easychat.support.request.HertBeatReq;
import com.easychat.support.request.HertBeatResp;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;

/**
 * 心跳处理 直接回复一个心跳返回消息即可
 */
@ChannelHandler.Sharable
@Component
public class HeartBeatRequestHandler extends SimpleChannelInboundHandler<HertBeatReq> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HertBeatReq req) {
        TextWebSocketFrame twx = new TextWebSocketFrame(JSON.toJSONString(new HertBeatResp()));
        ctx.channel().writeAndFlush(twx);
    }
}
