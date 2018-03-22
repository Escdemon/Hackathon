package com.cgi.commons.utils.export;

import java.io.File;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.cgi.commons.db.DbQuery;
import com.cgi.commons.ref.context.RequestContext;

/**
 * Utility class used to handle Excel export (XLSX format).
 */
public class XlsxWriter extends ExcelWriter {

	public XlsxWriter(RequestContext ctx, DbQuery query) {
		super(ctx, query);
	}

	@Override
	protected Workbook initWorkbook() {
		initPoiTmpDirectory();
		SXSSFWorkbook workbook = new SXSSFWorkbook(1);
		workbook.setCompressTempFiles(true); // temp files will be gzipped
		return workbook;
	}

	@Override
	protected void disposeWorkbook(Workbook workbook) {
		((SXSSFWorkbook) workbook).dispose();
	}

	/**
	 * Initialise le dossier temporaire de POI si n�cessaire.
	 */
	private void initPoiTmpDirectory() {
		/*
		 * La strat�gie par d�faut utilis�e par POI pour �crire les fichiers temporaires ne v�rifie pas que le dossier cr�� initialement existe
		 * avant �criture d'un nouveau fichier, ce qui peut provoquer une IOException si le dossier a �t� supprim� exterieurement. Ce code est
		 * inutile si vous changez la-dite strat�gie...
		 */
		File poiTmpDir = new File(System.getProperty("java.io.tmpdir"), "poifiles");

		if (!poiTmpDir.exists()) {
			poiTmpDir.mkdir();
		}
	}

	@Override
	protected void resizeColumns(Sheet sheet) {
		// New SXSSF format needs column tracking before autosizing
		((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();

		super.resizeColumns(sheet);
	}

}
