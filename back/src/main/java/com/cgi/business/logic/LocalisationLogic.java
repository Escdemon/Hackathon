package com.cgi.business.logic;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cgi.business.application.DefaultLogic;
import com.cgi.commons.db.DB;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.controller.Request;
import com.cgi.commons.ref.entity.Action;
import com.cgi.commons.ref.entity.Key;
import com.cgi.commons.ref.entity.Action.Persistence;
import com.cgi.commons.utils.DateUtils;
import com.cgi.models.beans.Localisation;
import com.cgi.models.constants.LocalisationConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Business class for the entity Localisation.
 *
 */
public class LocalisationLogic extends DefaultLogic<Localisation> implements LocalisationConstants {
	
	@Override
	public List<Key> doCustomAction(Request<Localisation> request, Localisation entity, RequestContext ctx) {
		Action action = request.getAction();
		if (action.is(Actions.ACTION_CREATE) || action.is(Actions.ACTION_CREATE_ALERT)) {
			DB.insert(entity, ctx);
		}
		return super.doCustomAction(request, entity, ctx);
	}
	
	@Override
	public void dbOnSave(Localisation bean, Action action, RequestContext ctx) {
		if (action.is(Actions.ACTION_CREATE_ALERT)) {
			// statut à KO automatiquement
			bean.setStatut(false);
		}
		if (Persistence.INSERT.equals(action.getPersistence())) {
			bean.setHeure(DateUtils.todayNow());
			bean.setBaliseId(new Long(1));
			Localisation temp = getPositionString();
			if (temp != null) {
				bean.setCoordX(temp.getCoordX());
				bean.setCoordY(temp.getCoordY());
			}			
		}
		super.dbOnSave(bean, action, ctx);
	}
	
	private Localisation getPositionString() {
		try {
			FileInputStream fis = new FileInputStream(new File("C:\\tmp\\position.json"));
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line = null;
			Localisation temp = new Localisation();
			while ((line = br.readLine()) != null) {
				/*Pattern p = Pattern.compile("([0-9]*) ([0-9]*)");
				Matcher m = p.matcher(line);
				m.find();
				Integer coordX = Integer.valueOf(m.group(1));
				Integer coordY = Integer.valueOf(m.group(2));
				temp.setCoordX(coordX);
				temp.setCoordY(coordY);*/
				try {
					if (!line.equals("") && line.length() > 13 && line.substring(0,1).equals("x")) {
				        String[] t = line.split(" ");
				        temp.setCoordX(Integer.valueOf(t[0].split(":")[1]));
				        temp.setCoordX(Integer.valueOf(t[1].split(":")[1]));   
	                }
	            } catch (ArrayIndexOutOfBoundsException e) {
	            	continue;
	            }
			}		 
			br.close();
			return temp;
		} catch (Exception e) {
			
		}
		return null;
	}

}
