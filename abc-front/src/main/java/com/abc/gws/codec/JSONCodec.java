package com.abc.gws.codec;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.net.SocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.abc.gws.model.ProtocolModel;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * @author Administrator
 *
 */
@Component
@Sharable
public class JSONCodec extends
		MessageToMessageCodec<TextWebSocketFrame, String> {

	@Value("${msg.body.max:512}")
	private int msg_body_max;

	private static final Logger logger = LoggerFactory
			.getLogger(JSONCodec.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, String msg,
			List<Object> out) throws Exception {
		out.add(new TextWebSocketFrame(msg));
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame msg,
			List<Object> out) throws Exception {
		String text = msg.text();

		if (msg_body_max >= text.length()) {

			ProtocolModel model = new ProtocolModel();

			JsonObject jo = new JsonParser().parse(text).getAsJsonObject();

			if (jo.has("version")) {
				model.setVersion(jo.get("version").getAsInt());
			}

			if (jo.has("method")) {
				model.setMethod(jo.get("method").getAsInt());
			}

			if (jo.has("seqId")) {
				model.setSeqId(jo.get("seqId").getAsLong());
			}

			if (jo.has("timestamp")) {
				model.setTimestamp(jo.get("timestamp").getAsLong());
			}

			if (jo.has("data")) {
				model.setData(jo.get("data").getAsString());
			}

			if (jo.has("backendId")) {
				model.setBackendId(jo.get("backendId").getAsString());
			}

			out.add(model);

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
