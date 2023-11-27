package com.example.witchesmeeting.extension

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


fun FragmentActivity.addFragment(
    @IdRes containerId: Int,
    fragment: Fragment?,
    addBackStack: Boolean = false
) {
    requireNotNull(fragment)

    val transaction = supportFragmentManager.beginTransaction()
    transaction.add(containerId, fragment).apply {
        if (addBackStack) addToBackStack(null)
    }
    transaction.commitAllowingStateLoss()
}