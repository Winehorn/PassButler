package edu.hm.cs.ig.passbutler;

import android.content.Context;
import android.content.res.Resources;
import android.database.CursorIndexOutOfBoundsException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.hm.cs.ig.passbutler.password.PassphraseDB;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PassphraseDBInstrumentedTest {
    @Test
    public void getWordById() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        PassphraseDB db = new PassphraseDB(appContext);

        boolean cursorIndexOutOfBoundsThrown = false;

        String word1 = db.getWordById(41645);
        assertEquals("napped", word1);
        assertNotEquals("nap", word1);

        String word2 = db.getWordById(36231);
        assertEquals("linoleum", word2);
        assertNotEquals("nap", word2);

        try{
            String word3 = db.getWordById(362318);
        } catch (CursorIndexOutOfBoundsException e) {
            cursorIndexOutOfBoundsThrown = true;
        }

        assertTrue(cursorIndexOutOfBoundsThrown);

    }
}
