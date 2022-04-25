package org.mifos.connector.ams.paygops.paygopsDTO;

public class PaygopsResponseDto {

    /*"transaction_id": "simon_nationalID_test2",
    "amount": "123",
    "sender_name": "Simon Test2 National ID",
    "sender_phone_number": "+25512345676",
    "sent_datetime": "2022-04-25T12:07:58.048847Z",
    "memo": "abd1",
    "wallet_operator": "",
    "country": "",
    "currency": "",
    "warnings": [],
    "reconciled": false
    "destination_type": "contract_repayment",
    "destination": "C391270014"
    */

    private String transaction_id;
    private String amount;
    private String sender_name;
    private String sender_phone_number;
    private String sent_datetime;
    private String memo;
    private String wallet_operator;
    private String country;
    private String currency;
    private String [] warnings = new String[1];
    private Boolean reconciled;
    private String destination_type;
    private String destination;


    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSender_name() {
        return sender_name;
    }

    public void setSender_name(String sender_name) {
        this.sender_name = sender_name;
    }

    public String getSender_phone_number() {
        return sender_phone_number;
    }

    public void setSender_phone_number(String sender_phone_number) {
        this.sender_phone_number = sender_phone_number;
    }

    public String getSent_datetime() {
        return sent_datetime;
    }

    public void setSent_datetime(String sent_datetime) {
        this.sent_datetime = sent_datetime;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getWallet_operator() {
        return wallet_operator;
    }

    public void setWallet_operator(String wallet_operator) {
        this.wallet_operator = wallet_operator;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String[] getWarnings() {
        return warnings;
    }

    public void setWarnings(String[] warnings) {
        this.warnings = warnings;
    }

    public Boolean getReconciled() {
        return reconciled;
    }

    public void setReconciled(Boolean reconciled) {
        this.reconciled = reconciled;
    }

    public String getDestination_type() {
        return destination_type;
    }

    public void setDestination_type(String destination_type) {
        this.destination_type = destination_type;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
