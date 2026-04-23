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

import org.beangle.notify.SendingObserver

/** 邮件发送抽象：由 `JavaMailSender` 等实现，通过 `SendingObserver` 汇报进度与错误。 */
trait MailSender:
  /** 发送单封邮件。 */
  def send(message: MailMessage, observer: SendingObserver): Unit

  /** 批量发送；实现可优化为共用连接等。 */
  def send(message: Iterable[MailMessage], observer: SendingObserver): Unit
