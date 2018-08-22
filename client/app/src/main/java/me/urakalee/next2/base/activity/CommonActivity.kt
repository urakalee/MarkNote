package me.urakalee.next2.base.activity

import android.os.Bundle
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

/**
 * @author Uraka.Lee
 */
abstract class CommonActivity : BaseActivity() {

    protected abstract val layoutResId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fabric.with(this, Crashlytics())

        if (layoutResId <= 0) {
            throw AssertionError("Subclass must provide a valid layout resource id")
        }

        beforeSetContentView()

        setContentView(layoutResId)

        doCreateView(savedInstanceState)
    }

    protected fun beforeSetContentView() {}

    protected abstract fun doCreateView(savedInstanceState: Bundle?)
}
