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

/** 短信验证码发送服务
 */
trait SmsCodeService {

  def send(receiver: Receiver, template: String): (Boolean, String)

  def send(receiver: Receiver): (Boolean, String)

  /** 验证验证码
   * @param mobile
   * @param code
   * @param destroy 验证成功后是否销毁，默认验证失败不销毁
   * @return
   */
  def verify(mobile: String, code: String, destroy: Boolean): Boolean

  def validate(mobile: String): Boolean

  def ttlMinutes: Int
}
