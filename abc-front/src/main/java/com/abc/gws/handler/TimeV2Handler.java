package com.abc.gws.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import com.abc.gws.model.ProtocolModel;
import com.abc.gws.util.Constants;
import com.abc.util.StringUtil;
import com.google.gson.Gson;

/**
 *
 * @author Administrator
 *
 */
@PropertySource("classpath:activemq.properties")
@Component
@Sharable
public class TimeV2Handler extends SimpleChannelInboundHandler<ProtocolModel> {

	@Resource(name = "jmsMessagingTemplate")
	private JmsMessagingTemplate jmsMessagingTemplate;

	@Value("${server.id}")
	private String server_id;

	@Value("${allow.queue}")
	private String allow_queue;

	private static final Logger logger = LoggerFactory
			.getLogger(TimeV2Handler.class);

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ProtocolModel msg)
			throws Exception {
		logger.info("{}:{}:{}:{}", msg.getVersion(), msg.getMethod(),
				msg.getSeqId(), msg.getTimestamp());

		String destName = msg.getMethod().toString();

		String sb = StringUtil.isEmpty(msg.getBackendId());

		if (null != sb) {
			destName += '.' + sb;
		}

		if (-1 < allow_queue.indexOf("," + destName + ",")) {

			msg.setServerId(server_id);
			msg.setChannelId(ctx.channel().id().asLongText());

			Gson gson = new Gson();

			jmsMessagingTemplate.convertAndSend(Constants.PLUGIN + destName,
					gson.toJson(msg));
			ctx.flush();

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

}
