package edu.hm.cs.ig.passbutler;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import edu.hm.cs.ig.passbutler.data.AccountListHandler;

/**
 * Created by dennis on 15.11.17.
 */

public class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.AccountListAdapterViewHolder> {

    private static final String TAG = AccountListAdapter.class.getName();
    private AccountListHandler accountListHandler;
    private final Context context;
    private final AccountListAdapterOnClickHandler clickHandler;
    private final AccountListAdapterOnMenuItemClickHandler menuItemClickHandler;

    public AccountListAdapter(
            Context context,
            AccountListAdapterOnClickHandler clickHandler,
            AccountListAdapterOnMenuItemClickHandler menuItemClickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;
        this.menuItemClickHandler = menuItemClickHandler;
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

    public class AccountListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        public final TextView accountNameTextView;

        public AccountListAdapterViewHolder(View view) {
            super(view);
            accountNameTextView = view.findViewById(R.id.account_list_item_text_view);
            ImageButton imageButton = view.findViewById(R.id.account_list_item_image_button);
            imageButton.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.account_list_item_card_view) {
                final String accountName = accountListHandler.getAccountName(context, getAdapterPosition());
                clickHandler.onClick(v, accountName);
            }
            else if(v.getId() == R.id.account_list_item_image_button) {
                final PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.account_list_more_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final String accountName = accountListHandler.getAccountName(context, getAdapterPosition());
            return menuItemClickHandler.onMenuItemClick(item, accountName);
        }
    }
}
