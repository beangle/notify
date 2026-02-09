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

import org.beangle.commons.codec.digest.Digests
import org.beangle.commons.lang.Charsets
import org.beangle.commons.net.http.HttpUtils
import org.beangle.notify.sms.{Receiver, SmsResponse, SmsSender}

import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** B2m发送实现
 */
class B2mSmsSender extends SmsSender {

  var appId: String = _
  var appSecret: String = _
  var url: String = _

  override def send(receiver: Receiver, contents: String): SmsResponse = {
    val sendTime = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
    val sign = Digests.md5Hex(appId + appSecret + sendTime)
    val encodedContent = URLEncoder.encode(contents, Charsets.UTF_8)
    val sendUrl = s"${url}?appId=${appId}&timestamp=${sendTime}&sign=${sign}&mobiles=${receiver.mobile}&content=${encodedContent}"
    val res = HttpUtils.get(sendUrl)
    if (res.isOk && res.getText.contains("SUCCESS")) {
      SmsResponse("OK", "发送成功")
    } else {
      SmsResponse("Failure", res.getText)
    }
  }
}
