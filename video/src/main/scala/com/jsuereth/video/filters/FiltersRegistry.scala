package com.jsuereth.video.filters

import com.jsuereth.video.Filter

import scala.collection.mutable

/**
  * Created by michael on 27.12.16.
  */
object FiltersRegistry {

  case class FilterMeta(key: String, name: String, description: String)

  private val filters = new mutable.HashMap[String, (String, String, Filter)]()

  def register(key: String, name: String, description: String, filter: Filter): Unit = {
    filters += key -> (name, description, filter)
  }

  def apply(): Seq[FilterMeta] = filters.map(en => FilterMeta(en._1, en._2._1, en._2._2)).toSeq

  def apply(key: String): Filter = filters(key)._3
}
