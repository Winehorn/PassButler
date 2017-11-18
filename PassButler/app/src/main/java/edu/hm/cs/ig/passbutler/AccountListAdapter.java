package edu.hm.cs.ig.passbutler;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.hm.cs.ig.passbutler.data.AccountListHandler;

/**
 * Created by dennis on 15.11.17.
 */

public class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.AccountListAdapterViewHolder> {

    private static final String TAG = AccountListAdapter.class.getName();
    private AccountListHandler accountListHandler;
    private final Context context;
    private final AccountListAdapterOnClickHandler clickHandler;

    public AccountListAdapter(Context context, AccountListAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
    }

    @Override
    public AccountListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.account_list_item, parent, false);
        return new AccountListAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AccountListAdapterViewHolder holder, int position) {
        holder.accountNameTextView.setText(accountListHandler.getAccountName(context, position));
    }

    @Override
    public int getItemCount() {
        if(accountListHandler == null) {
            return 0;
        }
        return accountListHandler.getAccountCount(context);
    }

    public void setAccountListHandler(AccountListHandler accountListHandler) {
        this.accountListHandler = accountListHandler;
        notifyDataSetChanged();
    }

    public class AccountListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView accountNameTextView;

        public AccountListAdapterViewHolder(View view) {
            super(view);
            accountNameTextView = (TextView) view.findViewById(R.id.account_list_item_text_view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String accountName = accountListHandler.getAccountName(context, adapterPosition);
            clickHandler.onClick(accountName);
        }
    }
}
