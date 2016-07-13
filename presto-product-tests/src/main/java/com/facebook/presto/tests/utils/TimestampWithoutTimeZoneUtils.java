/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.tests.utils;

import java.sql.Timestamp;
import java.time.Instant;

public class TimestampWithoutTimeZoneUtils
{
    private TimestampWithoutTimeZoneUtils()
    {}

    /**
     * @param value string with date and time in following format: yyyy-mm-ddThh:mm:ssZ (eg. "2000-01-01T01:23:45Z")
     * @return valid java.sql.Timestamp representation of Timestamp without time zone
     */
    public static Timestamp getTimestampWithoutTimeZoneRepresentation(String value)
    {
        return new Timestamp(Instant.parse(value).getEpochSecond() * 1000);
    }
}