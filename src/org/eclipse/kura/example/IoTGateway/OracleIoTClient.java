package org.eclipse.kura.example.IoTGateway;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
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
	* encapsulated in XXXXModel
	*/

	// path and name of trusted assett store (from Device registration)
	private static String TA_STORE_PWD = null;
	private static String TA_STORE_PATH = null;

	private GatewayDevice gw = null;
	
	// instead of 4 device model an Hashtable
	private Hashtable<String, DeviceModel> tableDevModel = new Hashtable<String, DeviceModel>();
	
	// device model for loading table
	private DeviceModel deviceModel = null;
		
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
				
				info("OK gateway activated");
			} else
			{
				info("Gateway already activated !");
			}

			info("OK after Gateway activation ...");

			// download list of supported device models
			// use an hashmap, with the urn as a key
			downloadDeviceModels();
			
		} catch (Exception e)
		{
			error("in activating gateway...");
			
			error(e.getMessage());
			error(e.toString());

			return;
		}
	}
	
	private void downloadDeviceModels()
	{
		// OBD2, TEMP, LIGHT, EDISON, AIRCARE
		try
		{
			List<String> modelList = new ArrayList<String>();
			
			// define list of supported DeviceModel
			modelList.add(OBD2Model.OBD2_URN_MSG);
			modelList.add(TemperatureModel.TEMP_URN_MSG);
			modelList.add(SmartLightModel.LIGHT_URN_MSG);
			modelList.add(EdisonModel.EDI_URN_MSG);
			modelList.add(AircareModel.AIRCARE_URN_MSG);
			
			// load in tableDevModel
			for(String modelType : modelList) 
			{
				deviceModel = gw.getDeviceModel(modelType);
				
				if (deviceModel != null)
				{
					tableDevModel.put(modelType, deviceModel);
					debug(deviceModel.getURN());
				}
			}
		} catch (Exception e)
		{
			error("in downloadDeviceModels...");
			
			error(e.getMessage());
			error(e.toString());
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
				
				info("OK: Msg sent to Iot...");
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
				
				// Modified, now, search in hashtable
				DeviceModel deviceModel = tableDevModel.get(deviceModelUrn);
				
				
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
