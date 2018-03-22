package com.cgi.commons.rest.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.cgi.commons.ref.entity.FileContainer;

/**
 * File Container Adapter.
 */
public class FileContainerAdapter extends XmlAdapter<String, FileContainer> {
	@Override
	public FileContainer unmarshal(String v) throws Exception {
		// Hope nobody will call this.
		FileContainer file = new FileContainer();
		file.setNull(!Boolean.valueOf(v));
		return file;
	}

	@Override
	public String marshal(FileContainer v) throws Exception {
		return String.valueOf(!v.isNull());
	}
}
