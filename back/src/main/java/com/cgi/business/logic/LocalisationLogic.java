package com.cgi.business.logic;

import java.util.List;

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
			// get position from position.json
//			JSONParser parser = new JSONParser();
//			JSONArray a = (JSONArray) parser.parse(new FileReader("c:\\tmp\\position.json"));
			
		}
		super.dbOnSave(bean, action, ctx);
	}

}
