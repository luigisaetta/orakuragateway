package org.eclipse.kura.example.IoTGateway;

import com.google.gson.annotations.SerializedName;

public class TempMessage extends Message
{
	@SerializedName("CARID")
	String carId;
	
	@SerializedName("LAT")
	double lat;
	@SerializedName("LON")
	double lng;
	@SerializedName("COOLANT_TEMP")
	double temp;
	
	public double getLat()
	{
		return lat;
	}
	public void setLat(double lat)
	{
		this.lat = lat;
	}
	public double getLng()
	{
		return lng;
	}
	public void setLng(double lng)
	{
		this.lng = lng;
	}
	public double getTemp()
	{
		return temp;
	}
	public void setTemp(double temp)
	{
		this.temp = temp;
	}
	public String getCarId()
	{
		return carId;
	}
	public void setCarId(String carId)
	{
		this.carId = carId;
	}
	/*
	 * in this case deviceId == carId
	 */
	public String getDeviceId()
	{
		return carId;
	}
}
