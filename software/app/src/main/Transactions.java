package com.christo.moneyplant.models.api;

import com.christo.moneyplant.models.transaction.TransactionInfo;

import java.util.ArrayList;

public class Transactions {

    public ArrayList<TransactionInfo> getTransactions() {
        return transactions;
    }

    ArrayList<TransactionInfo> transactions;

    public Transactions(ArrayList<TransactionInfo> transactions) {
        this.transactions = transactions;
    }
}
