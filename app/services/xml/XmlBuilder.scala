/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services.xml

import models.mapping.*
import play.api.libs.json.*
import services.json.JsonPathExtractor

import scala.xml.*

class XmlBuilder:

  def build(
             mapping: XmlMapping,
             json: JsValue
           ): Elem =

    Elem(
      null,
      mapping.root.name,
      new UnprefixedAttribute(
        "xmlns",
        mapping.root.namespace,
        Null
      ),
      TopScope,
      true,
      mapping.children.toSeq
        .flatMap {
          case (name, node) =>
            buildNode(name, node, json)
        }*
    )

  private def buildNode(
                         name: String,
                         node: MappingNode,
                         json: JsValue
                       ): Seq[Node] =

    node.repeat match

      case Some("$.accounts[*]") =>

        val accounts =
          (json \ "accounts")
            .asOpt[JsArray]
            .map(_.value.toSeq)
            .getOrElse(Seq.empty)

        accounts.map { account =>

          Elem(
            null,
            name,
            Null,
            TopScope,
            true,
            node.children.toSeq
              .flatMap {
                case (childName, childNode) =>
                  buildNode(
                    childName,
                    childNode,
                    account
                  )
              }*
          )
        }

      case _ =>

        node.path match

          case Some(path) =>

            JsonPathExtractor
              .read(json, path)
              .map { value =>

                Elem(
                  null,
                  name,
                  Null,
                  TopScope,
                  true,
                  Text(value)
                )
              }
              .toSeq

          case None =>

            Seq(
              Elem(
                null,
                name,
                Null,
                TopScope,
                true,
                node.children.toSeq
                  .flatMap {
                    case (childName, childNode) =>
                      buildNode(
                        childName,
                        childNode,
                        json
                      )
                  }*
              )
            )