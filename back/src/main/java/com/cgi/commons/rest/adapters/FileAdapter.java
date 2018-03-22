package com.cgi.commons.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.codec.binary.Base64;

/**
 * File Adapter.
 */
public class FileAdapter extends XmlAdapter<String, byte[]>
{
	@Override
	public byte[] unmarshal(String v) throws Exception
	{
		return Base64.decodeBase64(v);
	}

	@Override
	public String marshal(byte[] v) throws Exception
	{
		return new String(Base64.encodeBase64(v));
	}
}
