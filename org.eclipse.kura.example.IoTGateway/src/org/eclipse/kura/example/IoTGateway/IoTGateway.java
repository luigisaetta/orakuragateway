package org.eclipse.kura.example.IoTGateway;

import java.util.Map;

import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.data.DataService;
import org.eclipse.kura.data.listener.DataServiceListener;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.kura.gpio.*;

public class IoTGateway implements DataServiceListener, ConfigurableComponent
{
	private static final Logger s_logger = LoggerFactory.getLogger(IoTGateway.class);

	private static final String APP_ID = "org.eclipse.kura.example.IoTGateway";

	/*
	 * 
	 * configurable parameters
	 */
	// the # of the PIN where the led is connected
	private static String testMode = "YES";
	private static int PIN_NUM = 17;
	private static String TOPIC = "device/AIRCARE/data";
	private static String MSG_TYPE = "AIRCARE";

	private static String TA_STORE_PWD = "Amsterdam1";
	// uso car2
	private static String TA_STORE_PATH = "/home/pi/orakuraconfig/device.tas";

	// here I have access to the Configurable properties
	private Map<String, Object> properties;

	// MQTT
	private DataService _dataService;
	// GPIO
	private GPIOService _gpioService = null;

	MessageParser parser = MessageParserFactory.createProcessor(MSG_TYPE);

	OracleIoTClient iotClient = null;

	// to control attached LED
	MyLED led = null;

	public IoTGateway()
	{
		led = new MyLED(PIN_NUM);
	}

	protected void setDataService(DataService dataService)
	{
		debug("Bundle " + APP_ID + " setDataServiceCalled!");

		_dataService = dataService;

		_dataService.addDataServiceListener(this);
	}

	protected void unsetDataService(DataService dataService)
	{
		// to avoid multiple message effect
		_dataService.removeDataServiceListener(this);

		_dataService = null;
	}

	protected void setGPIOService(GPIOService gpioService)
	{
		info("Bundle " + APP_ID + " setGPIOServiceCalled!");

		_gpioService = gpioService;

		led.setGPIOService(_gpioService);
	}

	protected void unsetGPIOService(GPIOService gpioService)
	{
		_gpioService = null;
	}


	/*
	 * new method for configuration
	 */
	protected void activate(ComponentContext componentContext, Map<String, Object> properties)
	{
		debug("Bundle " + APP_ID + " has started with config!");

		// subscribe to TOPIC
		subscribe();

		updated(properties);

		// define OUTPUT PIN
		led.activatePin();

		if (testMode.equals("NO"))
		{
			iotClient = new OracleIoTClient(TA_STORE_PATH, TA_STORE_PWD);
			iotClient.activateOracleGateway();
		}
	}

	protected void deactivate(ComponentContext componentContext)
	{
		info("Bundle " + APP_ID + " has stopped!");

		// here the code that disactivate bundle processing
		unSubscribe();

		iotClient = null;
	}

	/*
	 * 
	 * handle change of configuration
	 * 
	 */
	public void updated(Map<String, Object> properties)
	{
		info("called updated...");

		this.properties = properties;

		// changed topic
		Object newMsgTopic = properties.get("msg.topic");

		if ((newMsgTopic != null) && (!TOPIC.equals((String) newMsgTopic)))
		{
			// topic changed
			info("topic changed!");

			// unsubscribe from old topic
			unSubscribe();

			TOPIC = (String) newMsgTopic;

			subscribe();
		}

		// change msg.type
		Object newMsgType = properties.get("msg.type");

		if ((newMsgType != null) && (!MSG_TYPE.equals((String) newMsgType)))
		{
			// MSG_TYPE changed
			info("MSG_TYPE changed!");

			MSG_TYPE = (String) newMsgType;

			parser = MessageParserFactory.createProcessor(MSG_TYPE);
		}

		// changed mode.test
		Object newTestMode = properties.get("mode.test");

		if ((newTestMode != null) && (!testMode.equals(newTestMode)))
		{
			// MSG_TYPE changed
			info("testMode changed!");

			testMode = (String) newTestMode;

			if (testMode.equals("NO")) // not test mode
			{
				iotClient = new OracleIoTClient(TA_STORE_PATH, TA_STORE_PWD);
				iotClient.activateOracleGateway();
			}

		}

	}

	@Override
	public void onConnectionEstablished()
	{
		info("Connection established");

		subscribe();
	}

	@Override
	public void onConnectionLost(Throwable arg0)
	{
		info("Connection lost");

	}

	@Override
	public void onDisconnected()
	{

	}

	@Override
	public void onDisconnecting()
	{

	}

	@Override
	public void onMessageArrived(String topic, byte[] payload, int qos, boolean retained)
	{
		info(" message arrived on topic " + topic);

		// is on the subscribed topic?
		if (TOPIC.equals(topic))
		{
			// arrived on the subscribed here, process

			led.blink(100);

			String msg = new String(payload);

			info("sending to Oracle IoT Cloud ...");
			info("Msg: " + msg);

			// in test mode doesn't really send msg to IoT
			if (testMode.equals("NO"))
			{
				switch (MSG_TYPE)
				{
				case "TEMP":
					TempMessage tMsg = (TempMessage) parser.parse(msg);

					// send to Oracle IoT
					if (tMsg != null)
					{
						iotClient.send(tMsg);
					}

					break;
				case "OBD2":
					OBD2Message oMsg = (OBD2Message) parser.parse(msg);

					// send to Oracle IoT
					if (oMsg != null)
					{
						iotClient.send(oMsg);
					}

					break;

				case "SMART_LIGHT":
					SmartLightMessage sMsg = (SmartLightMessage) parser.parse(msg);

					if (sMsg != null)
					{
						info("Light Status: " + sMsg.getLightStatus());

						iotClient.send(sMsg);
					}
					break;

				case "EDISON":
					EdisonMessage eMsg = (EdisonMessage) parser.parse(msg);

					if (eMsg != null)
					{
						info("Temperature: " + eMsg.getData().getTemp());

						iotClient.send(eMsg);
					}
					break;
				case "AIRCARE":
					AircareMessage acMsg = (AircareMessage) parser.parse(msg);

					if (acMsg != null)
					{
						info("Temperature: " + acMsg.getTemp());

						iotClient.send(acMsg);
					}
					break;
				}
			} else
			{
				switch (MSG_TYPE)
				{
				case "BLE":
					BLEMessage bMsg = (BLEMessage) parser.parse(msg);

					if (bMsg != null)
					{
						info("UUid: " + bMsg.getMetrics().getUuid());
						info("UUid: " + bMsg.getMetrics().getMajor());
						info("UUid: " + bMsg.getMetrics().getMinor());
					}
					break;
				case "EDISON":
					EdisonMessage eMsg = (EdisonMessage) parser.parse(msg);

					if (eMsg != null)
					{
						info("Temp: " + eMsg.getData().getTemp());
						info("Air Quality: " + eMsg.getData().getAirQ());
						info("Light: " + eMsg.getData().getLight());
						info("Gas: " + eMsg.getData().getGas());
					}
					break;

				case "SMART_LIGHT":
					SmartLightMessage sMsg = (SmartLightMessage) parser.parse(msg);

					if (sMsg != null)
					{
						info("Light Status: " + sMsg.getLightStatus());
					}
					break;
				}
			}
		}

	}

	@Override
	public void onMessageConfirmed(int arg0, String arg1)
	{

	}

	@Override
	public void onMessagePublished(int arg0, String arg1)
	{

	}

	private void subscribe()
	{
		try
		{
			if ((_dataService != null) && (_dataService.isConnected()))
			{
				// subscribe to MQTT topic on local broker
				_dataService.subscribe(TOPIC, 1);

				debug("subscription done to topic " + TOPIC);
			}
		} catch (Exception e)
		{
			error("failed to subscribe to topic: " + e);
		}
	}

	private void unSubscribe()
	{
		try
		{
			if ((_dataService != null) && (_dataService.isConnected()))
			{
				_dataService.unsubscribe(TOPIC);
				debug("unsubscribe done on " + TOPIC);
			}

		} catch (Exception e)
		{
			error("failed to unsubscribe to topic: " + e);
		}
	}

	/*
	 * utility methods for logging
	 * 
	 */
	private static void info(String msg)
	{
		s_logger.info(msg);
	}

	private static void debug(String msg)
	{
		s_logger.debug(msg);
	}

	private static void error(String msg)
	{
		s_logger.error(msg);
	}
}
