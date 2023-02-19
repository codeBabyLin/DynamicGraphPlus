//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.io.layout;

import org.neo4j.helpers.collection.Iterables;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.io.layout.DatabaseFile;
import org.neo4j.io.layout.StoreLayout;
import org.neo4j.stream.Streams;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatabaseLayout {
    private static final File[] EMPTY_FILES_ARRAY = new File[0];
    private final File databaseDirectory;
    private final StoreLayout storeLayout;
    private final String databaseName;

    public static DatabaseLayout of(StoreLayout storeLayout, String databaseName) {
        return new DatabaseLayout(storeLayout, databaseName);
    }

    public static DatabaseLayout of(File databaseDirectory) {
        File canonicalFile = FileUtils.getCanonicalFile(databaseDirectory);
        return of(canonicalFile.getParentFile(), canonicalFile.getName());
    }

    public static DatabaseLayout of(File rootDirectory, String databaseName) {
        return new DatabaseLayout(StoreLayout.of(rootDirectory), databaseName);
    }

    private DatabaseLayout(StoreLayout storeLayout, String databaseName) {
        this.storeLayout = storeLayout;
        this.databaseDirectory = new File(storeLayout.storeDirectory(), databaseName);
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public StoreLayout getStoreLayout() {
        return this.storeLayout;
    }

    public File databaseDirectory() {
        return this.databaseDirectory;
    }

    public File metadataStore() {
        return this.file(DatabaseFile.METADATA_STORE.getName());
    }

    public File labelScanStore() {
        return this.file(DatabaseFile.LABEL_SCAN_STORE.getName());
    }

    public File countStoreA() {
        return this.file(DatabaseFile.COUNTS_STORE_A.getName());
    }

    public File countStoreB() {
        return this.file(DatabaseFile.COUNTS_STORE_B.getName());
    }

    public File propertyStringStore() {
        return this.file(DatabaseFile.PROPERTY_STRING_STORE.getName());
    }

    public File relationshipStore() {
        return this.file(DatabaseFile.RELATIONSHIP_STORE.getName());
    }

    public File propertyStore() {
        return this.file(DatabaseFile.PROPERTY_STORE.getName());
    }

    public File nodeStore() {
        return this.file(DatabaseFile.NODE_STORE.getName());
    }

    public File nodeLabelStore() {
        return this.file(DatabaseFile.NODE_LABEL_STORE.getName());
    }

    //Dynamic
    //***********************************************************

    public File nodeVersionLabelStore(){return this.file(DatabaseFile.Node_VERSION_LABEL_STORE.getName());}

    public File dbVersionStore(){
        return this.file(DatabaseFile.DB_VERSION_STORE.getName());
    }
    public File idDbVersionStore() {
        return this.idFile(DatabaseFile.DB_VERSION_STORE.getName());
    }

    //Dynamic
    //***********************************************************

    public File propertyArrayStore() {
        return this.file(DatabaseFile.PROPERTY_ARRAY_STORE.getName());
    }

    public File propertyKeyTokenStore() {
        return this.file(DatabaseFile.PROPERTY_KEY_TOKEN_STORE.getName());
    }

    public File propertyKeyTokenNamesStore() {
        return this.file(DatabaseFile.PROPERTY_KEY_TOKEN_NAMES_STORE.getName());
    }

    public File relationshipTypeTokenStore() {
        return this.file(DatabaseFile.RELATIONSHIP_TYPE_TOKEN_STORE.getName());
    }

    public File relationshipTypeTokenNamesStore() {
        return this.file(DatabaseFile.RELATIONSHIP_TYPE_TOKEN_NAMES_STORE.getName());
    }

    public File labelTokenStore() {
        return this.file(DatabaseFile.LABEL_TOKEN_STORE.getName());
    }

    public File schemaStore() {
        return this.file(DatabaseFile.SCHEMA_STORE.getName());
    }

    public File relationshipGroupStore() {
        return this.file(DatabaseFile.RELATIONSHIP_GROUP_STORE.getName());
    }

    public File labelTokenNamesStore() {
        return this.file(DatabaseFile.LABEL_TOKEN_NAMES_STORE.getName());
    }

    public Set<File> idFiles() {
        return (Set)Arrays.stream(DatabaseFile.values()).filter(DatabaseFile::hasIdFile).flatMap((value) -> {
            return Streams.ofOptional(this.idFile(value));
        }).collect(Collectors.toSet());
    }

    public Set<File> storeFiles() {
        return (Set)Arrays.stream(DatabaseFile.values()).flatMap(this::file).collect(Collectors.toSet());
    }

    public Optional<File> idFile(DatabaseFile file) {
        return file.hasIdFile() ? Optional.of(this.idFile(file.getName())) : Optional.empty();
    }

    public File file(String fileName) {
        return new File(this.databaseDirectory, fileName);
    }

    public Stream<File> file(DatabaseFile databaseFile) {
        Iterable<String> names = databaseFile.getNames();
        return Iterables.stream(names).map(this::file);
    }

    public File[] listDatabaseFiles(FilenameFilter filter) {
        File[] files = this.databaseDirectory.listFiles(filter);
        return files != null ? files : EMPTY_FILES_ARRAY;
    }

    public File idMetadataStore() {
        return this.idFile(DatabaseFile.METADATA_STORE.getName());
    }

    public File idNodeStore() {
        return this.idFile(DatabaseFile.NODE_STORE.getName());
    }

    public File idNodeLabelStore() {
        return this.idFile(DatabaseFile.NODE_LABEL_STORE.getName());
    }

    public File idPropertyStore() {
        return this.idFile(DatabaseFile.PROPERTY_STORE.getName());
    }

    public File idPropertyKeyTokenStore() {
        return this.idFile(DatabaseFile.PROPERTY_KEY_TOKEN_STORE.getName());
    }

    public File idPropertyKeyTokenNamesStore() {
        return this.idFile(DatabaseFile.PROPERTY_KEY_TOKEN_NAMES_STORE.getName());
    }

    public File idPropertyStringStore() {
        return this.idFile(DatabaseFile.PROPERTY_STRING_STORE.getName());
    }

    public File idPropertyArrayStore() {
        return this.idFile(DatabaseFile.PROPERTY_ARRAY_STORE.getName());
    }

    public File idRelationshipStore() {
        return this.idFile(DatabaseFile.RELATIONSHIP_STORE.getName());
    }

    public File idRelationshipGroupStore() {
        return this.idFile(DatabaseFile.RELATIONSHIP_GROUP_STORE.getName());
    }

    public File idRelationshipTypeTokenStore() {
        return this.idFile(DatabaseFile.RELATIONSHIP_TYPE_TOKEN_STORE.getName());
    }

    public File idRelationshipTypeTokenNamesStore() {
        return this.idFile(DatabaseFile.RELATIONSHIP_TYPE_TOKEN_NAMES_STORE.getName());
    }

    public File idLabelTokenStore() {
        return this.idFile(DatabaseFile.LABEL_TOKEN_STORE.getName());
    }

    public File idLabelTokenNamesStore() {
        return this.idFile(DatabaseFile.LABEL_TOKEN_NAMES_STORE.getName());
    }

    public File idSchemaStore() {
        return this.idFile(DatabaseFile.SCHEMA_STORE.getName());
    }

    private File idFile(String name) {
        return this.file(idFileName(name));
    }

    private static String idFileName(String storeName) {
        return storeName + ".id";
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.databaseDirectory, this.storeLayout});
    }

    public String toString() {
        return String.valueOf(this.databaseDirectory);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            DatabaseLayout that = (DatabaseLayout)o;
            return Objects.equals(this.databaseDirectory, that.databaseDirectory) && Objects.equals(this.storeLayout, that.storeLayout);
        } else {
            return false;
        }
    }
}
