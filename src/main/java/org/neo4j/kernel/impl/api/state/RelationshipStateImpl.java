//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.kernel.impl.api.state;

import java.util.Collections;
import java.util.Iterator;
import org.eclipse.collections.api.IntIterable;
import org.eclipse.collections.impl.factory.primitive.IntSets;
import org.neo4j.kernel.impl.util.collection.CollectionsFactory;
import org.neo4j.storageengine.api.RelationshipVisitor;
import org.neo4j.storageengine.api.StorageProperty;
import org.neo4j.storageengine.api.txstate.RelationshipState;
import org.neo4j.values.storable.Value;

class RelationshipStateImpl extends PropertyContainerStateImpl implements RelationshipState {
    static final RelationshipState EMPTY = new RelationshipState() {
        public long getId() {
            throw new UnsupportedOperationException("id not defined");
        }

        public <EX extends Exception> boolean accept(RelationshipVisitor<EX> visitor) {
            return false;
        }

        public Iterator<StorageProperty> addedProperties() {
            return Collections.emptyIterator();
        }

        public Iterator<StorageProperty> changedProperties() {
            return Collections.emptyIterator();
        }

        public IntIterable removedProperties() {
            return IntSets.immutable.empty();
        }

        public Iterator<StorageProperty> addedAndChangedProperties() {
            return Collections.emptyIterator();
        }

        public boolean hasPropertyChanges() {
            return false;
        }

        public boolean isPropertyChangedOrRemoved(int propertyKey) {
            return false;
        }

        public Value propertyValue(int propertyKey) {
            return null;
        }
    };
    private long startNode = -1L;
    private long endNode = -1L;
    private int type = -1;
    private long version;
    private boolean versionChanged;

    RelationshipStateImpl(long id, CollectionsFactory collectionsFactory) {
        super(id, collectionsFactory);
    }

    void setMetaData(long startNode, long endNode, int type) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.type = type;
    }
    long getVersion(){
        return this.version;
    }
    boolean isVersionChanged(){
        return this.versionChanged;
    }
    void setVersion(long version){
        this.version = version;
        this.versionChanged = true;
    }

    public <EX extends Exception> boolean accept(RelationshipVisitor<EX> visitor) throws EX {
        if (this.type != -1) {
            visitor.visit(this.getId(), this.type, this.startNode, this.endNode);
            return true;
        } else {
            return false;
        }
    }
}
