package com.easychat;

import com.easychat.code.PacketDecoder;
import com.easychat.code.Spliter;
import com.easychat.support.request.LoginReq;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NettySpringApplicationTests {

    public static volatile Channel channelCache = null;

    @Test
    public void contextLoads() {
        Bootstrap bootstrap = new Bootstrap();
        final Channel[] channelCache = {null};
        int retry = 3;

        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap
                .group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<NioSocketChannel>(){

                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        //ch.pipeline().addLast(new IMIdleStateHandler());
                        ch.pipeline().addLast(new Spliter());
                        ch.pipeline().addLast(new PacketDecoder());
                    }
                });

        bootstrap.connect("127.0.0.1", 8000).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("启动连接成功");
                boolean needLogin = false;
                if (channelCache[0] != null) {
                    channelCache[0].close();
                    needLogin = true;
                }
                channelCache[0] = ((ChannelFuture) future).channel();
                if (needLogin) {
                    reLogin(channelCache[0]);
                }
                reLogin(channelCache[0]);
                //startCommand(channel);

            } else if (retry == 0){
                System.out.println("不在重试连接");
            } else {
                int sleepSecond = 1<<retry;
                Thread.sleep(sleepSecond);
              //  connect(bootstrap,retry - 1);
            }
        });
    }

    private static void reLogin(Channel channelCache) {
        LoginReq req = new LoginReq();
        req.setUserName("fdsf");
        req.setPassword("fsdf");
        req.setDateTime(new Date().toString());
        System.out.println("重新登录！");
        System.out.println(channelCache.id());
        channelCache.writeAndFlush(req);
    }

}
