package com.tunjid.androidx.uidrivers

import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Transition
import android.transition.TransitionSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.tunjid.androidx.navigation.MultiStackNavigator
import com.tunjid.androidx.navigation.Navigator

const val BACKGROUND_TINT_DURATION = 1200L

fun FragmentTransaction.crossFade() = setCustomAnimations(
        android.R.anim.fade_in,
        android.R.anim.fade_out,
        android.R.anim.fade_in,
        android.R.anim.fade_out
)

fun MultiStackNavigator.materialFadeThroughTransition(): FragmentTransaction.(Int) -> Unit = fade@{ index ->
    val rootFragmentManager = current?.activity?.supportFragmentManager ?: return@fade

    rootFragmentManager.findFragmentByTag(activeIndex.toString())?.apply {
        enterTransition = null
        if (exitTransition !is MaterialFadeThrough) exitTransition = MaterialFadeThrough.create().setDuration(300)
    }
    rootFragmentManager.findFragmentByTag(index.toString())?.apply {
        exitTransition = null
        if (enterTransition !is MaterialFadeThrough) enterTransition = MaterialFadeThrough.create().setDuration(300)
    }
}

fun MultiStackNavigator.materialDepthAxisTransition(): FragmentTransaction.(Fragment) -> Unit = modifier@{ incomingFragment ->
    val current = current ?: return@modifier
    if (current is Navigator.TransactionModifier) current.augmentTransaction(this, incomingFragment)
    else {
        current.apply {
            enterTransition = MaterialSharedAxis.create(MaterialSharedAxis.Z, false)
            exitTransition = MaterialSharedAxis.create(MaterialSharedAxis.Z, true)
        }
        incomingFragment.apply {
            enterTransition = MaterialSharedAxis.create(MaterialSharedAxis.Z, true)
            exitTransition = MaterialSharedAxis.create(MaterialSharedAxis.Z, false)
        }
    }
}

fun baseSharedTransition(
        initial: UiState? = null
): Transition = TransitionSet()
        .setOrdering(TransitionSet.ORDERING_TOGETHER)
        .addTransition(ChangeImageTransform())
        .addTransition(ChangeTransform())
        .addTransition(UiStateAwareChangeBounds(initial))
        .setDuration(GlobalUiDriver.ANIMATION_DURATION.toLong())
