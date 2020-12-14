/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.notify.service

import java.util.concurrent.LinkedBlockingQueue

import org.beangle.notify.{Message, MessageQueue}

class DefaultMessageQueue extends MessageQueue {

  private val queue = new LinkedBlockingQueue[Message]

  def addMessage(message: Message): Unit = {
    queue.add(message)
  }

  def addMessages(contexts: Iterable[Message]): Unit = {
    contexts foreach addMessage
  }

  def poll(): Message = queue.poll()

  def size(): Int = queue.size()
}
