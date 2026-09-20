package com.zoti321.c2cmarket.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri

object GeoMapIntents {
    fun buildViewUri(query: String): Uri =
        Uri.parse("geo:0,0?q=${Uri.encode(query)}")

    fun canOpen(context: Context, query: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, buildViewUri(query))
        return intent.resolveActivity(context.packageManager) != null
    }

    fun open(context: Context, query: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, buildViewUri(query))
        if (intent.resolveActivity(context.packageManager) == null) return false
        context.startActivity(intent)
        return true
    }
}
