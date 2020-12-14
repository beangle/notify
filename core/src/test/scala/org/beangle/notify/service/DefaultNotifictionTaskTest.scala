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

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetupTest
import org.beangle.notify.mail.JavaMailSender
import org.beangle.notify.mail.DefaultMailNotifier
import org.beangle.notify.mail.MailMessage
import javax.mail.MessagingException
import org.beangle.commons.lang.Throwables

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class DefaultNotifictionTaskTest extends AnyFunSpec with Matchers {
  private val greenMail = new GreenMail(ServerSetupTest.ALL)
  greenMail.start()
  greenMail.setUser("test1@localhost", "user1", "password")
  greenMail.setUser("test2@localhost", "user2", "password")

  describe("JavaMailSender") {
    it("testMail") {
      try {
        val mailSender = new JavaMailSender()
        mailSender.host = "localhost"
        mailSender.username= "user1"
        mailSender.password = "password"
        mailSender.port = 3025

        val notifier = new DefaultMailNotifier(mailSender,"测试name<user1@localhost>")
        val task = new DefaultNotificationTask(notifier)
        val mmc = new MailMessage("测试", "测试简单邮件发送机制", "user2@localhost")
        task.queue.addMessage(mmc)
        task.send()
        val msgs = greenMail.getReceivedMessages()
        msgs.length should be(1)
        greenMail.stop()
      } catch {
        case e: MessagingException => Throwables.propagate(e)
      }
    }
  }

}
