package edu.hm.cs.ig.passbutler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by dennis on 15.11.17.
 */

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountAdapterViewHolder> {

    private static final String TAG = AccountAdapter.class.getName();
    private AccountHandler accountHandler;
    private Context context;

    public AccountAdapter(Context context) {
        this.context = context;
    }

    @Override
    public AccountAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.account_list_item, parent, false);
        return new AccountAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AccountAdapterViewHolder holder, int position) {
        holder.accountNameTextView.setText(accountHandler.getAccountName(context, position));
    }

    @Override
    public int getItemCount() {
        if(accountHandler == null) {
            return 0;
        }
        return accountHandler.getAccountCount(context);
    }

    public void setAccountHandler(AccountHandler accountHandler) {
        this.accountHandler = accountHandler;
        notifyDataSetChanged();
    }

    public class AccountAdapterViewHolder extends RecyclerView.ViewHolder {

        public final TextView accountNameTextView;

        public AccountAdapterViewHolder(View view) {
            super(view);
            accountNameTextView = (TextView) view.findViewById(R.id.account_list_item_text_view);
        }
    }
}
