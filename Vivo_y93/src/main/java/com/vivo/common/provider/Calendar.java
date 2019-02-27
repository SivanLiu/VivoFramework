package com.vivo.common.provider;

import android.accounts.Account;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorEntityIterator;
import android.content.Entity;
import android.content.EntityIterator;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.SyncStateContract.Columns;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

public final class Calendar {
    public static final String AUTHORITY = "com.android.bbk.calendar";
    public static final String CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";
    public static final Uri CONTENT_URI = Uri.parse("content://com.android.bbk.calendar");
    public static final String EVENT_BEGIN_TIME = "beginTime";
    public static final String EVENT_END_TIME = "endTime";
    public static final String EVENT_REMINDER_ACTION = "android.intent.action.EVENT_REMINDER";
    public static final String TAG = "Calendar";

    public interface AttendeesColumns {
        public static final String ATTENDEE_EMAIL = "attendeeEmail";
        public static final String ATTENDEE_NAME = "attendeeName";
        public static final String ATTENDEE_RELATIONSHIP = "attendeeRelationship";
        public static final String ATTENDEE_STATUS = "attendeeStatus";
        public static final int ATTENDEE_STATUS_ACCEPTED = 1;
        public static final int ATTENDEE_STATUS_DECLINED = 2;
        public static final int ATTENDEE_STATUS_INVITED = 3;
        public static final int ATTENDEE_STATUS_NONE = 0;
        public static final int ATTENDEE_STATUS_TENTATIVE = 4;
        public static final String ATTENDEE_TYPE = "attendeeType";
        public static final String EVENT_ID = "event_id";
        public static final int RELATIONSHIP_ATTENDEE = 1;
        public static final int RELATIONSHIP_NONE = 0;
        public static final int RELATIONSHIP_ORGANIZER = 2;
        public static final int RELATIONSHIP_PERFORMER = 3;
        public static final int RELATIONSHIP_SPEAKER = 4;
        public static final int TYPE_NONE = 0;
        public static final int TYPE_OPTIONAL = 2;
        public static final int TYPE_REQUIRED = 1;
    }

    public interface EventsColumns {
        public static final String ALL_DAY = "allDay";
        public static final String CALENDAR_ID = "calendar_id";
        public static final String CAN_INVITE_OTHERS = "canInviteOthers";
        public static final String COMMENTS_URI = "commentsUri";
        public static final String DELETED = "deleted";
        public static final String DESCRIPTION = "description";
        public static final String DTEND = "dtend";
        public static final String DTSTART = "dtstart";
        public static final String DURATION = "duration";
        public static final String EVENT_LOCATION = "eventLocation";
        public static final String EVENT_TIMEZONE = "eventTimezone";
        public static final String EXDATE = "exdate";
        public static final String EXRULE = "exrule";
        public static final String GUESTS_CAN_INVITE_OTHERS = "guestsCanInviteOthers";
        public static final String GUESTS_CAN_MODIFY = "guestsCanModify";
        public static final String GUESTS_CAN_SEE_GUESTS = "guestsCanSeeGuests";
        public static final String HAS_ALARM = "hasAlarm";
        public static final String HAS_ATTENDEE_DATA = "hasAttendeeData";
        public static final String HAS_EXTENDED_PROPERTIES = "hasExtendedProperties";
        public static final String HTML_URI = "htmlUri";
        public static final String LAST_DATE = "lastDate";
        public static final String MODIFY_TIME = "modifyTime";
        public static final String ORGANIZER = "organizer";
        public static final String ORIGINAL_ALL_DAY = "originalAllDay";
        public static final String ORIGINAL_EVENT = "originalEvent";
        public static final String ORIGINAL_INSTANCE_TIME = "originalInstanceTime";
        public static final String OWNER_ACCOUNT = "ownerAccount";
        public static final String RDATE = "rdate";
        public static final String RRULE = "rrule";
        public static final String SELF_ATTENDEE_STATUS = "selfAttendeeStatus";
        public static final String STATUS = "eventStatus";
        public static final int STATUS_CANCELED = 2;
        public static final int STATUS_CONFIRMED = 1;
        public static final int STATUS_TENTATIVE = 0;
        public static final String SYNC_ADAPTER_DATA = "syncAdapterData";
        public static final String TITLE = "title";
        public static final String TRANSPARENCY = "transparency";
        public static final int TRANSPARENCY_OPAQUE = 0;
        public static final int TRANSPARENCY_TRANSPARENT = 1;
        public static final String VISIBILITY = "visibility";
        public static final int VISIBILITY_CONFIDENTIAL = 1;
        public static final int VISIBILITY_DEFAULT = 0;
        public static final int VISIBILITY_PRIVATE = 2;
        public static final int VISIBILITY_PUBLIC = 3;
    }

    public static final class Attendees implements BaseColumns, AttendeesColumns, EventsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.bbk.calendar/attendees");
    }

    public interface CalendarAlertsColumns {
        public static final String ALARM_TIME = "alarmTime";
        public static final String BEGIN = "begin";
        public static final String CREATION_TIME = "creationTime";
        public static final String DEFAULT_SORT_ORDER = "begin ASC,title ASC";
        public static final int DISMISSED = 2;
        public static final String END = "end";
        public static final String EVENT_ID = "event_id";
        public static final int FIRED = 1;
        public static final String MINUTES = "minutes";
        public static final String NOTIFY_TIME = "notifyTime";
        public static final String RECEIVED_TIME = "receivedTime";
        public static final int SCHEDULED = 0;
        public static final String STATE = "state";
    }

    public interface CalendarsColumns {
        public static final String ACCESS_LEVEL = "access_level";
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
        public static final String COLOR = "color";
        public static final int CONTRIBUTOR_ACCESS = 500;
        public static final int EDITOR_ACCESS = 600;
        public static final int FREEBUSY_ACCESS = 100;
        public static final int NO_ACCESS = 0;
        public static final int OVERRIDE_ACCESS = 400;
        public static final int OWNER_ACCESS = 700;
        public static final int READ_ACCESS = 200;
        public static final int RESPOND_ACCESS = 300;
        public static final int ROOT_ACCESS = 800;
        public static final String SELECTED = "selected";
        public static final String SYNC_EVENTS = "sync_events";
        public static final String SYNC_STATE = "sync_state";
        public static final String TIMEZONE = "timezone";
        public static final String _SYNC_ACCOUNT = "_sync_account";
        public static final String _SYNC_ACCOUNT_TYPE = "_sync_account_type";
        public static final String _SYNC_DATA = "_sync_local_id";
        public static final String _SYNC_DIRTY = "_sync_dirty";
        public static final String _SYNC_ID = "_sync_id";
        public static final String _SYNC_MARK = "_sync_mark";
        public static final String _SYNC_TIME = "_sync_time";
        public static final String _SYNC_VERSION = "_sync_version";
    }

    public static final class CalendarAlerts implements BaseColumns, CalendarAlertsColumns, EventsColumns, CalendarsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.bbk.calendar/calendar_alerts");
        public static final Uri CONTENT_URI_BY_INSTANCE = Uri.parse("content://com.android.bbk.calendar/calendar_alerts/by_instance");
        private static final boolean DEBUG = true;
        private static final String SORT_ORDER_ALARMTIME_ASC = "alarmTime ASC";
        public static final String TABLE_NAME = "CalendarAlerts";
        private static final String WHERE_ALARM_EXISTS = "event_id=? AND begin=? AND alarmTime=?";
        private static final String WHERE_FINDNEXTALARMTIME = "alarmTime>=?";
        private static final String WHERE_RESCHEDULE_MISSED_ALARMS = "state=0 AND alarmTime<? AND alarmTime>? AND end>=?";

        public static final Uri insert(ContentResolver cr, long eventId, long begin, long end, long alarmTime, int minutes) {
            ContentValues values = new ContentValues();
            values.put("event_id", Long.valueOf(eventId));
            values.put("begin", Long.valueOf(begin));
            values.put("end", Long.valueOf(end));
            values.put(CalendarAlertsColumns.ALARM_TIME, Long.valueOf(alarmTime));
            values.put(CalendarAlertsColumns.CREATION_TIME, Long.valueOf(System.currentTimeMillis()));
            values.put(CalendarAlertsColumns.RECEIVED_TIME, Integer.valueOf(0));
            values.put(CalendarAlertsColumns.NOTIFY_TIME, Integer.valueOf(0));
            values.put("state", Integer.valueOf(0));
            values.put("minutes", Integer.valueOf(minutes));
            return cr.insert(CONTENT_URI, values);
        }

        public static final Cursor query(ContentResolver cr, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            return cr.query(CONTENT_URI, projection, selection, selectionArgs, sortOrder);
        }

        public static final long findNextAlarmTime(ContentResolver cr, long millis) {
            String selection = "alarmTime>=" + millis;
            Cursor cursor = query(cr, new String[]{CalendarAlertsColumns.ALARM_TIME}, WHERE_FINDNEXTALARMTIME, new String[]{Long.toString(millis)}, SORT_ORDER_ALARMTIME_ASC);
            long alarmTime = -1;
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        alarmTime = cursor.getLong(0);
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return alarmTime;
        }

        public static final void rescheduleMissedAlarms(ContentResolver cr, Context context, AlarmManager manager) {
            long ancient = System.currentTimeMillis() - 86400000;
            ContentResolver contentResolver = cr;
            Cursor cursor = query(contentResolver, new String[]{CalendarAlertsColumns.ALARM_TIME}, WHERE_RESCHEDULE_MISSED_ALARMS, new String[]{Long.toString(now), Long.toString(ancient), Long.toString(now)}, SORT_ORDER_ALARMTIME_ASC);
            if (cursor != null) {
                Log.d(Calendar.TAG, "missed alarms found: " + cursor.getCount());
                long alarmTime = -1;
                while (cursor.moveToNext()) {
                    try {
                        long newAlarmTime = cursor.getLong(0);
                        if (alarmTime != newAlarmTime) {
                            Log.w(Calendar.TAG, "rescheduling missed alarm. alarmTime: " + newAlarmTime);
                            scheduleAlarm(context, manager, newAlarmTime);
                            alarmTime = newAlarmTime;
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }
        }

        public static void scheduleAlarm(Context context, AlarmManager manager, long alarmTime) {
            Time time = new Time();
            time.set(alarmTime);
            Log.d(Calendar.TAG, "Schedule alarm at " + alarmTime + " " + time.format(" %a, %b %d, %Y %I:%M%P"));
            if (manager == null) {
                manager = (AlarmManager) context.getSystemService("alarm");
            }
            Intent intent = new Intent(Calendar.EVENT_REMINDER_ACTION);
            intent.setData(ContentUris.withAppendedId(Calendar.CONTENT_URI, alarmTime));
            intent.putExtra(CalendarAlertsColumns.ALARM_TIME, alarmTime);
            manager.set(0, alarmTime, PendingIntent.getBroadcast(context, 0, intent, 0));
        }

        public static final boolean alarmExists(ContentResolver cr, long eventId, long begin, long alarmTime) {
            Cursor cursor = query(cr, new String[]{CalendarAlertsColumns.ALARM_TIME}, WHERE_ALARM_EXISTS, new String[]{Long.toString(eventId), Long.toString(begin), Long.toString(alarmTime)}, null);
            boolean found = false;
            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        found = DEBUG;
                    }
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return found;
        }
    }

    public interface CalendarCacheColumns {
        public static final String KEY = "key";
        public static final String VALUE = "value";
    }

    public static class CalendarCache implements CalendarCacheColumns {
        public static final String[] POJECTION = new String[]{CalendarCacheColumns.KEY, "value"};
        public static final String TIMEZONE_KEY_INSTANCES = "timezoneInstances";
        public static final String TIMEZONE_KEY_INSTANCES_PREVIOUS = "timezoneInstancesPrevious";
        public static final String TIMEZONE_KEY_TYPE = "timezoneType";
        public static final String TIMEZONE_TYPE_AUTO = "auto";
        public static final String TIMEZONE_TYPE_HOME = "home";
        public static final Uri URI = Uri.parse("content://com.android.bbk.calendar/properties");
        public static final String WHERE = "key=?";
    }

    public interface CalendarMetaDataColumns {
        public static final String LOCAL_TIMEZONE = "localTimezone";
        public static final String MAX_EVENTDAYS = "maxEventDays";
        public static final String MAX_INSTANCE = "maxInstance";
        public static final String MIN_EVENTDAYS = "minEventDays";
        public static final String MIN_INSTANCE = "minInstance";
    }

    public static final class CalendarMetaData implements CalendarMetaDataColumns {
    }

    public static class Calendars implements BaseColumns, CalendarsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.bbk.calendar/calendars");
        public static final String DEFAULT_SORT_ORDER = "displayName";
        public static final String DISPLAY_NAME = "displayName";
        public static final String HIDDEN = "hidden";
        public static final String LOCATION = "location";
        public static final String NAME = "name";
        public static final String ORGANIZER_CAN_RESPOND = "organizerCanRespond";
        public static final String OWNER_ACCOUNT = "ownerAccount";
        public static final String URL = "url";
        private static final String WHERE_DELETE_FOR_ACCOUNT = "_sync_account=? AND _sync_account_type=?";

        public static final Cursor query(ContentResolver cr, String[] projection, String where, String orderBy) {
            String str;
            Uri uri = CONTENT_URI;
            if (orderBy == null) {
                str = "displayName";
            } else {
                str = orderBy;
            }
            return cr.query(uri, projection, where, null, str);
        }

        public static int delete(ContentResolver cr, String selection, String[] selectionArgs) {
            return cr.delete(CONTENT_URI, selection, selectionArgs);
        }

        public static int deleteCalendarsForAccount(ContentResolver cr, Account account) {
            return delete(cr, WHERE_DELETE_FOR_ACCOUNT, new String[]{account.name, account.type});
        }
    }

    public interface EventDaysColumns {
        public static final String ENDDAY = "endDay";
        public static final String STARTDAY = "startDay";
    }

    public static final class EventDays implements EventDaysColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.bbk.calendar/instances/groupbyday");
        public static final String[] PROJECTION = new String[]{"startDay", "endDay"};
        public static final String SELECTION = "selected=1";

        public static final Cursor query(ContentResolver cr, int startDay, int numDays) {
            if (numDays < 1) {
                return null;
            }
            int endDay = (startDay + numDays) - 1;
            Builder builder = CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, (long) startDay);
            ContentUris.appendId(builder, (long) endDay);
            return cr.query(builder.build(), PROJECTION, SELECTION, null, "startDay");
        }
    }

    public static final class Events implements BaseColumns, EventsColumns, CalendarsColumns {
        private static final String[] ATTENDEES_COLUMNS = new String[]{AttendeesColumns.ATTENDEE_NAME, AttendeesColumns.ATTENDEE_EMAIL, AttendeesColumns.ATTENDEE_RELATIONSHIP, AttendeesColumns.ATTENDEE_TYPE, AttendeesColumns.ATTENDEE_STATUS};
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.bbk.calendar/events");
        public static final String DEFAULT_SORT_ORDER = "";
        public static final Uri DELETED_CONTENT_URI = Uri.parse("content://com.android.bbk.calendar/deleted_events");
        private static final String[] FETCH_ENTRY_COLUMNS = new String[]{CalendarsColumns._SYNC_ACCOUNT, CalendarsColumns._SYNC_ID};

        public static final Cursor query(ContentResolver cr, String[] projection) {
            return cr.query(CONTENT_URI, projection, null, null, DEFAULT_SORT_ORDER);
        }

        public static final Cursor query(ContentResolver cr, String[] projection, String where, String orderBy) {
            String str;
            Uri uri = CONTENT_URI;
            if (orderBy == null) {
                str = DEFAULT_SORT_ORDER;
            } else {
                str = orderBy;
            }
            return cr.query(uri, projection, where, null, str);
        }
    }

    public static final class EventsEntity implements BaseColumns, EventsColumns, CalendarsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.bbk.calendar/event_entities");

        private static class EntityIteratorImpl extends CursorEntityIterator {
            private static final String[] ATTENDEES_PROJECTION = new String[]{AttendeesColumns.ATTENDEE_NAME, AttendeesColumns.ATTENDEE_EMAIL, AttendeesColumns.ATTENDEE_RELATIONSHIP, AttendeesColumns.ATTENDEE_TYPE, AttendeesColumns.ATTENDEE_STATUS};
            private static final int COLUMN_ATTENDEE_EMAIL = 1;
            private static final int COLUMN_ATTENDEE_NAME = 0;
            private static final int COLUMN_ATTENDEE_RELATIONSHIP = 2;
            private static final int COLUMN_ATTENDEE_STATUS = 4;
            private static final int COLUMN_ATTENDEE_TYPE = 3;
            private static final int COLUMN_ID = 0;
            private static final int COLUMN_METHOD = 1;
            private static final int COLUMN_MINUTES = 0;
            private static final int COLUMN_NAME = 1;
            private static final int COLUMN_VALUE = 2;
            private static final String[] EXTENDED_PROJECTION = new String[]{"_id", "name", "value"};
            private static final String[] REMINDERS_PROJECTION = new String[]{"minutes", RemindersColumns.METHOD};
            private static final String WHERE_EVENT_ID = "event_id=?";
            private final ContentProviderClient mProvider;
            private final ContentResolver mResolver;

            public EntityIteratorImpl(Cursor cursor, ContentResolver resolver) {
                super(cursor);
                this.mResolver = resolver;
                this.mProvider = null;
            }

            public EntityIteratorImpl(Cursor cursor, ContentProviderClient provider) {
                super(cursor);
                this.mResolver = null;
                this.mProvider = provider;
            }

            public Entity getEntityAndIncrementCursor(Cursor cursor) throws RemoteException {
                Cursor subCursor;
                long eventId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                ContentValues cv = new ContentValues();
                cv.put("_id", Long.valueOf(eventId));
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.CALENDAR_ID);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.HTML_URI);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.TITLE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.DESCRIPTION);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.EVENT_LOCATION);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.STATUS);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.SELF_ATTENDEE_STATUS);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.COMMENTS_URI);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, EventsColumns.DTSTART);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, EventsColumns.DTEND);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.DURATION);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.EVENT_TIMEZONE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.ALL_DAY);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.VISIBILITY);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.TRANSPARENCY);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.HAS_ALARM);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.HAS_EXTENDED_PROPERTIES);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.RRULE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.RDATE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.EXRULE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.EXDATE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.ORIGINAL_EVENT);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, EventsColumns.ORIGINAL_INSTANCE_TIME);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.ORIGINAL_ALL_DAY);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, EventsColumns.LAST_DATE);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.HAS_ATTENDEE_DATA);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.GUESTS_CAN_INVITE_OTHERS);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.GUESTS_CAN_MODIFY);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.GUESTS_CAN_SEE_GUESTS);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.ORGANIZER);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarsColumns._SYNC_ID);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarsColumns._SYNC_DATA);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, CalendarsColumns._SYNC_DIRTY);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, CalendarsColumns._SYNC_VERSION);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, EventsColumns.DELETED);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, Calendars.URL);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EventsColumns.MODIFY_TIME);
                Entity entity = new Entity(cv);
                if (this.mResolver != null) {
                    subCursor = this.mResolver.query(Reminders.CONTENT_URI, REMINDERS_PROJECTION, WHERE_EVENT_ID, new String[]{Long.toString(eventId)}, null);
                } else {
                    subCursor = this.mProvider.query(Reminders.CONTENT_URI, REMINDERS_PROJECTION, WHERE_EVENT_ID, new String[]{Long.toString(eventId)}, null);
                }
                while (subCursor.moveToNext()) {
                    try {
                        ContentValues reminderValues = new ContentValues();
                        reminderValues.put("minutes", Integer.valueOf(subCursor.getInt(0)));
                        reminderValues.put(RemindersColumns.METHOD, Integer.valueOf(subCursor.getInt(1)));
                        entity.addSubValue(Reminders.CONTENT_URI, reminderValues);
                    } finally {
                        subCursor.close();
                    }
                }
                if (this.mResolver != null) {
                    subCursor = this.mResolver.query(Attendees.CONTENT_URI, ATTENDEES_PROJECTION, WHERE_EVENT_ID, new String[]{Long.toString(eventId)}, null);
                } else {
                    subCursor = this.mProvider.query(Attendees.CONTENT_URI, ATTENDEES_PROJECTION, WHERE_EVENT_ID, new String[]{Long.toString(eventId)}, null);
                }
                while (subCursor.moveToNext()) {
                    try {
                        ContentValues attendeeValues = new ContentValues();
                        attendeeValues.put(AttendeesColumns.ATTENDEE_NAME, subCursor.getString(0));
                        attendeeValues.put(AttendeesColumns.ATTENDEE_EMAIL, subCursor.getString(1));
                        attendeeValues.put(AttendeesColumns.ATTENDEE_RELATIONSHIP, Integer.valueOf(subCursor.getInt(2)));
                        attendeeValues.put(AttendeesColumns.ATTENDEE_TYPE, Integer.valueOf(subCursor.getInt(3)));
                        attendeeValues.put(AttendeesColumns.ATTENDEE_STATUS, Integer.valueOf(subCursor.getInt(4)));
                        entity.addSubValue(Attendees.CONTENT_URI, attendeeValues);
                    } finally {
                        subCursor.close();
                    }
                }
                if (this.mResolver != null) {
                    subCursor = this.mResolver.query(ExtendedProperties.CONTENT_URI, EXTENDED_PROJECTION, WHERE_EVENT_ID, new String[]{Long.toString(eventId)}, null);
                } else {
                    subCursor = this.mProvider.query(ExtendedProperties.CONTENT_URI, EXTENDED_PROJECTION, WHERE_EVENT_ID, new String[]{Long.toString(eventId)}, null);
                }
                while (subCursor.moveToNext()) {
                    try {
                        ContentValues extendedValues = new ContentValues();
                        extendedValues.put("_id", subCursor.getString(0));
                        extendedValues.put("name", subCursor.getString(1));
                        extendedValues.put("value", subCursor.getString(2));
                        entity.addSubValue(ExtendedProperties.CONTENT_URI, extendedValues);
                    } finally {
                        subCursor.close();
                    }
                }
                cursor.moveToNext();
                return entity;
            }
        }

        public static EntityIterator newEntityIterator(Cursor cursor, ContentResolver resolver) {
            return new EntityIteratorImpl(cursor, resolver);
        }

        public static EntityIterator newEntityIterator(Cursor cursor, ContentProviderClient provider) {
            return new EntityIteratorImpl(cursor, provider);
        }
    }

    public interface ExtendedPropertiesColumns {
        public static final String EVENT_ID = "event_id";
        public static final String NAME = "name";
        public static final String VALUE = "value";
    }

    public static final class ExtendedProperties implements BaseColumns, ExtendedPropertiesColumns, EventsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.bbk.calendar/extendedproperties");
    }

    public static final class Instances implements BaseColumns, EventsColumns, CalendarsColumns {
        public static final String BEGIN = "begin";
        public static final Uri CONTENT_BY_DAY_URI = Uri.parse("content://com.android.bbk.calendar/instances/whenbyday");
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.bbk.calendar/instances/when");
        public static final String DEFAULT_SORT_ORDER = "begin ASC";
        public static final String END = "end";
        public static final String END_DAY = "endDay";
        public static final String END_MINUTE = "endMinute";
        public static final String EVENT_ID = "event_id";
        public static final String SORT_CALENDAR_VIEW = "begin ASC, end DESC, title ASC";
        public static final String START_DAY = "startDay";
        public static final String START_MINUTE = "startMinute";
        private static final String WHERE_CALENDARS_SELECTED = "selected=1";

        public static final Cursor query(ContentResolver cr, String[] projection, long begin, long end) {
            Builder builder = CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, begin);
            ContentUris.appendId(builder, end);
            return cr.query(builder.build(), projection, "selected=1", null, DEFAULT_SORT_ORDER);
        }

        public static final Cursor query(ContentResolver cr, String[] projection, long begin, long end, String where, String orderBy) {
            String str;
            Builder builder = CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, begin);
            ContentUris.appendId(builder, end);
            if (TextUtils.isEmpty(where)) {
                where = "selected=1";
            } else {
                where = "(" + where + ") AND " + "selected=1";
            }
            Uri build = builder.build();
            if (orderBy == null) {
                str = DEFAULT_SORT_ORDER;
            } else {
                str = orderBy;
            }
            return cr.query(build, projection, where, null, str);
        }
    }

    public interface RemindersColumns {
        public static final String EVENT_ID = "event_id";
        public static final String METHOD = "method";
        public static final int METHOD_ALERT = 1;
        public static final int METHOD_DEFAULT = 0;
        public static final int METHOD_EMAIL = 2;
        public static final int METHOD_SMS = 3;
        public static final String MINUTES = "minutes";
        public static final int MINUTES_DEFAULT = -1;
    }

    public static final class Reminders implements BaseColumns, RemindersColumns, EventsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.bbk.calendar/reminders");
        public static final String TABLE_NAME = "Reminders";
    }

    public static final class SyncState implements Columns {
        public static final String CONTENT_DIRECTORY = "syncstate";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(Calendar.CONTENT_URI, CONTENT_DIRECTORY);

        private SyncState() {
        }
    }
}
