package top.fols.aapp.utils;

import android.os.Handler;
import android.os.Message;
import top.fols.box.annotation.XAnnotations;
import top.fols.box.statics.XStaticFixedValue;
public class XUIHandler {
	public static interface Run {
		public abstract void run(Object[] value);
	}
	public static interface MessageDeal {
		public abstract Object dealMessages(Object[] value);
	}



	private static enum type {
		sendMessage(0),
		sendRun(2);

		int value;
		type (int i) {
			this.value = i;
		}
	}

	private Handler uIhandler;
	@XAnnotations("do not run before Activity onCreate")
	public XUIHandler() {
		this.uIhandler = new XHandler();
	}





	public XUIHandler sendMessages(MessageDeal methodback) {
		if (methodback == null)
			return this;
		sendMessages(null, methodback);
		return this;
	}
	public XUIHandler sendMessages(Object[] value, MessageDeal methodback) {
		if (methodback == null)
			return this;
		send(type.sendMessage.value, value, methodback);
		return this;
	}



	@XAnnotations("in handler exec method @dealExecMethodInHandlerThrowable")
	public XUIHandler sendRun(Run Run) {
		if (Run == null)
			return this;
		sendRun(null, Run);
		return this;
	}
	@XAnnotations("in handler exec method @dealExecMethodInHandlerThrowable")
	public XUIHandler sendRun(Object[] sendValue, Run Run) {
		if (Run == null)
			return this;
		send(type.sendRun.value, sendValue, Run);
		return this;
	}
	
	
	
	
	
	public static XUIHandler create() {
		try {
			return new XUIHandler();
		} catch (Throwable e) {
			return null;
		}
	}
	
	
	
	
	
	private void send(int args, Object... sendValue) {
		if (sendValue == null)
			sendValue = XStaticFixedValue.nullObjectArray;
		Message message = new Message();
		message.arg1 = args;
		message.obj = sendValue;
		uIhandler.sendMessage(message);
	}
	private class XHandler extends Handler {
		public void handleMessage(Message message) {
			super.handleMessage(message);
			
			if (message.arg1 == type.sendMessage.value)
				((MessageDeal)(((Object[])message.obj)[1])).dealMessages((Object[])(((Object[])message.obj)[0]));
			else if (message.arg1 == type.sendRun.value)
				((Run)(((Object[])message.obj)[1])).run((Object[])(((Object[])message.obj)[0]));
			
			message.obj = null;
		}
	}


}



