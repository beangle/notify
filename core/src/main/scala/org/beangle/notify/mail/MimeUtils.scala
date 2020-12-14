/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.notify.mail

import scala.collection.mutable
import javax.mail.internet.InternetAddress
import org.beangle.commons.lang.Strings

object MimeUtils {

  def parseAddress(address: String, encoding: String): List[InternetAddress] = {
    if (Strings.isEmpty(address)) List.empty
    try {
      var parsed = InternetAddress.parse(address)
      var returned = new mutable.ArrayBuffer[InternetAddress]
      parsed.foreach(raw => returned.addOne(if (encoding != null) new InternetAddress(raw.getAddress(), raw.getPersonal(), encoding) else raw))
      returned.toList
    } catch {
      case ex: Exception =>
        throw new RuntimeException("Failed to parse embedded personal name to correct encoding", ex)
        List.empty
    }
  }
}
