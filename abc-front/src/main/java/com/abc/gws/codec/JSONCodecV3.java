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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 *
 * @author Administrator
 *
 */
@Component
@Sharable
public class JSONCodecV3 extends
		MessageToMessageCodec<TextWebSocketFrame, String> {

	@Value("${msg.body.max:512}")
	private int msg_body_max;

	private static final Logger logger = LoggerFactory
			.getLogger(JSONCodecV3.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, String msg,
			List<Object> out) throws Exception {
		out.add(new TextWebSocketFrame(msg));
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame msg,
			List<Object> out) throws Exception {
		String text = msg.text();

		if (msg_body_max < text.length()) {
			logout(ctx);
			return;
		}

		JsonArray ja = null;

		try {
			ja = new JsonParser().parse(text).getAsJsonArray();
		} catch (Exception ex) {
			logout(ctx);
			return;
		}

		if (null == ja || 6 != ja.size()) {
			logout(ctx);
			return;
		}

		ProtocolModel model = new ProtocolModel();

		try {
			model.setVersion(ja.get(0).getAsInt());
			model.setMethod(ja.get(1).getAsInt());
			model.setSeqId(ja.get(2).getAsLong());
			model.setTimestamp(ja.get(3).getAsLong());

			JsonElement _je_4 = ja.get(4);

			if (!_je_4.isJsonNull()) {
				model.setData(_je_4.getAsString());
			}

			JsonElement _je_5 = ja.get(5);

			if (!_je_5.isJsonNull()) {
				model.setBackendId(_je_5.getAsString());
			}

		} catch (Exception ex) {
			logout(ctx);
			return;
		}

		out.add(model);
	}

	private void logout(ChannelHandlerContext ctx) {

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

	public static void main(String[] args) {
		String arrStr = "['a', 'b', 3, , ]";

		System.err.println(arrStr);

		JsonArray jo = new JsonParser().parse(arrStr).getAsJsonArray();

		System.err.println(jo);

		System.err.println(jo.size());

		System.err.println(jo.get(0).getAsString());

		System.err.println(jo.get(2).getAsString());

		System.err.println(jo.get(3).isJsonNull());

		System.err.println(jo.get(4).isJsonNull());
	}

}
