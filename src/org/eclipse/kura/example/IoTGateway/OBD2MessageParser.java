package org.eclipse.kura.example.IoTGateway;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class OBD2MessageParser implements MessageParser
{
	private static final Logger s_logger = LoggerFactory.getLogger(MessageParser.class);

	private int MIN_LENGTH = 70;

	Gson gson = new Gson();

	@Override
	public OBD2Message parse(String mess)
	{
		OBD2Message outMess = null;

		mess = encodeUTF8(mess);

		if (isPayloadOK(mess))
		{
			try
			{
				outMess = gson.fromJson(mess, OBD2Message.class);
			} catch (JsonSyntaxException e)
			{
				error("Malformed request!");
				e.printStackTrace();
			}

		} else
		{
			error("Malformed request!");
		}
		return outMess;
	}

	/*
	 * does a minimal control on the payload
	 */
	private boolean isPayloadOK(String s)
	{
		if (s != null && s.length() >= MIN_LENGTH)
			return true;
		else
			return false;
	}

	private String encodeUTF8(String sInput)
	{
		String sOutput = null;

		try
		{
			sOutput = new String(sInput.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return sOutput;
	}

	private static void error(String msg)
	{
		s_logger.error(msg);
	}
}
