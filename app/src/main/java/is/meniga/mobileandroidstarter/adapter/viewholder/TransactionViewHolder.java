package is.meniga.mobileandroidstarter.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import is.meniga.mobileandroidstarter.R;
import is.meniga.mobileandroidstarter.adapter.viewmodel.BaseListObject;
import is.meniga.mobileandroidstarter.adapter.viewmodel.Transaction;

/**
 * Copyright 2017 Meniga Iceland Inc.
 * Created by agustk on 20.2.2018.
 */
public class TransactionViewHolder extends BaseViewHolder<Transaction> {
    @BindView(R.id.transaction_cell_date)
    TextView date;
    @BindView(R.id.transaction_cell_text)
    TextView text;
    @BindView(R.id.transaction_cell_amount)
    TextView amount;

    public TransactionViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    @Override
    public void setData(Transaction data) {
        date.setText(data.getDate());
        text.setText(data.getLabel());
        amount.setText(data.getAmount());
    }
}
