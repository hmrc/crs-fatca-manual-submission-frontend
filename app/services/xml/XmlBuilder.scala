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
      mapping.children.toSeq.flatMap {
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

      case Some(repeatPath) =>

        JsonPathExtractor
          .readArray(json, repeatPath)
          .flatMap { item =>

            val children =
              node.children.toSeq.flatMap {
                case (childName, childNode) =>
                  buildNode(
                    childName,
                    childNode,
                    item
                  )
              }

            if children.isEmpty then
              Seq.empty
            else
              Seq(
                Elem(
                  null,
                  name,
                  buildAttributes(node, item),
                  TopScope,
                  true,
                  children*
                )
              )
          }

      case None =>

        buildSingleNode(
          name,
          node,
          json
        )

  private def buildSingleNode(
                               name: String,
                               node: MappingNode,
                               json: JsValue
                             ): Seq[Node] =

    node.valuePath match

      case Some(valuePath) =>

        JsonPathExtractor
          .read(json, valuePath)
          .map { value =>

            Elem(
              null,
              name,
              buildAttributes(node, json),
              TopScope,
              true,
              Text(value)
            )

          }
          .toSeq

      case None =>

        node.path match

          case Some(path) =>

            JsonPathExtractor
              .read(json, path)
              .map { value =>

                Elem(
                  null,
                  name,
                  buildAttributes(node, json),
                  TopScope,
                  true,
                  Text(value)
                )

              }
              .toSeq

          case None =>

            val children =
              node.children.toSeq.flatMap {
                case (childName, childNode) =>
                  buildNode(
                    childName,
                    childNode,
                    json
                  )
              }

            if children.isEmpty then
              Seq.empty
            else
              Seq(
                Elem(
                  null,
                  name,
                  buildAttributes(node, json),
                  TopScope,
                  true,
                  children*
                )
              )

  private def buildAttributes(
                               node: MappingNode,
                               json: JsValue
                             ): MetaData =

    node.attributes.foldLeft(Null: MetaData) {
      case (meta, (attributeName, jsonPath)) =>

        JsonPathExtractor
          .read(json, jsonPath)
          .map { value =>

            new UnprefixedAttribute(
              attributeName,
              value,
              meta
            )
          }
          .getOrElse(meta)
    }