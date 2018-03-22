package com.cgi.commons.security;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.apache.log4j.Logger;

/**
 * Factory for LdapConfig.
 */
public class LdapConfigFactory implements ObjectFactory {

	/** Log4j logger. */
	private static final Logger LOGGER = Logger.getLogger(LdapConfigFactory.class);

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
		LdapConfig config = new LdapConfig();

		Reference ref = (Reference) obj;
		Enumeration<RefAddr> addrs = ref.getAll();
		while (addrs.hasMoreElements()) {
			RefAddr addr = addrs.nextElement();

			if (addr.getType().equals("java.naming.provider.url")) {
				config.url = addr.getContent().toString();
			} else if (addr.getType().equals("java.naming.security.authentication")) {
				config.authentication = addr.getContent().toString();
			} else if (addr.getType().equals("java.naming.security.principal")) {
				config.principal = addr.getContent().toString();
			} else if (addr.getType().equals("java.naming.security.credentials")) {
				config.credentials = addr.getContent().toString();
			} else if (addr.getType().startsWith("ldap.")) {
				String attr = addr.getType().substring(5); // Remove "ldap."
				try {
					Field f = config.getClass().getDeclaredField(attr);
					f.set(config, addr.getContent().toString());
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
		}

		return config;
	}

}
