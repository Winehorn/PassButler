package edu.hm.cs.ig.passbutler.account_list;

import android.view.MenuItem;

/**
 * Created by dennis on 18.11.17.
 */

public interface AccountListAdapterOnMenuItemClickHandler {
    boolean onMenuItemClick(MenuItem item, String accountName);
}
