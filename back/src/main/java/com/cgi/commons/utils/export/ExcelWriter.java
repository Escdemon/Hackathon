package com.cgi.commons.utils.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.cgi.business.application.ApplicationLogic;
import com.cgi.commons.db.DbManager;
import com.cgi.commons.db.DbQuery;
import com.cgi.commons.db.DbQuery.Var;
import com.cgi.commons.logic.DomainLogic;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.data.ColumnData;
import com.cgi.commons.ref.entity.Entity;
import com.cgi.commons.utils.ApplicationUtils;
import com.cgi.commons.utils.MessageUtils;
import com.cgi.commons.utils.TechnicalException;
import com.cgi.commons.utils.reflect.DomainUtils;

/**
 * Utility class used to handle Excel export (common methods).
 */
public abstract class ExcelWriter {

	/** Logger. */
	protected final Logger LOGGER = Logger.getLogger(this.getClass());
	
	/** Current context */
	protected RequestContext context;
	
	/** Launched query */
	protected DbQuery query;
	
	/** Message util instanciated with context */
	protected MessageUtils msg;
	
	/** Number of columns in the sheet */
	protected int columnCount = 0;
	
	/** Query's main entity DomainLogic instance */
	private DomainLogic<? extends Entity> domainLogic;

	/** Columns metadata */
	private Map<String, ColumnData> columnsData;
	
	/**
	 * Constructor
	 * 
	 * @param ctx current context
	 * @param query DbQuery to export
	 */
	public ExcelWriter(RequestContext ctx, DbQuery query) {
		this.context = ctx;
		this.query = query;
		this.msg = MessageUtils.getInstance(context);

		domainLogic = DomainUtils.getLogic(query.getMainEntity().name(), context);
		
		// Init columns meta data
		columnsData = new HashMap<String, ColumnData>(query.getOutVars().size());
		for (DbQuery.Var var : query.getOutVars()) {
			String columnKey = var.tableId + "_" + var.name;
			ColumnData columnData = new ColumnData();
			columnData.setTitle(domainLogic.internalDbQueryColumnCaption(query, null, columnKey, context));
			columnData.setVisible(domainLogic.internalDbQueryColumnIsVisible(query, null, columnKey, context));
			columnsData.put(columnKey, columnData);
		}
	}


	/**
	 * Export query results to an Excel file.
	 * 
	 * @param file
	 *            Destination file.
	 * @see #writeFile(File, Workbook)
	 */
	public void export(File file) {
		Workbook workbook = null;
		DbManager mgr = null;
		try {
			// Init Workbook with correct format
			workbook = initWorkbook();
			Sheet sheet = workbook.createSheet(query.getName());

			/* Headers. */
			Row headerRow = sheet.createRow(0);
			initHeaderColumns(columnsData, headerRow);

			/* Data. */
			ApplicationLogic appLogic = ApplicationUtils.getApplicationLogic();
			DateFormat defaultDateFormatter = appLogic.getDateFormat(context);
			DateFormat defaultTimeFormatter = appLogic.getTimeFormat(context);
			DateFormat defaultTimestampFormatter = appLogic.getTimestampFormat(context);
			Row row;
			int i = 1;
			mgr = new DbManager(context, query);
			while (mgr.next())
			{
				com.cgi.commons.ref.data.Row r = mgr.getNextRow(domainLogic, defaultDateFormatter, defaultTimeFormatter, defaultTimestampFormatter);
				row = sheet.createRow(i++);

				populateRow(columnsData, row, r);
			}
			
			resizeColumns(sheet);
			
			writeFile(file, workbook);
		} catch (Exception ex) {
			throw new TechnicalException(ex.getMessage(), ex);
		} finally {
			// Close DbManager
			if (mgr != null)
			{
				mgr.close();
				mgr = null;
			}
			if (this.context != null) {
				this.context.close();
				this.context = null;
			}
			this.domainLogic = null;
			this.query = null;
			this.msg = null;
			if (workbook != null)
			{
				disposeWorkbook(workbook);
			}
		}
	}
	
	/**
	 * Preprare the column title cells.
	 * 
	 * @param columnsData columns data
	 * @param headerRow current workbook row
	 */
	private void initHeaderColumns(Map<String, ColumnData> columnsData, Row headerRow) {
		Cell headerCell;
		int i = 0;
		for (Var v : query.getOutVars()) {
			ColumnData columnData = columnsData.get(v.tableId + "_" + v.name);
			if (!columnData.isVisible()) {
				continue;
			}
			String header = columnData.getTitle();

			if (header == null || "".equals(header)) {
				header = msg.getQryVarTitle(query.getName(), v.tableId, v.name);
			}
			headerCell = headerRow.createCell(i++);
			headerCell.setCellValue(header);
		}
		columnCount = i;
	}
	
	/**
	 * Store current data row into the sheet row
	 * 
	 * @param columnsData columns data
	 * @param sheetRow current workbook row
	 * @param dataRow current data row
	 */
	private void populateRow(Map<String, ColumnData> columnsData, Row sheetRow, com.cgi.commons.ref.data.Row dataRow) {
		Cell cell;
		int j = 0;
		for (Var var : query.getOutVars()) {
			ColumnData columnData = columnsData.get(var.tableId + "_" + var.name);

			if (!columnData.isVisible()) {
				continue;
			}
			cell = sheetRow.createCell(j++);
			
			Object value = dataRow.get(var.tableId + "_" + var.name);

			if (value instanceof String) {
				cell.setCellValue((String) value);
			} else if (value instanceof Boolean) {
				cell.setCellValue((Boolean) value);
			} else if (value instanceof Number) {
				cell.setCellValue(((Number) value).doubleValue());
			}
		}
	}
	
	/**
	 * Resize Sheet columns
	 * 
	 * @param sheet current WorkSheet
	 */
	protected void resizeColumns(Sheet sheet) {
		/* Columns are resized automatically, then a character is added to avoid problems (with false boolean for example). */
		for (int i = 0; i < columnCount; i++) {
			sheet.autoSizeColumn(i);
			if (sheet.getColumnWidth(i) <= (254 * 256))
				// max is 255 chars
				sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 256);
		}
	}

	/**
	 * Write Excel workbook in the given file.
	 * 
	 * @param file
	 *            File in which workbook is written.
	 * @param workbook
	 *            Excel workbook to write.
	 * @throws IOException
	 *             Exception thrown is an error occurred during writing workbook or closing the file output stream.
	 */
	private void writeFile(File file, Workbook workbook) throws IOException {
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(file);
			workbook.write(fos);

		} catch (IOException exception) {
			LOGGER.error("Error while writing Excel workbook in the file : " + file.getAbsolutePath(), exception);
			throw exception;

		} finally {

			if (null != fos) {

				try {
					fos.close();

				} catch (IOException exception) {
					LOGGER.error("Error while closing output stream for file : " + file.getAbsolutePath(), exception);
					throw exception;
				}
			}
		}
	}

	/**
	 * Create a new workbook in the desired format
	 * 
	 * @return an instance of Workbook
	 */
	protected abstract Workbook initWorkbook();
	
	/**
	 * Dispose of the Workbook after processing
	 * @param the current Workbook
	 */
	protected abstract void disposeWorkbook(Workbook workbook);
}
