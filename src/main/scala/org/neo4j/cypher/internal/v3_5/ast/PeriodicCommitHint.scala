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

import org.neo4j.cypher.internal.v3_5.ast.semantics.{SemanticCheck, SemanticCheckResult, SemanticCheckable, SemanticError}
import org.neo4j.cypher.internal.v3_5.expressions.IntegerLiteral
import org.neo4j.cypher.internal.v3_5.util.{ASTNode, InputPosition}

case class PeriodicCommitHint(size: Option[IntegerLiteral])(val position: InputPosition) extends ASTNode with SemanticCheckable {
  def name = s"USING PERIODIC COMMIT $size"

  override def semanticCheck: SemanticCheck = size match {
    case Some(integer) if integer.value <= 0 =>
      SemanticError(s"Commit size error - expected positive value larger than zero, got ${integer.value}", integer.position)
    case _ =>
      SemanticCheckResult.success
  }
}
