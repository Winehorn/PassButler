package edu.hm.cs.ig.passbutler.gui;

import android.content.Context;
import android.graphics.Rect;

/**
 * Created by dennis on 17.11.17.
 */

public class InstantAutoCompleteTextView extends android.support.v7.widget.AppCompatAutoCompleteTextView {

    public InstantAutoCompleteTextView(Context context) {
        super(context);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && getAdapter() != null) {
            showDropDown();
        }
    }
}
