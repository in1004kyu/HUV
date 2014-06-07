package kookmin.kesl ;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import zephyr.android.HxMBT.*;

public class NewConnectedListener extends ConnectListenerImpl
{
	private Handler _aNewHandler; 
	private int HR_SPD_DIST_PACKET =0x26;
	
	private final int HEART_RATE = 0x100;
	private HRSpeedDistPacketInfo HRSpeedDistPacket = new HRSpeedDistPacketInfo();
	public NewConnectedListener(Handler handler,Handler _NewHandler) {
		super(handler, null);
		_aNewHandler = _NewHandler;
		// TODO Auto-generated constructor stub

	}
	public void Connected(ConnectedEvent<BTClient> eventArgs) {
		System.out.println(String.format("Connected to BioHarness %s.", eventArgs.getSource().getDevice().getName()));

		//Creates a new ZephyrProtocol object and passes it the BTComms object
		ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms());
		//ZephyrProtocol _protocol = new ZephyrProtocol(eventArgs.getSource().getComms(), );
		_protocol.addZephyrPacketEventListener(new ZephyrPacketListener() {
			public void ReceivedPacket(ZephyrPacketEvent eventArgs) {
				ZephyrPacketArgs msg = eventArgs.getPacket();

				if (HR_SPD_DIST_PACKET==msg.getMsgID())
				{				
					byte [] DataArray = msg.getBytes();
					
					//***************Displaying the Heart Rate********************************
					int HRate =  HRSpeedDistPacket.GetHeartRate(DataArray);
					Message text1 = _aNewHandler.obtainMessage(HEART_RATE);
					Bundle b1 = new Bundle();
					b1.putString("HeartRate", String.valueOf(HRate));
					text1.setData(b1);
					_aNewHandler.sendMessage(text1);
					System.out.println("Heart Rate is "+ HRate);

				}
			}
		});
	}
	
}