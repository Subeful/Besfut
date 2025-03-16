package com.subefu.besfut.models

import com.subefu.besfut.db.DbItem
import com.subefu.besfut.db.DbStorageItem

data class ModelGroup(val name: String, val listItems: List<DbItem>)
data class ModelGroupStorage(val name: String, val listItems: List<DbStorageItem>)