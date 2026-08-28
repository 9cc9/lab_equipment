package org.uestc.weglas.core.util;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LocationParser {

    private static final Pattern ROOM_CODE_PATTERN = Pattern.compile("([AB]\\d{3})");

    private LocationParser() {
    }

    public static ParsedLocation parse(String rawLocation) {
        if (StringUtils.isBlank(rawLocation)) {
            return new ParsedLocation(null, rawLocation, null);
        }
        String trimmed = rawLocation.trim();
        Matcher matcher = ROOM_CODE_PATTERN.matcher(trimmed);
        String roomCode = null;
        if (matcher.find()) {
            roomCode = matcher.group(1);
        }
        String note = null;
        int noteStart = trimmed.indexOf('（');
        if (noteStart < 0) {
            noteStart = trimmed.indexOf('(');
        }
        if (noteStart >= 0) {
            note = trimmed.substring(noteStart).trim();
            if (roomCode == null) {
                roomCode = trimmed.substring(0, noteStart).trim();
            }
        } else if (roomCode == null) {
            roomCode = trimmed;
        }
        if (roomCode != null) {
            roomCode = roomCode.replace("6号科研楼", "").replace("号科研楼", "").trim();
            Matcher again = ROOM_CODE_PATTERN.matcher(roomCode);
            if (again.find()) {
                roomCode = again.group(1);
            }
        }
        return new ParsedLocation(roomCode, trimmed, note);
    }

    public static class ParsedLocation {
        private final String roomCode;
        private final String raw;
        private final String note;

        public ParsedLocation(String roomCode, String raw, String note) {
            this.roomCode = roomCode;
            this.raw = raw;
            this.note = note;
        }

        public String getRoomCode() {
            return roomCode;
        }

        public String getRaw() {
            return raw;
        }

        public String getNote() {
            return note;
        }
    }
}
