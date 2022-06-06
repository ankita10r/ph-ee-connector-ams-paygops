package org.mifos.connector.ams.paygops.zeebe;

public class ZeebeVariables {

    private ZeebeVariables() {
    }

    public static final String TRANSACTION_ID = "transactionId";
    public static final String PARTY_LOOKUP_FAILED = "partyLookupFailed";
    public static final String TRANSFER_SETTLEMENT_FAILED = "transferSettlementFailed";
    public static final String SERVER_TRANSACTION_RECEIPT_NUMBER = "mpesaReceiptNumber";
    public static final String AMS_REQUEST = "amsRequest";
}
