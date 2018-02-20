package is.meniga.mobileandroidstarter.adapter.viewmodel;

import com.meniga.sdk.models.transactions.MenigaTransaction;

import java.text.NumberFormat;

/**
 * Copyright 2017 Meniga Iceland Inc.
 * Created by agustk on 20.2.2018.
 */
public class Transaction extends BaseListObject {
    private MenigaTransaction data;

    public Transaction(MenigaTransaction data) {
        this.data = data;
    }

    public String getLabel() {
        return data.getText();
    }

    public String getDate() {
        return data.getDate().toString("dd.MM.YYYY");
    }

    public String getAmount() {
        NumberFormat formatter = data.getCurrency() == null ? NumberFormat.getCurrencyInstance() : NumberFormat.getInstance();
        if (data.getCurrency() == null) {
            return formatter.format(data.getAmount().getBigDecimal());
        } else {
            return formatter.format(data.getAmount().getBigDecimal()) + " " + data.getCurrency();
        }
    }
}
