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

import org.neo4j.cypher.internal.v3_5.ast.semantics.{SemanticCheck, SemanticCheckable}
import org.neo4j.cypher.internal.v3_5.util.{ASTNode, InputPosition}

sealed trait MergeAction extends ASTNode with SemanticCheckable

case class OnCreate(action: SetClause)(val position: InputPosition) extends MergeAction {
  def semanticCheck: SemanticCheck = action.semanticCheck
}

case class OnMatch(action: SetClause)(val position: InputPosition) extends MergeAction {
  def semanticCheck: SemanticCheck = action.semanticCheck
}
