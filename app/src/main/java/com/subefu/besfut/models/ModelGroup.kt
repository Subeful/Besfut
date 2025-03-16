package com.subefu.besfut.models

import com.subefu.besfut.db.DbStorageItem

data class ModelGroup<T>(val name: String, val listItems: List<T>)
data class ModelGroupStorage(val name: String, val listItems: List<DbStorageItem>)