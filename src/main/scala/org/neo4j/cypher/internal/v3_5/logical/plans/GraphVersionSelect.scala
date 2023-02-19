package org.neo4j.cypher.internal.v3_5.logical.plans

import org.neo4j.cypher.internal.v3_5.expressions.{Ands, Expression}
import org.neo4j.cypher.internal.v3_5.util.attribution.IdGen


case class GraphVersionSelect(version: Expression,
                     source: LogicalPlan
                    )(implicit idGen: IdGen) extends LogicalPlan(idGen) with LazyLogicalPlan {
  //assert(Expression, "A selection plan should never be created without predicates")

  val lhs = Some(source)
  def rhs = None

  //def numPredicates: Int = predicate.exprs.size

  val availableSymbols: Set[String] = source.availableSymbols
}