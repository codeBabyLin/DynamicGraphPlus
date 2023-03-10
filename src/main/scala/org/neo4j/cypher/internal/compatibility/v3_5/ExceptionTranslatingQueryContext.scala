/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compatibility.v3_5

import java.net.URL

import org.eclipse.collections.api.iterator.LongIterator
import org.neo4j.cypher.internal.planner.v3_5.spi.IndexDescriptor
import org.neo4j.cypher.internal.runtime._
import org.neo4j.cypher.internal.runtime.interpreted.{DelegatingOperations, DelegatingQueryTransactionalContext}
import org.neo4j.cypher.internal.v3_5.logical.plans.{IndexOrder, QualifiedName}
import org.neo4j.graphdb.{Path, PropertyContainer}
import org.neo4j.internal.kernel.api.helpers.RelationshipSelectionCursor
import org.neo4j.internal.kernel.api.{IndexQuery, IndexReference, NodeValueIndexCursor}
import org.neo4j.kernel.impl.api.store.RelationshipIterator
import org.neo4j.kernel.impl.core.EmbeddedProxySPI
import org.neo4j.values.AnyValue
import org.neo4j.values.storable.{TextValue, Value}
import org.neo4j.values.virtual.{ListValue, MapValue, NodeValue, RelationshipValue}
import org.neo4j.cypher.internal.v3_5.expressions.SemanticDirection

import scala.collection.Iterator

class ExceptionTranslatingQueryContext(val inner: QueryContext) extends QueryContext with ExceptionTranslationSupport {


  override def setVersion(version: Long): Unit ={
    inner.setVersion(version)
  }
  override def isVersion(): Boolean = inner.isVersion
  override def getVersion(): Long = inner.getVersion

  override def entityAccessor: EmbeddedProxySPI = inner.entityAccessor

  override def withActiveRead: QueryContext = inner.withActiveRead

  override def resources: ResourceManager = inner.resources

  override def transactionalContext =
    new ExceptionTranslatingTransactionalContext(inner.transactionalContext)

  override def setLabelsOnNode(node: Long, labelIds: Iterator[Int]): Int =
    translateException(inner.setLabelsOnNode(node, labelIds))

  override def createNode(labels: Array[Int]): NodeValue =
    translateException(inner.createNode(labels))

  override def createNodeId(labels: Array[Int]): Long =
    translateException(inner.createNodeId(labels))

  override def getLabelsForNode(node: Long): ListValue =
    translateException(inner.getLabelsForNode(node))

  override def getLabelName(id: Int): String =
    translateException(inner.getLabelName(id))

  override def getOptLabelId(labelName: String): Option[Int] =
    translateException(inner.getOptLabelId(labelName))

  override def getLabelId(labelName: String): Int =
    translateException(inner.getLabelId(labelName))

  override def getOrCreateLabelId(labelName: String): Int =
    translateException(inner.getOrCreateLabelId(labelName))

  override def nodeOps: Operations[NodeValue] =
    new ExceptionTranslatingOperations[NodeValue](inner.nodeOps)

  override def relationshipOps: Operations[RelationshipValue] =
    new ExceptionTranslatingOperations[RelationshipValue](inner.relationshipOps)

  override def removeLabelsFromNode(node: Long, labelIds: Iterator[Int]): Int =
    translateException(inner.removeLabelsFromNode(node, labelIds))

  override def getPropertyKeyName(propertyKeyId: Int): String =
    translateException(inner.getPropertyKeyName(propertyKeyId))

  override def getOptPropertyKeyId(propertyKeyName: String): Option[Int] =
    translateException(inner.getOptPropertyKeyId(propertyKeyName))

  override def getPropertyKeyId(propertyKey: String): Int =
    translateException(inner.getPropertyKeyId(propertyKey))

  override def getOrCreatePropertyKeyId(propertyKey: String): Int =
    translateException(inner.getOrCreatePropertyKeyId(propertyKey))

  override def getOrCreatePropertyKeyIds(propertyKeys: Array[String]): Array[Int] =
    translateException(inner.getOrCreatePropertyKeyIds(propertyKeys))

  override def addIndexRule(descriptor: IndexDescriptor) =
    translateException(inner.addIndexRule(descriptor))

  override def dropIndexRule(descriptor: IndexDescriptor) =
    translateException(inner.dropIndexRule(descriptor))

  override def indexSeek[RESULT <: AnyRef](index: IndexReference,
                                           needsValues: Boolean,
                                           indexOrder: IndexOrder,
                                           values: Seq[IndexQuery]): NodeValueIndexCursor =
    translateException(inner.indexSeek(index, needsValues, indexOrder, values))

  override def getNodesByLabel(id: Int): Iterator[NodeValue] =
    translateException(inner.getNodesByLabel(id))

  override def getNodesByLabelPrimitive(id: Int): LongIterator =
    translateException(inner.getNodesByLabelPrimitive(id))


  override def nodeAsMap(id: Long): MapValue = translateException(inner.nodeAsMap(id))

  override def relationshipAsMap(id: Long): MapValue = translateException(inner.relationshipAsMap(id))

  override def nodeGetOutgoingDegree(node: Long): Int =
    translateException(inner.nodeGetOutgoingDegree(node))

  override def nodeGetOutgoingDegree(node: Long, relationship: Int): Int =
    translateException(inner.nodeGetOutgoingDegree(node, relationship))

  override def nodeGetIncomingDegree(node: Long): Int =
    translateException(inner.nodeGetIncomingDegree(node))

  override def nodeGetIncomingDegree(node: Long, relationship: Int): Int =
    translateException(inner.nodeGetIncomingDegree(node, relationship))

  override def nodeGetTotalDegree(node: Long): Int = translateException(inner.nodeGetTotalDegree(node))

  override def nodeGetTotalDegree(node: Long, relationship: Int): Int =
    translateException(inner.nodeGetTotalDegree(node, relationship))

  override def createNodeKeyConstraint(descriptor: IndexDescriptor): Boolean =
    translateException(inner.createNodeKeyConstraint(descriptor))

  override def dropNodeKeyConstraint(descriptor: IndexDescriptor) =
    translateException(inner.dropNodeKeyConstraint(descriptor))

  override def createUniqueConstraint(descriptor: IndexDescriptor): Boolean =
    translateException(inner.createUniqueConstraint(descriptor))

  override def dropUniqueConstraint(descriptor: IndexDescriptor) =
    translateException(inner.dropUniqueConstraint(descriptor))

  override def createNodePropertyExistenceConstraint(labelId: Int, propertyKeyId: Int): Boolean =
    translateException(inner.createNodePropertyExistenceConstraint(labelId, propertyKeyId))

  override def dropNodePropertyExistenceConstraint(labelId: Int, propertyKeyId: Int) =
    translateException(inner.dropNodePropertyExistenceConstraint(labelId, propertyKeyId))

  override def createRelationshipPropertyExistenceConstraint(relTypeId: Int, propertyKeyId: Int): Boolean =
    translateException(inner.createRelationshipPropertyExistenceConstraint(relTypeId, propertyKeyId))

  override def dropRelationshipPropertyExistenceConstraint(relTypeId: Int, propertyKeyId: Int) =
    translateException(inner.dropRelationshipPropertyExistenceConstraint(relTypeId, propertyKeyId))

  override def callReadOnlyProcedure(id: Int, args: Seq[Any], allowed: Array[String]): Iterator[Array[AnyRef]] =
    translateIterator(inner.callReadOnlyProcedure(id, args, allowed))

  override def callReadWriteProcedure(id: Int, args: Seq[Any], allowed: Array[String]): Iterator[Array[AnyRef]] =
    translateIterator(inner.callReadWriteProcedure(id, args, allowed))

  override def callSchemaWriteProcedure(id: Int, args: Seq[Any], allowed: Array[String]): Iterator[Array[AnyRef]] =
    translateIterator(inner.callSchemaWriteProcedure(id, args, allowed))

  override def callDbmsProcedure(id: Int, args: Seq[Any], allowed: Array[String]): Iterator[Array[AnyRef]] =
    translateIterator(inner.callDbmsProcedure(id, args, allowed))

  override def callReadOnlyProcedure(name: QualifiedName, args: Seq[Any], allowed: Array[String]): Iterator[Array[AnyRef]] =
    translateIterator(inner.callReadOnlyProcedure(name, args, allowed))

  override def callReadWriteProcedure(name: QualifiedName, args: Seq[Any], allowed: Array[String]): Iterator[Array[AnyRef]] =
    translateIterator(inner.callReadWriteProcedure(name, args, allowed))

  override def callSchemaWriteProcedure(name: QualifiedName, args: Seq[Any], allowed: Array[String]): Iterator[Array[AnyRef]] =
    translateIterator(inner.callSchemaWriteProcedure(name, args, allowed))

  override def callDbmsProcedure(name: QualifiedName, args: Seq[Any], allowed: Array[String]): Iterator[Array[AnyRef]] =
    translateIterator(inner.callDbmsProcedure(name, args, allowed))

  override def callFunction(id: Int, args: Seq[AnyValue], allowed: Array[String]) =
    translateException(inner.callFunction(id, args, allowed))

  override def callFunction(name: QualifiedName, args: Seq[AnyValue], allowed: Array[String]) =
    translateException(inner.callFunction(name, args, allowed))

  override def aggregateFunction(id: Int,
                                 allowed: Array[String]): UserDefinedAggregator =
    translateException(inner.aggregateFunction(id, allowed))

  override def aggregateFunction(name: QualifiedName,
                                 allowed: Array[String]): UserDefinedAggregator =
    translateException(inner.aggregateFunction(name, allowed))

  override def withAnyOpenQueryContext[T](work: (QueryContext) => T): T =
    inner.withAnyOpenQueryContext(qc =>
      translateException(
        work(new ExceptionTranslatingQueryContext(qc))
      ))

  override def isLabelSetOnNode(label: Int, node: Long): Boolean =
    translateException(inner.isLabelSetOnNode(label, node))

  override def getRelTypeId(relType: String) =
    translateException(inner.getRelTypeId(relType))

  override def getRelTypeName(id: Int) =
    translateException(inner.getRelTypeName(id))

  override def lockingUniqueIndexSeek[RESULT](index: IndexReference,
                                              values: Seq[IndexQuery.ExactPredicate]): NodeValueIndexCursor =
    translateException(inner.lockingUniqueIndexSeek(index, values))

  override def getImportURL(url: URL) =
    translateException(inner.getImportURL(url))

  override def relationshipGetStartNode(relationships: RelationshipValue) =
    translateException(inner.relationshipGetStartNode(relationships))

  override def relationshipGetEndNode(relationships: RelationshipValue) =
    translateException(inner.relationshipGetEndNode(relationships))

  override def createRelationship(start: Long, end: Long, relType: Int) =
    translateException(inner.createRelationship(start, end, relType))

  override def getOrCreateRelTypeId(relTypeName: String) =
    translateException(inner.getOrCreateRelTypeId(relTypeName))

  override def getRelationshipsForIds(node: Long, dir: SemanticDirection, types: Option[Array[Int]]) =
    translateException(inner.getRelationshipsForIds(node, dir, types))

  override def getRelationshipsForIdsPrimitive(node: Long, dir: SemanticDirection, types: Option[Array[Int]]): RelationshipIterator =
    translateException(inner.getRelationshipsForIdsPrimitive(node, dir, types))

  override def getRelationshipsCursor(node: Long, dir: SemanticDirection, types: Option[Array[Int]]): RelationshipSelectionCursor =
    translateException(inner.getRelationshipsCursor(node, dir, types))

  override def getRelationshipFor(relationshipId: Long, typeId: Int, startNodeId: Long, endNodeId: Long): RelationshipValue =
    translateException(inner.getRelationshipFor(relationshipId, typeId, startNodeId, endNodeId))

  override def indexSeekByContains[RESULT <: AnyRef](index: IndexReference,
                                                     needsValues: Boolean,
                                                     indexOrder: IndexOrder,
                                                     value: TextValue): NodeValueIndexCursor =
    translateException(inner.indexSeekByContains(index, needsValues, indexOrder, value))

  override def indexSeekByEndsWith[RESULT <: AnyRef](index: IndexReference,
                                                     needsValues: Boolean,
                                                     indexOrder: IndexOrder,
                                                     value: TextValue): NodeValueIndexCursor =
    translateException(inner.indexSeekByEndsWith(index, needsValues, indexOrder, value))

  override def indexScan[RESULT <: AnyRef](index: IndexReference,
                                           needsValues: Boolean,
                                           indexOrder: IndexOrder): NodeValueIndexCursor =
    translateException(inner.indexScan(index, needsValues, indexOrder))

  override def nodeIsDense(node: Long) =
    translateException(inner.nodeIsDense(node))

  override def asObject(value: AnyValue): AnyRef =
    translateException(inner.asObject(value))

  override def variableLengthPathExpand(realNode: Long, minHops: Option[Int], maxHops: Option[Int], direction: SemanticDirection, relTypes: Seq[String]) =
    translateException(inner.variableLengthPathExpand(realNode, minHops, maxHops, direction, relTypes))

  override def singleShortestPath(left: Long, right: Long, depth: Int, expander: Expander, pathPredicate: KernelPredicate[Path], filters: Seq[KernelPredicate[PropertyContainer]]) =
    translateException(inner.singleShortestPath(left, right, depth, expander, pathPredicate, filters))

  override def allShortestPath(left: Long, right: Long, depth: Int, expander: Expander, pathPredicate: KernelPredicate[Path], filters: Seq[KernelPredicate[PropertyContainer]]) =
    translateException(inner.allShortestPath(left, right, depth, expander, pathPredicate, filters))

  override def nodeCountByCountStore(labelId: Int) =
    translateException(inner.nodeCountByCountStore(labelId))

  override def relationshipCountByCountStore(startLabelId: Int, typeId: Int, endLabelId: Int) =
    translateException(inner.relationshipCountByCountStore(startLabelId, typeId, endLabelId))

  override def lockNodes(nodeIds: Long*) =
    translateException(inner.lockNodes(nodeIds:_*))

  override def lockRelationships(relIds: Long*) =
    translateException(inner.lockRelationships(relIds:_*))

  override def getOptRelTypeId(relType: String) =
    translateException(inner.getOptRelTypeId(relType))

  override def detachDeleteNode(node: Long): Int =
    translateException(inner.detachDeleteNode(node))

  override def assertSchemaWritesAllowed(): Unit = translateException(inner.assertSchemaWritesAllowed())

  class ExceptionTranslatingOperations[T](inner: Operations[T])
    extends DelegatingOperations[T](inner) {
    override def delete(id: Long) =
      translateException(inner.delete(id))

    override def setProperty(id: Long, propertyKey: Int, value: Value) =
      translateException(inner.setProperty(id, propertyKey, value))

    override def getById(id: Long): T =
      translateException(inner.getById(id))

    override def getProperty(id: Long, propertyKeyId: Int): Value =
      translateException(inner.getProperty(id, propertyKeyId))

    override def hasProperty(id: Long, propertyKeyId: Int): Boolean =
      translateException(inner.hasProperty(id, propertyKeyId))

    override def propertyKeyIds(id: Long): Array[Int] =
      translateException(inner.propertyKeyIds(id))

    override def removeProperty(id: Long, propertyKeyId: Int) =
      translateException(inner.removeProperty(id, propertyKeyId))

    override def all: Iterator[T] =
      translateException(inner.all)

    override def allPrimitive: LongIterator =
      translateException(inner.allPrimitive)

    override def isDeletedInThisTx(id: Long): Boolean =
      translateException(inner.isDeletedInThisTx(id))

    override def getByIdIfExists(id: Long): Option[T] =
      translateException(inner.getByIdIfExists(id))
  }

  class ExceptionTranslatingTransactionalContext(inner: QueryTransactionalContext) extends DelegatingQueryTransactionalContext(inner) {
    override def close(success: Boolean) { translateException(super.close(success)) }
  }

  override def createNewQueryContext() = new ExceptionTranslatingQueryContext(inner.createNewQueryContext())

  override def indexReference(label: Int, properties: Int*): IndexReference =
    translateException(inner.indexReference(label, properties:_*))
}

