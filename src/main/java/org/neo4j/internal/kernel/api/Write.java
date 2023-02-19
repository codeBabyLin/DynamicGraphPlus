//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.internal.kernel.api;

import org.neo4j.internal.kernel.api.exceptions.EntityNotFoundException;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.internal.kernel.api.exceptions.explicitindex.AutoIndexingKernelException;
import org.neo4j.internal.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo4j.values.storable.Value;

public interface Write {

    //DynamicGraph
    long nodeCreateWithVersion(long version);

    long nodeCreateWithLabelsWithVersion(int[] var1, long version) throws ConstraintValidationException;


    boolean nodeDeleteWithVersion(long var1, long version) throws AutoIndexingKernelException;

    //int nodeDetachDelete(long var1) throws KernelException;

    long relationshipCreateWithVersion(long var1, int var3, long var4, long version) throws EntityNotFoundException;

    boolean relationshipDeleteWithVersion(long var1, long version) throws AutoIndexingKernelException;

    boolean nodeAddLabelWithVersion(long var1, int var3, long version) throws KernelException;

    boolean nodeRemoveLabelWithVersion(long var1, int var3, long version) throws EntityNotFoundException;

    Value nodeSetPropertyWithVersion(long var1, int var3, Value var4, long version) throws KernelException;

    Value nodeRemovePropertyWithVersion(long var1, int var3, long version) throws EntityNotFoundException, AutoIndexingKernelException;

    Value relationshipSetPropertyWithVersion(long var1, int var3, Value var4, long version) throws EntityNotFoundException, AutoIndexingKernelException;

    Value relationshipRemovePropertyWithVersion(long var1, int var3, long version) throws EntityNotFoundException, AutoIndexingKernelException;

    //DynamicGraph



    long nodeCreate();

    long nodeCreateWithLabels(int[] var1) throws ConstraintValidationException;



    boolean nodeDelete(long var1) throws AutoIndexingKernelException;

    int nodeDetachDelete(long var1) throws KernelException;

    long relationshipCreate(long var1, int var3, long var4) throws EntityNotFoundException;

    boolean relationshipDelete(long var1) throws AutoIndexingKernelException;

    boolean nodeAddLabel(long var1, int var3) throws KernelException;

    boolean nodeRemoveLabel(long var1, int var3) throws EntityNotFoundException;

    Value nodeSetProperty(long var1, int var3, Value var4) throws KernelException;

    Value nodeRemoveProperty(long var1, int var3) throws EntityNotFoundException, AutoIndexingKernelException;

    Value relationshipSetProperty(long var1, int var3, Value var4) throws EntityNotFoundException, AutoIndexingKernelException;

    Value relationshipRemoveProperty(long var1, int var3) throws EntityNotFoundException, AutoIndexingKernelException;

    Value graphSetProperty(int var1, Value var2);

    Value graphRemoveProperty(int var1);
}
