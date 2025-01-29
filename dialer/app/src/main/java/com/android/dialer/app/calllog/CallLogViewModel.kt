package com.android.dialer.app.calllog

import androidx.lifecycle.ViewModel
import com.android.contacts.common.phonenumbercache.ContactInfo
import com.android.dialer.app.contactinfo.NumberWithCountryIso
import com.android.dialer.common.util.ExpirableCache

class CallLogViewModel : ViewModel() {
    val cache: ExpirableCache<NumberWithCountryIso, ContactInfo> by lazy {
        ExpirableCache.create(CONTACT_INFO_CACHE_SIZE)
    }

    companion object {
        private const val CONTACT_INFO_CACHE_SIZE = 100
    }
}
