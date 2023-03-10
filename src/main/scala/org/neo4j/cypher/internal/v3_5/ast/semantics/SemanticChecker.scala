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
package org.neo4j.cypher.internal.v3_5.ast.semantics

import org.neo4j.cypher.internal.v3_5.ast.Statement
import org.neo4j.cypher.internal.v3_5.util.InternalException

object SemanticChecker {
  def check(statement: Statement, state: SemanticState = SemanticState.clean): SemanticCheckResult = {
    val result = statement.semanticCheck(state)
    val scopeTreeIssues = ScopeTreeVerifier.verify(result.state.scopeTree)
    if (scopeTreeIssues.nonEmpty)
      throw new InternalException(scopeTreeIssues.mkString(s"\n"))

    result
  }
}
