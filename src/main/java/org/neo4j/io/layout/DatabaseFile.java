//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.io.layout;

import org.neo4j.util.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public enum DatabaseFile {

    NODE_STORE("neostore.nodestore.db"),
    NODE_LABEL_STORE("neostore.nodestore.db.labels"),
    PROPERTY_STORE("neostore.propertystore.db"),
    PROPERTY_ARRAY_STORE("neostore.propertystore.db.arrays"),
    PROPERTY_STRING_STORE("neostore.propertystore.db.strings"),
    PROPERTY_KEY_TOKEN_STORE("neostore.propertystore.db.index"),
    PROPERTY_KEY_TOKEN_NAMES_STORE("neostore.propertystore.db.index.keys"),
    RELATIONSHIP_STORE("neostore.relationshipstore.db"),
    RELATIONSHIP_GROUP_STORE("neostore.relationshipgroupstore.db"),
    RELATIONSHIP_TYPE_TOKEN_STORE("neostore.relationshiptypestore.db"),
    RELATIONSHIP_TYPE_TOKEN_NAMES_STORE("neostore.relationshiptypestore.db.names"),
    LABEL_TOKEN_STORE("neostore.labeltokenstore.db"),
    LABEL_TOKEN_NAMES_STORE("neostore.labeltokenstore.db.names"),
    SCHEMA_STORE("neostore.schemastore.db"),
    COUNTS_STORES(false, new String[]{"neostore.counts.db.a", "neostore.counts.db.b"}),
    COUNTS_STORE_A(false, new String[]{"neostore.counts.db.a"}),
    COUNTS_STORE_B(false, new String[]{"neostore.counts.db.b"}),
    METADATA_STORE("neostore"),
    LABEL_SCAN_STORE(false, new String[]{"neostore.labelscanstore.db"}),
    DB_VERSION_STORE("neostore.versionStore.db"),
    Node_VERSION_LABEL_STORE("neostore.nodestore.db.versionlabels");

    private final List<String> names;
    private final boolean hasIdFile;

    private DatabaseFile(String name) {
        this(true, name);
    }

    private DatabaseFile(boolean hasIdFile, String... names) {
        this.names = Arrays.asList(names);
        this.hasIdFile = hasIdFile;
    }

    Iterable<String> getNames() {
        return this.names;
    }

    public String getName() {
        Preconditions.checkState(this.names.size() == 1, "Database file has more then one file names.");
        return (String)this.names.get(0);
    }

    boolean hasIdFile() {
        return this.hasIdFile;
    }

    public static Optional<DatabaseFile> fileOf(String name) {
        Objects.requireNonNull(name);
        DatabaseFile[] databaseFiles = values();
        DatabaseFile[] var2 = databaseFiles;
        int var3 = databaseFiles.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            DatabaseFile databaseFile = var2[var4];
            if (databaseFile.names.contains(name)) {
                return Optional.of(databaseFile);
            }
        }

        return Optional.empty();
    }
}
