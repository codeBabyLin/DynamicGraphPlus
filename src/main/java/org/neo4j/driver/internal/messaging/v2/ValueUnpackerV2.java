//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v2;

import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.driver.internal.messaging.v1.ValueUnpackerV1;
import org.neo4j.driver.internal.packstream.PackInput;
import org.neo4j.driver.internal.types.TypeConstructor;

import java.io.IOException;
import java.time.*;

public class ValueUnpackerV2 extends ValueUnpackerV1 {
    public ValueUnpackerV2(PackInput input) {
        super(input);
    }

    protected Value unpackStruct(long size, byte type) throws IOException {
        switch(type) {
            case 68:
                this.ensureCorrectStructSize(TypeConstructor.DATE, 1, size);
                return this.unpackDate();
            case 69:
                this.ensureCorrectStructSize(TypeConstructor.DURATION, 4, size);
                return this.unpackDuration();
            case 70:
                this.ensureCorrectStructSize(TypeConstructor.DATE_TIME, 3, size);
                return this.unpackDateTimeWithZoneOffset();
            case 84:
                this.ensureCorrectStructSize(TypeConstructor.TIME, 2, size);
                return this.unpackTime();
            case 88:
                this.ensureCorrectStructSize(TypeConstructor.POINT, 3, size);
                return this.unpackPoint2D();
            case 89:
                this.ensureCorrectStructSize(TypeConstructor.POINT, 4, size);
                return this.unpackPoint3D();
            case 100:
                this.ensureCorrectStructSize(TypeConstructor.LOCAL_DATE_TIME, 2, size);
                return this.unpackLocalDateTime();
            case 102:
                this.ensureCorrectStructSize(TypeConstructor.DATE_TIME, 3, size);
                return this.unpackDateTimeWithZoneId();
            case 116:
                this.ensureCorrectStructSize(TypeConstructor.LOCAL_TIME, 1, size);
                return this.unpackLocalTime();
            default:
                return super.unpackStruct(size, type);
        }
    }

    private Value unpackDate() throws IOException {
        long epochDay = this.unpacker.unpackLong();
        return Values.value(LocalDate.ofEpochDay(epochDay));
    }

    private Value unpackTime() throws IOException {
        long nanoOfDayLocal = this.unpacker.unpackLong();
        int offsetSeconds = Math.toIntExact(this.unpacker.unpackLong());
        LocalTime localTime = LocalTime.ofNanoOfDay(nanoOfDayLocal);
        ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetSeconds);
        return Values.value(OffsetTime.of(localTime, offset));
    }

    private Value unpackLocalTime() throws IOException {
        long nanoOfDayLocal = this.unpacker.unpackLong();
        return Values.value(LocalTime.ofNanoOfDay(nanoOfDayLocal));
    }

    private Value unpackLocalDateTime() throws IOException {
        long epochSecondUtc = this.unpacker.unpackLong();
        int nano = Math.toIntExact(this.unpacker.unpackLong());
        return Values.value(LocalDateTime.ofEpochSecond(epochSecondUtc, nano, ZoneOffset.UTC));
    }

    private Value unpackDateTimeWithZoneOffset() throws IOException {
        long epochSecondLocal = this.unpacker.unpackLong();
        int nano = Math.toIntExact(this.unpacker.unpackLong());
        int offsetSeconds = Math.toIntExact(this.unpacker.unpackLong());
        return Values.value(newZonedDateTime(epochSecondLocal, (long)nano, ZoneOffset.ofTotalSeconds(offsetSeconds)));
    }

    private Value unpackDateTimeWithZoneId() throws IOException {
        long epochSecondLocal = this.unpacker.unpackLong();
        int nano = Math.toIntExact(this.unpacker.unpackLong());
        String zoneIdString = this.unpacker.unpackString();
        return Values.value(newZonedDateTime(epochSecondLocal, (long)nano, ZoneId.of(zoneIdString)));
    }

    private Value unpackDuration() throws IOException {
        long months = this.unpacker.unpackLong();
        long days = this.unpacker.unpackLong();
        long seconds = this.unpacker.unpackLong();
        int nanoseconds = Math.toIntExact(this.unpacker.unpackLong());
        return Values.isoDuration(months, days, seconds, nanoseconds);
    }

    private Value unpackPoint2D() throws IOException {
        int srid = Math.toIntExact(this.unpacker.unpackLong());
        double x = this.unpacker.unpackDouble();
        double y = this.unpacker.unpackDouble();
        return Values.point(srid, x, y);
    }

    private Value unpackPoint3D() throws IOException {
        int srid = Math.toIntExact(this.unpacker.unpackLong());
        double x = this.unpacker.unpackDouble();
        double y = this.unpacker.unpackDouble();
        double z = this.unpacker.unpackDouble();
        return Values.point(srid, x, y, z);
    }

    private static ZonedDateTime newZonedDateTime(long epochSecondLocal, long nano, ZoneId zoneId) {
        Instant instant = Instant.ofEpochSecond(epochSecondLocal, nano);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        return ZonedDateTime.of(localDateTime, zoneId);
    }
}
