/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.notify.sms.vendor

import org.beangle.commons.lang.{Charsets, Strings}
import org.beangle.commons.logging.Logging
import org.beangle.commons.net.Networks
import org.beangle.commons.net.http.{HttpMethods, HttpUtils, Request}
import org.beangle.notify.sms.{Receiver, SmsResponse, SmsSender}

import java.io.OutputStreamWriter
import java.net.{URI, URLEncoder}
import java.nio.charset.Charset
import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.time.{Duration, Instant}

class EcuplSmsSender extends SmsSender, Logging {

  var base: String = _
  var appId: String = _
  var appSecret: String = _
  var tokenInfo: (String, Instant) = _
  var tokenLiveTime = 600 //600s

  def fetchToken(): Option[String] = {
    val now = Instant.now
    if (null == tokenInfo || Math.abs(Duration.between(tokenInfo._2, now).get(ChronoUnit.SECONDS)) >= tokenLiveTime) {
      val tokenRes = HttpUtils.getText(s"${base}/msg/getThirdAPIToken?appId=${appId}&appPassword=${appSecret}")
      if (tokenRes.isOk && tokenRes.getText.contains("000000")) {
        val token = Strings.substringBetween(tokenRes.getText, "\"token\":\"", "\"")
        if (Strings.isNotEmpty(token)) {
          this.tokenInfo = (token, Instant.now)
          Some(token)
        } else None
      } else None
    } else {
      Some(tokenInfo._1)
    }
  }

  override def send(receiver: Receiver, contents: String): SmsResponse = {
    fetchToken() match
      case Some(token) =>
        val postUrl = s"${base}/message/sendMessageBySMSApi"
        val receiverContacts = List(receiver).map(x => s"{\"name\": \"${URLEncoder.encode(x.name, Charsets.UTF_8)}\",\"mobile\":\"${x.mobile}\"}").mkString(",")
        val formData = s"""token=${token}&msgContent=${URLEncoder.encode(contents, Charsets.UTF_8)}&receivers=[${receiverContacts}]"""
        val request = new Request(formData,"application/x-www-form-urlencoded")
        val res = HttpUtils.post(Networks.url(postUrl),request)
        if (res.isOk) {
          val restext = res.getText
          SmsResponse("OK", Strings.substringBetween(restext, "\"msgId\":\"", "\""))
        } else {
          logger.error("sms error:" + res.getText + "(receivers:" + receiver.toString() + " msg:" + contents + ")")
          SmsResponse("Failure", res.getText)
        }
      case None =>
        logger.error("Cannot get invoke token")
        SmsResponse("Failure", "Cannot get invoke token")
  }

}
