package com.subefu.besfut.utils

import android.view.View


interface BindViewHolder<T> {
    fun bind(view: View, item: T, position: Int, listener: (Int, Int) -> Unit)
}