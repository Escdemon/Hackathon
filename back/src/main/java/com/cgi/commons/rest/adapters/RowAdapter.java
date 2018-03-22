package com.cgi.commons.rest.adapters;

import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.cgi.commons.ref.data.Row;
import com.cgi.commons.rest.domain.MapElements;

/**
 * Row Adapter.
 */
public class RowAdapter extends XmlAdapter<MapElements[], Row> {

	@Override
	public MapElements[] marshal(Row r) throws Exception {
		MapElements[] mapElements = new MapElements[r.size()];
		int i = 0;
		for (Map.Entry<String, Object> entry : r.entrySet())
			mapElements[i++] = new MapElements(entry.getKey(), entry.getValue());

		return mapElements;
	}

	@Override
	public Row unmarshal(MapElements[] v) throws Exception {
		Row r = new Row();
		for (MapElements mapelement : v) {
			r.put(mapelement.key, mapelement.value);
		}
		return r;
	}

}
