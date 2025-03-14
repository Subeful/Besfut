package com.subefu.besfut.models

import com.subefu.besfut.db.DbItem

data class ModelGroup(val name: String, val listItems: List<DbItem>)