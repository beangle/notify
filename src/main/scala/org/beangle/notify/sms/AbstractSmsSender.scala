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

import org.beangle.commons.bean.Initializing

/** 使用 appId、appSecret 与 HTTP 根路径（endpoint）的短信发送器公共配置。
 *
 * `endpoint` 表示网关根 URL（不含末尾具体接口路径），由各实现自行拼接路径或查询串。
 */
abstract class AbstractSmsSender(protected val endpoint: String,
                                 protected val appId: String,
                                 protected val appSecret: String) extends SmsSender, Initializing {

  override def init(): Unit = {}
}
