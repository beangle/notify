
import de.heikoseeberger.sbtheader.HeaderPlugin
import sbt._

object CopyrightHeader extends AutoPlugin {
  import HeaderPlugin.autoImport._

  override def requires = HeaderPlugin
  override def trigger  = allRequirements

  override def projectSettings =
    Seq(
      headerEmptyLine := false,
      headerLicense := Some(HeaderLicense.Custom(
      """|Beangle, Agile Development Scaffold and Toolkits.
         |
         |Copyright © 2005, The Beangle Software.
         |
         |This program is free software: you can redistribute it and/or modify
         |it under the terms of the GNU Lesser General Public License as published by
         |the Free Software Foundation, either version 3 of the License, or
         |(at your option) any later version.
         |
         |This program is distributed in the hope that it will be useful,
         |but WITHOUT ANY WARRANTY; without even the implied warranty of
         |MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
         |GNU Lesser General Public License for more details.
         |
         |You should have received a copy of the GNU Lesser General Public License
         |along with this program.If not, see <http://www.gnu.org/licenses/>.
         |""".stripMargin
      ))
    )

}