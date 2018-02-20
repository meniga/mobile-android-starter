package is.meniga.mobileandroidstarter.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meniga.sdk.models.transactions.MenigaTransaction;

import java.util.ArrayList;
import java.util.List;

import is.meniga.mobileandroidstarter.R;
import is.meniga.mobileandroidstarter.adapter.viewholder.BaseViewHolder;
import is.meniga.mobileandroidstarter.adapter.viewholder.TransactionViewHolder;
import is.meniga.mobileandroidstarter.adapter.viewmodel.BaseListObject;
import is.meniga.mobileandroidstarter.adapter.viewmodel.Transaction;

/**
 * Copyright 2017 Meniga Iceland Inc.
 * Created by agustk on 20.2.2018.
 */
public class TransactionsAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List<BaseListObject> viewItems = new ArrayList<>();

    public TransactionsAdapter() {
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (TransactionListViewType.values()[viewType]) {
            case TRANSACTION:
                View transactionView = inflater.inflate(R.layout.item_transaction_list_item, parent, false);
                return new TransactionViewHolder(transactionView);

            default:
                // Return empty view if we don't know the view type
                View view = new View(parent.getContext());
                parent.addView(view);
                return new BaseViewHolder<BaseListObject>(view) {
                    @Override
                    public void setData(BaseListObject data) {
                    }
                };
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.setData(viewItems.get(position));
    }

    @Override
    public int getItemCount() {
        return viewItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        BaseListObject item = viewItems.get(position);
        if(item instanceof Transaction) {
            return TransactionListViewType.TRANSACTION.ordinal();
        }
        return -1;
    }

    public void addViewItems(List<MenigaTransaction> items) {
        for (MenigaTransaction item : items) {
            viewItems.add(new Transaction(item));
        }
    }
}
