package com.example.mapper;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.oidc.mappers.*;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomTokenMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    private static final Logger logger = Logger.getLogger(CustomTokenMapper.class);
    public static final String PROVIDER_ID = "custom-token-mapper";
    private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    // Configuration property for the API endpoint URL
    public static final String API_ENDPOINT_URL_CONFIG = "api.endpoint.url";
    // Configuration property for the JSON field to extract
    public static final String JSON_FIELD_CONFIG = "json.field";

    static {
        // Standard claim name and token inclusion settings
        OIDCAttributeMapperHelper.addTokenClaimNameConfig(configProperties);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(configProperties, CustomTokenMapper.class);

        // Custom configuration property for the API Endpoint
        ProviderConfigProperty apiEndpointProperty = new ProviderConfigProperty();
        apiEndpointProperty.setName(API_ENDPOINT_URL_CONFIG);
        apiEndpointProperty.setLabel("API Endpoint URL");
        apiEndpointProperty.setType(ProviderConfigProperty.STRING_TYPE);
        apiEndpointProperty.setHelpText("URL of the external API to call for fetching claims. " +
                "You can use ${username} as a placeholder for the user's name.");
        configProperties.add(apiEndpointProperty);

        // Custom configuration property for the JSON field to extract
        ProviderConfigProperty jsonFieldProperty = new ProviderConfigProperty();
        jsonFieldProperty.setName(JSON_FIELD_CONFIG);
        jsonFieldProperty.setLabel("JSON Field");
        jsonFieldProperty.setType(ProviderConfigProperty.STRING_TYPE);
        jsonFieldProperty.setHelpText("The name of the field to extract from the JSON response (e.g., 'permissions.datasets'). Use dot notation for nested objects.");
        configProperties.add(jsonFieldProperty);
    }


    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel,
                            UserSessionModel userSession, KeycloakSession keycloakSession,
                            ClientSessionContext clientSessionCtx) {

        // Get the configured API URL and JSON field from the mapper settings

        String apiUrlTemplate = mappingModel.getConfig().get(API_ENDPOINT_URL_CONFIG);
        String jsonField = mappingModel.getConfig().get(JSON_FIELD_CONFIG);

        if (apiUrlTemplate == null || apiUrlTemplate.isEmpty() || jsonField == null || jsonField.isEmpty()) {
            logger.warn("API Endpoint URL or JSON Field is not configured for the custom token mapper.");
            return;
        }

        // always assign this as a multivalued claim otherwise keycloak gets upset
        // KC-SERVICES0046: Multiple values found '[source_a, source_b]' for protocol mapper 'sources' but expected just single value
        mappingModel.getConfig().put(ProtocolMapperUtils.MULTIVALUED, "true");

        // Replace placeholder with the actual username
        String username = userSession.getUser().getUsername();
        String apiUrl = apiUrlTemplate.replace("${username}", username);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);

            logger.infof("Calling external API for user '%s': %s", username, apiUrl);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    String jsonResponse = EntityUtils.toString(response.getEntity());

                    // Parse the JSON response into a Map
                    Map<String, Object> responseMap = mapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>(){});

                    // Extract the desired value from the map using dot notation
                    Object claimValue = extractValueFromJson(responseMap, jsonField);

                    if (claimValue != null) {
                        OIDCAttributeMapperHelper.mapClaim(token, mappingModel, claimValue);
                    } else {
                        logger.warnf("JSON field '%s' not found in API response for user '%s'.", jsonField, username);
                    }

                } else {
                    logger.errorf("Failed to call API for user '%s'. Status: %s",
                            username, response.getStatusLine().toString());
                }
            }
        } catch (IOException e) {
            logger.errorf(e, "Exception while calling external API for user '%s'", username);
        }
    }

    // Helper method to extract a value from a nested map using dot notation
    private Object extractValueFromJson(Map<String, Object> jsonMap, String fieldPath) {
        String[] keys = fieldPath.split("\\.");
        Object currentValue = jsonMap;
        for (String key : keys) {
            if (!(currentValue instanceof Map)) {
                return null;
            }
            currentValue = ((Map<?, ?>) currentValue).get(key);
            if (currentValue == null) {
                return null;
            }
        }
        return currentValue;
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