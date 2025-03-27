package com.subefu.besfut.models

import com.subefu.besfut.db.DbStorageItem
import com.subefu.besfut.db.ReceiveInfoItem

data class ModelGroup<T: ReceiveInfoItem>(val name: String, val listItems: List<T>)

data class ModelGroupStorage(val name: String, val listItems: List<DbStorageItem>)