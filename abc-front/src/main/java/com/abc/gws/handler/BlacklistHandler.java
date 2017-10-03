package com.abc.gws.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;

import com.abc.gws.util.RedisUtil;

/**
 *
 * @author Administrator
 *
 */
@Component
@Sharable
public class BlacklistHandler extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory
			.getLogger(BlacklistHandler.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		InetSocketAddress addr = (InetSocketAddress) ctx.channel()
				.remoteAddress();
		String incoming = addr.getAddress().getHostAddress();

		if (check(incoming)) {
			ctx.pipeline().remove(this);
			return;
		}

		ChannelFuture future = ctx.close();

		future.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future)
					throws Exception {
				SocketAddress addr = ctx.channel().remoteAddress();

				if (future.isSuccess()) {
					logger.info("ctx close: {}", addr);
					return;
				}

				logger.info("ctx close failure: {}", addr);
				ctx.close();
			}
		});
	}

	/**
	 * 从redis中查黑名单ip
	 *
	 * @param ip
	 * @return 不存在则返回true
	 */
	private boolean check(String ip) {
		Jedis j = RedisUtil.getDefault().getJedis();

		if (null == j)
			return false;

		j.close();
		return true;
	}
}
