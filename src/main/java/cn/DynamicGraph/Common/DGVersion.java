package cn.DynamicGraph.Common;

public class DGVersion {

    // endVersion = 0  node or rel is alive until the database boom boom boom
    // endVersion = Int.Max  whatever the endversion is

    public static long getStartVersion(long version){
        //(long)((version & 4294901760L) >> 16);
        long startVersion = (long)((version & 4294901760L) >> 16);
        return startVersion;
    }

    public static long getEndVersion(long version){
        long endVersion = (long)(version & 65535L);
        return endVersion;
    }

    public static long setStartVersion(long startVersion){

        return setStartEndVersion(startVersion, 65535L);
    }
    public static long setEndVersion(long endVersion){

        return setStartEndVersion(0, endVersion);
    }

    public static long setStartVersion(long startVersion, long version){
        return setStartEndVersion(startVersion,getEndVersion(version));
    }
    public static long setEndVersion(long endVersion, long version){
        return setStartEndVersion(getStartVersion(version),endVersion);
    }

    public static long setStartEndVersion(long startVersion, long endVersion){
        return (long)((startVersion << 16) & 4294901760L) + (endVersion & 65535L);
    }

    public static boolean hasEndVersion(long version){
        return getEndVersion(version) < 65535 ? true: false;
    }

    public static boolean hasStartVersion(long version){
        return getStartVersion(version) >= 0 ? true: false;
    }
    public static String toString(long version){
        String info = "startVersion: " + getStartVersion(version) + ",endVersion: " + getEndVersion(version);
        return info;
    }
   // public static boolean

}
