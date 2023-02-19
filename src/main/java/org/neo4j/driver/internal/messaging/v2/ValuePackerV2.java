//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.neo4j.driver.internal.messaging.v2;

import org.neo4j.driver.internal.InternalPoint2D;
import org.neo4j.driver.internal.InternalPoint3D;
import org.neo4j.driver.internal.messaging.v1.ValuePackerV1;
import org.neo4j.driver.internal.packstream.PackOutput;
import org.neo4j.driver.internal.types.TypeConstructor;
import org.neo4j.driver.internal.value.InternalValue;
import org.neo4j.driver.types.IsoDuration;
import org.neo4j.driver.types.Point;

import java.io.IOException;
import java.time.*;

public class ValuePackerV2 extends ValuePackerV1 {
    public ValuePackerV2(PackOutput output) {
        super(output, true);
    }

    protected void packInternalValue(InternalValue value) throws IOException {
        TypeConstructor typeConstructor = value.typeConstructor();
        switch(typeConstructor) {
            case DATE:
                this.packDate(value.asLocalDate());
                break;
            case TIME:
                this.packTime(value.asOffsetTime());
                break;
            case LOCAL_TIME:
                this.packLocalTime(value.asLocalTime());
                break;
            case LOCAL_DATE_TIME:
                this.packLocalDateTime(value.asLocalDateTime());
                break;
            case DATE_TIME:
                this.packZonedDateTime(value.asZonedDateTime());
                break;
            case DURATION:
                this.packDuration(value.asIsoDuration());
                break;
            case POINT:
                this.packPoint(value.asPoint());
                break;
            default:
                super.packInternalValue(value);
        }

    }

    private void packDate(LocalDate localDate) throws IOException {
        this.packer.packStructHeader(1, (byte)68);
        this.packer.pack(localDate.toEpochDay());
    }

    private void packTime(OffsetTime offsetTime) throws IOException {
        long nanoOfDayLocal = offsetTime.toLocalTime().toNanoOfDay();
        int offsetSeconds = offsetTime.getOffset().getTotalSeconds();
        this.packer.packStructHeader(2, (byte)84);
        this.packer.pack(nanoOfDayLocal);
        this.packer.pack((long)offsetSeconds);
    }

    private void packLocalTime(LocalTime localTime) throws IOException {
        this.packer.packStructHeader(1, (byte)116);
        this.packer.pack(localTime.toNanoOfDay());
    }

    private void packLocalDateTime(LocalDateTime localDateTime) throws IOException {
        long epochSecondUtc = localDateTime.toEpochSecond(ZoneOffset.UTC);
        int nano = localDateTime.getNano();
        this.packer.packStructHeader(2, (byte)100);
        this.packer.pack(epochSecondUtc);
        this.packer.pack((long)nano);
    }

    private void packZonedDateTime(ZonedDateTime zonedDateTime) throws IOException {
        long epochSecondLocal = zonedDateTime.toLocalDateTime().toEpochSecond(ZoneOffset.UTC);
        int nano = zonedDateTime.getNano();
        ZoneId zone = zonedDateTime.getZone();
        if (zone instanceof ZoneOffset) {
            int offsetSeconds = ((ZoneOffset)zone).getTotalSeconds();
            this.packer.packStructHeader(3, (byte)70);
            this.packer.pack(epochSecondLocal);
            this.packer.pack((long)nano);
            this.packer.pack((long)offsetSeconds);
        } else {
            String zoneId = zone.getId();
            this.packer.packStructHeader(3, (byte)102);
            this.packer.pack(epochSecondLocal);
            this.packer.pack((long)nano);
            this.packer.pack(zoneId);
        }

    }

    private void packDuration(IsoDuration duration) throws IOException {
        this.packer.packStructHeader(4, (byte)69);
        this.packer.pack(duration.months());
        this.packer.pack(duration.days());
        this.packer.pack(duration.seconds());
        this.packer.pack((long)duration.nanoseconds());
    }

    private void packPoint(Point point) throws IOException {
        if (point instanceof InternalPoint2D) {
            this.packPoint2D(point);
        } else {
            if (!(point instanceof InternalPoint3D)) {
                throw new IOException(String.format("Unknown type: type: %s, value: %s", point.getClass(), point.toString()));
            }

            this.packPoint3D(point);
        }

    }

    private void packPoint2D(Point point) throws IOException {
        this.packer.packStructHeader(3, (byte)88);
        this.packer.pack((long)point.srid());
        this.packer.pack(point.x());
        this.packer.pack(point.y());
    }

    private void packPoint3D(Point point) throws IOException {
        this.packer.packStructHeader(4, (byte)89);
        this.packer.pack((long)point.srid());
        this.packer.pack(point.x());
        this.packer.pack(point.y());
        this.packer.pack(point.z());
    }
}
