package edu.hm.cs.ig.passbutler.account_detail;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

import java.text.SimpleDateFormat;

import edu.hm.cs.ig.passbutler.R;
import edu.hm.cs.ig.passbutler.data.AccountItemHandler;
import edu.hm.cs.ig.passbutler.security.KeyHolder;
import edu.hm.cs.ig.passbutler.security.MissingKeyException;
import edu.hm.cs.ig.passbutler.util.NavigationUtil;
import edu.hm.cs.ig.passbutler.util.PasswordUtil;

/**
 * Created by dennis on 16.11.17.
 */

public class AccountDetailAdapter extends RecyclerView.Adapter<AccountDetailAdapter.AccountDetailAdapterViewHolder> {

    private final String TAG = AccountDetailAdapter.class.getName();
    private Context context;
    private AccountItemHandler accountItemHandler;
    private final AccountDetailAdapterOnMenuItemClickHandler menuItemClickHandler;

    public AccountDetailAdapter(Context context, AccountDetailAdapterOnMenuItemClickHandler menuItemClickHandler) {
        this.context = context;
        this.menuItemClickHandler = menuItemClickHandler;
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
        try {
            String key = accountItemHandler.getAttributeKey(context, position);
            String value = accountItemHandler.getAttributeValue(context, key);
            String lastModified = new SimpleDateFormat(context.getString(R.string.date_format))
                    .format(accountItemHandler.getAttributeLastModified(context, key));
            holder.accountAttributeKeyTextView.setText(key);
            holder.accountAttributeValueTextView.setText(value);
            holder.accountAttributeLastModifiedTextView.setText(lastModified);
            if(key.equals(context.getString(R.string.account_attribute_password_key))) {
                int strength = PasswordUtil.checkPassword(value);
                switch (strength) {
                    case 0:
                    case 1:
                        holder.accountAttributeLockImageView.setImageResource(R.drawable.ic_info_pw_red);
                        break;
                    case 2:
                    case 3:
                        holder.accountAttributeLockImageView.setImageResource(R.drawable.ic_info_pw_yellow);
                        break;
                    case 4:
                        holder.accountAttributeLockImageView.setImageResource(R.drawable.ic_info_pw_green);
                }
                holder.accountAttributeLockImageView.setVisibility(View.VISIBLE);
            } else {
                holder.accountAttributeLockImageView.setVisibility(View.INVISIBLE);
            }
        } catch (MissingKeyException e) {
            Toast.makeText(context, context.getString(R.string.missing_key_error_msg), Toast.LENGTH_SHORT).show();
            Log.wtf(TAG, "Could not bind view holder because " + KeyHolder.class.getSimpleName() + " contains no key for decryption.");
            NavigationUtil.goToUnlockActivity(context);
            return;
        }
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

    public class AccountDetailAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        public final TextView accountAttributeKeyTextView;
        public final TextView accountAttributeValueTextView;
        public final TextView accountAttributeLastModifiedTextView;
        private final ImageView accountAttributeLockImageView;

        public AccountDetailAdapterViewHolder(View view) {
            super(view);
            accountAttributeKeyTextView = view.findViewById(R.id.account_attribute_key_text_view);
            accountAttributeValueTextView = view.findViewById(R.id.account_attribute_value_text_view);
            accountAttributeLastModifiedTextView = view.findViewById(R.id.account_attribute_last_modified_date_text_view);
            accountAttributeLockImageView = view.findViewById(R.id.account_detail_item_lock_img);
            ImageButton imageButton = view.findViewById(R.id.account_detail_item_image_button);
            imageButton.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.account_detail_item_image_button) {
                final PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.account_detail_more_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.show();
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final String attributeKey = accountItemHandler.getAttributeKey(context, getAdapterPosition());
            return menuItemClickHandler.onMenuItemClick(item, attributeKey);
        }
    }
}
