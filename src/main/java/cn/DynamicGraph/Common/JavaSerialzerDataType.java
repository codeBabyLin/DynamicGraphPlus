package cn.DynamicGraph.Common;

public class JavaSerialzerDataType{
    public static final int STRING_ID = 1;
    public static final int STRING_ARRAY_ID = -1;
    public static final int INT_ID = 2;
    public static final int INT_ARRAY_ID = -2;

    public static final int LONG_ID = 3;
    public static final int LONG_ARRAY_ID = -3;

    public static final int DOUBLE_ID = 4;
    public static final int DOUBLE_ARRAY_ID = -4;

    public static final int FLOAT_ID = 5;
    public static final int FLOAT_ARRAY_ID = -5;

    public static final int BOOLEAN_ID = 6;
    public static final int BOOLEAN_ARRAY_ID = -6;


    public static final int DATE_ID = 7;
    public static final int DATE_ARRAY_ID = -7;


    public static final int DATE_TIME_ID = 8;
    public static final int DATE_TIME_ARRAY_ID = -8;


    public static final int LOCAL_DATE_TIME_ID = 9;
    public static final int LOCAL_DATE_TIME_ARRAY_ID = -9;

    public static final int LOCAL_TIME_ID = 10;
    public static final int LOCAL_TIME_ARRAY_ID = -10;

    public static final int TIME_ID = 11;
    public static final int TIME_ARRAY_ID = -11;

    public static final int ANY_ID = 12;
    public static final int ANY_ARRAY_ID = -12;

}


/*
enum JavaSerialzerDataTypes {

    //STRING(1,"String"),
    //ARRAY_STRING(-STRING.id,"ARRAY_STRING");
    STRING(1, "String"),
    ARRAY_STRING(-STRING.id, "Array_String"),


    INT(2, "Int"),
    ARRAY_INT(-INT.id, "Array_Int"),

    LONG(3, "Long"),
    ARRAY_LONG(-LONG.id, "Array_Long"),

    DOUBLE(4, "Double"),
    ARRAY_DOUBLE(-DOUBLE.id, "Array_Double"),


    FLOAT(5, "Float"),
    ARRAY_FLOAT(-FLOAT.id, "Array_Float"),

    BOOLEAN(6, "Boolean"),
    ARRAY_BOOLEAN(-BOOLEAN.id, "Array_Boolean"),

    BLOB(7, "Blob"),
    ARRAY_BLOB(-BLOB.id, "Array_Blob"),

    DATE(8, "Date"),
    ARRAY_DATE(-DATE.id, "Array_Date"),

    DATE_TIME(9, "DateTime"),
    ARRAY_DATE_TIME(-DATE_TIME.id, "Array_Date_Time"),

    LOCAL_DATE_TIME(10, "LocalDateTime"),
    ARRAY_LOCAL_DATE_TIME(-LOCAL_DATE_TIME.id, "Array_LocalDateTime"),

    LOCAL_TIME(11, "LocalTime"),
    ARRAY_LOCAL_TIME(-LOCAL_TIME.id, "Array_LocalTime"),

    TIME(12, "Time"),
    ARRAY_TIME(-TIME.id, "Time"),

    ANY (127, "Any"),
    ARRAY_ANY(-ANY.id, "Array_Any");


    private int id;
    private String name;
    JavaSerialzerDataTypes(int id, String name){
        this.id = id;
        this.name = name;
    }
    public int id(){
        return this.id;
    }

}
*/
