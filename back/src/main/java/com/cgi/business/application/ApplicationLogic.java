package com.cgi.business.application;


import com.cgi.commons.db.DB;
import com.cgi.commons.logic.DefaultApplicationLogic;
import com.cgi.commons.ref.context.ApplicationContext;
import com.cgi.commons.ref.context.RequestContext;
import com.cgi.models.beans.Localisation;
import com.cgi.models.constants.LocalisationConstants;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class used to store application logic methods.
 * Use {@link ApplicationUtils#getApplicationLogic()} to get an instance.
 */
public class ApplicationLogic extends DefaultApplicationLogic {
	
	private ScheduledExecutorService scheduler;
	private ScheduledFuture<?> scheduledFuture;
	
	@Override
	public void initializeApplication(ApplicationContext context) {
		
		scheduler = Executors.newScheduledThreadPool(1);
		scheduledFuture = scheduler.scheduleAtFixedRate(new Thread(				
				new TimerTask() {
			  @Override
			  public void run() {
				RequestContext ctx = new RequestContext(new User("admin"));
			    DB.insert(new Localisation(), Localisation.getEntityModel().getAction(LocalisationConstants.Actions.ACTION_CREATE), ctx);
			    System.out.println("done");
			    ctx.getDbConnection().commit();
			  }
			})			
				, 1*1000, 10*1000, TimeUnit.MILLISECONDS);
		super.initializeApplication(context);
	}
	
	@Override
	public void finalizeApplication(ApplicationContext context) {
		if (scheduledFuture != null)
			scheduledFuture.cancel(true);
		scheduler.shutdown();
		super.finalizeApplication(context);
	}

}
