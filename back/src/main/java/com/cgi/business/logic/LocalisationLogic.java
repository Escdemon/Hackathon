package com.cgi.business.logic;

import com.cgi.business.application.DefaultLogic;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.commons.ref.entity.Action;
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
	public void dbOnSave(Localisation bean, Action action, RequestContext ctx) {
		if (action.is(Actions.ACTION_CREATE_ALERT)) {
			// statut à KO automatiquement
			bean.setStatut(false);
		}
		if (Persistence.INSERT.equals(action.getPersistence())) {
			bean.setHeure(DateUtils.todayNow());
		}
		super.dbOnSave(bean, action, ctx);
	}

}
