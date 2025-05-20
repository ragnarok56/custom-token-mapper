package com.example.mapper;

import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.ArrayList;
import java.util.List;

public class CustomTokenMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final Logger logger = Logger.getLogger(CustomTokenMapper.class);
    public static final String PROVIDER_ID = "custom-token-mapper";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, CustomTokenMapper.class);
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel,
            UserSessionModel userSession, KeycloakSession keycloakSession,
            ClientSessionContext clientSessionCtx) {

        MultivaluedMap<String, String> formParameters = keycloakSession.getContext()
                .getHttpRequest()
                .getDecodedFormParameters();

        String customValue = formParameters.getFirst("custom_value");

        if (customValue != null) {
            OIDCAttributeMapperHelper.mapClaim(token, mappingModel, customValue);
        }
    }
    
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }

    @Override
    public String getHelpText() {
        return "Adds custom value to the token.";
    }

    @Override
    public String getDisplayCategory() {
        return "Token mapper";
    }

    @Override
    public String getDisplayType() {
        return "Custom Value Token Mapper";
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}