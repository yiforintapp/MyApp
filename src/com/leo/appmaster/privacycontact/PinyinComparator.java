
package com.leo.appmaster.privacycontact;

import java.util.Comparator;

import android.util.Log;

/**
 * @author
 */
public class PinyinComparator implements Comparator<ContactBean> {

    public int compare(ContactBean o1, ContactBean o2) {
        if (o1.getSortLetter().equals("#")) {
            return -1;
        } else if (o1.getSortLetter().equals("#")) {
            return 1;
        } else {
            return o1.getSortLetter().compareTo(o2.getSortLetter());
        }
    }

}
