package com.cgi.commons.rest.auth;

import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.http.client.direct.AnonymousClient;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;

public class SecurityConfigFactory implements ConfigFactory {
    public static final String JWT_SALT = "12345678901234567890123456789012"; // FIXME Generate this with random string

    @Override
    public Config build() {
        final JwtAuthenticator tokenAuthenticator = new JwtAuthenticator(JWT_SALT);
        HeaderClient jwtToken = new HeaderClient(tokenAuthenticator);
        jwtToken.setHeaderName("Authorization");
        jwtToken.setPrefixHeader("Bearer ");
        jwtToken.setName("must-be-connected");

        final Clients clients = new Clients(jwtToken, new AnonymousClient());

        final Config config = new Config(clients);
        config.addAuthorizer("must-be-anonymous", new IsAnonymousAuthorizer());
        config.addAuthorizer("must-be-connected", new IsAuthenticatedAuthorizer());
        return config;
    }
}
