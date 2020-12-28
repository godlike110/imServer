package com.easychat.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.easychat.code.PacketCode;
import com.easychat.support.Packet;
import com.easychat.support.request.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;

@ChannelHandler.Sharable
@Component
@Slf4j
public class NioWebSocketHandler extends SimpleChannelInboundHandler<Object> {
    private final Logger logger= LoggerFactory.getLogger(this.getClass());

    private WebSocketServerHandshaker handshaker;

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
    private ApplyGroupReqHandler applyGroupReqHandler;

    @Autowired
    private AllowGroupReqHandler allowGroupReqHandler;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("收到消息："+msg);
      //  handleHttpRequest(ctx, (FullHttpRequest) msg);
        if (msg instanceof FullHttpRequest){
            //以http请求形式接入，但是走的是websocket
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame){
            //处理websocket客户端的消息
            handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // 判断是否关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(
                    new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 本例程仅支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            logger.debug("本例程仅支持文本消息，不支持二进制消息");
            throw new UnsupportedOperationException(String.format(
                    "%s frame types not supported", frame.getClass().getName()));
        }
        // 返回应答消息
        String request = ((TextWebSocketFrame) frame).text();
        JSONObject req = JSON.parseObject(request);
        logger.debug("服务端收到：" + req);

        byte cmd = req.getByte("type");
        Class<? extends Packet> packet = PacketCode.getPacketByCommand(cmd);
        if(packet == LoginReq.class) {
            loginReqHandler.channelRead0(ctx,JSON.parseObject(request,LoginReq.class));
        } else if (packet == AddUserReq.class) {
            addUserReqHandler.channelRead0(ctx,JSON.parseObject(request,AddUserReq.class));
        } else if (packet == CreateGroupReq.class) {
            createGroupReqHandler.channelRead0(ctx,JSON.parseObject(request,CreateGroupReq.class));
        } else if (packet == InviteGroupReq.class) {
            inviteGroupReqHandler.channelRead0(ctx,JSON.parseObject(request,InviteGroupReq.class));
        } else if (packet == GroupMessageReq.class) {
            groupMessageReqHandler.channelRead0(ctx,JSON.parseObject(request,GroupMessageReq.class));
        } else if (packet == AcceptGroupReq.class) {
            acceptGroupReqHandler.channelRead0(ctx,JSON.parseObject(request,AcceptGroupReq.class));
        } else if (packet == AcceptReq.class) {
            acceptReqHandler.channelRead0(ctx,JSON.parseObject(request,AcceptReq.class));
        } else if (packet == RegisterReq.class) {
            registerReqHandler.channelRead0(ctx,JSON.parseObject(request,RegisterReq.class));
        } else if (packet == UpdatePasswdReq.class) {
            updatePasswdReqHandler.channelRead0(ctx,JSON.parseObject(request,UpdatePasswdReq.class));
        } else if (packet == HertBeatReq.class) {
            heartBeatRequestHandler.channelRead0(ctx,JSON.parseObject(request,HertBeatReq.class));
        } else if (packet == SyncMessageReq.class) {
            synctReqHandler.channelRead0(ctx,JSON.parseObject(request,SyncMessageReq.class));
        } else if (packet == ApplyGroupReq.class) {
            applyGroupReqHandler.channelRead0(ctx,JSON.parseObject(request,ApplyGroupReq.class));
        } else if (packet == AllowGroupReq.class) {
            allowGroupReqHandler.channelRead0(ctx,JSON.parseObject(request,AllowGroupReq.class));
        } else if (packet == MessageReq.class) {
            messageReqHandler.channelRead0(ctx,JSON.parseObject(request,MessageReq.class));
        }

        TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()
                + ctx.channel().id() + "：" + request);
        // 群发
        //ChannelSupervise.send2All(tws);
        // 返回【谁发的发给谁】
      //   ctx.channel().writeAndFlush(tws);
    }
    /**
     * 唯一的一次http请求，用于创建websocket
     * */
    private void handleHttpRequest(ChannelHandlerContext ctx,
                                   FullHttpRequest req) {
        //要求Upgrade为websocket，过滤掉get/Post
        if (!req.decoderResult().isSuccess()
                || (!"websocket".equals(req.headers().get("Upgrade")))) {
            //若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                "wss://127.0.0.1:8000/", null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory
                    .sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }
    /**
     * 拒绝不合法的请求，并返回错误信息
     * */
    private static void sendHttpResponse(ChannelHandlerContext ctx,
                                         FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(),
                    CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        // 如果是非Keep-Alive，关闭连接
        if (!isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
