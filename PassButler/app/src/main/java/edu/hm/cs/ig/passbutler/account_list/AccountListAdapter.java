package edu.hm.cs.ig.passbutler.account_list;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.account_detail.AccountDetailActivity;
import edu.hm.cs.ig.passbutler.data.AccountItemHandler;
import edu.hm.cs.ig.passbutler.data.AccountListHandler;
import edu.hm.cs.ig.passbutler.security.KeyHolder;
import edu.hm.cs.ig.passbutler.security.MissingKeyException;
import edu.hm.cs.ig.passbutler.util.DateUtil;
import edu.hm.cs.ig.passbutler.util.NavigationUtil;

/**
 * Created by dennis on 15.11.17.
 */

public class AccountListAdapter extends RecyclerView.Adapter<AccountListAdapter.AccountListAdapterViewHolder> {

    private static final String TAG = AccountListAdapter.class.getName();
    private final Context context;
    private final AccountListAdapterOnClickHandler clickHandler;
    private final AccountListAdapterOnMenuItemClickHandler menuItemClickHandler;
    private AccountListHandler accountListHandler;
    private ArrayList<String> passwordRepetitionAccounts;

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
        String accountName = accountListHandler.getAccountName(context, position);
        AccountItemHandler account = accountListHandler.getAccount(context, accountName);
        holder.accountNameTextView.setText(accountName);

        if (account.attributeExists(context, context.getString(R.string.account_attribute_password_key))) {
            Date lastModified = null;
            try {
                lastModified = accountListHandler.getAccount(context, accountName)
                        .getAttributeLastModified(context, context.getString(R.string.account_attribute_password_key));
            } catch (MissingKeyException e) {
                Toast.makeText(context, context.getString(R.string.missing_key_error_msg), Toast.LENGTH_SHORT).show();
                Log.wtf(TAG, "Could not check password for too high age because " + KeyHolder.class.getSimpleName() + " contains no key for decryption.");
                NavigationUtil.goToUnlockActivity(context);
                return;
            }
            int daysBetween = DateUtil.absoluteDayDif(lastModified, new Date());
            // TODO: replace with settings value
            if (daysBetween >= 1) {
                holder.accountTimerImageView.setVisibility(View.VISIBLE);
            } else {
                holder.accountTimerImageView.setVisibility(View.GONE);
            }
        }

        if (passwordRepetitionAccounts.contains(accountName)) {
            holder.accountRepetitionImageView.setVisibility(View.VISIBLE);
        } else {
            holder.accountRepetitionImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (accountListHandler == null) {
            return 0;
        }
        return accountListHandler.getAccountCount(context);
    }

    public void setAccountListHandler(AccountListHandler accountListHandler) {
        this.accountListHandler = accountListHandler;
        notifyDataSetChanged();
    }

    public void checkPasswordDuplicates() {
        ArrayList<String> dupAccountList = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();
        String pwKey = context.getString(R.string.account_attribute_password_key);

        for (AccountItemHandler handler : accountListHandler.getAccounts(context)) {
            if (handler.attributeExists(context, pwKey)) {
                try {
                    map.put(handler.getAccountName(context),
                            handler.getAttributeValue(context, pwKey));
                } catch (MissingKeyException e) {
                    Toast.makeText(context, context.getString(R.string.missing_key_error_msg), Toast.LENGTH_SHORT).show();
                    Log.wtf(TAG, "Could not check passwords for duplicates because " + KeyHolder.class.getSimpleName() + " contains no key for decryption.");
                    NavigationUtil.goToUnlockActivity(context);
                    return;
                }
            }
        }

        for (Map.Entry<String, String> pair : map.entrySet()) {
            String accountName = pair.getKey();
            String pw = pair.getValue();
            int dupCount = 0;

            for (Map.Entry<String, String> innerPair : map.entrySet()) {
                if (innerPair.getValue().equals(pw)) {
                    dupCount++;
                }
            }

            if (dupCount >= 2) {
                dupAccountList.add(accountName);
            }
        }
        passwordRepetitionAccounts = dupAccountList;
    }

    public class AccountListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        public final TextView accountNameTextView;
        private final ImageView accountTimerImageView;
        private final ImageView accountRepetitionImageView;

        public AccountListAdapterViewHolder(View view) {
            super(view);
            accountNameTextView = view.findViewById(R.id.account_list_item_text_view);
            ImageButton imageButton = view.findViewById(R.id.account_list_item_image_button);
            imageButton.setOnClickListener(this);
            view.setOnClickListener(this);
            accountTimerImageView = view.findViewById(R.id.account_list_item_timer_img);
            accountRepetitionImageView = view.findViewById(R.id.account_list_item_repetition_img);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.account_list_item_card_view) {
                final String accountName = accountListHandler.getAccountName(context, getAdapterPosition());
                clickHandler.onClick(v, accountName);
            } else if (v.getId() == R.id.account_list_item_image_button) {
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
