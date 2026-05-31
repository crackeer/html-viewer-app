package com.example.htmlbrowser.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ArchiveDao_Impl implements ArchiveDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ArchiveEntity> __insertionAdapterOfArchiveEntity;

  private final EntityDeletionOrUpdateAdapter<ArchiveEntity> __deletionAdapterOfArchiveEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteArchiveById;

  public ArchiveDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfArchiveEntity = new EntityInsertionAdapter<ArchiveEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `archives` (`id`,`title`,`originalFileName`,`extractPath`,`sizeBytes`,`importDate`,`lastAccessed`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ArchiveEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getOriginalFileName());
        statement.bindString(4, entity.getExtractPath());
        statement.bindLong(5, entity.getSizeBytes());
        statement.bindLong(6, entity.getImportDate());
        statement.bindLong(7, entity.getLastAccessed());
      }
    };
    this.__deletionAdapterOfArchiveEntity = new EntityDeletionOrUpdateAdapter<ArchiveEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `archives` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ArchiveEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteArchiveById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM archives WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertArchive(final ArchiveEntity archive,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfArchiveEntity.insert(archive);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteArchive(final ArchiveEntity archive,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfArchiveEntity.handle(archive);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteArchiveById(final String archiveId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteArchiveById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, archiveId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteArchiveById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ArchiveEntity>> getAllArchives() {
    final String _sql = "SELECT * FROM archives ORDER BY importDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"archives"}, new Callable<List<ArchiveEntity>>() {
      @Override
      @NonNull
      public List<ArchiveEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfOriginalFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "originalFileName");
          final int _cursorIndexOfExtractPath = CursorUtil.getColumnIndexOrThrow(_cursor, "extractPath");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfImportDate = CursorUtil.getColumnIndexOrThrow(_cursor, "importDate");
          final int _cursorIndexOfLastAccessed = CursorUtil.getColumnIndexOrThrow(_cursor, "lastAccessed");
          final List<ArchiveEntity> _result = new ArrayList<ArchiveEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ArchiveEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpOriginalFileName;
            _tmpOriginalFileName = _cursor.getString(_cursorIndexOfOriginalFileName);
            final String _tmpExtractPath;
            _tmpExtractPath = _cursor.getString(_cursorIndexOfExtractPath);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final long _tmpImportDate;
            _tmpImportDate = _cursor.getLong(_cursorIndexOfImportDate);
            final long _tmpLastAccessed;
            _tmpLastAccessed = _cursor.getLong(_cursorIndexOfLastAccessed);
            _item = new ArchiveEntity(_tmpId,_tmpTitle,_tmpOriginalFileName,_tmpExtractPath,_tmpSizeBytes,_tmpImportDate,_tmpLastAccessed);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getArchiveById(final String archiveId,
      final Continuation<? super ArchiveEntity> $completion) {
    final String _sql = "SELECT * FROM archives WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, archiveId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ArchiveEntity>() {
      @Override
      @Nullable
      public ArchiveEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfOriginalFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "originalFileName");
          final int _cursorIndexOfExtractPath = CursorUtil.getColumnIndexOrThrow(_cursor, "extractPath");
          final int _cursorIndexOfSizeBytes = CursorUtil.getColumnIndexOrThrow(_cursor, "sizeBytes");
          final int _cursorIndexOfImportDate = CursorUtil.getColumnIndexOrThrow(_cursor, "importDate");
          final int _cursorIndexOfLastAccessed = CursorUtil.getColumnIndexOrThrow(_cursor, "lastAccessed");
          final ArchiveEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpOriginalFileName;
            _tmpOriginalFileName = _cursor.getString(_cursorIndexOfOriginalFileName);
            final String _tmpExtractPath;
            _tmpExtractPath = _cursor.getString(_cursorIndexOfExtractPath);
            final long _tmpSizeBytes;
            _tmpSizeBytes = _cursor.getLong(_cursorIndexOfSizeBytes);
            final long _tmpImportDate;
            _tmpImportDate = _cursor.getLong(_cursorIndexOfImportDate);
            final long _tmpLastAccessed;
            _tmpLastAccessed = _cursor.getLong(_cursorIndexOfLastAccessed);
            _result = new ArchiveEntity(_tmpId,_tmpTitle,_tmpOriginalFileName,_tmpExtractPath,_tmpSizeBytes,_tmpImportDate,_tmpLastAccessed);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
