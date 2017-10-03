package com.abc.gws.amq;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.SocketAddress;

import javax.annotation.Resource;
import javax.jms.BytesMessage;
import javax.jms.JMSException;

import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

import com.abc.gws.util.ChannelUtil;
import com.abc.gws.util.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

/**
 *
 * @author Administrator
 *
 */
@PropertySource("classpath:activemq.properties")
@Component
public class ConsumerV3 {

	@Value("${server.id}")
	private String server_id;

	@Value("${queue.channel.close}")
	private String queue_channel_close;

	@Resource(name = "jmsMessagingTemplate")
	private JmsMessagingTemplate jmsMessagingTemplate;

	private static final Logger logger = LoggerFactory
			.getLogger(ConsumerV3.class);

	@JmsListener(destination = "${queue.back.send.v3}.${server.id}")
	public void back_send(BytesMessage msg) {

		try {
			byte[] data = new byte[(int) msg.getBodyLength()];
			msg.readBytes(data);

			String s = new String(data, Charsets.UTF_8);

			JsonArray ja = new JsonParser().parse(s).getAsJsonArray();

			String _receiver = ja.get(0).getAsString();

			String _data = ja.get(1).getAsString();

			// 开始发送

			if (Constants.ALL.equals(_receiver)) {
				ChannelUtil.getDefault().broadcast(_data);
				return;
			}

			Channel c = ChannelUtil.getDefault().getChannel(_receiver);

			if (null != c) {
				c.writeAndFlush(_data);
				return;
			}

			jmsMessagingTemplate.convertAndSend(queue_channel_close, server_id
					+ "::" + _receiver);
			logger.info("channel amq close: {}:{}", server_id, _receiver);

		} catch (JMSException e) {
			logger.error("", e);
		}
	}

	@JmsListener(destination = "${queue.front.force.v3}.${server.id}")
	public void front_force(BytesMessage msg) {

		try {
			byte[] data = new byte[(int) msg.getBodyLength()];
			msg.readBytes(data);

			String s = new String(data, Charsets.UTF_8);

			Channel c = ChannelUtil.getDefault().getChannel(s);

			if (null == c)
				return;

			ChannelFuture future = c.close();

			future.addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future)
						throws Exception {
					SocketAddress addr = c.remoteAddress();

					if (future.isSuccess()) {
						logger.info("ctx close: {}", addr);
						return;
					}

					logger.info("ctx close failure: {}", addr);
					c.close();
				}
			});

		} catch (JMSException e) {
			logger.error("", e);
		}
	}

}