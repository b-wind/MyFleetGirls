package com.ponkotuy.restype

import java.io.ByteArrayOutputStream

import com.ponkotuy.http.MFGHttp
import com.ponkotuy.parser.Query
import org.jboss.netty.buffer.ChannelBuffer

import scala.util.Try
import scala.util.matching.Regex

/**
 * @author ponkotuy
 * Date: 15/04/12.
 */
case object ShipSWF extends ResType {
  override def regexp: Regex = """\A/kcs/resources/swf/ships/[a-z]+\.swf\z""".r

  override def postables(q: Query): Seq[Result] = {
    parseKey(q.toString).filterNot(MFGHttp.existsImage).map { key =>
      val swf = allRead(q.response.getContent)
      FilePostable("/swf/ship/" + key, "image", 1, swf, "swf")
    }.toList
  }

  private def parseKey(str: String): Option[String] =
    Try {
      val filename = str.split('/').last
      filename.takeWhile(_ != '.')
    }.toOption

  def allRead(cb: ChannelBuffer): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    cb.getBytes(0, baos, cb.readableBytes())
    baos.toByteArray
  }
}
