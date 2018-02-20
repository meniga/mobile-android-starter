package is.meniga.mobileandroidstarter.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import is.meniga.mobileandroidstarter.adapter.viewmodel.BaseListObject;

/**
 * Copyright 2017 Meniga Iceland Inc.
 * Created by agustk on 20.2.2018.
 */
public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {
    protected View root;
    protected T data;

    public BaseViewHolder(View itemView) {
        super(itemView);
        root = itemView;
    }

    public abstract void setData(T data);
}
