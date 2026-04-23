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

import org.beangle.commons.lang.Consoles
import org.beangle.notify.{Message, SendingObserver}

import java.io.File
import scala.collection.mutable.ArrayBuffer

/** 交互式命令行调试 `MailSender`。由 `org.beangle.notify.Main` 调度。 */
object MailShell {

  private var conf: CliConfig = _
  private var draft: Draft = newDraft()

  /** 提示 SMTP 与发件人后进入 `mail>` 命令循环。 */
  def run(args: Array[String]): Unit = {
    val sender = createSender()

    println("Ready.")
    println("Commands:")
    println("  new")
    println("  to <addr1,addr2>")
    println("  subject <text>")
    println("  body")
    println("  body-file <path>")
    println("  attach <filePath>")
    println("  attach-list")
    println("  send")
    println("  show")
    println("  help")
    println("  quit")

    repl(sender)
  }

  private final class CliConfig(
    var host: String,
    var port: Int,
    var username: String,
    var password: String,
    var from: String
  )

  private def createSender(): JavaMailSender = {
    val host = Consoles.prompt("smtp host: ")
    val port = askPort()
    val username = Consoles.prompt("username(mail): ")
    val password = Consoles.prompt("password: ")
    val from = Consoles.prompt("from(default username): ", username)
    this.conf = new CliConfig(host, port, username, password, from)
    JavaMailSender.smtp(host, username, password, port)
  }

  private def askPort(): Int = {
    val raw = Consoles.prompt("smtp port(25/465/587): ", "465")
    raw.toIntOption match
      case Some(p) => p
      case None =>
        println("invalid port, fallback to 465")
        465
  }

  private final class Draft(
    var to: String,
    var subject: String,
    var body: String,
    var contentType: String,
    val attachments: ArrayBuffer[File]
  )

  private def newDraft(): Draft = {
    new Draft("", "", "", Message.TEXT, new ArrayBuffer[File])
  }

  private def repl(sender: JavaMailSender): Unit = {
    Consoles.shell("mail> ", Set("quit", "exit", "q")) { trimmed =>
      val lower = trimmed.toLowerCase
      if (lower == "help") {
        println("new")
        println("to <addr1,addr2>")
        println("subject <text>")
        println("body")
        println("body-file <path>")
        println("attach <filePath>")
        println("attach-list")
        println("send")
        println("show")
        println("quit")
      } else if (lower == "new") {
        draft = newDraft()
        println("draft reset")
      } else if (lower.startsWith("to ")) {
        draft.to = trimmed.substring(3).trim
        println(s"to=${draft.to}")
      } else if (lower.startsWith("subject ")) {
        draft.subject = trimmed.substring(8).trim
        println(s"subject=${draft.subject}")
      } else if (lower == "body") {
        val text = readMultiline()
        draft.body = text
        if (text.trim.startsWith("<!DOCTYPE html>") || text.trim.startsWith("<html")) draft.contentType = Message.HTML
        else draft.contentType = Message.TEXT
        println(s"body loaded, chars=${text.length}")
      } else if (lower.startsWith("body-file ")) {
        val path = trimmed.substring(10).trim
        loadBodyFromFile(path)
      } else if (lower.startsWith("attach ")) {
        val path = trimmed.substring(7).trim
        addAttachment(path)
      } else if (lower == "attach-list") {
        showAttachments()
      } else if (lower == "send") {
        sendDraft(sender)
      } else if (lower == "show") {
        showConfig()
      } else if (trimmed.nonEmpty) {
        println("unknown command, use help")
      }
    }
  }

  private def sendDraft(sender: JavaMailSender): Unit = {
    if (draft.to.isBlank) {
      println("missing to, use: to <addr1,addr2>")
      return
    }
    if (draft.subject.isBlank) {
      println("missing subject, use: subject <text>")
      return
    }
    if (draft.body.isBlank) {
      println("missing body, use: body or body-file <path>")
      return
    }
    val mail = new MailMessage(draft.subject, draft.body, draft.to)
    mail.from(conf.from)
    mail.contentType = draft.contentType
    draft.attachments.foreach(mail.addAttachment)
    sender.send(mail, ConsoleObserver)
  }

  private def readMultiline(): String = {
    println("enter mail body, finish with single line '.'")
    val lines = new ArrayBuffer[String]
    var continue = true
    while (continue) {
      val line = scala.io.StdIn.readLine()
      if (line == null || line == ".") continue = false
      else lines += line
    }
    lines.mkString(System.lineSeparator())
  }

  private def loadBodyFromFile(path: String): Unit = {
    val file = new File(path)
    if (!file.exists() || !file.isFile) {
      println(s"body-file not found: $path")
      return
    }
    val text = java.nio.file.Files.readString(file.toPath)
    draft.body = text
    if (text.trim.startsWith("<!DOCTYPE html>") || text.trim.startsWith("<html")) draft.contentType = Message.HTML
    else draft.contentType = Message.TEXT
    println(s"body loaded from file, chars=${text.length}")
  }

  private def addAttachment(path: String): Unit = {
    val file = new File(path)
    if (!file.exists() || !file.isFile) {
      println(s"attachment not found: $path")
      return
    }
    draft.attachments += file
    println(s"attachment added: ${file.getName}")
  }

  private def showAttachments(): Unit = {
    if (draft.attachments.isEmpty) {
      println("attachments: (none)")
    } else {
      println("attachments:")
      draft.attachments.zipWithIndex.foreach { (f, i) =>
        println(s"  [$i] ${f.getAbsolutePath}")
      }
    }
  }

  private object ConsoleObserver extends SendingObserver {
    override def onStart(msg: org.beangle.notify.Message): Unit = {
      println(s"start sending '${msg.subject}' to ${msg.recipients.mkString(",")}")
    }

    override def onFinish(msg: org.beangle.notify.Message): Unit = {
      println(s"done: ${msg.subject}")
    }

    override def onFail(e: Exception): Unit = {
      System.err.println(s"failure: ${e.getMessage}")
    }
  }

  private def showConfig(): Unit = {
    println(s"host=${conf.host}")
    println(s"port=${conf.port}")
    println(s"username=${conf.username}")
    println(s"password=${maskSecret(conf.password)}")
    println(s"from=${conf.from}")
    println(s"draft.to=${draft.to}")
    println(s"draft.subject=${draft.subject}")
    println(s"draft.bodyChars=${draft.body.length}")
    println(s"draft.contentType=${draft.contentType}")
    println(s"draft.attachments=${draft.attachments.size}")
  }

  private def maskSecret(secret: String): String = {
    val s = secret.trim
    if s.isEmpty then "(empty)"
    else if s.length <= 4 then "****"
    else s"${s.substring(0, 2)}****${s.substring(s.length - 2)}"
  }
}
