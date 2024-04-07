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

package org.beangle.notify.sms

trait SmsSender {

  def send(receiver: Receiver, contents: String): SmsResponse
}

case class SmsResponse(code: String, message: String) {
  def isOk: Boolean = code == "OK"
}

case class Receiver(mobile: String, name: String) {
  def maskMobile: String = {
    if mobile.length == 11 then mobile.substring(0, 3) + "****" + mobile.substring(7, 11) else mobile
  }
}
