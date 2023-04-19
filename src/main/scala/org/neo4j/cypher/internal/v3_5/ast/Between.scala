package org.neo4j.cypher.internal.v3_5.ast

import org.neo4j.cypher.internal.v3_5.expressions.Expression
import org.neo4j.cypher.internal.v3_5.util.{ASTNode, InputPosition}

case class Between(expression: Expression)(val position: InputPosition) extends ASTNode with ASTSlicingPhrase {
  override def name = "Between" // ASTSlicingPhrase name
}