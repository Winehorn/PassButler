package edu.hm.cs.ig.passbutler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.hm.cs.ig.passbutler.data.AccountItemHandler;

/**
 * Created by dennis on 16.11.17.
 */

public class AccountDetailAdapter extends RecyclerView.Adapter<AccountDetailAdapter.AccountDetailAdapterViewHolder> {

    private static final String TAG = AccountDetailAdapter.class.getName();
    private Context context;
    private AccountItemHandler accountItemHandler;

    public AccountDetailAdapter(Context context) {
        this.context = context;
    }

    @Override
    public AccountDetailAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.account_detail_item, parent, false);
        return new AccountDetailAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AccountDetailAdapterViewHolder holder, int position) {
        String key = accountItemHandler.getAttributeKey(context, position);
        String value = accountItemHandler.getAttributeValue(context, key);
        holder.accountAttributeKeyTextView.setText(key);
        holder.accountAttributeValueTextView.setText(value);
    }

    @Override
    public int getItemCount() {
        if(accountItemHandler == null) {
            return 0;
        }
        return accountItemHandler.getAttributeCount(context);
    }

    public void setAccountItemHandler(AccountItemHandler accountItemHandler) {
        this.accountItemHandler = accountItemHandler;
        notifyDataSetChanged();
    }

    public class AccountDetailAdapterViewHolder extends RecyclerView.ViewHolder {

        public final TextView accountAttributeKeyTextView;
        public final TextView accountAttributeValueTextView;

        public AccountDetailAdapterViewHolder(View view) {
            super(view);
            accountAttributeKeyTextView = (TextView) view.findViewById(R.id.account_attribute_key_text_view);
            accountAttributeValueTextView = (TextView) view.findViewById(R.id.account_attribute_value_text_view);
        }
    }
}
