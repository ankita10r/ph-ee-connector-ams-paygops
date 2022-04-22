package org.mifos.connector.ams.paygops.camel.route;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.json.JSONObject;
import org.mifos.connector.ams.paygops.paygopsDTO.PaygopsRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.mifos.connector.ams.paygops.camel.config.CamelProperties.*;
import static org.mifos.connector.ams.paygops.camel.config.CamelProperties.AMS_REQUEST;
import static org.mifos.connector.ams.paygops.zeebe.ZeebeVariables.*;

@Component
public class PaygopsRouteBuilder extends RouteBuilder {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${paygops.base-url}")
    private String paygopsBaseUrl;

    @Value("${paygops.endpoint.verification}")
    private String verificationEndpoint;

    @Value("${paygops.endpoint.confirmation}")
    private String confirmationEndpoint;

    @Value("${paygops.auth-header}")
    private String accessToken;


    @Override
    public void configure() {

        from("rest:POST:/api/v1/payments/validate")
                .process(exchange -> {
                    JSONObject channelRequest = new JSONObject(exchange.getIn().getBody(String.class));
                    String transactionId = "123";
                    log.info(channelRequest.toString());
                    exchange.setProperty(CHANNEL_REQUEST, channelRequest);
                    exchange.setProperty(TRANSACTION_ID, transactionId);
                })
                .to("direct:transfer-validation-base");

        from("rest:POST:/api/paymentHub/Confirmation")
                .process(exchange -> {
                    JSONObject channelRequest = new JSONObject(exchange.getIn().getBody(String.class));
                    String transactionId = "123";
                    exchange.setProperty(CHANNEL_REQUEST, channelRequest);
                    exchange.setProperty(TRANSACTION_ID, transactionId);
                })
                .to("direct:transfer-settlement-base");

        from("direct:transfer-validation-base")
                .id("transfer-validation-base")
                .log(LoggingLevel.INFO, "## Starting Paygops Validation base route")
                .to("direct:transfer-validation")
                .choice()
                .when(header("CamelHttpResponseCode").isEqualTo("200"))
                .log(LoggingLevel.INFO, "Paygops Validation successful")
                .process(exchange -> {
                    // processing success case
                    exchange.setProperty(PARTY_LOOKUP_FAILED, false);
                })
                .otherwise()
                .log(LoggingLevel.ERROR, "Paygops Validation unsuccessful")
                .process(exchange -> {
                    // processing unsuccessful case
                    String body = exchange.getIn().getBody(String.class);
                    JSONObject jsonObject = new JSONObject(body);
                    Integer errorCode = jsonObject.getInt("error");
                    String errorDescription = jsonObject.getString("error_message");
                    exchange.setProperty(ERROR_CODE, errorCode);
                    exchange.setProperty(ERROR_INFORMATION, jsonObject.toString(1));
                    exchange.setProperty(ERROR_DESCRIPTION, errorDescription);
                    exchange.setProperty(PARTY_LOOKUP_FAILED, true);
                });

        from("direct:transfer-validation")
                .id("transfer-validation")
                .log(LoggingLevel.INFO, "## Starting Paygops Validation route")
                .log(LoggingLevel.INFO, "Bearer token is - " + accessToken)
                .removeHeader("*")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader("Authorization", simple("Bearer "+ accessToken))
                .setHeader("Content-Type", constant("application/json"))
                .setBody(exchange -> {
                    JSONObject channelRequest = (JSONObject) exchange.getProperty(CHANNEL_REQUEST);
                    logger.info(exchange.getProperty(CHANNEL_REQUEST).toString());
                    String transactionId = exchange.getProperty(TRANSACTION_ID, String.class);
                    PaygopsRequestDTO verificationRequestDTO = getPaygopsDtoFromChannelRequest(channelRequest,
                            transactionId);

                    logger.info("Validation request DTO: \n\n\n" + verificationRequestDTO);
                    return verificationRequestDTO;
                })
                .marshal().json(JsonLibrary.Jackson)
                .toD(getVerificationEndpoint() + "?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .log(LoggingLevel.INFO, "Paygops verification api response: \n\n..\n\n..\n\n.. ${body}");

        from("direct:transfer-settlement-base")
                .id("transfer-settlement-base")
                .log(LoggingLevel.INFO, "## Transfer Settlement route")
                .to("direct:transfer-settlement")
                .choice()
                .when(header("CamelHttpResponseCode").isEqualTo("200"))
                .log(LoggingLevel.INFO, "Settlement successful")
                .process(exchange -> {
                    // processing success case
                    String body = exchange.getIn().getBody(String.class);
                    JSONObject jsonObject = new JSONObject(body);
                    logger.info(jsonObject.toString());
                        exchange.setProperty(TRANSFER_SETTLEMENT_FAILED, false);

                })
                .otherwise()
                .log(LoggingLevel.ERROR, "Settlement unsuccessful")
                .process(exchange -> {
                    // processing unsuccessful case
                    String body = exchange.getIn().getBody(String.class);
                    JSONObject jsonObject = new JSONObject(body);
                    Integer errorCode = jsonObject.getInt("error");
                    String errorDescription = jsonObject.getString("error_message");
                    exchange.setProperty(ERROR_CODE, errorCode);
                    exchange.setProperty(ERROR_INFORMATION, jsonObject.toString(1));
                    exchange.setProperty(ERROR_DESCRIPTION, errorDescription);
                    exchange.setProperty(TRANSFER_SETTLEMENT_FAILED, true);
                });


        from("direct:transfer-settlement")
                .id("transfer-settlement")
                .log(LoggingLevel.INFO, "## Starting transfer settlement route")
                .removeHeader("*")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader("Authorization", simple("Bearer "+ accessToken))
                .setHeader("Content-Type", constant("application/json"))
                .setBody(exchange -> {

                    JSONObject channelRequest = (JSONObject) exchange.getProperty(CHANNEL_REQUEST);
                    String transactionId = exchange.getProperty(TRANSACTION_ID, String.class);
                    logger.info(exchange.getProperty(CHANNEL_REQUEST).toString());
                    PaygopsRequestDTO confirmationRequestDTO = getPaygopsDtoFromChannelRequest(channelRequest,
                            transactionId);

                    logger.info("Confirmation request DTO: \n\n\n" + confirmationRequestDTO);
                    exchange.setProperty(AMS_REQUEST,confirmationRequestDTO.toString());
                    return confirmationRequestDTO;
                })
                .marshal().json(JsonLibrary.Jackson)
                .toD(getConfirmationEndpoint() + "?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .log(LoggingLevel.INFO, "Paygops verification api response: \n ${body}");

    }

    // returns the complete URL for verification request
    private String getVerificationEndpoint() {
        return paygopsBaseUrl + verificationEndpoint;
    }

    // returns the complete URL for confirmation request
    private String getConfirmationEndpoint() {
        return paygopsBaseUrl + confirmationEndpoint;
    }

    private PaygopsRequestDTO getPaygopsDtoFromChannelRequest(JSONObject channelRequest, String transactionId) {
        PaygopsRequestDTO verificationRequestDTO = new PaygopsRequestDTO();

        String phoneNumber = channelRequest.getJSONObject("payer")
                .getJSONObject("partyIdInfo").getString("partyIdentifier");
        String memoId = channelRequest.getJSONObject("payee")
                .getJSONObject("partyIdInfo").getString("partyIdentifier"); // instead of account id this value corresponds to national id
        JSONObject amountJson = channelRequest.getJSONObject("amount");
        String operatorName = "MPESA";


        Long amount = amountJson.getLong("amount");
        String currency = amountJson.getString("currency");
        String country = "KE";

        verificationRequestDTO.setTransactionId(transactionId);
        verificationRequestDTO.setAmount(amount);
        verificationRequestDTO.setPhoneNumber(phoneNumber);
        verificationRequestDTO.setCurrency(currency);
        verificationRequestDTO.setOperator(operatorName);
        verificationRequestDTO.setMemo(memoId);
        verificationRequestDTO.setCountry(country);
        verificationRequestDTO.setWalletName(phoneNumber);

        return verificationRequestDTO;
    }
}
