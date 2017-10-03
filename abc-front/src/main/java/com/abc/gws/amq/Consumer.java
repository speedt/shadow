package com.abc.gws.amq;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.SocketAddress;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.abc.gws.util.ChannelUtil;

/**
 *
 * @author Administrator
 *
 */
@PropertySource("classpath:activemq.properties")
@Component
public class Consumer {

	private static final Logger logger = LoggerFactory
			.getLogger(Consumer.class);

	// @JmsListener(destination = "${queue.back.send}" + "." + "${server.id}")
	// public void back_send(BytesMessage msg) {
	//
	// try {
	// int len = (int) msg.getBodyLength();
	// byte[] data = new byte[len];
	// msg.readBytes(data);
	//
	// ReceiverProtobuf rec = ReceiverProtobuf.parseFrom(data);
	//
	// if (Constants.ALL.equals(rec.getReceiver())) {
	// ChannelUtil.getDefault().broadcast(rec.getData());
	// return;
	// }
	//
	// Channel c = ChannelUtil.getDefault().getChannel(rec.getReceiver());
	//
	// if (null != c)
	// c.writeAndFlush(rec.getData());
	//
	// } catch (InvalidProtocolBufferException e) {
	// logger.error("", e);
	// } catch (JMSException e) {
	// logger.error("", e);
	// }
	// }

	@JmsListener(destination = "${queue.front.force}" + "." + "${server.id}")
	public void front_force(TextMessage msg) {

		try {

			Channel c = ChannelUtil.getDefault().getChannel(msg.getText());

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