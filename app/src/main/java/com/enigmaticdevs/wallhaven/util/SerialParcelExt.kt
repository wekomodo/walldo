package com.enigmaticdevs.wallhaven.util

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import java.io.Serializable

 fun <T : Serializable?> getSerializable(intent: Intent, name: String, clazz: Class<T>): T
{
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        intent.getSerializableExtra(name, clazz)!!
    else
        intent.getSerializableExtra(name) as T
}
 fun <T: Parcelable?> getParcelable(intent: Intent, name: String, clazz : Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        intent.getParcelableExtra(name, clazz)
    else
        intent.getParcelableExtra(name)
}