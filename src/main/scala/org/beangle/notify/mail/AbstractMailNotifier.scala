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

import org.beangle.notify.{Message, Notifier, NotifyLogger, SendingObserver}

/** 将通用 `Message` 转为 `MailMessage` 并委托 `mailSender` 投递；子类实现主题/正文拼装。 */
abstract class AbstractMailNotifier(val mailSender: MailSender, val from: String) extends Notifier:

  override def getType: String = "mail"

  /** 单条投递，内部转为列表调用批量接口。 */
  override def deliver(message: Message, observer: SendingObserver): Unit =
    deliver(List(message), observer)

  /** 仅处理 `MailMessage`；其它类型记警告。未设发件人时用构造参数 `from`。 */
  override def deliver(messages: Iterable[Message], observer: SendingObserver): Unit =
    messages foreach {
      case msg: MailMessage =>
        if (null == msg.from && null != from)
          msg.from(from)
        mailSender.send(msg, observer)
      case _ => NotifyLogger.warn("Mail Notifier should deliver mail message only")
    }

  /** 由子类从业务消息生成邮件主题。 */
  protected def buildSubject(msg: Message): String

  /** 由子类从业务消息生成邮件正文。 */
  protected def buildText(msg: Message): String
