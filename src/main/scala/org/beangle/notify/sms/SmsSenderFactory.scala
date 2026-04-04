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

import org.beangle.commons.bean.Properties
import org.beangle.commons.collection.Collections
import org.beangle.notify.sms.vendor.{B2mSmsSender, EcuplSmsSender, LixinSmsSender}

object SmsSenderFactory {
  val Vendor = "vendor"
  val EndPoint = "endpoint"
  val AppId = "appId"
  val AppSecret = "appSecret"

  def createSender(params: collection.Map[String, String]): SmsSender = {
    val ps = Collections.newMap[String, String]
    ps.addAll(params)
    val endpointOpt = ps.remove(EndPoint)
    val appIdOpt = ps.remove(AppId)
    val appSecretOpt = ps.remove(AppSecret)
    val vendorOpt = ps.remove(Vendor)
    require(vendorOpt.nonEmpty && endpointOpt.nonEmpty && appIdOpt.nonEmpty && appSecretOpt.nonEmpty)
    val vendor = vendorOpt.get
    val endpoint = endpointOpt.get
    val appId = appIdOpt.get
    val appSecret = appSecretOpt.get

    val sender = vendor match {
      case "lixin" => new LixinSmsSender(endpoint, appId, appSecret)
      case "b2m" => new B2mSmsSender(endpoint, appId, appSecret)
      case "ecupl" => new EcuplSmsSender(endpoint, appId, appSecret)
      case _ => throw new IllegalArgumentException(s"cannot find vendor for ${vendor}")
    }
    ps foreach { (k, v) =>
      Properties.copy(sender, k, v)
    }
    sender.init()
    sender
  }
}
