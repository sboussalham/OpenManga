package org.nv95.openmanga.core.storage.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by koitharu on 24.12.17.
 */

public interface Repository<T> {

	boolean add(@NonNull T t);
	boolean remove(@NonNull T t);
	boolean update(@NonNull T t);
	void clear();
	boolean contains(@NonNull T t);

	@Nullable
	List<T> query(@NonNull SqlSpecification specification);
}
