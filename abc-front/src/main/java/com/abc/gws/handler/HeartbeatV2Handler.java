package com.abc.gws.handler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.abc.gws.model.ProtocolModel;

/**
 *
 * @author Administrator
 *
 */
@Component
@Sharable
public class HeartbeatV2Handler extends
		SimpleChannelInboundHandler<ProtocolModel> {

	private static final Logger logger = LoggerFactory
			.getLogger(HeartbeatV2Handler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ProtocolModel msg)
			throws Exception {
		logger.info("method: {}", msg.getMethod());

		if (666 == msg.getMethod()) {
			ctx.flush();
			return;
		}

		ctx.fireChannelRead(msg);
	}

}
