/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
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
package org.neo4j.cypher.internal.v3_5.ast

import org.neo4j.cypher.internal.v3_5.ast.semantics.{SemanticCheckable, SemanticExpressionCheck}
import org.neo4j.cypher.internal.v3_5.expressions.{LabelName, LogicalProperty, LogicalVariable}
import org.neo4j.cypher.internal.v3_5.util.symbols._
import org.neo4j.cypher.internal.v3_5.util.{ASTNode, InputPosition}

sealed trait RemoveItem extends ASTNode with SemanticCheckable

case class RemoveLabelItem(variable: LogicalVariable, labels: Seq[LabelName])(val position: InputPosition) extends RemoveItem {
  def semanticCheck =
    SemanticExpressionCheck.simple(variable) chain
    SemanticExpressionCheck.expectType(CTNode.covariant, variable)
}

case class RemovePropertyItem(property: LogicalProperty) extends RemoveItem {
  def position = property.position

  def semanticCheck = SemanticExpressionCheck.simple(property)
}
