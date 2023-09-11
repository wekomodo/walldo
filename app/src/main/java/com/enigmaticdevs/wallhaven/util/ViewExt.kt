package com.enigmaticdevs.wallhaven.util

import android.content.Context
import android.widget.Toast

fun errorToast(context : Context){
      Toast.makeText(context,"Some error occurred",Toast.LENGTH_SHORT).show()
}

fun customToast(context : Context,message : String,){
    Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
}