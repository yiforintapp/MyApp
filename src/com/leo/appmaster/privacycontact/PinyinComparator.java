
package com.leo.appmaster.privacycontact;

import java.util.Comparator;

/**
 * @author
 */
public class PinyinComparator implements Comparator<ContactBean> {

    public int compare(ContactBean o1, ContactBean o2) {
        return o1.getSortLetter().compareTo(o2.getSortLetter());
    }

}
