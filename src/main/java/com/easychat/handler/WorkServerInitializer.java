package com.easychat.handler;

import com.easychat.code.PacketDecoder;
import com.easychat.code.PacketEncoder;
import com.easychat.code.Spliter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.security.pkcs11.wrapper.Constants;

/**
 * @author: Zed
 * date: 2019/08/22.
 * description: netty消息链
 */
@Component
public class WorkServerInitializer extends ChannelInitializer<SocketChannel>  {
    @Autowired
    private AcceptGroupReqHandler acceptGroupReqHandler;
    @Autowired
    private AcceptReqHandler acceptReqHandler;
    @Autowired
    private AddUserReqHandler addUserReqHandler;
    @Autowired
    private CreateGroupReqHandler createGroupReqHandler;
    @Autowired
    private GroupMessageReqHandler groupMessageReqHandler;
    @Autowired
    private InviteGroupReqHandler inviteGroupReqHandler;
    @Autowired
    private LoginReqHandler loginReqHandler;
    @Autowired
    private MessageReqHandler messageReqHandler;
    @Autowired
    private RegisterReqHandler registerReqHandler;
    @Autowired
    private UpdatePasswdReqHandler updatePasswdReqHandler;
    @Autowired
    private HeartBeatRequestHandler heartBeatRequestHandler;
    
    @Autowired
    private SynctReqHandler synctReqHandler;

    @Autowired
    private NioWebSocketHandler nioWebSocketHandler;
    
    @Autowired
    private ApplyGroupReqHandler applyGroupReqHandler;
    
    @Autowired
    private AllowGroupReqHandler allowGroupReqHandler;
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new HttpServerCodec());// http 编解器
//        // http 消息聚合器  512*1024为接收的最大contentlength
        ch.pipeline().addLast("httpAggregator", new HttpObjectAggregator(512 * 1024));
//        // 支持异步发送大的码流(大的文件传输),但不占用过多的内存，防止java内存溢出
//        ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
        ch.pipeline().addLast(new WebSocketServerProtocolHandler("/",null,true,65535));

        ch.pipeline().addLast(nioWebSocketHandler);
//        ch.pipeline().addLast(new Spliter());
//        ch.pipeline().addLast(new PacketDecoder());
//        ch.pipeline().addLast(new IMIdleStateHandler());
//        ch.pipeline().addLast(acceptGroupReqHandler);
//        ch.pipeline().addLast(acceptReqHandler);
//        ch.pipeline().addLast(addUserReqHandler);
//        ch.pipeline().addLast(createGroupReqHandler);
//        ch.pipeline().addLast(groupMessageReqHandler);
//        ch.pipeline().addLast(inviteGroupReqHandler);
//        ch.pipeline().addLast(loginReqHandler);
//        ch.pipeline().addFirst(messageReqHandler);
//        ch.pipeline().addLast(registerReqHandler);
//        ch.pipeline().addLast(updatePasswdReqHandler);
//        ch.pipeline().addLast(heartBeatRequestHandler);
//        ch.pipeline().addLast(synctReqHandler);
//        ch.pipeline().addLast(applyGroupReqHandler);
//        ch.pipeline().addLast(allowGroupReqHandler);
//        ch.pipeline().addLast(new PacketEncoder());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }
}
