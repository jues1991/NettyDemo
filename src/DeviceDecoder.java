import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class DeviceDecoder extends ReplayingDecoder<DeviceDecoder.STATE> {
	private ByteBuffer m_buffer;

	// 报文阶段状态
	public enum STATE {
		BEGIN, // 报文开始阶段
		CONTENT, // 报文内容阶段
		END,// 报文结束阶段

	}

	public DeviceDecoder() {
		// 报文默认阶段
		this.state(STATE.BEGIN);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		byte b = in.readByte();
		if ('[' == b) {
			// 当接收到“[”时进入CONTENT阶段
			this.state(STATE.CONTENT);
			this.m_buffer = ByteBuffer.allocate(100);
		} else if (STATE.CONTENT == this.state()) {
			// 当CONTENT阶段后
			if (']' == b) {
				// 接收到“]”时为结束阶段
				this.state(STATE.END);

				// 转换为字符串
				this.m_buffer.flip();
				String text = Charset.forName("UTF-8").newDecoder().decode(this.m_buffer.asReadOnlyBuffer()).toString();
				// 输出1包数据
				out.add(text);
				this.m_buffer = null;
			} else {
				// 接收到非“]”时的为内容部分
				this.m_buffer.put(b);
			}
		} else {
			// 其它情况为开始阶段，此处功能可过滤乱数据
			this.state(STATE.BEGIN);
		}
	}

}
