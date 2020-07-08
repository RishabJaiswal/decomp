package com.decomp.comp.decomp.application

import androidx.multidex.MultiDexApplication

class DeCompApplicaton : MultiDexApplication() {

    init {
        instance = this
    }

    companion object {
        private lateinit var instance: DeCompApplicaton

        fun getInstance(): DeCompApplicaton {
            return instance
        }
    }
}