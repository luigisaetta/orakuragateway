package org.eclipse.kura.example.IoTGateway;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oracle.iot.client.DeviceModel;
import oracle.iot.client.device.GatewayDevice;
import oracle.iot.client.device.VirtualDevice;

public class OracleIoTClient
{
	private static final Logger s_logger = LoggerFactory.getLogger(OracleIoTClient.class);
	
	/* integration with Oracle IoT
	* the Device Model used is
	* encapsulated on OBD2Model/TemperatureModel
	
	* name of attributes in Oracle DeviceModel
	* are now encapsulated as static in OBD2Model
	*/

	// path and name of trusted assett store (from Device registration)
	private static String TA_STORE_PWD = null;
	private static String TA_STORE_PATH = null;

	private GatewayDevice gw = null;
	
	// instead of 4 devicemodel an Hashtable
	private Hashtable<String, DeviceModel> tableDevModel = new Hashtable<String, DeviceModel>();
	
	// device model for OBD2
	private DeviceModel deviceModel1 = null;
	// device model for TEMP
	private DeviceModel deviceModel2 = null;
	//device model for smart light
	private DeviceModel deviceModel3 = null;
	//device model for edison
	private DeviceModel deviceModel4 = null;
	// device model for Aircare
	private DeviceModel deviceModel5 = null;
		
	// the cache containing the list of devices already registered with this gateway
	DeviceCache hDevices = new DeviceCache();
	
	public OracleIoTClient(String tasPath, String tasPwd)
	{
		TA_STORE_PATH = tasPath;
		TA_STORE_PWD = tasPwd;
	}

	/*
	 * 
	 * Activation of Oracle Gateway (the Kura one)
	 * 
	 */
	public void activateOracleGateway()
	{
		try
		{
			gw = new GatewayDevice(TA_STORE_PATH, TA_STORE_PWD);

			if (!gw.isActivated())
			{
				info("Activating the Gateway !");
				gw.activate();
				
				info("OK gateway activate");
			} else
			{
				info("Gateway already activated !");
			}

			info("OK after Gateway activation ...");

			// download list of supported device models
			// TODO could be better handled (use an hashmap, with the urn as a key)
			deviceModel1 = gw.getDeviceModel(OBD2Model.OBD2_URN_MSG);
			
			if (deviceModel1 != null)
			{
				tableDevModel.put(OBD2Model.OBD2_URN_MSG, deviceModel1);
				debug(deviceModel1.getURN());
			}
			
			deviceModel2 = gw.getDeviceModel(TemperatureModel.TEMP_URN_MSG);
			
			if (deviceModel2 != null)
			{
				tableDevModel.put(TemperatureModel.TEMP_URN_MSG, deviceModel2);
				debug(deviceModel2.getURN());
			}
			
			deviceModel3 = gw.getDeviceModel(SmartLightModel.LIGHT_URN_MSG);
			
			if (deviceModel3 != null)
			{
				tableDevModel.put(SmartLightModel.LIGHT_URN_MSG, deviceModel3);
				debug(deviceModel3.getURN());
			}
			
			deviceModel4 = gw.getDeviceModel(EdisonModel.EDI_URN_MSG);
			
			if (deviceModel4 != null)
			{
				debug(deviceModel4.getURN());
				tableDevModel.put(EdisonModel.EDI_URN_MSG, deviceModel4);
			}
			
			deviceModel5 = gw.getDeviceModel(AircareModel.AIRCARE_URN_MSG);
			
			if (deviceModel5 != null)
			{
				debug(deviceModel5.getURN());
				tableDevModel.put(AircareModel.AIRCARE_URN_MSG, deviceModel5);
			}
			
		} catch (Exception e)
		{
			error("in activating gateway...");
			
			error(e.getMessage());
			error(e.toString());
			
			e.printStackTrace();

			return;
		}
	}

	/**
	 * 
	 * send the message to Oracle IoT
	 * 
	 */
	public void send(OBD2Message msg)
	{
		VirtualDevice virtualDevice = null;

		// added check to avoid NPE
		if ((msg == null) || (msg.getDeviceId() == null))
			return;

		try
		{
			//
			// data set to zero are not available now
			//
			debug("Trying to sending to Iot...");
			
			// Lazy Registration of Device
			virtualDevice = getVirtualDevice(msg.getDeviceId(), OBD2Model.OBD2_URN_MSG);
			
			if (virtualDevice != null)
			{
				// name of attributes in Oracle DeviceModel are encapsulated in OBD2Model
				virtualDevice.update()
					.set(OBD2Model.LATITUDE, msg.getLat())
					.set(OBD2Model.LONGITUDE, msg.getLng())
					.set(OBD2Model.ALTITUDE, msg.getAlt())
					.set(OBD2Model.SPEED, msg.getSpeed())
					.set(OBD2Model.RPM, msg.getRpmAsLong())
					.set(OBD2Model.COOLANT_TEMPERATURE, msg.getCoolantTemp())
					.set(OBD2Model.MAF, msg.getMaf())
					.set(OBD2Model.RUNTIME_SINCE_ENGINE_START, msg.getRuntime())
					.set(OBD2Model.DTCS, 0)
					.finish();
				
				info("Msg sent to Iot...");
			}
			
		} catch (Exception e)
		{
			// continue
			e.printStackTrace();
		}
	}
	
	public void send(TempMessage msg)
	{
		info("sending to Oracle Iot TEMP = " + msg.getTemp());
		
		VirtualDevice virtualDevice = null;

		// added check to avoid NPE
		if ((msg == null) || (msg.getDeviceId() == null))
			return;

		try
		{
			//
			// data set to zero are not available now
			//
			debug("Trying to sending to Iot...");
			
			// Lazy Registration of Device
			virtualDevice = getVirtualDevice(msg.getDeviceId(), TemperatureModel.TEMP_URN_MSG);
			
			if (virtualDevice != null)
			{
				// name of attributes in Oracle DeviceModel are encapsulated in TemperatureModel
				virtualDevice.update()
					.set(TemperatureModel.LATITUDE, msg.getLat())
					.set(TemperatureModel.LONGITUDE, msg.getLng())
					.set(TemperatureModel.TEMP, msg.getTemp())
					.finish();
				
				info("Msg sent to Iot...");
			}
			
		} catch (Exception e)
		{
			// continue
			e.printStackTrace();
		}
	}
	
	public void send(BLEMessage msg)
	{
		// still in test
		info("Msg sent to Iot...");
	}
	
	public void send(SmartLightMessage msg)
	{
		VirtualDevice virtualDevice = null;

		// added check to avoid NPE
		if ((msg == null) || (msg.getDeviceId() == null))
			return;
		
		try
		{
			//
			// data set to zero are not available now
			//
			debug("Trying to sending to Iot...");
			
			// Lazy Registration of Device
			virtualDevice = getVirtualDevice(msg.getDeviceId(), SmartLightModel.LIGHT_URN_MSG);
			
			if (virtualDevice != null)
			{
				// name of attributes in Oracle DeviceModel are encapsulated in TemperatureModel
				virtualDevice.update()
					.set(SmartLightModel.LIGHT_STATUS, msg.getLightStatus())
					.finish();
				
				info("Msg sent to Iot...");
			}
			
		} catch (Exception e)
		{
			// continue
			e.printStackTrace();
		}
	}
	
	public void send(EdisonMessage msg)
	{
		VirtualDevice virtualDevice = null;

		// added check to avoid NPE
		if ((msg == null) || (msg.getSource() == null))
			return;
		
		try
		{
			//
			// data set to zero are not available now
			//
			debug("Trying to sending to Iot...");
			
			// Lazy Registration of Device
			virtualDevice = getVirtualDevice(msg.getSource(), EdisonModel.EDI_URN_MSG);
			
			if (virtualDevice != null)
			{
				// name of attributes in Oracle DeviceModel are encapsulated in TemperatureModel
				virtualDevice.update()
					.set(EdisonModel.TEMP, msg.getData().getTemp())
					.set(EdisonModel.HUMIDITY, msg.getData().getHum())
					.set(EdisonModel.LIGHT, msg.getData().getLight())
					.set(EdisonModel.AIRQ, msg.getData().getAirQ())
					.set(EdisonModel.GAS, msg.getData().getGas())
					.finish();
				
				info("Msg sent to Iot...");
			}
			
		} catch (Exception e)
		{
			// continue
			e.printStackTrace();
		}
	}
	
	public void send(AircareMessage msg)
	{
		VirtualDevice virtualDevice = null;

		// added check to avoid NPE
		if ((msg == null))
			return;
		
		try
		{
			//
			// data set to zero are not available now
			//
			debug("Trying to sending to Iot...");
			
			// Lazy Registration of Device
			
			//TODO.. here I have forced deviceId == aircare (should better implement)
			virtualDevice = getVirtualDevice("aircare", AircareModel.AIRCARE_URN_MSG);
			
			if (virtualDevice != null)
			{
				// name of attributes in Oracle DeviceModel are encapsulated in TemperatureModel
				virtualDevice.update()
					.set(AircareModel.TEMP, msg.getTemp())
					.set(AircareModel.HUMIDITY, msg.getHum())
					.set(AircareModel.LIGHT, msg.getLight())
					.set(AircareModel.PM25, msg.getPm25())
					.set(AircareModel.PM10, msg.getPm10())
					.finish();
				
				info("Msg sent to Iot...");
			}
			
		} catch (Exception e)
		{
			// continue
			e.printStackTrace();
		}
	}
	/*
	 * 
	 * get Virtual Device from hashmap... if needed register
	 * 
	 * 
	 */
	private VirtualDevice getVirtualDevice(String msgDeviceId, String deviceModelUrn)
	{
		String deviceId = null;
		VirtualDevice virtualDevice = null;
		
		// Lazy Registration of Device
		if (hDevices.get(msgDeviceId) == null)
		{
			// NOT FOUND in HashTable, register it

			// add any vendor-specific meta-data to the metaData Map
			Map<String, String> metaData = initMetadataMap(msgDeviceId);

			try
			{
				// getDeviceId (carID) as DEVICE_ACTIVATION_ID
				deviceId = gw.registerDevice(msgDeviceId, metaData, deviceModelUrn);

				info("OK after device registration: " + deviceId);
				
				// to be better handled
				DeviceModel deviceModel = null;
				
				
				// Modified, now, search in hashtable
				deviceModel = tableDevModel.get(deviceModelUrn);
				
				virtualDevice = gw.createVirtualDevice(deviceId, deviceModel);

				// save in Hashtable
				info("save in hashtable");
				hDevices.put(msgDeviceId, virtualDevice);
			} catch (Exception e)
			{
				e.printStackTrace();

				return null;
			}
		}
		else
		{
			virtualDevice = hDevices.get(msgDeviceId);
		}
		
		return virtualDevice;
	}

	/**
	 * init metadata Map
	 */
	private Map<String, String> initMetadataMap(String deviceActivationId)
	{
		// create meta-data with the indirectly-connected device's
		// manufacturer, model, and serial number
		Map<String, String> metaData = new HashMap<String, String>();

		metaData.put(oracle.iot.client.device.GatewayDevice.MANUFACTURER, OBD2Model.MANUFACTURER);
		metaData.put(oracle.iot.client.device.GatewayDevice.MODEL_NUMBER, OBD2Model.DEVICE_MODEL_NUMBER);
		metaData.put(oracle.iot.client.device.GatewayDevice.SERIAL_NUMBER, deviceActivationId);

		return metaData;
	}

	/*
	 * utility methods for logging
	 * 
	 */
	private void info(String msg)
	{
		s_logger.info(msg);
	}

	private void debug(String msg)
	{
		s_logger.debug(msg);
	}
	private static void error(String msg)
	{
		s_logger.error(msg);
	}
}
