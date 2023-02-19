package org.neo4j.cypher.internal.v3_5.ast

import org.neo4j.cypher.internal.v3_5.ast.semantics.{SemanticCheckable, SemanticExpressionCheck}
import org.neo4j.cypher.internal.v3_5.expressions.Expression
import org.neo4j.cypher.internal.v3_5.util.symbols._
import org.neo4j.cypher.internal.v3_5.util.{ASTNode, InputPosition}

case class At(expression: Expression)(val position: InputPosition)
  extends ASTNode with SemanticCheckable {

  def dependencies = expression.dependencies

  def semanticCheck =
    SemanticExpressionCheck.simple(expression) chain
      SemanticExpressionCheck.expectType(CTInteger.covariant, expression)
}