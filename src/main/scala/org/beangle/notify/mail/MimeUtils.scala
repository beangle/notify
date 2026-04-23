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

package org.beangle.notify.mail

import jakarta.mail.internet.InternetAddress
import org.beangle.commons.lang.Strings

import scala.collection.mutable

/** 邮件 MIME 相关小工具（地址解析等）。 */
object MimeUtils {

  /** 将逗号分隔的 RFC822 地址串解析为 `InternetAddress` 列表；空串返回空列表。`encoding` 非空时用于 personal 名编码。 */
  def parseAddress(address: String, encoding: String): List[InternetAddress] = {
    if (Strings.isEmpty(address)) List.empty
    else {
      try
        val parsed = InternetAddress.parse(address)
        val returned = new mutable.ArrayBuffer[InternetAddress]
        parsed.foreach(raw => returned.addOne(if (encoding != null) new InternetAddress(raw.getAddress, raw.getPersonal, encoding) else raw))
        returned.toList
      catch
        case ex: Exception => throw new RuntimeException("Failed to parse embedded personal name to correct encoding", ex)
    }
  }
}
