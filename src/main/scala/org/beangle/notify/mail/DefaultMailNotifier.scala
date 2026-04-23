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

import org.beangle.notify.Message

/** `AbstractMailNotifier` 的默认实现：主题与正文直接取自 `Message`。 */
class DefaultMailNotifier(mailSender: MailSender, from: String) extends AbstractMailNotifier(mailSender, from):
  protected def buildSubject(context: Message): String = context.subject
  protected def buildText(context: Message): String = context.text
