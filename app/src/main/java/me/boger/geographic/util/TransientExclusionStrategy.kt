package me.boger.geographic.util

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

/**
 * Created by BogerChan on 2017/7/12.
 */
object TransientExclusionStrategy : ExclusionStrategy {
    override fun shouldSkipClass(clazz: Class<*>?) = false

    override fun shouldSkipField(f: FieldAttributes)
            = f.getAnnotation(Transient::class.java) != null
            || f.name.endsWith("\$delegate")
}