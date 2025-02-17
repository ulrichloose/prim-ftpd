package org.primftpd.filepicker.nononsenseapps;

import android.net.Uri;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

/**
 * An interface for the methods required to handle backend-specific stuff.
 */
@SuppressWarnings("ALL")
public interface LogicHandler<T> {

    int VIEWTYPE_HEADER = 0;
    int VIEWTYPE_DIR = 1;
    int VIEWTYPE_CHECKABLE = 2;

    /**
     * Return true if the path is a directory and not a file.
     *
     * @param path
     */
    boolean isDir(@NonNull final T path);

    /**
     * @param path
     * @return filename of path
     */
    @NonNull
    String getName(@NonNull final T path);

    /**
     * Convert the path to a URI for the return intent
     *
     * @param path
     * @return a Uri
     */
    @NonNull
    Uri toUri(@NonNull final T path);

    /**
     * Return the path to the parent directory. Should return the root if
     * from is root.
     *
     * @param from
     */
    @NonNull
    T getParent(@NonNull final T from);

    /**
     * @param path
     * @return the full path to the file
     */
    @NonNull
    String getFullPath(@NonNull final T path);

    /**
     * Convert the path to the type used.
     *
     * @param path
     */
    @NonNull
    T getPath(@NonNull final String path);

    /**
     * Get the root path (lowest allowed).
     */
    @NonNull
    T getRoot();

    /**
     * Get a loader that lists the files in the current path,
     * and monitors changes.
     */
    @NonNull
    Loader<SortedList<T>> getLoader();

    /**
     * Bind the header ".." which goes to parent folder.
     *
     * @param viewHolder
     */
    void onBindHeaderViewHolder(@NonNull AbstractFilePickerFragment<T>.HeaderViewHolder viewHolder);

    /**
     * Header is subtracted from the position
     *
     * @param parent
     * @param viewType
     * @return a view holder for a file or directory
     */
    @NonNull
    RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    /**
     * @param viewHolder to bind data from either a file or directory
     * @param position   0 - n, where the header has been subtracted
     * @param data
     */
    void onBindViewHolder(@NonNull AbstractFilePickerFragment<T>.DirViewHolder viewHolder,
                          int position, @NonNull T data);

    /**
     * @param position 0 - n, where the header has been subtracted
     * @param data
     * @return an integer greater than 0
     */
    int getItemViewType(int position, @NonNull T data);
}
