import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class DeviceDecoder extends ReplayingDecoder<DeviceDecoder.STATE> {
	private ByteBuffer m_buffer;

	// ���Ľ׶�״̬
	public enum STATE {
		BEGIN, // ���Ŀ�ʼ�׶�
		CONTENT, // �������ݽ׶�
		END,// ���Ľ����׶�

	}

	public DeviceDecoder() {
		// ����Ĭ�Ͻ׶�
		this.state(STATE.BEGIN);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		byte b = in.readByte();
		if ('[' == b) {
			// �����յ���[��ʱ����CONTENT�׶�
			this.state(STATE.CONTENT);
			this.m_buffer = ByteBuffer.allocate(100);
		} else if (STATE.CONTENT == this.state()) {
			// ��CONTENT�׶κ�
			if (']' == b) {
				// ���յ���]��ʱΪ�����׶�
				this.state(STATE.END);

				// ת��Ϊ�ַ���
				this.m_buffer.flip();
				String text = Charset.forName("UTF-8").newDecoder().decode(this.m_buffer.asReadOnlyBuffer()).toString();
				// ���1������
				out.add(text);
				this.m_buffer = null;
			} else {
				// ���յ��ǡ�]��ʱ��Ϊ���ݲ���
				this.m_buffer.put(b);
			}
		} else {
			// �������Ϊ��ʼ�׶Σ��˴����ܿɹ���������
			this.state(STATE.BEGIN);
		}
	}

}
