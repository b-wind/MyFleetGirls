package com.ponkotuy.data.master

import org.json4s._

/**
 *
 * @author kPherox
 * Date: 18/09/13
 */
case class MapData(
  areaId: Int, infoNo: Int, name: String, frameX: Int, frameY: Int, frameW: Int, frameH: Int, version: Int
)


object MapData {
  //def fromJson(json: JValue,  areaId: Int, infoNo: Int, version: Int): List[MapData]
}

case class CellPosition(
  areaId: Int, infoNo: Int, cell: Int, posX: Int, posY: Int, version: Int
)

object CellPosition {
  //def fromJson(json: JValue,  areaId: Int, infoNo: Int, version: Int): List[CellPosition]
}
