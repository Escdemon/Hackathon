package com.cgi.commons.utils.export;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import com.cgi.commons.db.DbQuery;
import com.cgi.commons.ref.context.RequestContext;

/**
 * Utility class used to handle Excel export (XLS format).
 */
public class XlsWriter extends ExcelWriter {

	public XlsWriter(RequestContext ctx, DbQuery query) {
		super(ctx, query);
	}

	@Override
	protected Workbook initWorkbook() {
		return new HSSFWorkbook();
	}

	@Override
	protected void disposeWorkbook(Workbook workbook) {
		// Nothing to do for dispose on this version
	}
}
