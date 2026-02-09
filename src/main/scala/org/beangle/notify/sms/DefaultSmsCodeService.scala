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

import org.beangle.cache.caffeine.CaffeineCacheManager
import org.beangle.cache.redis.RedisCacheManager
import org.beangle.commons.cache.Cache
import org.beangle.commons.io.DefaultBinarySerializer
import org.beangle.commons.lang.Strings
import redis.clients.jedis.RedisClient

import java.util.regex.Pattern

/** 缺省的短信验证码服务
 */
class DefaultSmsCodeService extends SmsCodeService {
  var smsSender: SmsSender = _
  //验证码模板
  var defaultTemplate: String = "您的验证码为{code}，{ttl}分钟有效!"
  //默认五分钟有效
  var ttl: Int = 5 * 60
  //可选的redis缓存，否则采用进程内缓存
  var redisClient: Option[RedisClient] = None
  /** 缓存名称 */
  var cacheName: String = "sms_code"

  private val cache = buildCache()
  private val mobilePattern = Pattern.compile("^1(3[0-9]|4[01456879]|5[0-3,5-9]|6[2567]|7[0-8]|8[0-9]|9[0-3,5-9])\\d{8}$")

  override def send(receiver: Receiver): (Boolean, String) = {
    send(receiver, defaultTemplate)
  }

  override def send(receiver: Receiver, template: String): (Boolean, String) = {
    val templateStr = if Strings.isBlank(template) then defaultTemplate else template
    get(receiver.mobile) match
      case Some(code) => (true, "验证码已经发送")
      case None =>
        val code = generateCode()
        var contents = Strings.replace(templateStr, "{code}", code)
        contents = Strings.replace(contents, "{ttl}", ttlMinutes.toString)

        val res = smsSender.send(receiver, contents)
        if (res.isOk) {
          set(receiver.mobile, code)
          (true, s"验证码成功发送到${receiver.maskMobile}")
        } else {
          (false, "验证码发送失败:" + res.message)
        }
  }

  override def verify(mobile: String, code: String): Boolean = {
    val matched = get(mobile).contains(code)
    if matched then remove(mobile)
    matched
  }

  override def validate(mobile: String): Boolean = {
    mobilePattern.matcher(mobile).matches()
  }

  private def get(mobile: String): Option[String] = {
    cache.get(mobile)
  }

  private def set(mobile: String, verifyCode: String): Unit = {
    cache.put(mobile, verifyCode)
  }

  private def remove(mobile: String): Unit = {
    cache.evict(mobile)
  }

  override def ttlMinutes: Int = ttl / 60

  private def generateCode(): String = {
    DefaultSmsCodeService.generateDefaultCode()
  }

  protected def buildCache(): Cache[String, String] = {
    redisClient match {
      case None =>
        val cacheManager = new CaffeineCacheManager(true)
        cacheManager.ttl = ttl
        cacheManager.getCache(cacheName, classOf[String], classOf[String])
      case Some(client) =>
        val cacheManager = new RedisCacheManager(client, DefaultBinarySerializer, true)
        cacheManager.ttl = ttl
        cacheManager.getCache(cacheName, classOf[String], classOf[String])
    }
  }
}

object DefaultSmsCodeService {

  def generateDefaultCode(): String = {
    ((Math.random() * 9 + 1) * 100000).asInstanceOf[Int].toString
  }
}
